"""
Executa o roteiro de demonstração contra a API em execução (http://localhost:8080) e grava
as requisições/respostas em docs/evidencias/execucao-endpoints.md.

Uso:  python scripts/gerar_evidencias.py   (com a API rodando e os dados de demonstração carregados)
"""
import datetime as dt
import json
import pathlib
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
SAIDA = pathlib.Path(__file__).resolve().parent.parent / "docs" / "evidencias" / "execucao-endpoints.md"


def proxima(dia_semana: int) -> dt.date:
    hoje = dt.date.today()
    dias = (dia_semana - hoje.weekday()) % 7 or 7
    return hoje + dt.timedelta(days=dias)


def chamar(metodo, caminho, corpo=None):
    dados = json.dumps(corpo).encode() if corpo is not None else None
    req = urllib.request.Request(BASE + caminho, data=dados, method=metodo,
                                 headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as erro:
        return erro.code, erro.read().decode()


def main():
    segunda = proxima(0)
    terca = proxima(1)
    passos = []

    def passo(titulo, metodo, caminho, corpo=None):
        status, texto = chamar(metodo, caminho, corpo)
        try:
            resposta = json.loads(texto) if texto else None
        except json.JSONDecodeError:
            resposta = texto
        passos.append((titulo, metodo, caminho, corpo, status, resposta))
        return resposta

    passo("Listar profissionais de psicologia", "GET", "/api/v1/profissionais?especialidade=PSICOLOGIA")
    passo("Consultar horários livres da psicóloga", "GET", f"/api/v1/profissionais/1/horarios-livres?data={segunda}")
    criado = passo("Agendar atendimento (sucesso)", "POST", "/api/v1/agendamentos", {
        "beneficiarioId": 4, "profissionalId": 1, "inicio": f"{segunda}T08:50:00",
        "observacao": "Encaminhado pela assistente social"})
    passo("Tentar agendar o mesmo horário (conflito)", "POST", "/api/v1/agendamentos", {
        "beneficiarioId": 3, "profissionalId": 1, "inicio": f"{segunda}T09:00:00"})
    passo("Tentar agendar fora da disponibilidade (regra de negócio)", "POST", "/api/v1/agendamentos", {
        "beneficiarioId": 3, "profissionalId": 3, "inicio": f"{terca}T08:00:00"})
    agendamento_id = criado["id"]
    passo("Confirmar presença", "PATCH", f"/api/v1/agendamentos/{agendamento_id}/confirmar")
    passo("Registrar comparecimento antes do horário (transição inválida)", "PATCH",
          f"/api/v1/agendamentos/{agendamento_id}/comparecimento")
    passo("Cancelar agendamento", "PATCH", f"/api/v1/agendamentos/{agendamento_id}/cancelar",
          {"motivo": "Beneficiário conseguiu atendimento no CAPS"})
    passo("Cadastrar beneficiário com CPF inválido", "POST", "/api/v1/beneficiarios", {
        "nome": "Teste", "cpf": "123.456.789-00", "dataNascimento": "1990-01-01", "telefone": "(88) 90000-0000"})
    passo("Cadastrar beneficiário com campos inválidos", "POST", "/api/v1/beneficiarios", {
        "nome": "", "cpf": "", "dataNascimento": "2090-01-01", "telefone": "", "email": "invalido"})
    passo("Cadastrar beneficiário (sucesso)", "POST", "/api/v1/beneficiarios", {
        "nome": "Raimunda Nonata Freitas", "cpf": "610.531.783-28", "dataNascimento": "1975-08-21",
        "telefone": "(88) 99905-5005"})
    passo("Buscar agendamento inexistente", "GET", "/api/v1/agendamentos/999")
    passo("Listar agenda da semana", "GET", f"/api/v1/agendamentos?de={segunda}&ate={segunda + dt.timedelta(days=6)}")
    inicio_mes = dt.date.today().replace(day=1)
    passo("Relatório de impacto do mês", "GET",
          f"/api/v1/relatorios/atendimentos?de={inicio_mes - dt.timedelta(days=10)}&ate={inicio_mes + dt.timedelta(days=30)}")

    linhas = ["# Evidência de execução dos endpoints", "",
              f"Gerado automaticamente em {dt.datetime.now():%d/%m/%Y %H:%M} por `scripts/gerar_evidencias.py` "
              "contra a API em execução local (`http://localhost:8080`).", "",
              "| # | Passo | Requisição | Status |", "|---|---|---|---|"]
    for i, (titulo, metodo, caminho, _, status, _) in enumerate(passos, 1):
        linhas.append(f"| {i} | {titulo} | `{metodo} {caminho}` | **{status}** |")
    for i, (titulo, metodo, caminho, corpo, status, resposta) in enumerate(passos, 1):
        linhas += ["", f"## {i}. {titulo}", "", f"`{metodo} {caminho}` → **HTTP {status}**"]
        if corpo is not None:
            linhas += ["", "Corpo enviado:", "```json", json.dumps(corpo, ensure_ascii=False, indent=2), "```"]
        if resposta is not None:
            linhas += ["", "Resposta:", "```json", json.dumps(resposta, ensure_ascii=False, indent=2), "```"]
    SAIDA.parent.mkdir(parents=True, exist_ok=True)
    SAIDA.write_text("\n".join(linhas) + "\n", encoding="utf-8")
    for i, (titulo, metodo, caminho, _, status, _) in enumerate(passos, 1):
        print(f"{i:2} {status} {metodo} {caminho}")


if __name__ == "__main__":
    main()

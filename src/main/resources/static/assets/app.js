const API = "/api/v1";
const DIAS = {
  MONDAY: "Segunda", TUESDAY: "Terça", WEDNESDAY: "Quarta",
  THURSDAY: "Quinta", FRIDAY: "Sexta", SATURDAY: "Sábado", SUNDAY: "Domingo"
};
const ESPECIALIDADES = {
  PSICOLOGIA: "Psicologia", NUTRICAO: "Nutrição", FISIOTERAPIA: "Fisioterapia",
  ASSISTENCIA_SOCIAL: "Assistência Social", ODONTOLOGIA: "Odontologia",
  ORIENTACAO_JURIDICA: "Orientação Jurídica"
};

function nomeEspecialidade(codigo) {
  return ESPECIALIDADES[codigo] || String(codigo).replaceAll("_", " ");
}

const alerta = document.getElementById("alerta");
let cancelandoId = null;

function isoDate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function formatarDataHora(iso) {
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function formatarHora(valor) {
  return String(valor).slice(0, 5);
}

function mostrarAlerta(texto, tipo) {
  alerta.hidden = false;
  alerta.className = "alerta " + tipo;
  alerta.textContent = texto;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function detalheErro(corpo, fallback) {
  if (!corpo) return fallback;
  try {
    const json = JSON.parse(corpo);
    return json.detail || json.title || fallback;
  } catch {
    return fallback;
  }
}

async function api(caminho, opcoes = {}) {
  const resposta = await fetch(API + caminho, {
    headers: { "Content-Type": "application/json", ...(opcoes.headers || {}) },
    ...opcoes
  });
  const texto = await resposta.text();
  const json = texto ? JSON.parse(texto) : null;
  if (!resposta.ok) {
    const erro = new Error(detalheErro(texto, "Não foi possível concluir a operação"));
    erro.status = resposta.status;
    throw erro;
  }
  return json;
}

function abrirPagina(nome) {
  document.querySelectorAll(".pagina").forEach((p) => { p.hidden = true; });
  document.getElementById("pagina-" + nome).hidden = false;
  document.querySelectorAll(".aba").forEach((b) => {
    b.classList.toggle("ativa", b.dataset.pagina === nome);
  });
  if (nome === "painel") carregarPainel();
  if (nome === "agenda") carregarAgenda();
  if (nome === "marcar") prepararMarcacao();
  if (nome === "pessoas") carregarPessoas();
}

document.querySelectorAll(".aba").forEach((botao) => {
  botao.addEventListener("click", () => abrirPagina(botao.dataset.pagina));
});

function card(valor, rotulo) {
  return `<div class="card"><strong>${valor}</strong><span>${rotulo}</span></div>`;
}

function percentual(n) {
  return Number(n || 0).toFixed(0) + "%";
}

async function carregarPainel() {
  const de = document.getElementById("rel-de").value;
  const ate = document.getElementById("rel-ate").value;
  try {
    const r = await api(`/relatorios/atendimentos?de=${de}&ate=${ate}`);
    const status = r.porStatus || {};
    document.getElementById("cards-impacto").innerHTML = [
      card(r.totalAgendamentos, "Atendimentos no período"),
      card(r.beneficiariosDistintosAtendidos, "Pessoas efetivamente atendidas"),
      card(percentual(r.taxaComparecimentoGeral), "Taxa de comparecimento"),
      card(status.FALTOU || 0, "Faltas registradas")
    ].join("");

    const linhas = (r.porEspecialidade || []).map((e) => `
      <tr>
        <td>${nomeEspecialidade(e.especialidade)}</td>
        <td>${e.agendamentos}</td>
        <td>${e.realizados}</td>
        <td>${e.faltas}</td>
        <td>${percentual(e.taxaComparecimento)}</td>
      </tr>`).join("");
    document.getElementById("tabela-especialidade").innerHTML = linhas
      ? `<table><thead><tr><th>Área</th><th>Total</th><th>Realizados</th><th>Faltas</th><th>Comparecimento</th></tr></thead><tbody>${linhas}</tbody></table>`
      : "<p class='vazio'>Sem dados no período.</p>";

    const agenda = await api("/agendamentos");
    const agora = Date.now();
    const proximos = agenda
      .filter((a) => ["AGENDADO", "CONFIRMADO"].includes(a.status) && new Date(a.inicio).getTime() >= agora)
      .sort((a, b) => new Date(a.inicio) - new Date(b.inicio))
      .slice(0, 6);
    document.getElementById("lista-proximos").innerHTML = proximos.length
      ? proximos.map(itemAgenda).join("")
      : "<p class='vazio'>Nenhum atendimento futuro.</p>";
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
}

function itemAgenda(a) {
  const acoes = [];
  if (a.status === "AGENDADO") {
    acoes.push(`<button data-acao="confirmar" data-id="${a.id}">Confirmar</button>`);
  }
  if (a.status === "AGENDADO" || a.status === "CONFIRMADO") {
    acoes.push(`<button class="secundario" data-acao="cancelar" data-id="${a.id}">Cancelar</button>`);
  }
  if (a.status === "CONFIRMADO") {
    acoes.push(`<button data-acao="comparecimento" data-id="${a.id}">Compareceu</button>`);
    acoes.push(`<button class="perigo" data-acao="falta" data-id="${a.id}">Faltou</button>`);
  }
  return `
    <div class="item">
      <div>
        <span class="selo ${a.status}">${a.status}</span>
        <div><strong>${a.beneficiario.nome}</strong> · ${a.profissional.nome}</div>
        <small>${nomeEspecialidade(a.profissional.especialidade)} · ${formatarDataHora(a.inicio)} às ${formatarHora(a.fim)}</small>
        ${a.observacao ? `<div><small>${a.observacao}</small></div>` : ""}
      </div>
      <div class="acoes">${acoes.join("")}</div>
    </div>`;
}

async function carregarAgenda() {
  const status = document.getElementById("filtro-status").value;
  const qs = status ? `?status=${status}` : "";
  try {
    const lista = await api("/agendamentos" + qs);
    document.getElementById("lista-agenda").innerHTML = lista.length
      ? `<article class="cartao">${lista.sort((a, b) => new Date(a.inicio) - new Date(b.inicio)).map(itemAgenda).join("")}</article>`
      : "<p class='vazio'>Nenhum agendamento encontrado.</p>";
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
}

document.getElementById("lista-agenda").addEventListener("click", tratarAcaoAgenda);
document.getElementById("lista-proximos").addEventListener("click", tratarAcaoAgenda);

async function tratarAcaoAgenda(ev) {
  const botao = ev.target.closest("button[data-acao]");
  if (!botao) return;
  const { acao, id } = botao.dataset;
  try {
    if (acao === "cancelar") {
      cancelandoId = id;
      document.getElementById("motivo-cancelamento").value = "";
      document.getElementById("dialogo-cancelar").showModal();
      return;
    }
    await api(`/agendamentos/${id}/${acao}`, { method: "PATCH" });
    mostrarAlerta("Agenda atualizada.", "ok");
    await carregarAgenda();
    if (!document.getElementById("pagina-painel").hidden) await carregarPainel();
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
}

document.getElementById("form-cancelar").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  try {
    await api(`/agendamentos/${cancelandoId}/cancelar`, {
      method: "PATCH",
      body: JSON.stringify({ motivo: document.getElementById("motivo-cancelamento").value })
    });
    document.getElementById("dialogo-cancelar").close();
    mostrarAlerta("Horário liberado.", "ok");
    await carregarAgenda();
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
});
document.getElementById("fechar-cancelar").addEventListener("click", () => {
  document.getElementById("dialogo-cancelar").close();
});

async function prepararMarcacao() {
  const [profissionais, beneficiarios] = await Promise.all([
    api("/profissionais"),
    api("/beneficiarios")
  ]);
  document.getElementById("marcar-profissional").innerHTML =
    `<option value="">Selecione</option>` +
    profissionais.map((p) => `<option value="${p.id}">${p.nome} — ${p.especialidadeDescricao}</option>`).join("");
  document.getElementById("marcar-beneficiario").innerHTML =
    `<option value="">Selecione</option>` +
    beneficiarios.map((b) => `<option value="${b.id}">${b.nome}</option>`).join("");
  document.getElementById("marcar-horarios").textContent = "Escolha o profissional e a data.";
  document.getElementById("marcar-inicio").value = "";
}

async function carregarHorariosLivres() {
  const id = document.getElementById("marcar-profissional").value;
  const data = document.getElementById("marcar-data").value;
  const caixa = document.getElementById("marcar-horarios");
  document.getElementById("marcar-inicio").value = "";
  if (!id || !data) {
    caixa.textContent = "Escolha o profissional e a data.";
    return;
  }
  try {
    const r = await api(`/profissionais/${id}/horarios-livres?data=${data}`);
    if (!r.horarios.length) {
      caixa.textContent = "Nenhum horário livre nesta data. Veja os dias de disponibilidade do profissional.";
      return;
    }
    caixa.innerHTML = r.horarios.map((h) =>
      `<button type="button" class="horario" data-hora="${h}">${formatarHora(h)}</button>`
    ).join("");
  } catch (e) {
    caixa.textContent = e.message;
  }
}

document.getElementById("marcar-profissional").addEventListener("change", carregarHorariosLivres);
document.getElementById("marcar-data").addEventListener("change", carregarHorariosLivres);
document.getElementById("marcar-horarios").addEventListener("click", (ev) => {
  const botao = ev.target.closest(".horario");
  if (!botao) return;
  document.querySelectorAll(".horario").forEach((h) => h.classList.remove("escolhido"));
  botao.classList.add("escolhido");
  const data = document.getElementById("marcar-data").value;
  const hora = formatarHora(botao.dataset.hora);
  document.getElementById("marcar-inicio").value = `${data}T${hora}:00`;
});

document.getElementById("form-marcar").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const inicio = document.getElementById("marcar-inicio").value;
  if (!inicio) {
    mostrarAlerta("Escolha um horário livre.", "erro");
    return;
  }
  try {
    await api("/agendamentos", {
      method: "POST",
      body: JSON.stringify({
        beneficiarioId: Number(document.getElementById("marcar-beneficiario").value),
        profissionalId: Number(document.getElementById("marcar-profissional").value),
        inicio,
        observacao: document.getElementById("marcar-obs").value
      })
    });
    mostrarAlerta("Atendimento marcado. Se alguém tentar o mesmo horário, a API recusa com conflito.", "ok");
    ev.target.reset();
    abrirPagina("agenda");
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
});

async function carregarPessoas() {
  try {
    const [beneficiarios, profissionais] = await Promise.all([
      api("/beneficiarios"),
      api("/profissionais")
    ]);
    document.getElementById("lista-beneficiarios").innerHTML = beneficiarios.map((b) => `
      <div class="item">
        <div>
          <strong>${b.nome}</strong>
          <div><small>CPF ${b.cpf} · ${b.idade} anos · ${b.telefone}</small></div>
        </div>
      </div>`).join("");
    document.getElementById("lista-profissionais").innerHTML = profissionais.map((p) => `
      <div class="item">
        <div>
          <strong>${p.nome}</strong>
          <div><small>${p.especialidadeDescricao} · ${p.duracaoAtendimentoMinutos} min · ${p.registroProfissional || ""}</small></div>
          <div><small>${p.disponibilidades.map((d) => `${DIAS[d.diaSemana]} ${formatarHora(d.horaInicio)}–${formatarHora(d.horaFim)}`).join(" · ")}</small></div>
        </div>
      </div>`).join("");
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
}

document.getElementById("form-beneficiario").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const dados = Object.fromEntries(new FormData(ev.target).entries());
  if (!dados.email) delete dados.email;
  try {
    await api("/beneficiarios", { method: "POST", body: JSON.stringify(dados) });
    mostrarAlerta("Beneficiário cadastrado. O CPF volta mascarado, em atenção à LGPD.", "ok");
    ev.target.reset();
    await carregarPessoas();
  } catch (e) {
    mostrarAlerta(e.message, "erro");
  }
});

document.getElementById("form-periodo").addEventListener("submit", (ev) => {
  ev.preventDefault();
  carregarPainel();
});
document.getElementById("btn-atualizar-agenda").addEventListener("click", carregarAgenda);
document.getElementById("filtro-status").addEventListener("change", carregarAgenda);

(function iniciar() {
  const hoje = new Date();
  const de = new Date(); de.setDate(hoje.getDate() - 21);
  const ate = new Date(); ate.setDate(hoje.getDate() + 21);
  document.getElementById("rel-de").value = isoDate(de);
  document.getElementById("rel-ate").value = isoDate(ate);
  document.getElementById("marcar-data").value = isoDate(hoje);
  carregarPainel();
})();

# Evidência de execução dos endpoints

Gerado automaticamente em 07/11/2026 10:00 por `scripts/gerar_evidencias.py` contra a API em execução local (`http://localhost:8080`).

| # | Passo | Requisição | Status |
|---|---|---|---|
| 1 | Listar profissionais de psicologia | `GET /api/v1/profissionais?especialidade=PSICOLOGIA` | **200** |
| 2 | Consultar horários livres da psicóloga | `GET /api/v1/profissionais/1/horarios-livres?data=2026-11-09` | **200** |
| 3 | Agendar atendimento (sucesso) | `POST /api/v1/agendamentos` | **201** |
| 4 | Tentar agendar o mesmo horário (conflito) | `POST /api/v1/agendamentos` | **409** |
| 5 | Tentar agendar fora da disponibilidade (regra de negócio) | `POST /api/v1/agendamentos` | **422** |
| 6 | Confirmar presença | `PATCH /api/v1/agendamentos/7/confirmar` | **200** |
| 7 | Registrar comparecimento antes do horário (transição inválida) | `PATCH /api/v1/agendamentos/7/comparecimento` | **422** |
| 8 | Cancelar agendamento | `PATCH /api/v1/agendamentos/7/cancelar` | **200** |
| 9 | Cadastrar beneficiário com CPF inválido | `POST /api/v1/beneficiarios` | **422** |
| 10 | Cadastrar beneficiário com campos inválidos | `POST /api/v1/beneficiarios` | **400** |
| 11 | Cadastrar beneficiário (sucesso) | `POST /api/v1/beneficiarios` | **201** |
| 12 | Buscar agendamento inexistente | `GET /api/v1/agendamentos/999` | **404** |
| 13 | Listar agenda da semana | `GET /api/v1/agendamentos?de=2026-11-09&ate=2026-11-15` | **200** |
| 14 | Relatório de impacto do mês | `GET /api/v1/relatorios/atendimentos?de=2026-10-22&ate=2026-12-01` | **200** |

## 1. Listar profissionais de psicologia

`GET /api/v1/profissionais?especialidade=PSICOLOGIA` → **HTTP 200**

Resposta:
```json
[
  {
    "id": 1,
    "nome": "Ana Beatriz Lima",
    "especialidade": "PSICOLOGIA",
    "especialidadeDescricao": "Psicologia",
    "registroProfissional": "CRP 11/12345",
    "duracaoAtendimentoMinutos": 50,
    "disponibilidades": [
      {
        "diaSemana": "MONDAY",
        "horaInicio": "08:00:00",
        "horaFim": "12:00:00"
      },
      {
        "diaSemana": "WEDNESDAY",
        "horaInicio": "14:00:00",
        "horaFim": "18:00:00"
      }
    ],
    "ativo": true
  }
]
```

## 2. Consultar horários livres da psicóloga

`GET /api/v1/profissionais/1/horarios-livres?data=2026-11-09` → **HTTP 200**

Resposta:
```json
{
  "profissionalId": 1,
  "data": "2026-11-09",
  "duracaoAtendimentoMinutos": 50,
  "horarios": [
    "08:50:00",
    "09:40:00",
    "10:30:00"
  ]
}
```

## 3. Agendar atendimento (sucesso)

`POST /api/v1/agendamentos` → **HTTP 201**

Corpo enviado:
```json
{
  "beneficiarioId": 4,
  "profissionalId": 1,
  "inicio": "2026-11-09T08:50:00",
  "observacao": "Encaminhado pela assistente social"
}
```

Resposta:
```json
{
  "id": 7,
  "status": "AGENDADO",
  "inicio": "2026-11-09T08:50:00",
  "fim": "2026-11-09T09:40:00",
  "beneficiario": {
    "id": 4,
    "nome": "Antônio Carlos Bezerra",
    "telefone": "(88) 99904-4004"
  },
  "profissional": {
    "id": 1,
    "nome": "Ana Beatriz Lima",
    "especialidade": "PSICOLOGIA"
  },
  "observacao": "Encaminhado pela assistente social",
  "motivoCancelamento": null,
  "criadoEm": "2026-11-07T10:00:07.2348685"
}
```

## 4. Tentar agendar o mesmo horário (conflito)

`POST /api/v1/agendamentos` → **HTTP 409**

Corpo enviado:
```json
{
  "beneficiarioId": 3,
  "profissionalId": 1,
  "inicio": "2026-11-09T09:00:00"
}
```

Resposta:
```json
{
  "type": "about:blank",
  "title": "Conflito",
  "status": 409,
  "detail": "Horário já ocupado na agenda do profissional",
  "instance": "/api/v1/agendamentos"
}
```

## 5. Tentar agendar fora da disponibilidade (regra de negócio)

`POST /api/v1/agendamentos` → **HTTP 422**

Corpo enviado:
```json
{
  "beneficiarioId": 3,
  "profissionalId": 3,
  "inicio": "2026-11-10T08:00:00"
}
```

Resposta:
```json
{
  "type": "about:blank",
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "O profissional não atende neste dia/horário",
  "instance": "/api/v1/agendamentos"
}
```

## 6. Confirmar presença

`PATCH /api/v1/agendamentos/7/confirmar` → **HTTP 200**

Resposta:
```json
{
  "id": 7,
  "status": "CONFIRMADO",
  "inicio": "2026-11-09T08:50:00",
  "fim": "2026-11-09T09:40:00",
  "beneficiario": {
    "id": 4,
    "nome": "Antônio Carlos Bezerra",
    "telefone": "(88) 99904-4004"
  },
  "profissional": {
    "id": 1,
    "nome": "Ana Beatriz Lima",
    "especialidade": "PSICOLOGIA"
  },
  "observacao": "Encaminhado pela assistente social",
  "motivoCancelamento": null,
  "criadoEm": "2026-11-07T10:00:07.234869"
}
```

## 7. Registrar comparecimento antes do horário (transição inválida)

`PATCH /api/v1/agendamentos/7/comparecimento` → **HTTP 422**

Resposta:
```json
{
  "type": "about:blank",
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "Só é possível registrar comparecimento/falta após o horário de início",
  "instance": "/api/v1/agendamentos/7/comparecimento"
}
```

## 8. Cancelar agendamento

`PATCH /api/v1/agendamentos/7/cancelar` → **HTTP 200**

Corpo enviado:
```json
{
  "motivo": "Beneficiário conseguiu atendimento no CAPS"
}
```

Resposta:
```json
{
  "id": 7,
  "status": "CANCELADO",
  "inicio": "2026-11-09T08:50:00",
  "fim": "2026-11-09T09:40:00",
  "beneficiario": {
    "id": 4,
    "nome": "Antônio Carlos Bezerra",
    "telefone": "(88) 99904-4004"
  },
  "profissional": {
    "id": 1,
    "nome": "Ana Beatriz Lima",
    "especialidade": "PSICOLOGIA"
  },
  "observacao": "Encaminhado pela assistente social",
  "motivoCancelamento": "Beneficiário conseguiu atendimento no CAPS",
  "criadoEm": "2026-11-07T10:00:07.234869"
}
```

## 9. Cadastrar beneficiário com CPF inválido

`POST /api/v1/beneficiarios` → **HTTP 422**

Corpo enviado:
```json
{
  "nome": "Teste",
  "cpf": "123.456.789-00",
  "dataNascimento": "1990-01-01",
  "telefone": "(88) 90000-0000"
}
```

Resposta:
```json
{
  "type": "about:blank",
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "CPF inválido",
  "instance": "/api/v1/beneficiarios"
}
```

## 10. Cadastrar beneficiário com campos inválidos

`POST /api/v1/beneficiarios` → **HTTP 400**

Corpo enviado:
```json
{
  "nome": "",
  "cpf": "",
  "dataNascimento": "2090-01-01",
  "telefone": "",
  "email": "invalido"
}
```

Resposta:
```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos",
  "instance": "/api/v1/beneficiarios",
  "campos": {
    "dataNascimento": "deve ser uma data passada",
    "nome": "não deve estar em branco",
    "telefone": "não deve estar em branco",
    "cpf": "não deve estar em branco",
    "email": "deve ser um endereço de e-mail bem formado"
  }
}
```

## 11. Cadastrar beneficiário (sucesso)

`POST /api/v1/beneficiarios` → **HTTP 201**

Corpo enviado:
```json
{
  "nome": "Raimunda Nonata Freitas",
  "cpf": "610.531.783-28",
  "dataNascimento": "1975-08-21",
  "telefone": "(88) 99905-5005"
}
```

Resposta:
```json
{
  "id": 5,
  "nome": "Raimunda Nonata Freitas",
  "cpf": "***.531.783-**",
  "dataNascimento": "1975-08-21",
  "idade": 51,
  "telefone": "(88) 99905-5005",
  "email": null,
  "ativo": true
}
```

## 12. Buscar agendamento inexistente

`GET /api/v1/agendamentos/999` → **HTTP 404**

Resposta:
```json
{
  "type": "about:blank",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Agendamento com id 999 não encontrado(a)",
  "instance": "/api/v1/agendamentos/999"
}
```

## 13. Listar agenda da semana

`GET /api/v1/agendamentos?de=2026-11-09&ate=2026-11-15` → **HTTP 200**

Resposta:
```json
[
  {
    "id": 5,
    "status": "AGENDADO",
    "inicio": "2026-11-09T08:00:00",
    "fim": "2026-11-09T08:50:00",
    "beneficiario": {
      "id": 1,
      "nome": "Maria das Graças Oliveira",
      "telefone": "(88) 99901-1001"
    },
    "profissional": {
      "id": 1,
      "nome": "Ana Beatriz Lima",
      "especialidade": "PSICOLOGIA"
    },
    "observacao": "Retorno - acompanhamento de ansiedade",
    "motivoCancelamento": null,
    "criadoEm": "2026-11-07T09:57:49.351343"
  },
  {
    "id": 7,
    "status": "CANCELADO",
    "inicio": "2026-11-09T08:50:00",
    "fim": "2026-11-09T09:40:00",
    "beneficiario": {
      "id": 4,
      "nome": "Antônio Carlos Bezerra",
      "telefone": "(88) 99904-4004"
    },
    "profissional": {
      "id": 1,
      "nome": "Ana Beatriz Lima",
      "especialidade": "PSICOLOGIA"
    },
    "observacao": "Encaminhado pela assistente social",
    "motivoCancelamento": "Beneficiário conseguiu atendimento no CAPS",
    "criadoEm": "2026-11-07T10:00:07.234869"
  },
  {
    "id": 6,
    "status": "AGENDADO",
    "inicio": "2026-11-10T08:30:00",
    "fim": "2026-11-10T09:00:00",
    "beneficiario": {
      "id": 2,
      "nome": "José Raimundo Silva",
      "telefone": "(88) 99902-2002"
    },
    "profissional": {
      "id": 2,
      "nome": "Carlos Eduardo Nogueira",
      "especialidade": "NUTRICAO"
    },
    "observacao": "Controle de diabetes",
    "motivoCancelamento": null,
    "criadoEm": "2026-11-07T09:57:49.38295"
  }
]
```

## 14. Relatório de impacto do mês

`GET /api/v1/relatorios/atendimentos?de=2026-10-22&ate=2026-12-01` → **HTTP 200**

Resposta:
```json
{
  "inicio": "2026-10-22",
  "fim": "2026-12-01",
  "totalAgendamentos": 7,
  "beneficiariosDistintosAtendidos": 3,
  "porStatus": {
    "AGENDADO": 2,
    "CONFIRMADO": 0,
    "REALIZADO": 3,
    "FALTOU": 1,
    "CANCELADO": 1
  },
  "porEspecialidade": [
    {
      "especialidade": "PSICOLOGIA",
      "agendamentos": 4,
      "realizados": 2,
      "faltas": 0,
      "taxaComparecimento": 100.0
    },
    {
      "especialidade": "NUTRICAO",
      "agendamentos": 2,
      "realizados": 1,
      "faltas": 0,
      "taxaComparecimento": 100.0
    },
    {
      "especialidade": "FISIOTERAPIA",
      "agendamentos": 1,
      "realizados": 0,
      "faltas": 1,
      "taxaComparecimento": 0.0
    }
  ],
  "taxaComparecimentoGeral": 75.0
}
```

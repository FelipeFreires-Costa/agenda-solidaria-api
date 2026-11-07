# Documento de Requisitos — Agenda Solidária

## 1. Contexto e problema

Associações comunitárias de Quixadá-CE (associações de moradores, pastorais, projetos sociais) oferecem
atendimentos gratuitos à população em situação de vulnerabilidade: psicologia, nutrição, fisioterapia,
assistência social e orientação jurídica. Esses atendimentos são feitos por **profissionais voluntários** que
doam algumas horas por semana.

Hoje o controle costuma ser feito em **caderno, planilha ou grupo de WhatsApp**, o que gera:

- dois beneficiários marcados no mesmo horário com o mesmo profissional (*overbooking*);
- horários vagos que ninguém sabe que estão livres;
- faltas sem registro e sem avisar o voluntário, que se desloca à toa;
- poucas famílias concentrando muitas vagas;
- dificuldade de **prestar contas** (quantas pessoas foram atendidas, em quais áreas), informação exigida
  por parceiros, editais e doadores.

## 2. Objetivo do MVP

Disponibilizar uma **API RESTful** que centralize a agenda de atendimentos voluntários da associação, aplique
automaticamente as regras de marcação, registre o ciclo de vida de cada atendimento e gere indicadores de
impacto social.

## 3. Atores

| Ator | Descrição |
|---|---|
| Atendente / voluntário da recepção | Cadastra beneficiários, consulta horários e faz as marcações |
| Profissional voluntário | Informa sua disponibilidade semanal e registra comparecimento/falta |
| Coordenação da associação | Cadastra profissionais e acompanha os indicadores |
| Beneficiário (secundário) | Recebe as notificações sobre seus atendimentos |

## 4. Requisitos funcionais

| ID | Requisito | Endpoint(s) |
|---|---|---|
| RF01 | Cadastrar, consultar, atualizar e inativar beneficiários (CPF validado e único) | `/api/v1/beneficiarios` |
| RF02 | Cadastrar, consultar, atualizar e inativar profissionais voluntários por especialidade | `/api/v1/profissionais` |
| RF03 | Definir janelas semanais de disponibilidade do profissional e a duração do atendimento | `PUT /api/v1/profissionais/{id}/disponibilidades` |
| RF04 | Consultar os horários livres de um profissional em uma data | `GET /api/v1/profissionais/{id}/horarios-livres` |
| RF05 | Agendar atendimento validando as regras de negócio (RN01–RN06) | `POST /api/v1/agendamentos` |
| RF06 | Confirmar presença | `PATCH /api/v1/agendamentos/{id}/confirmar` |
| RF07 | Cancelar agendamento com motivo, liberando o horário | `PATCH /api/v1/agendamentos/{id}/cancelar` |
| RF08 | Registrar comparecimento ou falta | `PATCH /api/v1/agendamentos/{id}/comparecimento` e `/falta` |
| RF09 | Consultar a agenda com filtros (profissional, beneficiário, status, período) | `GET /api/v1/agendamentos` |
| RF10 | Emitir relatório de impacto (totais por status e especialidade, pessoas atendidas, taxa de comparecimento) | `GET /api/v1/relatorios/atendimentos` |
| RF11 | Notificar o beneficiário a cada mudança no agendamento | (Observer — simulado em log) |

## 5. Regras de negócio

| ID | Regra | Classe |
|---|---|---|
| RN01 | Beneficiário e profissional precisam estar ativos | `RegraParticipantesAtivos` |
| RN02 | Agendamento com antecedência mínima configurável (padrão 2 h) | `RegraAntecedenciaMinima` |
| RN03 | O horário precisa caber inteiramente em uma janela de disponibilidade do profissional | `RegraDentroDaDisponibilidade` |
| RN04 | O profissional não pode ter dois atendimentos sobrepostos | `RegraSemConflitoProfissional` |
| RN05 | O beneficiário não pode ter dois atendimentos sobrepostos | `RegraSemConflitoBeneficiario` |
| RN06 | Cada beneficiário pode ter no máximo N atendimentos futuros ativos (padrão 3) — **equidade no acesso** | `RegraLimiteAgendamentosAtivos` |
| RN07 | A duração do atendimento é definida pelo profissional (10 a 180 min) | `Profissional` |
| RN08 | Transições de status permitidas: AGENDADO→CONFIRMADO→REALIZADO/FALTOU; AGENDADO/CONFIRMADO→CANCELADO | `StatusAgendamento` |
| RN09 | Comparecimento/falta só podem ser registrados após o horário de início | `Agendamento` |
| RN10 | Cancelamento exige motivo; agendamentos cancelados liberam o horário | `Agendamento` |
| RN11 | Exclusão é lógica (inativação), preservando o histórico para prestação de contas | `Beneficiario`, `Profissional` |

## 6. Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF01 | API REST com JSON, versionada (`/api/v1`), verbos e códigos HTTP semânticos (200, 201 + `Location`, 204, 400, 404, 409, 422) |
| RNF02 | Erros no padrão **Problem Details (RFC 9457)** |
| RNF03 | Documentação interativa OpenAPI 3 / Swagger UI |
| RNF04 | Código tipado (Java 17), organizado em camadas segundo a Clean Architecture |
| RNF05 | Testes automatizados de unidade e de integração |
| RNF06 | **LGPD**: CPF devolvido mascarado nas respostas; coleta apenas dos dados necessários ao atendimento |
| RNF07 | Fuso horário configurável (padrão `America/Fortaleza`) |
| RNF08 | Executável com um comando (`mvnw spring-boot:run`) sem instalar banco de dados |

## 7. Escopo do MVP e evolução

**Dentro do MVP:** tudo o que está nas seções 4 e 5.

**Fora do MVP (próximas versões):** autenticação e perfis de acesso (Spring Security/JWT), banco PostgreSQL em
produção, envio real de WhatsApp/SMS (basta implementar um novo `AgendamentoObserver`), lista de espera,
lembrete automático na véspera e front-end web/mobile.

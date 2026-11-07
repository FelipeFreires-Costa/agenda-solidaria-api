# Roteiro do Pitch (vídeo de até 1 minuto)

Grave a tela (OBS Studio, Xbox Game Bar `Win+G` ou o gravador do PowerPoint) com a API rodando
(`mvnw.cmd spring-boot:run`) e o Swagger aberto em http://localhost:8080/swagger-ui/index.html.

| Tempo | O que mostrar | Fala sugerida |
|---|---|---|
| 0–10 s | Foto da associação / slide com o problema | "Na associação [NOME], psicólogos e nutricionistas voluntários atendem a comunidade de graça, mas a agenda era um caderno: horários duplicados, faltas sem registro e nenhum número para prestar contas." |
| 10–20 s | Diagrama de casos de uso e de arquitetura (`docs/img`) | "Modelamos o problema em UML e construímos a Agenda Solidária, uma API REST em Spring Boot com Clean Architecture: o domínio não depende de framework." |
| 20–35 s | Swagger: `GET horarios-livres` → `POST agendamentos` (201) → repetir (409) | "A recepção vê os horários livres e agenda. Se o horário já está ocupado, a API recusa: cada regra é uma Strategy, e adicionar uma nova não mexe no caso de uso." |
| 35–45 s | `PATCH confirmar` + log da notificação no terminal | "O status segue o padrão State, e a cada mudança um Observer avisa o beneficiário." |
| 45–55 s | `GET relatorios/atendimentos` | "No fim do mês, a coordenação tem o relatório de impacto: pessoas atendidas, por especialidade, e a taxa de comparecimento." |
| 55–60 s | README do repositório + tela dos 40 testes | "Código, UML e 40 testes automatizados estão no repositório. Obrigado!" |

Dicas: publique o vídeo no YouTube como **não listado** ou no Google Drive com **"qualquer pessoa com o link
pode ver"**, e teste o link em uma janela anônima antes de enviar ao professor.

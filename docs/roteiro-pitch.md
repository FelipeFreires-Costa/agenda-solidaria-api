# Roteiro do Pitch (vídeo de até 1 minuto)

Grave a tela (OBS Studio, Xbox Game Bar `Win+G` ou o gravador do PowerPoint) com a API rodando
(`mvnw.cmd spring-boot:run`). Abra http://localhost:8080/ e, se quiser, o Swagger em outra aba.

| Tempo | O que mostrar | Fala sugerida |
|---|---|---|
| 0–10 s | Tela da associação / problema | "Na Sociedade de São Vicente de Paulo de Quixadá, voluntários atendem a comunidade, mas a agenda ainda costuma viver em caderno e WhatsApp: horário duplicado, falta sem registro e nenhum número para prestar contas." |
| 10–20 s | Diagrama de casos de uso ou arquitetura (`docs/img`) | "Modelamos em UML e construímos a Agenda Solidária: API REST em Spring Boot com Clean Architecture. O domínio não depende de framework." |
| 20–38 s | Tela **Marcar**: horários livres → salvar → tentar o mesmo horário | "A recepção só vê horários livres. Se o horário já está ocupado, a API recusa. Cada regra é uma Strategy: incluir uma nova não mexe no caso de uso." |
| 38–48 s | Agenda: confirmar, e o terminal com o log do Observer | "O status segue o padrão State. A cada mudança, um Observer avisa o beneficiário." |
| 48–56 s | Painel de impacto | "No fim do mês a coordenação tem pessoas atendidas, especialidade e taxa de comparecimento." |
| 56–60 s | Swagger + testes no README | "Código, UML e testes automatizados estão no repositório. Obrigado!" |

Dicas: publique o vídeo no YouTube como **não listado** ou no Google Drive com **"qualquer pessoa com o link
pode ver"**, e teste o link em uma janela anônima antes de enviar ao professor.

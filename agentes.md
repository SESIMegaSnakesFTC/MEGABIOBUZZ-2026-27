# agentes.md

Contexto compartilhado entre as IAs orquestradas pelo dev (Claude Code, Agy/Gemini e outras). Leia antes de mexer no código. Estado de **2026-11-20**: confira `git log` e `git status` para ver o que já foi commitado.

Regras gerais: `CLAUDE.md` (Claude Code) e `GEMINI.md` (Agy). Descrição do código: `README.md`.

## Projeto
- Robô FTC da equipe Mega Snakes, temporada BIOBUZZ 2026-2027. Código em `TeamCode/`.
- Sem testes automatizados: tudo é testado no robô, pelo Driver Station.
- Versões: FTC SDK 12.0.0, Pedro Pathing core 3.0.0, Pedro Ivy 1.1.1. Escreva código para essas versões.

## Branches (layouts diferentes)

| Branch | O que é |
| :--- | :--- |
| `main` | Competição: `TeleOperados/TeleOp_RED` e `TeleOp_BLUE`, autônomos Ivy em `Autonomo/pedro/Ivy/`, `Mech/ImportantMech/`. É a única com shooter, gate e hood ligados num TeleOp. |
| `Test` | Experimentação: `Mech/` plano, pasta `Testes/` com as bancadas, sem TeleOps e sem `@Autonomous`. |
| `Regional` | Saiu da `Test`: `Autonomo/` virou `Autonomous/`, tem `Ivy/RED/RED.java`, `TeleOp/Teleoperado` e `TeleoperadoLed`, sem `Testes/`. Shooter, gate e hood não estão ligados em nenhum TeleOp aqui. |

Sempre rode `git branch` antes de assumir que um arquivo existe.

## O que foi feito (2026-11-20)

| # | Mudança | Arquivo | Branches |
| :-: | :--- | :--- | :--- |
| 1 | `HoldPosition`: condição `&&` virou `\|\|` e o path é criado uma vez (`!follower.following()`) | `AutoPath.java` | só `Regional` |
| 2 | `pathzika`: path com `.constant(pose)`. Sem heading o Pedro 3.0.0 lança `path has no heading` | `AutoPath.java` | só `Regional` |
| 3 | `getDistance`: ângulo em graus passava direto para `Math.sin` (radianos) | `Mech/LimeLight.java` | `Regional`, `main`, `Test` |
| 4 | `getSin`: tabela `SENO` com interpolação linear e fallback `Math.sin(Math.toRadians(...))` fora de 45°-70°. Antes buscava chave `double` num mapa `Integer` (sempre `null`, `NullPointerException`) | `Mech/LimeLight.java` | `Regional`, `main`, `Test` |
| 5 | Documentação: `CLAUDE.md`, `README.md` e este arquivo | raiz | `Regional`, `main`, `Test` |

Nada disso foi compilado nem testado no robô.

## Pendências e riscos
- `AutoPath` ainda tem o `HoldPosition` antigo e o path sem heading em `main` (`Autonomo/pedro/AutoPath.java`) e `Test`. Só a `Regional` foi corrigida.
- A distância mudou com a correção de radianos. Pontos da `ValuesToShoot` medidos antes podem precisar de nova calibração. A maioria das entradas ainda é `NaN` (não medida).
- O gate abre por **timer fixo de 1,5 s** depois de ligar o shooter (TeleOps da `main`), não por velocidade medida. Posições aberto/fechado ainda não testadas.
- `HoodServo.MaxServoTICKS` ainda não foi medido. Posições do hood ainda não foram testadas.
- Retângulo proibido do `AutoPath` (`X_MIN..Y_MAX`) é um chute, marcado `shortcut:`.
- Tracking azul: sem field map azul e sem teste em campo.
- A tabela `SENO` dá suavidade e previsibilidade, não ganho de velocidade. `Math.sin` seria igualmente rápido.

## Como o hood funciona (versão aprovada pelo dev)
- Sistema de ajuste de angulação do tiro, em malha fechada.
- A Limelight lê o ângulo vertical da tag; somado à inclinação da câmera, a trigonometria dá a distância até a hive.
- A distância reajusta o ângulo de saída e, com ele, a curvatura (parábola) do tiro.
- `ValuesToShoot` (montada a partir de testes) devolve o alvo do hood e a velocidade do shooter, interpolando entre pontos.
- Encoder externo lê a posição real; PID leva o servo ao alvo, com zona morta, anti-windup e potência limitada.
- Alvo `NaN`: o servo para. Os TeleOps da `main` seguram o último alvo válido.
- O hood usa **distância até a hive**, não o ângulo da mira da turret.

## Preferências do dev
- Responder em português, em modo caveman (direto), com resumo em tópicos numerados após alterações.
- Pedir permissão antes de editar código. Dar ao menos uma ideia de implementação antes.
- Git é tarefa **somente do dev**: nenhuma IA deve commitar, dar push, subir para o GitHub nem sugerir mensagem de commit.
- **Nunca chamar a turret de "torre".** Sempre "turret".
- Textos para copiar (documento, celular): bloco ```text``` sem indentação aninhada.
- Ao explicar mecanismos para documento ou juízes: tom técnico, focar em como funciona. Não listar o que falta e não citar posições ainda não testadas.
- Quando uma tarefa não indicar o arquivo, lembrar o dev.

## Textos aprovados para o documento

Coisas legais:

```text
1. AUTOPATH (NAVEGAÇÃO ASSISTIDA NO TELEOP)
- Leva o robô a 8 pontos fixos, contornando o meio da arena.
- Dijkstra escolhe a menor rota pelos 4 cantos.
- Liang-Barsky evita a zona proibida do centro.
- O path é contínuo e o heading fica travado. O stick é o override.
- HoldPosition segura a pose e, se o robô for empurrado em qualquer eixo, volta por um path com heading definido.

2. CONTAGEM DE BOLAS + FEEDBACK NO LED
- O sensor detecta por cor (HSV) ou distância.
- Conta pela borda de subida (0 a 4), com modo inverso e reset manual.
- O LED muda de cor conforme a carga: apagado, vermelho, laranja, amarelo e verde.

3. SHOOTER + GATE
- PIDF de velocidade com F compensado pela tensão da bateria, filtrada.
- O gate abre 1,5 s depois de ligar, para a bola não sair com o shooter lento.

4. HOOD (AJUSTE DE ANGULAÇÃO)
- Ver seção "Como o hood funciona".

5. TURRET AUTOMÁTICA
- Estimador que mistura odometria e visão.
- Compensação de latência da Limelight, feedforward e correção de pose por MegaTag.

6. DIREÇÃO FIELDCENTRIC
- Roda pelo Pedro Follower, então odometria e controle compartilham o mesmo drivetrain.
```

Aprendizados e habilidades:

```text
- Reaproveitamento de código do autônomo (Pedro Pathing) no TeleOp, unificando odometria e controle manual.
- Aplicação de matemática em classes: grafos (Dijkstra), geometria (Liang-Barsky), trigonometria e interpolação linear.
- Integração entre mecanismos: sensor de cor com LED, distância com hood e shooter, shooter com gate, turret com odometria e visão.
- Teoria de controle: PID, PIDF, feedforward, anti-windup e zona morta.
- Compensação de bateria no shooter, com tensão filtrada.
- Fusão de sensores: odometria e visão, com compensação de latência.
- Otimização do loop de controle: reenvio de parâmetros só quando mudam e bulk caching.
```

## Não verificado
- A turret (`Mech/Turret/`) e o `PollenDetector` foram descritos pelo `CLAUDE.md`, não lidos linha a linha.
- Os autônomos Ivy da `main` não foram lidos.
- Nenhuma branch foi compilada após as mudanças acima.

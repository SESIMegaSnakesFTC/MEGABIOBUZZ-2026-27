# Diagnóstico: Erros do `AutoPath` (Pedro Pathing 3.0.0)

> [!IMPORTANT]
> **Resumo**: o robô não chega ao local desejado e fica "sambando" parado porque o
> heading do caminho é interpolado **ao contrário**. Com `line().linear()`, o follower
> exige que o robô já esteja virado para o heading do **destino** no primeiro instante
> do trajeto (erro de ~90°). O controlador de heading satura, o `Mecanum.maxScaling`
> **zera a translação** (um mecanum não gira em potência máxima e anda ao mesmo tempo),
> e o robô fica girando no lugar sem avançar.

---

## 1. Causa Raiz — `.linear()` invertido em `Line`

**Arquivo:** `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/pedro/AutoPath.java:44`

```java
follower.follow(line(atual, locais[i]).linear(atual, locais[i]));
```

O heading do path sai invertido:

```
de (56, 9, 90°)  ate (10, 103, 0°)        -> RED_PARK
  line.linear  : h(t=0)=  0,0°   h(t=0.5)= 45,0°   h(t=1)= 90,0°   <-- ERRADO
  curve.linear : h(t=0)= 90,0°   h(t=0.5)= 45,0°   h(t=1)=  0,0°   <-- correto
```

### Por que acontece (dentro da biblioteca)

| Arquivo | Linha | O que faz |
|---|---|---|
| `paths/interpolator/Interpolator.java` | 37 | `linear` usa `curve.pathCompletion(t)` para interpolar o heading |
| `paths/curves/Curve.java` | 27-30 | `pathCompletion` **default** = `remainingDistance/length` = **`1-t`** (fração *restante*) |
| `paths/curves/bezier/BezierCurve.java` | 247-252 | **sobrescreve** com a fração *concluída* correta |

Ou seja: a `BezierCurve` está certa, a `Line` está invertida — e `.linear()` depende
disso. É por isso que `Ivy_model.java` (que usa `line().constant()`) funciona e o
`AutoPath` não.

### Cadeia completa até o "sambando"

1. Heading alvo no `t=0` = `0°` (destino), robô em `90°` → **erro de -88°**.
2. `Constants.java:58` — P de heading com ganho `4.07` → satura em `turn = -1.000`.
3. `ForesightPowerAllocator.java:83` → `clampPowers` → `Mecanum.maxScaling`
   (`Mecanum.java:87`): **mecanum não gira em potência máxima e translada junto** →
   a translação é escalonada para `0`.
4. Robô **gira no lugar, sem avançar**; como o alvo de heading anda ao contrário,
   o comando de giro inverte de sinal várias vezes → o tranco do "sambando".

### Evidência (simulação com o core 3.0.0 real + Mecanum real)

```
--- ATUAL   line(s,t).linear(s,t) ---
  hAlvoDoPath=  0,0°  hRobo= 88,3°  erro= -88,3°  ->  f= 0,000 s= 0,000 turn= -1,000
  hAlvoDoPath=  0,0°  hRobo= 86,6°  erro= -86,6°  ->  f= 0,000 s= 0,000 turn= -1,000

--- CORRETO line(s,t).heading(interp) ---
  hAlvoDoPath= 90,0°  hRobo= 90,0°  erro=   0,0°  ->  f= 0,671 s= 0,329 turn=  0,000
```

| variante | posição final | heading final | ciclos "girando sem andar" |
|---|---|---|---|
| `line(s,t).linear(s,t)` (atual) | (10.0, 103.0) ok | **90°** (era pra ser 0°) | **39** |
| `line(s,t).heading(interp)` (corrigido) | (10.3, 103.1) ok | **0°** ok | **0** |

---

## 2. Correção da causa raiz

### Opção A — interpolador próprio (imune ao bug da lib) — recomendada

```java
import com.pedropathing.paths.interpolator.Interpolator;
import com.pedropathing.utils.Angle;

private static Interpolator headingDe(Pose de, Pose ate) {
    return (curve, t) -> Angle.normalize(
            de.heading() + Angle.error(de.heading(), ate.heading()) * t);
}

// no AutoPath.GoToPosition:
Pose atual = follower.pose();
follower.follow(line(atual, locais[i]).heading(headingDe(atual, locais[i])));
```

### Opção B — Bézier (`BezierCurve` tem o `pathCompletion` correto)

```java
Pose meio = new Pose(
        (atual.x() + locais[i].x()) / 2,
        (atual.y() + locais[i].y()) / 2,
        atual.heading());
follower.follow(curve(atual, meio, locais[i]).linear(atual, locais[i]));
```

### Opção C — hack de uma linha (inverte os argumentos)

```java
follower.follow(line(atual, locais[i]).linear(locais[i], atual));
```

> [!WARNING]
> Funciona, mas é bomba-relógio: parece errado e alguém vai "consertar" de volta.
> Prefira a Opção A.

---

## 3. Outros erros encontrados

| # | Onde | Erro | Impacto |
|---|---|---|---|
| 1 | `TesteAutoPath.java:45` | `follower.manual()` é o **getter** de modo; o override do stick só funciona porque `GoToPosition()` roda antes no mesmo ciclo. Quando o path acaba o `Follower` entra em **HOLD** (`holdEnd = true`, `Follower.java:32`) e `follower.manual()` passa a retornar `false` → **o analógico nunca mais tira o robô do HOLD**. | Robô trava no destino; piloto perde o controle |
| 2 | `AutoPath.java` (classe) | O `AutoPathPlan.md` descreve `MANUAL / FOLLOWING / HOLDING`, timeout de 4 s, driver-override e `hold(targetPose)`. **Nada disso foi implementado** — a classe só dispara um path por botão, sem estado, sem cancelamento, sem hold. | Comportamento diferente do planejado |
| 3 | `HiveTracking.java:22` (não commitado) | `RED_DOWN` foi alterado para `90°`, mas `Ivy_model.java:45` ainda usa `p.of(56, 9, 180)` para a mesma posição física. **Um dos dois está errado.** | 90° de erro de pose inicial reproduz o mesmo sintoma (gira parado) |
| 4 | `Constants.java:38` (não commitado) | `xPodDirection = REVERSED`. Se estiver errado, o eixo X da odometria está espelhado e o follower se auto-corrige para sempre. | Robô não converge, odometria mentirosa |
| 5 | `TesteAutoPath.java:34-36` (não commitado) | `y = gamepad1.left_stick_y` sem o `-` → empurrar o analógico para frente **anda para trás**. O `Moviment.java` original usava `-left_stick_y`. | Manual invertido |
| 6 | `Constants.java:27-30` (não commitado) | Direções dos motores foram invertidas em relação ao commit. Precisa bater com o `Chassi.java` e com a odometria. | Movimento físico ≠ comando do follower |
| 7 | Geometria do path | A reta de (56, 9) até (10, 103) tem tangente em **116°**, mas o destino pede heading **0°**. O robô anda numa diagonal de 116° enquanto gira 90°. | Funciona, mas instável — prefira 2 fases (ir até perto → alinhar) ou Bézier |

---

## 4. Checklist de verificação no robô

- [ ] Confirmar fisicamente o heading em `RED_DOWN` (90° ou 180°?) e corrigir
      `HiveTracking` **e** `Ivy_model` para o mesmo valor.
- [ ] Andar para frente e conferir se o `X`/`Y` reportado pelo Pinpoint acompanha
      (se o X andar para o lado, `xPodDirection` está invertido).
- [ ] Conferir se `follower.manual(forward, lateral, heading)` com
      `forward = -left_stick_y` move o robô para frente.
- [ ] Rodar `TesteAutoPath` e olhar o telemetry: no `PATH`, o `Heading` deve
      **começar** igual ao heading atual e **terminar** no heading do destino.
- [ ] Checar que o robô **para no destino** e que o analógico ainda devolve o
      controle manual (erro #1).

---

## 5. Referência rápida da biblioteca (Pedro Pathing 3.0.0)

| Comportamento | Onde |
|---|---|
| `Curve.pathCompletion` default = `1-t` (invertido) | `paths/curves/Curve.java:27` |
| `BezierCurve.pathCompletion` correto | `paths/curves/bezier/BezierCurve.java:247` |
| `Interpolator.linear` depende de `pathCompletion` | `paths/interpolator/Interpolator.java:37` |
| `Path.linear(start, end, endT)` tb. invertido em `Line` | `paths/Path.java:83-92` |
| `Mecanum.maxScaling` limita giro+translação | `revhub/drivetrains/Mecanum.java:87` |
| `ForesightPowerAllocator.clampPowers` | `algorithm/ForesightPowerAllocator.java:83` |
| `holdEnd = true` (entra em HOLD ao terminar) | `follower/Follower.java:32` |
| `follower.manual()` é getter de modo | `follower/Follower.java:220` |

# Plano de Implementação: Detecção e Agrupamento de Pólens (Logitech C270)

## Visão Geral
Criar o processador de visão OpenCV `PollenDetector` (implementando `VisionProcessor`) para a webcam Logitech C270. 
O processador detecta pólens individuais (amarelos), agrupa-os por proximidade espacial (clusters de pólens), identifica o cluster com maior quantidade e calcula o desvio horizontal (`Tx`) desse grupo dominante para uso posterior no controle de tracking (`runToPolen(Tx)`).

---

## Arquivos Alvo
- **Implementação Principal**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/c270/ArtefactDetector.java` (ou `PollenDetector.java`).
- **OpMode de Teste/Exemplo**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Mech/c270/Functions.java` ou `TestPollenTracking.java`.

---

## Algoritmo Proposto

1. **Conversão de Cor & Segmentação**:
   - Converte BGR para HSV.
   - Aplica `Core.inRange` usando faixa para amarelo:
     - `LOWER_YELLOW = new Scalar(15, 100, 100)`
     - `UPPER_YELLOW = new Scalar(35, 255, 255)`
   - Operações morfológicas (`MORPH_OPEN`, `MORPH_CLOSE`) com kernel 5x5 para reduzir ruído de reflexo.

2. **Detecção dos Pólens Individuais**:
   - `Imgproc.findContours` em modo `RETR_EXTERNAL`.
   - Filtra contornos por área mínima (`MIN_AREA = 150` px) e máxima (`MAX_AREA = 15000` px).
   - Para cada contorno válido, calcula o centróide $(c_x, c_y)$ via momentos e o bounding box.

3. **Agrupamento em Clusters (Clustering por Proximidade)**:
   - Critério: se a distância euclidiana entre dois pólens for menor que `CLUSTER_DIST_PX` (ex: 80 px), pertencem ao mesmo grupo.
   - Algoritmo de União de Grupos (Union-Find / Agrupamento por vizinhança):
     - Atribui pólens a grupos conectados.
     - Calcula para cada grupo:
       - Quantidade de pólens (`size`).
       - Centroide médio do grupo $(\bar{X}, \bar{Y}) = (\frac{1}{N}\sum x_i, \frac{1}{N}\sum y_i)$.

4. **Seleção do Maior Grupo**:
   - Encontra o grupo com o maior número de pólens.
   - Em caso de empate no número de pólens, seleciona o grupo com $|\bar{X} - \text{centroX}|$ menor (mais alinhado com o robô).

5. **Cálculo de Tx**:
   - Desvio horizontal em relação ao centro da imagem:
     $$\text{Tx (normalizado)} = \frac{\bar{X} - (\text{width} / 2.0)}{\text{width} / 2.0} \quad \in [-1.0, 1.0]$$
     $$\text{Tx (graus)} = \text{Tx (normalizado)} \times \left(\frac{\text{FOV\_H}}{2}\right) \quad \text{(Logitech C270 FOV\_H } \approx 60^\circ)$$

6. **Interface Pública Thread-Safe**:
   - `int getTotalPollenCount()`: Total de pólens na câmera.
   - `int getLargestGroupCount()`: Quantidade de pólens no maior grupo.
   - `double getTx()`: Ângulo ou desvio horizontal do maior grupo.
   - `boolean hasTarget()`: Se há ao menos um grupo detectado.

---

## Verificação e Testes
- Compilação via Gradle: `./gradlew compileDebugSources`.
- Teste visual com stream do VisionPortal e telemetria no Driver Station / FTC Dashboard.

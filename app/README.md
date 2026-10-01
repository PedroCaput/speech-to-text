# Transcritor de Áudio WAV com Diarização para Word (.docx)

Aplicação desktop profissional em Java para Windows com interface gráfica moderna e minimalista (FlatLaf). Permite carregar arquivos de áudio no formato `.wav`, realizar a transcrição identificando os diferentes falantes (diarização) e exportar automaticamente os resultados para o formato Microsoft Word (`.docx`).

---

## 🚀 Funcionalidades

1. **Interface Gráfica Minimalista e Responsiva (Swing + FlatLaf):**
   - Seletor de arquivos de áudio `.wav` com filtro nativo.
   - Campo para salvar/persistir a chave da API (AssemblyAI).
   - Modo Demonstração/Teste para uso imediato sem necessidade de API Key.
   - Indicador de status dinâmico e barra de progresso em tempo real (`JProgressBar`).
   - Botões diretos para abrir o documento `.docx` gerado e a pasta de destino.

2. **Transcrição & Diarização de Voz (AssemblyAI API):**
   - Transcreve arquivos de áudio `.wav` com suporte total a Português (`pt`).
   - Identifica automaticamente a troca de vozes e nomeia os falantes sequencialmente como `Narrador 1`, `Narrador 2`, `Narrador 3`, etc.

3. **Formatação Estrita Microsoft Word (.docx) via Apache POI:**
   - Nome do falante em **negrito** (ex: `Narrador 1 - `).
   - Texto transcrito em seguida no mesmo parágrafo.
   - Quebra de linha dupla (parágrafo em branco) entre trocas de falante.

---

## 📁 Estrutura do Projeto

```
app/
├── pom.xml                                   # Configuração de dependências Maven
├── build_exe.bat                             # Script automatizado para compilar e gerar o .exe
├── README.md                                 # Documentação do projeto
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── transcriptor/
    │               ├── Main.java             # Ponto de entrada (Launcher)
    │               ├── config/
    │               │   └── AppConfig.java    # Persistência de configurações locais
    │               ├── model/
    │               │   └── Utterance.java    # Modelo de dados da fala
    │               ├── service/
    │               │   ├── AssemblyAIService.java # Transcrição e Diarização
    │               │   └── DocxExporter.java      # Geração do arquivo Word .docx
    │               └── gui/
    │                   └── MainFrame.java         # Interface Gráfica
    └── test/
        └── java/
            └── com/
                └── transcriptor/
                    └── service/
                        └── DocxExporterTest.java # Teste unitário da exportação DOCX
```

---

## 🛠️ Como Compilar e Rodar via Terminal / Maven

### Pré-requisitos
- **Java 17 LTS ou superior** instalado.
- **Apache Maven 3.8+** instalado.

### 1. Compilar o Projeto
```bash
mvn clean package
```

### 2. Executar o JAR Gerado
```bash
java -jar target/audio-transcriptor-1.0.0.jar
```

---

## 📦 Passo a Passo Completo: Geração do Executável Windows (.exe)

O executável gerado será **standalone**, ou seja, possui o **JRE (Java Runtime Environment) totalmente embutido**. O usuário final pode rodar a aplicação em qualquer computador com Windows 10/11 sem precisar instalar o Java manualmente.

### Método 1: Automático via Script Batch (Recomendado)

1. Abra o terminal na pasta do projeto.
2. Execute o script `build_exe.bat`:
   ```cmd
   build_exe.bat
   ```
3. O executável standalone será gerado na pasta `dist\AudioTranscriptor\AudioTranscriptor.exe`.

---

### Método 2: Manual utilizando `jpackage` (Nativo do JDK 14+)

#### Passo 1: Gerar o Fat-JAR com dependências
```bash
mvn clean package
```

#### Passo 2: Executar o `jpackage` para embutir o JRE e gerar a pasta executável
No PowerShell ou Prompt de Comando:
```powershell
& "C:\Program Files\Java\jdk-17\bin\jpackage.exe" `
  --name "AudioTranscriptor" `
  --app-version "1.0.0" `
  --input "target" `
  --main-jar "audio-transcriptor-1.0.0.jar" `
  --main-class "com.transcriptor.Main" `
  --dest "dist" `
  --type app-image `
  --vendor "Transcriptor" `
  --description "Transcritor de Áudio WAV para Word DOCX com Diarização"
```

A pasta `dist\AudioTranscriptor` conterá o `AudioTranscriptor.exe` pronto para distribuição!

---

### Método 3: Gerando um Instalador Windows (.exe / .msi) com Inno Setup ou Launch4j

Se você deseja gerar um **Instalador de Arquivo Único (`Setup.exe`)**:

#### Opção A: Usando Inno Setup (Recomendado para distribuição profissional)
1. Baixe e instale o [Inno Setup](https://jrsoftware.org/isinfo.php) (Gratuito).
2. Abra o Inno Setup Compiler e selecione **"Create a new script file using the Script Wizard"**.
3. Defina o nome do aplicativo: `AudioTranscriptor`.
4. Em **"Application Main Executable File"**, selecione o arquivo `dist\AudioTranscriptor\AudioTranscriptor.exe`.
5. Em **"Other application files"**, clique em **Add Folder** e inclua toda a pasta `dist\AudioTranscriptor` (para levar a pasta `runtime/` e `app/`).
6. Clique em **Compile**. O Inno Setup gerará um instalador `mysetup.exe` compacto de um único arquivo.

#### Opção B: Usando `jpackage --type exe`
Caso tenha a ferramenta **WiX Toolset v3** instalada no Windows, você pode rodar diretamente:
```powershell
jpackage --name "AudioTranscriptorSetup" --app-version "1.0.0" --input "target" --main-jar "audio-transcriptor-1.0.0.jar" --main-class "com.transcriptor.Main" --dest "dist" --type exe --win-shortcut --win-menu
```

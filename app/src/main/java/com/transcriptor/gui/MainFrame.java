package com.transcriptor.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.transcriptor.config.AppConfig;
import com.transcriptor.model.Utterance;
import com.transcriptor.service.AssemblyAIService;
import com.transcriptor.service.DocxExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * Interface Gráfica Principal do Transcritor de Áudio (Swing + FlatLaf).
 * Design moderno, minimalista e intuitivo para o usuário.
 */
public class MainFrame extends JFrame {

    private JTextField txtFilePath;
    private JTextField txtApiKey;
    private JCheckBox chkDemoMode;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    private JButton btnSelectFile;
    private JButton btnStartProcess;
    private JButton btnOpenDocx;
    private JButton btnOpenFolder;

    private File selectedAudioFile;
    private File generatedDocxFile;
    private final AssemblyAIService aiService;

    public MainFrame() {
        this.aiService = new AssemblyAIService();
        initUI();
    }

    private void initUI() {
        setTitle("Transcritor de Áudio com Diarização - WAV para DOCX");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 480);
        setLocationRelativeTo(null); // Centraliza na tela
        setResizable(false);

        // Painel Principal com margens
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. Cabeçalho / Título
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Transcritor de Áudio & Diarização de Voz");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel lblSubtitle = new JLabel("Carregue um arquivo .wav para transcrição automática com falantes no Word (.docx)");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 2. Seção de Seleção de Arquivo
        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setOpaque(false);
        filePanel.setBorder(BorderFactory.createTitledBorder("1. Seleção do Arquivo de Áudio"));

        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        txtFilePath.setText("Nenhum arquivo .wav selecionado");
        txtFilePath.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        btnSelectFile = new JButton("Selecionar Arquivo (.wav)");
        btnSelectFile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSelectFile.addActionListener(this::onSelectFile);

        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnSelectFile, BorderLayout.EAST);
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 3. Seção de Configuração da API
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setOpaque(false);
        configPanel.setBorder(BorderFactory.createTitledBorder("2. Configurações de Transcrição"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblKey = new JLabel("Chave da API (AssemblyAI):");
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        configPanel.add(lblKey, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0;
        txtApiKey = new JTextField(AppConfig.getApiKey());
        txtApiKey.setFont(new Font("Consolas", Font.PLAIN, 12));
        configPanel.add(txtApiKey, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        chkDemoMode = new JCheckBox("Usar Modo Demonstração / Teste (sem necessidade de API Key)");
        chkDemoMode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkDemoMode.setSelected(txtApiKey.getText().trim().isEmpty());
        chkDemoMode.addActionListener(e -> txtApiKey.setEnabled(!chkDemoMode.isSelected()));
        txtApiKey.setEnabled(!chkDemoMode.isSelected());
        configPanel.add(chkDemoMode, gbc);

        mainPanel.add(configPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 4. Seção de Progresso e Status
        JPanel progressPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createTitledBorder("3. Progresso do Processamento"));

        lblStatus = new JLabel("Status: Aguardando seleção de arquivo...", JLabel.LEFT);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(0, 120, 215));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        progressPanel.add(lblStatus);
        progressPanel.add(progressBar);
        mainPanel.add(progressPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 5. Painel de Ações e Botões
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        btnStartProcess = new JButton("Iniciar Transcrição & Exportar DOCX");
        btnStartProcess.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStartProcess.setEnabled(false); // Ativado somente ao escolher o arquivo
        btnStartProcess.addActionListener(this::onStartProcessing);

        btnOpenDocx = new JButton("Abrir Documento Word");
        btnOpenDocx.setEnabled(false);
        btnOpenDocx.addActionListener(e -> openFile(generatedDocxFile));

        btnOpenFolder = new JButton("Abrir Pasta");
        btnOpenFolder.setEnabled(false);
        btnOpenFolder.addActionListener(e -> openFile(selectedAudioFile != null ? selectedAudioFile.getParentFile() : null));

        actionPanel.add(btnOpenFolder);
        actionPanel.add(btnOpenDocx);
        actionPanel.add(btnStartProcess);

        mainPanel.add(actionPanel);

        add(mainPanel);
    }

    private void onSelectFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar Arquivo de Áudio WAV");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Áudio WAV (*.wav)", "wav"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedAudioFile = chooser.getSelectedFile();
            txtFilePath.setText(selectedAudioFile.getAbsolutePath());
            txtFilePath.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnStartProcess.setEnabled(true);
            lblStatus.setText("Status: Arquivo selecionado! Pronto para iniciar.");
            lblStatus.setForeground(new Color(0, 150, 0));
            progressBar.setValue(0);
            btnOpenDocx.setEnabled(false);
            btnOpenFolder.setEnabled(false);
        }
    }

    private void onStartProcessing(ActionEvent e) {
        if (selectedAudioFile == null || !selectedAudioFile.exists()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um arquivo .wav válido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String apiKey = chkDemoMode.isSelected() ? "DEMO" : txtApiKey.getText().trim();
        if (!chkDemoMode.isSelected()) {
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, insira a chave da API da AssemblyAI ou marque a opção 'Modo Demonstração'.", "API Key Necessária", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AppConfig.saveApiKey(apiKey);
        }

        // Desabilitar botões durante o processamento
        btnSelectFile.setEnabled(false);
        btnStartProcess.setEnabled(false);
        btnOpenDocx.setEnabled(false);
        btnOpenFolder.setEnabled(false);
        txtApiKey.setEnabled(false);
        chkDemoMode.setEnabled(false);

        // Executar processamento em background (SwingWorker)
        SwingWorker<List<Utterance>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Utterance> doInBackground() throws Exception {
                return aiService.transcribeAndDiarize(selectedAudioFile, apiKey, (status, percent) -> {
                    publish(status);
                    setProgress(percent);
                });
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String latestStatus = chunks.get(chunks.size() - 1);
                    lblStatus.setText("Status: " + latestStatus);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Utterance> utterances = get();

                    // Gerar nome do arquivo .docx no mesmo diretório do áudio
                    String audioPath = selectedAudioFile.getAbsolutePath();
                    String docxPath = audioPath.toLowerCase().endsWith(".wav")
                            ? audioPath.substring(0, audioPath.length() - 4) + "_transcricao.docx"
                            : audioPath + "_transcricao.docx";

                    generatedDocxFile = new File(docxPath);
                    lblStatus.setText("Status: Gerando arquivo Word (.docx)...");
                    progressBar.setValue(95);

                    DocxExporter.exportToDocx(utterances, generatedDocxFile);

                    lblStatus.setText("Status: Concluído com sucesso!");
                    lblStatus.setForeground(new Color(0, 150, 0));
                    progressBar.setValue(100);

                    btnOpenDocx.setEnabled(true);
                    btnOpenFolder.setEnabled(true);

                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Transcrição e Diarização concluídas com sucesso!\n\nArquivo salvo em:\n" + generatedDocxFile.getAbsolutePath(),
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblStatus.setText("Status: Erro durante o processamento.");
                    lblStatus.setForeground(Color.RED);
                    progressBar.setValue(0);

                    String errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ocorreu um erro ao transcrever o áudio:\n" + errorMessage,
                            "Erro no Processamento", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnSelectFile.setEnabled(true);
                    btnStartProcess.setEnabled(true);
                    chkDemoMode.setEnabled(true);
                    txtApiKey.setEnabled(!chkDemoMode.isSelected());
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }

    private void openFile(File file) {
        if (file != null && file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível abrir o arquivo/pasta:\n" + ex.getMessage(), "Erro ao Abrir", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

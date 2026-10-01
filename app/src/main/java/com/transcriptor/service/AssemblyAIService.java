package com.transcriptor.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.transcriptor.model.Utterance;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Serviço de Transcrição de Áudio com Diarização de Falantes via AssemblyAI API.
 */
public class AssemblyAIService {

    private static final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";

    private final OkHttpClient client;

    public interface ProgressCallback {
        void onProgress(String statusMessage, int percentage);
    }

    public AssemblyAIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS) // Upload de arquivos grandes
                .build();
    }

    /**
     * Transcreve um arquivo de áudio WAV identificando os falantes (Diarização).
     *
     * @param audioFile Arquivo .wav local
     * @param apiKey Chave de API da AssemblyAI (ou "DEMO" para modo demonstração)
     * @param callback Callback para atualização da interface gráfica
     * @return Lista de falas formatadas
     * @throws Exception Se ocorrer erro na comunicação ou processamento
     */
    public List<Utterance> transcribeAndDiarize(File audioFile, String apiKey, ProgressCallback callback) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.trim().equalsIgnoreCase("DEMO")) {
            return runDemoMode(callback);
        }

        String cleanKey = apiKey.trim();

        // 1. Upload do Arquivo de Áudio
        if (callback != null) callback.onProgress("Enviando arquivo de áudio...", 15);
        String audioUrl = uploadAudio(audioFile, cleanKey);

        // 2. Solicitar Transcrição com Diarização (speaker_labels = true)
        if (callback != null) callback.onProgress("Solicitando transcrição e identificação de falantes...", 35);
        String transcriptId = requestTranscript(audioUrl, cleanKey);

        // 3. Polling do Progresso
        if (callback != null) callback.onProgress("Processando inteligência artificial (diarização)...", 50);
        JsonObject resultJson = pollTranscriptResult(transcriptId, cleanKey, callback);

        // 4. Processar e mapear falantes (A -> Narrador 1, B -> Narrador 2, etc.)
        if (callback != null) callback.onProgress("Mapeando falantes e organizando o texto...", 90);
        return parseUtterances(resultJson);
    }

    private String uploadAudio(File audioFile, String apiKey) throws IOException {
        RequestBody requestBody = RequestBody.create(audioFile, MediaType.parse("audio/wav"));

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .addHeader("authorization", apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Falha no upload do áudio (Código " + response.code() + "): " + response.body().string());
            }

            String responseData = response.body().string();
            JsonObject json = JsonParser.parseString(responseData).getAsJsonObject();
            return json.get("upload_url").getAsString();
        }
    }

    private String requestTranscript(String audioUrl, String apiKey) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("audio_url", audioUrl);
        payload.addProperty("speaker_labels", true);
        payload.addProperty("language_code", "pt");

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(TRANSCRIPT_URL)
                .addHeader("authorization", apiKey)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Falha ao iniciar transcrição (Código " + response.code() + "): " + response.body().string());
            }

            String responseData = response.body().string();
            JsonObject json = JsonParser.parseString(responseData).getAsJsonObject();
            return json.get("id").getAsString();
        }
    }

    private JsonObject pollTranscriptResult(String transcriptId, String apiKey, ProgressCallback callback) throws Exception {
        String pollingUrl = TRANSCRIPT_URL + "/" + transcriptId;
        int currentProgress = 50;

        while (true) {
            Request request = new Request.Builder()
                    .url(pollingUrl)
                    .addHeader("authorization", apiKey)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Erro na verificação de status: " + response.code());
                }

                String responseData = response.body().string();
                JsonObject json = JsonParser.parseString(responseData).getAsJsonObject();
                String status = json.get("status").getAsString();

                if ("completed".equalsIgnoreCase(status)) {
                    return json;
                } else if ("error".equalsIgnoreCase(status)) {
                    String errorMsg = json.has("error") ? json.get("error").getAsString() : "Erro desconhecido na transcrição.";
                    throw new RuntimeException("Erro da API de Transcrição: " + errorMsg);
                } else {
                    currentProgress = Math.min(85, currentProgress + 5);
                    if (callback != null) callback.onProgress("Processando transcrição (" + status + ")...", currentProgress);
                    Thread.sleep(3000); // Aguarda 3 segundos entre verificações
                }
            }
        }
    }

    private List<Utterance> parseUtterances(JsonObject json) {
        List<Utterance> utterances = new ArrayList<>();
        if (!json.has("utterances") || json.get("utterances").isJsonNull()) {
            // Caso não haja separação em utterances, pega a transcrição inteira como Narrador 1
            String text = json.has("text") ? json.get("text").getAsString() : "";
            utterances.add(new Utterance("Narrador 1", text));
            return utterances;
        }

        JsonArray utterancesArray = json.getAsJsonArray("utterances");
        Map<String, String> speakerMapping = new LinkedHashMap<>();
        int speakerCounter = 1;

        for (JsonElement elem : utterancesArray) {
            JsonObject uObj = elem.getAsJsonObject();
            String rawSpeaker = uObj.get("speaker").getAsString(); // Ex: "A", "B"
            String text = uObj.get("text").getAsString();

            // Mapear "A" -> "Narrador 1", "B" -> "Narrador 2", etc.
            String formattedSpeaker = speakerMapping.computeIfAbsent(rawSpeaker, k -> "Narrador " + (speakerMapping.size() + 1));

            utterances.add(new Utterance(formattedSpeaker, text));
        }

        return utterances;
    }

    /**
     * Modo de demonstração com simulação de fala e progresso em tempo real para testes imediatos.
     */
    private List<Utterance> runDemoMode(ProgressCallback callback) throws Exception {
        if (callback != null) callback.onProgress("Modo Demonstração: Carregando áudio...", 20);
        Thread.sleep(1000);
        if (callback != null) callback.onProgress("Modo Demonstração: Processando voz dos falantes...", 50);
        Thread.sleep(1500);
        if (callback != null) callback.onProgress("Modo Demonstração: Finalizando diarização...", 85);
        Thread.sleep(1000);

        List<Utterance> demo = new ArrayList<>();
        demo.add(new Utterance("Narrador 1", "Boa noite. Quanta saudade."));
        demo.add(new Utterance("Narrador 2", "E eu posso perguntar com quem eu falo?"));
        demo.add(new Utterance("Narrador 1", "Sim, irmã, é o Marmi. Os irmãos pediram pra que entreguem uma mensagem aos meus familiares e à casa. Após entregar a mensagem eles virão esclarecer. Quanta saudade de todos. Agradecimento à casa, aos irmãos com os quais, quando estava encarnado, convivi tantos anos. Tenho só gratidão."));
        demo.add(new Utterance("Narrador 2", "Fique com Deus também. Muito obrigada pela mensagem."));

        return demo;
    }
}

package com.transcriptor.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Armazena e persiste preferências da aplicação (ex: API Key da AssemblyAI).
 */
public class AppConfig {

    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".audio_transcriptor.properties";
    private static final String API_KEY_PROP = "assemblyai.api.key";

    public static String getApiKey() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                return props.getProperty(API_KEY_PROP, "");
            } catch (IOException e) {
                // Silencioso
            }
        }
        return "";
    }

    public static void saveApiKey(String apiKey) {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                // Ignorar
            }
        }
        props.setProperty(API_KEY_PROP, apiKey != null ? apiKey.trim() : "");
        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "Configuracoes do Transcritor de Audio");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

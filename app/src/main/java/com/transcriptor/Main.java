package com.transcriptor;

import com.formdev.flatlaf.FlatDarkLaf;
import com.transcriptor.gui.MainFrame;

import javax.swing.*;

/**
 * Ponto de entrada da aplicação Transcritor de Áudio.
 */
public class Main {

    public static void main(String[] args) {
        // Configurar Look & Feel Moderno Minimalista (FlatLaf Dark)
        try {
            // Habilitar suporte a High-DPI no Windows 10/11
            System.setProperty("flatlaf.uiScale", "1.0");
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o tema FlatLaf: " + e.getMessage());
        }

        // Inicializar a Interface Gráfica na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

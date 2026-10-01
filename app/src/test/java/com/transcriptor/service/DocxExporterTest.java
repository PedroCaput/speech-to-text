package com.transcriptor.service;

import com.transcriptor.model.Utterance;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocxExporterTest {

    @Test
    public void testDocxExportFormat() throws Exception {
        File tempDocx = File.createTempFile("test_transcription", ".docx");
        tempDocx.deleteOnExit();

        List<Utterance> utterances = new ArrayList<>();
        utterances.add(new Utterance("Narrador 1", "Boa noite. Quanta saudade."));
        utterances.add(new Utterance("Narrador 2", "E eu posso perguntar com quem eu falo?"));

        DocxExporter.exportToDocx(utterances, tempDocx);

        assertTrue(tempDocx.exists(), "O arquivo DOCX deve ser gerado.");
        assertTrue(tempDocx.length() > 0, "O arquivo DOCX nao deve estar vazio.");

        try (FileInputStream fis = new FileInputStream(tempDocx);
             XWPFDocument doc = new XWPFDocument(fis)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            assertNotNull(paragraphs);
            assertTrue(paragraphs.size() >= 3, "Deve conter os parágrafos das falas e espaçamento.");

            // Primeiro parágrafo: Narrador 1
            XWPFParagraph p1 = paragraphs.get(0);
            assertTrue(p1.getText().contains("Narrador 1 - Boa noite. Quanta saudade."));
            assertTrue(p1.getRuns().get(0).isBold(), "O nome do Narrador deve estar em negrito.");
        }
    }
}

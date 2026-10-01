package com.transcriptor.service;

import com.transcriptor.model.Utterance;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Serviço responsável por exportar a lista de falas (Utterances) para um documento Word (.docx)
 * seguindo estritamente a formatação exigida.
 */
public class DocxExporter {

    /**
     * Exporta a lista de utterances para um arquivo .docx no caminho especificado.
     *
     * @param utterances Lista de falas transcritas com os falantes.
     * @param outputFile Arquivo de destino (.docx).
     * @throws IOException Se houver falha ao criar ou salvar o arquivo.
     */
    public static void exportToDocx(List<Utterance> utterances, File outputFile) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            
            for (int i = 0; i < utterances.size(); i++) {
                Utterance u = utterances.get(i);

                // Criar parágrafo da fala
                XWPFParagraph p = document.createParagraph();
                p.setSpacingAfter(0); // Sem espaçamento extra nativo do POI, para controle manual por parágrafo em branco

                // Run do Narrador (em negrito)
                XWPFRun speakerRun = p.createRun();
                speakerRun.setBold(true);
                speakerRun.setFontFamily("Calibri");
                speakerRun.setFontSize(11);
                speakerRun.setText(u.getSpeaker() + " - ");

                // Run do Texto Transcrito
                XWPFRun textRun = p.createRun();
                textRun.setBold(false);
                textRun.setFontFamily("Calibri");
                textRun.setFontSize(11);
                textRun.setText(u.getText());

                // Sempre que houver a fala de um narrador, insere um parágrafo em branco (quebra de linha dupla)
                // para separar da próxima fala ou manter o padrão estrito solicitado
                if (i < utterances.size()) {
                    XWPFParagraph emptyP = document.createParagraph();
                    emptyP.setSpacingAfter(0);
                }
            }

            // Salvar documento no disco
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }
    }
}

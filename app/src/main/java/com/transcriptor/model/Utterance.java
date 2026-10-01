package com.transcriptor.model;

/**
 * Representa um segmento de fala transcrito associado a um falante (Narrador).
 */
public class Utterance {
    private String speaker;
    private String text;

    public Utterance(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return speaker + " - " + text;
    }
}

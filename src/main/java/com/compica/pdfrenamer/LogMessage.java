package com.compica.pdfrenamer;

public class LogMessage {
    private String event;
    private String item;

    public LogMessage(String event, String item) {
        this.event = event;
        this.item = item;
    }

    @Override
    public String toString() {
        return "event=" + event + " item=" + item;
    }
}
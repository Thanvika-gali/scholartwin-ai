package com.aischolartwin.entity;

public enum DocumentFormat {
    PDF,
    DOCX;

    public static DocumentFormat fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String clean = extension.trim().toLowerCase();
        if (clean.startsWith(".")) {
            clean = clean.substring(1);
        }
        if ("pdf".equals(clean)) {
            return PDF;
        }
        if ("docx".equals(clean)) {
            return DOCX;
        }
        return null;
    }
}


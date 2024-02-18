package com.server.exception;

public class FailedToEnrichCsvFile extends RuntimeException {

    public FailedToEnrichCsvFile() {
        super("Failed to enrich csv");
    }
}

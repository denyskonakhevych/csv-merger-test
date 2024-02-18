package com.server.csv;

import com.opencsv.bean.exceptionhandler.CsvExceptionHandler;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingCsvExceptionHandler implements CsvExceptionHandler {

    @Override
    public CsvException handleException(final CsvException e) {
        log.warn("Invalid record at line {}", e.getLineNumber());
        return null;
    }
}

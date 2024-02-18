package com.server.csv;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.util.Arrays;

public class HeaderColumnNameToLowerCaseMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        String[] header = super.generateHeader(bean);
        return Arrays.stream(header)
                .map(String::toLowerCase)
                .toArray(String[]::new);
    }
}

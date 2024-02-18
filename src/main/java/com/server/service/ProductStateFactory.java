package com.server.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.server.model.Product;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductStateFactory {

    public ProductState createProductState(final Resource productResource) throws IOException {
        final Map<Integer, Product> data = new HashMap<>();
        try (final Reader reader = Files.newBufferedReader(productResource.getFile().toPath());
             final CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().build())
                     .build()) {
            final CsvToBean<Product> csvToBean = new CsvToBeanBuilder<Product>(csvReader)
                    .withType(Product.class)
                    .build();
            for (final Product product : csvToBean) {
                data.put(product.getProductId(), product);
            }
        }
        return ProductState.of(data);
    }
}

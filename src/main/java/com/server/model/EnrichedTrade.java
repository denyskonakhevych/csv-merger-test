package com.server.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EnrichedTrade {

    @CsvDate(value = "yyyyMMdd")
    @CsvBindByName(column = "date")
    private LocalDate date;
    @CsvBindByName(column = "product_name")
    private String productName;
    @CsvBindByName(column = "currency")
    private String currency;
    @CsvNumber("#.##")
    @CsvBindByName(column = "price")
    private BigDecimal price;
}

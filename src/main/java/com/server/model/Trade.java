package com.server.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Trade {

    @CsvDate(value = "yyyyMMdd")
    @CsvBindByName(column = "date", required = true)
    private LocalDate date;
    @CsvBindByName(column = "product_id", required = true)
    private int productId;
    @CsvBindByName(column = "currency", required = true)
    private String currency;
    @CsvNumber("#.##")
    @CsvBindByName(column = "price", required = true)
    private BigDecimal price;
}

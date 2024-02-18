package com.server.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @CsvBindByName(column = "product_id")
    private Integer productId;
    @CsvBindByName(column = "product_name")
    private String productName;
}

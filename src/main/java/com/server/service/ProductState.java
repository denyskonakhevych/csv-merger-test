package com.server.service;

import com.server.model.Product;
import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Value(staticConstructor = "of")
public class ProductState {

    @Getter
    Map<Integer, Product> data;

    private ProductState(final Map<Integer, Product> data) {
        this.data = Collections.unmodifiableMap(Objects.requireNonNullElse(data, Collections.emptyMap()));
    }
}

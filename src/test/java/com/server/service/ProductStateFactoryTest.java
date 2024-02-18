package com.server.service;

import com.server.model.Product;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

public class ProductStateFactoryTest {

    private final ProductStateFactory factory = new ProductStateFactory();

    @Test
    @SneakyThrows
    public void givenEmptyCsvWillResultEmptyState() {
        final ProductState productState = factory.createProductState(new ClassPathResource("empty-product.csv"));
        Assertions.assertThat(productState).isNotNull();
        Assertions.assertThat(productState.getData()).isEmpty();
    }

    @Test
    @SneakyThrows
    public void givenNonEmptyCsvWillResultParsedState() {
        final ProductState productState = factory.createProductState(new ClassPathResource("product.csv"));
        Assertions.assertThat(productState).isNotNull();
        Assertions.assertThat(productState.getData()).containsExactly(
                Map.entry(1, new Product(1, "Treasury Bills Domestic")),
                Map.entry(2, new Product(2, "Corporate Bonds Domestic")),
                Map.entry(3, new Product(3, "REPO Domestic"))
        );
    }
}

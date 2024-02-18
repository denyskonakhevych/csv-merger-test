package com.server.service;

import com.server.model.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class InMemoryProductReadServiceTest {

    @Test
    public void whenSearchingNonExistingProductsShouldReturnEmptyResponse() {
        final InMemoryProductReadService inMemoryProductReadService = new InMemoryProductReadService(ProductState.of(Collections.emptyMap()));
        final Map<Integer, Product> retrievedProductsById = inMemoryProductReadService.findAllById(Set.of(1, 2, 3));
        Assertions.assertThat(retrievedProductsById).isEmpty();
    }

    @Test
    public void whenSearchingNonProductsShouldReturnExistingOnes() {
        final Product product2 = new Product(2, "Product 2");
        final Product product3 = new Product(3, "Product 3");
        final InMemoryProductReadService inMemoryProductReadService = new InMemoryProductReadService(
                ProductState.of(Map.of(2, product2, 3, product3)));
        final Map<Integer, Product> retrievedProductsById = inMemoryProductReadService.findAllById(Set.of(1, 2));
        Assertions.assertThat(retrievedProductsById).isNotEmpty();
        Assertions.assertThat(retrievedProductsById).containsExactly(Map.entry(2, product2));
    }
}

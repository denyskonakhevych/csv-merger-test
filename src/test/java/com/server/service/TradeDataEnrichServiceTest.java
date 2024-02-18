package com.server.service;

import com.server.exception.InvalidCsvException;
import com.server.model.Product;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TradeDataEnrichServiceTest {

    private TradeDataEnrichService service;

    @BeforeEach
    @SneakyThrows
    public void init() {
        final Map<Integer, Product> productIdToProduct = Map.of(
                1, new Product(1, "Treasury Bills Domestic"),
                2, new Product(2, "Corporate Bonds Domestic"),
                3, new Product(3, "REPO Domestic"),
                4, new Product(4, "Interest rate swaps International"),
                5, new Product(5, "OTC Index Option"),
                6, new Product(6, "Currency Options"),
                7, new Product(7, "Reverse Repos International"),
                8, new Product(8, "REPO International"),
                9, new Product(9, "766A_CORP BD"),
                10, new Product(10, "766B_CORP BD")
        );
        ProductReadService productReadService = new InMemoryProductReadService(ProductState.of(productIdToProduct));
        service = new TradeDataEnrichService(200, productReadService);
    }

    @Test
    @SneakyThrows
    public void whenEnrichingCsvWithInvalidRecordShouldIgnoreInvalidRecordsAndProceedOthers() {
        final ClassPathResource tradeCsv = new ClassPathResource("trade.csv");
        final Path enrichedTrades = service.enrich(tradeCsv.getFile().toPath());
        final long mismatch = Files.mismatch(enrichedTrades, new ClassPathResource("expected-enriched-trade.csv").getFile().toPath());
        Files.deleteIfExists(enrichedTrades);
        Assertions.assertThat(mismatch).isEqualTo(-1);
    }

    @Test
    @SneakyThrows
    public void whenProvidedCsvWithWrongDataShouldThrowException() {
        final ClassPathResource tradeCsv = new ClassPathResource("product.csv");
        Assertions.assertThatThrownBy(() -> service.enrich(tradeCsv.getFile().toPath())).isInstanceOf(InvalidCsvException.class);
    }
}

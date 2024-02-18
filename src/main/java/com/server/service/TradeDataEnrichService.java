package com.server.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.server.csv.HeaderColumnNameToLowerCaseMappingStrategy;
import com.server.csv.LoggingCsvExceptionHandler;
import com.server.exception.FailedToEnrichCsvFile;
import com.server.exception.InvalidCsvException;
import com.server.model.EnrichedTrade;
import com.server.model.Product;
import com.server.model.Trade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradeDataEnrichService {

    public static final String MISSING_PRODUCT_NAME = "Missing Product Name";

    public static final FixedOrderComparator<String> ENRICHED_TRADE_COLUMN_ORDER_COMPARATOR = new FixedOrderComparator<>("DATE", "PRODUCT_NAME", "CURRENCY", "PRICE");
    public static final LoggingCsvExceptionHandler LOGGING_CSV_EXCEPTION_HANDLER = new LoggingCsvExceptionHandler();

    private final Integer bufferSize;
    private final ProductReadService productReadService;

    public TradeDataEnrichService(final @Value("${csv.enrichment.batch-size}") Integer bufferSize,
                                  final ProductReadService productReadService) {
        this.bufferSize = bufferSize;
        this.productReadService = productReadService;
    }

    /**
     * Enriches the CSV data obtained from the HttpServletRequest by adding additional information
     * to each trade entry and writes the enriched data to the HttpServletResponse in a pass-through mode.
     *
     * @param request  The HttpServletRequest containing the CSV data.
     * @param response The HttpServletResponse for writing enriched CSV data.
     * @throws FailedToEnrichCsvFile If an error occurs during CSV processing.
     */
    @SneakyThrows
    public void enrich(final HttpServletRequest request, final HttpServletResponse response) {
        try (final Reader reader = request.getReader();
             final PrintWriter writer = response.getWriter()) {
            enrich(reader, writer);
        }
    }

    /**
     * Enriches the CSV data located at the given path by adding additional information
     * to each trade entry and returns the path to the enriched CSV file.
     *
     * @param path The path to the CSV file to be enriched.
     * @return The path to the enriched CSV file.
     * @throws FailedToEnrichCsvFile If an error occurs during CSV processing.
     */
    public Path enrich(final Path path) throws Exception {
        final File tempFile = File.createTempFile("test", UUID.randomUUID() + ".csv");
        tempFile.deleteOnExit();
        try (final Reader reader = Files.newBufferedReader(path);
             final Writer writer = Files.newBufferedWriter(tempFile.toPath())) {
            enrich(reader, writer);
        } catch (final Exception e) {
            Files.deleteIfExists(tempFile.toPath());
            throw e;
        }
        return tempFile.toPath();
    }

    /**
     * Enriches the provided CSV data by adding additional information to each trade entry
     * and writes the enriched data to the provided writer.
     *
     * @param reader The reader for reading CSV data.
     * @param writer The writer for writing enriched CSV data.
     * @throws InvalidCsvException If csv was considered as invalid.
     * @throws FailedToEnrichCsvFile  If unpredictable exception occurred while enriching csv.
     */
    public void enrich(final Reader reader, final Writer writer) {
        final Instant start = Instant.now();
        int entriesCounter = 0;
        try (final CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().build())
                .build();
             final ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                     .build()
        ) {
            final CsvToBean<Trade> csvToBean = getCsvToBean(csvReader);
            final StatefulBeanToCsv<EnrichedTrade> beanToCsv = getCsvToBean(csvWriter);
            final List<Trade> buffer = new ArrayList<>(bufferSize);
            for (final Trade trade : csvToBean) {
                buffer.add(trade);
                if (buffer.size() >= bufferSize) {
                    flushBuffer(beanToCsv, buffer);
                    entriesCounter += buffer.size();
                    buffer.clear();
                }
            }
            flushBuffer(beanToCsv, buffer);
            entriesCounter += buffer.size();
        } catch (final Exception e) {
            log.warn("Failed to enrich csv", e);
            if (e.getCause() instanceof CsvRequiredFieldEmptyException) {
                log.warn("Invalid csv");
                throw new InvalidCsvException(e.getCause());
            }
            throw new FailedToEnrichCsvFile();
        }
        log.debug("Successfully enriched csv of size {} in {}", entriesCounter, Duration.between(start, Instant.now()));
    }

    /**
     * Flushes the buffer of trades by enriching each trade entry with additional information,
     * such as product name, and writing the enriched data to the CSV.
     *
     * @param beanToCsv The StatefulBeanToCsv object for writing enriched trade data.
     * @param buffer    The list of trades to be enriched and written to beanToCsv.
     * @throws CsvException If an error occurs during CSV processing.
     */
    private void flushBuffer(final StatefulBeanToCsv<EnrichedTrade> beanToCsv, final List<Trade> buffer) throws CsvException {
        if (buffer.isEmpty()) {
            return;
        }
        final Set<Integer> productIds = buffer.stream()
                .map(Trade::getProductId)
                .collect(Collectors.toSet());
        final Map<Integer, Product> resolvedProducts = productReadService.findAllById(productIds);
        for (final Trade trade : buffer) {
            final EnrichedTrade enrichedTrade = EnrichedTrade.builder()
                    .date(trade.getDate())
                    .productName(resolveProductName(trade.getProductId(), resolvedProducts))
                    .currency(trade.getCurrency())
                    .price(trade.getPrice())
                    .build();
            beanToCsv.write(enrichedTrade);
        }
    }

    /**
     * Constructs a StatefulBeanToCsv object with default configuration for the provided ICSVWriter
     * for writing enriched trade data to CSV.
     *
     * @param csvWriter The ICSVWriter object to be used for CSV writing.
     * @return The constructed StatefulBeanToCsv object.
     */
    private static StatefulBeanToCsv<EnrichedTrade> getCsvToBean(final ICSVWriter csvWriter) {
        var mappingStrategy = new HeaderColumnNameToLowerCaseMappingStrategy<EnrichedTrade>();
        mappingStrategy.setType(EnrichedTrade.class);
        mappingStrategy.setColumnOrderOnWrite(ENRICHED_TRADE_COLUMN_ORDER_COMPARATOR);
        return new StatefulBeanToCsvBuilder<EnrichedTrade>(csvWriter)
                .withApplyQuotesToAll(false)
                .withOrderedResults(true)
                .withMappingStrategy(mappingStrategy)
                .build();
    }

    /**
     * Constructs a CsvToBean object with default configuration for the provided CSVReader
     * for reading trade data from CSV.
     *
     * @param csvReader The CSVReader object to be used for CSV reading.
     * @return The constructed CsvToBean object.
     */
    private static CsvToBean<Trade> getCsvToBean(final CSVReader csvReader) {
        return new CsvToBeanBuilder<Trade>(csvReader)
                .withType(Trade.class)
                .withExceptionHandler(LOGGING_CSV_EXCEPTION_HANDLER)
                .build();
    }

    private static String resolveProductName(final int productId, final Map<Integer, Product> resolvedProducts) {
        if (resolvedProducts.containsKey(productId)) {
            return resolvedProducts.get(productId).getProductName();
        } else {
            log.warn("Could not resolve product with id {}", productId);
            return MISSING_PRODUCT_NAME;
        }
    }
}

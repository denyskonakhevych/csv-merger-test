package com.server.configuration;

import com.server.service.ProductState;
import com.server.service.ProductStateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Slf4j
@Configuration
public class ApplicationConfiguration {

    @Bean
    public ProductState productState(final ProductStateFactory productStateFactory,
                                     @Value("${csv.data.product}") final Resource productResource) throws IOException {
        return productStateFactory.createProductState(productResource);
    }
}

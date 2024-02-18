package com.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class TradeDataEnrichControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .alwaysDo(print())
                .build();
    }

    @Test
    @SneakyThrows
    public void whenEnrichingCsvShouldReturnEnrichedData() {
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/enrich")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .accept(MediaType.parseMediaType("text/csv"))
                        .content(Files.readAllBytes(new ClassPathResource("trade.csv").getFile().toPath()))
                )
                .andExpect(status().isOk())
                .andReturn();
        final String contentAsString = mvcResult.getResponse().getContentAsString();
        Assertions.assertThat(contentAsString).isEqualTo(Files.readString(new ClassPathResource("expected-enriched-trade.csv").getFile().toPath()));
    }

    @Test
    @SneakyThrows
    public void whenProvidedEmptyDataShouldReturnEmptyResponse() {
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/enrich")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .accept(MediaType.parseMediaType("text/csv"))
                )
                .andExpect(status().isOk())
                .andReturn();
        final String contentAsString = mvcResult.getResponse().getContentAsString();
        final JsonNode response = new ObjectMapper().readTree(contentAsString);
        Assertions.assertThat(response.get("status").asInt()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.get("detail").asText()).contains("Header is missing required fields");
    }
}

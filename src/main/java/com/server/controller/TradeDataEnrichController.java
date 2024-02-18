package com.server.controller;

import com.server.service.TradeDataEnrichService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TradeDataEnrichController {

    private final TradeDataEnrichService tradeDataEnrichService;

    @PostMapping(value = "/enrich", consumes = "text/csv", produces = "text/csv")
    public ResponseEntity<?> enrich(final HttpServletRequest request, final HttpServletResponse response) {
        tradeDataEnrichService.enrich(request, response);
        return ResponseEntity.ok().build();
    }
}

package com.carmats.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("Should use incoming X-Request-ID and set on response header and MDC during execution")
    void shouldUseIncomingRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog/categories");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "custom-request-12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));
            res.getOutputStream().write("OK".getBytes());
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("custom-request-12345");
        assertThat(mdcValueInsideChain.get()).isEqualTo("custom-request-12345");
        // MDC must be cleaned up after request finishes
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should generate UUID when X-Request-ID is missing")
    void shouldGenerateUuidWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/vehicles/brands");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));
        };

        filter.doFilter(request, response, chain);

        String generatedId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generatedId).isNotBlank();
        assertThat(mdcValueInsideChain.get()).isEqualTo(generatedId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
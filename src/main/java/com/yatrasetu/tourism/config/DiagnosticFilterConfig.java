package com.yatrasetu.tourism.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;

/**
 * Registered at the very front of the servlet filter chain — ahead of Spring
 * Security's own filters — so that if ANY filter (ours or Spring's) throws,
 * this catches it instead of letting the container return a blank 500.
 *
 * Effect: any backend crash now always (a) prints a full stack trace to the
 * console, and (b) sends a readable JSON body back to the browser, so you
 * can diagnose from the Network tab alone without digging through logs.
 */
@Configuration
public class DiagnosticFilterConfig {

    @Bean
    public FilterRegistrationBean<DiagnosticFilter> diagnosticFilterRegistration() {
        FilterRegistrationBean<DiagnosticFilter> registration = new FilterRegistrationBean<>(new DiagnosticFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    static class DiagnosticFilter implements Filter {
        @Override
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            // Always allow the frontend origin, even on an error response,
            // so the browser can actually read the JSON body below instead
            // of blocking it as a failed CORS request.
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");

            try {
                chain.doFilter(req, res);
            } catch (Throwable t) {
                System.err.println("========== YATRASETU BACKEND ERROR ==========");
                System.err.println("Request: " + request.getMethod() + " " + request.getRequestURI());
                t.printStackTrace();
                System.err.println("===============================================");

                if (!response.isCommitted()) {
                    response.reset();
                    response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setStatus(500);
                    response.setContentType("application/json");
                    String safeMessage = String.valueOf(t.getMessage()).replace("\"", "'").replace("\n", " ");
                    String body = "{\"message\":\"" + t.getClass().getSimpleName() + ": " + safeMessage + "\"}";
                    response.getWriter().write(body);
                    response.getWriter().flush();
                }
            }
        }
    }
}

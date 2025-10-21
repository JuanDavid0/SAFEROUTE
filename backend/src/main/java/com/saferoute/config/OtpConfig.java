package com.saferoute.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de parámetros OTP desde application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "otp")
public class OtpConfig {

    private Integer length = 6;
    private Integer expirationMinutes = 5;
    private Integer maxAttempts = 3;

    // Getters y Setters
    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(Integer expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}

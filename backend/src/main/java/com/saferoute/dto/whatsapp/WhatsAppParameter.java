package com.saferoute.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para parámetros de plantillas de WhatsApp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppParameter {

    @JsonProperty("type")
    private String type;

    @JsonProperty("text")
    private String text;

    public static WhatsAppParameter text(String value) {
        return WhatsAppParameter.builder()
                .type("text")
                .text(value)
                .build();
    }
}

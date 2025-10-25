package com.saferoute.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el idioma de la plantilla de WhatsApp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppLanguage {

    @JsonProperty("code")
    private String code;

    public static WhatsAppLanguage spanish() {
        return WhatsAppLanguage.builder()
                .code("es")
                .build();
    }

    public static WhatsAppLanguage english() {
        return WhatsAppLanguage.builder()
                .code("en_US")
                .build();
    }
}

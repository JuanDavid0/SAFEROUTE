package com.saferoute.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para mensajes de texto de WhatsApp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppText {

    @JsonProperty("preview_url")
    private Boolean previewUrl;

    @JsonProperty("body")
    private String body;
}

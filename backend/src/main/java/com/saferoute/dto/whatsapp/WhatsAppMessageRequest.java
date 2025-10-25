package com.saferoute.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar mensajes de WhatsApp usando la API de Meta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageRequest {

    @Builder.Default
    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";

    @Builder.Default
    @JsonProperty("recipient_type")
    private String recipientType = "individual";

    @JsonProperty("to")
    private String to;

    @JsonProperty("type")
    private String type;

    @JsonProperty("template")
    private WhatsAppTemplate template;

    @JsonProperty("text")
    private WhatsAppText text;
}

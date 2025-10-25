package com.saferoute.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para componentes de plantillas de WhatsApp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppComponent {

    @JsonProperty("type")
    private String type;

    @JsonProperty("parameters")
    private List<WhatsAppParameter> parameters;
}

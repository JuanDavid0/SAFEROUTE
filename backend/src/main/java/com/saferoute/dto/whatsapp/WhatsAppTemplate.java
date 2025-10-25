package com.saferoute.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para plantillas de WhatsApp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppTemplate {

    @JsonProperty("name")
    private String name;

    @JsonProperty("language")
    private WhatsAppLanguage language;

    @JsonProperty("components")
    private List<WhatsAppComponent> components;
}

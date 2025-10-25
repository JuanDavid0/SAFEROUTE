package com.saferoute.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de Hashids para ofuscar IDs de pedidos en URLs publicas
 * 
 * Hashids permite convertir IDs numericos en strings aleatorios y cortos,
 * evitando exponer IDs secuenciales en URLs publicas
 * 
 * Ejemplo: ID 123 -> "5N6y2Kl" (reversible)
 */
@Configuration
public class HashidsConfig {

    @Value("${saferoute.hashids.salt}")
    private String salt;

    @Value("${saferoute.hashids.min-length}")
    private int minLength;

    @Bean
    public Hashids hashids() {
        // Crear instancia de Hashids con:
        // - salt: clave secreta para generar hashes (debe ser unica por aplicacion)
        // - minLength: longitud minima del hash generado
        // - alphabet: caracteres permitidos en el hash (por defecto usa alphanumericos)
        return new Hashids(salt, minLength);
    }
}

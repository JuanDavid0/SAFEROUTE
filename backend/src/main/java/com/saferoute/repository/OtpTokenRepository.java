package com.saferoute.repository;

import com.saferoute.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Integer> {

    /**
     * Busca el último token OTP válido (no usado y no expirado) para una cédula
     */
    Optional<OtpToken> findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
            String cedula, LocalDateTime now);

    /**
     * Busca todos los tokens de una cédula
     */
    List<OtpToken> findByCedula(String cedula);

    /**
     * Elimina tokens expirados (limpieza periódica)
     */
    void deleteByFechaExpiracionBefore(LocalDateTime fecha);
}

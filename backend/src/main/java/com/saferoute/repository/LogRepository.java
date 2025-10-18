package com.saferoute.repository;

import com.saferoute.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    List<Log> findByUsuarioIdUsuario(Integer idUsuario);
    List<Log> findByFechaLogBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Log> findByAccionContaining(String accion);
}

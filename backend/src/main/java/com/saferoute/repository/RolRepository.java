package com.saferoute.repository;

import com.saferoute.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByTipoRol(String tipoRol);
}
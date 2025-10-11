package com.saferoute.repository;

import com.saferoute.model.UsuarioRol;
import com.saferoute.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.id.idUsuario = :idUsuario")
    List<UsuarioRol> findByIdUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT ur FROM UsuarioRol ur WHERE ur.id.idRol = :idRol")
    List<UsuarioRol> findByIdRol(@Param("idRol") Integer idRol);
}
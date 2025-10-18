package com.saferoute.repository;

import com.saferoute.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByTelefono(String telefono);

    Optional<Usuario> findByCedula(String cedula);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.usuarioRoles ur " +
            "LEFT JOIN FETCH ur.rol r " +
            "WHERE u.cedula = :cedula")
    Optional<Usuario> findByCedulaWithRoles(@Param("cedula") String cedula);
}
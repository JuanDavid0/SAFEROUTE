package com.saferoute.helper;

import com.saferoute.constants.AuthConstants;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.exception.AuthBusinessException;
import com.saferoute.model.Rol;
import com.saferoute.model.Usuario;
import com.saferoute.model.UsuarioRol;
import com.saferoute.model.UsuarioRolId;
import com.saferoute.repository.RolRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Helper para operaciones comunes de creación de usuarios.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioCreacionHelper {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario a partir de los datos de registro.
     *
     * @param registroRequest Datos del usuario a crear
     * @return Usuario creado y guardado en BD
     */
    public Usuario crearUsuario(RegistroRequest registroRequest) {
        Usuario usuario = new Usuario();
        usuario.setNombres(registroRequest.getNombres());
        usuario.setApellidos(registroRequest.getApellidos());
        usuario.setTelefono(registroRequest.getTelefono());
        usuario.setCedula(registroRequest.getCedula());
        usuario.setDireccion(registroRequest.getDireccion());
        usuario.setContrasenia(codificarContrasenia(registroRequest.getContrasenia()));

        return usuarioRepository.save(usuario);
    }

    /**
     * Asigna un rol a un usuario.
     *
     * @param usuario Usuario al que se asignará el rol
     * @param tipoRol Tipo de rol a asignar (CLI, ADM, SAD)
     */
    public void asignarRol(Usuario usuario, String tipoRol) {
        Rol rol = buscarRolPorTipo(tipoRol);

        UsuarioRolId usuarioRolId = new UsuarioRolId(rol.getIdRol(), usuario.getIdUsuario());
        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setId(usuarioRolId);
        usuarioRol.setRol(rol);
        usuarioRol.setUsuario(usuario);

        usuarioRolRepository.save(usuarioRol);
        log.debug("Rol {} asignado al usuario ID: {}", tipoRol, usuario.getIdUsuario());
    }

    /**
     * Codifica la contraseña. Si es nula o vacía, usa contraseña vacía.
     *
     * @param contrasenia Contraseña en texto plano
     * @return Contraseña hasheada
     */
    private String codificarContrasenia(String contrasenia) {
        if (contrasenia == null || contrasenia.isEmpty()) {
            return passwordEncoder.encode(AuthConstants.CONTRASENIA_VACIA);
        }
        return passwordEncoder.encode(contrasenia);
    }

    /**
     * Busca un rol por tipo o lanza excepción si no existe.
     *
     * @param tipoRol Tipo de rol (CLI, ADM, SAD)
     * @return Rol encontrado
     * @throws AuthBusinessException si el rol no existe
     */
    private Rol buscarRolPorTipo(String tipoRol) {
        return rolRepository.findByTipoRol(tipoRol)
                .orElseThrow(() -> new AuthBusinessException(
                        String.format(AuthConstants.ERROR_ROL_NO_ENCONTRADO, tipoRol)));
    }
}

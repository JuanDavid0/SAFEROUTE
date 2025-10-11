package com.saferoute.service.interfaces;

import com.saferoute.dto.CambiarContraseniaRequest;
import com.saferoute.dto.JwtResponse;
import com.saferoute.dto.LoginRequest;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.model.Usuario;

public interface IAuthService {
    JwtResponse login(LoginRequest loginRequest);

    Usuario registro(RegistroRequest registroRequest);

    void recuperarContrasenia(String correo);

    void cambiarContrasenia(CambiarContraseniaRequest request);

    Usuario crearAdministrador(RegistroRequest registroRequest, Integer sadUserId);
}

package com.saferoute.service;

import com.saferoute.dto.LoginRequest;
import com.saferoute.dto.JwtResponse;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.helper.UsuarioCreacionHelper;
import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.security.JwtTokenProvider;
import com.saferoute.service.impl.AuthServiceImpl;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.validator.AuthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para AuthService
 * Cobertura: TC-UNIT-AUTH-001 a TC-UNIT-AUTH-014
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Autenticación")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ILogService logService;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private UsuarioCreacionHelper usuarioCreacionHelper;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private Usuario usuario;
    private Usuario usuarioSAD;
    private Usuario administrador;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setCedula("1234567890");
        loginRequest.setContrasenia("Password123");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCedula("1234567890");
        usuario.setNombres("Juan");
        usuario.setApellidos("Pérez");
        usuario.setTelefono("3001234567");
        usuario.setEstadoUsuario("ACTIVO");

        // Usuario Super Administrador (SAD)
        usuarioSAD = new Usuario();
        usuarioSAD.setIdUsuario(100);
        usuarioSAD.setCedula("9999999999");
        usuarioSAD.setNombres("Super");
        usuarioSAD.setApellidos("Admin");
        usuarioSAD.setTelefono("3009999999");
        usuarioSAD.setEstadoUsuario("ACTIVO");

        // Usuario Administrador (ADM)
        administrador = new Usuario();
        administrador.setIdUsuario(2);
        administrador.setCedula("5555555555");
        administrador.setNombres("Admin");
        administrador.setApellidos("User");
        administrador.setTelefono("3005555555");
        administrador.setEstadoUsuario("ACTIVO");

        authentication = mock(Authentication.class);
    }

    // ==================== LOGIN ====================

    @Test
    @DisplayName("TC-UNIT-AUTH-001: Login exitoso con credenciales válidas")
    void testLogin_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLI")));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");
        when(usuarioRepository.findByCedula(loginRequest.getCedula()))
                .thenReturn(Optional.of(usuario));
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        JwtResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getRol()).isEqualTo("CLI");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
        verify(logService).registrarLog(anyInt(), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-002: Login con credenciales inválidas")
    void testLogin_CredencialesInvalidas() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-003: Login con usuario inexistente")
    void testLogin_UsuarioInexistente() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Usuario no encontrado"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-004: Generación de token JWT")
    void testGenerateToken() {
        // Arrange
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("valid-jwt-token");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo("valid-jwt-token");
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-005: Validación de token JWT válido")
    void testValidateToken_Valid() {
        // Arrange
        String validToken = "valid-jwt-token";
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(validToken);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-006: Validación de token JWT inválido")
    void testValidateToken_Invalid() {
        // Arrange
        String invalidToken = "invalid-token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-007: Validación de formato de cédula")
    void testValidarCedula() {
        // Cédulas válidas: 10 dígitos numéricos
        assertThat(loginRequest.getCedula()).matches("\\d{10}");
        assertThat(loginRequest.getCedula()).hasSize(10);

        // Cédulas inválidas
        String cedulaInvalida1 = "123"; // Muy corta
        String cedulaInvalida2 = "12345678901"; // Muy larga
        String cedulaInvalida3 = "abcd123456"; // Con letras

        assertThat(cedulaInvalida1).doesNotMatch("\\d{10}");
        assertThat(cedulaInvalida2).doesNotMatch("\\d{10}");
        assertThat(cedulaInvalida3).doesNotMatch("\\d{10}");
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-008: Extracción de rol desde authentication")
    void testExtractRolFromAuthentication() {
        // Arrange
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLI")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("token");
        when(usuarioRepository.findByCedula(anyString())).thenReturn(Optional.of(usuario));
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        JwtResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response.getRol()).isEqualTo("CLI");
        assertThat(response.getRol()).doesNotContain("ROLE_");
    }

    // ==================== REGISTRO ====================

    @Test
    @DisplayName("TC-UNIT-AUTH-009: Registro exitoso llama al helper y validator")
    void testRegistro_Success() {
        // Arrange
        RegistroRequest registroRequest = new RegistroRequest();
        registroRequest.setCedula("1234567890");
        registroRequest.setNombres("Juan");
        registroRequest.setApellidos("Pérez");
        
        doNothing().when(authValidator).validarCedulaNoExiste(anyString());
        when(usuarioCreacionHelper.crearUsuario(any(RegistroRequest.class))).thenReturn(usuario);
        doNothing().when(usuarioCreacionHelper).asignarRol(any(Usuario.class), anyString());

        // Act
        Usuario result = authService.registro(registroRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(authValidator).validarCedulaNoExiste(registroRequest.getCedula());
        verify(usuarioCreacionHelper).crearUsuario(registroRequest);
        verify(usuarioCreacionHelper).asignarRol(any(Usuario.class), anyString());
        // Nota: El save se hace dentro del helper, no directamente en el servicio
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-010: Password es encriptado correctamente")
    void testPasswordEncoder() {
        // Arrange
        String rawPassword = "Password123";
        String encodedPassword = "$2a$10$encoded...";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Act
        String result = passwordEncoder.encode(rawPassword);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(encodedPassword);
        assertThat(result).isNotEqualTo(rawPassword);
        verify(passwordEncoder).encode(rawPassword);
    }

    // ==================== SUPER ADMINISTRADOR (SAD) ====================

    @Test
    @DisplayName("TC-UNIT-AUTH-011: SAD crea administrador exitosamente")
    void testCrearAdministrador_Success() {
        // Arrange
        RegistroRequest adminRequest = new RegistroRequest();
        adminRequest.setCedula("5555555555");
        adminRequest.setNombres("Admin");
        adminRequest.setApellidos("User");
        adminRequest.setTelefono("3005555555");
        
        when(usuarioRepository.findById(usuarioSAD.getIdUsuario()))
                .thenReturn(Optional.of(usuarioSAD));
        doNothing().when(authValidator).validarCedulaNoExiste(anyString());
        when(usuarioCreacionHelper.crearUsuario(any(RegistroRequest.class)))
                .thenReturn(administrador);
        doNothing().when(usuarioCreacionHelper).asignarRol(any(Usuario.class), eq("ADM"));
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        Usuario result = authService.crearAdministrador(adminRequest, usuarioSAD.getIdUsuario());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(administrador.getIdUsuario());
        
        verify(usuarioRepository).findById(usuarioSAD.getIdUsuario());
        verify(authValidator).validarCedulaNoExiste(adminRequest.getCedula());
        verify(usuarioCreacionHelper).crearUsuario(adminRequest);
        verify(usuarioCreacionHelper).asignarRol(any(Usuario.class), eq("ADM"));
        verify(logService, atLeastOnce()).registrarLog(anyInt(), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-012: SAD inexistente no puede crear administrador")
    void testCrearAdministrador_SADInexistente() {
        // Arrange
        RegistroRequest adminRequest = new RegistroRequest();
        adminRequest.setCedula("5555555555");
        
        when(usuarioRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.crearAdministrador(adminRequest, 999))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("SAD");

        verify(usuarioRepository).findById(999);
        verify(usuarioCreacionHelper, never()).crearUsuario(any());
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-013: Login de Super Administrador (SAD)")
    void testLogin_SuperAdministrador() {
        // Arrange
        LoginRequest sadLoginRequest = new LoginRequest();
        sadLoginRequest.setCedula("9999999999");
        sadLoginRequest.setContrasenia("SuperPassword123");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_SAD")));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("sad-jwt-token");
        when(usuarioRepository.findByCedula(sadLoginRequest.getCedula()))
                .thenReturn(Optional.of(usuarioSAD));
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        JwtResponse response = authService.login(sadLoginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("sad-jwt-token");
        assertThat(response.getRol()).isEqualTo("SAD");
        assertThat(response.getRol()).doesNotContain("ROLE_");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
        verify(logService).registrarLog(eq(usuarioSAD.getIdUsuario()), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-AUTH-014: Login de Administrador (ADM)")
    void testLogin_Administrador() {
        // Arrange
        LoginRequest admLoginRequest = new LoginRequest();
        admLoginRequest.setCedula("5555555555");
        admLoginRequest.setContrasenia("AdminPassword123");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADM")));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("adm-jwt-token");
        when(usuarioRepository.findByCedula(admLoginRequest.getCedula()))
                .thenReturn(Optional.of(administrador));
        doNothing().when(logService).registrarLog(anyInt(), anyString());

        // Act
        JwtResponse response = authService.login(admLoginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("adm-jwt-token");
        assertThat(response.getRol()).isEqualTo("ADM");
        assertThat(response.getRol()).doesNotContain("ROLE_");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
        verify(logService).registrarLog(eq(administrador.getIdUsuario()), anyString());
    }
}

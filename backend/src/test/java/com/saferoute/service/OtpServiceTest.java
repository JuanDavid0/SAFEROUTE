package com.saferoute.service;

import com.saferoute.config.OtpConfig;
import com.saferoute.dto.OtpResponse;
import com.saferoute.exception.OtpBusinessException;
import com.saferoute.exception.ResourceNotFoundException;
import com.saferoute.helper.OtpMessageFormatter;
import com.saferoute.helper.OtpTokenValidator;
import com.saferoute.model.OtpToken;
import com.saferoute.model.Usuario;
import com.saferoute.repository.OtpTokenRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.security.JwtTokenProvider;
import com.saferoute.service.impl.OtpServiceImpl;
import com.saferoute.service.impl.TwilioService;
import com.saferoute.validator.OtpValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para OtpServiceImpl
 * 
 * Pruebas de autenticación OTP (One-Time Password)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de OTP Service")
class OtpServiceTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TwilioService twilioService;

    @Mock
    private OtpConfig otpConfig;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OtpValidator otpValidator;

    @Mock
    private OtpTokenValidator tokenValidator;

    @Mock
    private OtpMessageFormatter messageFormatter;

    @InjectMocks
    private OtpServiceImpl otpService;

    private Usuario usuario;
    private OtpToken otpToken;

    @BeforeEach
    void setUp() {
        // Configurar usuario de prueba
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCedula("1234567890");
        usuario.setNombres("Juan");
        usuario.setApellidos("Pérez");
        usuario.setTelefono("3001234567");
        usuario.setEstadoUsuario("ACTIVO");

        // Configurar token OTP de prueba
        otpToken = new OtpToken();
        otpToken.setIdOtp(1);
        otpToken.setCedula("1234567890");
        otpToken.setTelefono("3001234567");
        otpToken.setCodigoOtp("123456");
        otpToken.setFechaCreacion(LocalDateTime.now());
        otpToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(5));
        otpToken.setIntentosFallidos(0);
        otpToken.setVerificado(false);
        otpToken.setUsado(false);
    }

    // ==================== SOLICITAR OTP ====================

    @Test
    @DisplayName("TC-UNIT-OTP-001: Solicitar OTP exitosamente")
    void testSolicitarOtp_Success() {
        // Arrange
        when(usuarioRepository.findByCedula(usuario.getCedula()))
                .thenReturn(Optional.of(usuario));
        doNothing().when(otpValidator).validarUsuarioActivo(any(Usuario.class), anyString());
        doNothing().when(otpValidator).validarTelefonoRegistrado(anyString(), anyString());
        when(otpConfig.getExpirationMinutes()).thenReturn(5);
        when(otpConfig.getLength()).thenReturn(6);
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);
        when(twilioService.enviarSmsOtp(anyString(), anyString())).thenReturn(true);
        when(messageFormatter.formatearMensajeOtpEnviado(anyString()))
                .thenReturn("OTP enviado al teléfono ***4567");

        // Act
        OtpResponse response = otpService.solicitarOtp(usuario.getCedula());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isExito()).isTrue();
        assertThat(response.getMensaje()).contains("OTP enviado");

        verify(usuarioRepository).findByCedula(usuario.getCedula());
        verify(otpValidator).validarUsuarioActivo(any(Usuario.class), eq(usuario.getCedula()));
        verify(otpValidator).validarTelefonoRegistrado(eq(usuario.getTelefono()), eq(usuario.getCedula()));
        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(twilioService).enviarSmsOtp(eq(usuario.getTelefono()), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-002: Solicitar OTP con usuario inexistente")
    void testSolicitarOtp_UsuarioNoExiste() {
        // Arrange
        String cedulaInexistente = "9999999999";
        when(usuarioRepository.findByCedula(cedulaInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> otpService.solicitarOtp(cedulaInexistente))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("cédula");

        verify(usuarioRepository).findByCedula(cedulaInexistente);
        verify(otpTokenRepository, never()).save(any());
        verify(twilioService, never()).enviarSmsOtp(anyString(), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-003: Solicitar OTP con usuario inactivo")
    void testSolicitarOtp_UsuarioInactivo() {
        // Arrange
        usuario.setEstadoUsuario("INACTIVO");
        when(usuarioRepository.findByCedula(usuario.getCedula()))
                .thenReturn(Optional.of(usuario));
        doThrow(new OtpBusinessException("Usuario inactivo", "OTP_USUARIO_INACTIVO"))
                .when(otpValidator).validarUsuarioActivo(any(Usuario.class), anyString());

        // Act & Assert
        assertThatThrownBy(() -> otpService.solicitarOtp(usuario.getCedula()))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("Usuario inactivo");

        verify(usuarioRepository).findByCedula(usuario.getCedula());
        verify(otpValidator).validarUsuarioActivo(any(Usuario.class), eq(usuario.getCedula()));
        verify(otpTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-004: Solicitar OTP con teléfono no registrado")
    void testSolicitarOtp_TelefonoNoRegistrado() {
        // Arrange
        usuario.setTelefono(null);
        when(usuarioRepository.findByCedula(usuario.getCedula()))
                .thenReturn(Optional.of(usuario));
        doNothing().when(otpValidator).validarUsuarioActivo(any(Usuario.class), anyString());
        doThrow(new OtpBusinessException("Teléfono no registrado", "OTP_TELEFONO_NO_REGISTRADO"))
                .when(otpValidator).validarTelefonoRegistrado(isNull(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> otpService.solicitarOtp(usuario.getCedula()))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("Teléfono no registrado");

        verify(usuarioRepository).findByCedula(usuario.getCedula());
        verify(otpValidator).validarTelefonoRegistrado(isNull(), eq(usuario.getCedula()));
        verify(twilioService, never()).enviarSmsOtp(anyString(), anyString());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-005: Solicitar OTP cuando falla el envío de SMS")
    void testSolicitarOtp_ErrorEnviarSms() {
        // Arrange
        when(usuarioRepository.findByCedula(usuario.getCedula()))
                .thenReturn(Optional.of(usuario));
        doNothing().when(otpValidator).validarUsuarioActivo(any(Usuario.class), anyString());
        doNothing().when(otpValidator).validarTelefonoRegistrado(anyString(), anyString());
        when(otpConfig.getExpirationMinutes()).thenReturn(5);
        when(otpConfig.getLength()).thenReturn(6);
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);
        when(twilioService.enviarSmsOtp(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> otpService.solicitarOtp(usuario.getCedula()))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("SMS");

        verify(twilioService).enviarSmsOtp(eq(usuario.getTelefono()), anyString());
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    // ==================== VERIFICAR OTP ====================

    @Test
    @DisplayName("TC-UNIT-OTP-006: Verificar OTP exitosamente")
    void testVerificarOtp_Success() {
        // Arrange
        String codigoOtp = "123456";
        when(otpTokenRepository.findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpToken));
        doNothing().when(tokenValidator).validarTokenNoExpirado(any(OtpToken.class), anyString());
        doNothing().when(tokenValidator).validarMaximosIntentos(any(OtpToken.class), anyString());
        when(tokenValidator.validarCodigoOtp(any(OtpToken.class), eq(codigoOtp), anyString()))
                .thenReturn(true);
        when(jwtTokenProvider.generateOtpToken(usuario.getCedula()))
                .thenReturn("jwt-token-temporal");
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);

        // Act
        OtpResponse response = otpService.verificarOtp(usuario.getCedula(), codigoOtp);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isExito()).isTrue();
        assertThat(response.getToken()).isEqualTo("jwt-token-temporal");
        assertThat(response.getMensaje()).isNotBlank();

        verify(otpTokenRepository).findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class));
        verify(tokenValidator).validarTokenNoExpirado(any(OtpToken.class), eq(usuario.getCedula()));
        verify(tokenValidator).validarMaximosIntentos(any(OtpToken.class), eq(usuario.getCedula()));
        verify(tokenValidator).validarCodigoOtp(any(OtpToken.class), eq(codigoOtp), eq(usuario.getCedula()));
        verify(jwtTokenProvider).generateOtpToken(usuario.getCedula());
        verify(otpTokenRepository, times(1)).save(any(OtpToken.class));
    }

    @Test
    @DisplayName("TC-UNIT-OTP-007: Verificar OTP con código incorrecto")
    void testVerificarOtp_CodigoIncorrecto() {
        // Arrange
        String codigoIncorrecto = "999999";
        when(otpTokenRepository.findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpToken));
        doNothing().when(tokenValidator).validarTokenNoExpirado(any(OtpToken.class), anyString());
        doNothing().when(tokenValidator).validarMaximosIntentos(any(OtpToken.class), anyString());
        when(tokenValidator.validarCodigoOtp(any(OtpToken.class), eq(codigoIncorrecto), anyString()))
                .thenReturn(false);
        when(tokenValidator.obtenerMensajeIntentosRestantes(anyInt()))
                .thenReturn("Código OTP incorrecto. Tienes 2 intentos restantes");
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);

        // Act & Assert
        assertThatThrownBy(() -> otpService.verificarOtp(usuario.getCedula(), codigoIncorrecto))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("incorrecto");

        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(jwtTokenProvider, never()).generateOtpToken(anyString());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-008: Verificar OTP con token expirado")
    void testVerificarOtp_TokenExpirado() {
        // Arrange
        otpToken.setFechaExpiracion(LocalDateTime.now().minusMinutes(10));
        when(otpTokenRepository.findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpToken));
        doThrow(new OtpBusinessException("OTP expirado", "OTP_EXPIRED"))
                .when(tokenValidator).validarTokenNoExpirado(any(OtpToken.class), anyString());

        // Act & Assert
        assertThatThrownBy(() -> otpService.verificarOtp(usuario.getCedula(), "123456"))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("expirado");

        verify(tokenValidator).validarTokenNoExpirado(any(OtpToken.class), eq(usuario.getCedula()));
        verify(jwtTokenProvider, never()).generateOtpToken(anyString());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-009: Verificar OTP con máximos intentos excedidos")
    void testVerificarOtp_MaximosIntentosExcedidos() {
        // Arrange
        otpToken.setIntentosFallidos(3);
        when(otpTokenRepository.findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpToken));
        doNothing().when(tokenValidator).validarTokenNoExpirado(any(OtpToken.class), anyString());
        doThrow(new OtpBusinessException("Máximo de intentos excedidos", "OTP_MAX_ATTEMPTS"))
                .when(tokenValidator).validarMaximosIntentos(any(OtpToken.class), anyString());

        // Act & Assert
        assertThatThrownBy(() -> otpService.verificarOtp(usuario.getCedula(), "123456"))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("intentos");

        verify(tokenValidator).validarMaximosIntentos(any(OtpToken.class), eq(usuario.getCedula()));
        verify(jwtTokenProvider, never()).generateOtpToken(anyString());
    }

    @Test
    @DisplayName("TC-UNIT-OTP-010: Verificar OTP cuando no existe token válido")
    void testVerificarOtp_TokenNoExiste() {
        // Arrange
        when(otpTokenRepository.findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> otpService.verificarOtp(usuario.getCedula(), "123456"))
                .isInstanceOf(OtpBusinessException.class)
                .hasMessageContaining("verificación");

        verify(otpTokenRepository).findFirstByCedulaAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(usuario.getCedula()), any(LocalDateTime.class));
        verify(jwtTokenProvider, never()).generateOtpToken(anyString());
    }

    // ==================== LIMPIAR TOKENS EXPIRADOS ====================

    @Test
    @DisplayName("TC-UNIT-OTP-011: Limpiar tokens expirados exitosamente")
    void testLimpiarTokensExpirados_Success() {
        // Arrange
        doNothing().when(otpTokenRepository).deleteByFechaExpiracionBefore(any(LocalDateTime.class));

        // Act
        otpService.limpiarTokensExpirados();

        // Assert
        verify(otpTokenRepository).deleteByFechaExpiracionBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("TC-UNIT-OTP-012: Limpiar tokens expirados con error")
    void testLimpiarTokensExpirados_ConError() {
        // Arrange
        doThrow(new RuntimeException("Error de base de datos"))
                .when(otpTokenRepository).deleteByFechaExpiracionBefore(any(LocalDateTime.class));

        // Act - No debe lanzar excepción, solo loguear
        assertThatCode(() -> otpService.limpiarTokensExpirados())
                .doesNotThrowAnyException();

        // Assert
        verify(otpTokenRepository).deleteByFechaExpiracionBefore(any(LocalDateTime.class));
    }
}

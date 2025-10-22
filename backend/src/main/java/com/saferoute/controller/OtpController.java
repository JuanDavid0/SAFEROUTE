package com.saferoute.controller;

import com.saferoute.dto.OtpResponse;
import com.saferoute.dto.SolicitarOtpRequest;
import com.saferoute.dto.VerificarOtpRequest;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación OTP de clientes
 * Permite solicitar y verificar códigos OTP por SMS para que los clientes
 * puedan modificar sus solicitudes
 */
@RestController
@RequestMapping("/auth/otp")
public class OtpController {

    private final IOtpService otpService;

    public OtpController(IOtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Solicita un código OTP para la cédula especificada
     * El código se envía por SMS al número de teléfono registrado
     * 
     * POST /api/auth/otp/solicitar
     * 
     * @param request DTO con la cédula del cliente
     * @return Respuesta estandarizada indicando si el OTP fue enviado exitosamente
     */
    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<OtpResponse>> solicitarOtp(@Valid @RequestBody SolicitarOtpRequest request) {
        OtpResponse otpResponse = otpService.solicitarOtp(request.getCedula());

        ApiResponse<OtpResponse> response = ApiResponse.success(
                otpResponse,
                "Código OTP enviado exitosamente al teléfono registrado");

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica el código OTP ingresado por el cliente
     * Si es correcto, devuelve un token JWT temporal para modificar solicitudes
     * 
     * POST /api/auth/otp/verificar
     * 
     * @param request DTO con la cédula y el código OTP
     * @return Respuesta estandarizada con token JWT temporal si la verificación es
     *         exitosa
     */
    @PostMapping("/verificar")
    public ResponseEntity<ApiResponse<OtpResponse>> verificarOtp(@Valid @RequestBody VerificarOtpRequest request) {
        OtpResponse otpResponse = otpService.verificarOtp(request.getCedula(), request.getCodigoOtp());

        ApiResponse<OtpResponse> response = ApiResponse.success(
                otpResponse,
                "Código OTP verificado correctamente. Token de autorización generado");

        return ResponseEntity.ok(response);
    }
}

package com.saferoute.controller;

import com.saferoute.dto.OtpResponse;
import com.saferoute.dto.SolicitarOtpRequest;
import com.saferoute.dto.VerificarOtpRequest;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/otp")
public class OtpController {

    private final IOtpService otpService;

    public OtpController(IOtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<OtpResponse>> solicitarOtp(@Valid @RequestBody SolicitarOtpRequest request) {
        OtpResponse otpResponse = otpService.solicitarOtp(request.getCedula());

        ApiResponse<OtpResponse> response = ApiResponse.success(
                otpResponse,
                "Código OTP enviado exitosamente al teléfono registrado");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verificar")
    public ResponseEntity<ApiResponse<OtpResponse>> verificarOtp(@Valid @RequestBody VerificarOtpRequest request) {
        OtpResponse otpResponse = otpService.verificarOtp(request.getCedula(), request.getCodigoOtp());

        ApiResponse<OtpResponse> response = ApiResponse.success(
                otpResponse,
                "Código OTP verificado correctamente. Token de autorización generado");

        return ResponseEntity.ok(response);
    }
}

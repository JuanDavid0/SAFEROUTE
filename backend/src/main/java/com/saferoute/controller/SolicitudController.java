package com.saferoute.controller;

import com.saferoute.dto.*;
import com.saferoute.service.interfaces.ISolicitudService;
import com.saferoute.service.SolicitudAuthorizationService;
import com.saferoute.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final ISolicitudService solicitudService;
    private final SolicitudAuthorizationService authorizationService;
    private final JwtTokenProvider jwtTokenProvider;

    public SolicitudController(
            ISolicitudService solicitudService,
            SolicitudAuthorizationService authorizationService,
            JwtTokenProvider jwtTokenProvider) {
        this.solicitudService = solicitudService;
        this.authorizationService = authorizationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Extrae la cédula del token OTP del header Authorization
     */
    private String getCedulaFromToken(HttpServletRequest request) {
        String jwt = getJwtFromRequest(request);
        if (jwt == null) {
            throw new RuntimeException("Token no encontrado. Debe autenticarse vía OTP primero.");
        }

        if (!jwtTokenProvider.validateToken(jwt)) {
            throw new RuntimeException("Token inválido o expirado.");
        }

        if (!jwtTokenProvider.isOtpToken(jwt)) {
            throw new RuntimeException("Este endpoint requiere autenticación OTP.");
        }

        return jwtTokenProvider.getCedulaFromJWT(jwt);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @PreAuthorize("hasRole('CLI')")
    @PostMapping("/{idCliente}")
    public SolicitudDTO crearSolicitud(@PathVariable Integer idCliente, @Valid @RequestBody SolicitudDTO dto) {
        return solicitudService.crearSolicitud(dto, idCliente);
    }

    @PostMapping("/public/nueva")
    public SolicitudDTO crearSolicitudClienteNuevo(@Valid @RequestBody SolicitudClienteDTO dto) {
        return solicitudService.crearSolicitudClienteNuevo(dto);
    }

    /**
     * Listar las solicitudes del cliente autenticado vía OTP
     * Usa la cédula del token para filtrar las solicitudes
     */
    @GetMapping("/mis-solicitudes")
    public List<SolicitudDTO> listarMisSolicitudes(HttpServletRequest request) {
        String cedula = getCedulaFromToken(request);
        return solicitudService.listarSolicitudesPorCedula(cedula);
    }

    @PutMapping("/{idSolicitud}/modificar")
    public SolicitudDTO modificarSolicitud(
            @PathVariable Integer idSolicitud,
            @RequestBody SolicitudModificacionDTO dto,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        return solicitudService.modificarSolicitud(idSolicitud, dto);
    }

    /**
     * Agregar un producto a la solicitud
     */
    @PostMapping("/{idSolicitud}/productos")
    public SolicitudDTO agregarProducto(
            @PathVariable Integer idSolicitud,
            @Valid @RequestBody SolicitudProductoDTO productoDTO,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        return solicitudService.agregarProducto(idSolicitud, productoDTO);
    }

    /**
     * Eliminar un producto de la solicitud
     */
    @DeleteMapping("/{idSolicitud}/productos/{idProducto}")
    public SolicitudDTO eliminarProducto(
            @PathVariable Integer idSolicitud,
            @PathVariable Integer idProducto,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        return solicitudService.eliminarProducto(idSolicitud, idProducto);
    }

    /**
     * Modificar la cantidad de un producto específico en la solicitud
     */
    @PutMapping("/{idSolicitud}/productos/{idProducto}")
    public SolicitudDTO modificarCantidadProducto(
            @PathVariable Integer idSolicitud,
            @PathVariable Integer idProducto,
            @RequestBody Map<String, Integer> body,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        Integer nuevaCantidad = body.get("cantidad");
        if (nuevaCantidad == null || nuevaCantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }
        return solicitudService.modificarCantidadProducto(idSolicitud, idProducto, nuevaCantidad);
    }

    @DeleteMapping("/{idSolicitud}")
    public ResponseEntity<Map<String, String>> cancelarSolicitud(@PathVariable Integer idSolicitud) {
        solicitudService.cancelarSolicitud(idSolicitud);
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud cancelada exitosamente"));
    }

    @GetMapping("/{idCliente}")
    public List<SolicitudDTO> listarSolicitudesCliente(@PathVariable Integer idCliente) {
        return solicitudService.listarSolicitudesCliente(idCliente);
    }

    /**
     * Cambiar estado de solicitud manualmente (para confirmación de pago manual)
     */
    @PutMapping("/{idSolicitud}/estado")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<Map<String, String>> cambiarEstadoSolicitud(
            @PathVariable Integer idSolicitud,
            @RequestBody Map<String, String> body) {

        String nuevoEstado = body.get("nuevoEstado"); // "PGD" o "CAN"
        solicitudService.cambiarEstado(idSolicitud, nuevoEstado);
        return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado a " + nuevoEstado));
    }

    /**
     * Listar solicitudes de un pedido por estado (filtro)
     */
    @GetMapping("/pedido/{idPedido}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public List<SolicitudDTO> listarSolicitudesPorPedidoYEstado(
            @PathVariable Integer idPedido,
            @RequestParam(required = false) String estado) {

        if (estado != null) {
            return solicitudService.listarPorPedidoYEstado(idPedido, estado);
        } else {
            return solicitudService.listarPorPedido(idPedido);
        }
    }
}

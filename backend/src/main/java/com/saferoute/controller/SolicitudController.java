package com.saferoute.controller;

import com.saferoute.dto.*;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.exception.BusinessException;
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
            throw new BusinessException("Token no encontrado. Debe autenticarse vía OTP primero.", "TOKEN_NOT_FOUND");
        }

        if (!jwtTokenProvider.validateToken(jwt)) {
            throw new BusinessException("Token inválido o expirado.", "TOKEN_INVALID");
        }

        if (!jwtTokenProvider.isOtpToken(jwt)) {
            throw new BusinessException("Este endpoint requiere autenticación OTP.", "OTP_TOKEN_REQUIRED");
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
    public ResponseEntity<ApiResponse<SolicitudDTO>> crearSolicitud(
            @PathVariable Integer idCliente,
            @Valid @RequestBody SolicitudDTO dto) {
        SolicitudDTO solicitud = solicitudService.crearSolicitud(dto, idCliente);

        ApiResponse<SolicitudDTO> response = ApiResponse.success(
                solicitud,
                "Solicitud creada exitosamente");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/nueva")
    public ResponseEntity<ApiResponse<SolicitudDTO>> crearSolicitudClienteNuevo(
            @Valid @RequestBody SolicitudClienteDTO dto) {
        SolicitudDTO solicitud = solicitudService.crearSolicitudClienteNuevo(dto);

        ApiResponse<SolicitudDTO> response = ApiResponse.success(
                solicitud,
                "Solicitud y cliente creados exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Listar las solicitudes del cliente autenticado vía OTP para un pedido
     * específico
     * Usa la cédula del token y el hash del pedido para filtrar las solicitudes
     * 
     * @param hashPedido Hash único del pedido
     * @param request    Request con el token OTP
     * @return Lista de solicitudes del cliente para ese pedido específico,
     *         ordenadas por estado (PDP, PGD, CAN)
     */
    @GetMapping("/mis-solicitudes/{hashPedido}")
    public ResponseEntity<ApiResponse<List<SolicitudDTO>>> listarMisSolicitudesPorPedido(
            @PathVariable String hashPedido,
            HttpServletRequest request) {
        String cedula = getCedulaFromToken(request);
        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudesPorCedulaYPedido(cedula, hashPedido);

        ApiResponse<List<SolicitudDTO>> response = ApiResponse.success(
                solicitudes,
                String.format("Se encontraron %d solicitud(es) para este pedido", solicitudes.size()));

        return ResponseEntity.ok(response);
    }

    /**
     * Listar TODAS las solicitudes del cliente autenticado vía OTP (todos los
     * pedidos)
     * Usa la cédula del token para filtrar las solicitudes
     */
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<ApiResponse<List<SolicitudDTO>>> listarMisSolicitudes(HttpServletRequest request) {
        String cedula = getCedulaFromToken(request);
        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudesPorCedula(cedula);

        ApiResponse<List<SolicitudDTO>> response = ApiResponse.success(
                solicitudes,
                String.format("Se encontraron %d solicitud(es)", solicitudes.size()));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idSolicitud}/modificar")
    public ResponseEntity<ApiResponse<SolicitudDTO>> modificarSolicitud(
            @PathVariable Integer idSolicitud,
            @RequestBody SolicitudModificacionDTO dto,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        SolicitudDTO solicitud = solicitudService.modificarSolicitud(idSolicitud, dto);

        ApiResponse<SolicitudDTO> response = ApiResponse.success(
                solicitud,
                "Solicitud modificada exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Agregar un producto a la solicitud
     */
    @PostMapping("/{idSolicitud}/productos")
    public ResponseEntity<ApiResponse<SolicitudDTO>> agregarProducto(
            @PathVariable Integer idSolicitud,
            @Valid @RequestBody SolicitudProductoDTO productoDTO,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        SolicitudDTO solicitud = solicitudService.agregarProducto(idSolicitud, productoDTO);

        ApiResponse<SolicitudDTO> response = ApiResponse.success(
                solicitud,
                "Producto agregado exitosamente a la solicitud");

        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un producto de la solicitud
     */
    @DeleteMapping("/{idSolicitud}/productos/{idProducto}")
    public ResponseEntity<ApiResponse<SolicitudDTO>> eliminarProducto(
            @PathVariable Integer idSolicitud,
            @PathVariable Integer idProducto,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        SolicitudDTO solicitud = solicitudService.eliminarProducto(idSolicitud, idProducto);

        ApiResponse<SolicitudDTO> response = ApiResponse.success(
                solicitud,
                "Producto eliminado exitosamente de la solicitud");

        return ResponseEntity.ok(response);
    }

    /**
     * Modificar la cantidad de un producto específico en la solicitud
     */
    @PutMapping("/{idSolicitud}/productos/{idProducto}")
    public ResponseEntity<ApiResponse<SolicitudDTO>> modificarCantidadProducto(
            @PathVariable Integer idSolicitud,
            @PathVariable Integer idProducto,
            @RequestBody Map<String, Integer> body,
            HttpServletRequest request) {
        // Validar que el token OTP pertenece al dueño de la solicitud
        String cedula = getCedulaFromToken(request);
        authorizationService.validateSolicitudOwnership(idSolicitud, cedula);

        Integer nuevaCantidad = body.get("cantidad");
        if (nuevaCantidad == null || nuevaCantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0", "INVALID_QUANTITY");
        }

        SolicitudDTO solicitud = solicitudService.modificarCantidadProducto(idSolicitud, idProducto, nuevaCantidad);

        ApiResponse<SolicitudDTO> response = ApiResponse.success(
                solicitud,
                "Cantidad del producto modificada exitosamente");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idSolicitud}")
    public ResponseEntity<ApiResponse<Void>> cancelarSolicitud(@PathVariable Integer idSolicitud) {
        solicitudService.cancelarSolicitud(idSolicitud);

        ApiResponse<Void> response = ApiResponse.success("Solicitud cancelada exitosamente");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idCliente}")
    public ResponseEntity<ApiResponse<List<SolicitudDTO>>> listarSolicitudesCliente(@PathVariable Integer idCliente) {
        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudesCliente(idCliente);

        ApiResponse<List<SolicitudDTO>> response = ApiResponse.success(
                solicitudes,
                String.format("Se encontraron %d solicitud(es) para el cliente", solicitudes.size()));

        return ResponseEntity.ok(response);
    }

    /**
     * Cambiar estado de solicitud manualmente (para confirmación de pago manual)
     */
    @PutMapping("/{idSolicitud}/estado")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstadoSolicitud(
            @PathVariable Integer idSolicitud,
            @RequestBody Map<String, String> body) {

        String nuevoEstado = body.get("nuevoEstado"); // "PGD" o "CAN"
        solicitudService.cambiarEstado(idSolicitud, nuevoEstado);

        ApiResponse<Void> response = ApiResponse.success(
                String.format("Estado actualizado a %s", nuevoEstado));

        return ResponseEntity.ok(response);
    }

    /**
     * Listar solicitudes de un pedido por estado (filtro)
     */
    @GetMapping("/pedido/{idPedido}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<ApiResponse<List<SolicitudDTO>>> listarSolicitudesPorPedidoYEstado(
            @PathVariable Integer idPedido,
            @RequestParam(required = false) String estado) {

        List<SolicitudDTO> solicitudes;
        String mensaje;

        if (estado != null) {
            solicitudes = solicitudService.listarPorPedidoYEstado(idPedido, estado);
            mensaje = String.format("Se encontraron %d solicitud(es) con estado %s", solicitudes.size(), estado);
        } else {
            solicitudes = solicitudService.listarPorPedido(idPedido);
            mensaje = String.format("Se encontraron %d solicitud(es) para el pedido", solicitudes.size());
        }

        ApiResponse<List<SolicitudDTO>> response = ApiResponse.success(solicitudes, mensaje);

        return ResponseEntity.ok(response);
    }
}

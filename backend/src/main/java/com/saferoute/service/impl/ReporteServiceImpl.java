package com.saferoute.service.impl;

import com.saferoute.constants.ReporteConstants;
import com.saferoute.dto.reporte.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.IReporteService;
import com.saferoute.service.helper.AgrupamientoReporteHelper;
import com.saferoute.service.helper.CalculosFinancierosHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de Reportes
 * Refactorizado siguiendo principios SOLID y buenas prácticas
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements IReporteService {

    private final SolicitudRepository solicitudRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CalculosFinancierosHelper calculosHelper;
    private final AgrupamientoReporteHelper agrupamientoHelper;

    @Override
    public ReporteIngresosDTO generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin, String agrupacion) {
        log.info(ReporteConstants.LOG_GENERANDO_REPORTE_INGRESOS, fechaInicio, fechaFin, agrupacion);

        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findSolicitudesPagadasEnRango(fechaInicio, fechaFin);

        if (solicitudesPagadas.isEmpty()) {
            log.warn(ReporteConstants.LOG_SIN_SOLICITUDES);
            return crearReporteVacio(fechaInicio, fechaFin, agrupacion);
        }

        BigDecimal[] totales = calcularTotalesFinancieros(solicitudesPagadas);
        List<ReporteIngresosDTO.DatosPeriodo> datosPorPeriodo = agrupamientoHelper.agruparPorPeriodo(solicitudesPagadas,
                agrupacion);

        ReporteIngresosDTO reporte = construirReporteIngresos(
                fechaInicio, fechaFin, agrupacion, totales, solicitudesPagadas.size(), datosPorPeriodo);

        log.info(ReporteConstants.LOG_REPORTE_GENERADO,
                solicitudesPagadas.size(), totales[0], totales[2]);

        return reporte;
    }

    private BigDecimal[] calcularTotalesFinancieros(List<Solicitud> solicitudes) {
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalCostos = BigDecimal.ZERO;

        for (Solicitud solicitud : solicitudes) {
            totalIngresos = totalIngresos.add(calculosHelper.calcularIngresosSolicitud(solicitud));
            totalCostos = totalCostos.add(calculosHelper.calcularCostosSolicitud(solicitud));
        }

        BigDecimal gananciaNeta = totalIngresos.subtract(totalCostos);
        return new BigDecimal[] { totalIngresos, totalCostos, gananciaNeta };
    }

    private ReporteIngresosDTO construirReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin,
            String agrupacion, BigDecimal[] totales,
            int cantidadSolicitudes,
            List<ReporteIngresosDTO.DatosPeriodo> datosPorPeriodo) {
        ReporteIngresosDTO reporte = new ReporteIngresosDTO();
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setAgrupacion(agrupacion);
        reporte.setTotalIngresos(totales[0]);
        reporte.setTotalCostos(totales[1]);
        reporte.setGananciaNeta(totales[2]);
        reporte.setTotalSolicitudesPagadas(cantidadSolicitudes);
        reporte.setDatosPorPeriodo(datosPorPeriodo);
        return reporte;
    }

    @Override
    public List<ProductoVendidoDTO> obtenerProductosMasVendidos(Integer limite) {
        int limiteReal = limite != null ? limite : ReporteConstants.LIMITE_DEFAULT_PRODUCTOS;
        log.info(ReporteConstants.LOG_PRODUCTOS_MAS_VENDIDOS, limiteReal);

        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findByEstadoSolicitud(EstadoSolicitudEnum.PGD);

        Map<Integer, ProductoVendidoDTO> productosMap = construirMapaProductos(solicitudesPagadas);
        calcularGananciasProductos(productosMap);

        List<ProductoVendidoDTO> resultado = productosMap.values().stream()
                .sorted(Comparator.comparing(ProductoVendidoDTO::getCantidadTotalVendida).reversed())
                .limit(limiteReal)
                .collect(Collectors.toList());

        log.info(ReporteConstants.LOG_RESULTADO_GENERADO,
                String.format("Top %d productos más vendidos", resultado.size()));
        return resultado;
    }

    private Map<Integer, ProductoVendidoDTO> construirMapaProductos(List<Solicitud> solicitudesPagadas) {
        Map<Integer, ProductoVendidoDTO> productosMap = new HashMap<>();

        for (Solicitud solicitud : solicitudesPagadas) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                Integer idProducto = sp.getProducto().getIdProducto();
                ProductoVendidoDTO dto = productosMap.computeIfAbsent(
                        idProducto, k -> crearProductoVendidoDTO(sp));

                acumularDatosProducto(dto, sp);
            }
        }

        return productosMap;
    }

    private ProductoVendidoDTO crearProductoVendidoDTO(SolicitudProducto sp) {
        ProductoVendidoDTO dto = new ProductoVendidoDTO();
        dto.setIdProducto(sp.getProducto().getIdProducto());
        dto.setNombreProducto(sp.getProducto().getNombreProducto());
        dto.setDescripcion(sp.getProducto().getDescripcionProducto());
        dto.setCategoria(sp.getProducto().getTipoProducto());
        dto.setCantidadTotalVendida(0);
        dto.setIngresosGenerados(BigDecimal.ZERO);
        dto.setCostosAsociados(BigDecimal.ZERO);
        dto.setNumeroPedidos(0);
        return dto;
    }

    private void acumularDatosProducto(ProductoVendidoDTO dto, SolicitudProducto sp) {
        dto.setCantidadTotalVendida(dto.getCantidadTotalVendida() + sp.getCantidadSolicitada());

        BigDecimal ingresos = calculosHelper.calcularMontoProducto(sp);
        dto.setIngresosGenerados(dto.getIngresosGenerados().add(ingresos));

        BigDecimal costos = calculosHelper.calcularCostoProducto(sp);
        dto.setCostosAsociados(dto.getCostosAsociados().add(costos));
    }

    private void calcularGananciasProductos(Map<Integer, ProductoVendidoDTO> productosMap) {
        productosMap.values().forEach(dto -> {
            BigDecimal ganancia = dto.getIngresosGenerados().subtract(dto.getCostosAsociados());
            dto.setGananciaNeta(ganancia);
        });
    }

    @Override
    public List<ProductoVendidoDTO> obtenerProductosMayorGanancia(Integer limite) {
        int limiteReal = limite != null ? limite : ReporteConstants.LIMITE_DEFAULT_PRODUCTOS;
        log.info(ReporteConstants.LOG_PRODUCTOS_MAYOR_GANANCIA, limiteReal);

        List<ProductoVendidoDTO> todosProductos = obtenerProductosMasVendidos(Integer.MAX_VALUE);

        List<ProductoVendidoDTO> resultado = todosProductos.stream()
                .sorted(Comparator.comparing(ProductoVendidoDTO::getGananciaNeta).reversed())
                .limit(limiteReal)
                .collect(Collectors.toList());

        log.info(ReporteConstants.LOG_RESULTADO_GENERADO,
                String.format("Top %d productos con mayor ganancia", resultado.size()));
        return resultado;
    }

    @Override
    public List<ClienteFrecuenteDTO> obtenerClientesFrecuentes(Integer limite) {
        int limiteReal = limite != null ? limite : ReporteConstants.LIMITE_DEFAULT_CLIENTES;
        log.info(ReporteConstants.LOG_CLIENTES_FRECUENTES, limiteReal);

        List<Solicitud> todasSolicitudes = solicitudRepository.findAll();
        Map<String, ClienteFrecuenteDTO> clientesMap = construirMapaClientes(todasSolicitudes);
        calcularPromediosClientes(clientesMap);

        List<ClienteFrecuenteDTO> resultado = clientesMap.values().stream()
                .sorted(Comparator.comparing(ClienteFrecuenteDTO::getTotalSolicitudes).reversed())
                .limit(limiteReal)
                .collect(Collectors.toList());

        log.info(ReporteConstants.LOG_RESULTADO_GENERADO,
                String.format("Top %d clientes frecuentes", resultado.size()));
        return resultado;
    }

    private Map<String, ClienteFrecuenteDTO> construirMapaClientes(List<Solicitud> todasSolicitudes) {
        Map<String, ClienteFrecuenteDTO> clientesMap = new HashMap<>();

        for (Solicitud solicitud : todasSolicitudes) {
            Usuario cliente = solicitud.getCliente();
            String cedula = cliente.getCedula();

            ClienteFrecuenteDTO dto = clientesMap.computeIfAbsent(
                    cedula, k -> crearClienteFrecuenteDTO(cliente));

            actualizarDatosCliente(dto, solicitud);
        }

        return clientesMap;
    }

    private ClienteFrecuenteDTO crearClienteFrecuenteDTO(Usuario cliente) {
        ClienteFrecuenteDTO dto = new ClienteFrecuenteDTO();
        dto.setCedula(cliente.getCedula());
        dto.setNombreCompleto(cliente.getNombres() + " " + cliente.getApellidos());
        dto.setEmail(cliente.getTelefono());
        dto.setTelefono(cliente.getTelefono());
        dto.setTotalSolicitudes(0);
        dto.setSolicitudesPagadas(0);
        dto.setMontoTotalGastado(BigDecimal.ZERO);
        dto.setPrimeraCompra(null);
        dto.setUltimaCompra(null);
        return dto;
    }

    private void actualizarDatosCliente(ClienteFrecuenteDTO dto, Solicitud solicitud) {
        dto.setTotalSolicitudes(dto.getTotalSolicitudes() + 1);
        actualizarFechasCliente(dto, solicitud);

        if (solicitud.getEstadoSolicitud() == EstadoSolicitudEnum.PGD) {
            dto.setSolicitudesPagadas(dto.getSolicitudesPagadas() + 1);
            BigDecimal montoSolicitud = calculosHelper.calcularMontoSolicitud(solicitud);
            dto.setMontoTotalGastado(dto.getMontoTotalGastado().add(montoSolicitud));
        }
    }

    private void actualizarFechasCliente(ClienteFrecuenteDTO dto, Solicitud solicitud) {
        if (dto.getPrimeraCompra() == null ||
                solicitud.getFechaSolicitud().isBefore(dto.getPrimeraCompra())) {
            dto.setPrimeraCompra(solicitud.getFechaSolicitud());
        }
        if (dto.getUltimaCompra() == null ||
                solicitud.getFechaSolicitud().isAfter(dto.getUltimaCompra())) {
            dto.setUltimaCompra(solicitud.getFechaSolicitud());
        }
    }

    private void calcularPromediosClientes(Map<String, ClienteFrecuenteDTO> clientesMap) {
        clientesMap.values().forEach(dto -> {
            if (dto.getSolicitudesPagadas() > 0) {
                BigDecimal promedio = dto.getMontoTotalGastado()
                        .divide(new BigDecimal(dto.getSolicitudesPagadas()), 2, RoundingMode.HALF_UP);
                dto.setPromedioGastoPorSolicitud(promedio);
            } else {
                dto.setPromedioGastoPorSolicitud(BigDecimal.ZERO);
            }
        });
    }

    @Override
    public List<PedidoEnCursoDTO> obtenerPedidosEnCurso() {
        log.info(ReporteConstants.LOG_PEDIDOS_EN_CURSO);

        List<Pedido> pedidosEnCurso = pedidoRepository.findPedidosEnCurso();
        List<PedidoEnCursoDTO> resultado = pedidosEnCurso.stream()
                .map(this::construirPedidoEnCursoDTO)
                .collect(Collectors.toList());

        log.info(ReporteConstants.LOG_PEDIDOS_EN_CURSO_COUNT, resultado.size());
        return resultado;
    }

    private PedidoEnCursoDTO construirPedidoEnCursoDTO(Pedido pedido) {
        PedidoEnCursoDTO dto = new PedidoEnCursoDTO();
        establecerDatosBasicosPedido(dto, pedido);

        List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(pedido.getIdPedido());
        establecerEstadisticasSolicitudes(dto, solicitudes);
        calcularMontosPedido(dto, solicitudes);
        calcularProgresoPedido(dto, solicitudes);

        return dto;
    }

    private void establecerDatosBasicosPedido(PedidoEnCursoDTO dto, Pedido pedido) {
        dto.setIdPedido(pedido.getIdPedido());
        dto.setEstadoPedido(pedido.getEstadoPedido().name());
        dto.setEstadoPedidoTraducido(ReporteConstants.traducirEstadoPedido(pedido.getEstadoPedido()));
        dto.setFechaCreacion(pedido.getFechaCreado());
        dto.setFechaCierre(pedido.getFechaCierre());

        long diasTranscurridos = ChronoUnit.DAYS.between(pedido.getFechaCreado(), LocalDate.now());
        dto.setDiasTranscurridos((int) diasTranscurridos);
    }

    private void establecerEstadisticasSolicitudes(PedidoEnCursoDTO dto, List<Solicitud> solicitudes) {
        dto.setTotalSolicitudes(solicitudes.size());

        long pendientes = solicitudes.stream()
                .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PDP)
                .count();
        long pagadas = solicitudes.stream()
                .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD)
                .count();

        dto.setSolicitudesPendientes((int) pendientes);
        dto.setSolicitudesConfirmadas(0);
        dto.setSolicitudesPagadas((int) pagadas);
    }

    private void calcularMontosPedido(PedidoEnCursoDTO dto, List<Solicitud> solicitudes) {
        BigDecimal montoTotal = BigDecimal.ZERO;
        BigDecimal montoPagado = BigDecimal.ZERO;

        for (Solicitud solicitud : solicitudes) {
            BigDecimal monto = calculosHelper.calcularMontoSolicitud(solicitud);
            montoTotal = montoTotal.add(monto);

            if (solicitud.getEstadoSolicitud() == EstadoSolicitudEnum.PGD) {
                montoPagado = montoPagado.add(monto);
            }
        }

        dto.setMontoTotalEstimado(montoTotal);
        dto.setMontoPagado(montoPagado);
    }

    private void calcularProgresoPedido(PedidoEnCursoDTO dto, List<Solicitud> solicitudes) {
        int progreso = solicitudes.isEmpty() ? 0 : (int) ((dto.getSolicitudesPagadas() * 100) / solicitudes.size());
        dto.setProgresoPercent(progreso);
    }

    @Override
    public ResumenEstadisticasDTO obtenerResumenEstadisticas() {
        log.info(ReporteConstants.LOG_RESUMEN_ESTADISTICAS);

        ResumenEstadisticasDTO resumen = new ResumenEstadisticasDTO();

        establecerEstadisticasPedidos(resumen);
        establecerEstadisticasSolicitudes(resumen);
        calcularEstadisticasFinancieras(resumen);
        establecerEstadisticasClientes(resumen);
        establecerEstadisticasProductos(resumen);

        log.info(ReporteConstants.LOG_RESULTADO_GENERADO);
        return resumen;
    }

    private void establecerEstadisticasPedidos(ResumenEstadisticasDTO resumen) {
        resumen.setTotalPedidos(Math.toIntExact(pedidoRepository.count()));
        resumen.setPedidosActivos(
                Math.toIntExact(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.ACT)));
        resumen.setPedidosEnCurso(
                Math.toIntExact(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.RTA) +
                        pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.ADU)));
        resumen.setPedidosEntregados(
                Math.toIntExact(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.ENT)));
        resumen.setPedidosCancelados(
                Math.toIntExact(pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.CRM) +
                        pedidoRepository.countByEstadoPedido(EstadoPedidoEnum.CRA)));
    }

    private void establecerEstadisticasSolicitudes(ResumenEstadisticasDTO resumen) {
        resumen.setTotalSolicitudes(Math.toIntExact(solicitudRepository.count()));
        resumen.setSolicitudesPendientes(
                Math.toIntExact(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.PDP)));
        resumen.setSolicitudesConfirmadas(0);
        resumen.setSolicitudesPagadas(
                Math.toIntExact(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.PGD)));
        resumen.setSolicitudesRechazadas(
                Math.toIntExact(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.CAN)));
    }

    private void calcularEstadisticasFinancieras(ResumenEstadisticasDTO resumen) {
        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findByEstadoSolicitud(EstadoSolicitudEnum.PGD);

        BigDecimal ingresosTotales = BigDecimal.ZERO;
        BigDecimal costosTotales = BigDecimal.ZERO;

        for (Solicitud solicitud : solicitudesPagadas) {
            BigDecimal ingresos = calculosHelper.calcularIngresosSolicitud(solicitud);
            BigDecimal costos = calculosHelper.calcularCostosSolicitud(solicitud);

            ingresosTotales = ingresosTotales.add(ingresos);
            costosTotales = costosTotales.add(costos);
        }

        resumen.setIngresosTotales(ingresosTotales);
        resumen.setCostosTotales(costosTotales);
        resumen.setGananciaNeta(ingresosTotales.subtract(costosTotales));
    }

    private void establecerEstadisticasClientes(ResumenEstadisticasDTO resumen) {
        resumen.setTotalClientes(Math.toIntExact(usuarioRepository.count()));
        resumen.setClientesActivos(Math.toIntExact(solicitudRepository.countClientesConSolicitudes()));
    }

    private void establecerEstadisticasProductos(ResumenEstadisticasDTO resumen) {
        resumen.setTotalProductos(Math.toIntExact(productoRepository.count()));

        Set<Integer> productosActivos = obtenerProductosActivosDePedidos();
        resumen.setProductosActivos(productosActivos.size());
    }

    private Set<Integer> obtenerProductosActivosDePedidos() {
        Set<Integer> productosActivos = new HashSet<>();
        List<Pedido> pedidosActivos = pedidoRepository.findByEstadoPedido(EstadoPedidoEnum.ACT);

        for (Pedido pedido : pedidosActivos) {
            for (ProductoPedido pp : pedido.getProductos()) {
                productosActivos.add(pp.getProducto().getIdProducto());
            }
        }

        return productosActivos;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private ReporteIngresosDTO crearReporteVacio(LocalDate fechaInicio, LocalDate fechaFin, String agrupacion) {
        ReporteIngresosDTO reporte = new ReporteIngresosDTO();
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setAgrupacion(agrupacion);
        reporte.setTotalIngresos(BigDecimal.ZERO);
        reporte.setTotalCostos(BigDecimal.ZERO);
        reporte.setGananciaNeta(BigDecimal.ZERO);
        reporte.setTotalSolicitudesPagadas(0);
        reporte.setDatosPorPeriodo(new ArrayList<>());
        return reporte;
    }
}

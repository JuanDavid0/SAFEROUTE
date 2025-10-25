package com.saferoute.service.impl;

import com.saferoute.dto.reporte.*;
import com.saferoute.model.*;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.*;
import com.saferoute.service.IReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements IReporteService {

    private final SolicitudRepository solicitudRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public ReporteIngresosDTO generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin, String agrupacion) {
        log.info("📊 Generando reporte de ingresos desde {} hasta {} - Agrupación: {}",
                fechaInicio, fechaFin, agrupacion);

        // Obtener solicitudes pagadas en el rango
        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findSolicitudesPagadasEnRango(fechaInicio, fechaFin);

        if (solicitudesPagadas.isEmpty()) {
            log.warn("⚠️ No se encontraron solicitudes pagadas en el rango especificado");
            return crearReporteVacio(fechaInicio, fechaFin, agrupacion);
        }

        // Calcular totales
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalCostos = BigDecimal.ZERO;

        for (Solicitud solicitud : solicitudesPagadas) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                BigDecimal cantidad = new BigDecimal(sp.getCantidadSolicitada());

                // Ingresos = precio * cantidad
                BigDecimal ingresos = sp.getPrecio().multiply(cantidad);
                totalIngresos = totalIngresos.add(ingresos);

                // Buscar costo del producto en el pedido
                // Nota: ProductoPedido no tiene costoUnitario, se obtiene de Producto
                BigDecimal costos = sp.getProducto().getCostoUnitario().multiply(cantidad);
                totalCostos = totalCostos.add(costos);
            }
        }

        BigDecimal gananciaNeta = totalIngresos.subtract(totalCostos);

        // Agrupar por periodo
        List<ReporteIngresosDTO.DatosPeriodo> datosPorPeriodo = agruparPorPeriodo(solicitudesPagadas, agrupacion);

        ReporteIngresosDTO reporte = new ReporteIngresosDTO();
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setAgrupacion(agrupacion);
        reporte.setTotalIngresos(totalIngresos);
        reporte.setTotalCostos(totalCostos);
        reporte.setGananciaNeta(gananciaNeta);
        reporte.setTotalSolicitudesPagadas(solicitudesPagadas.size());
        reporte.setDatosPorPeriodo(datosPorPeriodo);

        log.info("✅ Reporte generado: {} solicitudes, Ingresos: {}, Ganancia: {}",
                solicitudesPagadas.size(), totalIngresos, gananciaNeta);

        return reporte;
    }

    @Override
    public List<ProductoVendidoDTO> obtenerProductosMasVendidos(Integer limite) {
        log.info("📦 Obteniendo top {} productos más vendidos", limite);

        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findByEstadoSolicitud(EstadoSolicitudEnum.PGD);

        Map<Integer, ProductoVendidoDTO> productosMap = new HashMap<>();

        for (Solicitud solicitud : solicitudesPagadas) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                Integer idProducto = sp.getProducto().getIdProducto();

                ProductoVendidoDTO dto = productosMap.computeIfAbsent(idProducto, k -> {
                    ProductoVendidoDTO nuevo = new ProductoVendidoDTO();
                    nuevo.setIdProducto(idProducto);
                    nuevo.setNombreProducto(sp.getProducto().getNombreProducto());
                    nuevo.setDescripcion(sp.getProducto().getDescripcionProducto());
                    nuevo.setCategoria(sp.getProducto().getTipoProducto()); // Categoría = Tipo
                    nuevo.setCantidadTotalVendida(0);
                    nuevo.setIngresosGenerados(BigDecimal.ZERO);
                    nuevo.setCostosAsociados(BigDecimal.ZERO);
                    nuevo.setNumeroPedidos(0);
                    return nuevo;
                });

                // Acumular cantidades e ingresos
                dto.setCantidadTotalVendida(dto.getCantidadTotalVendida() + sp.getCantidadSolicitada());

                BigDecimal ingresos = sp.getPrecio()
                        .multiply(new BigDecimal(sp.getCantidadSolicitada()));
                dto.setIngresosGenerados(dto.getIngresosGenerados().add(ingresos));

                // Calcular costos (de Producto directamente)
                BigDecimal costos = sp.getProducto().getCostoUnitario()
                        .multiply(new BigDecimal(sp.getCantidadSolicitada()));
                dto.setCostosAsociados(dto.getCostosAsociados().add(costos));
            }
        }

        // Calcular ganancia neta
        productosMap.values().forEach(dto -> {
            BigDecimal ganancia = dto.getIngresosGenerados().subtract(dto.getCostosAsociados());
            dto.setGananciaNeta(ganancia);
        });

        // Ordenar por cantidad vendida y limitar
        List<ProductoVendidoDTO> resultado = productosMap.values().stream()
                .sorted(Comparator.comparing(ProductoVendidoDTO::getCantidadTotalVendida).reversed())
                .limit(limite != null ? limite : 10)
                .collect(Collectors.toList());

        log.info("✅ Top {} productos más vendidos obtenidos", resultado.size());
        return resultado;
    }

    @Override
    public List<ProductoVendidoDTO> obtenerProductosMayorGanancia(Integer limite) {
        log.info("💰 Obteniendo top {} productos con mayor ganancia", limite);

        // Reutilizamos la lógica de productos más vendidos
        List<ProductoVendidoDTO> todosProductos = obtenerProductosMasVendidos(Integer.MAX_VALUE);

        // Ordenar por ganancia neta
        List<ProductoVendidoDTO> resultado = todosProductos.stream()
                .sorted(Comparator.comparing(ProductoVendidoDTO::getGananciaNeta).reversed())
                .limit(limite != null ? limite : 10)
                .collect(Collectors.toList());

        log.info("✅ Top {} productos con mayor ganancia obtenidos", resultado.size());
        return resultado;
    }

    @Override
    public List<ClienteFrecuenteDTO> obtenerClientesFrecuentes(Integer limite) {
        log.info("👥 Obteniendo top {} clientes frecuentes", limite);

        List<Solicitud> todasSolicitudes = solicitudRepository.findAll();
        Map<String, ClienteFrecuenteDTO> clientesMap = new HashMap<>();

        for (Solicitud solicitud : todasSolicitudes) {
            Usuario cliente = solicitud.getCliente();
            String cedula = cliente.getCedula();

            ClienteFrecuenteDTO dto = clientesMap.computeIfAbsent(cedula, k -> {
                ClienteFrecuenteDTO nuevo = new ClienteFrecuenteDTO();
                nuevo.setCedula(cedula);
                nuevo.setNombreCompleto(cliente.getNombres() + " " + cliente.getApellidos());
                nuevo.setEmail(cliente.getTelefono()); // Email no existe, usamos teléfono
                nuevo.setTelefono(cliente.getTelefono());
                nuevo.setTotalSolicitudes(0);
                nuevo.setSolicitudesPagadas(0);
                nuevo.setMontoTotalGastado(BigDecimal.ZERO);
                nuevo.setPrimeraCompra(null);
                nuevo.setUltimaCompra(null);
                return nuevo;
            });

            // Incrementar contador
            dto.setTotalSolicitudes(dto.getTotalSolicitudes() + 1);

            // Actualizar fechas
            if (dto.getPrimeraCompra() == null ||
                    solicitud.getFechaSolicitud().isBefore(dto.getPrimeraCompra())) {
                dto.setPrimeraCompra(solicitud.getFechaSolicitud());
            }
            if (dto.getUltimaCompra() == null ||
                    solicitud.getFechaSolicitud().isAfter(dto.getUltimaCompra())) {
                dto.setUltimaCompra(solicitud.getFechaSolicitud());
            }

            // Si está pagada, sumar al monto
            if (solicitud.getEstadoSolicitud() == EstadoSolicitudEnum.PGD) {
                dto.setSolicitudesPagadas(dto.getSolicitudesPagadas() + 1);

                BigDecimal montoSolicitud = calcularMontoSolicitud(solicitud);
                dto.setMontoTotalGastado(dto.getMontoTotalGastado().add(montoSolicitud));
            }
        }

        // Calcular promedio de gasto
        clientesMap.values().forEach(dto -> {
            if (dto.getSolicitudesPagadas() > 0) {
                BigDecimal promedio = dto.getMontoTotalGastado()
                        .divide(new BigDecimal(dto.getSolicitudesPagadas()), 2, RoundingMode.HALF_UP);
                dto.setPromedioGastoPorSolicitud(promedio);
            } else {
                dto.setPromedioGastoPorSolicitud(BigDecimal.ZERO);
            }
        });

        // Ordenar por total de solicitudes y limitar
        List<ClienteFrecuenteDTO> resultado = clientesMap.values().stream()
                .sorted(Comparator.comparing(ClienteFrecuenteDTO::getTotalSolicitudes).reversed())
                .limit(limite != null ? limite : 20)
                .collect(Collectors.toList());

        log.info("✅ Top {} clientes frecuentes obtenidos", resultado.size());
        return resultado;
    }

    @Override
    public List<PedidoEnCursoDTO> obtenerPedidosEnCurso() {
        log.info("🚚 Obteniendo pedidos en curso");

        List<Pedido> pedidosEnCurso = pedidoRepository.findPedidosEnCurso();
        List<PedidoEnCursoDTO> resultado = new ArrayList<>();

        for (Pedido pedido : pedidosEnCurso) {
            PedidoEnCursoDTO dto = new PedidoEnCursoDTO();
            dto.setIdPedido(pedido.getIdPedido());
            dto.setEstadoPedido(pedido.getEstadoPedido().name());
            dto.setEstadoPedidoTraducido(traducirEstadoPedido(pedido.getEstadoPedido()));
            dto.setFechaCreacion(pedido.getFechaCreado()); // fechaCreado, no fechaCreacion
            dto.setFechaCierre(pedido.getFechaCierre());

            // Calcular días transcurridos
            long diasTranscurridos = ChronoUnit.DAYS.between(
                    pedido.getFechaCreado(), LocalDate.now());
            dto.setDiasTranscurridos((int) diasTranscurridos);

            // Obtener solicitudes del pedido
            List<Solicitud> solicitudes = solicitudRepository
                    .findByPedido_IdPedido(pedido.getIdPedido());

            dto.setTotalSolicitudes(solicitudes.size());

            // Contar por estado
            long pendientes = solicitudes.stream()
                    .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PDP)
                    .count();
            long confirmadas = 0; // No existe CFM en el enum actual
            long pagadas = solicitudes.stream()
                    .filter(s -> s.getEstadoSolicitud() == EstadoSolicitudEnum.PGD)
                    .count();

            dto.setSolicitudesPendientes((int) pendientes);
            dto.setSolicitudesConfirmadas((int) confirmadas);
            dto.setSolicitudesPagadas((int) pagadas);

            // Calcular montos
            BigDecimal montoTotal = BigDecimal.ZERO;
            BigDecimal montoPagado = BigDecimal.ZERO;

            for (Solicitud solicitud : solicitudes) {
                BigDecimal monto = calcularMontoSolicitud(solicitud);
                montoTotal = montoTotal.add(monto);

                if (solicitud.getEstadoSolicitud() == EstadoSolicitudEnum.PGD) {
                    montoPagado = montoPagado.add(monto);
                }
            }

            dto.setMontoTotalEstimado(montoTotal);
            dto.setMontoPagado(montoPagado);

            // Calcular progreso (basado en solicitudes pagadas)
            int progreso = solicitudes.isEmpty() ? 0 : (int) ((pagadas * 100) / solicitudes.size());
            dto.setProgresoPercent(progreso);

            resultado.add(dto);
        }

        log.info("✅ {} pedidos en curso obtenidos", resultado.size());
        return resultado;
    }

    @Override
    public ResumenEstadisticasDTO obtenerResumenEstadisticas() {
        log.info("📈 Generando resumen de estadísticas generales");

        ResumenEstadisticasDTO resumen = new ResumenEstadisticasDTO();

        // Estadísticas de Pedidos
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

        // Estadísticas de Solicitudes
        resumen.setTotalSolicitudes(Math.toIntExact(solicitudRepository.count()));
        resumen.setSolicitudesPendientes(
                Math.toIntExact(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.PDP)));
        resumen.setSolicitudesConfirmadas(0); // No existe CFM
        resumen.setSolicitudesPagadas(
                Math.toIntExact(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.PGD)));
        resumen.setSolicitudesRechazadas(
                Math.toIntExact(solicitudRepository.countByEstadoSolicitud(EstadoSolicitudEnum.CAN)));

        // Estadísticas Financieras (solo solicitudes pagadas)
        List<Solicitud> solicitudesPagadas = solicitudRepository
                .findByEstadoSolicitud(EstadoSolicitudEnum.PGD);

        BigDecimal ingresosTotales = BigDecimal.ZERO;
        BigDecimal costosTotales = BigDecimal.ZERO;

        for (Solicitud solicitud : solicitudesPagadas) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                BigDecimal cantidad = new BigDecimal(sp.getCantidadSolicitada());
                BigDecimal ingresos = sp.getPrecio().multiply(cantidad);
                ingresosTotales = ingresosTotales.add(ingresos);

                // Costos desde Producto directamente
                BigDecimal costos = sp.getProducto().getCostoUnitario().multiply(cantidad);
                costosTotales = costosTotales.add(costos);
            }
        }

        resumen.setIngresosTotales(ingresosTotales);
        resumen.setCostosTotales(costosTotales);
        resumen.setGananciaNeta(ingresosTotales.subtract(costosTotales));

        // Estadísticas de Clientes
        resumen.setTotalClientes(Math.toIntExact(usuarioRepository.count()));
        resumen.setClientesActivos(Math.toIntExact(solicitudRepository.countClientesConSolicitudes()));

        // Estadísticas de Productos
        resumen.setTotalProductos(Math.toIntExact(productoRepository.count()));

        // Productos activos = productos en pedidos ACT
        Set<Integer> productosActivos = new HashSet<>();
        List<Pedido> pedidosActivos = pedidoRepository
                .findByEstadoPedido(EstadoPedidoEnum.ACT);
        for (Pedido pedido : pedidosActivos) {
            for (ProductoPedido pp : pedido.getProductos()) {
                productosActivos.add(pp.getProducto().getIdProducto());
            }
        }
        resumen.setProductosActivos(productosActivos.size());

        log.info("✅ Resumen de estadísticas generado exitosamente");
        return resumen;
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

    private List<ReporteIngresosDTO.DatosPeriodo> agruparPorPeriodo(
            List<Solicitud> solicitudes, String agrupacion) {

        DateTimeFormatter formatter = agrupacion.equals("MENSUAL") ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy");

        Map<String, ReporteIngresosDTO.DatosPeriodo> periodosMap = new TreeMap<>();

        for (Solicitud solicitud : solicitudes) {
            String periodo = solicitud.getFechaSolicitud().format(formatter);

            ReporteIngresosDTO.DatosPeriodo datos = periodosMap.computeIfAbsent(periodo, k -> {
                ReporteIngresosDTO.DatosPeriodo nuevo = new ReporteIngresosDTO.DatosPeriodo();
                nuevo.setPeriodo(periodo);
                nuevo.setIngresos(BigDecimal.ZERO);
                nuevo.setCostos(BigDecimal.ZERO);
                nuevo.setGanancia(BigDecimal.ZERO);
                nuevo.setCantidadSolicitudes(0);
                return nuevo;
            });

            datos.setCantidadSolicitudes(datos.getCantidadSolicitudes() + 1);

            for (SolicitudProducto sp : solicitud.getProductos()) {
                BigDecimal cantidad = new BigDecimal(sp.getCantidadSolicitada());
                BigDecimal ingresos = sp.getPrecio().multiply(cantidad);
                datos.setIngresos(datos.getIngresos().add(ingresos));

                // Costos desde Producto directamente
                BigDecimal costos = sp.getProducto().getCostoUnitario().multiply(cantidad);
                datos.setCostos(datos.getCostos().add(costos));
            }

            datos.setGanancia(datos.getIngresos().subtract(datos.getCostos()));
        }

        return new ArrayList<>(periodosMap.values());
    }

    private BigDecimal calcularMontoSolicitud(Solicitud solicitud) {
        return solicitud.getProductos().stream()
                .map(sp -> sp.getPrecio().multiply(new BigDecimal(sp.getCantidadSolicitada())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String traducirEstadoPedido(EstadoPedidoEnum estado) {
        return switch (estado) {
            case CRT -> "Creado";
            case ACT -> "Activo";
            case RTA -> "En Ruta";
            case ADU -> "En Aduanas";
            case ENT -> "Entregado";
            case CRM -> "Cancelado por Manager";
            case CRA -> "Cancelado por Admin";
            case PRD -> "Perdido";
            case RCP -> "Recuperado";
        };
    }
}

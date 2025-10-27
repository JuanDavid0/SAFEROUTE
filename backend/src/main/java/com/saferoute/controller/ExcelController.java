package com.saferoute.controller;

import com.saferoute.service.interfaces.IExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExcelController {

    private final IExcelService excelService;

    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<byte[]> generarInformeCliente(@PathVariable("id") Integer idCliente) {
        log.info("📥 Solicitud para generar Excel de compras del cliente #{}", idCliente);

        ByteArrayOutputStream excel = excelService.generarInformeCliente(idCliente);

        String filename = String.format("Compras_Cliente_%d_%s.xlsx",
                idCliente,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excel.size());

        log.info(" Excel de compras del cliente #{} generado exitosamente - {} bytes",
                idCliente, excel.size());

        return ResponseEntity.ok()
                .headers(headers)
                .body(excel.toByteArray());
    }

    @GetMapping("/pedido/{id}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<byte[]> generarInformePedido(@PathVariable("id") Integer idPedido) {
        log.info("📥 Solicitud para generar Excel del pedido #{}", idPedido);

        ByteArrayOutputStream excel = excelService.generarInformePedidoConsolidado(idPedido);

        String filename = String.format("Informe_Pedido_%d_%s.xlsx",
                idPedido,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excel.size());

        log.info(" Excel del pedido #{} generado exitosamente - {} bytes", idPedido, excel.size());

        return ResponseEntity.ok()
                .headers(headers)
                .body(excel.toByteArray());
    }

}

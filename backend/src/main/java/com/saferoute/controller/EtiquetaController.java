package com.saferoute.controller;

import com.saferoute.dto.EtiquetasResponseDTO;
import com.saferoute.service.interfaces.IEtiquetaService;
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
@RequestMapping("/etiquetas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EtiquetaController {

    private final IEtiquetaService etiquetaService;

    @GetMapping("/pedido/{id}/json")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<EtiquetasResponseDTO> generarEtiquetasJSON(@PathVariable("id") Integer idPedido) {
        log.info("📥 Solicitud para generar etiquetas JSON del pedido #{}", idPedido);

        EtiquetasResponseDTO etiquetas = etiquetaService.generarEtiquetasJSON(idPedido);

        log.info(" Etiquetas JSON del pedido #{} generadas exitosamente - {} etiquetas",
                idPedido, etiquetas.getTotalEtiquetas());

        return ResponseEntity.ok(etiquetas);
    }

    @GetMapping("/pedido/{id}/pdf")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<byte[]> generarEtiquetasPDF(@PathVariable("id") Integer idPedido) {
        log.info("📥 Solicitud para generar PDF de etiquetas del pedido #{}", idPedido);

        ByteArrayOutputStream pdf = etiquetaService.generarEtiquetasPDF(idPedido);

        String filename = String.format("Etiquetas_Pedido_%d_%s.pdf",
                idPedido,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdf.size());

        log.info(" PDF de etiquetas del pedido #{} generado exitosamente - {} bytes",
                idPedido, pdf.size());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf.toByteArray());
    }

}

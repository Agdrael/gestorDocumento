package com.prograIV.gestorDocumento.controller;

import com.prograIV.gestorDocumento.model.Documento;
import com.prograIV.gestorDocumento.repository.DocumentoRepository;
import com.prograIV.gestorDocumento.service.UsuarioDatosService;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

@Controller
@RequiredArgsConstructor
public class DocumentosController {

    private final DocumentoRepository documentoRepo;
    private final UsuarioDatosService datosService;

    // API para Mis Documentos (lista personalizada por usuario)
    @GetMapping("/api/documentos/mios")
    @ResponseBody
    public Object obtenerMisDocs() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);
        Long idUsuario = datos.getIdUsuario();

        return documentoRepo.listarDocumentosPorUsuario(idUsuario);
    }

    // VER DOCUMENTO EN NAVEGADOR
    @GetMapping("/documentos/{id}/ver")
    public ResponseEntity<Resource> verDocumento(@PathVariable Long id) throws Exception {

        var doc = documentoRepo.findById(id).orElseThrow();
        Path path = Paths.get(doc.getUbicacion());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource recurso = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + path.getFileName().toString() + "\"")
                .body(recurso);
    }

    // DESCARGAR DOCUMENTO
    @GetMapping("/documentos/{id}/descargar")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long id) throws Exception {

        var doc = documentoRepo.findById(id).orElseThrow();
        Path path = Paths.get(doc.getUbicacion());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource recurso = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(recurso);
    }

    @PostMapping("/documentos/subir")
    @ResponseBody
    public ResponseEntity<?> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("titulo") String titulo) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            var datos = datosService.obtenerDatosPorUsername(username);
            Long idUsuario = datos.getIdUsuario();

            // 1) Validación
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body("Debe seleccionar un archivo.");
            }

            // Ruta portable (funciona en cualquier OS)
            String rootPath = new File("").getAbsolutePath();
            Path carpeta = Paths.get(rootPath, "uploads", "docs");

            // Crear carpeta si no existe
            if (!Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }

            // Guardar archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String nuevoNombre = "doc_" + idUsuario + "_" + System.currentTimeMillis() + "_" + nombreOriginal;

            Path destino = carpeta.resolve(nuevoNombre);
            archivo.transferTo(destino.toFile());

            // 4) Crear registro en la BD
            Documento doc = new Documento();
            doc.setTitulo(titulo);
            doc.setUbicacion(destino.toAbsolutePath().toString());
            doc.setCreadoPor(idUsuario);
            doc.setEstadoActual("primera_vista"); // Estado inicial
            doc.setVersion(1);
            doc.setCreadoEn(java.time.LocalDateTime.now());
            doc.setActualizadoEn(java.time.LocalDateTime.now());

            documentoRepo.save(doc);

            return ResponseEntity.ok("Documento subido correctamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir documento: " + e.getMessage());
        }
    }

}

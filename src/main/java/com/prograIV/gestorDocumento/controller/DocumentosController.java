package com.prograIV.gestorDocumento.controller;

import com.prograIV.gestorDocumento.model.Documento;
import com.prograIV.gestorDocumento.model.DocumentoEnvio;
import com.prograIV.gestorDocumento.repository.DocumentoEnvioRepository;
import com.prograIV.gestorDocumento.repository.DocumentoRepository;
import com.prograIV.gestorDocumento.repository.UsuarioRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.File;

@Controller
@RequiredArgsConstructor
public class DocumentosController {

    private final DocumentoRepository documentoRepo;
    private final UsuarioRepository usuarioRepo;
    private final DocumentoEnvioRepository envioRepo;
    private final UsuarioDatosService datosService;

    @GetMapping("/api/documentos/mios")
    @ResponseBody
    public Object obtenerMisDocs() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);
        Long idUsuario = datos.getIdUsuario();

        return documentoRepo.listarDocumentosPorUsuario(idUsuario);
    }

    @GetMapping("/api/documentos/papelera")
    @ResponseBody
    public Object obtenerPapelera() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);
        Long idUsuario = datos.getIdUsuario();

        return documentoRepo.listarDocumentosEnPapelera(idUsuario);
    }

    @GetMapping("/documentos/{id}/ver")
    public ResponseEntity<Resource> verDocumento(@PathVariable Long id) throws Exception {

        var doc = documentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        Path path = Paths.get(doc.getUbicacion());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource recurso = new UrlResource(path.toUri());

        String mime = Files.probeContentType(path);
        if (mime == null) {
            mime = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + path.getFileName().toString() + "\"")
                .body(recurso);
    }

    @GetMapping("/documentos/{id}/descargar")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long id) throws Exception {

        var doc = documentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

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

            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body("Debe seleccionar un archivo.");
            }

            String rootPath = new File("").getAbsolutePath();
            Path carpeta = Paths.get(rootPath, "uploads", "docs");

            if (!Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }

            String nombreOriginal = archivo.getOriginalFilename();
            String nuevoNombre = "doc_" + idUsuario + "_" + System.currentTimeMillis() + "_" + nombreOriginal;

            Path destino = carpeta.resolve(nuevoNombre);
            archivo.transferTo(destino.toFile());

            Documento doc = new Documento();
            doc.setTitulo(titulo);
            doc.setUbicacion(destino.toAbsolutePath().toString());
            doc.setCreadoPor(idUsuario);
            doc.setEstadoActual("almacenar");
            doc.setVersion(1);
            doc.setCreadoEn(java.time.LocalDateTime.now());
            doc.setActualizadoEn(java.time.LocalDateTime.now());

            documentoRepo.save(doc);

            return ResponseEntity.ok("Documento subido correctamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir documento: " + e.getMessage());
        }
    }

    @PostMapping("/documentos/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarDocumento(@PathVariable Long id) {

        var doc = documentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        doc.setEstadoActual("papelera");
        doc.setActualizadoEn(java.time.LocalDateTime.now());

        documentoRepo.save(doc);

        return ResponseEntity.ok("Documento enviado a papelera");
    }

    @PostMapping("/documentos/{id}/restaurar")
    @ResponseBody
    public ResponseEntity<?> restaurarDocumento(@PathVariable Long id) {

        var doc = documentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        doc.setEstadoActual("almacenar");
        doc.setActualizadoEn(java.time.LocalDateTime.now());

        documentoRepo.save(doc);

        return ResponseEntity.ok("Documento restaurado");
    }

    @DeleteMapping("/documentos/{id}/eliminar-definitivo")
    @ResponseBody
    public ResponseEntity<?> eliminarDefinitivo(@PathVariable Long id) {

        var doc = documentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        try {
            Path path = Paths.get(doc.getUbicacion());
            Files.deleteIfExists(path);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("No se pudo eliminar archivo físico");
        }

        documentoRepo.delete(doc);

        return ResponseEntity.ok("Documento eliminado permanentemente");
    }

    @PostMapping("/api/documentos/{id}/enviar")
    @ResponseBody
    public String enviarDocumento(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        // Obtener ID del admin destino desde el JSON
        Long idAdmin = Long.valueOf(body.get("idAdmin").toString());

        // Obtener el usuario que envía (usuario logueado)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);
        Long idUsuarioEnvia = datos.getIdUsuario();

        // Obtener documento
        var doc = documentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        // Crear registro en documento_envio
        DocumentoEnvio envio = new DocumentoEnvio();
        envio.idUsuarioEnvia = idUsuarioEnvia;
        envio.idUsuarioRecibe = idAdmin;
        envio.idDocumento = id;
        envio.fechaEnvio = LocalDateTime.now();
        envio.estado = "pendiente";

        envioRepo.save(envio);

        doc.estadoActual = "primera_vista";
        doc.actualizadoEn = LocalDateTime.now();
        documentoRepo.save(doc);

        return "Documento enviado correctamente.";
    }

    @GetMapping("/api/documentos/pendientes")
    @ResponseBody
    public List<Map<String, Object>> documentosPendientes() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);
        Long idAdmin = datos.getIdUsuario();

        return envioRepo.listarPendientesPorAdmin(idAdmin);
    }

    @PostMapping("/api/documentos/{id}/aprobar")
    @ResponseBody
    public String aprobarDocumento(@PathVariable Long id) {

        var envio = envioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe este envío"));

        envio.estado = "aprobado";
        envioRepo.save(envio);

        // actualizar el documento también
        var doc = documentoRepo.findById(envio.idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        doc.estadoActual = "verificado";
        doc.actualizadoEn = LocalDateTime.now();
        documentoRepo.save(doc);

        return "Documento aprobado correctamente";
    }

    @GetMapping("/api/documentos/enviados")
    @ResponseBody
    public List<Map<String, Object>> listarEnviados() {

        // Obtener sesión
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Buscar usuario en BD
        var usuario = usuarioRepo.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener el ID real del usuario
        Long idUsuario = usuario.idUsuario;

        // Obtener documentos enviados
        return documentoRepo.listarEnviadosPorUsuario(idUsuario);
    }

    @GetMapping("/api/documentos/por-aprobar")
    @ResponseBody
    public List<Object[]> documentosPorAprobar() {

        Long idAdmin = datosService.obtenerIdUsuarioActual();

        return envioRepo.documentosPorAprobar(idAdmin);
    }

    @PostMapping("/api/documentos/{id}/rechazar")
    @ResponseBody
    public String rechazar(@PathVariable Long id) {

        var envio = envioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Envio no encontrado"));

        envio.estado = "rechazado";
        envioRepo.save(envio);

        // Cambiar estado del documento
        var doc = documentoRepo.findById(envio.idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        doc.estadoActual = "rechazado";
        doc.actualizadoEn = LocalDateTime.now();
        documentoRepo.save(doc);

        return "Documento rechazado.";
    }

}

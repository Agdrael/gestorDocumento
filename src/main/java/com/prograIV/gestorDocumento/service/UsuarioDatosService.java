package com.prograIV.gestorDocumento.service;

import com.prograIV.gestorDocumento.model.Usuario;
import com.prograIV.gestorDocumento.model.UsuarioDatos;
import com.prograIV.gestorDocumento.repository.UsuarioRepository;
import com.prograIV.gestorDocumento.repository.UsuarioDatosRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
@RequiredArgsConstructor
public class UsuarioDatosService {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioDatosRepository datosRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene datos del usuario usando username (forma correcta con JOIN)
     */
    public UsuarioDatos obtenerDatosPorUsername(String username) {

        return datosRepo.findByUsuario_NombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Datos del usuario no encontrados"));
    }

    /**
     * Actualiza:
     * - nombre
     * - apellido
     * - password
     * - foto
     */
    @Transactional
    public void actualizarPerfil(Long idUsuario,
                                 String nombre,
                                 String apellido,
                                 String newPassword,
                                 MultipartFile foto) {

        UsuarioDatos datos = datosRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Datos no encontrados"));

        Usuario usuario = datos.getUsuario();

        // Actualizar nombre
        if (nombre != null && !nombre.isBlank()) {
            datos.setNombre(nombre.trim());
        }

        // Actualizar apellido
        if (apellido != null && !apellido.isBlank()) {
            datos.setApellido(apellido.trim());
        }

        // Actualizar contraseña
        if (newPassword != null && !newPassword.isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(newPassword));
            usuarioRepo.save(usuario);
        }

        // Actualizar foto si se envió
        if (foto != null && !foto.isEmpty()) {
            try {
                String original = foto.getOriginalFilename();
                String filename = "user_" + idUsuario + "_" + original;

                Path ruta = Paths.get("uploads/fotos/" + filename);
                Files.createDirectories(ruta.getParent());
                Files.write(ruta, foto.getBytes());

                datos.setFoto(filename);

            } catch (IOException e) {
                throw new RuntimeException("Error guardando foto", e);
            }
        }

        datosRepo.save(datos);
    }


    public Long obtenerIdUsuarioActual() {

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    String username;

    if (principal instanceof UserDetails userDetails) {
        username = userDetails.getUsername();
    } else {
        username = principal.toString();
    }

    // buscar id del usuario usando el username
    Usuario usuario = usuarioRepo.findByNombreUsuario(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    return usuario.idUsuario;
}

}

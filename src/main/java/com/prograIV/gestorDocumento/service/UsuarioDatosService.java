package com.prograIV.gestorDocumento.service;

import com.prograIV.gestorDocumento.model.Usuario;
import com.prograIV.gestorDocumento.model.UsuarioDatos;
import com.prograIV.gestorDocumento.repository.UsuarioRepository;
import com.prograIV.gestorDocumento.repository.UsuarioDatosRepository;
import lombok.RequiredArgsConstructor;
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

    public UsuarioDatos obtenerDatosPorUsername(String username) {
        Usuario u = usuarioRepo.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return datosRepo.findById(u.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Datos del usuario no encontrados"));
    }

    /**
     * Actualiza perfil:
     * - nombre
     * - apellido
     * - password (si viene)
     * - foto (si viene)
     *
     * Guarda foto en uploads/fotos/ y guarda el nombre del archivo en usuario_datos.foto
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

        if (nombre != null && !nombre.isBlank()) {
            datos.setNombre(nombre.trim());
        }

        if (apellido != null && !apellido.isBlank()) {
            datos.setApellido(apellido.trim());
        }

        if (newPassword != null && !newPassword.isBlank()) {
            String hash = passwordEncoder.encode(newPassword);
            usuario.setPasswordHash(hash);
            usuarioRepo.save(usuario);
        }

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
}

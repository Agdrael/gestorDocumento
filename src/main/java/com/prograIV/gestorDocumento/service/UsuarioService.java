package com.prograIV.gestorDocumento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.prograIV.gestorDocumento.model.*;
import com.prograIV.gestorDocumento.repository.*;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repoUsuario;
    private final UsuarioDatosRepository repoDatos;
    private final UsuarioRolRepository repoUsuarioRol;
    private final PasswordEncoder encoder;

    public void crearUsuarioCompleto(
            String nombreCompleto,
            String nombreUsuario,
            String clave,
            String correo,
            Long idRol // ahora se recibe el id real del rol
    ) {
        if (repoUsuario.existsByNombreUsuario(nombreUsuario)) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // Crear usuario base
        Usuario u = new Usuario();
        u.setNombreUsuario(nombreUsuario);
        u.setPasswordHash(encoder.encode(clave));
        u = repoUsuario.save(u);

        // Crear datos personales
        UsuarioDatos datos = new UsuarioDatos();
        datos.setUsuario(u);
        datos.setNombre(nombreCompleto);
        datos.setApellido(""); // si quieres agregarlo como parámetro
        datos.setCorreo(correo);
        datos.setActivo(true);

        repoDatos.save(datos);

        // Asignar rol en la tabla usuario_rol
        UsuarioRol ur = new UsuarioRol(u.getIdUsuario(), idRol.intValue());
        repoUsuarioRol.save(ur);
    }
}

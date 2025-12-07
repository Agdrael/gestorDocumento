package com.prograIV.gestorDocumento.controller;

import com.prograIV.gestorDocumento.model.Usuario;
import com.prograIV.gestorDocumento.model.UsuarioDatos;
import com.prograIV.gestorDocumento.model.UsuarioRol;
import com.prograIV.gestorDocumento.model.UsuarioRolId;
import com.prograIV.gestorDocumento.model.Rol;

import com.prograIV.gestorDocumento.repository.UsuarioRepository;
import com.prograIV.gestorDocumento.repository.UsuarioDatosRepository;
import com.prograIV.gestorDocumento.repository.RolRepository;
import com.prograIV.gestorDocumento.repository.UsuarioRolRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioDatosRepository datosRepo;
    private final RolRepository rolRepo;
    private final UsuarioRolRepository usuarioRolRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/api/usuarios/crear")
    @ResponseBody
    @Transactional
    public String crearUsuario(
            @RequestParam String nombreUsuario,
            @RequestParam String password,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String correo,
            @RequestParam String rol) {

        // 1. Verificar duplicado
        if (usuarioRepo.existsByNombreUsuario(nombreUsuario)) {
            return "El usuario ya existe.";
        }

        // 2. Crear usuario base (padre)
        Usuario u = new Usuario();
        u.setNombreUsuario(nombreUsuario);
        u.setPasswordHash(passwordEncoder.encode(password));
        usuarioRepo.save(u); // Aquí ya tiene ID

        // 3. Crear usuario_datos (hijo)
        UsuarioDatos datos = new UsuarioDatos();
        datos.setUsuario(u); // ✔ MapsId toma el idUsuario automáticamente
        datos.setNombre(nombre);
        datos.setApellido(apellido);
        datos.setCorreo(correo);
        datos.setActivo(true);

        datosRepo.save(datos);

        // 4. Asignar ROL
        String rolFinal = rol.equals("admin") ? "admin" : "usuario";

        Rol rolObj = rolRepo.findByNombre(rolFinal)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolFinal));

        UsuarioRol relacion = new UsuarioRol();
        relacion.setIdUsuario(u.getIdUsuario());
        relacion.setIdRol(rolObj.getIdRol());

        usuarioRolRepo.save(relacion);

        return "Usuario creado correctamente.";
    }

    @GetMapping("/api/usuarios/listar")
    @ResponseBody
    public List<Map<String, Object>> listarUsuarios() {
        return usuarioRepo.listarUsuariosConRol();
    }

    @PostMapping("/api/usuarios/{id}/desactivar")
    @ResponseBody
    public String desactivarUsuario(@PathVariable Long id) {

        UsuarioDatos datos = datosRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        datos.setActivo(false);
        datosRepo.save(datos);

        return "Usuario desactivado correctamente.";
    }

    @PostMapping("/api/usuarios/{id}/activar")
    @ResponseBody
    public String activarUsuario(@PathVariable Long id) {

        UsuarioDatos datos = datosRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        datos.setActivo(true);
        datosRepo.save(datos);

        return "Usuario activado correctamente.";
    }

    @PostMapping("/api/usuarios/restablecer")
    @ResponseBody
    @Transactional
    public String restablecerPassword(@RequestBody Map<String, Object> data) {

        Long idUsuario = Long.valueOf(data.get("idUsuario").toString());
        String nuevaPassword = data.get("nuevaPassword").toString();

        Usuario u = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepo.save(u);

        return "Contraseña actualizada correctamente.";
    }

    @GetMapping("/api/usuarios/admins")
    @ResponseBody
    public List<Map<String, Object>> listarAdmins() {

        // Obtener todos los roles admin
        var admins = usuarioRolRepo.findByRolNombre("admin");

        List<Map<String, Object>> lista = new ArrayList<>();

        for (var ur : admins) {

            Long idUsuario = ur.idUsuario;

            var datos = datosRepo.findById(idUsuario);

            if (datos.isPresent()) {
                UsuarioDatos d = datos.get();

                Map<String, Object> item = new HashMap<>();
                item.put("id", d.idUsuario);
                item.put("nombre", d.nombre + " " + d.apellido);
                item.put("correo", d.correo);

                lista.add(item);
            }
        }

        return lista;
    }

    @GetMapping("/api/usuarios/rol-actual")
@ResponseBody
public String obtenerRolActual() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();

    var usuario = usuarioRepo.findByNombreUsuario(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // Obtener rol desde usuario_rol
    var rol = usuarioRolRepo.obtenerRolUsuario(usuario.idUsuario);

    return rol; // "admin", "firmante", "revisor", "usuario"
}


}

package com.prograIV.gestorDocumento.controller;

import com.prograIV.gestorDocumento.service.UsuarioDatosService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioDatosService datosService;

    @GetMapping("/perfil")
    public String perfil(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);

        model.addAttribute("idUsuario", datos.getIdUsuario());
        model.addAttribute("usuarioNombre", datos.getNombre());
        model.addAttribute("usuarioApellido", datos.getApellido());
        model.addAttribute("usuarioFoto", datos.getFoto());

        return "sap/perfil"; // templates/sap/perfil.html (fragmento)
    }

    @PostMapping("/perfil/actualizar")
    @ResponseBody
    public String actualizarPerfil(
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        datosService.actualizarPerfil(idUsuario, nombre, apellido, password, foto);
        return "Datos actualizados correctamente";
    }
}

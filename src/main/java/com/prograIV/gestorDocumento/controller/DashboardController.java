package com.prograIV.gestorDocumento.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.prograIV.gestorDocumento.service.UsuarioDatosService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioDatosService datosService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);

        model.addAttribute("usuarioNombre", datos.getNombre());
        model.addAttribute("usuarioRol", datos.getRol());
        model.addAttribute("usuarioFoto", datos.getFoto()); // puede ser null

        return "dashboard"; // templates/dashboard.html
    }
}

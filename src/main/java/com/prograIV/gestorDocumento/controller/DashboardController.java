package com.prograIV.gestorDocumento.controller;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.prograIV.gestorDocumento.repository.DashboardRepository;
import com.prograIV.gestorDocumento.service.UsuarioDatosService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioDatosService datosService;
    private final DashboardRepository dashRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);

        // Nombre del usuario
        model.addAttribute("usuarioNombre", datos.getNombre());

        // Obtener el rol REAL desde Spring Security
        String rol = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("sin rol");

        model.addAttribute("usuarioRol", rol);

        model.addAttribute("usuarioFoto", datos.getFoto());

        return "dashboard";
    }

    @GetMapping("/dashboard-admin")
    public String dashboardAdmin(Authentication auth) {

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));

        if (!esAdmin) {
            return "redirect:/dashboard";
        }

        return "dashboard-admin";
    }

    @GetMapping("/dashboard-content")
    public String dashboardContent(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var datos = datosService.obtenerDatosPorUsername(username);

        String rol = auth.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("sin rol");

        model.addAttribute("usuarioNombre", datos.getNombre());
        model.addAttribute("usuarioApellido", datos.getApellido());
        model.addAttribute("usuarioFoto", datos.getFoto());
        model.addAttribute("usuarioRol", rol);

        return "dashboard-content";
    }

    @GetMapping("/api/admin/dashboard")
    @ResponseBody
    public Map<String, Object> obtenerDashboardAdmin() {

        Map<String, Object> data = new HashMap<>();

        data.put("porUsuario", dashRepo.documentosPorUsuario());
        data.put("ranking", dashRepo.rankingUsuarios());
        data.put("mensual", dashRepo.documentosPorMes());

        return data;
    }

    @GetMapping("/papelera")
    public String papelera() {
        return "papelera";
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "usuarios";
    }

    @GetMapping("/documentos/enviados")
    public String documentosEnviados() {
        return "documentos-enviados";
    }

    @GetMapping("/documentos/por-aprobar")
    public String documentosPorAprobar(Model model, Principal principal) {

        model.addAttribute("usuarioNombre", principal.getName());

        return "documentos-por-aprobar";
    }

}

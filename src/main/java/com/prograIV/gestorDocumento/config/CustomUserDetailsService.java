package com.prograIV.gestorDocumento.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.prograIV.gestorDocumento.model.Usuario;
import com.prograIV.gestorDocumento.model.UsuarioDatos;
import com.prograIV.gestorDocumento.repository.UsuarioRepository;
import com.prograIV.gestorDocumento.repository.UsuarioDatosRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioDatosRepository datosRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscar usuario principal
        Usuario usuario = usuarioRepo.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no existe"));

        // Verificar datos asociados
        UsuarioDatos datos = datosRepo.findById(usuario.getIdUsuario())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario sin datos asociados"));

        if (Boolean.FALSE.equals(datos.getActivo())) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        // Obtener roles REALES desde la DB
        List<String> roles = usuarioRepo.findRolesByUsuarioId(usuario.getIdUsuario());

        if (roles.isEmpty()) {
            throw new UsernameNotFoundException("El usuario no tiene roles asignados");
        }

        List<SimpleGrantedAuthority> authorities =
                roles.stream()
                        .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()))
                        .toList();

        return new org.springframework.security.core.userdetails.User(
                usuario.getNombreUsuario(),
                usuario.getPasswordHash(),
                authorities
        );
    }
}

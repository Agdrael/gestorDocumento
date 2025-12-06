package com.prograIV.gestorDocumento.repository;

import com.prograIV.gestorDocumento.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    @Query("""
                SELECT r.nombre
                FROM Rol r
                JOIN UsuarioRol ur ON r.idRol = ur.idRol
                WHERE ur.idUsuario = :idUsuario
            """)
    List<String> findRolesByUsuarioId(Long idUsuario);

}

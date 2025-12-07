package com.prograIV.gestorDocumento.repository;

import com.prograIV.gestorDocumento.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
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

    @Query(value = """
            SELECT ud.id_usuario AS id,
                   u.nombre_usuario AS usuario,
                   CONCAT(ud.nombre, ' ', ud.apellido) AS nombre,
                   ud.correo AS correo,
                   r.nombre AS rol,
                   ud.activo AS activo
            FROM usuario_datos ud
            JOIN usuarios u ON u.id_usuario = ud.id_usuario
            LEFT JOIN usuario_rol ur ON ur.id_usuario = ud.id_usuario
            LEFT JOIN roles r ON r.id_rol = ur.id_rol
            ORDER BY ud.id_usuario
            """, nativeQuery = true)
    List<Map<String, Object>> listarUsuariosConRol();

    

}

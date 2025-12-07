package com.prograIV.gestorDocumento.repository;

import com.prograIV.gestorDocumento.model.UsuarioRol;
import com.prograIV.gestorDocumento.model.UsuarioRolId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    @Query("""
                SELECT ur
                FROM UsuarioRol ur
                JOIN Rol r ON ur.idRol = r.idRol
                WHERE r.nombre = :nombreRol
            """)
    List<UsuarioRol> findByRolNombre(String nombreRol);

    @Query("""
                SELECT r.nombre
                FROM UsuarioRol ur
                JOIN Rol r ON r.idRol = ur.idRol
                WHERE ur.idUsuario = :idUsuario
                LIMIT 1
            """)
    String obtenerRolUsuario(Long idUsuario);

}

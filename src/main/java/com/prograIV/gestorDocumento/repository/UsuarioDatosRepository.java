package com.prograIV.gestorDocumento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.prograIV.gestorDocumento.model.UsuarioDatos;

import java.util.Optional;

public interface UsuarioDatosRepository extends JpaRepository<UsuarioDatos, Long> {

    Optional<UsuarioDatos> findByUsuario_NombreUsuario(String nombreUsuario);

}

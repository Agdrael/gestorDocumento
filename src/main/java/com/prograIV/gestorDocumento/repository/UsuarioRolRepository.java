package com.prograIV.gestorDocumento.repository;

import com.prograIV.gestorDocumento.model.UsuarioRol;
import com.prograIV.gestorDocumento.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
}

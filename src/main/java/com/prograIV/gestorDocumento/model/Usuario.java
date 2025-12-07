package com.prograIV.gestorDocumento.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Long idUsuario;

    @Column(name = "nombre_usuario", unique = true, nullable = false)
    private String nombreUsuario;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Timestamp creadoEn;

}


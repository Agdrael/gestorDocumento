package com.prograIV.gestorDocumento.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario_datos")
@Data
public class UsuarioDatos {

    @Id
    @Column(name = "id_usuario")
    public Long idUsuario;

    @Column(nullable = false)
    public String nombre;

    @Column(nullable = false)
    public String apellido;

    private String foto;

    @Column(nullable = false)
    public String correo;

    private Boolean activo = true;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}

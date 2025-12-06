package com.prograIV.gestorDocumento.model;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioRolId implements Serializable {
    private Long idUsuario;
    private Integer idRol;
}

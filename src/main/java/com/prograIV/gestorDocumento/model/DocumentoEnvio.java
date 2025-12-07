package com.prograIV.gestorDocumento.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "documento_envio")
@Data
public class DocumentoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quién envía el documento
    @Column(name = "id_usuario_envia")
    public Long idUsuarioEnvia;

    // A quién se envía
    @Column(name = "id_usuario_recibe")
    public Long idUsuarioRecibe;

    @Column(name = "id_documento")
    public Long idDocumento;

    public LocalDateTime fechaEnvio;

    public String estado;  // pendiente, visto, aprobado, rechazado
}

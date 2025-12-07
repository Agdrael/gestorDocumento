package com.prograIV.gestorDocumento.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Data
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doc")
    private Long idDoc;
    
    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String ubicacion;

    @Column(name = "estado_actual", nullable = false, length = 30)
    public String estadoActual;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    public LocalDateTime actualizadoEn;
}

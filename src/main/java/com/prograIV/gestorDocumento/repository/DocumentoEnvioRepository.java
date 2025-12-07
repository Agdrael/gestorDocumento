package com.prograIV.gestorDocumento.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.prograIV.gestorDocumento.model.DocumentoEnvio;
import com.prograIV.gestorDocumento.model.Documento;

public interface DocumentoEnvioRepository extends JpaRepository<DocumentoEnvio, Long> {

    @Query("""
                SELECT d, e
                FROM DocumentoEnvio e
                JOIN Documento d ON d.id = e.idDocumento
                WHERE e.idUsuarioEnvia = :idUsuario
                ORDER BY e.fechaEnvio DESC
            """)
    List<Object[]> listarEnviados(@Param("idUsuario") Long idUsuario);

    @Query("""
                SELECT d.titulo AS titulo,
                       d.idDoc AS id,
                       u.nombreUsuario AS usuarioEnvia,
                       e.fechaEnvio AS fecha
                FROM DocumentoEnvio e
                JOIN Documento d ON d.idDoc = e.idDocumento
                JOIN Usuario u ON u.idUsuario = e.idUsuarioEnvia
                WHERE e.idUsuarioRecibe = :idAdmin
                AND e.estado = 'pendiente'
            """)
    List<Map<String, Object>> listarPendientesPorAdmin(Long idAdmin);

    @Query("""
                SELECT d, e, u.nombreUsuario
                FROM DocumentoEnvio e
                JOIN Documento d ON d.idDoc = e.idDocumento
                JOIN Usuario u ON u.idUsuario = e.idUsuarioEnvia
                WHERE e.idUsuarioRecibe = :idAdmin
                AND e.estado = 'pendiente'
                ORDER BY e.fechaEnvio DESC
            """)
    List<Object[]> documentosPorAprobar(@Param("idAdmin") Long idAdmin);

}

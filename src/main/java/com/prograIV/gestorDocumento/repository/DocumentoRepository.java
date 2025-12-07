package com.prograIV.gestorDocumento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prograIV.gestorDocumento.model.Documento;

import java.util.List;
import java.util.Map;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

  @Query(value = """
      SELECT d.id_doc AS id,
             d.titulo AS titulo,
             d.ubicacion AS ubicacion,
             d.creado_en AS fecha,
             d.estado_actual AS estado,
             ud.nombre AS nombre,
             ud.apellido AS apellido
      FROM documentos d
      JOIN usuario_datos ud ON ud.id_usuario = d.creado_por
      WHERE d.creado_por = :idUsuario
        AND d.estado_actual <> 'papelera'
      ORDER BY d.creado_en DESC
      """, nativeQuery = true)
  List<Map<String, Object>> listarDocumentosPorUsuario(Long idUsuario);

  @Query(value = """
      SELECT d.id_doc AS id,
             d.titulo AS titulo,
             d.ubicacion AS ubicacion,
             d.creado_en AS fecha,
             d.estado_actual AS estado,
             ud.nombre AS nombre,
             ud.apellido AS apellido
      FROM documentos d
      JOIN usuario_datos ud ON ud.id_usuario = d.creado_por
      WHERE d.creado_por = :idUsuario
        AND d.estado_actual = 'papelera'
      ORDER BY d.actualizado_en DESC
      """, nativeQuery = true)
  List<Map<String, Object>> listarDocumentosEnPapelera(Long idUsuario);

  @Query(value = """
          SELECT *
          FROM documentos
          WHERE estado_actual = :estado
          ORDER BY actualizado_en DESC
      """, nativeQuery = true)
  List<Documento> listaPorEstado(String estado);

  @Query(value = """
            SELECT d.id_doc AS id,
                   d.titulo AS titulo,
                   d.creado_en AS fecha,
                   ud.nombre AS nombre,
                   ud.apellido AS apellido
            FROM documentos d
            JOIN usuario_datos ud ON ud.id_usuario = d.creado_por
            WHERE estado_actual <> 'primera_vista'
            ORDER BY d.actualizado_en DESC
      """, nativeQuery = true)
  List<Map<String, Object>> listarDocumentosPendientes();

  @Query(value = """
      SELECT d.id_doc AS id,
             d.titulo AS titulo,
             d.ubicacion AS ubicacion,
             d.creado_en AS fecha,
             d.estado_actual AS estado
      FROM documentos d
      WHERE d.creado_por = :idUsuario
      AND d.estado_actual IN ('primera_vista', 'enviado','rechazado','verificado')
      ORDER BY d.creado_en DESC
      """, nativeQuery = true)
  List<Map<String, Object>> listarEnviadosPorUsuario(Long idUsuario);

}

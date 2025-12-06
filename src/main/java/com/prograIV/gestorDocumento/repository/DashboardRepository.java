package com.prograIV.gestorDocumento.repository;

import com.prograIV.gestorDocumento.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DashboardRepository extends JpaRepository<Documento, Long> {

    @Query(value = """
        SELECT u.nombre_usuario AS usuario,
               COUNT(d.id_doc) AS cantidad
        FROM usuarios u
        LEFT JOIN documentos d ON d.creado_por = u.id_usuario
        GROUP BY u.id_usuario, u.nombre_usuario
        ORDER BY cantidad DESC
    """, nativeQuery = true)
    List<Map<String, Object>> documentosPorUsuario();


    @Query(value = """
        SELECT u.nombre_usuario AS usuario,
               COALESCE(COUNT(d.id_doc), 0) AS total
        FROM usuarios u
        LEFT JOIN documentos d ON d.creado_por = u.id_usuario
        GROUP BY u.id_usuario
        ORDER BY total DESC
    """, nativeQuery = true)
    List<Map<String, Object>> rankingUsuarios();


    @Query(value = """
        SELECT DATE_FORMAT(d.creado_en, '%Y-%m') AS mes,
               SUM(CASE WHEN d.estado_actual = 'primera_vista' THEN 1 ELSE 0 END) AS primera_vista,
               SUM(CASE WHEN d.estado_actual = 'verificado' THEN 1 ELSE 0 END) AS verificado,
               SUM(CASE WHEN d.estado_actual = 'firmado' THEN 1 ELSE 0 END) AS firmado,
               SUM(CASE WHEN d.estado_actual = 'rechazado' THEN 1 ELSE 0 END) AS rechazado
        FROM documentos d
        WHERE d.creado_en >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
        GROUP BY mes
        ORDER BY mes ASC
    """, nativeQuery = true)
    List<Map<String, Object>> documentosPorMes();


    @Query(value = "SELECT COUNT(*) FROM documentos", nativeQuery = true)
    Long totalDocumentos();

    @Query(value = "SELECT COUNT(*) FROM usuarios", nativeQuery = true)
    Long totalUsuarios();

    @Query(value = "SELECT COUNT(*) FROM documentos WHERE DATE(creado_en) = CURDATE()", nativeQuery = true)
    Long documentosHoy();

    @Query(value = "SELECT COUNT(*) FROM documentos WHERE estado_actual = 'firmado' AND DATE(creado_en) = CURDATE()", nativeQuery = true)
    Long firmadosHoy();

    @Query(value = "SELECT COUNT(*) FROM documentos WHERE estado_actual = 'primera_vista'", nativeQuery = true)
    Long pendientes();

    @Query(value = "SELECT COUNT(*) FROM documentos WHERE estado_actual = 'rechazado'", nativeQuery = true)
    Long rechazados();
}

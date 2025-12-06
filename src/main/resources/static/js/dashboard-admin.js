async function cargarDashboardAdmin() {

    const res = await fetch("/api/admin/dashboard");
    const data = await res.json();

    // --------------------------
    // PIE - DOCUMENTOS POR USUARIO
    // --------------------------
    const pieLabels = data.porUsuario.map(x => x.usuario);
    const pieData = data.porUsuario.map(x => x.cantidad);

    new Chart(document.getElementById("chartPieUsuarios"), {
        type: "pie",
        data: {
            labels: pieLabels,
            datasets: [{
                data: pieData
            }]
        }
    });

    // --------------------------
    // RANKING TABLA
    // --------------------------
    const ranking = document.getElementById("tablaRanking");
    ranking.innerHTML = "";
    data.ranking.forEach(r => {
        ranking.innerHTML += `
            <tr>
                <td>${r.usuario}</td>
                <td>${r.total}</td>
            </tr>
        `;
    });

    // --------------------------
    // LINEA - DOCUMENTOS MENSUALES
    // --------------------------
    const meses = data.mensual.map(x => x.mes);
    const primeraVista = data.mensual.map(x => x.primera_vista);
    const verificados = data.mensual.map(x => x.verificado);
    const firmados = data.mensual.map(x => x.firmado);
    const rechazados = data.mensual.map(x => x.rechazado);

    new Chart(document.getElementById("chartLineaMeses"), {
        type: "line",
        data: {
            labels: meses,
            datasets: [
                { label: "Primera Vista", data: primeraVista, borderColor: "yellow" },
                { label: "Verificados", data: verificados, borderColor: "aqua" },
                { label: "Firmados", data: firmados, borderColor: "lightgreen" },
                { label: "Rechazados", data: rechazados, borderColor: "red" }
            ]
        }
    });
}

cargarDashboardAdmin();

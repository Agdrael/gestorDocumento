document.addEventListener("DOMContentLoaded", () => {

    const sidebar = document.getElementById("sidebar");
    const content = document.getElementById("content");
    const btnToggle = document.getElementById("btnToggleSidebar");
    const sapBody = document.getElementById("sap-body");

    // Toggle sidebar (si tienes esos elementos)
    if (btnToggle && sidebar && content) {
        btnToggle.addEventListener("click", () => {
            sidebar.classList.toggle("collapsed");
            content.classList.toggle("expanded");

            document.querySelectorAll(".sidebar-text").forEach(el => {
                el.style.display = sidebar.classList.contains("collapsed") ? "none" : "inline";
            });
        });
    }

    // Cargar módulos del sidebar por AJAX
    document.querySelectorAll("#sidebar .menu a[data-module]").forEach(link => {
        link.addEventListener("click", async e => {
            e.preventDefault();
            const moduleName = link.dataset.module;

            const response = await fetch(`${moduleName}`);
            const html = await response.text();

            sapBody.innerHTML = html;
        });
    });

});

// Cargar cualquier vista dentro del dashboard
async function cargarVista(url) {


    const sapBody = document.getElementById("sap-body");

    const response = await fetch(url);
    if (!response.ok) {
        sapBody.innerHTML = "<h3 class='text-danger'>Error cargando vista</h3>";
        return;
    }

    const html = await response.text();
    sapBody.innerHTML = html;

    // preview de foto (solo si existe el input)
    const fotoInput = document.getElementById("fotoInput");
    if (fotoInput) {
        fotoInput.addEventListener("change", e => {
            const file = e.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = r => {
                const preview = document.getElementById("fotoPreview");
                if (preview) preview.src = r.target.result;
            };
            reader.readAsDataURL(file);
        });
    }

    if (url === "/dashboard-content") {
        setTimeout(() => cargarDocumentos(), 50);
    }

}

async function guardarPerfil() {

    const idUsuario = document.getElementById("idUsuario").value;
    const nombre = document.getElementById("nombre").value;
    const apellido = document.getElementById("apellido").value;
    const pass1 = document.getElementById("pass1").value;
    const pass2 = document.getElementById("pass2").value;
    const foto = document.getElementById("fotoInput").files[0];

    if (pass1 !== pass2) {
        alert("Las contraseñas no coinciden");
        return;
    }

    let form = new FormData();
    form.append("idUsuario", idUsuario);
    form.append("nombre", nombre);
    form.append("apellido", apellido);
    form.append("password", pass1);

    if (foto) {
        form.append("foto", foto);
    }

    const response = await fetch("/perfil/actualizar", {
        method: "POST",
        body: form
    });

    const msg = await response.text();
    alert(msg);
}

async function cargarDocumentos() {
    const res = await fetch("/api/documentos/mios");
    const docs = await res.json();

    const cont = document.getElementById("lista-documentos");
    cont.innerHTML = "";

    docs.forEach(doc => {

        cont.innerHTML += `
        <div class="col-md-6 col-xl-4">
            <div class="card p-3 h-100">

                <div class="d-flex justify-content-between align-items-center">
                    <span class="fw-bold">DOC</span>
                    <button class="btn p-0" onclick="descargarDoc('${doc.id}')">
                        <i class="bi bi-download fs-5"></i>
                    </button>
                </div>

                <h5 class="mt-2">${doc.titulo}</h5>
                <p class="text-muted small mb-1">${doc.fecha}</p>

                <p class="small mb-1">Por: ${doc.nombre} ${doc.apellido}</p>

                <div class="mb-3">
                    ${obtenerBadgeEstado(doc.estado)}
                </div>

                <div class="d-flex gap-2 mt-auto">
                    <button class="btn btn-outline-primary btn-sm" onclick="verDoc('${doc.id}')">
                        Ver
                    </button>

                    <button class="btn btn-primary btn-sm">
                        Aprobar
                    </button>
                </div>

            </div>
        </div>
        `;
    });
}

async function subirDocumento() {
    let titulo = document.getElementById("tituloDoc").value;
    let archivo = document.getElementById("archivoDoc").files[0];

    if (!titulo) {
        alert("Debe ingresar un título.");
        return;
    }

    if (!archivo) {
        alert("Debe seleccionar un archivo.");
        return;
    }

    let form = new FormData();
    form.append("titulo", titulo);
    form.append("archivo", archivo);

    let res = await fetch("/documentos/subir", {
        method: "POST",
        body: form
    });

    let texto = await res.text();
    alert(texto);

    // Recargar lista de documentos
    cargarDocumentos();
}

function obtenerBadgeEstado(estado) {
    switch (estado) {
        case "primera_vista":
            return '<span class="badge bg-warning text-dark">Primera vista</span>';

        case "verificado":
            return '<span class="badge bg-info text-dark">Verificado</span>';

        case "firmado":
            return '<span class="badge bg-success">Firmado</span>';

        case "rechazado":
            return '<span class="badge bg-danger">Rechazado</span>';

        default:
            return '<span class="badge bg-secondary">Desconocido</span>';
    }
}



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

    cargarVista('/dashboard-admin');

});

// Cargar cualquier vista dentro del dashboard
async function cargarVista(url) {

    const sapBody = document.getElementById("sap-body");

    const response = await fetch(url, {
        method: "GET",
        credentials: "include",
        headers: {
            "X-Requested-With": "XMLHttpRequest"
        }
    });

    if (!response.ok) {
        sapBody.innerHTML = "<h3 class='text-danger'>Error cargando vista</h3>";
        return;
    }

    const html = await response.text();
    sapBody.innerHTML = html;

    // preview de foto (se mantiene igual)
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

    // Si la vista es dashboard admin, carga scripts necesarios
    if (url === "/dashboard-admin") {
        await cargarScript("https://cdn.jsdelivr.net/npm/chart.js");
        await cargarScript("/js/dashboard-admin.js");
    }


    // cargar documentos si aplica
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
            <div class="card p-3 h-100 d-flex flex-column">

            <div class="d-flex justify-content-between align-items-center">
                <span class="fw-bold">DOC</span>
                <button class="btn p-0" onclick="descargarDoc('${doc.id}')">
                    <i class="bi bi-download fs-5"></i>
                </button>
            </div>

            <h5 class="mt-2">${doc.titulo}</h5>
            <p class="text-muted small mb-1">${doc.fecha}</p>

            <div class="d-flex gap-2 mt-auto">
                <button class="btn btn-outline-primary btn-sm flex-fill" onclick="verDoc('${doc.id}')">
                    Ver
                </button>

                <button class="btn btn-danger btn-sm flex-fill" onclick="eliminarDoc('${doc.id}')">
                    Eliminar
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

function cargarScript(url) {
    return new Promise(resolve => {
        const s = document.createElement("script");
        s.src = url;
        s.onload = resolve;
        document.body.appendChild(s);
    });
}


function descargarDoc(id) {
    window.location.href = `/documentos/${id}/descargar`;
}

function verDoc(id) {
    window.open(`/documentos/${id}/ver`, "_blank");
}

async function eliminarDoc(id) {
    if (!confirm("¿Enviar a papelera?")) return;

    const res = await fetch(`/documentos/${id}/eliminar`, {
        method: "POST"
    });

    const msg = await res.text();
    alert(msg);

    cargarDocumentos(); // recargar lista
}

//papelera
async function cargarPapelera() {

    const res = await fetch("/api/documentos/papelera");
    const docs = await res.json();

    const cont = document.getElementById("lista-papelera");
    cont.innerHTML = "";

    docs.forEach(doc => {

        cont.innerHTML += `
        <div class="col-md-6 col-xl-4">
            <div class="card p-3 h-100">

                <h5 class="mt-2">${doc.titulo}</h5>
                <p class="text-muted small mb-1">${doc.fecha}</p>

                <p class="small mb-1">Por: ${doc.nombre} ${doc.apellido}</p>

                <div class="d-flex gap-2 mt-auto">

                    <button class="btn btn-outline-primary btn-sm" onclick="restaurarDoc('${doc.id}')">
                        Restaurar
                    </button>

                    <button class="btn btn-danger btn-sm" onclick="eliminarDefinitivo('${doc.id}')">
                        Eliminar permanente
                    </button>

                </div>

            </div>
        </div>
        `;
    });
}

async function restaurarDoc(id) {

    if (!confirm("¿Restaurar documento?")) return;

    const res = await fetch(`/documentos/${id}/restaurar`, {
        method: "POST"
    });

    alert(await res.text());

    cargarPapelera();
}

async function eliminarDefinitivo(id) {

    if (!confirm("¿Eliminar permanentemente? Esta acción no se puede deshacer.")) return;

    const res = await fetch(`/documentos/${id}/eliminar-definitivo`, {
        method: "DELETE"
    });

    alert(await res.text());

    cargarPapelera();
}





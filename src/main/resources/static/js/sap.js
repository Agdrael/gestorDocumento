let docAEnviar = null;


document.addEventListener("DOMContentLoaded", () => {

    aplicarRestriccionesDeRol();

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
    loadDefaultView();


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

    if (url === "/papelera") {
        setTimeout(() => cargarPapelera(), 50);
    }

    if (url === "/usuarios") {
        setTimeout(() => cargarUsuarios(), 50);
    }

    if (url === "/documentos/enviados") {
        setTimeout(() => cargarDocsEnviados(), 50);
    }

    if (url === "/documentos/por-aprobar") {
        setTimeout(() => cargarDocumentosPorAprobar(), 50);
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

                 <button class="btn btn-warning btn-sm mt-2" onclick="abrirModalEnviar(${doc.id})">
                        <i class="bi bi-send"></i> Enviar
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

//usuarios
async function crearUsuario(e) {
    e.preventDefault();

    const form = new FormData();
    form.append("nombreUsuario", document.getElementById("nombreUsuario").value);
    form.append("password", document.getElementById("password").value);
    form.append("nombre", document.getElementById("nombre").value);
    form.append("apellido", document.getElementById("apellido").value);
    form.append("correo", document.getElementById("correo").value);
    form.append("rol", document.getElementById("rol").value);

    const res = await fetch("/api/usuarios/crear", {
        method: "POST",
        body: form
    });

    const texto = await res.text();
    alert(texto);

    cargarUsuarios();
}

async function cargarUsuarios() {

    const res = await fetch("/api/usuarios/listar");
    const lista = await res.json();

    let tbody = document.getElementById("tabla-usuarios");
    tbody.innerHTML = "";

    lista.forEach(u => {

        tbody.innerHTML += `
            <tr>
                <td>${u.id}</td>
                <td>${u.usuario}</td>
                <td>${u.nombre} </td>
                <td>${u.correo}</td>

                <td>
                    <span class="badge bg-primary">${u.rol}</span>
                </td>

                <td>
                    ${u.activo
                ? `<span class="badge bg-success">Activo</span>`
                : `<span class="badge bg-danger">Inactivo</span>`
            }
                </td>

                <td class="text-center">

                    ${u.activo
                ? `<button class="btn btn-warning btn-sm" onclick="desactivarUsuario(${u.idUsuario})">
                               Desactivar
                           </button>`
                : `<button class="btn btn-success btn-sm" onclick="activarUsuario(${u.idUsuario})">
                               Activar
                           </button>`


            }
                   

                </td>
                <td class="text-center">
                    <button class="btn btn-warning btn-sm" onclick="restablecerPassword(${u.id})">
                        <i class="bi bi-key"></i> Restablecer
                    </button>
                </td>
            </tr>
        `;
    });
}

async function desactivarUsuario(id) {
    if (!confirm("¿Desactivar este usuario?")) return;

    const res = await fetch(`/api/usuarios/${id}/desactivar`, {
        method: "POST"
    });

    alert(await res.text());
    cargarUsuarios();
}

async function activarUsuario(id) {
    if (!confirm("¿Activar este usuario?")) return;

    const res = await fetch(`/api/usuarios/${id}/activar`, {
        method: "POST"
    });

    alert(await res.text());
    cargarUsuarios();
}

function restablecerPassword(idUsuario) {
    const nuevaPass = prompt("Ingrese la nueva contraseña:");

    if (!nuevaPass || nuevaPass.trim() === "") {
        alert("Debe ingresar una contraseña válida.");
        return;
    }

    fetch("/api/usuarios/restablecer", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            idUsuario: idUsuario,
            nuevaPassword: nuevaPass
        })
    })
        .then(r => r.text())
        .then(msg => {
            alert(msg);
            cargarUsuarios();
        })
        .catch(err => alert("Error: " + err));
}

//enviados

// Cargar documentos enviados

function cargarDocsEnviados() {
    fetch("/api/documentos/enviados")
        .then(r => r.json())
        .then(lista => renderBandejaGmailEnviados(lista));
}




function renderBandejaGmailEnviados(lista) {

    const cont = document.getElementById("listaEnviados");
    cont.innerHTML = "";

    lista.forEach(doc => {

        let icono = "bi-file-earmark";
        if (doc.ubicacion.endsWith(".pdf")) icono = "bi-file-earmark-pdf-fill text-danger";
        if (doc.ubicacion.endsWith(".doc") || doc.ubicacion.endsWith(".docx")) icono = "bi-file-earmark-word-fill text-primary";

        const item = `
            <a class="list-group-item list-group-item-action d-flex gap-3 py-3 align-items-center">

                <i class="bi ${icono} fs-3"></i>

                <div class="w-100 d-flex flex-column">

                    <div class="d-flex justify-content-between">
                        <strong>${doc.titulo}</strong>
                        <small class="text-muted">${doc.fecha?.substring(0, 10)}</small>
                    </div>

                    <div class="d-flex justify-content-between">
                        <small class="text-muted">Estado: ${doc.estado}</small>

                        <div>
                            <button class="btn btn-sm btn-outline-primary me-2"
                                onclick="verDoc(${doc.id})">
                                Ver
                            </button>

                            <button class="btn btn-sm btn-outline-secondary"
                                onclick="descargarDoc(${doc.id})">
                                Descargar
                            </button>

                            
                        </div>
                    </div>

                </div>

            </a>
        `;

        cont.insertAdjacentHTML("beforeend", item);
    });
}


// Acciones

function verDoc(id) {
    window.open(`/documentos/${id}/ver`, "_blank");
}

function descargarDoc(id) {
    window.location.href = `/documentos/${id}/descargar`;
}

//modal
function abrirModalEnviar(idDoc) {
    docAEnviar = idDoc;

    fetch("/api/usuarios/admins")
        .then(r => r.json())
        .then(admins => {

            let select = document.getElementById("selectAdmin");
            select.innerHTML = "";

            admins.forEach(a => {
                select.innerHTML += `
                    <option value="${a.id}">
                        ${a.nombre}
                    </option>
                `;
            });

            let modal = new bootstrap.Modal(document.getElementById("modalEnviarDoc"));
            modal.show();
        });
}

function confirmarEnvio() {
    let admin = document.getElementById("selectAdmin").value;

    fetch(`/api/documentos/${docAEnviar}/enviar`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ idAdmin: admin })
    })
        .then(r => r.text())
        .then(msg => {
            alert(msg);
            bootstrap.Modal.getInstance(document.getElementById("modalEnviarDoc")).hide();
        });
}


async function aplicarRestriccionesDeRol() {

    let res = await fetch("/api/usuarios/rol-actual");
    let rol = await res.text();

    // Solo los admins pueden ver estas opciones
    if (rol !== "admin") {
cargarVista('/documentos/por-aprobar')
        // Ocultar Docs por Aprobar
        document.querySelector('a[onclick="cargarVista(\'/documentos/por-aprobar\')"]')?.classList.add("d-none");

        // Ocultar Crear Usuario / Admin
        document.querySelector('a[onclick="cargarVista(\'/usuarios\')"]')?.classList.add("d-none");

        // ocultar dashboard admin
        document.querySelector('a[onclick="cargarVista(\'/dashboard-admin\')"]')?.classList.add("d-none");
    }
}

async function loadDefaultView() {
    try {
        let resp = await fetch("/api/usuarios/rol-actual");
        let rol = await resp.text();

        // limpiar espacios
        rol = rol.trim();

        if (rol === "admin") {
            // carga el dashboard de administrador
            cargarVista('/dashboard-admin');
        } else {
            // carga MIS DOCUMENTOS (usuarios normales)
            cargarVista('/dashboard-content');
        }

    } catch (e) {
        console.error("Error obteniendo rol:", e);
        loadContent("dashboard-content.html"); // fallback
    }
}

async function cargarDocumentosPorAprobar() {
    let resp = await fetch("/api/documentos/por-aprobar");
    let data = await resp.json();
    let tbody = document.getElementById("tabla-aprobar");
    tbody.innerHTML = "";

    data.forEach(item => {
        let documento = item[0];
        let envio = item[1];
        let usuarioEnvia = item[2]; // ← nombreUsuario obtenido de la query

        tbody.innerHTML += `
            <tr>
                <td>${documento.idDoc}</td>
                <td>${documento.titulo}</td>
                <td>${usuarioEnvia}</td>
                <td>${new Date(envio.fechaEnvio).toLocaleString()}</td>

                <td class="text-center">
                    <button class="btn btn-success btn-sm"
                        onclick="aprobarDocumento(${envio.id})">
                        Aprobar
                    </button>

                    <button class="btn btn-danger btn-sm"
                        onclick="rechazarDocumento(${envio.id})">
                        Rechazar
                    </button>
                </td>
            </tr>`;
    });
}



async function aprobarDocumento(idEnvio) {
    await fetch(`/api/documentos/${idEnvio}/aprobar`, { method: "POST" });
    cargarDocumentosPorAprobar();
}

async function rechazarDocumento(idEnvio) {
    await fetch(`/api/documentos/${idEnvio}/rechazar`, { method: "POST" });
    cargarDocumentosPorAprobar();
}







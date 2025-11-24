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

            const response = await fetch(`/sap/${moduleName}`);
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

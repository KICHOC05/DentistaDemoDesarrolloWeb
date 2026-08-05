function getToken() {
    var meta = document.querySelector('meta[name="token"]');
    return meta ? meta.getAttribute('content') : '';
}

function apiFetch(url, options) {
    var token = getToken();
    var headers = options.headers || {};
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    options.headers = headers;
    return fetch(url, options);
}

function handleResponse(response) {
    if (!response.ok) {
        return response.json().then(function (err) {
            throw new Error(err.message || 'Error al procesar la solicitud.');
        });
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function handleError(error) {
    alert('Error: ' + error.message);
}

function toggleService(publicId) {
    if (!confirm('Desea cambiar el estado de este servicio?')) return;
    var row = document.getElementById('row-' + publicId);
    var badge = row.querySelector('.status-badge');
    var isActive = badge.classList.contains('status-active');

    apiFetch('/api/catalog/services/' + publicId + '/status', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ active: !isActive })
    })
    .then(handleResponse)
    .then(function (service) {
        if (service.active) {
            badge.textContent = 'Activo';
            badge.className = 'status-badge status-active';
        } else {
            badge.textContent = 'Inactivo';
            badge.className = 'status-badge status-inactive';
        }
    })
    .catch(handleError);
}

function deleteService(publicId) {
    if (!confirm('Esta seguro de eliminar este servicio? Esta accion no se puede deshacer.')) return;
    apiFetch('/api/catalog/services/' + publicId, { method: 'DELETE' })
        .then(function () {
            var row = document.getElementById('row-' + publicId);
            if (row) row.remove();
        })
        .catch(handleError);
}

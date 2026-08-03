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

function confirmAppointment(publicId) {
    if (!confirm('Confirmar esta cita?')) return;
    apiFetch('/api/appointments/' + publicId + '/confirm', { method: 'PATCH' })
        .then(handleResponse)
        .then(function () {
            alert('Cita confirmada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

function cancelAppointment(publicId) {
    if (!confirm('Cancelar esta cita?')) return;
    apiFetch('/api/appointments/' + publicId + '/cancel', { method: 'PATCH' })
        .then(handleResponse)
        .then(function () {
            alert('Cita cancelada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

function completeAppointment(publicId) {
    if (!confirm('Completar esta cita?')) return;
    apiFetch('/api/appointments/' + publicId + '/complete', { method: 'PATCH' })
        .then(handleResponse)
        .then(function () {
            alert('Cita completada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

var currentRescheduleId = null;

function rescheduleAppointment(publicId) {
    currentRescheduleId = publicId;
    document.getElementById('rescheduleModal').style.display = 'block';
    document.getElementById('rescheduleDate').value = '';
    document.getElementById('rescheduleStartTime').value = '';
    document.getElementById('rescheduleEndTime').value = '';
}

function closeRescheduleModal() {
    document.getElementById('rescheduleModal').style.display = 'none';
    currentRescheduleId = null;
}

function submitReschedule() {
    var newDate = document.getElementById('rescheduleDate').value;
    var newTime = document.getElementById('rescheduleStartTime').value;
    var newEndTime = document.getElementById('rescheduleEndTime').value;

    if (!newDate || !newTime || !newEndTime) {
        alert('Todos los campos son obligatorios.');
        return;
    }
    if (newTime >= newEndTime) {
        alert('La hora de fin debe ser posterior a la hora de inicio.');
        return;
    }

    apiFetch('/api/appointments/' + currentRescheduleId + '/reschedule', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            newDate: newDate,
            newTime: newTime + ':00',
            newEndTime: newEndTime + ':00'
        })
    })
        .then(handleResponse)
        .then(function () {
            alert('Cita reagendada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

window.onclick = function(event) {
    var modal = document.getElementById('rescheduleModal');
    if (event.target == modal) {
        closeRescheduleModal();
    }
};

function handleResponse(response) {
    if (!response.ok) {
        return response.json().then(function (err) {
            throw new Error(err.message || 'Error al procesar la solicitud.');
        });
    }
    return response.json();
}

function handleError(error) {
    alert('Error: ' + error.message);
}

function confirmAppointment(publicId) {
    if (!confirm('¿Confirmar esta cita?')) return;
    fetch('/api/appointments/' + publicId + '/confirm', { method: 'PATCH' })
        .then(handleResponse)
        .then(function () {
            alert('Cita confirmada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

function cancelAppointment(publicId) {
    if (!confirm('¿Cancelar esta cita?')) return;
    fetch('/api/appointments/' + publicId + '/cancel', { method: 'PATCH' })
        .then(handleResponse)
        .then(function () {
            alert('Cita cancelada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

function completeAppointment(publicId) {
    if (!confirm('¿Completar esta cita?')) return;
    fetch('/api/appointments/' + publicId + '/complete', { method: 'PATCH' })
        .then(handleResponse)
        .then(function () {
            alert('Cita completada exitosamente.');
            location.reload();
        })
        .catch(handleError);
}

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

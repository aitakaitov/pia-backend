// Prepare HTTP headers for REST calls (CSRF protection).
function getHeaders() {
	var headers = Array();
	headers[_csrfHeaderName] = _csrfToken;
	return headers;
}

// Updates the view after a successful REST call.
function ajaxSuccess(data) {
	$('#countView').html(data);
	$('#setForm\\:inputCount').val(data);
}

// Calls the click REST call - used as an event method for the CLICK button.
function clickButton(e) {
	if (e) {
		e.preventDefault();
	}
	var headers = getHeaders();
	var settings = {
		'headers': headers,
		'method': 'put',
		'success': ajaxSuccess
	};
	$.ajax('/api/click', settings);
}

// Calls the set REST call - used as an event method for the SET button.
function setButton(e) {
	if (e) {
		e.preventDefault();
	}
	var headers = getHeaders();
	var value = $('#setForm\\:inputCount').val();
	var settings = {
		'headers': headers,
		'method': 'put',
		'success': ajaxSuccess,
		'contentType': 'application/json',
		'dataType': 'json',
		'data': value
	};
	$.ajax('/api/set', settings);
}

function openUserEvent(e) {
	if (e.status == 'success') {
		$('#userDetailModal').modal({'show': true});
	}
}

function closeUserEvent(e) {
	if (e.status == 'success') {
		$('#userDetailModal').modal('hide');
	}
}

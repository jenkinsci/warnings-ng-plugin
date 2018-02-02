(function ($) {
    function format() {
        var warningMessage = $('#warning-message').text();
        var warningTooltip = $('#warning-toolTip').text();

        return '<div><strong>' + warningMessage + '</strong><br/>'
            + warningTooltip + '</div>';
    }

    $('#moduleName').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });
    $('#packageName').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });
    $('#fileName').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });
    $('#category').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });
    $('#authors').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });
    $('#type').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });
    $('#origin').DataTable({
        "order": [],
        "pagingType": "numbers",
        "columnDefs": [{
            "targets": 'no-sort',
            "orderable": false
        }]
    });

    // Add event listener for opening and closing details
    $('#warnings').on('click', 'td.details-control', function () {
        var tr = $(this).closest('tr');
        var row = table.row(tr);

        if (row.child.isShown()) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        } else {
            // Open this row
            row.child(format(tr.data('child-value'))).show();
            tr.addClass('shown');
        }
    });
})(jQuery);
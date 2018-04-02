var jQuery = require('jquery');
var dataTables = require('datatables.net')();

(function ($) {
    $('table.display').DataTable({
        pagingType: 'numbers',  // Page number button only
        columnDefs: [{
            targets: 'no-sort', // Columns with class 'no-sort' are not orderable
            orderable: false
        }]
    });

    var issuesTable = $('#warnings');
    issuesTable.DataTable({
        pagingType: 'numbers', // Page number button only
        order: [[ 1, 'asc' ]],
        columnDefs: [{
            targets: 0 ,       // First column contains details
            orderable: false
        }]
    });

    // Add event listener for opening and closing details
    issuesTable.on('click', 'div.details-control', function () {
        var tr = $(this).parents('tr');
        var table = $('#warnings').DataTable();
        var row = table.row(tr);

        if (row.child.isShown()) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        }
        else {
            // Open this row
            row.child($(this).data('description')).show();
            tr.addClass('shown');
        }
    });

    // Populate issues table with Ajax call
    view.getTableModel(function (t) {
        (function ($) {
            var table = $('#warnings').DataTable();
            table.rows.add(t.responseObject().data).draw()
        })(jQuery);
    });
})(jQuery);

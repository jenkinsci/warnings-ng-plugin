/**
 * @file Initialising DataTables
 * @author Cornelia Christof <cchristo@hm.edu>
 */
(function ($) {
    /**
     *
     * @summary Formatting code, that is shown in warnings table, by clicking the details plus icon
     * @param message
     * @param tooltip
     * @link https://datatables.net/examples/api/row_details.html
     * @returns {string}
     */
    function format(message, tooltip) {
        return '<div><strong>' + message + '</strong><br/>' + tooltip + '</div>';
    }

    /**
     *
     * @summary Initialise DataTable functions on all tables with the class display
     * @link https://datatables.net/
     */
    $('table.display').DataTable({
        'pagingType': 'numbers', // Page number button only
        'columnDefs': [{
            'targets': 'no-sort', // Columns with class 'no-sort' are not orderable
            'orderable': false
        }]
    });
})(jQuery);
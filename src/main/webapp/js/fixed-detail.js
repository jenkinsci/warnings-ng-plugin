/* global jQuery */
(function ($) {
    /**
     * Create a data table instance for the fixed warnings.
     */
    $('#fixed').DataTable({
        pagingType: 'numbers', // Page number button only
        order: [[0, 'asc'], [1, 'asc']] // Sort by file name and line number
    });
})(jQuery);

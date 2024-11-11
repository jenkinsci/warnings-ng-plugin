/* global jQuery3 */
jQuery3('#fixed').DataTable({
  pagingType: 'numbers', // Page number button only
  order: [[0, 'asc'], [1, 'asc']] // Sort by file name and line number
});

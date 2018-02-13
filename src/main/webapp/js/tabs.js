/**
 * @file select tab and save tab
 * @author Cornelia Christof <cchristo@hm.edu>
 */
(function ($) {
    /**
     *
     * @summary activate first tab, if none is saved yet
     * @link https://getbootstrap.com/docs/4.0/components/navs/
     */
    $('#tab-details').find('li:first-child a').tab('show');

    /**
     *
     * @summary Save element href attribute value locally to the users browser (HTML5 localStorage object)
     */
    $('a[data-toggle="tab"]').on('show.bs.tab', function (e) {
        localStorage.setItem('activeTab', $(e.target).attr('href'));
    });

    /**
     * @summary Activate saved Tab if attribute is set
     * @type {string | null}
     */
    var activeTab = localStorage.getItem('activeTab');
    if (activeTab) {
        $('#tab-details').find('a[href="' + activeTab + '"]').tab('show');
    }
})(jQuery);
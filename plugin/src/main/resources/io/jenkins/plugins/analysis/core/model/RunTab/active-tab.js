/*
 * Workaround for the new build page: Jenkins core marks a run tab (and a side panel task) as selected only if the
 * current URL ends with the URL of the tab (see Functions.hyperlinkMatchesCurrentPage). The pages of the Warnings tab
 * are subpages (e.g., .../warnings/overview or .../warnings/checkstyle), so the "Warnings" tab would not be shown as
 * selected. This script fixes the selection on the client side. It can be removed once Jenkins core matches
 * subpages as well.
 */
(function () {
    const TAB_URL = '/warnings'; // see RunTab.URL
    const TERTIARY = 'jenkins-button--tertiary';
    const ACTIVE_TASK = 'task-link--active';

    function selectWarningsTab() {
        const tabs = document.querySelectorAll('.app-build-tabs > a');
        const warningsTab = Array.from(tabs).find(tab => tab.getAttribute('href').endsWith(TAB_URL));
        if (!warningsTab || !isBelow(new URL(warningsTab.href, window.location.href).pathname)) {
            return;
        }
        tabs.forEach(tab => tab.classList.toggle(TERTIARY, tab !== warningsTab));
    }

    /**
     * Determines whether the current page is the specified page or one of its subpages.
     *
     * @param {String} path the path of the page
     * @return {Boolean} true if the current page is the page or one of its subpages
     */
    function isBelow(path) {
        const current = window.location.pathname.replace(/\/$/, '');
        const parent = path.replace(/\/$/, '');
        return current === parent || current.startsWith(parent + '/');
    }

    function selectSidePanelTask() {
        const sidePanel = document.querySelector('.wng-sidebar[data-active-task]');
        if (!sidePanel) {
            return; // the current page is selected by Jenkins core already
        }
        const suffix = '/' + sidePanel.getAttribute('data-active-task');
        sidePanel.querySelectorAll('a.task-link').forEach(function (link) {
            link.classList.toggle(ACTIVE_TASK, link.getAttribute('href').endsWith(suffix));
        });
    }

    function select() {
        selectWarningsTab();
        selectSidePanelTask();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', select);
    }
    else {
        select();
    }
})();

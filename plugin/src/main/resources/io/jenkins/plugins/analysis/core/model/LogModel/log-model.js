/* global jQuery3 */

(function ($) {
    const TOOL_SELECTOR = '#wng-log-tool';
    const TOOL_ICON_SELECTOR = '.wng-app-bar__tool-icon';
    const PANEL_SELECTOR = '.wng-log__panel';
    const ERRORS_SELECTOR = '.wng-log__errors';
    const INFO_SELECTOR = '.wng-log__info';
    const FOOTER_SELECTOR = '.page-footer';
    const HIDDEN_CLASS = 'jenkins-hidden';

    const MIN_LOG_HEIGHT = 150;
    const BOTTOM_MARGIN = 40;

    // The log page shows all tools of a run, so the setting is not stored per report ID.
    const STORAGE_KEY = 'wng-log-tool';

    // Name of the setting in the fragment of the URL, e.g., 'log#tool=pmd'
    const TOOL_PARAMETER = 'tool';

    let resizeTimeout;

    function readStorage() {
        try {
            return localStorage.getItem(STORAGE_KEY);
        }
        catch (e) {
            return null;
        }
    }

    function writeStorage(value) {
        try {
            localStorage.setItem(STORAGE_KEY, value);
        }
        catch (e) {
            // ignore: the setting is not persisted if the storage is not available
        }
    }

    function hasTool(value) {
        return value !== null && $(TOOL_SELECTOR + ' option').filter(function () {
            return this.value === value;
        }).length > 0;
    }

    /**
     * Selects the tool that is given in the fragment of the URL. If there is no such tool, the tool that has been
     * stored the last time is selected. Otherwise, the preselected tool of the page remains (the first tool with
     * errors, see LogModel#getDefaultToolId). A selection of the URL is not stored, so it is valid for the opened
     * link only.
     */
    function restoreSelection() {
        const requested = new URLSearchParams(window.location.hash.replace(/^#/, '')).get(TOOL_PARAMETER);
        if (hasTool(requested)) {
            $(TOOL_SELECTOR).val(requested);
            return;
        }
        const stored = readStorage();
        if (hasTool(stored)) {
            $(TOOL_SELECTOR).val(stored);
        }
    }

    function getFooterHeight() {
        const footer = document.querySelector(FOOTER_SELECTOR);
        if (!footer) {
            return 0;
        }
        return footer.getBoundingClientRect().height;
    }

    /**
     * Shows the icon and the messages of the selected tool and hides the icons and messages of all other tools.
     */
    function showSelectedTool() {
        const tool = $(TOOL_SELECTOR).val();
        $(TOOL_ICON_SELECTOR + ', ' + PANEL_SELECTOR).each(function () {
            $(this).toggleClass(HIDDEN_CLASS, this.getAttribute('data-tool') !== tool);
        });
    }

    /**
     * Sizes the cards with the error and information messages of the selected tool so that together they fill the
     * browser viewport (while keeping the footer visible), the same way the trend page sizes its chart. If only the
     * information messages are shown (no errors have been recorded), they get the full available height. If both
     * cards are shown, each one gets half of it.
     */
    function fitLogToViewport() {
        const panel = document.querySelector(PANEL_SELECTOR + ':not(.' + HIDDEN_CLASS + ')');
        if (!panel) {
            return;
        }
        const errors = panel.querySelector(ERRORS_SELECTOR);
        const info = panel.querySelector(INFO_SELECTOR);

        const bottomBoundary = window.innerHeight - getFooterHeight() - BOTTOM_MARGIN;

        if (!errors) {
            const top = info.getBoundingClientRect().top;
            info.style.height = Math.max(bottomBoundary - top, MIN_LOG_HEIGHT) + 'px';
            return;
        }

        // Reset both cards first so the chrome (title bar) and the gap between them can be measured
        // independently of any height that was computed on a previous call.
        errors.style.height = 'auto';
        info.style.height = 'auto';

        const errorsTop = errors.getBoundingClientRect().top;
        const chromeAndGap = info.getBoundingClientRect().top - errors.getBoundingClientRect().bottom;
        const half = Math.max((bottomBoundary - errorsTop - chromeAndGap) / 2, MIN_LOG_HEIGHT);

        errors.style.height = half + 'px';
        info.style.height = half + 'px';
    }

    $(document).ready(function () {
        restoreSelection();
        showSelectedTool();
        fitLogToViewport();

        $(TOOL_SELECTOR).on('change', function () {
            writeStorage($(this).val());
            showSelectedTool();
            fitLogToViewport();
        });

        $(window).on('resize', function () {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(fitLogToViewport, 150);
        });
    });
})(jQuery3);

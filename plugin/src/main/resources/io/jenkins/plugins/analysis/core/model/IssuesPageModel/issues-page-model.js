/* global jQuery3 */

(function ($) {
    const TOOL_SELECTOR = '#wng-issues-tool';
    const TABLE_SELECTOR = '#wng-issues-table';
    const BASELINE_SELECTOR = '#wng-issues-baseline';
    const TOOL_ICON_SELECTOR = '.wng-app-bar__tool-icon';
    const PANEL_SELECTOR = '.wng-issues__panel';
    const PANEL_ID_PREFIX = 'wng-issues-panel-';
    const HIDDEN_CLASS = 'jenkins-hidden';

    // Hidden columns that are used to filter the tables, see ToolsTableModel
    const TOOL_COLUMN = 'tool';
    const BASELINE_COLUMN = 'baseline';
    const ALL_BASELINES = 'all';
    const DEFAULT_TABLE = 'issues';

    // The rows are rendered by the table models of the classic details view. These models link the properties of an
    // issue (e.g., 'category.123/') and the severity (e.g., 'HIGH') to filtered views that are not part of the
    // Warnings tab, so these links are replaced by plain text. Links to the affected files are not changed.
    const PROPERTY_LINK = /^(?:[A-Za-z]+\.-?\d+\/|[A-Z_]+)$/;

    // The issues page shows all tools of a run, so the settings are not stored per report ID.
    const STORAGE_PREFIX = 'wng-issues-';
    const TOOL_KEY = 'tool';
    const TABLE_KEY = 'table';
    const BASELINE_KEY = 'baseline';

    // Names of the settings in the fragment of the URL, see IssuesPageModel#getUrlOf
    const TOOL_PARAMETER = 'tool';
    const TABLE_PARAMETER = 'table';
    const BASELINE_PARAMETER = 'show';

    function readStorage(key) {
        try {
            return localStorage.getItem(STORAGE_PREFIX + key);
        }
        catch (e) {
            return null;
        }
    }

    function writeStorage(key, value) {
        try {
            localStorage.setItem(STORAGE_PREFIX + key, value);
        }
        catch (e) {
            // ignore: settings are not persisted if the storage is not available
        }
    }

    function hasOption(selector, value) {
        return value !== null && $(selector + ' option').filter(function () {
            return this.value === value;
        }).length > 0;
    }

    /**
     * Returns the settings that are given in the fragment of the URL, e.g., '#tool=pmd&show=new'. Such links are
     * used by the overview page to open the issues of a specific tool.
     *
     * @return {URLSearchParams} the settings of the URL
     */
    function readUrlSettings() {
        return new URLSearchParams(window.location.hash.replace(/^#/, ''));
    }

    /**
     * Selects the option that is given in the URL. If there is no such option, the option that has been stored
     * the last time is selected. A selection of the URL is not stored, so it is valid for the opened link only.
     *
     * @param {String} selector the selector of the select box
     * @param {String} key the key of the setting in the local storage
     * @param {String} parameter the name of the setting in the URL
     * @param {URLSearchParams} urlSettings the settings of the URL
     */
    function restoreSelection(selector, key, parameter, urlSettings) {
        const requested = urlSettings.get(parameter);
        if (hasOption(selector, requested)) {
            $(selector).val(requested);
            return;
        }
        const stored = readStorage(key);
        if (hasOption(selector, stored)) {
            $(selector).val(stored);
        }
    }

    function getSelectedTable() {
        // the select box is missing if there is only the issues table
        return $(TABLE_SELECTOR).val() || DEFAULT_TABLE;
    }

    /**
     * Shows the table of the selected type and hides all other tables.
     */
    function showSelectedTable() {
        const visiblePanel = PANEL_ID_PREFIX + getSelectedTable();
        $(PANEL_SELECTOR).each(function () {
            $(this).toggleClass(HIDDEN_CLASS, this.id !== visiblePanel);
        });
        $(PANEL_SELECTOR + ':not(.' + HIDDEN_CLASS + ') table.data-table').each(function () {
            $(this).DataTable().columns.adjust();
        });
    }

    function escapeRegex(text) {
        return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    /**
     * Filters a hidden column of a table so that only rows with exactly the specified value remain. The filter is
     * always set explicitly since DataTables persists the search state of a column (stateSave).
     *
     * @param {Object} dataTable the DataTables instance
     * @param {String} property the data property of the column to filter
     * @param {String} value the value to show, or an empty string to show all rows
     */
    function filterColumn(dataTable, property, value) {
        const index = dataTable.columns().dataSrc().toArray().indexOf(property);
        if (index >= 0) {
            dataTable.column(index).search(value === '' ? '' : '^' + escapeRegex(value) + '$', true, false);
        }
    }

    /**
     * Shows the icon of the selected tool next to the select box and hides the icons of all other tools.
     */
    function showToolIcon() {
        const tool = $(TOOL_SELECTOR).val();
        $(TOOL_ICON_SELECTOR).each(function () {
            $(this).toggleClass(HIDDEN_CLASS, this.getAttribute('data-tool') !== tool);
        });
    }

    /**
     * Applies the selected tool and baseline to all tables.
     */
    function applyFilters() {
        const toolSelect = $(TOOL_SELECTOR);
        const tool = toolSelect.val() === toolSelect.attr('data-all-tools') ? '' : toolSelect.val();
        const baseline = $(BASELINE_SELECTOR).val() === ALL_BASELINES ? '' : $(BASELINE_SELECTOR).val();

        $(PANEL_SELECTOR + ' table.data-table').each(function () {
            const dataTable = $(this).DataTable();
            filterColumn(dataTable, TOOL_COLUMN, tool);
            filterColumn(dataTable, BASELINE_COLUMN, baseline);
            dataTable.draw();
        });
    }

    /**
     * Replaces the links to the filtered views of the classic details view with plain text.
     *
     * @param {Object} table the table element
     */
    function removePropertyLinks(table) {
        $(table).find('tbody a').each(function () {
            if (PROPERTY_LINK.test(this.getAttribute('href') || '')) {
                $(this).replaceWith($(this).contents());
            }
        });
    }

    function bindSelection(selector, key, onChange) {
        $(selector).on('change', function () {
            writeStorage(key, $(this).val());
            onChange();
        });
    }

    // The DataTables instances are created by the data-tables-api plugin when the document is ready. That script is
    // included by the table tag and hence registered before this script, so the instances are available here.
    $(document).ready(function () {
        $(PANEL_SELECTOR + ' table.data-table').on('draw.dt', function () {
            removePropertyLinks(this);
        });

        const urlSettings = readUrlSettings();
        restoreSelection(TOOL_SELECTOR, TOOL_KEY, TOOL_PARAMETER, urlSettings);
        restoreSelection(TABLE_SELECTOR, TABLE_KEY, TABLE_PARAMETER, urlSettings);
        restoreSelection(BASELINE_SELECTOR, BASELINE_KEY, BASELINE_PARAMETER, urlSettings);

        showToolIcon();
        showSelectedTable();
        applyFilters();

        bindSelection(TOOL_SELECTOR, TOOL_KEY, function () {
            showToolIcon();
            applyFilters();
        });
        bindSelection(BASELINE_SELECTOR, BASELINE_KEY, applyFilters);
        bindSelection(TABLE_SELECTOR, TABLE_KEY, showSelectedTable);
    });
})(jQuery3);

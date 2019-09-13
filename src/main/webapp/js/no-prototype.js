/* global jQuery, Prototype */
(function ($) {
    /**
     * Solves Bootstrap and Prototype.js conflict.
     *
     * @link http://jsfiddle.net/dgervalle/hhBc6/
     * @link http://www.softec.lu/site/DevelopersCorner/BootstrapPrototypeConflict
     */
    jQuery.noConflict();
    if (Prototype.BrowserFeatures.ElementExtensions) {
        const disablePrototypeJS = function (method, pluginsToDisable) {
            const handler = function (event) {
                event.target[method] = undefined;
                setTimeout(function () {
                    delete event.target[method];
                }, 0);
            };
            pluginsToDisable.each(function (plugin) {
                jQuery(window).on(method + '.bs.' + plugin, handler);
            });
        };
        const pluginsToDisable = ['collapse', 'dropdown', 'modal', 'tooltip', 'popover', 'tab'];
        disablePrototypeJS('show', pluginsToDisable);
        disablePrototypeJS('hide', pluginsToDisable);
    }
})(jQuery);

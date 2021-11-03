/* global hoverNotification */
const ResetQualityGateButtonHandler = function () {
    this.bindResetButton = function (toolId, proxy) {
        const button = document.getElementById(toolId + '-reset-reference');

        if (button) {
            button.addEventListener('click', async _ => {
                hoverNotification('Resetting Quality Gate for ' + toolId, button.parentNode);
                proxy.resetReference(function () {
                    button.remove();
                });
            });
        }
    };
};

window.addEventListener("DOMContentLoaded", () => {
    const dataHolders = document.querySelectorAll(".bind-reset-button-data-holder");

    dataHolders.forEach(dataHolder => {
        const actionId = dataHolder.getAttribute("data-action-id");
        const proxyName = dataHolder.getAttribute("data-proxy-name");

        const handler = new ResetQualityGateButtonHandler();
        handler.bindResetButton(actionId, window[proxyName]);
    });
});

window.addEventListener("DOMContentLoaded", () => {
    const dataHolders = document.querySelectorAll(".issues-chart-portlet-data-holder");

    dataHolders.forEach(dataHolder => {
        const id = dataHolder.getAttribute("data-id");
        const proxyName = dataHolder.getAttribute("data-proxy-name");

        echartsJenkinsApi.renderTrendChart(id + '-issues-chart', 'false', window[proxyName]);
    });
});

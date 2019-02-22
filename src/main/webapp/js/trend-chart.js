/**
 * Renders a trend chart in the specified div using ECharts.
 *
 * @param {String} chartDivId - the ID of the div where the chart should be shown in
 * @param {JSON} chartModel - the line chart model
 * @param {boolean} showZoom - determines whether the data zoom should be shown
 * @param {String} urlName - the URL to the results, if empty or unset then clicking on the chart is disabled
 */
function renderTrendChart(chartDivId, chartModel, showZoom, urlName) {
    var chartPlaceHolder = document.getElementById(chartDivId);
    var currentSelection; // the tooltip formatter will change this value while hoovering

    if (urlName) {
        chartPlaceHolder.onclick = function () {
            if (urlName && currentSelection) {
                window.location.assign(currentSelection.substring(1) + '/' + urlName);
            }
        };
    }

    var dataZoom;
    var gridTop;
    var gridBottom;

    if (showZoom) {
        dataZoom = [{
            id: 'dataZoomX',
            type: 'slider',
            xAxisIndex: [0],
            filterMode: 'filter',
            top: 'bottom',
            startValue: Math.max(0, chartModel.XAxisLabels.length - 50),
            handleIcon: 'M 256 8 C 119 8 8 119 8 256 C 8 393 119 504 256 504 C 393 504 504 393 504 256 C 504 119 393 8 256 8 z M 210.17773 95.966797 L 230.17773 95.966797 C 235.67773 95.966797 240.17773 100.4668 240.17773 105.9668 L 240.17773 405.9668 C 240.17773 411.4668 235.67773 415.9668 230.17773 415.9668 L 210.17773 415.9668 C 204.67773 415.9668 200.17773 411.4668 200.17773 405.9668 L 200.17773 105.9668 C 200.17773 100.4668 204.67773 95.966797 210.17773 95.966797 z M 290.17773 95.966797 L 310.17773 95.966797 C 315.67773 95.966797 320.17773 100.4668 320.17773 105.9668 L 320.17773 405.9668 C 320.17773 411.4668 315.67773 415.9668 310.17773 415.9668 L 290.17773 415.9668 C 284.67773 415.9668 280.17773 411.4668 280.17773 405.9668 L 280.17773 105.9668 C 280.17773 100.4668 284.67773 95.966797 290.17773 95.966797 z',
            handleSize: '70%',
            handleStyle: {
                color: '#b4b4b4'
            }
        }];
        gridTop = '15%';
        gridBottom = '15%';
    } else {
        dataZoom = null;
        gridTop = '30';
        gridBottom = '10';
    }

    var chart = echarts.init(chartPlaceHolder);
    var options = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            },
            formatter: function (params, ticket, callback) {
                if (params.componentType === 'legend') {
                    currentSelection = null;
                    return params.name;
                }
                currentSelection = params[0].name;
                var text = 'Build ' + params[0].name;
                for (var i = 0, l = params.length; i < l; i++) {
                    text += '<br/>' + params[i].marker + params[i].seriesName + ' : ' + params[i].value;
                }
                text += '<br />';
                return '<div align="left">' + text + '</div>';
            }
        },
        dataZoom: dataZoom,
        legend: {
            orient: 'horizontal',
            type: 'scroll',
            x: 'center',
            y: 'top'
        },
        grid: {
            left: '20',
            right: '10',
            bottom: gridBottom,
            top: gridTop,
            containLabel: true
        },
        xAxis: [{
            type: 'category',
            boundaryGap: false,
            data: chartModel.XAxisLabels
        }
        ],
        yAxis: [{
            type: 'value'
        }
        ],
        series: chartModel.series
    };
    chart.setOption(options);
    chart.on('legendselectchanged', function (params) {
            currentSelection = null; // clear selection to avoid navigating to the selected build
        }
    );
    chart.resize();
    window.onresize = function () {
        chart.resize();
    };
}




/**
 * Renders a trend chart in the specified div using ECharts.
 *
 * @param {String} chartDivId - the ID of the div where the chart should be shown in
 * @param {JSON} chartModel - the line chart model
 * @param {String} urlName - the URL to the results, if empty or unset then clicking on the chart is disabled
 */
function renderTrendChart(chartDivId, chartModel, urlName) {
    var chartPlaceHolder = document.getElementById(chartDivId);
    var currentSelection; // the tooltip formatter will change this value while hoovering

    if (urlName) {
        chartPlaceHolder.onclick = function () {
            if (urlName && currentSelection) {
                window.location.assign(currentSelection.substring(1) + '/' + urlName);
            }
        };
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
        legend: {
            orient: 'horizontal',
            type: 'scroll',
            x: 'center',
            y: 'top'
        },
        grid: {
            left: '20',
            right: '10',
            bottom: '10',
            top: '30',
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




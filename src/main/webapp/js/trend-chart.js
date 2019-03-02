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
            handleIcon: 'M 239.33984 -0.052734375 C 230.53984 -0.052734375 223.33984 7.1472658 223.33984 15.947266 L 223.33984 226 L 134.05859 226 L 134.05859 179.94141 C 134.05859 158.55941 108.20789 147.8517 93.087891 162.9707 L 7.0292969 249.0293 C -2.3437031 258.4023 -2.3437031 273.5977 7.0292969 282.9707 L 93.087891 369.0293 C 108.20689 384.1483 134.05859 373.44059 134.05859 352.05859 L 134.05859 306 L 223.33984 306 L 223.33984 495.94727 C 223.33984 504.74727 230.53984 511.94727 239.33984 511.94727 L 271.33984 511.94727 C 280.13984 511.94727 287.33984 504.74727 287.33984 495.94727 L 287.33984 306 L 377.94141 306 L 377.94141 352.05859 C 377.94141 373.44059 403.79211 384.1483 418.91211 369.0293 L 504.9707 282.9707 C 514.3437 273.5977 514.3437 258.4023 504.9707 249.0293 L 418.91211 162.9707 C 403.79311 147.8507 377.94141 158.55941 377.94141 179.94141 L 377.94141 226 L 287.33984 226 L 287.33984 15.947266 C 287.33984 7.1472658 280.13984 -0.052734375 271.33984 -0.052734375 L 239.33984 -0.052734375 z',
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

    return chart;
}




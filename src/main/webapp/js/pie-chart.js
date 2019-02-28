/**
 * Renders a trend chart in the specified div using ECharts.
 *
 * @param {String} chartDivId - the ID of the div where the chart should be shown in
 * @param {JSON} pieModel - the line chart model
 */
function renderPieChart(chartDivId, pieModel) {
    var chartPlaceHolder = document.getElementById(chartDivId);

    var chart = echarts.init(chartPlaceHolder);
    var options = {
        title: {
            text: pieModel.name,
            textStyle: {
                fontWeight: 'normal',
                fontSize: '16'
            },
            left: 'center',
        },
        tooltip: {
            trigger: 'item',
            formatter: "{b}: {c} ({d}%)"
        },
        legend: {
            orient: 'horizontal',
            x: 'center',
            y: 'bottom',
            type: 'scroll'
        },
        series: [{
            type: 'pie',
            radius: ['30%', '70%'],
            avoidLabelOverlap: false,
            color: pieModel.colors,
            label: {
                normal: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    show: false
                }
            },
            labelLine: {
                normal: {
                    show: true
                }
            },
            data: pieModel.data
        }
        ]
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




option = {
    tooltip: {
        trigger: 'axis',
        axisPointer: {
            type: 'cross',
            label: {
                backgroundColor: '#6a7985'
            }
        }
    },
    grid: {
        left: 0,
        right: 0,
        bottom: 0,
        top: 0,
        containLabel: false
    },
    xAxis: [
        {
            type: 'category',
            boundaryGap: false,
            data: ['#1', '#2', '#3', '#4', '#5', '#6', '#7']
        }
    ],
    yAxis: [
        {
            type: 'value'
        }
    ],
    series: [
        {
            name: 'High',
            type: 'line',
            stack: 'severity',
            areaStyle: {normal: {}},
            data: [120, 132, 101, 134, 90, 230, 210]
        },
        {
            name: 'Normal',
            type: 'line',
            stack: 'severity',
            areaStyle: {normal: {}},
            data: [220, 182, 191, 234, 290, 330, 310]
        },
        {
            name: 'Low',
            type: 'line',
            stack: 'severity',
            areaStyle: {normal: {}},
            data: [150, 232, 201, 154, 190, 330, 410]
        }
    ]
};

/**
 * Creates a doughnut chart that shows the number of issues per severity.
 * Requires that a DOM <div> element exists with the ID '#severities-chart'.
 */
view.getBuildTrend(function (pieModel) {
    (function ($) {
        var severitiesChart = echarts.init(document.getElementById('severities-chart'));
        var severitiesOptions = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                }
            },
            legend: {
                show: false
            },
            grid: {
                left: '1%',
                right: '5%',
                bottom: '1%',
                top: '5%',
                containLabel: true
            },
            xAxis: [
                {
                    type: 'category',
                    boundaryGap: false,
                    data: pieModel.responseJSON.XAxisLabels
                }
            ],
            yAxis: [
                {
                    type: 'value'
                }
            ],
            series: pieModel.responseJSON.series
        };
        severitiesChart.setOption(severitiesOptions);
        severitiesChart.resize();
        $(window).on('resize', function () {
            severitiesChart.resize();
        });
    })(jQuery);
});

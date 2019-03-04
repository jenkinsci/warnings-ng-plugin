(function ($) {
    $.fn.extend({
        /**
         * Renders a trend chart in the specified div using ECharts.
         *
         * @param {JSON} chartModel - the line chart model
         */
        renderTrendChart: function (chartModel) {
            var chart = echarts.init($(this)[0]);
            var options = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross',
                        label: {
                            backgroundColor: '#6a7985'
                        }
                    },
                },
                dataZoom: [{
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
                }],
                legend: {
                    orient: 'horizontal',
                    type: 'scroll',
                    x: 'center',
                    y: 'top'
                },
                grid: {
                    left: '20',
                    right: '10',
                    bottom: '15%',
                    top: '15%',
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
            chart.resize();

            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data("chart", chart);
        }
    });
})(jQuery);


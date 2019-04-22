(function ($) {
    $.fn.extend({
        /**
         * Renders a trend chart in the a div using ECharts.
         *
         * @param {String} model - the chart model
         * @param {boolean} isTitleVisible - determines whether a title should be shown
         */
        renderPieChart: function (model, isTitleVisible) {
            var pieModel = JSON.parse(model);
            var chart = echarts.init($(this)[0]);
            var options = {
                title: getTitle(isTitleVisible),
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
            chart.resize();
            chart.on('click', function (params) {
                window.location.assign(params.name);
            });
            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data("chart", chart);

            /**
             * Returns the title properties of the chart.
             *
             * @param {boolean} isTitleVisible - determines whether a title should be shown
             */
            function getTitle(isTitleVisible) {
                if (isTitleVisible) {
                    return {
                        text: pieModel.name,
                        textStyle: {
                            fontWeight: 'normal',
                            fontSize: '16'
                        },
                        left: 'center'
                    }
                } else {
                    return null;
                }
            }
        }
    });
})(jQuery);




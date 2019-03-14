(function ($) {
    $.fn.extend({
        /**
         * Renders a trend chart in the specified div using ECharts.
         *
         * @param {JSON} chartModel - the line chart model
         * @param {Function} redrawCallback - callback that will be invoked if the user toggles date or build domain
         */
        renderTrendChart: function (chartModel, redrawCallback) {
            var chart = echarts.init($(this)[0]);
            var options = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross',
                        label: {
                            backgroundColor: '#6a7985'
                        }
                    }
                },
                toolbox: {
                    itemSize: 16,
                    feature: {
                        myTool1: {
                            show: true,
                            title: 'Date',
                            icon: 'path://M148 288h-40c-6.6 0-12-5.4-12-12v-40c0-6.6 5.4-12 12-12h40c6.6 0 12 5.4 12 12v40c0 6.6-5.4 12-12 12zm108-12v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm96 0v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm-96 96v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm-96 0v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm192 0v-40c0-6.6-5.4-12-12-12h-40c-6.6 0-12 5.4-12 12v40c0 6.6 5.4 12 12 12h40c6.6 0 12-5.4 12-12zm96-260v352c0 26.5-21.5 48-48 48H48c-26.5 0-48-21.5-48-48V112c0-26.5 21.5-48 48-48h48V12c0-6.6 5.4-12 12-12h40c6.6 0 12 5.4 12 12v52h128V12c0-6.6 5.4-12 12-12h40c6.6 0 12 5.4 12 12v52h48c26.5 0 48 21.5 48 48zm-48 346V160H48v298c0 3.3 2.7 6 6 6h340c3.3 0 6-2.7 6-6z',
                            onclick: function () {
                                localStorage.setItem('#trendBuildAxis', 'date');
                                redrawCallback();
                            }
                        },
                        myTool2: {
                            show: true,
                            title: 'Build#',
                            icon: 'ipath://M440.667 182.109l7.143-40c1.313-7.355-4.342-14.109-11.813-14.109h-74.81l14.623-81.891C377.123 38.754 371.468 32 363.997 32h-40.632a12 12 0 0 0-11.813 9.891L296.175 128H197.54l14.623-81.891C213.477 38.754 207.822 32 200.35 32h-40.632a12 12 0 0 0-11.813 9.891L132.528 128H53.432a12 12 0 0 0-11.813 9.891l-7.143 40C33.163 185.246 38.818 192 46.289 192h74.81L98.242 320H19.146a12 12 0 0 0-11.813 9.891l-7.143 40C-1.123 377.246 4.532 384 12.003 384h74.81L72.19 465.891C70.877 473.246 76.532 480 84.003 480h40.632a12 12 0 0 0 11.813-9.891L151.826 384h98.634l-14.623 81.891C234.523 473.246 240.178 480 247.65 480h40.632a12 12 0 0 0 11.813-9.891L315.472 384h79.096a12 12 0 0 0 11.813-9.891l7.143-40c1.313-7.355-4.342-14.109-11.813-14.109h-74.81l22.857-128h79.096a12 12 0 0 0 11.813-9.891zM261.889 320h-98.634l22.857-128h98.634l-22.857 128z',
                            onclick: function () {
                                localStorage.setItem('#trendBuildAxis', 'build');
                                redrawCallback();
                            }
                        }
                    }
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




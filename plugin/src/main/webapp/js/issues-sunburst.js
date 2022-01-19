/* global jQuery3, echarts, portlet */
(function ($) {
    portlet.getId(function (id) {
        const chartDom = document.getElementById(id.responseObject());

        if (chartDom == null) {
            // portlet has no issues, skip generating the chart.
            return;
        }

        const portletChart = echarts.init(chartDom);
        const sunburstData = JSON.parse(chartDom.getAttribute('data'));

        const data = [{
            name: 'All\nIssues',
            itemStyle: {
                color: '#70a1d7'
            },
            children: [{
                name: 'New',
                itemStyle: {
                    color: '#EF9A9A'
                },
                value: sunburstData.new.total,
                children: [{
                    name: 'Low-Prio',
                    value: sunburstData.new.low,
                    itemStyle: {
                        color: '#e9e2d0'
                    }
                },
                {
                    name: 'Normal-Prio',
                    value: sunburstData.new.normal,
                    itemStyle: {
                        color: '#ffcda3'
                    }
                },
                {
                    name: 'High-Prio',
                    value: sunburstData.new.high,
                    itemStyle: {
                        color: '#ee9595'
                    }
                },
                {
                    name: 'Error',
                    value: sunburstData.new.error,
                    itemStyle: {
                        color: '#f05454'
                    }
                }]
            }, {
                name: 'Outstanding',
                value: sunburstData.outstanding,
                itemStyle: {
                    color: '#FFF59D'
                }
            }, {
                name: 'Fixed',
                value: sunburstData.fixed,
                itemStyle: {
                    color: '#A5D6A7'
                }
            }]
        }];

        const option = {
            series: {
                sort: null,
                type: 'sunburst',
                data: data,
                radius: ['15%', '80%'],
                itemStyle: {
                    color: '#ddd',
                    borderWidth: 2
                },
                label: {
                    rotate: 'horizontal',
                },
            },
            tooltip: {
                trigger: 'item',
                formatter: '{b}: {c}'
            }
        };

        option && portletChart.setOption(option);
    });
})(jQuery3);

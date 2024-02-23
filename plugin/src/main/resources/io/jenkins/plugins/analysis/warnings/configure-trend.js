(function () {
        function fillWarnings(trendConfiguration, jsonConfiguration) {
            const type = jsonConfiguration['chartType'];
            if (type) {
                trendConfiguration.find('#' + type + '-warnings').prop('checked', true);
            }
        }

        function saveWarnings(trendConfiguration) {
            return {
                'chartType': trendConfiguration.find('input[name=chartType-warnings]:checked').attr('id').replace('-warnings', '')
            };
        }

        echartsJenkinsApi.configureTrend('warnings', fillWarnings, saveWarnings)
    }
)();

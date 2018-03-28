var builder = require('@jenkins-cd/js-builder');

//
// Bundle the modules.
// See https://github.com/jenkinsci/js-builder
//
builder.bundle('src/main/js/priority-chart.js');
builder.bundle('src/main/js/trend-chart.js');
builder.bundle('src/main/js/select-url.js');



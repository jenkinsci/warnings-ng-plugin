package io.jenkins.plugins.analysis.warnings.groovy

return builder.setFileName(matcher.group(1))
        .setLineStart(Integer.parseInt(matcher.group(2)))
        .setCategory(matcher.group(3))
        .setMessage(matcher.group(4)).buildOptional()

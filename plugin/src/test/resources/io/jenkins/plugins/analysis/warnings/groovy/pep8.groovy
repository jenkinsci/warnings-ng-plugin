import edu.hm.hafner.analysis.Severity

String message = matcher.group(5)
String category = matcher.group(4)
Severity severity
if (category.contains("E")) {
    severity = Severity.WARNING_NORMAL
} else {
    severity = Severity.WARNING_LOW
}

return builder.setFileName(matcher.group(1))
        .setLineStart(Integer.parseInt(matcher.group(2)))
        .setColumnStart(Integer.parseInt(matcher.group(3)))
        .setCategory(category)
        .setMessage(message)
        .setSeverity(severity)
        .buildOptional()
import edu.hm.hafner.analysis.Severity

String type = matcher.group(1)
Severity severity
if ("warning".equalsIgnoreCase(type)) {
    severity = Severity.WARNING_NORMAL
}
else {
    severity = Severity.WARNING_HIGH
}
String fileName = matcher.group(2)
String eclipse34 = matcher.group(3)
String eclipse38 = matcher.group(4)

String lineNumber = eclipse34 != null && eclipse34.length() > 0? eclipse34 : eclipse38
String message = matcher.group(7)

int columnStart = matcher.group(5).length() + 1
int columnEnd = columnStart + matcher.group(6).length()

return builder
        .setFileName(fileName)
        .setLineStart(Integer.parseInt(lineNumber))
        .setColumnStart(columnStart)
        .setColumnEnd(columnEnd)
        .setMessage(message)
        .setSeverity(severity)
        .buildOptional()
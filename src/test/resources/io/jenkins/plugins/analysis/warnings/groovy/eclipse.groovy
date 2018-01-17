import edu.hm.hafner.analysis.Priority

String type = matcher.group(1)
Priority priority
if ("warning".equalsIgnoreCase(type)) {
    priority = Priority.NORMAL
}
else {
    priority = Priority.HIGH
}
String fileName = matcher.group(2)
String eclipse34 = matcher.group(3)
String eclipse38 = matcher.group(4)

String lineNumber = eclipse34 != null && eclipse34.length() > 0? eclipse34 : eclipse38
String message = matcher.group(6)

int columnStart = matcher.group(5).length() + 1
int columnEnd = columnStart + matcher.group(6).length()

return builder
        .setFileName(matcher.group(2))
        .setLineStart(Integer.parseInt(lineNumber))
        .setColumnStart(columnStart)
        .setColumnEnd(columnEnd)
        .setMessage(matcher.group(7))
        .setPriority(priority)
        .build()
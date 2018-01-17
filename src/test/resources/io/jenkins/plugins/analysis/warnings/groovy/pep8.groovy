import edu.hm.hafner.analysis.Priority

String message = matcher.group(5)
String category = matcher.group(4)
Priority priority
if (category.contains("E")) {
    priority = Priority.NORMAL
} else {
    priority = Priority.LOW
}

return builder.setFileName(matcher.group(1))
        .setLineStart(Integer.parseInt(matcher.group(2)))
        .setColumnStart(Integer.parseInt(matcher.group(3)))
        .setCategory(category)
        .setMessage(message)
        .setPriority(priority)
        .build()
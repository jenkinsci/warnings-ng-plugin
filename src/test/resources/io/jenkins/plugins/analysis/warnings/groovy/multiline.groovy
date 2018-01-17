import edu.hm.hafner.analysis.IssueBuilder
import edu.hm.hafner.analysis.Priority

String type = matcher.group(1)
Priority priority
if ("warning".equalsIgnoreCase(type)) {
    priority = Priority.NORMAL
} else {
    priority = Priority.HIGH
}
def builder = new IssueBuilder()
return builder.setFileName(matcher.group(2))
        .setMessage(matcher.group(4))
        .setLineStart(Integer.parseInt(matcher.group(3)))
        .build()

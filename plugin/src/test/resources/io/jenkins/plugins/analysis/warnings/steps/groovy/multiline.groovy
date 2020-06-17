import edu.hm.hafner.analysis.IssueBuilder
import edu.hm.hafner.analysis.Severity

String type = matcher.group(1)
Severity severity
if ("warning".equalsIgnoreCase(type)) {
    severity = Severity.WARNING_NORMAL
} 
else {
    severity = Severity.WARNING_HIGH
}
def builder = new IssueBuilder()
return builder.setFileName(matcher.group(2))
        .setMessage(matcher.group(4))
        .setLineStart(Integer.parseInt(matcher.group(3)))
        .buildOptional()

package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for messages from the NAG Fortran Compiler.
 *
 * @author Mat Cross.
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class NagFortranParser extends RegexpDocumentParser {
  private static final long serialVersionUID = 0L;
  private static final String
    NAGFOR_MSG_PATTERN = "^(Info|Warning|Questionable|Extension|Obsolescent|Deleted feature used|Error|Runtime Error|Fatal Error|Panic): (.+\\.[^,:\\n]+)(, line (\\d+))?: (.+($\\s+detected at .+)?)";

  /**
   * Creates a new instance of {@link NagFortranParser}.
   */
  public NagFortranParser() {
    super(Messages._Warnings_NagFortran_ParserName(),
          Messages._Warnings_NagFortran_LinkName(),
          Messages._Warnings_NagFortran_TrendName(),
          NAGFOR_MSG_PATTERN, true);
  }

  @Override
  protected Warning createWarning(final Matcher matcher) {
    String category = matcher.group(1);
    int lineNumber;
    Priority priority;

    if (StringUtils.isEmpty(matcher.group(4))) {
      lineNumber = 0;
    }
    else {
      lineNumber = Integer.parseInt(matcher.group(4));
    }

    if ("Error".equals(category) || "Runtime Error".equals(category)
            || "Fatal Error".equals(category) || "Panic".equals(category)) {
      priority = Priority.HIGH;
    }
    else if ("Info".equals(category)) {
      priority = Priority.LOW;
    }
    else {
      priority = Priority.NORMAL;
    }

    return createWarning(matcher.group(2), lineNumber, category,
                         matcher.group(5), priority);
  }

  @Override
  protected String getId() {
    return "NAG Fortran Compiler (nagfor)";
  }

}

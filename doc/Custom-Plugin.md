# Providing support for a custom static analysis tool
 
The most flexible way to add a new static analysis tool to the warnings plug-in is to provide a new custom plugin that 
contains a Java implementation of the tool. 

## Create a plugin

The plugin should depend on Jenkins plugin parent pom and on the warnings plugin.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.jenkins-ci.plugins</groupId>
    <artifactId>plugin</artifactId>
    <version>3.9</version>
    <relativePath />
  </parent>

  <groupId>[your.group.id]</groupId>
  <artifactId>[your.artifact.id]</artifactId>
  <packaging>hpi</packaging>
  <name>[Your Plugin Name]</name>
  <version>[your.version]</version>
   
  <dependencies>
    <dependency>
      <groupId>org.jvnet.hudson.plugins</groupId>
      <artifactId>warnings</artifactId>
      <version>5.0.0</version>
    </dependency>
  </dependencies>

</project>
```

## Create a parser class

Each custom tool requires a parser that will be instantiated to scan the console log (or a report file). The parser 
must derive from the abstract class `IssueParser`, or one of the child classes. 
The following base classes can be used as base class:

- `AbstractParser`: parses an input stream - you have under full control on how to access the input stream.
- `RegexpLineParser`: parses all lines of an input stream one by one with a regular expression.
- `FastRegexpLineParser`: same as `RegexpLineParser`, but with additional support to skip lines based on string comparisons.
- `RegexpDocumentParser`: parses the whole input stream by reading it into a string and parsing the whole string 
with a regular expression. This type of parser is quite slow and should only be used if there is no other way.
  
Please have a look at one of the 
[existing parsers](https://github.com/jenkinsci/analysis-model/tree/master/src/main/java/edu/hm/hafner/analysis/parser)
in order to see how to use these base classe.

## Register the tool in the warnings plugin

In order to get picked up by the warnings plugin your parser must be registered as an extension.
This is achieved by extending from the base class `StaticAnalysisTool` and registering it using the annotation 
`@Extension` at the associated `Descriptor` class. Typically you do this in Jenkins by adding the descriptor as
static nested class of the `StaticAnalysisTool`. Here is an example that can be used as a starting point. 

```java
package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.AjcParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for your tool.
 */
public class YourTool extends StaticAnalysisTool {
    private static final long serialVersionUID = 1L;
    static final String ID = "your-id";

    /** Creates a new instance of {@link YourTool}. */
    @DataBoundConstructor
    public YourTool() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public YourParserClass createParser() {
        return new YourParserClass();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Your parser name";
        }
    }
}
``` 

## Packaging the plugin

You can create a HPI of your plugin by calling `mvn clean install`. For more details on Jenkins' plugin development
please see [wiki](https://wiki.jenkins.io/display/JENKINS/Extend+Jenkins) or [homepage](https://jenkins.io/doc/developer/).  

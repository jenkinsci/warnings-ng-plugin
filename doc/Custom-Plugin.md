# Providing support for a custom static analysis tool
 
The most flexible way to add a new static analysis tool to the Warnings Next Generation plugin is to provide a new custom plugin that 
contains a Java implementation of the tool. 

## Create a plugin

The plugin should depend on Jenkins plugin parent pom and on the Warnings Next Generation plugin.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.jenkins-ci.plugins</groupId>
    <artifactId>plugin</artifactId>
    <version>3.32</version>
    <relativePath />
  </parent>

  <groupId>[your.group.id]</groupId>
  <artifactId>[your.artifact.id]</artifactId>
  <packaging>hpi</packaging>
  <name>[Your Plugin Name]</name>
  <version>[your.version]</version>
   
  <dependencies>
    <dependency>
      <groupId>io.jenkins.plugins</groupId>
      <artifactId>warnings-ng</artifactId>
      <version>[warnings-ng.version]</version>
    </dependency>
  </dependencies>

</project>
```

## Create a parser class

If your tool must parse a report file in order to produce the issues, you need to write a corresponding parser. 
Otherwise you can continue with section [registering your tool](#register-the-tool).
Each custom tool requires a parser that will be instantiated to scan the console log (or a report file). The parser 
must derive from the abstract class `IssueParser` or one of its child classes. 
The following classes can be used as  base class:

- `IssueParser`: parses a report file. You have under full control on how to access the report.
- `RegexpLineParser`: parses all lines of a report one by one with a regular expression.
- `FastRegexpLineParser`: same as `RegexpLineParser`, but with additional support to skip lines based on string comparisons.
- `RegexpDocumentParser`: parses the whole report by reading it into a string and parsing the whole string 
with a regular expression. This type of parser is quite slow and should only be used if there is no other way.
  
Please have a look at one of the 
[existing parsers](https://github.com/jenkinsci/analysis-model/tree/master/src/main/java/edu/hm/hafner/analysis/parser)
in order to see how to use these base classes.

## Register the tool

In order to get picked up by the warnings plugin your parser must be registered as an extension.
This is achieved by extending from the base class `Tool` and registering it using the annotation 
`@Extension` at your associated `Descriptor` class. Typically you do this in Jenkins by adding the descriptor as a
static nested class of your `Tool` class. 

Since most of the static analysis tools are based on a report file, the more specific base class `ReportScanningTool` 
has been provided that should be used in this case - this base class provides already support for adding a report 
`pattern` and `encoding` property in the user interface. 

Here is an example that can be used as a starting point. 

```java
package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for your tool.
 */
public class YourTool extends ReportScanningTool {
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
    public static class Descriptor extends ReportScanningToolDescriptor {
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

You can create a HPI of your plugin by calling `mvn clean install`. For more details on Jenkins plugin development
please see [wiki](https://wiki.jenkins.io/display/JENKINS/Extend+Jenkins) 
or [homepage](https://jenkins.io/doc/developer/).  

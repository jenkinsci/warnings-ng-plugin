# Jenkins Warning Plug-in

The Jenkins 'warnings' plug-in collects compiler warnings or issues reported by static analysis tools and visualizes the 
results. It has built-in support for numerous static analysis tools (including several compilers), see the list of
[supported report formats](../SUPPORTED-FORMATS.md). 

## Supported Project Types

Starting with release 5.x the warnings plug-in has support for the following Jenkins project types:

- Freestyle Project
- Maven Project
- Matrix Project
- Scripted Pipeline
- Parallel Pipeline
- Declarative Pipeline
- Multi-branch Pipeline

## Features Overview 

The warnings plug-in basically provides the following features if added as a post build step to a job: 

1. The plug-in scans the console log of a Jenkins build or files in the workspace of your job for any kind of issues. 
There are almost one hundred [report formats](../SUPPORTED-FORMATS.md) supported. It can detect errors from you 
compiler (C, C#, Java, etc.), warnings from a static analysis tool (CheckStyle, StyleCop, SpotBugs, etc.),
duplications from a copy-and-paste detector (CPD, Simian, etc.), vulnerabilities, or even open tasks in comments of 
your source files. 
2. The plug-in publishes a report of the found issues in your build, so you can navigate to a summary report from the 
main build page. From here you can also dive into the details: 
    1. distribution of new, fixed and outstanding issues
    2. distribution of the issues by severity, category, type, module, or package
    3. list of all issues including helpful comments from the reporting tool
    4. annotated source code of the affected files
    5. trend charts of the issues 
    
## Transition from 4.x to 5.x

The warnings plug-in previously was part of the static analysis suite, that provided the same set of features through 
several plugins (CheckStyle, PMD, Static Analysis Utilities, Analysis Collector etc.). 
In order to simplify the user experience and the development process, these
plug-ins and the core functionality have been merged into the warnings plug-ins. All other plug-in are not required
anymore and will not be supported in the future. If you already use one of these plug-ins you should migrate
your jobs to the new API as soon as possible. I will still maintain the old code for a while, but the main development
effort will be spent into the new code base.

### Migration of Pipelines

### Migration of all other jobs

Freestyle, Matrix or Maven Jobs using the old API typically used a so called **Post Build Action** to publish warnings. 
E.g., the FindBugs plug-in did provide the post build action *"Publish FindBugs analysis results"*. This publisher is not 
supported anymore. You need to add a new post build step - this step now is called *"Record static analysis results"*
for all kind of static analysis tools. The selection of the tool is part of the configuration of this post build step. 
Note: the warnings produced by a post build step using the old API could not be read by the new post build step.
I.e., you can't see a combined history of the old and new results - you simply see two unrelated results. There is
no automatic conversion of results stored in the old format available.

### Migration of Plug-in Depending on analysis-core

The following plug-ins have been integrated into this version of the warnings plug-in:

- Android-Lint Plug-in
- CheckStyle Plug-in
- CCM Plug-in
- Dry Plug-in
- PMD Plug-in
- FindBugs Plug-in

All other plug-ins still need to be integrated or need to be refactored to use the new API.

## Configuration

The basic configuration of the plug-in is the same for all Jenkins job types. Note that for scripted pipelines some
additional features are available to aggregate and group issues.
    
### Graphical Configuration

The plug-in is enabled in a job by adding the post build action *"Record static analysis results"*. The basic configuration
screen is shown in the image above:

![basic configuration](images/freestyle-start.png) 

First of all you need to specify the tool that should be used to parse the console log or the report file. 
Then you need to specify the pattern of the report files that should be parsed and scanned for issues. 
If you do not specify a pattern, then the console log of your build will be scanned. For several popular tools a default
pattern has been provided. Depending on the selected tool you might configure some additional parameters as well. 
You can specify multiple tools (and patterns) that will be used with the same configuration. Due to a technical 
(or marketing) limitation of Jenkins it is not possible to select different configurations by using multiple post build 
actions.  

One new feature is available by using the checkbox *"Aggregate Results"*: if this option is selected, then one result
is created that contains an aggregation of all issues of the selected tools. This is something the 
Static Analysis Collector Plug-in provided previously. When this option is activated you get a unique entry point 
for all of your issues.

### Simple Pipeline Configuration

### Advanced Pipeline Configuration

## New Features

### Remote API

The plug-in provides two REST API endpoints. 

#### Summary of the analysis result

You can obtain a summary of a particular analysis report by using the URL \[tool-id\]/api/xml (or json). The summary
contains the number of issues, the quality gate status, and all info and error messages.

Here is an example XML report:

```xml
<analysisResultApi _class='io.jenkins.plugins.analysis.core.restapi.AnalysisResultApi'>
  <totalSize>3</totalSize>
  <fixedSize>0</fixedSize>
  <newSize>0</newSize>
  <noIssuesSinceBuild>-1</noIssuesSinceBuild>
  <successfulSinceBuild>-1</successfulSinceBuild>
  <qualityGateStatus>WARNING</qualityGateStatus>
  <owner _class='org.jenkinsci.plugins.workflow.job.WorkflowRun'>
    <number>46</number>
    <url>http://localhost:8080/view/White%20Mountains/job/Full%20Analysis%20-%20Model/46/</url>
  </owner>
  <infoMessage>Searching for all files in '/tmp/node1/workspace/Full Analysis - Model' that match the pattern
    '**/target/spotbugsXml.xml'
  </infoMessage>
  <infoMessage>-> found 1 file</infoMessage>
  <infoMessage>Successfully parsed file /tmp/node1/workspace/Full Analysis - Model/target/spotbugsXml.xml</infoMessage>
  <infoMessage>-> found 3 issues (skipped 0 duplicates)</infoMessage>
  <infoMessage>Post processing issues on 'node1' with encoding 'UTF-8'</infoMessage>
  <infoMessage>Resolving absolute file names for all issues</infoMessage>
  <infoMessage>-> affected files for all issues already have absolute paths</infoMessage>
  <infoMessage>Copying affected files to Jenkins' build folder /Users/hafner/Development/jenkins/jobs/Full Analysis -
    Model/builds/46
  </infoMessage>
  <infoMessage>-> 2 copied, 0 not in workspace, 0 not-found, 0 with I/O error</infoMessage>
  <infoMessage>Resolving module names from module definitions (build.xml, pom.xml, or Manifest.mf files)</infoMessage>
  <infoMessage>-> all issues already have a valid module name</infoMessage>
  <infoMessage>Resolving package names (or namespaces) by parsing the affected files</infoMessage>
  <infoMessage>-> all affected files already have a valid package name</infoMessage>
  <infoMessage>Creating fingerprints for all affected code blocks to track issues over different builds</infoMessage>
  <infoMessage>No filter has been set, publishing all 3 issues</infoMessage>
  <infoMessage>No valid reference build found - all reported issues will be considered outstanding</infoMessage>
  <infoMessage>Evaluating quality gates</infoMessage>
  <infoMessage>-> WARNING - Total number of issues: 3 - Quality Gate: 1</infoMessage>
  <infoMessage>-> Some quality gates have been missed: overall result is WARNING</infoMessage>
  <infoMessage>Health report is disabled - skipping</infoMessage>
</analysisResultApi>
```

#### Details of the analysis result

The reported issues are also available as REST API. You can either query all issues, or only the 
new, fixed, or outstanding issues. The corresponding URLs are:

1. \[tool-id\]/all/api/xml
2. \[tool-id\]/fixed/api/xml
3. \[tool-id\]/new/api/xml
4. \[tool-id\]/outstanding/api/xml

Here is an example JSON report:

```json
{
  "_class" : "io.jenkins.plugins.analysis.core.restapi.ReportApi",
  "issues" : [
    {
      "baseName" : "AbstractParser.java",
      "category" : "EXPERIMENTAL",
      "columnEnd" : 0,
      "columnStart" : 0,
      "description" : "",
      "fileName" : "/private/tmp/node1/workspace/Full Analysis - Model/src/main/java/edu/hm/hafner/analysis/AbstractParser.java",
      "fingerprint" : "be18f803030f2af690fbeef09eafa5c9",
      "lineEnd" : 59,
      "lineStart" : 59,
      "message" : "edu.hm.hafner.analysis.AbstractParser.parse(File, Charset, Function) may fail to clean up java.io.InputStream",
      "moduleName" : "Static Analysis Model and Parsers",
      "origin" : "spotbugs",
      "packageName" : "edu.hm.hafner.analysis",
      "reference" : "46",
      "severity" : "LOW",
      "type" : "OBL_UNSATISFIED_OBLIGATION"
    },
    {
      "baseName" : "ReportTest.java",
      "category" : "STYLE",
      "columnEnd" : 0,
      "columnStart" : 0,
      "description" : "",
      "fileName" : "/private/tmp/node1/workspace/Full Analysis - Model/src/test/java/edu/hm/hafner/analysis/ReportTest.java",
      "fingerprint" : "331d509297fad027813365ad0fb37e69",
      "lineEnd" : 621,
      "lineStart" : 621,
      "message" : "Return value of Report.get(int) ignored, but method has no side effect",
      "moduleName" : "Static Analysis Model and Parsers",
      "origin" : "spotbugs",
      "packageName" : "edu.hm.hafner.analysis",
      "reference" : "46",
      "severity" : "LOW",
      "type" : "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT"
    },
    {
      "baseName" : "ReportTest.java",
      "category" : "STYLE",
      "columnEnd" : 0,
      "columnStart" : 0,
      "description" : "",
      "fileName" : "/private/tmp/node1/workspace/Full Analysis - Model/src/test/java/edu/hm/hafner/analysis/ReportTest.java",
      "fingerprint" : "1e641f9c0b35ed97140d639695e8ce18",
      "lineEnd" : 624,
      "lineStart" : 624,
      "message" : "Return value of Report.get(int) ignored, but method has no side effect",
      "moduleName" : "Static Analysis Model and Parsers",
      "origin" : "spotbugs",
      "packageName" : "edu.hm.hafner.analysis",
      "reference" : "46",
      "severity" : "LOW",
      "type" : "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT"
    }
  ],
  "size" : 3
}
```

## Still Available Features

- Build summary showing the new and fixed warnings of a build
- Several trend reports showing the number of warnings per build
- Overview of the found warnings per module, package, author, category, or type
- Detail reports of the found warnings optionally filtered by severity (or new and fixed)
- Colored HTML display of the corresponding source file and warning lines
- Quality Gates to mark builds as unstable or failed based on the number of issues
- Configurable project health support
- Highscore computation for builds without warnings and successful builds
- Token to simplify post processing of the analysis results (e.g., using Email notifications)

## Not Yet Supported Features

- Several portlets for the Jenkins dashboard view
- View column that shows the total number of warnings in a job

### REST API
### Token Macro
### Column


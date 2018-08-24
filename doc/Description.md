# Jenkins Warning Plug-in

The Jenkins 'warnings' plug-in collects compiler warnings or issues reported by static analysis tools and visualizes the 
results. It has built-in support for numerous static analysis tools (including several compilers), see the list of
[supported report formats](../SUPPORTED-FORMATS.md). 

## Supported Project Types

Starting with release 5.0.0 the warnings plug-in has support for the following Jenkins project types:

- Freestyle Project
- Maven Project
- Matrix Project
- Scripted Pipeline
- Parallel Pipeline
- Declarative Pipeline
- Multi-branch Pipeline

## Features Overview 

The warnings plug-in basically provides the following features if added as a post build step to a job: 

1. It scans the console log of a Jenkins build or files in the workspace of your job for any kind of issues. 
There are almost one hundred [report formats](../SUPPORTED-FORMATS.md) supported. It can detect errors from you 
compiler (C, C#, Java, etc.), warnings from a static analysis tool (CheckStyle, StyleCop, SpotBugs, etc.),
duplications from a copy-and-paste detector (CPD, Simian, etc.), vulnerabilities from a vulnerability scanner, 
or open tasks in your source files. 
2. It reports these issues as an action of your build. I.e., you can navigate to a summary that shows an overview of the 
 reported issues. From here you can also dive into the details: 
    1. distribution of new, fixed and outstanding issues
    2. distribution of the issues by severity, category, type, module, or package
    3. list of all issues including helpful comments from the reporting tool
    4. annotated source code of the affected files
    5. trend charts of the issues 
    
## Transition from 4.x to 5.x

The warnings plug-in previously was part of the static analysis suite, that provided the same set of features through 
several plugins (CheckStyle, PMD, etc.). In order to simplify the user experience and the development process, these
plug-ins and the core functionality have been merged into the warnings plug-ins. All other plug-in are not required
anymore and will not be supported in the future. If you already use one of these plug-ins, then you should migrate
your jobs to the new API as soon as possible. I still will maintain the old code for a while, but the main development
effort will be spent into the new code base.

### Migration of non Pipelines

### Migration of Pipelines

## Configuration

The basic configuration of the plug-in is the same for all Jenkins job types. Note that for scripted pipelines some
additional features are available to aggregate and group issues.
    
### Graphical Configuration

### Simple Pipeline Configuration

### Advanced Pipeline Configuration


## Advanced Features

- Build summary showing the new and fixed warnings of a build
- Several trend reports showing the number of warnings per build
- Overview of the found warnings per module, package, author, category, or type
- Detail reports of the found warnings optionally filtered by severity (or new and fixed)
- Colored HTML display of the corresponding source file and warning lines
- Quality Gates to mark builds as unstable or failed based on the number of issues
- Configurable project health support
- Highscore computation for builds without warnings and successful builds
- Remote API to export the results
- Token to simplify post processing of the analysis results (e.g., using Email notifications)

## Not Yet Supported Features

- Several portlets for the Jenkins dashboard view
- View column that shows the total number of warnings in a job

### REST API
### Token Macro
### Column


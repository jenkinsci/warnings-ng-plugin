# Jenkins Warnings Next Generation Plugin

[![Join the chat at https://gitter.im/jenkinsci/warnings-plugin](https://badges.gitter.im/jenkinsci/warnings-plugin.svg)](https://gitter.im/jenkinsci/warnings-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/warnings-ng.svg?label=latest%20version)](https://plugins.jenkins.io/warnings-ng)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/warnings-ng.svg?color=red)](https://plugins.jenkins.io/warnings-ng)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.204.4-green.svg?label=min.%20Jenkins)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg?label=min.%20JDK)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 

The Jenkins Next Generation Warnings plugin collects compiler warnings or issues reported by static analysis tools and 
visualizes the results. It has built-in support for more than hundred [report formats](SUPPORTED-FORMATS.md). 
Among the problems it can detect:
- errors from your compiler (C, C#, Java, etc.)
- warnings from a static analysis tool (CheckStyle, StyleCop, SpotBugs, etc.)
- duplications from a copy-and-paste detector (CPD, Simian, etc.)
- vulnerabilities
- open tasks in comments of your source files

The Jenkins **Next Generation** Warnings plug-in replaces the whole Jenkins Static Analysis Suite. 
I.e. it makes the following Jenkins plugins obsolete:
Android Lint, CheckStyle, Dry, FindBugs, PMD, Warnings, Static Analysis Utilities, Static Analysis Collector.

The plugin publishes a report of the issues found in your build, so you can navigate to a summary report from the 
main build page. From there you can also dive into the details: 
- distribution of new, fixed and outstanding issues
- distribution of the issues by severity, category, type, module, or package
- list of all issues including helpful comments from the reporting tool
- annotated source code of the affected files
- trend charts of the issues

If you are using Git as source code management system then the warnings plugin will optionally mine 
the repository in the style of 
[Code as a Crime Scene](https://www.adamtornhill.com/articles/crimescene/codeascrimescene.htm) 
(Adam Tornhill, November 2013) to determine statistics of the affected files (i.e. the files with issues):
- total number of commits 
- total number of different authors
- creation time
- last modification time

Additionally, the plugin shows the last person who modified the code that contains an issue (and the last commit ID).
In order to use this functionality you need to install the optional 
[Git Forensics Plugin](https://github.com/jenkinsci/git-forensics-plugin).

If your tool is not yet supported by the warnings plugin you can either define a new Groovy based parser in the 
user interface or provide a parser within a new small plug-in. If the parser is useful for other teams as well 
please share it and provide pull requests for the 
[Warnings Next Generation Plug-in](https://github.com/jenkinsci/warnings-ng-plugin/pulls) and 
the [Analysis Parsers Library](https://github.com/jenkinsci/analysis-model/pulls). 

For more details please refer to the [documentation](doc/Documentation.md) or to an 
[introductory video](https://www.youtube.com/watch?v=0GcEqML8nys).

All source code is licensed under the MIT license.

[![Jenkins](https://ci.jenkins.io/job/Plugins/job/warnings-ng-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/warnings-ng-plugin/job/master/)
[![GitHub Actions](https://github.com/jenkinsci/warnings-ng-plugin/workflows/CI%20on%20all%20platforms/badge.svg?branch=master)](https://github.com/jenkinsci/warnings-ng-plugin/actions)
[![Codacy](https://api.codacy.com/project/badge/Grade/2a5c80b9064749a09d128f89f661d1c3)](https://www.codacy.com/app/uhafner/warnings-ng-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/warnings-ng-plugin&amp;utm_campaign=Badge_Grade)
[![Codecov](https://codecov.io/gh/jenkinsci/warnings-ng-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/jenkinsci/warnings-ng-plugin/branch/master)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/jenkinsci/warnings-ng-plugin.svg)](https://github.com/jenkinsci/warnings-ng-plugin/pulls)


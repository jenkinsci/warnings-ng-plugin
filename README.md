# Jenkins Warnings Next Generation Plugin

[![Join the chat at https://gitter.im/jenkinsci/warnings-plugin](https://badges.gitter.im/jenkinsci/warnings-plugin.svg)](https://gitter.im/jenkinsci/warnings-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/warnings-ng.svg)](https://plugins.jenkins.io/warnings-ng)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.138.4-green.svg)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![GitHub pull requests](https://img.shields.io/github/issues-pr/jenkinsci/warnings-ng-plugin.svg)](https://github.com/jenkinsci/warnings-ng-plugin/pulls)

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
Android Lint, CheckStyle, Dry, FindBugs, PMD, Warnings, Static Analysis Utilities, Static Analysis Collector Plugins.

If your tool is not yet supported you can either define a new Groovy based parser in the user interface or provide 
a parser within a new small plug-in. If the parser is useful for other teams as well please share it and provide 
pull requests for the 
[Warnings Next Generation Plug-in](https://github.com/jenkinsci/warnings-ng-plugin/pulls) and 
the [Analysis Parsers Library](https://github.com/jenkinsci/analysis-model/pulls). 

The plugin publishes a report of the issues found in your build, so you can navigate to a summary report from the 
main build page. From there you can also dive into the details: 
- distribution of new, fixed and outstanding issues
- distribution of the issues by severity, category, type, module, or package
- list of all issues including helpful comments from the reporting tool
- annotated source code of the affected files
- trend charts of the issues

For more details please refer to the [documentation](doc/Documentation.md).

All source code is licensed under the MIT license.


[![Jenkins](https://ci.jenkins.io/job/Plugins/job/warnings-ng-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/warnings-ng-plugin/job/master/)
[![Travis](https://img.shields.io/travis/jenkinsci/warnings-ng-plugin/master.svg?logo=travis&branch=master&label=travis%20build&logoColor=white)](https://travis-ci.org/jenkinsci/warnings-ng-plugin)
[![Codacy](https://api.codacy.com/project/badge/Grade/2a5c80b9064749a09d128f89f661d1c3)](https://www.codacy.com/app/uhafner/warnings-ng-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/warnings-ng-plugin&amp;utm_campaign=Badge_Grade)
[![Codecov](https://img.shields.io/codecov/c/github/jenkinsci/warnings-ng-plugin/master.svg)](https://codecov.io/gh/jenkinsci/warnings-ng-plugin/branch/master)


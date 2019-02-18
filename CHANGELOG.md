# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-3.0.2...master)

## [3.0.2](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-3.0.1...warnings-ng-3.0.2) - 2019-2-15

- [JENKINS-56182](https://issues.jenkins-ci.org/browse/JENKINS-56182): 
Fixed NPE while configuring a job.

## [3.0.1](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-3.0.0...warnings-ng-3.0.1) - 2019-2-15

### Fixed
- [JENKINS-56142](https://issues.jenkins-ci.org/browse/JENKINS-56142): 
Fixed broken quality gate UI configuration (snippet generator).
- [JENKINS-50355](https://issues.jenkins-ci.org/browse/JENKINS-50355): 
Fixed validation of DRY thresholds.
- [JENKINS-56103](https://issues.jenkins-ci.org/browse/JENKINS-56103): 
Changed ID/URL of Maven Console Parser to 'maven-warnings' since 'maven' is already used by another plugin. 
- [JENKINS-55436](https://issues.jenkins-ci.org/browse/JENKINS-55436): 
Changed step symbol of PMD to 'pmdParser' and AndroidLint to 'androidLintParser' since 'pmd' and 'androidLint' are 
already used as step names of other plugins. 

## [3.0.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-2.2.1...warnings-ng-3.0.0) - 2019-2-13

### Added
- [JENKINS-54550](https://issues.jenkins-ci.org/browse/JENKINS-54550), 
[JENKINS-52098](https://issues.jenkins-ci.org/browse/JENKINS-52098):
Simplified and enhanced quality gates configuration: available properties are now the total number of warnings,
the number of new warnings, or the delta between two builds. Additionally, thresholds can be provided for all issues
or for a specific severity only.

## [2.2.1](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-2.2.0...warnings-ng-2.2.1) - 2019-2-07

### Fixed
- [JENKINS-55846](https://issues.jenkins-ci.org/browse/JENKINS-55846): 
ErrorProne parser: Added support for Gradle reports.

## [2.2.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-2.1.2...warnings-ng-2.2.0) - 2019-2-07

### Added
- [JENKINS-22526](https://issues.jenkins-ci.org/browse/JENKINS-22526), 
[JENKINS-17196](https://issues.jenkins-ci.org/browse/JENKINS-17196):
Added action to reset the reference build (quality gate evaluation).
- [JENKINS-51267](https://issues.jenkins-ci.org/browse/JENKINS-51267), 
[JENKINS-51438](https://issues.jenkins-ci.org/browse/JENKINS-51438),
[JENKINS-55730](https://issues.jenkins-ci.org/browse/JENKINS-55730),
[JENKINS-55775](https://issues.jenkins-ci.org/browse/JENKINS-55775),
[JENKINS-55839](https://issues.jenkins-ci.org/browse/JENKINS-55839),
[JENKINS-51439](https://issues.jenkins-ci.org/browse/JENKINS-51439): 
Group issues by folder if no package is available.
- [JENKINS-55442](https://issues.jenkins-ci.org/browse/JENKINS-55442): Added include/exclude filters for issue messages. 

### Fixed
- Fixed validation of Groovy parsers

## [2.1.2](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-2.1.1...warnings-ng-2.1.2) - 2019-1-28

### Fixed
- Fixed sandbox bypass via CSRF 
(see [Jenkins Security Advisory 2019-01-28](https://jenkins.io/security/advisory/2019-01-28/))

## [2.1.1](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-2.1.0...warnings-ng-2.1.1) - 2019-1-21

### Fixed
- Fixed NPE in column and portlet after restart of Jenkins.

## [2.1.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-2.0.0...warnings-ng-2.1.0) - 2019-1-21

### Added
- [JENKINS-55500](https://issues.jenkins-ci.org/browse/JENKINS-55500): dashboard view portlet
    - make tool selection configurable
    - provide direct links to the analysis results
- [JENKINS-52755](https://issues.jenkins-ci.org/browse/JENKINS-52755), [JENKINS-54239](https://issues.jenkins-ci.org/browse/JENKINS-54239):
View column that shows the number of issues.
- Click on trend chart to navigate to selected analysis results. 

### Fixed
- [JENKINS-55674](https://issues.jenkins-ci.org/browse/JENKINS-55674), [JENKINS-55564](https://issues.jenkins-ci.org/browse/JENKINS-55564): 
redraw tables after restoring the paging size.
- [JENKINS-55679](https://issues.jenkins-ci.org/browse/JENKINS-55679): fixed rendering of XML files in source view.

## [2.0.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-1.0.1...warnings-ng-2.0.0) - 2019-1-15

### Added
- New source code view based on [Prism](https://prismjs.com/). Provides client side syntax highlighting for 
several languages. Thanks to Philippe Arteau for the [PR](https://github.com/jenkinsci/warnings-plugin/pull/146).
- Support for [ErrorProne](http://errorprone.info) in maven builds. 
Parser now reports description with link to external documentation.
- [JENKINS-55500](https://issues.jenkins-ci.org/browse/JENKINS-55500): Added a portlet that renders a two-dimensional 
table of issues per type and job

### Changed
- [API]: Replaced `CheckForNull` annotations with `Nullable` in order to enable 
[NullAway](https://github.com/uber/NullAway) checker in build

### Fixed
- [JENKINS-55514](https://issues.jenkins-ci.org/browse/JENKINS-55514): 
Fixed handling of severity mappings with FindBugs (rank vs. priority).
- [JENKINS-55513](https://issues.jenkins-ci.org/browse/JENKINS-55513): 
Show 'loading...' message while the issues are loaded dynamically from the server.
- [JENKINS-55511](https://issues.jenkins-ci.org/browse/JENKINS-55511): 
Fixed rendering of issues table: check if order column in browsers local storage is valid before applying it.
- [JENKINS-55495](https://issues.jenkins-ci.org/browse/JENKINS-55495): 
Use Bootstrap to render the fixed issues table.
- [JENKINS-55337](https://issues.jenkins-ci.org/browse/JENKINS-55337): 
Navigate to maven warnings in console log view
- Maven Parser: Disabled post processing on agent since there are no source files involved.
- Do not show empty paragraph if issues have no message.

## [1.0.1](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-1.0.0...warnings-ng-1.0.1) - 2018-12-28

### Fixed
- [JENKINS-55328](https://issues.jenkins-ci.org/browse/JENKINS-55328): Show error message if symbol 'pmd' is used
- [JENKINS-55298](https://issues.jenkins-ci.org/browse/JENKINS-55298): Improved documentation of Groovy script syntax 
- [JENKINS-55293](https://issues.jenkins-ci.org/browse/JENKINS-55293): Improved health report validation error messages 

## 1.0.0 - 2018-12-20

First public release.

<!---
## 1.0.0 - year-month-day
### Added
- One 
- Two 

### Changed
- One 
- Two 

### Deprecated
- One 
- Two 

### Removed
- One 
- Two 

### Fixed
- One 
- Two 

### Security
- One 
- Two 

# Git Log

[Unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v0.3.0...v1.0.0

-->

# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-5.2.0...master)

### Fixed
- [JENKINS-57709](https://issues.jenkins-ci.org/browse/JENKINS-57709): Fixed class loading problems if Git plugin
is not installed.

### Added 

- [JENKINS-58056](https://issues.jenkins-ci.org/browse/JENKINS-58056),
  [PR#113](https://github.com/jenkinsci/warnings-ng-plugin/pull/113): Added option to fail a build if errors have been 
  reported during the execution of the issues recording step.

### Removed

- Removed dependency to Git plugin. Now all code required to invoke Git blame for all affected files
has been moved to the new Jenkins plugins [forensics-api](https://github.com/jenkinsci/forensics-api-plugin) and 
[git-forensics](https://github.com/jenkinsci/git-forensics-plugin). All SCM and Git related classes (blame)
have been removed as well.
  
## [5.3.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-5.2.0...warnings-ng-5.3.0) - 2019-7-4

### Added 

- [JENKINS-55051](https://issues.jenkins-ci.org/browse/JENKINS-55051),
[PR#116](https://github.com/jenkinsci/warnings-ng-plugin/pull/116): 
Added DScanner parser

## [5.2.1](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-5.2.0...warnings-ng-5.2.1) - 2019-7-1

### Fixed
- [JENKINS-58253](https://issues.jenkins-ci.org/browse/JENKINS-58253): Restored deprecated quality gate 
properties so that old scripts will still work.

## [5.2.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-5.1.0...warnings-ng-5.2.0) - 2019-6-27

### Added 
- [JENKINS-39203](https://issues.jenkins-ci.org/browse/JENKINS-39203),
  [PR#58](https://github.com/jenkinsci/warnings-ng-plugin/pull/58): Mark only the failed stage as unstable if a quality gate
  is failed. Other stages will not be affected anymore.
- [JENKINS-57098](https://issues.jenkins-ci.org/browse/JENKINS-57098),
[PR#177](https://github.com/jenkinsci/analysis-model/pull/177),
[PR#168](https://github.com/jenkinsci/analysis-model/pull/168): 
Added a generic JSON parser that reads all properties of the internal `Issue` object.
- [PR#57](https://github.com/jenkinsci/warnings-ng-plugin/pull/57):
Added a parser for MentorGraphcis Modelsim/Questa.
- [JENKINS-57245](https://issues.jenkins-ci.org/browse/JENKINS-57245), [PR#111](https://github.com/jenkinsci/warnings-ng-plugin/pull/111):
  Added a Kotlin Parser.

### Fixed
- [JENKINS-56007](https://issues.jenkins-ci.org/browse/JENKINS-56007): Obtain the affected files in a process on the 
master (rather than pushing them from the agent to the master) so that master - agent security will not block the copy process. 
- Fix resetting of reference build in Firefox.

## [5.1.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-5.0.0...warnings-ng-5.1.0) - 2019-5-31

### Fixed 

- Persisted XSS vulnerability in Warnings Next Generation Plugin (SECURITY-1373 / CVE-2019-10325): 
Plugin rendered the name of a custom warnings parser unescaped on Jenkins web pages. 
This allowed attackers with Job/Configure permission to define a custom parser whose name included HTML and JavaScript, 
resulting in a persisted cross-site scripting vulnerability. Plugin now properly escapes custom warnings parser names.

- CSRF vulnerability in Warnings Next Generation Plugin (SECURITY-1391 / CVE-2019-10326): 
Plugin did not require that requests sent to the endpoint used to reset warning counts use POST. This resulted in a 
cross-site request forgery vulnerability that allows attackers to reset warning counts for future builds.
Plugin now requires that these requests be sent via POST.


## [5.0.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-4.0.0...warnings-ng-5.0.0) - 2019-5-7

### Added 

I finally managed it to port the charting functionality of the former analysis-collector plugin:
- Trend chart (job page): shows the warnings of all static analysis results in a single chart. 
- Portlet (dashboard view): shows the number of warnings from all jobs in the view in a chart. 

Additional new features:
- [PR#27](https://github.com/jenkinsci/warnings-ng-plugin/pull/27):
 Support for [IBLinter](https://github.com/IBDecodable/IBLinter) - Xcode Storyboard Linter
- [PR#21](https://github.com/jenkinsci/warnings-ng-plugin/pull/21):
 Support for [PHPStan](https://github.com/phpstan/phpstan) - PHP Static Analysis Tool 
- [PR#20](https://github.com/jenkinsci/warnings-ng-plugin/pull/20):
 Support for [Axivion Suite](https://www.axivion.com/en/produkte-60)
- [PR#132](https://github.com/jenkinsci/analysis-model/pull/132): 
Support for CMake warnings.
- [PR#137](https://github.com/jenkinsci/analysis-model/pull/137):
Support for JSON output from Cargo.
- [JENKINS-56510](https://issues.jenkins-ci.org/browse/JENKINS-56510),
[PR#154](https://github.com/jenkinsci/analysis-model/pull/154): 
Added a generic parser that reads all properties of the internal `Issue` object.

### Fixed

- Added missing dependency to JSON-Smart library.
- [JENKINS-56484](https://issues.jenkins-ci.org/browse/JENKINS-56484),
[analysis-model-PR#19](https://github.com/jenkinsci/analysis-model/pull/130): 
Filter TimeStamper Console log decorations
- [PR#19](https://github.com/jenkinsci/warnings-ng-plugin/pull/19): Use `BuildStepMonitor.NONE` in step 
to allow for higher concurrency.
- [JENKINS-56324](https://issues.jenkins-ci.org/browse/JENKINS-56324): 
If an issue in the current build is equal to several issues in the reference build 
then select the issue with the same fingerprint as the outstanding issue. 

### Changed

- Decreased visibility of several internal elements
- \[API\]: Refactored trend chart API

### Removed

- \[API\]: Removed trend chart aggregation methods.

## [4.0.0](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-3.0.3...warnings-ng-4.0.0) - 2019-3-15

### Added
- Added new trend charts that show the trend of issues by severity, tools, and project health. See 
[documentation](https://github.com/jenkinsci/warnings-ng-plugin/blob/master/doc/Documentation.md#build-trend)
for more details.
- Enabled zooming (by number of visible build) in trend charts 
- Enabled switching of X-axis type: charts can show the results either by build number or date 
- Responsive design of details page: depending on the resolution of your display, different UI representations
will be used. 
- [PR#103](https://github.com/jenkinsci/analysis-model/pull/103),
[PR#13](https://github.com/jenkinsci/warnings-ng-plugin/pull/13): New parser for IAR CSTAT warnings.
- [JENKINS-56418](https://issues.jenkins-ci.org/browse/JENKINS-56418), 
[PR#18](https://github.com/jenkinsci/warnings-ng-plugin/pull/18): Added Robot Framework icons.
- [PR#14](https://github.com/jenkinsci/warnings-ng-plugin/pull/14): Added Pylint icons.
- [PR#15](https://github.com/jenkinsci/warnings-ng-plugin/pull/15): Add package and module support for Pylint 
and updated the documentation for the format to use.

### Fixed
- [JENKINS-55840](https://issues.jenkins-ci.org/browse/JENKINS-55840), 
[JENKINS-27973](https://issues.jenkins-ci.org/browse/JENKINS-27973),
[JENKINS-7178](https://issues.jenkins-ci.org/browse/JENKINS-7178): 
Refactored Gcc4 parser so that it is also capable of parsing Doxygen warnings.
- [PR#16](https://github.com/jenkinsci/warnings-ng-plugin/pull/16), 
[JENKINS-49965](https://issues.jenkins-ci.org/browse/JENKINS-49965): Improved performance of issues table.
- [JENKINS-44915](https://issues.jenkins-ci.org/browse/JENKINS-44915): Show description of Pylint rules in details.

### Changed
- \[API\]: Completely rewritten charts API.

## [3.0.3](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-3.0.1...warnings-ng-3.0.3) - 2019-2-18

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
- \[API\]: Replaced `CheckForNull` annotations with `Nullable` in order to enable 
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

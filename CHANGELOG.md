# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/jenkinsci/warnings-ng-plugin/compare/warnings-ng-1.0.1...master)

### Added
- Added support for [ErrorProne](http://errorprone.info) in maven builds. 
Parser now reports description with link to external documentation.

### Changed
- [API]: Replaced `CheckForNull` annotations with `Nullable` in order to enable 
[NullAway](https://github.com/uber/NullAway) checker in build

### Fixed
- [JENKINS-55513](https://issues.jenkins-ci.org/browse/JENKINS-55513): 
Show 'loading...' message while the issues are loaded dynamically from the server.
- [JENKINS-55514](https://issues.jenkins-ci.org/browse/JENKINS-55514): 
Fixed handling of severity mappings with FindBugs (rank vs. priority).
- [JENKINS-55511](https://issues.jenkins-ci.org/browse/JENKINS-55511): 
Fixed rendering of issues table: check if order column in browsers local storage is valid before applying it (report on Gitter).
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

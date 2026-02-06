# Contributing to the Warnings Next Generation Plug-in

This document provides information about contributing code to Jenkins' Warnings Next Generation plug-in.

:exclamation: There's a lot more to the Jenkins project than just code. 
For information on contributing to the Jenkins'
project overall, check out [Jenkins' contributing landing page](https://jenkins.io/participate/).
 
## Beginner Topics

If you don't have a specific problem or task in mind, i.e.,  you simply want to participate in this open source project 
I would suggest looking at the open newbie friendly issues in GitHub issues. 
I marked several newbie friendly issues with the label `newbie-friendly`. 
These are a good starting point to get in touch with this Jenkins plugin.
If you already have some experience with the plugin you can also fix one of the issues that are marked with the label
`help-wanted`. 

## Parser Implementations

If you are planning to provide your own parser, please also have a look at the project 
[Static Analysis Model and Parsers](https://github.com/jenkinsci/analysis-model). Here, all parsers need to be 
added. The Jenkins Warnings Plug-in does not include the parsers anymore, it just links all parsers using the 
analysis-model library. 

## Getting started

Setup your development environment as described in
[Development environment for Jenkins' Warnings and Code Coverage Plugins](https://github.com/uhafner/warnings-ng-plugin-devenv).

## Coding Guidelines

Start reading the code, and you'll get the hang of it. A complete description of the
coding guidelines is part of a [separate GitHub repository](https://github.com/uhafner/codingstyle) that I am also using
for my lectures about software development.

For [IntelliJ IDEA](https://www.jetbrains.com/idea/) users: If you use the
[provided development environment](https://github.com/uhafner/warnings-ng-plugin-devenv) then the coding style is stored
in configuration files below the `codingstyle/.idea` and `codingstyle/etc` folders. When you import all projects into IntelliJ
then this style will be used automatically.

Moreover, (since this project is about static code analysis :wink:) a configuration for the following static code
analysis tools is defined in the POM (and the `etc` and `.idea` folders of the `codingstyle` module):
- [Checkstyle](https://checkstyle.sourceforge.net/)
- [PMD](https://pmd.github.io/)
- [SpotBugs](https://spotbugs.github.io)
- [Error Prone](https://errorprone.info)
- [IntelliJ](https://www.jetbrains.com/help/idea/code-inspection.html)

This configuration will be picked up automatically if you build the project using Maven. If you install the CheckStyle
plugin of IntelliJ then the correct set of CheckStyle rules will used automatically. Moreover, the code formatter and
the inspection rules will be automatically picked up by IntelliJ.

## Proposing Changes

All proposed changes are submitted and code reviewed using the 
[GitHub Pull Request](https://help.github.com/articles/about-pull-requests/) process.

To submit a pull request:

1. Commit changes and push them to your fork on GitHub.
It is a good practice is to create branches instead of pushing to master.
2. In GitHub Web UI click the **New Pull Request** button.
3. Select `warnings-ng-plugin` as **base fork** and `master` as **base**, then click **Create Pull Request**.
4. Fill in the Pull Request description. It should reflect the changes, the reason behind the changes, and if available a
reference to the issue.
5. Click **Create Pull Request**.
6. Wait for CI results and reviews. 
7. Process the feedback (see previous step). If there are changes required, commit them in your local branch and push them
again to GitHub. Your pull request will be updated automatically. Review comments for changed lines will become outdated.

Once your Pull Request is ready to be merged, the repository maintainer will integrate it.
There is no additional action required from pull request authors at this point.

## Copyright

The Static Analysis Suite is licensed under [MIT license](./LICENSE). We consider all contributions as MIT unless it's 
explicitly stated otherwise. MIT-incompatible code contributions will be rejected.
Contributions under MIT-compatible licenses may be also rejected if they are not ultimately necessary.

## Continuous Integration

The Jenkins project has a Continuous Integration server... powered by Jenkins, of course.
The CI job for this project is located at [ci.jenkins.io](https://ci.jenkins.io/).


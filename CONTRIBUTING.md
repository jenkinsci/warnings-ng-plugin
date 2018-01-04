# Contributing to the Static Analysis Suite

This page provides information about contributing code to Jenkins' Static Analysis Suite

:exclamation: There's a lot more to the Jenkins project than just code. For information on contributing to the Jenkins'
project overall, check out [Jenkins' contributing landing page](https://jenkins.io/participate/).
 
## Getting started

1. Fork the repository on GitHub.
2. Clone the forked repository to your machine.
3. Install the development tools. In order to contribute to Jenkins' Static Analysis Suite, you need the following tools:
   * Java Development Kit (JDK) 8.
   * Maven 3.3.9 or above. You can download it [here](https://maven.apache.org/download.cgi).
   * Any IDE which supports importing Maven projects.
4. Setup your development environment as described in 
[Preparing for Plugin Development](https://jenkins.io/doc/developer/tutorial/prepare/).

## Coding Guidelines

Start reading the code and you'll get the hang of it. A complete description of the 
coding guidelines is part of a [separate GitHub repository](https://github.com/uhafner/codingstyle), which 
is only available in German. 

For [IntelliJ IDEA](https://www.jetbrains.com/idea/) users: the coding style is stored in configuration files below the 
`.idea` folder. If you import this project into IntelliJ this style will used automatically. 

Moreover (since this project is about static code analysis :wink:) a configuration for the following static code
analysis tools is defined in the `etc` folder:
- [Checkstyle](http://checkstyle.sourceforge.net/)
- [PMD](http://https://pmd.github.io/)
- [FindBugs](http://findbugs.sourceforge.net/) and [SpotBugs](https://spotbugs.github.io)
- [IntelliJ](https://www.jetbrains.com/help/idea/code-inspection.html)

This configuration will be picked up automatically if you build the project using maven. If you install the CheckStyle 
plugin of IntelliJ then the correct set of CheckStyle rules will used automatically. 

## Proposing Changes

The Jenkins project source code repositories are hosted on GitHub. All proposed changes are submitted and code reviewed 
using the [GitHub Pull Request](https://help.github.com/articles/about-pull-requests/) process.

To submit a pull request:

1. Commit changes and push them to your fork on GitHub.
It is a good practice is to create branches instead of pushing to master.
2. In GitHub Web UI click the **New Pull Request** button.
3. Select `analysis-core-plugin` as **base fork** and `master` as **base**, then click **Create Pull Request**.
4. Fill in the Pull Request description. It should reflect the changes, the reason behind the changes, and if available a
reference to the Jenkins ticket in our [issue tracker](https://issues.jenkins-ci.org/).
5. Click **Create Pull Request**.
6. Wait for CI results, reviews. 
7. Process the feedback (see previous step). If there are changes required, commit them in your local branch and push them
again to GitHub. Your pull request will be updated automatically. Review comments for changed lines will become outdated.

Once your Pull Request is ready to be merged, the repository maintainer will integrate it.
There is no additional action required from pull request authors at this point.

## Copyright

Static Analysis Suite  is licensed under [MIT license](./LICENSE). We consider all contributions as MIT unless it's 
explicitly stated otherwise. MIT-incompatible code contributions will be rejected.
Contributions under MIT-compatible licenses may be also rejected if they are not ultimately necessary.

We **Do NOT** require pull request submitters to sign the 
[contributor agreement](https://wiki.jenkins.io/display/JENKINS/Copyright+on+source+code)
as long as the code is licensed under MIT and merged by one of the contributors with the signed agreement.

## Continuous Integration

The Jenkins project has a Continuous Integration server... powered by Jenkins, of course.
The CI job for this project is located at [ci.jenkins.io](https://ci.jenkins.io/job/Plugins/job/analysis-core-plugin/).

# Links

* [Jenkins Contribution Landing Page](https://jenkins.io/paricipate/)
* [Jenkins IRC Channel](https://jenkins.io/chat/)
* [Beginners Guide To Contributing](https://wiki.jenkins.io/display/JENKINS/Beginners+Guide+to+Contributing)
* [List of newbie-friendly issues in the core](https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened)%20AND%20component%20%3D%20core%20AND%20labels%20in%20(newbie-friendly))


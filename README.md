[![Build Status](https://travis-ci.org/dependency-check/dependency-check-sonar-plugin.svg?branch=master)](https://travis-ci.org/dependency-check/dependency-check-sonar-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e9cebd3112ec4252804bba68a5b44071)](https://www.codacy.com/manual/dependency-check/dependency-check-sonar-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dependency-check/dependency-check-sonar-plugin&amp;utm_campaign=Badge_Grade)
[![Download](https://api.bintray.com/packages/dependency-check/owasp/sonar-dependency-check/images/download.svg)](https://bintray.com/dependency-check/owasp/sonar-dependency-check/_latestVersion)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dependency-check_dependency-check-sonar-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=dependency-check_dependency-check-sonar-plugin)

# Dependency-Check Plugin for SonarQube 7.x

Integrates [Dependency-Check][] reports into SonarQube v7.6 or higher.

The project will try to backport all code from master branch to last supported LTS. Please see the [SonarQube 6.x][] or [SonarQube 5.x][] branch for old supported version.

## About Dependency-Check

Dependency-Check is a utility that attempts to detect publicly disclosed vulnerabilities contained within project dependencies. It does this by determining if there is a Common Platform Enumeration (CPE) identifier for a given dependency. If found, it will generate a report linking to the associated CVE entries.

Dependency-Check supports the identification of project dependencies in a number of different languages including Java, .NET, Node.js, Ruby, and Python.

## Note

**This SonarQube plugin does not perform analysis**, rather, it reads existing Dependency-Check reports. Use one of the other available methods to scan project dependencies and generate the necessary XML report which can then be consumed by this plugin. Refer to the [Dependency-Check project](https://github.com/jeremylong/DependencyCheck) for relevant [documentation](https://jeremylong.github.io/DependencyCheck/).

## Metrics

The plugin keeps track of a number of statistics including:

-   Total number of dependencies scanned
-   Total number of vulnerabilities found across all dependencies
-   Total number of vulnerable components
-   Total number of critical, high, medium, and low severity vulnerabilities

Additionally, the following two metrics are defined:

### Inherited Risk Score (IRS)

```java
 (critical * 7) + (high * 5) + (medium * 3) + (low * 1)
```

The IRS is simply a weighted measurement of the vulnerabilities inherited by the application through the use of vulnerable components. It does not measure the applications actual risk due to those components. The higher the score the more risk the application inherits.

### Vulnerable Component Ratio

(vulnerabilities / vulnerableComponents)

This is simply a measurement of the number of vulnerabilities to the vulnerable components (as a percentage). A higher percentage indicates that a large number of components contain vulnerabilities. Lower percentages are better.

## Compiling

> $ mvn clean package

## Distribution

Ready to use binaries are available from [GitHub][] and [bintray][].

## Plugin version compatibility

Please use the newest minor version. Keep in mind, that SonarQube 5.6 LTS is not supported anymore.

| Plugin Version | SonarQube version    |
|--------------- | ---------------------|
| 1.2.x          | SonarQube 7.6 and up |
| 1.1.x          | SonarQube 6.7 LTS    |
| 1.0.3          | SonarQube 5.6 LTS    |

## Installation

Copy the plugin (jar file) to $SONAR_INSTALL_DIR/extensions/plugins and restart SonarQube.

## Using

Create aggregate reports with Dependency-Check. Dependency-Check will output a file named 'dependency-check-report.xml' or 'dependency-check-report.json' . The Dependency-Check SonarQube plugin reads an existing Dependency-Check XML or JSON report.

## Plugin Configuration

A typical SonarQube configuration will have the following parameter. This example assumes the use of a Jenkins workspace, but can easily be altered for other CI/CD systems.

```ini
sonar.dependencyCheck.xmlReportPath=${WORKSPACE}/dependency-check-report.xml
sonar.dependencyCheck.jsonReportPath=${WORKSPACE}/dependency-check-report.json
sonar.dependencyCheck.htmlReportPath=${WORKSPACE}/dependency-check-report.html
```

In this example, both the XML and HTML reports are specified. Only the XML report is required, however, if the HTML
report is also available, it greatly enhances the usability of the SonarQube plugin by incorporating the actual
Dependency-Check HTML report in the SonarQube project.

To configure the severity of the created issues you can optionally specify the minimum score for each severity with the following parameter. Specify a score of `-1` to completely disable a severity.

```ini
sonar.dependencyCheck.severity.blocker=9.0
sonar.dependencyCheck.severity.critical=7.0
sonar.dependencyCheck.severity.major=4.0
sonar.dependencyCheck.severity.minor=0.0
```

In large projects you have many dependencies with (hopefully) no vulnerabilities. The following configuration summarize all vulnerabilities of one dependency into one issue.

```ini
sonar.dependencyCheck.summarize=true
sonar.dependencyCheck.summarize=false (default)
```

If you want skip this plugin, it's possible with following configuration.

```ini
sonar.dependencyCheck.skip=true
sonar.dependencyCheck.skip=false (default)
```

## Ecosystem

Dependency-Check is available as a:
-   Command-line utility
-   Ant Task
-   Gradle Plugin
-   Jenkins Plugin
-   Maven Plugin
-   SonarQube Plugin

## Copyright & License

Dependency-Check Sonar Plugin is Copyright (c) dependency-check. All Rights Reserved.

Dependency-Check is Copyright (c) Jeremy Long. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the [LGPLv3][] license.

[lgplv3]: http://www.gnu.org/licenses/lgpl.txt
[github]: https://github.com/dependency-check/dependency-check-sonar-plugin/releases
[dependency-check]: https://www.owasp.org/index.php/OWASP_Dependency_Check
[sonarqube 5.x]: https://github.com/dependency-check/dependency-check-sonar-plugin/tree/SonarQube_5.x
[sonarqube 6.x]: https://github.com/dependency-check/dependency-check-sonar-plugin/tree/SonarQube_6.x
[bintray]: https://bintray.com/dependency-check/owasp/sonar-dependency-check

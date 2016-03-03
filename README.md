# Jackson Extension
[![Build Status](https://travis-ci.org/klausboeing/jackson-extension.svg?branch=master)](https://travis-ci.org/klausboeing/jackson-extension)

Module for Jackson adds utilities for serialization and deserialization.

## Getting Started

### Prerequisities

- Maven
- Java 8

### Installing

Configure the project repository:

```xml
        <repositories>
            <repository>
                <id>klausboeing-mvn-repo</id>
                <url>https://raw.github.com/klausboeing/jackson-extension/mvn-repo/</url>
                <snapshots>
                    <enabled>true</enabled>
                    <updatePolicy>always</updatePolicy>
                </snapshots>
            </repository>
        </repositories>
```
Add the following dependence on project:
```xml
        <dependency>
          <groupId>com.klausboeing</groupId>
          <artifactId>jackson-extension</artifactId>
          <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
## Versioning

We use [SemVer](http://semver.org/) for versioning.

## Authors

* **Klaus Boeing** - *Initial work*

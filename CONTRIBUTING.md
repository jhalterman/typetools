Contributing
============

## Reporting Bugs

Bug reports are welcome and appreciated. When filing an issue, please include a
small code snippet that demonstrates the bug if you can, else include a good
description of how to reproduce the bug.

## Contributing Bug Fixes

Pull requests for bugs related to existing features are always welcome.

## Contributing Features

If you have an idea for a new feature, the best place to start is not with a pull
request but rather  by opening an issue describing how the feature or API change
should work and why you think it is necessary. The reason we suggest starting
with an issue rather than a pull request is that we like to make sure every
feature and API change is widely useful and a good fit for the library, and 
would hate to reject a PR that someone puts a lot of time into if it's not a
good fit.

If your feature idea sounds good, you can then submit a PR, else we'll schedule
the feature for implementation.

# Building

To build the projects you must have JDKs, 1.8, 11, 17, 21, and 25 installed.

For example:

```shell
sudo apt install temurin-8-jdk temurin-11-jdk temurin-17-jdk temurin-21-jdk temurin-25-jdk
```

Then create a the `~/.m2/toolchains.xml` file. Note that the `version` for JDK 8
must be 8, not 1.8.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains
        xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 https://maven.apache.org/xsd/toolchains-1.1.0.xsd"
        xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <toolchain>
        <type>jdk</type>
        <provides>
            <lts>true</lts>
            <version>25</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/temurin-25-jdk-amd64</jdkHome>
        </configuration>
    </toolchain>
    <toolchain>
        <type>jdk</type>
        <provides>
            <lts>true</lts>
            <version>21</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/temurin-21-jdk-amd64</jdkHome>
        </configuration>
    </toolchain>
    <toolchain>
        <type>jdk</type>
        <provides>
            <lts>true</lts>
            <version>17</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/temurin-17-jdk-amd64</jdkHome>
        </configuration>
    </toolchain>
    <toolchain>
        <type>jdk</type>
        <provides>
            <lts>true</lts>
            <version>11</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/temurin-11-jdk-amd64</jdkHome>
        </configuration>
    </toolchain>
    <toolchain>
        <type>jdk</type>
        <provides>
            <lts>true</lts>
            <!-- Must be 8, not 1.8-->
            <version>8</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/temurin-8-jdk-amd64</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

To run the tests:

```shell
mvn verify
```

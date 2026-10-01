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

Then generate the `toolchains.xml` file:

```shell
mvn toolchains:generate-jdk-toolchains-xml -D"toolchain.file=~/.m2/toolchains.xml"
```

To run the tests:

```shell
mvn verify
```

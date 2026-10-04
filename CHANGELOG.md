# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Use a multi-release jar and provide `module-info.java` on JDK 9+. 

### Fixed
- Use `MethodHandle` before using `sun.misc.Unsafe` on Java 9+. See notes
  [On Lambda Support](./README.md#on-lambda-support) for details on how to
  suppress warnings about the use of `Unsafe` on JDK 22+.

## [0.6.3]
### Fixed
- Added support for lambda type argument resolution in Java 12 and above.

## [0.6.2]
### Fixed
- Export correct version with OSGI package
- Added Automatic-Module-Name

## [0.6.1]
### Fixed
- Fix stack overflow when reifying recursive types

## [0.6.0]
### Added
- Added support for reifying generics

## [0.5.0]
### Added
- Added Java 9 lambda and method reference resolution support

## [0.4.9]
### Added
- Added support for resolving constructor references

## [0.4.8]
### Added
- Added RetroLambda support.

## [0.4.7]
### Fixed
- Issue #27 - Fixed resolution of array type args.

## [0.4.6]
### Fixed
- Issue #23 - Fixed resolution of serializable lambdas.

## [0.4.5]
### Fixed
- Issue #18 - Added proper android support
- Issue #17 - Handle context final variables that are passed as argument

## [0.4.4]
### Fixed
- Fixed issue #11 - Disabling the cache breaks type resolution.

## [0.4.3]
### Fixed
- Detect constant pool offsets when resolving lambdas for 1.8.0_60+ JREs.

## [0.4.2]
### Fixed
- Added stricter checking for lambda/1.8 support.

## [0.4.1]
### Added
- Added support for resolving instance method reference type arguments. Fixes issue #5.
- Added OSGi support. From pull request #6.

## [0.4.0]
### Added
- Added support for resolving lambda expression type arguments.

## [0.3.1]
### Added
- Added support for resolving type arguments on inner classes.

## [0.3.0]
### Changed
- Updated API to be more consistently based on resolving for a particular type, given a sub-type

## [0.2.1]
### Added
- Initial implementation

[Unreleased]: https://github.com/jhalterman/typetools/compare/typetools-0.6.3...HEAD
[0.6.3]: https://github.com/jhalterman/typetools/compare/typetools-0.6.2...typetools-0.6.3
[0.6.2]: https://github.com/jhalterman/typetools/compare/typetools-0.6.1...typetools-0.6.2
[0.6.1]: https://github.com/jhalterman/typetools/compare/typetools-0.6.0...typetools-0.6.1
[0.6.0]: https://github.com/jhalterman/typetools/compare/typetools-0.5.0...typetools-0.6.0
[0.5.0]: https://github.com/jhalterman/typetools/compare/typetools-0.4.9...typetools-0.5.0
[0.4.9]: https://github.com/jhalterman/typetools/compare/typetools-0.4.8...typetools-0.4.9
[0.4.8]: https://github.com/jhalterman/typetools/compare/typetools-0.4.7...typetools-0.4.8
[0.4.7]: https://github.com/jhalterman/typetools/compare/typetools-0.4.6...typetools-0.4.7
[0.4.6]: https://github.com/jhalterman/typetools/compare/typetools-0.4.5...typetools-0.4.6
[0.4.5]: https://github.com/jhalterman/typetools/compare/typetools-0.4.4...typetools-0.4.5
[0.4.4]: https://github.com/jhalterman/typetools/compare/typetools-0.4.3...typetools-0.4.4
[0.4.3]: https://github.com/jhalterman/typetools/compare/typetools-0.4.2...typetools-0.4.3
[0.4.2]: https://github.com/jhalterman/typetools/compare/typetools-0.4.1...typetools-0.4.2
[0.4.1]: https://github.com/jhalterman/typetools/compare/typetools-0.4.0...typetools-0.4.1
[0.4.0]: https://github.com/jhalterman/typetools/compare/typetools-0.3.1...typetools-0.4.0
[0.3.1]: https://github.com/jhalterman/typetools/compare/typetools-0.3.0...typetools-0.3.1
[0.3.0]: https://github.com/jhalterman/typetools/compare/typetools-0.2.1...typetools-0.3.0
[0.2.1]: https://github.com/jhalterman/typetools/compare/typetools-0.2.1...756be7353d575b7524a99df5c6f67b58a7ae6d6a

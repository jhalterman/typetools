# 0.4.7

### Bug Fixes

* Issue #27 - Fixed resolution of array type args.

# 0.4.6

### Bug Fixes

* Issue #23 - Fixed resolution of serializable lambdas.

## 0.4.5

* Issue #18 - Added proper android support
* Issue #17 - Handle context final variables that are passed as argument

## 0.4.4

* Fixed issue #11 - Disabling the cache breaks type resolution.

## 0.4.3

### Bug Fixes

* Detect constant pool offsets when resolving lambdas for 1.8.0_60+ JREs.

## 0.4.2

### Bug Fixes

* Added stricter checking for lambda/1.8 support.

## 0.4.1

### New Features

* Added support for resolving instance method reference type arguments. Fixes issue #5.
* Added OSGi support. From pull request #6.

## 0.4.0

### New Features

* Added support for resolving lambda expression type arguments.

## 0.3.1

### New Features

* Added support for resolving type arguments on inner classes.
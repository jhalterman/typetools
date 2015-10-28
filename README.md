# TypeTools
[![Build Status](https://travis-ci.org/jhalterman/typetools.svg)](https://travis-ci.org/jhalterman/typetools) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.jodah/typetools/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.jodah/typetools) 

A simple, zero-dependency library for working with types. Supports Java 1.6+.

## Introduction

One of the sore points with Java involves working with type information. In particular, Java's generics do not provide a way to resolve the type information for a given class. TypeTools looks to solve this by fully resolving generic type information declared on any class, interface, lambda expression or method.

## Usage

The [TypeResolver](http://jodah.net/typetools/javadoc/net/jodah/typetools/TypeResolver.html) class provides the following methods:

* `Class<?>[] resolveRawArguments(Class<T> type, Class<S> subType)`
<br>Resolves the raw arguments for a `type` using type variable information from a `subType`.
* `Class<?> resolveRawArgument(Class<T> type, Class<S> subType)`
<br>Resolves the raw argument for a `type` using type variable information from a `subType`.
* `Type resolveGenericType(Class<?> type, Type subType)`
<br>Resolves the generic `type` using type variable information from a `subType`.
* `Class<?> resolveRawClass(Type genericType, Class<?> subType)`
<br>Resolves the raw class for a `genericType` using type variable information from a `subType`. 

## Examples

A typical use case is to resolve arguments for a type, given a sub-type:

```java
interface Foo<T1, T2> {}
class Bar implements Foo<Integer, String> {}

Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Foo.class, Bar.class);

assert typeArgs[0] == Integer.class;
assert typeArgs[1] == String.class;
```

Type arguments can also be resolved from lambda expressions:

```java
Function<String, Integer> strToInt = s -> Integer.valueOf(s);
Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, strToInt.getClass());

assert typeArgs[0] == String.class;
assert typeArgs[1] == Integer.class;
```

And from method references:

```java
Comparator<String> comparator = String::compareToIgnoreCase;
Class<?> typeArg = TypeResolver.resolveRawArgument(Comparator.class, comparator.getClass());

assert typeArg == String.class;
```

We can also resolve the raw class for type parameters on fields and methods:

```java
class Entity<ID extends Serializable> {
  ID id;
  void setId(ID id) {}
}

class SomeEntity extends Entity<Long> {}

Type fieldType = Entity.class.getDeclaredField("id").getGenericType();
Type mutatorType = Entity.class.getDeclaredMethod("setId", Serializable.class).getGenericParameterTypes()[0];

assert TypeResolver.resolveRawClass(fieldType, SomeEntity.class) == Long.class;
assert TypeResolver.resolveRawClass(mutatorType, SomeEntity.class) == Long.class;
```

## Common Use Cases

[Layer supertypes](http://martinfowler.com/eaaCatalog/layerSupertype.html) often utilize type parameters that are populated by subclasses. A common use case for TypeTools is to resolve the type arguments for a layer supertype given a sub-type. 

Following is an example **Generic DAO** layer supertype implementation:

```java
class Device {}
class Router extends Device {}

class GenericDAO<T, ID extends Serializable> {
  protected Class<T> persistentClass;
  protected Class<ID> idClass;

  private GenericDAO() {
    Class<?>[] typeArguments = TypeResolver.resolveRawArguments(GenericDAO.class, getClass());
    this.persistentClass = (Class<T>) typeArguments[0];
    this.idClass = (Class<ID>) typeArguments[1];
  }
}

class DeviceDAO<T extends Device> extends GenericDAO<T, Long> {}
class RouterDAO extends DeviceDAO<Router> {}
```

We can assert that type arguments are resolved as expected:

```java
RouterDAO routerDAO = new RouterDAO();
assert routerDAO.persistentClass == Router.class;
assert routerDAO.idClass == Long.class;
```

## Additional Features

By default, type variable information for each resolved type is weakly cached by the `TypeResolver`. Caching can be enabled/disabled via:

```java
TypeResolver.enableCache();
TypeResolver.disableCache();
```

## Additional Notes

#### On Lambda Support

Lambda type argument resolution is currently supported for:

* Oracle JDK 8
* Open JDK 8

#### On Unresolvable Lambda Type Arguments

When resolving type arguments with lambda expressions, only type parameters used in the functional interface's method signature can be resolved. Ex:

```java
interface ExtraFunction<T, R, Z> extends Function<T, R>{}
ExtraFunction<String, Integer, Long> strToInt = s -> Integer.valueOf(s);
Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, strToInt.getClass());

assert typeArgs[0] == String.class;
assert typeArgs[1] == Integer.class;
assert typeArgs[2] == Unknown.class;
```

Since the type parameter `Z` in this example is unused by `Function`, its argument resolves to `Unknown.class`.

#### On OSGi Support

When using TypeTools in an OSGi environment where lambda or method reference type argument resolution is desired, the `sun.reflect` system package should be exported to the application bundles. For example, for Felix, add the following to your config.properties file:

```
org.osgi.framework.system.packages.extra=sun.reflect
```

## Docs

JavaDocs are available [here](https://jhalterman.github.com/typetools/javadoc).

## License

Copyright 2010-2015 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).

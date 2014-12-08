# TypeTools [![Build Status](https://travis-ci.org/jhalterman/typetools.png)](https://travis-ci.org/jhalterman/typetools)

A simple, zero-dependency set of tools for working with Java types.

## Introduction

One of the sore points with Java involves working with type information. In particular, Java's generics implementation doesn't provide a way to resolve the type information for a given class. TypeTools looks to solve this by fully resolving generic type information declared on any class, interface, lambda expression or method.

## Setup

Add TypeTools as a Maven dependency:

```xml
<dependency>
  <groupId>net.jodah</groupId>
  <artifactId>typetools</artifactId>
  <version>0.3.1</version>
</dependency>
```

## Usage

The `TypeResolver` class provides the following methods:

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
class Bar implements Foo<HashSet<Integer>, ArrayList<String>> {}

Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Foo.class, Bar.class);

assert typeArgs[0] == HashSet.class;
assert typeArgs[1] == ArrayList.class;
```

Type arguments can also be resolved for lambda expressions:

```java
Function<String, Integer> strToInt = s -> Integer.valueOf(s);
Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, strToInt.getClass());

assert typeArgs[0] == String.class;
assert typeArgs[1] == Integer.class;
```

We can also resolve the raw class for any generic type, given a sub-type:

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

void assertTypeArguments() {
  RouterDAO routerDAO = new RouterDAO();
  assert routerDAO.persistentClass == Router.class;
  assert routerDAO.idClass == Long.class;
}
```

## Additional Notes

#### On Lambda Support

Lambda type argument resolution has currently been tested against:

* Oracle JDK 8
* Open JDK 8

Other runtimes may work as well, but have not yet been tested.

#### On Unresolvable Lambda Type Arguments

When resolving type arguments with lambda expressions is that only type parameters used in the functional interface's method signature can be resolved. Ex:

```java
interface ExtraFunction<T, R, Z> extends Function<T, R>{}
ExtraFunction<String, Integer, Long> strToInt = s -> Integer.valueOf(s);

assert typeArgs[0] == String.class;
assert typeArgs[1] == Integer.class;
assert typeArgs[2] == Unknown.class;
```

Since the type parameter `Z` in this example is unused by `Function`, its argument resolves to `Unknown.class`.


## Additional Features

By default, type variable information for each resolved type is weakly cached by the `TypeResolver`. Caching can be enabled/disabled via:

```java
TypeResolver.enableCache();
TypeResolver.disableCache();
```

## Docs

JavaDocs are available [here](https://jhalterman.github.com/typetools/javadoc).

## License

Copyright 2010-2014 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
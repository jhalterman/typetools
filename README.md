# TypeTools 0.3.0

A simple, zero-dependency set of tools for working with Java types.

## Introduction

One of the sore points with Java involves working with type information. In particular, Java's generics implementation doesn't provide a way to resolve the type information for a given class. TypeTools looks to solve this by fully resolving generic type information declared on any class, interface or method.

## Setup

Add TypeTools as a Maven dependency:

```xml
<dependency>
  <groupId>org.jodah</groupId>
  <artifactId>typetools</artifactId>
  <version>0.3.0</version>
</dependency>
```

## Usage

Generic type resolution offered by the `TypeResolver` class:

* `resolveRawArguments(Class<T> type, Class<S> subType)`: Resolves the raw classes representing type arguments for a `type` using type variable information from a `subType`.
* `resolveRawArgument(Class<T> type, Class<S> subType)`: Resolves the raw class representing the type argument for a `type` using type variable information from a `subType`.
* `resolveGenericType(Class<?> type, Type subType)`: Resolves the generic `type` using type variable information from a `subType`.
* `resolveRawClass(Type genericType, Class<?> subType)`: Resolves the raw class for a `genericType` using type variable information from a `subType`. 

## Examples

A typical use case is to resolve the type arguments for a type given a sub-type:

```java
class Foo extends Bar<ArrayList<String>> {}
class Bar<T extends List<String>> implements Baz<HashSet<Integer>, T> {}
interface Baz<T1 extends Set<Integer>, T2 extends List<String>> {}

Class<?>[] typeArguments = TypeResolver.resolveRawArguments(Baz.class, Foo.class);

assert typeArguments[0] == HashSet.class;
assert typeArguments[1] == ArrayList.class;
```

We can also fully resolve the raw class for any generic type given a sub-type:

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

Following is an example layer supertype implementation of a generic DAO:

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

## Additional Features

By default, type variable information for each resolved type is weakly cached by the `TypeResolver`. Caching can be enabled/disabled via:

```java
TypeResolver.enableCache();
TypeResolver.disableCache();
```

## License

Copyright 2010-2013 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
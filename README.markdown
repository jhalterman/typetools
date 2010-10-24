# TypeTools 0.0.1

A simple set of tools for working Java types.

### Motivation

One of the sore points with Java's generics implementation is that it doesn't provide a way to fully resolve the type arguments for a given class. Type arguments and parameters for a given class do not reflect its entire hierarchy. Among other things, TypeTools looks to solve this.

### Features

* Type argument resolution - Given a type to resolve arguments for and a starting point in the type hierarchy, TypeTools will walk the hierarchy, binding type arguments to parameters to provide a fully resolved set of type argument instances for the target type.
* Class resolution - Given a type, the corresponding raw class can be resolved
* Bound resolution - Given a type, the raw class for individual upper bounds can be resolved

### Examples

Type argument raw class resolution:

    class Foo extends Bar<ArrayList<?>> {}
    class Bar<T extends List<?>> implements Baz<HashSet<?>, T> {}
    Baz<T1 extends Set<?>, T2 extends List<?>> {}

    Class<?>[] typeArguments = Types.resolveArguments(Baz.class, Foo.class);
    assert typeArguments[0] == HashSet.class;
    assert typeArguments[1] == ArrayList.class;
    
Type bound raw class resolution:

    class MultiBounded<T extends List<?> & Comparable<?> & Serializable> {}
    
    Type typeParameter = MultiBounded.class.getTypeParameters()[0];
    assertEquals(Types.resolveBound(typeParameter, 0), List.class);
    assertEquals(Types.resolveBound(typeParameter, 1), Comparable.class);
    assertEquals(Types.resolveBound(typeParameter, 2), Serializable.class);

### Use Cases

Layer supertypes often utilize type parameters that are populated by subclasses. A common use case for TypeTools is to resolve the type parameter instances from a layer supertype's subclass, regardless of the complexity of the type hierarchy. 

Below is an example layer supertype implementation of a generic DAO:

    class Device {}
    class Router extends Device {}

    class GenericDAO<T, ID extends Serializable> {
        protected Class<T> persistentClass;
        protected Class<ID> idClass;

        private GenericDAO() {
            Class<?>[] typeArguments = Types.resolveArguments(GenericDAO.class, getClass());
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
    
While this example is oversimplified and lacks validation, it demonstrates a common use case for TypeTools.
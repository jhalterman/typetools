package org.typetools;

import java.io.Serializable;

/**
 * TypeTools examples.
 */
public class Examples {
    static class Device {
    }

    static class Router extends Device {
    }

    static class GenericDAO<T, ID extends Serializable> {
        protected Class<T> persistentClass;
        protected Class<ID> idClass;

        @SuppressWarnings("unchecked")
        private GenericDAO() {
            Class<?>[] typeArguments = Types.resolveArguments(GenericDAO.class, getClass());
            this.persistentClass = (Class<T>) typeArguments[0];
            this.idClass = (Class<ID>) typeArguments[1];
        }
    }

    static class DeviceDAO<T extends Device> extends GenericDAO<T, Long> {
    }

    static class RouterDAO extends DeviceDAO<Router> {
    }

    public static void main(String... args) {
        RouterDAO routerDAO = new RouterDAO();
        assert routerDAO.persistentClass == Router.class;
        assert routerDAO.idClass == Long.class;
    }
}

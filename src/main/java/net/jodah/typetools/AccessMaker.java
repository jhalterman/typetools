package net.jodah.typetools;

import java.lang.reflect.AccessibleObject;

interface AccessMaker {
    void makeAccessible(AccessibleObject object) throws Throwable;
}

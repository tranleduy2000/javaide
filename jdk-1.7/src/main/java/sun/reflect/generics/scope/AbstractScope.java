/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.reflect.generics.scope;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;



/**
 * Abstract superclass for lazy scope objects, used when building
 * factories for generic information repositories.
 * The type parameter <tt>D</tt> represents the type of reflective
 * object whose scope this class is representing.
 * <p> To subclass this, all one needs to do is implement
 * <tt>computeEnclosingScope</tt> and the subclass' constructor.
 */
public abstract class AbstractScope<D extends GenericDeclaration>
    implements Scope {

    private D recvr; // the declaration whose scope this instance represents
    private Scope enclosingScope; // the enclosing scope of this scope

    /**
     * Constructor. Takes a reflective object whose scope the newly
     * constructed instance will represent.
     * @param D - A generic declaration whose scope the newly
     * constructed instance will represent
     */
    protected AbstractScope(D decl){ recvr = decl;}

    /**
     * Accessor for the receiver - the object whose scope this <tt>Scope</tt>
     * object represents.
     * @return The object whose scope this <tt>Scope</tt> object represents
     */
    protected D getRecvr() {return recvr;}

    /** This method must be implemented by any concrete subclass.
     * It must return the enclosing scope of this scope. If this scope
     * is a top-level scope, an instance of  DummyScope must be returned.
     * @return The enclosing scope of this scope
     */
    protected abstract Scope computeEnclosingScope();

    /**
     * Accessor for the enclosing scope, which is computed lazily and cached.
     * @return the enclosing scope
     */
    protected Scope getEnclosingScope(){
        if (enclosingScope == null) {enclosingScope = computeEnclosingScope();}
        return enclosingScope;
    }

    /**
     * Lookup a type variable in the scope, using its name. Returns null if
     * no type variable with this name is declared in this scope or any of its
     * surrounding scopes.
     * @param name - the name of the type variable being looked up
     * @return the requested type variable, if found
     */
    public TypeVariable<?> lookup(String name) {
        TypeVariable[] tas = getRecvr().getTypeParameters();
        for (TypeVariable/*<?>*/ tv : tas) {
            if (tv.getName().equals(name)) {return tv;}
        }
        return getEnclosingScope().lookup(name);
    }
}

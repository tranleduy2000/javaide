/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package sun.reflect.generics.repository;

import java.lang.reflect.TypeVariable;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.Signature;
import sun.reflect.generics.visitor.Reifier;



/**
 * This class represents the generic type information for a generic
 * declaration.
 * The code is not dependent on a particular reflective implementation.
 * It is designed to be used unchanged by at least core reflection and JDI.
 */
public abstract class GenericDeclRepository<S extends Signature>
    extends AbstractRepository<S> {

    private TypeVariable[] typeParams; // caches the formal type parameters

    protected GenericDeclRepository(String rawSig, GenericsFactory f) {
        super(rawSig, f);
    }

    // public API
 /*
 * When queried for a particular piece of type information, the
 * general pattern is to consult the corresponding cached value.
 * If the corresponding field is non-null, it is returned.
 * If not, it is created lazily. This is done by selecting the appropriate
 * part of the tree and transforming it into a reflective object
 * using a visitor.
 * a visitor, which is created by feeding it the factory
 * with which the repository was created.
 */

    /**
     * Return the formal type parameters of this generic declaration.
     * @return the formal type parameters of this generic declaration
     */
    public TypeVariable/*<?>*/[] getTypeParameters(){
        if (typeParams == null) { // lazily initialize type parameters
            // first, extract type parameter subtree(s) from AST
            FormalTypeParameter[] ftps = getTree().getFormalTypeParameters();
            // create array to store reified subtree(s)
            TypeVariable[] tps = new TypeVariable[ftps.length];
            // reify all subtrees
            for (int i = 0; i < ftps.length; i++) {
                Reifier r = getReifier(); // obtain visitor
                ftps[i].accept(r); // reify subtree
                // extract result from visitor and store it
                tps[i] = (TypeVariable<?>) r.getResult();
            }
            typeParams = tps; // cache overall result
        }
        return typeParams.clone(); // return cached result
    }
}

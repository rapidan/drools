package org.drools.reteoo;

/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;

import org.drools.RuleBaseConfiguration;
import org.drools.base.ShadowProxy;
import org.drools.common.BaseNode;
import org.drools.common.EmptyBetaConstraints;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalWorkingMemory;
import org.drools.common.NodeMemory;
import org.drools.common.PropagationContextImpl;
import org.drools.spi.ObjectType;
import org.drools.spi.PropagationContext;
import org.drools.util.FactHashTable;
import org.drools.util.Iterator;
import org.drools.util.AbstractHashTable.FactEntry;

/**
 * <code>ObjectTypeNodes<code> are responsible for filtering and propagating the matching
 * fact assertions propagated from the <code>Rete</code> node using <code>ObjectType</code> interface.
 * <p>
 * The assert and retract methods do not attempt to filter as this is the role of the <code>Rete</code>
 * node which builds up a cache of matching <code>ObjectTypdeNodes</code>s for each asserted object, using
 * the <code>matches(Object object)</code> method. Incorrect propagation in these methods is not checked and
 * will result in <code>ClassCastExpcections</code> later on in the network.
 * <p>
 * Filters <code>Objects</code> coming from the <code>Rete</code> using a
 * <code>ObjectType</code> semantic module.
 * 
 * 
 * @see ObjectType
 * @see Rete
 * 
 * @author <a href="mailto:mark.proctor@jboss.com">Mark Proctor</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 */
public class ObjectTypeNode extends ObjectSource
    implements
    ObjectSink,
    Serializable,
    NodeMemory

{
    // ------------------------------------------------------------
    // Instance members
    // ------------------------------------------------------------

    /**
     * 
     */
    private static final long serialVersionUID = 320L;

    /** The <code>ObjectType</code> semantic module. */
    private final ObjectType  objectType;

    /** The parent Rete node */
    private final Rete        rete;   

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    /**
     * Construct given a semantic <code>ObjectType</code> and the provided
     * unique id. All <code>ObjectTypdeNode</code> have node memory.
     * 
     * @param id
     *          The unique id for the node.
     * @param objectType
     *           The semantic object-type differentiator.
     */
    public ObjectTypeNode(final int id,
                          final ObjectType objectType,
                          final Rete rete,
                          final int alphaNodeHashingThreshold) {
        super( id, null, alphaNodeHashingThreshold );
        this.rete = rete;
        this.objectType = objectType;
        setHasMemory( true );       
    }

    // ------------------------------------------------------------
    // Instance methods
    // ------------------------------------------------------------

    /**
     * Retrieve the semantic <code>ObjectType</code> differentiator.
     * 
     * @return 
     *      The semantic <code>ObjectType</code> differentiator.
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * Tests the provided object to see if this <code>ObjectTypeNode</code> can receive the object
     * for assertion and retraction propagations.
     * 
     * @param object
     * @return
     *      boolean value indicating whether the <code>ObjectTypeNode</code> can receive the object.
     */
    public boolean matches(final Object object) {
        return this.objectType.matches( object );
    }

    /**
     * Propagate the <code>FactHandleimpl</code> through the <code>Rete</code> network. All
     * <code>FactHandleImpl</code> should be remembered in the node memory, so that later runtime rule attachmnents
     * can have the matched facts propagated to them.
     * 
     * @param handle
     *            The fact handle.
     * @param object
     *            The object to assert.
     * @param workingMemory
     *            The working memory session.
     */
    public void assertObject(final InternalFactHandle handle,
                             final PropagationContext context,
                             final InternalWorkingMemory workingMemory) {
        final FactHashTable memory = (FactHashTable) workingMemory.getNodeMemory( this );        

        // checks if shadow is enabled
        if ( this.objectType.isShadowEnabled() ) {
            // need to improve this
            if ( !(handle.getObject() instanceof ShadowProxy) ) {
                // replaces the actual object by its shadow before propagating
                handle.setObject( this.objectType.getShadow( handle.getObject() ) );
                handle.setShadowFact( true );
            } else {
                ((ShadowProxy) handle.getObject()).updateProxy();
            }
        }
        
        if (context.getType() == PropagationContext.MODIFICATION && this.skipOnModify ) {
            // we do this after the shadowproxy update, just so that its up to date for the future
            return;
        }                

        memory.add( handle,
                    false );        

        this.sink.propagateAssertObject( handle,
                                         context,
                                         workingMemory );
    }

    /**
     * Retract the <code>FactHandleimpl</code> from the <code>Rete</code> network. Also remove the 
     * <code>FactHandleImpl</code> from the node memory.
     * 
     * @param handle
     *            The fact handle.
     * @param object
     *            The object to assert.
     * @param workingMemory
     *            The working memory session.
     */
    public void retractObject(final InternalFactHandle handle,
                              final PropagationContext context,
                              final InternalWorkingMemory workingMemory) {
        if (context.getType() == PropagationContext.MODIFICATION && this.skipOnModify ) {
            return;
        }         
        
        final FactHashTable memory = (FactHashTable) workingMemory.getNodeMemory( this );
        memory.remove( handle );               

        this.sink.propagateRetractObject( handle,
                                          context,
                                          workingMemory,
                                          true );
    }

    public void updateSink(final ObjectSink sink,
                           final PropagationContext context,
                           final InternalWorkingMemory workingMemory) {
        final FactHashTable memory = (FactHashTable) workingMemory.getNodeMemory( this );
        final Iterator it = memory.iterator();
        for ( FactEntry entry = (FactEntry) it.next(); entry != null; entry = (FactEntry) it.next() ) {
            sink.assertObject( entry.getFactHandle(),
                               context,
                               workingMemory );
        }
    }

    /**
     * Rete needs to know that this ObjectTypeNode has been added
     */
    public void attach() {
        this.rete.addObjectSink( this );
    }

    public void attach(final InternalWorkingMemory[] workingMemories) {
        attach();

        // we need to call updateSink on Rete, because someone 
        // might have already added facts matching this ObjectTypeNode 
        // to working memories
        for ( int i = 0, length = workingMemories.length; i < length; i++ ) {
            final InternalWorkingMemory workingMemory = workingMemories[i];
            final PropagationContext propagationContext = new PropagationContextImpl( workingMemory.getNextPropagationIdCounter(),
                                                                                      PropagationContext.RULE_ADDITION,
                                                                                      null,
                                                                                      null );
            this.rete.updateSink( this,
                                  propagationContext,
                                  workingMemory );
        }
    }

    public void remove(final BaseNode node,
                       final InternalWorkingMemory[] workingMemories) {
        if ( !node.isInUse() ) {
            removeObjectSink( (ObjectSink) node );
        }
        removeShare();
        if ( !this.isInUse() ) {
            for ( int i = 0, length = workingMemories.length; i < length; i++ ) {
                workingMemories[i].clearNodeMemory( this );
            }
            this.rete.removeObjectSink( this );
        }
    }

    /**
     * Rete needs to know that this ObjectTypeNode has had new nodes attached to
     * it one one of its ancestors
     */
    public void addShare() {
        super.addShare();
    }

    /**
     * Creates memory for the node using PrimitiveLongMap as its optimised for storage and reteivals of Longs.
     * However PrimitiveLongMap is not ideal for spase data. So it should be monitored incase its more optimal
     * to switch back to a standard HashMap.
     */
    public Object createMemory(final RuleBaseConfiguration config) {
        return new FactHashTable();
    }

    public String toString() {
        return "[ObjectTypeNode(" + this.id + ") objectType=" + this.objectType + "]";
    }

    /**
     * Uses he hashCode() of the underlying ObjectType implementation.
     */
    public int hashCode() {
        return this.objectType.hashCode();
    }

    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || !(object instanceof ObjectTypeNode ) ) {
            return false;
        }

        final ObjectTypeNode other = (ObjectTypeNode) object;

        return this.objectType.equals( other.objectType );
    }

}

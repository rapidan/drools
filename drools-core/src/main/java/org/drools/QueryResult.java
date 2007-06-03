package org.drools;

import java.util.Map;

import org.drools.base.ShadowProxy;
import org.drools.common.InternalFactHandle;
import org.drools.rule.Declaration;
import org.drools.spi.Tuple;

public class QueryResult {

    protected Tuple       tuple;
    private WorkingMemory workingMemory;
    private QueryResults  queryResults;

    public QueryResult(final Tuple tuple,
                       final WorkingMemory workingMemory,
                       final QueryResults queryResults) {
        this.tuple = tuple;
        this.workingMemory = workingMemory;
        this.queryResults = queryResults;
    }

    public Map getDeclarations() {
        return this.queryResults.getDeclarations();
    }

    public Object get(final int i) {
        //adjust for the DroolsQuery object
        return getObject( this.tuple.get( i + 1 ));
    }

    public Object get(final String identifier) {
        return get( (Declaration) this.queryResults.getDeclarations().get( identifier ) );
    }

    public Object get(final Declaration declaration) {
        return declaration.getValue( getObject( this.tuple.get( declaration ) ) );
    }

    public FactHandle[] getFactHandles() {
        // Strip the DroolsQuery fact
        final FactHandle[] src = this.tuple.getFactHandles();
        final FactHandle[] dst = new FactHandle[src.length - 1];
        System.arraycopy( src,
                          1,
                          dst,
                          0,
                          dst.length );
        return dst;
    }

    public int size() {
        // Adjust for the DroolsQuery object
        return this.tuple.getFactHandles().length - 1;
    }
    
    private Object getObject(InternalFactHandle handle) {
        if ( handle.isShadowFact() ) {
            return ((ShadowProxy) handle.getObject()).getShadowedObject();
        } else {
            return handle.getObject();
        }
    }
}

package org.drools.rule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.drools.WorkingMemory;
import org.drools.common.InternalFactHandle;
import org.drools.spi.Restriction;
import org.drools.spi.Tuple;

public class OrCompositeRestriction extends AbstractCompositeRestriction {

    private static final long serialVersionUID = 320;

    public OrCompositeRestriction(Restriction[] restriction) {
        super( restriction );
    }

    public boolean isAllowed(final Object object,
                             final InternalFactHandle handle,
                             final Tuple tuple,
                             final WorkingMemory workingMemory) {

        for ( int i = 0, ilength = this.restrictions.length; i < ilength; i++ ) {
            if ( restrictions[i].isAllowed( object,
                                            handle,
                                            tuple,
                                            workingMemory ) ) {
                return true;
            }
        }
        return false;

    }
}

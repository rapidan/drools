package org.drools.base.evaluators;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.RuntimeDroolsException;

public class Operator
    implements
    Externalizable {

    private static final long                  serialVersionUID = 400L;

    // a static private cache so that pluggable operator can register their implementations
    // it is automatically initialized with common operator implementations
    private static final Map<String, Operator> CACHE            = Collections.synchronizedMap( new HashMap<String, Operator>() );

    // these static operator constants are kept here just to make it easier for the engine
    // to reference common used operators. The addition of new constants here is not
    // advisable though.
    public static final Operator               EQUAL            = addOperatorToRegistry( "==",
                                                                                         false );
    public static final Operator               NOT_EQUAL        = addOperatorToRegistry( "!=",
                                                                                         false );
    public static final Operator               LESS             = addOperatorToRegistry( "<",
                                                                                         false );
    public static final Operator               LESS_OR_EQUAL    = addOperatorToRegistry( "<=",
                                                                                         false );
    public static final Operator               GREATER          = addOperatorToRegistry( ">",
                                                                                         false );
    public static final Operator               GREATER_OR_EQUAL = addOperatorToRegistry( ">=",
                                                                                         false );

    /**
     * Creates a new Operator instance for the given parameters,
     * adds it to the registry and return it
     *
     * @param operatorId the identification symbol of the operator
     * @param isNegated true if it is negated
     *
     * @return the newly created operator
     */
    public static Operator addOperatorToRegistry(final String operatorId,
                                                 final boolean isNegated) {
        Operator op = new Operator( operatorId,
                                    isNegated );
        CACHE.put( getKey( operatorId,
                           isNegated ),
                   op );
        return op;
    }

    /**
     * Returns the operator instance for the given parameters
     *
     * @param operatorId the identification symbol of the operator
     * @param isNegated true if it is negated
     *
     * @return the operator in case it exists
     */
    public static Operator determineOperator(final String operatorId,
                                             final boolean isNegated) {
        Operator op = CACHE.get( getKey( operatorId,
                                         isNegated ) );
        if ( op == null ) {
            throw new RuntimeDroolsException( "unable to determine operator for symbol [" + (isNegated ? "not " : "") + operatorId + "]" );
        }
        return op;
    }

    private static String getKey(final String string,
                                 final boolean isNegated) {
        return isNegated + ":" + string;
    }

    // This class attributes
    private String  operator;
    private boolean isNegated;

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        operator    = (String)in.readObject();
        isNegated   = in.readBoolean();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(operator);
        out.writeBoolean(isNegated);
    }

    public Operator() {
        
    }

    private Operator(final String operator,
                     final boolean isNegated) {
        this.operator = operator;
        this.isNegated = isNegated;
    }

    private Object readResolve() throws java.io.ObjectStreamException {
        return determineOperator( this.operator,
                                  this.isNegated );
    }

    public String toString() {
        return "Operator = '" + this.operator + "'";
    }

    public String getOperatorString() {
        return this.operator;
    }

    public boolean isNegated() {
        return this.isNegated;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + (isNegated ? 1231 : 1237);
        result = PRIME * result + ((operator == null) ? 0 : operator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Operator other = (Operator) obj;
        if ( isNegated != other.isNegated ) return false;
        if ( operator == null ) {
            if ( other.operator != null ) return false;
        } else if ( !operator.equals( other.operator ) ) return false;
        return true;
    }

}

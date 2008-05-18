package org.drools.marshalling;

public class PersisterEnums {
    public static final int REPEAT                 = 0;
    public static final int END                    = 1;
    public static final int FACT_HANDLE            = 2;
    public static final int LEFT_TUPLE             = 3;
    public static final int RIGHT_TUPLE            = 4;

    public static final int INITIAL_FACT_NODE      = 5;
    
    public static final int LEFT_TUPLE_BLOCKED     = 6;
    public static final int LEFT_TUPLE_NOT_BLOCKED = 7;

    public static final int ACTIVATION             = 8;
    public static final int PROPAGATION_CONTEXT    = 9;
    
    public static final int WORKING_MEMORY_ACTION  = 10;
    
    public static final int EQUALITY_KEY           = 11;
    public static final int LOGICAL_DEPENDENCY     = 12; 
    
    public static final int AGENDA_GROUP           = 13;
    public static final int ACTIVATION_GROUP       = 14;
    
    public static final int RULE_FLOW_GROUP        = 15;
    public static final int RULE_FLOW_NODE         = 16;
}
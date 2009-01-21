package org.drools.definition.process;

import org.drools.definition.KnowledgeDefinition;

public interface Process
    extends
    KnowledgeDefinition {

    String getId();

    String getName();

    String getVersion();

    String getPackageName();

    String getType();

    Object getMetaData(String name);

}

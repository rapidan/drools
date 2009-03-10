package org.drools.guvnor.client.rulefloweditor;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.guvnor.client.common.FormStyleLayout;
import org.drools.guvnor.client.rpc.RuleFlowContentModel;

import pl.balon.gwt.diagrams.client.connection.Connection;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class RuleFlowViewer extends AbsolutePanel {

    private Map<Long, RuleFlowBaseNode> nodes       = new HashMap<Long, RuleFlowBaseNode>();
    private List<Connection>            connections = new ArrayList<Connection>();

    public RuleFlowViewer(RuleFlowContentModel rfcm,
                          FormStyleLayout parametersForm) {

        int width = 600;
        int height = 600;

        // Handle nodes
        for ( TransferNode tn : rfcm.getNodes() ) {

            RuleFlowBaseNode node = RuleFlowNodeFactory.createNode( tn );

            if ( node != null ) {

                if ( node instanceof ElementContainerNode ) {
                    addElementContainerNodeSubNodes( (ElementContainerNode) node,
                                                     parametersForm );
                }

                node.addParametersForm( parametersForm );

                if ( (tn.getX() + tn.width) > width ) {
                    width = (tn.getX() + tn.width) + 20;
                }
                if ( (tn.getY() + tn.height) > height ) {
                    height = (tn.getY() + tn.height) + 20;
                }

                add( node,
                     node.getX(),
                     node.getY() );

                nodes.put( node.getId(),
                           node );
            }
        }

        for ( TransferConnection c : rfcm.getConnections() ) {
            try {
                Connection connection = RuleFlowConnectionFactory.createConnection( c,
                                                                                    nodes );

                connections.add( connection );

                connection.appendTo( this );

            } catch ( Exception e ) {
                // TODO: handle exception
            }
        }

        // Set the size by the most bottom and right nodes.
        this.setHeight( height + "px" );
        this.setWidth( width + "px" );

    }

    private void addElementContainerNodeSubNodes(ElementContainerNode fen,
                                                 FormStyleLayout parametersForm) {
        for ( RuleFlowBaseNode node : fen.getNodes().values() ) {

            node.addParametersForm( parametersForm );

            if ( node instanceof ElementContainerNode ) {
                addElementContainerNodeSubNodes( (ElementContainerNode) node,
                                                 parametersForm );
            }
        }
    }

    /**
     * Update the connections that this graph has.
     */
    public void update() {
        if ( connections != null ) {
            for ( Connection c : connections ) {
                c.update();
            }
        }

        // Nodes in ElementContainerNode have connections too.
        for ( RuleFlowBaseNode node : nodes.values() ) {
            if ( node instanceof ElementContainerNode ) {
                ((ElementContainerNode) node).update();
            }
        }
    }

    public List<TransferNode> getTransferNodes() {

        List<TransferNode> transferNodes = new ArrayList<TransferNode>();

        for ( RuleFlowBaseNode node : nodes.values() ) {
            transferNodes.add( TransferRuleFlowNodeFactory.createNode( node ) );
        }

        return transferNodes;
    }
}

package org.drools.guvnor.client.explorer;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.Inbox;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.qa.AnalysisView;
import org.drools.guvnor.client.qa.ScenarioPackageView;
import org.drools.guvnor.client.rpc.PackageConfigData;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rpc.WorkingSetConfigData;
import org.drools.guvnor.client.ruleeditor.MultiViewRow;
import org.drools.guvnor.client.rulelist.EditItemEvent;
import org.drools.guvnor.client.security.Capabilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Node;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.tree.TreeNode;
import com.gwtext.client.widgets.tree.event.TreeNodeListenerAdapter;

/*
 * This class contains static node config for BRMS' explorer widgets
 */
public class ExplorerNodeConfig {

    public static String     CATEGORY_ID = "category";                                 //NON-NLS
    public static String     STATES_ID   = "states";                                   //NON-NLS
    private static Constants constants   = ((Constants) GWT.create( Constants.class ));

    public static TreeNode getPackageItemStructure(String packageName,
                                                   String uuid) {

        TreeNode pkg = new TreeNode( packageName );
        pkg.setAttribute( "uuid",
                          uuid );
        pkg.setAttribute( "icon",
                          "images/package.gif" );

        pkg.appendChild( makeItem( constants.BusinessRuleAssets(),
                                   "images/rule_asset.gif",
                                   AssetFormats.BUSINESS_RULE_FORMATS ) );
        pkg.appendChild( makeItem( constants.TechnicalRuleAssets(),
                                   "images/technical_rule_assets.gif",
                                   new String[]{AssetFormats.DRL} ) );
        pkg.appendChild( makeItem( constants.Functions(),
                                   "images/function_assets.gif",
                                   new String[]{AssetFormats.FUNCTION} ) );
        pkg.appendChild( makeItem( constants.DSLConfigurations(),
                                   "images/dsl.gif",

                                   new String[]{AssetFormats.DSL} ) );
        pkg.appendChild( makeItem( constants.Model(),
                                   "images/model_asset.gif",

                                   new String[]{AssetFormats.MODEL, AssetFormats.DRL_MODEL} ) );

        if ( Preferences.getBooleanPref( "flex-bpel-editor" ) ) {
            pkg.appendChild( makeItem( constants.RuleFlows(),
                                       "images/ruleflow_small.gif",
                                       new String[]{AssetFormats.RULE_FLOW_RF, AssetFormats.BPMN2_PROCESS, AssetFormats.BPEL_PACKAGE} ) );
        } else {
            pkg.appendChild( makeItem( constants.RuleFlows(),
                                       "images/ruleflow_small.gif",
                                       new String[]{AssetFormats.RULE_FLOW_RF, AssetFormats.BPMN2_PROCESS} ) );
        }

        pkg.appendChild( makeItem( constants.Enumerations(),
                                   "images/enumeration.gif",

                                   new String[]{AssetFormats.ENUMERATION} ) );

        pkg.appendChild( makeItem( constants.TestScenarios(),
                                   "images/test_manager.gif",

                                   new String[]{AssetFormats.TEST_SCENARIO} ) );

        pkg.appendChild( makeItem( constants.XMLProperties(),
                                   "images/new_file.gif",
                                   new String[]{AssetFormats.XML, AssetFormats.PROPERTIES} ) );

        pkg.appendChild( makeItem( constants.OtherAssetsDocumentation(),
                                   "images/new_file.gif",
                                   new String[0] ) );

		pkg.appendChild(makeItem(constants.WorkingSets(), "images/workingset.gif", new String[] {AssetFormats.WORKING_SET}));
		
        return pkg;
    }

    private static TreeNode makeItem(final String txt,
                                     final String img,
                                     final String[] formats) {
        TreeNode tn = new TreeNode();
        tn.setIcon( img );
        tn.setText( txt );
        tn.setUserObject( new Object[]{formats, txt} );
        return tn;
    }

    public static TreeNode getAdminStructure() {

        TreeNode adminNode = new TreeNode( constants.Admin() );
        //adminNode.setAttribute("icon", "images/managment.gif");

		String[][] adminStructure = new String[][] {
				{ constants.Category(), "images/category_small.gif", "0"},
                { constants.Status(), "images/tag.png", "2" },
				{ constants.Archive(), "images/backup_small.gif", "1" }, 
                { constants.EventLog(), "images/error.gif", "4" },
                { constants.UserPermission(), "images/icoUsers.gif", "5" },
				{ constants.ImportExport(), "images/save_edit.gif", "3" },
				{ constants.RulesVerification(), "images/rule_verification.png", "7" },
				{ constants.About(), "images/information.gif", "6" }};

        for ( int i = 0; i < adminStructure.length; i++ ) {

            String[] packageData = adminStructure[i];
            TreeNode localChildNode = new TreeNode( packageData[0] );
			localChildNode.setAttribute("icon", packageData[1]);   //NON-NLS
			localChildNode.setAttribute("id", packageData[2]);

            adminNode.appendChild( localChildNode );
        }
        return adminNode;
    }

    public static TreeNode getRulesStructure() {
        TreeNode tn = new TreeNode();
        tn.setText( constants.AssetsTreeView() );
        tn.setExpanded( true );

        TreeNode tnc = new TreeNode();
        tnc.setIcon( "images/find.gif" ); //NON-NLS
        tnc.setId( "FIND" );
        tnc.setText( constants.Find() );

        final TreeNode inbox = getInboxStructure();
        tn.appendChild( tnc );

        tn.appendChild( inbox );
        if ( ExplorerLayoutManager.shouldShow( Capabilities.SHOW_PACKAGE_VIEW ) ) {
            tn.appendChild( getStatesStructure() );
        }
        tn.appendChild( getCategoriesStructure() );

        //seem to have to open it on a timer...
        Timer t = new Timer() {
            public void run() {
                inbox.expand();
            }
        };
        t.schedule( 100 );

        return tn;

    }

    private static TreeNode getInboxStructure() {
        final TreeNode treeNode = new TreeNode( "Inbox" );
        treeNode.setAttribute( "icon",
                               "images/inbox.gif" ); //NON-NLS
        treeNode.setAttribute( "id",
                               "inboxes" );

        treeNode.setAttribute("open", false);
        
        TreeNode incoming = new TreeNode( constants.IncomingChanges() );
        incoming.setId( "inbox3" );
        incoming.setAttribute( "icon",
                               "images/category_small.gif" ); //NON-NLS
        incoming.setUserObject( Inbox.INCOMING );
        treeNode.appendChild( incoming );

        TreeNode recentOpened = new TreeNode( constants.RecentlyOpened() );
        recentOpened.setId( "inbox1" );
        recentOpened.setAttribute( "icon",
                                   "images/category_small.gif" ); //NON-NLS
        recentOpened.setUserObject( Inbox.RECENT_VIEWED );
        treeNode.appendChild( recentOpened );

        TreeNode recentEdited = new TreeNode( constants.RecentlyEdited() );
        recentEdited.setId( "inbox2" );
        recentEdited.setAttribute( "icon",
                                   "images/category_small.gif" ); //NON-NLS
        recentEdited.setUserObject( Inbox.RECENT_EDITED );
        treeNode.appendChild( recentEdited );

        treeNode.expand();

        return treeNode;

    }

    public static TreeNode getCategoriesStructure() {
        final TreeNode treeNode = new TreeNode( constants.ByCategory() );
        treeNode.setAttribute( "icon",
                               "images/silk/chart_organisation.gif" );
        treeNode.setAttribute( "id",
                               CATEGORY_ID );
        doCategoryNode( treeNode,
                        "/" );
        return treeNode;
    }

    private static void doCategoryNode(final TreeNode treeNode,
                                       final String path) {
        infanticide( treeNode );
        RepositoryServiceFactory.getService().loadChildCategories( path,
                                                                   new GenericCallback() {
                                                                       public void onSuccess(Object data) {
                                                                           final String value[] = (String[]) data;
                                                                           if ( value.length == 0 ) {
                                                                               if ( path.equals( "/" ) && ExplorerLayoutManager.shouldShow( Capabilities.SHOW_ADMIN ) ) {
                                                                                   RepositoryServiceFactory.getService().listPackages( new GenericCallback<PackageConfigData[]>() {
                                                                                       public void onSuccess(PackageConfigData[] result) {
                                                                                           if ( result.length == 1 ) {
                                                                                               doNewRepoDialog();
                                                                                           }
                                                                                       }
                                                                                   } );

                                                                               }
                                                                               infanticide( treeNode );
                                                                           } else {
                                                                               for ( int i = 0; i < value.length; i++ ) {

                                                                                   final String current = value[i];
                                                                                   System.err.println( "VALUE: " + current + "(" + i + ")" );
                                                                                   final TreeNode childNode = new TreeNode();
                                                                                   childNode.setIcon( "images/category_small.gif" );
                                                                                   childNode.setText( current );

                                                                                   childNode.setUserObject( (path.equals( "/" )) ? current : path + "/" + current );
                                                                                   childNode.appendChild( new TreeNode( constants.PleaseWaitDotDotDot() ) );
                                                                                   childNode.addListener( new TreeNodeListenerAdapter() {
                                                                                       boolean expanding = false;

                                                                                       public void onExpand(Node node) {

                                                                                           if ( !expanding ) {
                                                                                               expanding = true;
                                                                                               infanticide( childNode );
                                                                                               doCategoryNode( childNode,
                                                                                                               (String) childNode.getUserObject() );
                                                                                               childNode.expand();
                                                                                               expanding = false;
                                                                                           }
                                                                                       }
                                                                                   } );

                                                                                   treeNode.appendChild( childNode );
                                                                               }
                                                                           }
                                                                       }

                                                                       private void doNewRepoDialog() {
                                                                           NewRepoDialog diag = new NewRepoDialog();
                                                                           diag.show();
                                                                       }

                                                                   } );
    }

    private static void infanticide(final TreeNode treeNode) {
        Node[] children = treeNode.getChildNodes();
        for ( int i = 0; i < children.length; i++ ) {
            treeNode.removeChild( children[i] );
        }
    }

    public static TreeNode getStatesStructure() {

        final TreeNode treeNode = new TreeNode( constants.ByStatus() );
        treeNode.setAttribute( "icon",
                               "images/status_small.gif" ); //NON-NLS
        treeNode.setAttribute( "id",
                               STATES_ID );

        RepositoryServiceFactory.getService().listStates( new GenericCallback<String[]>() {
            public void onSuccess(String[] value) {
                for ( int i = 0; i < value.length; i++ ) {
                    TreeNode childNode = new TreeNode( value[i] );
                    childNode.setAttribute( "icon",
                                            "images/category_small.gif" ); //NON-NLS
                    childNode.setUserObject( "-" + value[i] );
                    treeNode.appendChild( childNode );
                }
            }
        } );

        return treeNode;
    }

    public static TreeNode getQAStructure(final ExplorerViewCenterPanel centerPanel) {

        final TreeNode treeNode = new TreeNode();
        treeNode.setText( constants.QA() );

        final TreeNode scenarios = new TreeNode();
        scenarios.setText( constants.TestScenariosInPackages() );
        scenarios.setIcon( "images/test_manager.gif" ); //NON-NLS

        final EditItemEvent edit = new EditItemEvent() {
            public void open(String key) {
                centerPanel.openAsset( key );
            }

            public void open(MultiViewRow[] rows) {
                for ( MultiViewRow row : rows ) {
                    centerPanel.openAsset( row.uuid );
                }
            }
        };

        scenarios.appendChild( new TreeNode( constants.PleaseWaitDotDotDot() ) );
        treeNode.appendChild( scenarios );

        final TreeNode analysis = new TreeNode();
        analysis.setText( constants.Analysis() );
        analysis.setIcon( "images/analyze.gif" ); //NON-NLS
        analysis.setExpanded( false );
        analysis.appendChild( new TreeNode( constants.PleaseWaitDotDotDot() ) );

        if ( Preferences.getBooleanPref( "verifier" ) ) {
            treeNode.appendChild( analysis );
        }

        scenarios.addListener( new TreeNodeListenerAdapter() {
            public void onExpand(Node node) {

                RepositoryServiceFactory.getService().listPackages( new GenericCallback<PackageConfigData[]>() {
                    public void onSuccess(PackageConfigData[] conf) {
                        for ( int i = 0; i < conf.length; i++ ) {
                            final PackageConfigData c = conf[i];
                            TreeNode pkg = new TreeNode();
                            pkg.setText( c.name );
                            pkg.setIcon( "images/package.gif" ); //NON-NLS

                            scenarios.appendChild( pkg );
                            pkg.addListener( new TreeNodeListenerAdapter() {
                                public void onClick(Node node,
                                                    EventObject e) {
                                    if ( !centerPanel.showIfOpen( "scenarios" + c.uuid ) ) { //NON-NLS
                                        String m = Format.format( constants.ScenariosForPackage(),
                                                                  c.name );
                                        centerPanel.addTab( m,
                                                            true,
                                                            new ScenarioPackageView( c.uuid,
                                                                                     c.name,
                                                                                     edit,
                                                                                     centerPanel ),
                                                            "scenarios" + c.uuid ); //NON-NLS
                                    }
                                }
                            } );
                        }
                        scenarios.removeChild( scenarios.getFirstChild() );

                    }
                } );
            }

            public void onCollapse(Node node) {
                Node[] cs = node.getChildNodes();
                for ( int i = 0; i < cs.length; i++ ) {
                    node.removeChild( cs[i] );
                }
                node.appendChild( new TreeNode( constants.PleaseWaitDotDotDot() ) );
            }
        } );

        analysis.addListener( new TreeNodeListenerAdapter() {

            public void onExpand(Node node) {
                RepositoryServiceFactory.getService().listPackages( new GenericCallback<PackageConfigData[]>() {
                    public void onSuccess(PackageConfigData[] conf) {

                        for ( int i = 0; i < conf.length; i++ ) {
                            final PackageConfigData c = conf[i];
                            TreeNode pkg = new TreeNode();
                            pkg.setText( c.name );
                            pkg.setIcon( "images/package.gif" ); //NON-NLS

                            analysis.appendChild( pkg );
                            pkg.addListener( new TreeNodeListenerAdapter() {
                                public void onClick(Node node,
                                                    EventObject e) {
                                    if ( !centerPanel.showIfOpen( "analysis" + c.uuid ) ) { //NON-NLS
                                        final EditItemEvent edit = new EditItemEvent() {
                                            public void open(String key) {
                                                centerPanel.openAsset( key );
                                            }

                                            public void open(MultiViewRow[] rows) {
                                                for ( MultiViewRow row : rows ) {
                                                    centerPanel.openAsset( row.uuid );
                                                }
                                            }
                                        };
                                        String m = Format.format( constants.AnalysisForPackage(),
                                                                  c.name );
                                        centerPanel.addTab( m,
                                                            true,
                                                            new AnalysisView( c.uuid,
                                                                              c.name,
                                                                              edit ),
                                                            "analysis" + c.uuid ); //NON-NLS
                                    }
                                }
                            } );
                        }
                        analysis.removeChild( analysis.getFirstChild() );

                    }
                } );
            }

            public void onCollapse(Node node) {
                Node[] cs = node.getChildNodes();
                for ( int i = 0; i < cs.length; i++ ) {
                    node.removeChild( cs[i] );
                }
                node.appendChild( new TreeNode( constants.PleaseWaitDotDotDot() ) );
            }
        } );

        return treeNode;
    }

}

package org.drools.brms.client.admin;

import org.drools.brms.client.common.FormStyleLayout;
import org.drools.brms.client.common.GenericCallback;
import org.drools.brms.client.common.LoadingPopup;
import org.drools.brms.client.rpc.RepositoryServiceFactory;
import org.drools.brms.client.rpc.TableDataResult;
import org.drools.brms.client.rulelist.AssetItemListViewer;
import org.drools.brms.client.rulelist.EditItemEvent;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Fernando Meyer
 */

public class ArchivedAssetManager extends Composite {

    AssetItemListViewer listView;
    HorizontalPanel           layout;

    public ArchivedAssetManager() {

        FormStyleLayout widtab = new FormStyleLayout( "images/backup_large.png",
                                                      "Manage Archived Assets" );

        layout = new HorizontalPanel();
        layout.setWidth( "100%" );

        widtab.addRow( layout );
        
        listView = new AssetItemListViewer (new EditItemEvent () {
            public void open(String key) {
            }
        }, AssetItemListViewer.ARCHIVED_RULE_LIST_TABLE_ID );
        
        listView.setRefreshCommand( showArchivedAssets() );
        
        layout.add( listView );

        showArchivedAssets().execute();
        widtab.addRow( new HTML( "<hr/>" ) );
        widtab.addRow( newButtonsActionWiget() );
        initWidget( widtab );
    }

    
    private Widget newButtonsActionWiget() {

        HorizontalPanel horiz = new HorizontalPanel();
        
        Button refresh = new Button( "Refresh" );
        refresh.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                showArchivedAssets().execute();
            }
        } );
        

        Button unarchive = new Button( "Unarchive" );
        unarchive.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                
                RepositoryServiceFactory.getService().archiveAsset( listView.getSelectedElementUUID(), false, new AsyncCallback() {

                    public void onFailure(Throwable arg0) {
                        Window.alert( "Fail" );
                    }

                    public void onSuccess(Object arg0) {
                        showArchivedAssets().execute();
                        Window.alert( "Done!" );
                    }
                });
            }
        } );

        Button deleteperm = new Button( "Delete" );
        deleteperm.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                RepositoryServiceFactory.getService().removeAsset( listView.getSelectedElementUUID(), new AsyncCallback() {

                    public void onFailure(Throwable arg0) {
                        Window.alert( "Fail" );
                    }

                    public void onSuccess(Object arg0) {
                        showArchivedAssets().execute();
                        Window.alert( "Done!" );
                        
                    }
                });
            }
        } );

        horiz.add( refresh );
        horiz.add( unarchive );
        horiz.add( deleteperm );
        return horiz;
    }

    private Command showArchivedAssets() {

        final GenericCallback cb = new GenericCallback() {
            public void onSuccess(Object data) {
                final TableDataResult table = (TableDataResult) data;
                listView.loadTableData( table );
                listView.setWidth( "100%" );
                LoadingPopup.close();
            }
        };

        return new Command() {
            public void execute() {
                LoadingPopup.showMessage( "Loading list, please wait..." );
                RepositoryServiceFactory.getService().loadArchivedAssets( cb );
            }
        };
    }
}
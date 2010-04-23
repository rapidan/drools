package org.drools.guvnor.client.explorer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.packages.PackageEditor;
import org.drools.guvnor.client.packages.SnapshotView;
import org.drools.guvnor.client.packages.SuggestionCompletionCache;
import org.drools.guvnor.client.rpc.PackageConfigData;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.SnapshotInfo;
import org.drools.guvnor.client.ruleeditor.GuvnorEditor;
import org.drools.guvnor.client.ruleeditor.MultiViewEditor;
import org.drools.guvnor.client.ruleeditor.MultiViewRow;
import org.drools.guvnor.client.ruleeditor.RuleViewer;
import org.drools.guvnor.client.rulelist.EditItemEvent;
import org.drools.guvnor.client.rulelist.QueryWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Margins;
import com.gwtext.client.core.RegionPosition;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Container;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.PanelListenerAdapter;
import com.gwtext.client.widgets.event.TabPanelListenerAdapter;
import com.gwtext.client.widgets.layout.BorderLayoutData;

/**
 * This is the tab panel manager.
 * @author Fernando Meyer, Michael Neale
 */
public class ExplorerViewCenterPanel {

    final TabPanel                      tp;

    private MultiKeyMap<Panel>          openedTabs           = new MultiKeyMap<Panel>();
    private String                      id                   = Ext.generateId();
    private BorderLayoutData            centerLayoutData;

    /** to keep track of what is dirty, filthy */
    private Map<String, GuvnorEditor>   openedAssetEditors   = new HashMap<String, GuvnorEditor>();
    private Map<String, PackageEditor> openedPackageEditors = new HashMap<String, PackageEditor>();

    private Button                      closeAllButton;
    private Constants                   constants            = ((Constants) GWT.create( Constants.class ));

    public ExplorerViewCenterPanel() {
        tp = new TabPanel();

        tp.setBodyBorder( false );
        tp.setEnableTabScroll( true );
        tp.setAutoDestroy( true );
        tp.setResizeTabs( true );
        tp.setLayoutOnTabChange( true );
        tp.setActiveTab( 0 );
        tp.setEnableTabScroll( true );
        tp.setMinTabWidth( 90 );

        centerLayoutData = new BorderLayoutData( RegionPosition.CENTER );
        centerLayoutData.setMargins( new Margins( 5,
                                                  0,
                                                  5,
                                                  5 ) );

        //listener to try and stop people from forgetting to save...
        tp.addListener( new TabPanelListenerAdapter() {
            @Override
            public boolean doBeforeRemove(Container self,
                                          final Component component) {

                if ( openedAssetEditors.containsKey( component.getId() ) ) {

                    GuvnorEditor rv = openedAssetEditors.get( component.getId() );
                    if ( rv.isDirty() ) {
                        component.show();
                        return Window.confirm( constants.AreYouSureCloseWarningUnsaved() );
                    } else {
                        return true;
                    }
                }
                return true;
            }
        } );

        addCloseAllButton();

    }

    private void addCloseAllButton() {
        closeAllButton = new Button( constants.CloseAllItems() );
        closeAllButton.addListener( new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button,
                                EventObject e) {
                if ( Window.confirm( constants.AreYouSureYouWantToCloseOpenItems() ) ) {
                    tp.clear();
                    openedAssetEditors.clear();
                    openedPackageEditors.clear();
                    openedTabs.clear();
                    openFind();
                }
            }
        } );
        tp.addButton( closeAllButton );
    }

    public TabPanel getPanel() {
        return tp;
    }

    /**
     * Add a new tab. Should only do this if have checked showIfOpen to avoid dupes being opened.
     * @param tabname The displayed tab name.
     * @param closeable If you can close it !
     * @param widget The contents.
     * @param key A key which is unique.
     */
    public void addTab(final String tabname,
                       boolean closeable,
                       Widget widget,
                       final String key) {
        addTab( tabname,
                closeable,
                widget,
                new String[]{key} );
    }

    /**
     * Add a new tab. Should only do this if have checked showIfOpen to avoid dupes being opened.
     * @param tabname The displayed tab name.
     * @param closeable If you can close it !
     * @param widget The contents.
     * @param keys An array of keys which are unique.
     */
    public void addTab(final String tabname,
                       boolean closeable,
                       Widget widget,
                       final String[] keys) {

        final String panelId = (keys.length == 1 ? keys[0] + id : Arrays.toString( keys ) + id);
        Panel localTP = new Panel();
        localTP.setClosable( closeable );
        localTP.setTitle( tabname );
        localTP.setId( panelId );
        localTP.setAutoScroll( true );
        localTP.add( widget );
        tp.add( localTP,
                this.centerLayoutData );

        localTP.addListener( new PanelListenerAdapter() {
            public void onDestroy(Component component) {
                Panel p = openedTabs.remove( keys );
                if ( p != null ) {
                    p.destroy();
                }
                openedAssetEditors.remove( panelId );
                openedPackageEditors.remove( tabname );
            }
        } );

        if ( widget instanceof GuvnorEditor ) {
            this.openedAssetEditors.put( panelId,
                                         (GuvnorEditor) widget );
        } else if ( widget instanceof PackageEditor ) {
            this.openedPackageEditors.put( tabname,
                                           (PackageEditor) widget );
        }

        tp.activate( localTP.getId() );

        openedTabs.put( keys,
                        localTP );

    }

    /**
     * Will open if existing. If not it will return false;
     */
    public boolean showIfOpen(String key) {
        if ( openedTabs.containsKey( key ) ) {
            LoadingPopup.close();

            Panel tpi = (Panel) openedTabs.get( key );
            this.tp.activate( tpi.getId() );

            return true;
        } 
        return false;
    }

    public void close(String key) {
        tp.remove( key + id );
        Panel p = openedTabs.remove( key );
        if ( p != null ) p.destroy();
    }

    /**
     * Open an asset if it is not already open.
     */
	public void openAsset(final String uuid) {
		if (uuid.contains("<")) {
			return;
		}
		History.newItem("asset=" + uuid); // NON-NLS

		if (!showIfOpen(uuid)) {

			final boolean[] loading = { true };

			Timer t = new Timer() {
				public void run() {
					if (loading[0]) {
						LoadingPopup.showMessage(constants.LoadingAsset());
					}
				}
			};
			t.schedule(200);

			RepositoryServiceFactory.getService().loadRuleAsset(uuid, new GenericCallback<RuleAsset>() {
				public void onSuccess(final RuleAsset a) {
					SuggestionCompletionCache.getInstance().doAction(a.metaData.packageName, new Command() {
						public void execute() {
							loading[0] = false;
							EditItemEvent edit = new EditItemEvent() {
								public void open(String key) {
									openAsset(key);
								}

								public void open(MultiViewRow[] rows) {
									for (MultiViewRow row : rows) {
										openAsset(row.uuid);
									}
								}
							};
							RuleViewer rv = new RuleViewer(a, edit);
							addTab(a.metaData.name, true, rv, uuid);
							rv.setCloseCommand(new Command() {
								public void execute() {
									close(uuid);
								}
							});

							// When model is saved update the package view if it is
							// opened.
							if (a.metaData.format.equals(AssetFormats.MODEL)) {
							    Command command =new Command() {
                                    public void execute() {
                                        PackageEditor packageEditor = openedPackageEditors.get(a.metaData.packageName);
                                        if (packageEditor != null) {
                                            packageEditor.reload();
                                        }
                                    }
                                };
								rv.setCheckedInCommand( command );
								rv.setArchiveCommand( command );
							}

							LoadingPopup.close();
						}
					});

				}
			});
		}
	}

    public void openAssets(MultiViewRow[] rows) {

        String blockingAssetName = null;
        final String[] uuids = new String[rows.length];
        final String[] names = new String[rows.length];

        for ( int i = 0; i < rows.length; i++ ) {
            // Check if any of these assets are already opened.
            if ( showIfOpen( rows[i].uuid ) ) {
                blockingAssetName = rows[i].name;
                break;
            }
            uuids[i] = rows[i].uuid;
            names[i] = rows[i].name;
        }

        if ( blockingAssetName != null ) {
            FormStylePopup popup = new FormStylePopup( "images/information.gif", //NON-NLS
                                                       Format.format( constants.Asset0IsAlreadyOpenPleaseCloseItBeforeOpeningMultiview(),
                                                                      blockingAssetName ) );
            popup.show();
            return;
        }

        MultiViewEditor multiview = new MultiViewEditor( rows,
                                                         new EditItemEvent() {
                                                             public void open(String key) {
                                                                 openAsset( key );
                                                             }

                                                             public void open(MultiViewRow[] rows) {
                                                                 for ( MultiViewRow row : rows ) {
                                                                     openAsset( row.uuid );
                                                                 }
                                                             }
                                                         } );

        multiview.setCloseCommand( new Command() {
            public void execute() {
                close( Arrays.toString( uuids ) );
            }
        } );

        addTab( Arrays.toString( names ),
                true,
                multiview,
                uuids );

    }

    /**
     * Open a package editor if it is not already open.
     */
	public void openPackageEditor(final String uuid, final Command refPackageList) {

		if (!showIfOpen(uuid)) {
			LoadingPopup.showMessage(constants.LoadingPackageInformation());
			RepositoryServiceFactory.getService().loadPackageConfig(uuid, new GenericCallback<PackageConfigData>() {
				public void onSuccess(PackageConfigData conf) {
					PackageEditor ed = new PackageEditor(conf, new Command() {
						public void execute() {
							close(uuid);
						}
					}, refPackageList, new EditItemEvent() {
						public void open(String uuid) {
							openAsset(uuid);
						}

						public void open(MultiViewRow[] rows) {
							for (MultiViewRow row : rows) {
								openAsset(row.uuid);
							}
						}
					});
					addTab(conf.name, true, ed, conf.uuid);
					LoadingPopup.close();
				}
			});
		}
	}

    public void openFind() {
        if ( !showIfOpen( "FIND" ) ) { //NON-NLS
            this.addTab( constants.Find(),
                         true,
                         new QueryWidget( new EditItemEvent() {
                             public void open(String uuid) {
                                 openAsset( uuid );
                             }

                             public void open(MultiViewRow[] rows) {
                                 for ( MultiViewRow row : rows ) {
                                     openAsset( row.uuid );
                                 }
                             }
                         } ),
                         "FIND" ); //NON-NLS

        }
    }

    public void openSnapshot(final SnapshotInfo snap) {
        //make this refresh the 'snap'

        if ( !showIfOpen( snap.name + snap.uuid ) ) {
            LoadingPopup.showMessage( constants.LoadingSnapshot() );
            RepositoryServiceFactory.getService().loadPackageConfig( snap.uuid,
                                                                     new GenericCallback<PackageConfigData>() {
                                                                         public void onSuccess(PackageConfigData conf) {
                                                                             addTab( Format.format( constants.SnapshotLabel(),
                                                                                                    snap.name ),
                                                                                     true,
                                                                                     new SnapshotView( snap,
                                                                                                       conf,
                                                                                                       new Command() {
                                                                                                           public void execute() {
                                                                                                               close( snap.name + snap.uuid );
                                                                                                           }
                                                                                                       },
                                                                                                       ExplorerViewCenterPanel.this ),
                                                                                     snap.name + snap.uuid );
                                                                             LoadingPopup.close();
                                                                         }
                                                                     } );

        }
    }

}

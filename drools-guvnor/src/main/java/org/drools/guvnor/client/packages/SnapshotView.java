package org.drools.guvnor.client.packages;

import java.util.ArrayList;
import java.util.List;

import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.PrettyFormLayout;
import org.drools.guvnor.client.common.RulePackageSelector;
import org.drools.guvnor.client.explorer.ExplorerNodeConfig;
import org.drools.guvnor.client.explorer.ExplorerViewCenterPanel;
import org.drools.guvnor.client.explorer.GenericPanel;
import org.drools.guvnor.client.rpc.PackageConfigData;
import org.drools.guvnor.client.rpc.RepositoryServiceAsync;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rpc.SnapshotInfo;
import org.drools.guvnor.client.rulelist.AssetItemGrid;
import org.drools.guvnor.client.rulelist.AssetItemGridDataLoader;
import org.drools.guvnor.client.rulelist.EditItemEvent;
import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.tree.TreeNode;
import com.gwtext.client.widgets.tree.TreePanel;
import com.gwtext.client.widgets.tree.event.TreePanelListenerAdapter;
import com.gwtext.client.util.Format;

/**
 * This is the new snapshot view.
 * @author Michael Neale
 *
 */
public class SnapshotView extends Composite {


    public static final String LATEST_SNAPSHOT = "LATEST";

    private PackageConfigData parentConf;
    private SnapshotInfo snapInfo;

    private Command close;

    private ExplorerViewCenterPanel centerPanel;
    private static Constants constants = ((Constants) GWT.create(Constants.class));

    public SnapshotView(SnapshotInfo snapInfo, PackageConfigData parentPackage, Command closeSnap, ExplorerViewCenterPanel center) {

        VerticalPanel vert = new VerticalPanel();
        this.snapInfo = snapInfo;
        this.parentConf = parentPackage;
        this.close = closeSnap;
        PrettyFormLayout head = new PrettyFormLayout();

        head.addHeader("images/snapshot.png", header());

        this.centerPanel = center;

        vert.add(head);




        vert.add(infoPanel());


        vert.setWidth("100%");
        initWidget(vert);

    }

    private Widget header() {
        FlexTable ft = new FlexTable();




        ft.setWidget(0, 0, new Label(constants.ViewingSnapshot()));
        ft.setWidget(0, 1, new HTML("<b>" + this.snapInfo.name + "</b>"));
        ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        ft.setWidget(1, 0, new Label(constants.ForPackage()));
        ft.setWidget(1, 1, new Label(this.parentConf.name));
        ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        HTML dLink = new HTML("<a href='" + PackageBuilderWidget.getDownloadLink(this.parentConf)
                + "' target='_blank'>" + constants.clickHereToDownloadBinaryOrCopyURLForDeploymentAgent() + "</a>");
        ft.setWidget(2, 0, new Label(constants.DeploymentURL()));
        ft.setWidget(2, 1, dLink);
        ft.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        ft.setWidget(3, 0, new Label(constants.SnapshotCreatedOn()));
        ft.setWidget(3, 1, new Label( parentConf.lastModified.toLocaleString() ));
        ft.getFlexCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        ft.setWidget(4,0, new Label(constants.CommentColon()));
        ft.setWidget(4,1, new Label( parentConf.checkinComment ));
        ft.getFlexCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        HorizontalPanel actions = new HorizontalPanel();

        actions.add(getDeleteButton(this.snapInfo.name, this.parentConf.name));
        actions.add(getCopyButton(this.snapInfo.name, this.parentConf.name));


        ft.setWidget(5, 0, actions);
        ft.getFlexCellFormatter().setColSpan(5, 0, 2);



        return ft;
    }

    private Button getDeleteButton(final String snapshotName, final String pkgName) {
        Button btn = new Button(constants.Delete());
        btn.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                if (Window.confirm(Format.format(constants.SnapshotDeleteConfirm(), snapshotName, pkgName))) {
                    RepositoryServiceFactory.getService().copyOrRemoveSnapshot( pkgName, snapshotName, true, null, new GenericCallback() {
                        public void onSuccess(Object data) {
                            close.execute();
                            Window.alert(constants.SnapshotWasDeleted());

                        }
                    });
                }
            }

        });
        return btn;
    }

    private Button getCopyButton(final String snapshotName, final String packageName) {
        final RepositoryServiceAsync serv = RepositoryServiceFactory.getService();
        Button btn = new Button(constants.Copy());
        btn.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                serv.listSnapshots(packageName, new GenericCallback<SnapshotInfo[]>() {
                    public void onSuccess(final SnapshotInfo[] snaps) {
                        final FormStylePopup copy = new FormStylePopup("images/snapshot.png", Format.format(constants.CopySnapshotText(), snapshotName));
                        final TextBox box = new TextBox();
                        final List<RadioButton> options = new ArrayList<RadioButton>();
                        VerticalPanel vert = new VerticalPanel();
						for (int i = 0; i < snaps.length; i++) {
							RadioButton existing = new RadioButton(
									"snapshotNameGroup", snaps[i].name);   //NON-NLS
							options.add(existing);
							vert.add(existing);
						}
						final RadioButton newBut = new RadioButton("snapshotNameGroup", "NEW NAME");   //NON-NLS
						vert.add(newBut);

						copy.addAttribute(constants.ExistingSnapshots(), vert);
						copy.addAttribute(constants.NewSnapshotNameIs(), box );
                        Button ok = new Button(constants.OK());
                        copy.addAttribute( "", ok );
                        ok.addClickListener( new ClickListener() {
                            public void onClick(Widget w) {
                            	if (newBut.isChecked()) {
                            		if (checkUnique(snaps, box.getText())) {
		                                serv.copyOrRemoveSnapshot( packageName, snapshotName, false, box.getText(), new GenericCallback() {
		                                    public void onSuccess(Object data) {
		                                        copy.hide();
                                                Window.alert(Format.format(constants.CreatedSnapshot0ForPackage1(), box.getText(), packageName));
		                                    }
		                                });
                            		}
                            	} else {
                            		for (RadioButton rb : options) {
                            			if (rb.isChecked()) {
                            				final String newName = rb.getText();
    		                                serv.copyOrRemoveSnapshot( packageName, snapshotName, false, newName, new GenericCallback() {
    		                                    public void onSuccess(Object data) {
    		                                        copy.hide();
                                                    Window.alert(Format.format(constants.Snapshot0ForPackage1WasCopiedFrom2(), newName, packageName, snapshotName));
    		                                    }
    		                                });
                            			}
                            		}
                            	}
                            }

							private boolean checkUnique(SnapshotInfo[] snaps,
									String name) {
								for(SnapshotInfo sn : snaps) {
									if (sn.name.equals(name)) {
										Window.alert(constants.PleaseEnterANonExistingSnapshotName());
										return false;
									}
								}
								return true;
							}
                        } );
                        copy.show();
                    }
                });
            }
        });
        return btn;
    }



    private Widget infoPanel() {
        return packageTree();
    }


    protected Widget packageTree() {
        TreeNode pkg = ExplorerNodeConfig.getPackageItemStructure(parentConf.name, snapInfo.uuid);
        pkg.setUserObject(snapInfo);
        TreeNode root = new TreeNode(snapInfo.name);
        root.appendChild(pkg);
        TreePanel tp = GenericPanel.genericExplorerWidget(root);
        tp.setRootVisible(false);
        tp.addListener(new TreePanelListenerAdapter() {

            public void onClick(TreeNode node, EventObject e) {
                Object uo = node.getUserObject();
                if (uo instanceof Object[]) {
                    Object o = ((Object[]) uo)[0];
                    showAssetList((String[]) o);
                } else if (uo instanceof SnapshotInfo) {
                    SnapshotInfo s = (SnapshotInfo) uo;
                    //todo - add snap notice to this..
                    centerPanel.openPackageEditor(s.uuid, null);
                }

            }
        });
        return tp;

    }

    protected void showAssetList(final String[] assetTypes) {


        String key = this.snapInfo.uuid;
        for (int i = 0; i < assetTypes.length; i++) {
            key = key + assetTypes[i];
        }

        if (!centerPanel.showIfOpen(key)) {
            AssetItemGrid grid = new AssetItemGrid(new EditItemEvent() {
                public void open(String key) {
                    //todo add snap notice to this...
                    centerPanel.openAsset(key);
                }
            }, AssetItemGrid.RULE_LIST_TABLE_ID, new AssetItemGridDataLoader() {
                public void loadData(int startRow, int numberOfRows,
                        GenericCallback cb) {
                    RepositoryServiceFactory.getService().listAssets(snapInfo.uuid, assetTypes, startRow, numberOfRows, AssetItemGrid.RULE_LIST_TABLE_ID , cb);
                }
            });

            VerticalPanel vp = new VerticalPanel();
            vp.add(new HTML("<i><small>" + constants.SnapshotListingFor() + this.snapInfo.name + "</small></i>"));
            vp.add(grid);
            centerPanel.addTab(constants.SnapshotItems(), true, vp, key);
        }



    }

    public static void showNewSnapshot() {
        final FormStylePopup pop = new FormStylePopup("images/snapshot.png", ((Constants) GWT.create(Constants.class)).NewSnapshot());
        final RulePackageSelector sel = new RulePackageSelector();

        pop.addAttribute(constants.ForPackage(), sel);
        Button ok = new Button(constants.OK());
        pop.addAttribute("", ok);
        pop.show();

        ok.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                pop.hide();
                String pkg = sel.getSelectedPackage();
                PackageBuilderWidget.showSnapshotDialog(pkg);
            }
        });


    }

    public static void rebuildBinaries() {
        if (Window.confirm(constants.SnapshotRebuildWarning())) {
            LoadingPopup.showMessage(constants.RebuildingSnapshotsPleaseWaitThisMayTakeSomeTime());
            RepositoryServiceFactory.getService().rebuildSnapshots( new GenericCallback() {
                public void onSuccess(Object data) {
                    LoadingPopup.close();
                    Window.alert(constants.SnapshotsWereRebuiltSuccessfully());
                }
            });
        }
    }

}

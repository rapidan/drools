package org.drools.guvnor.client.qa;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.PrettyFormLayout;
import org.drools.guvnor.client.explorer.ExplorerViewCenterPanel;
import org.drools.guvnor.client.rpc.BulkTestRunResult;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rulelist.AssetItemGrid;
import org.drools.guvnor.client.rulelist.AssetItemGridDataLoader;
import org.drools.guvnor.client.rulelist.EditItemEvent;
import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;

/**
 * This shows a list of scenarios in a package.
 * And allows them to be run in bulk.
 * @author Michael Neale
 */
public class ScenarioPackageView extends Composite {


	private EditItemEvent editEvent;

	private VerticalPanel layout;

	private AssetItemGrid grid;
    private Constants constants = ((Constants) GWT.create(Constants.class));

    public ScenarioPackageView(final String packageUUID, String packageName, EditItemEvent editEvent, ExplorerViewCenterPanel centerPanel) {
		this.editEvent = editEvent;

		grid = new AssetItemGrid(editEvent, AssetItemGrid.RULE_LIST_TABLE_ID, new AssetItemGridDataLoader() {
			public void loadData(int startRow, int numberOfRows,
					GenericCallback cb) {
				RepositoryServiceFactory.getService().listAssets(packageUUID, new String[] {AssetFormats.TEST_SCENARIO},
						startRow, numberOfRows,AssetItemGrid.RULE_LIST_TABLE_ID, cb);
			}
		});

		layout = new VerticalPanel();
		layout.setWidth("100%");
		PrettyFormLayout pf = new PrettyFormLayout();

		VerticalPanel vert = new VerticalPanel();
		vert.add(new HTML("<b>" + constants.ScenariosForPackage1() + "</b>" + packageName));
		Button run = new Button(constants.RunAllScenarios());
		run.addClickListener(new ClickListener() {
			public void onClick(Widget w) {
				runAllScenarios(packageUUID);
			}
		});

		vert.add(run);


		pf.addHeader("images/scenario_large.png", vert); //NON-NLS

		layout.add(pf);
		layout.add(grid);

		initWidget(layout);




	}

	private void refreshShowGrid() {
		layout.remove(1);
		layout.add(grid);
	}


	/**
	 * Run all the scenarios, obviously !
	 */
	private void runAllScenarios(String uuid) {
		LoadingPopup.showMessage(constants.BuildingAndRunningScenarios());
		RepositoryServiceFactory.getService().runScenariosInPackage(uuid, new GenericCallback<BulkTestRunResult>() {
			public void onSuccess(BulkTestRunResult d) {
				BulkRunResultWidget w = new BulkRunResultWidget(d, editEvent, new Command() {
					public void execute() {
						refreshShowGrid();
					}

				});
				layout.remove(1);
				layout.add(w);
				LoadingPopup.close();
			}
		});
	}

}

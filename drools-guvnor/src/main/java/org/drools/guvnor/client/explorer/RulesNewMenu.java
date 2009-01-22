package org.drools.guvnor.client.explorer;

import org.drools.guvnor.client.common.AssetFormats;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;

/**
 * TODO: this class should be generated via ant task:  'ant plug-editors'
 *
 * @author ant plug-editors
 */
public class RulesNewMenu {

    public static Menu getMenu(final GenericPanel manager) {
        Menu m = new Menu();

        m.addItem(new Item("Business Rule (Guided editor)", new BaseItemListenerAdapter() {
            public void onClick(BaseItem item, EventObject e) {
                manager.launchWizard(AssetFormats.BUSINESS_RULE, "New Business Rule (Guided editor)", true);
            }
        }, "images/business_rule.gif"));


        m.addItem(new Item("DSL Business Rule (Text editor)", new BaseItemListenerAdapter() {
            public void onClick(BaseItem item, EventObject e) {
                manager.launchWizard(AssetFormats.DSL_TEMPLATE_RULE, "New Rule using DSL", true);
            }
        }, "images/business_rule.gif"));


        m.addItem(new Item("DRL Rule (Technical rule - text editor)", new BaseItemListenerAdapter() {
            public void onClick(BaseItem item, EventObject e) {
                manager.launchWizard(AssetFormats.DRL, "New DRL", true);
            }
        }, "images/rule_asset.gif"));

        m.addItem(new Item("Decision Table (Spreadsheet)", new BaseItemListenerAdapter() {
            public void onClick(BaseItem item, EventObject e) {
                manager.launchWizard(AssetFormats.DECISION_SPREADSHEET_XLS, "New Decision Table (Spreadsheet)", true);
            }
        }, "images/spreadsheet_small.gif"));

        m.addItem(new Item("Decision Table (Web - guided editor)", new BaseItemListenerAdapter() {
            public void onClick(BaseItem item, EventObject e) {
                manager.launchWizard(AssetFormats.DECISION_TABLE_GUIDED, "New Decision Table (Guided editor)", true);
            }
        }, "images/gdst.gif"));

        m.addItem(new Item("Test Scenario", new BaseItemListenerAdapter() {
            public void onClick(BaseItem item, EventObject e) {
                manager.launchWizard(AssetFormats.TEST_SCENARIO,
                        "Create a test scenario.", false);
            }
        }, "images/test_manager.gif"));

        return m;
    }


}

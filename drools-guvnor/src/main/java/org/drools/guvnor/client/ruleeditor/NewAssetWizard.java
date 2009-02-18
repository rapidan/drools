package org.drools.guvnor.client.ruleeditor;
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



import org.drools.guvnor.client.categorynav.CategoryExplorerWidget;
import org.drools.guvnor.client.categorynav.CategorySelectHandler;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.RulePackageSelector;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rulelist.EditItemEvent;
import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;
import com.gwtext.client.util.Format;

/**
 * This provides a popup for creating a new rule/asset from scratch.
 * reuses a few other widgets.
 */
public class NewAssetWizard extends FormStylePopup {

    private Constants constants = GWT.create(Constants.class);
    
    private TextBox                name        = new TextBox();
    private TextArea               description = new TextArea();
    private String                 initialCategory;
    
    private ListBox                 formatChooser = getFormatChooser();

    private RulePackageSelector packageSelector = new RulePackageSelector();
    private EditItemEvent afterCreate;
    private boolean showCats;
    private String format;


    /** This is used when creating a new rule. */
    public NewAssetWizard(EditItemEvent afterCreate, boolean showCats, String format, String title) {
        super("images/new_wiz.gif", title); //NON-NLS
        this.showCats = showCats;
        this.format = format;

        this.afterCreate = afterCreate;


        addAttribute( constants.Name(), name );

        this.setAfterShow(new Command() {
			public void execute() {
				name.setFocus(true);			}
        });


        if (showCats) {

            addAttribute(constants.InitialCategory(), getCatChooser());
        }

        if (format == null) {
            addAttribute(constants.TypeFormatOfRule(), this.formatChooser );
        } else if (format == "*") { //NON-NLS
        	final TextBox fmt = new TextBox();
        	addAttribute(constants.FileExtensionTypeFormat(), fmt);
        	fmt.addChangeListener(new ChangeListener() {
				public void onChange(Widget w) {
					NewAssetWizard.this.format = fmt.getText();
				}
        	});
        }

        addAttribute(constants.Package() + ":", packageSelector);

        description.setVisibleLines( 4 );
        description.setWidth( "100%" );
        //initial description
        if (format == AssetFormats.DSL_TEMPLATE_RULE) {
        	description.setText(constants.DSLMappingTip());
        } else if (format == AssetFormats.ENUMERATION) {
        	description.setText(constants.NewEnumDoco());
        }

        addAttribute(constants.InitialDescription(), description);

        Button ok = new Button( constants.OK() );
        ok.addClickListener( new ClickListener() {
            public void onClick(Widget arg0) {
                ok();
            }

        } );

        addAttribute( "", ok );

    }


    /**
     * This will create a new asset wizard with the given preselected package.
     */
    public NewAssetWizard(
                          EditItemEvent event, boolean showCategories, String format2, String title, String currentlySelectedPackage) {
        this(event, showCategories, format2, title);
        packageSelector.selectPackage(currentlySelectedPackage);

    }

    private Widget getCatChooser() {

       Widget w = new CategoryExplorerWidget( new CategorySelectHandler() {
            public void selected(String selectedPath) {
                initialCategory = selectedPath;
            }
        });
       ScrollPanel scroll = new ScrollPanel(w);
       scroll.setAlwaysShowScrollBars(true);
       scroll.setSize("300px", "130px"); //NON-NLS
       return scroll;

    }

    private ListBox getFormatChooser() {

        ListBox box = new ListBox();

        box.addItem(constants.BusinessRuleGuidedEditor(), AssetFormats.BUSINESS_RULE );
        box.addItem(constants.DSLBusinessRuleTextEditor(), AssetFormats.DSL_TEMPLATE_RULE );
        box.addItem(constants.DRLRuleTechnicalRuleTextEditor(), AssetFormats.DRL );
        box.addItem(constants.DecisionTableSpreadsheet(), AssetFormats.DECISION_SPREADSHEET_XLS );
        box.addItem(constants.DecisionTableWebGuidedEditor(), AssetFormats.DECISION_TABLE_GUIDED );

        box.setSelectedIndex( 0 );

        return box;
    }

    /**
     * When OK is pressed, it will update the repository with the new rule.
     */
    void ok() {

        if (this.showCats && this.initialCategory == null) {
			Window.alert(constants.YouHaveToPickAnInitialCategory());
			return;
		} else {
			if (!validatePathPerJSR170(this.name.getText())) return;
		}

        String fmt = getFormat();
        if (fmt == null || fmt.equals("*")) {
        	Window.alert(constants.PleaseEnterAFormatFileType());
        	return;
        }

        GenericCallback cb = new GenericCallback() {
            public void onSuccess(Object result) {
            		String uuid = (String) result;
            		if (uuid.startsWith("DUPLICATE")) { //NON-NLS
            			LoadingPopup.close();
            			Window.alert(constants.AssetNameAlreadyExistsPickAnother());
            		} else {
            			openEditor((String) result);
            			hide();
            		}
            }
        };


        LoadingPopup.showMessage( constants.PleaseWaitDotDotDot() );
        RepositoryServiceFactory.getService().createNewRule( name.getText(),
                                                          description.getText(),
                                                          initialCategory,
                                                          packageSelector.getSelectedPackage(),
                                                          getFormat(),
                                                          cb );

    }

    private String getFormat() {
        if (format != null) return format;
        return formatChooser.getValue( formatChooser.getSelectedIndex() );
    }

    /**
     * After creating the item we open it in the editor.
     * @param uuid
     */
    protected void openEditor(String uuid) {
        afterCreate.open( uuid );
    }

    /**
	 * Validate name per JSR-170. Only following characters are valid: char ::=
	 * nonspace | ' ' nonspace ::= (* Any Unicode character except: '/', ':',
	 * '[', ']', '*', ''', '"', '|' or any whitespace character *)
	 *
	 * @param jsrPath
	 */
	public static boolean validatePathPerJSR170(String jsrPath) {
		int len = jsrPath == null ? 0 : jsrPath.length();

		if (len == 0) {
			Window.alert(((Constants) GWT.create(Constants.class)).emptyNameIsNotAllowed());
			return false;
		}

		int pos = 0;

		while (pos < len) {
			char c = jsrPath.charAt(pos);
			pos++;

			switch (c) {
			case '/':
			case ':':
			case '[':
			case ']':
			case '*':
			case '\'':
			case '\"':
                Window.alert(Format.format(((Constants) GWT.create(Constants.class)).NonValidJCRName(), jsrPath, ""+ c));
				return false;
			default:
			}
		}

		return true;
	}

}
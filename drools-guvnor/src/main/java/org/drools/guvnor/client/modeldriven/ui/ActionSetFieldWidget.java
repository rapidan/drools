package org.drools.guvnor.client.modeldriven.ui;
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



import org.drools.guvnor.client.common.*;
import org.drools.guvnor.client.modeldriven.DropDownData;
import org.drools.guvnor.client.modeldriven.HumanReadable;
import org.drools.guvnor.client.modeldriven.SuggestionCompletionEngine;
import org.drools.guvnor.client.modeldriven.brl.ActionFieldValue;
import org.drools.guvnor.client.modeldriven.brl.ActionInsertFact;
import org.drools.guvnor.client.modeldriven.brl.ActionSetField;
import org.drools.guvnor.client.modeldriven.brl.ActionUpdateField;
import org.drools.guvnor.client.modeldriven.brl.FactPattern;
import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;
import com.gwtext.client.util.Format;


/**
 * This widget is for setting fields on a bound fact or global variable.
 *
 * @author Michael Neale
 */
public class ActionSetFieldWidget extends DirtyableComposite {

    final private ActionSetField model;
    final private SuggestionCompletionEngine completions;
    final private DirtyableFlexTable layout;
    private boolean isBoundFact = false;

    private String[] fieldCompletions;
    final private RuleModeller modeller;
    private String variableClass;
    private Constants constants = GWT.create(Constants.class);


    public ActionSetFieldWidget(RuleModeller mod,  ActionSetField set, SuggestionCompletionEngine com) {
        this.model = set;
        this.completions = com;
        this.layout = new DirtyableFlexTable();
        this.modeller = mod;

        layout.setStyleName( "model-builderInner-Background" );
        if (completions.isGlobalVariable( set.variable )) {
            this.fieldCompletions = completions.getFieldCompletionsForGlobalVariable( set.variable );
            this.variableClass = (String) completions.globalTypes.get( set.variable );
        } else {
            FactPattern pattern = mod.getModel().getBoundFact( set.variable );
            if (pattern !=null){
            	this.fieldCompletions = completions.getFieldCompletions( pattern.factType );
            	this.variableClass = pattern.factType;
            	this.isBoundFact = true;
            }else{
            	ActionInsertFact patternRhs = mod.getModel().getRhsBoundFact(set.variable);
            	if (patternRhs!=null){
                	this.fieldCompletions = completions.getFieldCompletions( patternRhs.factType );
                	this.variableClass = patternRhs.factType;
                	this.isBoundFact = true;
            	}
            }
        }

        doLayout();

        initWidget( this.layout );
    }


    private void doLayout() {
        layout.clear();

        //layout.setWidget( 0, 0, getSetterLabel() );

        //DirtyableFlexTable inner = new DirtyableFlexTable();

        for ( int i = 0; i < model.fieldValues.length; i++ ) {
            ActionFieldValue val = model.fieldValues[i];

            layout.setWidget(i, 0, getSetterLabel());
            layout.setWidget( i, 1, fieldSelector(val) );
            layout.setWidget( i, 2, valueEditor(val) );
            final int idx = i;
            Image remove = new ImageButton("images/delete_item_small.gif"); //NON-NLS
            remove.addClickListener( new ClickListener() {
                public void onClick(Widget w) {
                	if (Window.confirm(constants.RemoveThisItem())) {
                            model.removeField( idx );
                            modeller.refreshWidget();
                	}
                }
            });
            layout.setWidget( i, 3, remove );
        }

        if (model.fieldValues.length == 0) {
            HorizontalPanel h = new HorizontalPanel();
            h.add(getSetterLabel());
            h.add(new ImageButton("images/new_item.gif", constants.AddFirstNewField(), new ClickListener() {
                public void onClick(Widget sender) {
                    showAddFieldPopup(sender);
                }
            }));
            layout.setWidget(0, 0, h);
        }

        //layout.setWidget( 0, 1, inner );


    }


    private Widget getSetterLabel() {



        ClickListener clk =  new ClickListener() {
            public void onClick(Widget w) {
                showAddFieldPopup(w);
            }
        };
        String modifyType = "set";
        if (this.model instanceof ActionUpdateField) {
            modifyType = "modify";
        }



        FactPattern fp = this.modeller.getModel().getBoundFact(model.variable);

        String descFact = (fp != null)? this.modeller.getModel().getBoundFact(model.variable).factType + " <b>[" + model.variable + "]</b>" : model.variable;

        String sl = Format.format(constants.setterLabel(), new String[] {HumanReadable.getActionDisplayName(modifyType), descFact});
        return new ClickableLabel(sl, clk);//HumanReadable.getActionDisplayName(modifyType) + " value of <b>[" + model.variable + "]</b>", clk);
    }


    protected void showAddFieldPopup(Widget w) {
        final FormStylePopup popup = new FormStylePopup("images/newex_wiz.gif", constants.AddAField());

        final ListBox box = new ListBox();
        box.addItem( "..." );

        for ( int i = 0; i < fieldCompletions.length; i++ ) {
            box.addItem( fieldCompletions[i] );
        }

        box.setSelectedIndex( 0 );

        popup.addAttribute(constants.AddField(), box );
        box.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                String fieldName = box.getItemText( box.getSelectedIndex() );

                String fieldType = completions.getFieldType( variableClass, fieldName );
                model.addFieldValue( new ActionFieldValue( fieldName, "", fieldType ) );
                modeller.refreshWidget();
                popup.hide();
            }
        });


        popup.show();

    }


    private Widget valueEditor(final ActionFieldValue val) {

    	String type = "";
    	if (this.completions.isGlobalVariable(this.model.variable)) {
    		type = (String) this.completions.globalTypes.get(this.model.variable);
    	} else {
    		type = this.modeller.getModel().getBoundFact(this.model.variable).factType;
    		/*
    		 * to take in account if the using a rhs bound variable
    		 */
    		if (type==null){
        		type = this.modeller.getModel().getRhsBoundFact(this.model.variable).factType;
    		}
    	}

    	DropDownData enums = this.completions.getEnums(type, this.model.fieldValues, val.field);
    	return new ActionValueEditor(val, enums,modeller,val.type);
    }





    private Widget fieldSelector(final ActionFieldValue val) {
        return new SmallLabel(val.field);
    }

    /**
     * This returns true if the values being set are on a fact.
     */
    public boolean isBoundFact() {
        return isBoundFact;
    }

    public boolean isDirty() {
        return layout.hasDirty();
    }

}
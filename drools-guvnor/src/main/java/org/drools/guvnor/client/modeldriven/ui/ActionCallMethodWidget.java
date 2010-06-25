/**
 *
 */
package org.drools.guvnor.client.modeldriven.ui;

import java.util.ArrayList;
import java.util.List;

import org.drools.guvnor.client.common.DirtyableFlexTable;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.modeldriven.HumanReadable;
import org.drools.ide.common.client.modeldriven.DropDownData;
import org.drools.ide.common.client.modeldriven.MethodInfo;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.ActionCallMethod;
import org.drools.ide.common.client.modeldriven.brl.ActionFieldFunction;
import org.drools.ide.common.client.modeldriven.brl.ActionInsertFact;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * This widget is for modifying facts bound to a variable.
 * 
 * @author isabel
 * 
 */
public class ActionCallMethodWidget extends RuleModellerWidget {

    final private ActionCallMethod           model;
    final private DirtyableFlexTable         layout;
    private boolean                          isBoundFact = false;

    private String[]                         fieldCompletionTexts;
    private String[]                         fieldCompletionValues;
    private String                           variableClass;
    private Constants                        constants   = GWT.create( Constants.class );

    private boolean readOnly;

    public ActionCallMethodWidget(RuleModeller mod,
                                  ActionCallMethod set) {
        this(mod, set, null);
    }

    public ActionCallMethodWidget(RuleModeller mod,
                                  ActionCallMethod set, Boolean readOnly) {
        super(mod);
        this.model = set;
        this.layout = new DirtyableFlexTable();

        layout.setStyleName( "model-builderInner-Background" ); // NON-NLS

        SuggestionCompletionEngine completions = this.getModeller().getSuggestionCompletions();
        if ( completions.isGlobalVariable( set.variable ) ) {

            List<MethodInfo> infos = completions.getMethodInfosForGlobalVariable( set.variable );
            this.fieldCompletionTexts = new String[infos.size()];
            this.fieldCompletionValues = new String[infos.size()];
            int i = 0;
            for ( MethodInfo info : infos ) {
                this.fieldCompletionTexts[i] = info.getName();
                this.fieldCompletionValues[i] = info.getNameWithParameters();
                i++;
            }

            this.variableClass = (String) completions.getGlobalVariable( set.variable );
        } else {
            FactPattern pattern = mod.getModel().getBoundFact( set.variable );
            if ( pattern != null ) {
                List<String> methodList = completions.getMethodNames( pattern.factType );
                fieldCompletionTexts = new String[methodList.size()];
                fieldCompletionValues = new String[methodList.size()];
                int i = 0;
                for ( String methodName : methodList ) {
                    fieldCompletionTexts[i] = methodName;
                    fieldCompletionValues[i] = methodName;
                    i++;
                }
                this.variableClass = pattern.factType;
                this.isBoundFact = true;
            } else {
                /*
                 *  if the call method is applied on a bound variable created in the rhs
                 */
                ActionInsertFact patternRhs = mod.getModel().getRhsBoundFact( set.variable );
                if ( patternRhs != null ) {
                    List<String> methodList = completions.getMethodNames( patternRhs.factType );
                    fieldCompletionTexts = new String[methodList.size()];
                    fieldCompletionValues = new String[methodList.size()];
                    int i = 0;
                    for ( String methodName : methodList ) {
                        fieldCompletionTexts[i] = methodName;
                        fieldCompletionValues[i] = methodName;
                        i++;
                    }
                    this.variableClass = patternRhs.factType;
                    this.isBoundFact = true;
                }
            }
        }

        if (readOnly == null){
           this.readOnly = !completions.containsFactType(this.variableClass);
        }else{
           this.readOnly = readOnly;
        }

        if (this.readOnly){
            layout.addStyleName("editor-disabled-widget");
        }


        doLayout();
        initWidget( this.layout );
    }

    private void doLayout() {
        layout.clear();
        layout.setWidget( 0,
                          0,
                          getSetterLabel() );
        DirtyableFlexTable inner = new DirtyableFlexTable();
        for ( int i = 0; i < model.fieldValues.length; i++ ) {
            ActionFieldFunction val = model.getFieldValue( i );

            inner.setWidget( i,
                             0,
                             fieldSelector( val ) );
            inner.setWidget( i,
                             1,
                             valueEditor( val ) );
            final int idx = i;
            /*
             * It is not possible to remove a parameter of a function
             * 
             * Image remove = new ImageButton("images/delete_item_small.gif");
             * //NON-NLS remove.addClickListener( new ClickListener() { public
             * void onClick(Widget w) { if
             * (Window.confirm(constants.RemoveThisItem())) { model.removeField(
             * idx ); modeller.refreshWidget(); }; } }); inner.setWidget( i, 3,
             * remove );
             */
        }
        layout.setWidget( 0,
                          1,
                          inner );
    }

    private Widget getSetterLabel() {
        HorizontalPanel horiz = new HorizontalPanel();

        if ( model.state == ActionCallMethod.TYPE_UNDEFINED ) {
            Image edit = new ImageButton( "images/add_field_to_fact.gif" ); //NON-
            // NLS
            edit.setTitle( constants.AddAnotherFieldToThisSoYouCanSetItsValue() );
            edit.addClickListener( new ClickListener() {
                public void onClick(Widget w) {
                    showAddFieldPopup( w );
                }
            } );
            horiz.add( new SmallLabel( HumanReadable.getActionDisplayName( "call" ) + " [" + model.variable + "]" ) ); // NON-NLS
            if (!this.readOnly){
                horiz.add( edit );
            }
        } else {
            horiz.add( new SmallLabel( HumanReadable.getActionDisplayName( "call" ) + " [" + model.variable + "." + model.methodName + "]" ) ); // NON-NLS
        }
        
        return horiz;
    }

    protected void showAddFieldPopup(Widget w) {

        final SuggestionCompletionEngine completions = this.getModeller().getSuggestionCompletions();

        final FormStylePopup popup = new FormStylePopup( "images/newex_wiz.gif",
                                                         constants.ChooseAMethodToInvoke() ); // NON-NLS
        final ListBox box = new ListBox();
        box.addItem( "..." );

        for ( int i = 0; i < fieldCompletionTexts.length; i++ ) {
            box.addItem( fieldCompletionTexts[i],
                         fieldCompletionValues[i] );
        }

        box.setSelectedIndex( 0 );

        popup.addAttribute( constants.ChooseAMethodToInvoke(),
                            box );
        box.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                model.state = ActionCallMethod.TYPE_DEFINED;

                String methodName = box.getItemText( box.getSelectedIndex() );
                String methodNameWithParams = box.getValue( box.getSelectedIndex() );

                model.methodName = methodName;
                List<String> fieldList = new ArrayList<String>();

                fieldList.addAll( completions.getMethodParams( variableClass,
                                                               methodNameWithParams ) );

                // String fieldType = completions.getFieldType( variableClass,
                // fieldName );
                int i = 0;
                for ( String fieldParameter : fieldList ) {
                    model.addFieldValue( new ActionFieldFunction( methodName,
                                                                  String.valueOf( i ),
                                                                  fieldParameter ) );
                    i++;
                }

                getModeller().refreshWidget();
                popup.hide();
            }
        } );
        popup.setPopupPosition( w.getAbsoluteLeft(),
                                w.getAbsoluteTop() );
        popup.show();

    }

    private Widget valueEditor(final ActionFieldFunction val) {

        SuggestionCompletionEngine completions = this.getModeller().getSuggestionCompletions();

		String type = "";
		if (completions.isGlobalVariable(this.model.variable)) {
			type = completions.getGlobalVariable(this.model.variable);
		} else {
			type = this.getModeller().getModel().getBindingType(this.model.variable);
			if (type == null) {
				type = this.getModeller().getModel().getRhsBoundFact(this.model.variable).factType;
			}
		}

		DropDownData enums = completions.getEnums(type, this.model.fieldValues, val.field);
		
        return new MethodParameterValueEditor(val,
                enums,
                this.getModeller(),
                val.type, new Command() {

            public void execute() {
                setModified(true);
            }
        });
    }

    /**
     * This will return a keyboard listener for field setters, which will obey
     * numeric conventions - it will also allow formulas (a formula is when the
     * first value is a "=" which means it is meant to be taken as the user
     * typed)
     */
    public static KeyboardListener getNumericFilter(final TextBox box) {
        return new KeyboardListener() {

            public void onKeyDown(Widget arg0,
                                  char arg1,
                                  int arg2) {

            }

            public void onKeyPress(Widget w,
                                   char c,
                                   int i) {
                if ( Character.isLetter( c ) && c != '=' && !(box.getText().startsWith( "=" )) ) {
                    ((TextBox) w).cancelKey();
                }
            }

            public void onKeyUp(Widget arg0,
                                char arg1,
                                int arg2) {
            }

        };
    }

    private Widget fieldSelector(final ActionFieldFunction val) {
        return new SmallLabel( val.type );
    }

    private Widget actionSelector(final ActionFieldFunction val) {
        SuggestionCompletionEngine completions = this.getModeller().getSuggestionCompletions();
        
        final ListBox box = new ListBox();
        final String fieldType = val.type;
        final String[] modifiers = completions.getModifiers( fieldType );

        if ( modifiers != null ) {
            for ( int i = 0; i < modifiers.length; i++ ) {
                box.addItem( modifiers[i] );
            }
        }
        box.addChangeListener( new ChangeListener() {

            public void onChange(Widget arg0) {
                String methodName = box.getItemText( box.getSelectedIndex() );
                val.setMethod( methodName );
            }

        } );
        return box;
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

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

}
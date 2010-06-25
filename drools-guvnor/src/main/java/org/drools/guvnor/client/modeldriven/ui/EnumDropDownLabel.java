package org.drools.guvnor.client.modeldriven.ui;

import org.drools.guvnor.client.common.DropDownValueChanged;
import org.drools.guvnor.client.messages.Constants;
import org.drools.ide.common.client.modeldriven.DropDownData;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.brl.BaseSingleFieldConstraint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Toni Rikkola
 */
public class EnumDropDownLabel extends Composite {

    private Constants          constants = ((Constants) GWT.create( Constants.class ));
    protected Panel            panel     = new HorizontalPanel();

    // The value is not always same as the text
    private final Label        textWidget;

    private final EnumDropDown enumDropDown;

    private final Button       okButton  = new Button( constants.OK() );

    private Command onValueChangeCommand;

    public EnumDropDownLabel(FactPattern pattern,
                             String fieldName,
                             SuggestionCompletionEngine sce,
                             BaseSingleFieldConstraint constraint) {
        this.textWidget = getTextLabel();
        this.enumDropDown = getEnumDropDown( constraint,
                                             sce,
                                             pattern,
                                             fieldName );

        panel.add( textWidget );

        initWidget( panel );

    }

    private Label getTextLabel() {
        Label label = new Label();
        label.setStyleName( "x-form-field" );

        label.addClickListener( new ClickListener() {
            public void onClick(Widget arg0) {
                showPopup();
            }
        } );

        if ( label.getText() == null && "".equals( label.getText() ) ) {
            label.setText( constants.Value() );
        }

        return label;
    }

    private void showPopup() {
        final PopupPanel popup = new PopupPanel();
        HorizontalPanel horizontalPanel = new HorizontalPanel();

        popup.setPopupPosition( this.getAbsoluteLeft(),
                                this.getAbsoluteTop() );

        okButton.addClickListener( new ClickListener() {
            public void onClick(Widget arg0) {
                executeOnValueChangeCommand();
                panel.clear();
                panel.add( textWidget );
                popup.hide();
            }
        } );

        horizontalPanel.add( enumDropDown );
        horizontalPanel.add( okButton );

        popup.add( horizontalPanel );

        popup.show();

    }

    private EnumDropDown getEnumDropDown(final BaseSingleFieldConstraint constraint,
                                         SuggestionCompletionEngine sce,
                                         FactPattern pattern,
                                         String fieldName) {
        String valueType = sce.getFieldType( pattern.factType,
                                             fieldName );

        final DropDownData dropDownData;
        if ( SuggestionCompletionEngine.TYPE_BOOLEAN.equals( valueType ) ) {
            dropDownData = DropDownData.create( new String[]{"true", "false"} ); //NON-NLS
        } else {
            dropDownData = sce.getEnums( pattern,
                                         fieldName );
        }

        final EnumDropDown box = new EnumDropDown( constraint.getValue(),
                                                   new DropDownValueChanged() {
                                                       public void valueChanged(String newText,
                                                                                String newValue) {
                                                           textWidget.setText( newText );
                                                           constraint.setValue(newValue);
                                                           okButton.click();
                                                       }
                                                   },
                                                   dropDownData );

        if ( box.getItemCount() > 6 ) {
            box.setVisibleItemCount( 6 );
        } else {
            box.setVisibleItemCount( box.getItemCount() );
        }

        return box;
    }

    private void executeOnValueChangeCommand(){
        if (this.onValueChangeCommand != null){
            this.onValueChangeCommand.execute();
        }
    }

    public void setOnValueChangeCommand(Command onValueChangeCommand) {
        this.onValueChangeCommand = onValueChangeCommand;
    }

}

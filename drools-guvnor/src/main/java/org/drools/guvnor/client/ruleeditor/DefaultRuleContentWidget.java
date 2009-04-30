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

import org.drools.guvnor.client.common.DirtyableComposite;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.RuleContentText;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is the default rule editor widget (just text editor based) - more to come later.
 * @author michael neale
 */
public class DefaultRuleContentWidget extends DirtyableComposite {

    private TextArea              text;
    final private RuleContentText data;

    final private RuleAsset       asset;

    public DefaultRuleContentWidget(RuleAsset a, RuleViewer v) {
        this(a);
    }


    public DefaultRuleContentWidget(RuleAsset a) {
    	this(a, -1);
    }

    public DefaultRuleContentWidget(RuleAsset a, int visibleLines) {
        asset = a;
        data = (RuleContentText) asset.content;

        if (data.content == null) {
        	data.content = "";
        }


        text = new TextArea();
        text.setWidth( "100%" );
        text.setVisibleLines((visibleLines == -1) ?  16 : visibleLines );
        text.setText( data.content );

        text.getElement().setAttribute("spellcheck", "false");    //NON-NLS

        text.setStyleName( "default-text-Area" ); //NON-NLS

        text.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                data.content = text.getText();
                makeDirty();
            }
        } );

        text.addKeyboardListener( new KeyboardListenerAdapter() {
            public void onKeyDown(Widget arg0,
                                  char arg1,
                                  int arg2) {
                System.err.println(arg1);
                if ( arg1 == KEY_TAB ) {
                	int pos = text.getCursorPos();
                    insertText( "\t" );
                    text.setCursorPos( pos + 1 );
                    text.cancelKey();
                    text.setFocus(true);
                }
            }
        } );

        initWidget( text );

    }

    void insertText(String ins) {
        int i = text.getCursorPos();
        String left = text.getText().substring( 0,
                                                i );
        String right = text.getText().substring( i,
                                                 text.getText().length() );
        text.setText( left + ins + right );
        this.data.content = text.getText();
    }


}
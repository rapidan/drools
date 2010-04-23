package org.drools.guvnor.client.rulelist;
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



import java.util.ArrayList;
import java.util.List;

import org.drools.guvnor.client.common.FormStyleLayout;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.PrettyFormLayout;
import org.drools.guvnor.client.rpc.*;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.ruleeditor.EditorLauncher;
import org.drools.guvnor.client.packages.SuggestionCompletionCache;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.Command;
import com.google.gwt.core.client.GWT;

/**
 * This is for quickly finding an asset by name. Partial completion is allowed.
 * This also uses some auto completion magic.
 * @author Michael Neale
 */
public class QuickFindWidget extends Composite {

    private final FormStyleLayout layout;
    private final FlexTable listPanel;
    private SuggestBox searchBox;
    private CheckBox archiveBox;
    private final EditItemEvent openItem;
    private Constants constants = ((Constants) GWT.create(Constants.class));


    public QuickFindWidget(EditItemEvent editEvent) {
        layout = new FormStyleLayout("images/system_search.png", ""); //NON-NLS

        searchBox = new SuggestBox(new SuggestOracle() {
			public void requestSuggestions(Request r, Callback cb) {
				loadShortList(r.getQuery(), r, cb);

			}
        });

        final SimplePanel resultsP = new SimplePanel();

        this.openItem = editEvent;
        HorizontalPanel srch = new HorizontalPanel();
        Button go = new Button(constants.Search());
        final ClickListener cl = new ClickListener() {        
            public void onClick(Widget w) {
               //updateList();
                resultsP.clear();
                AssetItemGrid grid = new AssetItemGrid( openItem,
                                                        "searchresults",
                                                        new AssetItemGridDataLoader() { //NON-NLS
                                                            public void loadData(int startRow,
                                                                                 int numberOfRows,
                                                                                 GenericCallback<TableDataResult> cb) {
                                                                RepositoryServiceFactory.getService().quickFindAsset(searchBox.getText(),
                                                                                                                     archiveBox.isChecked(),
                                                                                                                     startRow,
                                                                                                                     numberOfRows,
                                                                                                                     cb );
                                                             }
                                                        } );
                resultsP.add( grid );

            }
        } ;
        go.addClickListener(cl);
       
        searchBox.addKeyboardListener(new KeyboardListenerAdapter() {
            @Override
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {
                if (keyCode == KeyboardListener.KEY_ENTER) {
                    cl.onClick( sender );
                }
            }
        });
        srch.add( searchBox );

        archiveBox = new CheckBox();

        archiveBox.setChecked(false);

        layout.addAttribute(constants.FindItemsWithANameMatching(), srch );
        layout.addAttribute(constants.IncludeArchivedAssetsInResults(), archiveBox);
        layout.addAttribute("", go );


        listPanel = new FlexTable();
        listPanel.setWidget( 0, 0, new HTML("<img src='images/information.gif'/>&nbsp;" + constants.EnterSearchString()) ); //NON-NLS

        PrettyFormLayout pfl = new PrettyFormLayout();
        pfl.startSection();
        pfl.addRow(listPanel);
        pfl.addRow( resultsP );

        pfl.endSection();
        layout.addRow(pfl);

        /*

        Button b = new Button("Do a request");
        b.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + "");
            }
        });

        */

        initWidget( layout );
    }

    void scrollyRuleLoaderExample() {
        final VerticalPanel vp = new VerticalPanel();
        final ScrollPanel panel = new ScrollPanel(vp);
        panel.setHeight("10em");

        String cat = "Home Mortgage/Eligibility rules";

        RepositoryServiceFactory.getService().loadRuleListForCategories(cat, 0, 10, AssetItemGrid.RULE_LIST_TABLE_ID, new GenericCallback<TableDataResult>() {
            public void onSuccess(TableDataResult result) {
                final List<String> ids = new ArrayList<String>();
                for (TableDataRow aData : result.data) {
                    ids.add(aData.id);
                }

                RepositoryServiceFactory.getService().loadRuleAsset(ids.get(0), new GenericCallback<RuleAsset>() {
                    public void onSuccess(final RuleAsset result) {
                        SuggestionCompletionCache.getInstance().doAction(result.metaData.packageName, new Command() {
                            public void execute() {
                                final Widget last = EditorLauncher.getEditorViewer(result, null);
                                vp.add(last);
                                panel.addScrollListener(new ScrollListener() {
                                    int i = 0;
                                    Widget end = last;
                                    public void onScroll(Widget widget, int scrollLeft, int scrollTop) {
                                        //System.err.println("final pos: " + (f.getAbsoluteTop() + f.getOffsetHeight() + " Panel pos: " + (panel.getAbsoluteTop() + panel.getOffsetHeight()))) ;
                                        int finalPos = end.getAbsoluteTop() + end.getOffsetHeight();
                                        int panelPos = panel.getAbsoluteTop() + panel.getOffsetHeight();
                                        System.err.println(panelPos + " " + finalPos);
                                        if (finalPos == panelPos) {
                                            i++;
                                            if (i < ids.size() -1) {
                                                RepositoryServiceFactory.getService().loadRuleAsset(ids.get(i), new GenericCallback<RuleAsset>() {
                                                    public void onSuccess(RuleAsset result) {
                                                        end = EditorLauncher.getEditorViewer(result, null);
                                                        vp.add(end);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        });

                    }
                });

            }
        });
    }

    /**
     * This will load a list of items as they are typing.
     */
    protected void loadShortList(String match, final Request r, final Callback cb) {
        RepositoryServiceFactory.getService().quickFindAsset( match, archiveBox.isChecked() ,0, 5, new GenericCallback<TableDataResult>() {

            public void onSuccess(TableDataResult result) {
                List items = new ArrayList();
                for ( int i = 0; i < result.data.length; i++ ) {
                    if (!result.data[i].id.equals( "MORE" )) {         //NON-NLS
                    	final String str = result.data[i].values[0];
                    	items.add( new SuggestOracle.Suggestion() {

							public String getDisplayString() {
								return str;
							}

							public String getReplacementString() {
								return str;
							}

                    	});

                    }
                }
                cb.onSuggestionsReady(r, new SuggestOracle.Response(items));
            }

        });

    }

}
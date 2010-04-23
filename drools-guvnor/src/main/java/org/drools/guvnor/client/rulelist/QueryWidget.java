package org.drools.guvnor.client.rulelist;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.drools.guvnor.client.common.ErrorPopup;
import org.drools.guvnor.client.common.FormStyleLayout;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.explorer.Preferences;
import org.drools.guvnor.client.rpc.MetaDataQuery;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.modeldriven.ui.DatePickerLabel;
import org.drools.guvnor.client.modeldriven.ui.DatePickerTextBox;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.DateField;

public class QueryWidget extends Composite {

    private VerticalPanel layout;
    private EditItemEvent openItem;
    private Constants     constants = ((Constants) GWT.create( Constants.class ));

    public QueryWidget(EditItemEvent openItem) {
        layout = new VerticalPanel();
        this.openItem = openItem;
        doQuickFind();
        doTextSearch();
        doMetaSearch();

        layout.setWidth( "100%" );

        initWidget( layout );

        setWidth( "100%" );

    }

    private void doMetaSearch() {
        Panel p = new Panel();
        p.setCollapsible( true );
        p.setTitle( constants.AttributeSearch() );

        final Map<String, MetaDataQuery> atts = new HashMap<String, MetaDataQuery>() {
            {
                put( constants.CreatedBy(),
                     new MetaDataQuery( "drools:creator" ) ); //NON-NLS
                put( constants.Format1(),
                     new MetaDataQuery( "drools:format" ) ); //NON-NLS
                put( constants.Subject(),
                     new MetaDataQuery( "drools:subject" ) ); //NON-NLS
                put( constants.Type1(),
                     new MetaDataQuery( "drools:type" ) ); //NON-NLS
                put( constants.ExternalLink(),
                     new MetaDataQuery( "drools:relation" ) ); //NON-NLS
                put( constants.Source(),
                     new MetaDataQuery( "drools:source" ) ); //NON-NLS
                put( constants.Description1(),
                     new MetaDataQuery( "drools:description" ) ); //NON-NLS
                put( constants.LastModifiedBy(),
                     new MetaDataQuery( "drools:lastContributor" ) ); //NON-NLS
                put( constants.CheckinComment(),
                     new MetaDataQuery( "drools:checkinComment" ) ); //NON-NLS
            }
        };

        FormStyleLayout fm = new FormStyleLayout();
        for ( Iterator iterator = atts.keySet().iterator(); iterator.hasNext(); ) {
            String fieldName = (String) iterator.next();
            final MetaDataQuery q = (MetaDataQuery) atts.get( fieldName );
            final TextBox box = new TextBox();
            box.setTitle( constants.WildCardsSearchTip() );
            fm.addAttribute( fieldName + ":",
                             box );
            box.addChangeListener( new ChangeListener() {
                public void onChange(Widget w) {
                    q.valueList = box.getText();
                }
            } );
        }

        HorizontalPanel created = new HorizontalPanel();
        created.add( new SmallLabel( constants.AfterColon() ) );
        final DatePickerTextBox createdAfter = new DatePickerTextBox( "" );
        created.add( createdAfter );

        created.add( new SmallLabel( "&nbsp;" ) ); //NON-NLS

        created.add( new SmallLabel( constants.BeforeColon() ) );
        final DatePickerTextBox createdBefore = new DatePickerTextBox( "" );
        created.add( createdBefore );

        fm.addAttribute( constants.DateCreated1(),
                         created );

        HorizontalPanel lastMod = new HorizontalPanel();
        lastMod.add( new SmallLabel( constants.AfterColon() ) );
        final DatePickerTextBox lastModAfter = new DatePickerTextBox( "" );
        lastMod.add( lastModAfter );

        lastMod.add( new SmallLabel( "&nbsp;" ) ); //NON-NLS

        lastMod.add( new SmallLabel( constants.BeforeColon() ) );
        final DatePickerTextBox lastModBefore = new DatePickerTextBox( "" );
        lastMod.add( lastModBefore );

        fm.addAttribute( constants.LastModified1(),
                         lastMod );

        final SimplePanel resultsP = new SimplePanel();
        Button search = new Button( constants.Search() );
        fm.addAttribute( "",
                         search );
        search.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                resultsP.clear();
                AssetItemGrid grid = new AssetItemGrid( openItem,
                                                        "searchresults",
                                                        new AssetItemGridDataLoader() { //NON-NLS
                                                            public void loadData(int startRow,
                                                                                 int numberOfRows,
                                                                                 GenericCallback cb) {
                                                                MetaDataQuery[] mdq = new MetaDataQuery[atts.size()];
                                                                int i = 0;
                                                                for ( Iterator iterator = atts.keySet().iterator(); iterator.hasNext(); ) {
                                                                    String name = (String) iterator.next();
                                                                    mdq[i] = (MetaDataQuery) atts.get( name );
                                                                    i++;
                                                                }
                                                                try {
                                                                    RepositoryServiceFactory.getService().queryMetaData( mdq,
                                                                                                                         getDate( createdAfter ),
                                                                                                                         getDate( createdBefore ),
                                                                                                                         getDate( lastModAfter ),
                                                                                                                         getDate( lastModBefore ),
                                                                                                                         false,
                                                                                                                         startRow,
                                                                                                                         numberOfRows,
                                                                                                                         cb );
                                                                } catch ( IllegalArgumentException e ) {
                                                                    ErrorPopup.showMessage( Format.format( constants.BadDateFormatPleaseTryAgainTryTheFormatOf0(),
                                                                                                           Preferences.getStringPref( "drools.dateformat" ) ) );
                                                                }
                                                            }

                                                            private Date getDate(final DatePickerTextBox datePicker) {
                                                                try {
                                                                    return datePicker.getDate();
                                                                } catch ( IllegalArgumentException e ) {
                                                                    datePicker.clear();
                                                                    throw e;
                                                                }
                                                            }
                                                        } );
                resultsP.add( grid );
            }
        } );
        fm.addRow( resultsP );
        p.add( fm );
        p.setCollapsed( true );
        layout.add( p );
    }

    private void doQuickFind() {
        Panel p = new Panel();
        p.setCollapsible( true );
        p.setTitle( constants.NameSearch() );
        p.add( new QuickFindWidget( openItem ) );

        p.setCollapsed( false );

        layout.add( p );
    }

    private void doTextSearch() {
        Panel p = new Panel();
        p.setCollapsible( true );
        p.setTitle( constants.TextSearch() );

        p.setCollapsed( true );

        FormStyleLayout ts = new FormStyleLayout();
        final TextBox tx = new TextBox();
        ts.addAttribute( constants.SearchFor(),
                         tx );
        Button go = new Button();
        go.setText( constants.Search1() );
        ts.addAttribute( "",
                         go );
        ts.setWidth( "100%" );
        p.add( ts );

        final SimplePanel resultsP = new SimplePanel();
        final ClickListener cl = new ClickListener() {
            public void onClick(Widget w) {
                if ( tx.getText().equals( "" ) ) {
                    Window.alert( constants.PleaseEnterSomeSearchText() );
                    return;
                }
                resultsP.clear();
                AssetItemGrid grid = new AssetItemGrid( openItem,
                                                        "searchresults",
                                                        new AssetItemGridDataLoader() { //NON-NLS
                                                            public void loadData(int startRow,
                                                                                 int numberOfRows,
                                                                                 GenericCallback cb) {
                                                                RepositoryServiceFactory.getService().queryFullText( tx.getText(),
                                                                                                                     false,
                                                                                                                     startRow,
                                                                                                                     numberOfRows,
                                                                                                                     cb );
                                                            }
                                                        } );
                resultsP.add( grid );
            }
        };

        go.addClickListener( cl );
        tx.addKeyboardListener( new KeyboardListenerAdapter() {
            @Override
            public void onKeyUp(Widget sender,
                                char keyCode,
                                int modifiers) {
                if ( keyCode == KeyboardListener.KEY_ENTER ) {
                    cl.onClick( sender );
                }
            }
        } );
        ts.addRow( resultsP );
        layout.add( p );
    }

}

package org.drools.guvnor.client.factmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.packages.SuggestionCompletionCache;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.RuleContentText;
import org.drools.guvnor.client.ruleeditor.DefaultRuleContentWidget;
import org.drools.guvnor.client.ruleeditor.RuleViewer;
import org.drools.guvnor.client.ruleeditor.SaveEventListener;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.gwtext.client.widgets.form.FormPanel;

/**
 * The editor for fact models (drl declared types).
 *
 * @author Michael Neale
 */
public class FactModelWidget extends Composite implements SaveEventListener {

	private RuleAsset asset;
	private VerticalPanel layout;
	private int editingFact = -1;
    private static Map<String, String> TYPE_DESCRIPTIONS = new HashMap<String, String>() {
        {
            put ("Integer", "Whole number (integer)");
            put ("Boolean", "True or False");
            put ("java.util.Date", "Date");
            put ("java.math.BigDecimal", "Decimal number");
            put ("String", "Text");

        }
    };


    public FactModelWidget(RuleAsset asset, RuleViewer viewer) {
        this(asset);
    }

    public FactModelWidget(final RuleAsset asset) {
		this.asset = asset;
		this.layout = new VerticalPanel();

		if (asset.content instanceof RuleContentText) {
			layout.add(new DefaultRuleContentWidget(asset));
		} else {
			//loadTestData(asset);
			if (asset.content == null) {
				asset.content = new FactModels();
			}
			renderEditor();
		}

		layout.setWidth("100%");
		initWidget(layout);
		setStyleName("model-builder-Background");
	}


	private void renderEditor() {
		layout.clear();
		final FactModels m = (FactModels) asset.content;

		String factHeaderStyle = "modeller-fact-TypeHeader";
		for (int i = 0; i < m.models.size(); i++) {

			final FactMetaModel mm = (FactMetaModel) m.models.get(i);

	        FormPanel config = new FormPanel();
	        config.setTitle(mm.name);
	        config.setCollapsible(true);
	        config.setCollapsed(!(editingFact == i));


			FlexTable tb = new FlexTable();
			config.add(tb);
			tb.setStyleName("modeller-fact-pattern-Widget");
			tb.setWidth("100%");
			//layout.add(tb);
			layout.add(config);

			HorizontalPanel headerPanel = new HorizontalPanel();
			//headerPanel.add(new HTML("<b><small>" + mm.name + "</small></b>"));

			//ImageButton addField = new ImageButton("images/add_field_to_fact.gif");
			Button addField = new Button("Add field");
			addField.addClickListener(new ClickListener() {
				public void onClick(Widget arg0) {
					showFieldEditor(m, mm, null);
				}
			});
			headerPanel.add(addField);
			headerPanel.add(editFact(mm, m));

			tb.setWidget(0, 0, headerPanel);
			FlexCellFormatter formatter = tb.getFlexCellFormatter();
			formatter.setColSpan(0, 0, 2);
			formatter.setStyleName(0, 0, factHeaderStyle);
			formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

			for (int j = 0; j < mm.fields.size(); j++) {
				final FieldMetaModel fm = (FieldMetaModel) mm.fields.get(j);
				tb.setWidget(j + 1, 0, new HTML("<b><small>" + fm.name + ":</small></b>"));
				formatter.setHorizontalAlignment(j + 1, 0, HasHorizontalAlignment.ALIGN_RIGHT);

				HorizontalPanel type = new HorizontalPanel();
				type.add(new SmallLabel(getDesc(fm)));
				ImageButton del = new ImageButton("images/delete_item_small.gif");
				del.addClickListener(new ClickListener() {
					public void onClick(Widget w) {
						if (Window.confirm("Are you sure you want to remove the field " + fm.name + " ?")) {
							mm.fields.remove(fm);
							editingFact = m.models.indexOf(mm);
							renderEditor();
						}
					}
				});

				ImageButton edit = new ImageButton("images/edit.gif");
				edit.addClickListener(new ClickListener() {
					public void onClick(Widget arg0) {
						showFieldEditor(m, mm, fm);
					}
				});

				type.add(edit);
				type.add(del);



				tb.setWidget(j + 1, 1, type);
				formatter.setHorizontalAlignment(j + 1, 1, HasHorizontalAlignment.ALIGN_LEFT);
			}



		}
		final Button addNewFact = new Button("Add new fact type");
		addNewFact.addClickListener(new ClickListener() {
			public void onClick(Widget w) {
				String type = Window.prompt("New type", "Enter new type name");
				if (type != null)  {
					if (uniqueName(type, m.models)) {
						m.models.add(new FactMetaModel(type, new ArrayList()));
						editingFact = m.models.size() -1;
						renderEditor();
					} else {
						Window.alert("The type name [" + type + "] already exists, please choose another name.");
						addNewFact.click();
					}
				}
			}
		});
		layout.add(addNewFact);

	}

	private boolean uniqueName(String type, List<FactMetaModel> models) {
		for (FactMetaModel m : models) {
			if (m.name.equals(type)) {
				return false;
			}
		}
		return true;
	}


    private String getDesc(FieldMetaModel fm) {
        if (TYPE_DESCRIPTIONS.containsKey(fm.type)) {
            return TYPE_DESCRIPTIONS.get(fm.type);
        }
        return fm.type;
    }


    /**
	 * Display the field editor.
	 */
	private void showFieldEditor(final FactModels models, final FactMetaModel mm, final FieldMetaModel field) {
		final FormStylePopup pop = new FormStylePopup();
		final TextBox fieldName = new TextBox();
		final TextBox fieldType = new TextBox();
		fieldName.addKeyboardListener(noSpaceListener());
		fieldType.addKeyboardListener(noSpaceListener());
		if (field != null) {
			fieldName.setText(field.name);
			fieldType.setText(getDesc(field));
		}
		HorizontalPanel typeP = new HorizontalPanel();
		typeP.add(fieldType);
		final ListBox typeChoice = new ListBox();
		typeChoice.addItem("-- choose type --");

        for (String k : TYPE_DESCRIPTIONS.keySet()) {
            typeChoice.addItem(TYPE_DESCRIPTIONS.get(k), k);
        }


		int idx = models.models.indexOf(mm);
		for (int i = 0; i < idx; i++) {
			FactMetaModel mm_ = (FactMetaModel) models.models.get(i);
			typeChoice.addItem(mm_.name);
		}
		typeChoice.setSelectedIndex(0);
		typeChoice.addChangeListener(new ChangeListener() {
			public void onChange(Widget w) {
				fieldType.setText(typeChoice.getValue(typeChoice.getSelectedIndex()));
			}
		});

		typeP.add(typeChoice);

		pop.addAttribute("Field name", fieldName);
		pop.addAttribute("Type", typeP);

		Button ok = new Button("OK");
		ok.addClickListener(new ClickListener() {
			public void onClick(Widget arg0) {
				FieldMetaModel fld = field;
				if (field == null) {
					fld = new FieldMetaModel();
					mm.fields.add(fld);
				}
				fld.name = fieldName.getText();
				fld.type = fieldType.getText();
				editingFact = models.models.indexOf(mm);
				renderEditor();
				pop.hide();
			}
		});
		pop.addAttribute("", ok);

		pop.show();
	}

	/**
	 * An editor for fact header name.
	 * @param m
	 */
	private Widget editFact(final FactMetaModel mm, final FactModels m) {
		ImageButton edit = new ImageButton("images/edit.gif");
		//Button edit = new Button("Edit/remove");
		edit.addClickListener(new ClickListener() {
			public void onClick(Widget arg0) {
				final FormStylePopup pop = new FormStylePopup();
				HorizontalPanel changeName = new HorizontalPanel();
				final TextBox name = new TextBox();
				name.setText(mm.name);
				changeName.add(name);
				Button nameBut = new Button("Change name");

				nameBut.addKeyboardListener(noSpaceListener());

				nameBut.addClickListener(new ClickListener() {
					public void onClick(Widget w) {
                        if (!uniqueName(name.getText(), m.models)) {
                            Window.alert("The name [" + name.getText() + "] is already taken - please choose another");
                            return;
                        }
						if (Window.confirm("Are you sure you want to change the name? Its possible that rules will need to be changed to reflect the new name.")) {
							mm.name = name.getText();
							pop.hide();
							renderEditor();
						}
					}
				});
				changeName.add(nameBut);
				pop.addAttribute("Change fact name", changeName);

				Button delFact = new Button("Delete");
				delFact.addClickListener(new ClickListener() {
					public void onClick(Widget w) {
						if (Window.confirm("Are you sure you want to remove this fact?")) {
							m.models.remove(mm);
							pop.hide();
							renderEditor();
						}
					}
				});
				pop.addAttribute("Remove this fact type", delFact);

				pop.show();
			}

		});
		return edit;
	}

	private KeyboardListener noSpaceListener() {
		return new KeyboardListener() {
			public void onKeyDown(Widget arg0, char arg1, int arg2) {
			}

	        public void onKeyPress(Widget w, char c, int i) {
	                if (c == ' ') {
	                    ((TextBox) w).cancelKey();
	                }
	        }

			public void onKeyUp(Widget arg0, char arg1, int arg2) {

			}

		};
	}

	public void onAfterSave() {
		LoadingPopup.showMessage("Refreshing model...");
		SuggestionCompletionCache.getInstance().loadPackage(this.asset.metaData.packageName, new Command() {
			public void execute() {
				LoadingPopup.close();
			}
		});
	}

	public void onSave() {
		//not needed.

	}


}

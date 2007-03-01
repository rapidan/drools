package org.drools.ide.flow.common.view.datatype.editor.impl;
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

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.flow.common.datatype.IDataTypeRegistry;
import org.drools.ruleflow.common.datatype.IDataType;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Combobox for datatypes based on a datatype registry.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DataTypeCombo extends Composite {
    
    private IDataTypeRegistry registry;
    private ComboViewer dataTypeCombo;
    
    public DataTypeCombo(Composite parent, int style, IDataTypeRegistry registry) {
        super(parent, style);
        this.registry = registry;
        setLayout(new FillLayout());
        dataTypeCombo = new ComboViewer(this, SWT.READ_ONLY);
        dataTypeCombo.setLabelProvider(new LabelProvider() {
            public String getText(Object element) {
                return ((IDataTypeRegistry.IDataTypeInfo) element).getName();
            }
        });
        dataTypeCombo.add(registry.getDataTypes().toArray(
            new IDataTypeRegistry.IDataTypeInfo[0]));
    }
    
    private IDataTypeRegistry.IDataTypeInfo getSelectedTypeInfo() {
        ISelection selection = dataTypeCombo.getSelection();
        if (!selection.isEmpty()) {
            return (IDataTypeRegistry.IDataTypeInfo)
                ((StructuredSelection) selection).getFirstElement();
        }
        return null;
    }
    
    public void setDataType(IDataType dataType) {
        if (dataType == null) {
        	dataTypeCombo.setSelection(null);
        } else {
        	// TODO : check what happens if dataType not in combo
            try {
            	dataTypeCombo.setSelection(new StructuredSelection(registry.getDataTypeInfo(dataType.getClass())));
            } catch (IllegalArgumentException e) {
            	// "DataTypeInfo not found in registry: " + dataType.getClass()
            	DroolsIDEPlugin.log(e);
            }
        }
    }
    
    public IDataType getDataType() {
    	IDataTypeRegistry.IDataTypeInfo dataTypeInfo =
    		getSelectedTypeInfo();
        return dataTypeInfo == null ? null : 
        	dataTypeInfo.getDataTypeFactory().createDataType();
    }
    
    public void reset() {
    	dataTypeCombo.setSelection(null);
    }
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dataTypeCombo.getCombo().setEnabled(enabled);
    }
    
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    	dataTypeCombo.addSelectionChangedListener(listener);
    }
}

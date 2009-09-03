/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.etl.dt.launching.tabs;

import org.eclipse.epsilon.common.dt.EpsilonPlugin;
import org.eclipse.epsilon.common.dt.launching.AbstractSourceConfigurationTab;
import org.eclipse.epsilon.etl.dt.EtlPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class EtlSourceConfigurationTab extends AbstractSourceConfigurationTab{

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout( new RowLayout());
		
		new EtlSourceConfigurationTab().createControl(shell);
		
		shell.pack();
		shell.open();
		while( !shell.isDisposed())
		{
			if(!display.readAndDispatch()) 
				display.sleep();
		}
		display.dispose();
	}
	
	@Override
	public EpsilonPlugin getPlugin() {
		return EtlPlugin.getDefault();
	}

	@Override
	public String getImagePath() {
		return "icons/etl.gif";
	}

	@Override
	public String getFileExtension() {
		return "etl";
	}

	@Override
	public String getSelectionTitle() {
		return "Select an ETL Transformation";
	}

	@Override
	public String getSelectionSubtitle() {
		return "ETL Transformations in Workspace";
	}

	public String getLaunchConfigurationKey() {
		return "SOURCE.ETL";
	}
	
	//public String getTitle() {
	//	return "ETL Source";
	//}

}

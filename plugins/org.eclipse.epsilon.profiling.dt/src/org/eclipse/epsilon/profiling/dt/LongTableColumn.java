/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.profiling.dt;

import org.eclipse.epsilon.common.util.StringUtil;
import org.eclipse.jface.viewers.TableViewer;

public class LongTableColumn extends SortableTableColumn {

	public LongTableColumn(TableViewer tableViewer, int style) {
		super(tableViewer, style);
	}

	public LongTableColumn(TableViewer tableViewer, int style, int index) {
		super(tableViewer, style, index);
	}

	@Override
	public int compare(Object o1, Object o2) {
		long l1 = Long.parseLong(StringUtil.toString(o1));
		long l2 = Long.parseLong(StringUtil.toString(o2));
		if (l1>l2) return 1;
		else if (l2>l1) return -1;
		else return 0;
	}

}

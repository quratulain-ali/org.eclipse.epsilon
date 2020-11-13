package org.eclipse.epsilon.eol.query;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.epsilon.common.dt.util.ListContentProvider;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

public class QueryRewritingView extends ViewPart {
	protected IEditorPart editor;
     ListViewer viewer;
     
	 public QueryRewritingView() {
             super();
     }
	 
	 public void init(IViewSite site) throws PartInitException {
         super.init(site);
         // Normally we might do other stuff here.
	 }
	 
	 
	@Override
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent,0);
		viewer.setContentProvider(new ListContentProvider());
		
        IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new RefreshAction(this));
        
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorPart editor = page.getActiveEditor();
        IEditorInput input = editor.getEditorInput();
        IPath path = ((FileEditorInput)input).getPath();
        EolModule module = new EolModule();
		try {
			module.parse(new File(path.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		module.compile();
		new EolStaticAnalyser().validate(module);
	    viewer.setInput(module.getTranslatedQueries());
	}
	
	public IEditorPart getEditor() {
		return editor;
	}
	
	public void render(IEditorPart editor) {
		//if (editor != null) {
			IWorkbench wb = PlatformUI.getWorkbench();
	        IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
	        IWorkbenchPage page = window.getActivePage();
	        editor = page.getActiveEditor();
	        IEditorInput input = editor.getEditorInput();
	        IPath path = ((FileEditorInput)input).getPath();
	        EolModule module = new EolModule();
			try {
				module.parse(new File(path.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		module.compile();
		new EolStaticAnalyser().validate(module);
		viewer.setInput(module.getTranslatedQueries());
		}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	

}
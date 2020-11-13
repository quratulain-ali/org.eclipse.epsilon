package org.eclipse.epsilon.eol.query;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.epsilon.common.dt.util.ListContentProvider;
import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
	 private Label label;
	 public String text;
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
        viewer.setLabelProvider(new MyLabelProvider());
       
        
//        label.setText("text");
//        
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		for(ModelDeclaration modeldeclaration: module.getDeclaredModelDeclarations()) {
//			
//			if(modeldeclaration.getDriverNameExpression().getName().equals("MySQL"))
//				module.getCompilationContext().setModelFactory(new SubModelFactory());
//			else
//				module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
//		}
		module.compile();
		new EolStaticAnalyser().validate(module);
	    viewer.setInput(module.getTranslatedQueries());
//		String string = new EolUnparser().unparse(module);
//		viewer.setInput(Arrays.asList(string.split(System.lineSeparator())));
	}

	public class MyLabelProvider extends LabelProvider implements IColorProvider {

	    public String getText(Object element){
	        return String.valueOf(element);
	    }

	    public Color getForeground(Object element){
	        Display display = Display.getDefault();
	        return display.getSystemColor(SWT.COLOR_RED);
	    }

	    public Color getBackground(Object element){
	        return null;
	    }
	}
	
	@Override
	public void setFocus() {
		//label.setFocus();

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		for(ModelDeclaration modeldeclaration: module.getDeclaredModelDeclarations()) {
			
			if(modeldeclaration.getDriverNameExpression().getName().equals("MySQL"))
				module.getCompilationContext().setModelFactory(new SubModelFactory());
			else
				module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
		}
		module.compile();
		new EolStaticAnalyser().validate(module);
		viewer.setInput(module.getTranslatedQueries());
		}
	

}
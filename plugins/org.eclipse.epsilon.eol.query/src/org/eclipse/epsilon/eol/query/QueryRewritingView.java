package org.eclipse.epsilon.eol.query;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.epsilon.common.dt.editor.ModelTypeExtensionFactory;
import org.eclipse.epsilon.common.dt.util.ListContentProvider;
import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dt.editor.EolEditor;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.epsilon.common.dt.editor.AbstractModuleEditorSourceViewerConfiguration;

public class QueryRewritingView extends ViewPart {
	protected IEditorPart editor;
	public String text;
	SourceViewer viewer;
	IEolCompilationContext context;
	Document d;

	public QueryRewritingView() {
		super();
	}

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	public void createPartControl(Composite parent) {
	//	label = new Label(parent, 0);
		
		 viewer = new SourceViewer(parent, null, SWT.NONE);
//		  SourceViewerConfiguration configuration = new AntSourceViewerConfiguration();
//		  viewer.configure(configuration);
		 SourceViewerConfiguration configuration = new AbstractModuleEditorSourceViewerConfiguration(new EolEditor());
		configuration.getPresentationReconciler(viewer).install(viewer);
		viewer.configure(configuration); 
		viewer.setEditable(false);
		  Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
		  viewer.getTextWidget().setFont(font);
		  
		  d = new Document();
		   
		 
		//TODO: remove
//		viewer = new ListViewer(parent, 0);
//		viewer.setContentProvider(new ListContentProvider());
//		viewer.setLabelProvider(new MyLabelProvider());
       
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new RefreshAction(this));
	
		EolModule module = new EolModule();
		
		try {
			module.parse(new File(getActiveEditorFilePath()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		context = module.getCompilationContext();
		
		context.setModelFactory(new ModelTypeExtensionFactory());
		
		new EolStaticAnalyser().validate(module);
		
		new QueryRewriter().invokeRewriters(module);
		
//		 viewer.setInput(new EolUnparser().unparse(module));
//		 viewer.getControl().setFocus();
		 d.set(new EolUnparser().unparse(module));
		    viewer.setDocument(d);
		
		//label.setText(new EolUnparser().unparse(module));
	}

	public class MyLabelProvider extends LabelProvider implements IColorProvider {

		public String getText(Object element) {
			return String.valueOf(element);
		}

		public Color getForeground(Object element) {
			Display display = Display.getDefault();
			return display.getSystemColor(SWT.COLOR_RED);
		}

		public Color getBackground(Object element) {
			return null;
		}
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();

	}

	public IEditorPart getEditor() {
		return editor;
	}
	
	public String getActiveEditorFilePath() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		IPath path = ((FileEditorInput) input).getPath();
		return path.toString();
	}

	public void render(IEditorPart editor) {
		
		EolModule module = new EolModule();
		try {
			module.parse(new File(getActiveEditorFilePath()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		context = module.getCompilationContext();
		context.setModelFactory(new ModelTypeExtensionFactory());
		
		new EolStaticAnalyser().validate(module);
		
		new QueryRewriter().invokeRewriters(module);

		d.set(new EolUnparser().unparse(module));
	    viewer.setDocument(d);
	
	}

}
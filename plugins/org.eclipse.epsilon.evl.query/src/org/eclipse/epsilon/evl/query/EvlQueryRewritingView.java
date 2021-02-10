package org.eclipse.epsilon.evl.query;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.epsilon.common.dt.editor.ModelTypeExtensionFactory;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.dt.editor.EvlEditor;
import org.eclipse.epsilon.evl.parse.EvlUnparser;
import org.eclipse.epsilon.evl.staticanalyser.EvlStaticAnalyser;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.epsilon.common.dt.editor.AbstractModuleEditorSourceViewerConfiguration;

public class EvlQueryRewritingView extends ViewPart {
	protected IEditorPart editor;
	public String text;
	SourceViewer viewer;
	IEolCompilationContext context;
	Document translatedCode;

	public EvlQueryRewritingView() {
		super();
	}

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	public void createPartControl(Composite parent) {

		viewer = new SourceViewer(parent, null, SWT.H_SCROLL | SWT.V_SCROLL);

		SourceViewerConfiguration configuration = new AbstractModuleEditorSourceViewerConfiguration(new EvlEditor());
		configuration.getPresentationReconciler(viewer).install(viewer);
		viewer.configure(configuration);
		viewer.setEditable(false);
		Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);

		translatedCode = new Document();

		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new RefreshAction(this));

		if(getActiveEditorFilePath().split("\\.")[1].equals("evl"))
			doRewriting(getActiveEditorFilePath());
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
		if(getActiveEditorFilePath().split("\\.")[1].equals("eol"))
		doRewriting(getActiveEditorFilePath());
	}

	public void doRewriting(String activeFile) {
		EvlModule module = new EvlModule();

		try {
			module.parse(new File(activeFile));
		} catch (Exception e) {
			e.printStackTrace();
		}

		context = module.getCompilationContext();

		context.setModelFactory(new ModelTypeExtensionFactory());

		EvlStaticAnalyser staticAnlayser = new EvlStaticAnalyser();
		staticAnlayser.validate(module);
		

		new EvlRewritingHandler().invokeRewriters(module);
		
		translatedCode.set(new EvlUnparser().unparse(module));
		viewer.setDocument(translatedCode);
	}
}
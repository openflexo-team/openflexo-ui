package org.openflexo.fml.fib.widget;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutionException;


import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.fml.controller.widget.fmleditor.FMLEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.gina.test.OpenflexoFIBTestCase;
import org.openflexo.gina.test.SwingGraphicalContextDelegate;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;
import org.openflexo.foundation.lsp.LSPService;


@RunWith(OrderedRunner.class)
public class TestLspFMLEditor extends OpenflexoFIBTestCase {

	private static SwingGraphicalContextDelegate gcDelegate;

	private static Resource fibResource;

	static FlexoEditor editor;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	private static CompilationUnitResource fmlResource;

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void loadFMLResource() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		VirtualModel viewPoint = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml");
		assertNotNull(viewPoint);

		fmlResource = viewPoint.getResource();
		assertNotNull(fmlResource);

	}

	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testInstanciateFMLEditor() {

		FMLEditor editor = new FMLEditor(fmlResource, null);
		gcDelegate.addTab("FML Editor", editor);
		
	}

	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testInstanciateWidget() {

		fibResource = ResourceLocator.locateResource("Fib/FML/CompilationUnitView.fib");
		assertTrue(fibResource != null);
		FIBJPanel<FMLCompilationUnit> widget = instanciateFIB(fibResource, fmlResource.getCompilationUnit(), FMLCompilationUnit.class);
		FMLFIBController fibController = (FMLFIBController) widget.getController();
		InspectorGroup fmlInspectorGroup = new InspectorGroup(ResourceLocator.locateResource("Inspectors/FML"),
				ApplicationFIBLibraryImpl.instance(), null);
		fibController.setDefaultInspectorGroup(fmlInspectorGroup);

		// ModuleInspectorController inspectorController = new ModuleInspectorController(null);
		// fibController.setInspectorController
		gcDelegate.addTab("Standard GUI", widget.getController());
	}
	
	@Test
	@TestOrder(6)
	@Category(UITest.class)
	public void testInitLsp() {
		
		// LSPService initialization
        LSPService lspServer = serviceManager.getLspService();
	}

	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestLspFMLEditor.class.getSimpleName());
	}

	@AfterClass
	public static void waitGUI() {
		gcDelegate.waitGUI();
	}

	@Before
	public void setUp() {
		gcDelegate.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		gcDelegate.tearDown();
	}

}


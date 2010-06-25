package org.drools.guvnor.server.files;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.core.util.FileManager;
import org.drools.definition.KnowledgePackage;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.server.ServiceImplementation;
import org.drools.guvnor.server.util.TestEnvironmentSessionHelper;
import org.drools.io.ResourceChangeScannerConfiguration;
import org.drools.io.ResourceFactory;
import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;
import org.drools.util.codec.Base64;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class PackageDeploymentServletTest extends TestCase {

	public void FIXMEtestLoadingRules() throws Exception {
		RulesRepository repo = new RulesRepository( TestEnvironmentSessionHelper.getSession( true ) );

		ServiceImplementation impl = new ServiceImplementation();
		impl.repository = repo;

		PackageItem pkg = repo.createPackage("testPDSGetPackage", "");
		AssetItem header  = pkg.addAsset("drools", "");
		header.updateFormat("package");
		header.updateContent("import org.drools.SampleFact\n global org.drools.SampleFact sf");
		header.checkin("");


		AssetItem asset = pkg.addAsset("someRule", "");
		asset.updateContent("when \n SampleFact() \n then \n System.err.println(42);");
		asset.updateFormat(AssetFormats.DRL);
		asset.checkin("");

		assertNull(impl.buildPackage(pkg.getUUID(), true));



		//check source
		PackageDeploymentServlet serv = new PackageDeploymentServlet();
		MockHTTPRequest req = new MockHTTPRequest("/package/testPDSGetPackage/LATEST.drl", null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MockHTTPResponse res = new MockHTTPResponse(out);
		serv.doGet(req, res);

		assertNotNull(out.toByteArray());
		String drl = new String(out.toByteArray());
		assertTrue(drl.indexOf("rule") > -1);


		//now binary
		serv = new PackageDeploymentServlet();
		req = new MockHTTPRequest("/package/testPDSGetPackage/LATEST", null);
		out = new ByteArrayOutputStream();
		res = new MockHTTPResponse(out);
		serv.doGet(req, res);

		assertNotNull(out.toByteArray());
		byte[] bin = out.toByteArray();
		byte[] bin_ = pkg.getCompiledPackageBytes();

        org.drools.rule.Package o = (org.drools.rule.Package) DroolsStreamUtils.streamIn( new ByteArrayInputStream(bin) );
        assertNotNull(o);
        assertEquals(1, o.getRules().length);
        assertEquals(1, o.getGlobals().size());

		assertEquals(bin_.length, bin.length);

		assertSameArray(bin_, bin);

		//now some snapshots
		impl.createPackageSnapshot("testPDSGetPackage", "SNAP1", false, "hey");

		serv = new PackageDeploymentServlet();
		req = new MockHTTPRequest("/package/testPDSGetPackage/SNAP1.drl", null);
		out = new ByteArrayOutputStream();
		res = new MockHTTPResponse(out);
		serv.doGet(req, res);

		assertNotNull(out.toByteArray());
		drl = new String(out.toByteArray());
		assertTrue(drl.indexOf("rule") > -1);

		//now binary
		serv = new PackageDeploymentServlet();
		req = new MockHTTPRequest("/package/testPDSGetPackage/SNAP1", null);
		out = new ByteArrayOutputStream();
		res = new MockHTTPResponse(out);
		serv.doGet(req, res);

		assertNotNull(out.toByteArray());
		bin = out.toByteArray();
		bin_ = pkg.getCompiledPackageBytes();
		assertEquals(bin_.length, bin.length);


		//now get an individual asset source
		serv = new PackageDeploymentServlet();
		req = new MockHTTPRequest("/package/testPDSGetPackage/SNAP1/someRule.drl", null);
		out = new ByteArrayOutputStream();
		res = new MockHTTPResponse(out);
		serv.doGet(req, res);

		assertNotNull(out.toByteArray());
		drl = new String(out.toByteArray());
		System.err.println(drl);
		assertTrue(drl.indexOf("rule") > -1);
		assertEquals(-1, drl.indexOf("package"));


        //now test HEAD
        serv = new PackageDeploymentServlet();
        req = new MockHTTPRequest("/package/testPDSGetPackage/LATEST", null);
        req.method = "HEAD";
        out = new ByteArrayOutputStream();
        res = new MockHTTPResponse(out);
        serv.doHead(req, res);
        assertTrue(res.headers.size() > 0);
        String lm = res.headers.get("Last-Modified");
        assertNotNull(lm);

        serv = new PackageDeploymentServlet();
        req = new MockHTTPRequest("/package/testPDSGetPackage/LATEST", null);
        req.method = "HEAD";
        out = new ByteArrayOutputStream();
        res = new MockHTTPResponse(out);
        serv.doHead(req, res);
        assertTrue(res.headers.size() > 0);

        assertEquals(lm, res.headers.get("Last-Modified"));

        serv = new PackageDeploymentServlet();
        req = new MockHTTPRequest("/package/testPDSGetPackage/LATEST.drl", null);
        req.method = "HEAD";
        out = new ByteArrayOutputStream();
        res = new MockHTTPResponse(out);
        serv.doHead(req, res);
        assertTrue(res.headers.size() > 0);

        assertEquals(lm, res.headers.get("Last-Modified"));
        System.out.println(lm);


        //
        //now lets run it in a real server !
        //
        Server server = new Server(9000);

        Context ctx = new Context(server, "/", Context.SESSIONS);




        ctx.addServlet(new ServletHolder(new PackageDeploymentServlet()), "/package/*");
        

        server.setStopAtShutdown(true);
        server.start();

        ResourceFactory.getResourceChangeNotifierService().start();
        ResourceFactory.getResourceChangeScannerService().start();

        ResourceChangeScannerConfiguration sconf = ResourceFactory.getResourceChangeScannerService().newResourceChangeScannerConfiguration();
        sconf.setProperty( "drools.resource.scanner.interval", "1" );
        ResourceFactory.getResourceChangeScannerService().configure( sconf );



        String xml = "";
        xml += "<change-set xmlns='http://drools.org/drools-5.0/change-set'";
        xml += "    xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'";
        xml += "    xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >";
        xml += "    <add> ";
        xml += "        <resource source='http://localhost:9000/package/testPDSGetPackage/LATEST.drl' type='DRL' />";
        xml += "    </add> ";
        xml += "</change-set>";

        FileManager fileManager = new FileManager();
        fileManager.setUp();

        File fxml = fileManager.newFile( "changeset.xml" );
        Writer output = new BufferedWriter( new FileWriter( fxml ) );
        output.write( xml );
        output.close();


        KnowledgeAgent ag = KnowledgeAgentFactory.newKnowledgeAgent("fii", KnowledgeAgentFactory.newKnowledgeAgentConfiguration());
        ag.applyChangeSet( ResourceFactory.newUrlResource( fxml.toURI().toURL() ) );

        KnowledgeBase kb = ag.getKnowledgeBase();
        assertEquals(1, kb.getKnowledgePackages().size());
        KnowledgePackage kp = kb.getKnowledgePackages().iterator().next();
        assertTrue(kb.getKnowledgePackages().size() > 0);
        assertEquals(1, kp.getRules().size());


        //check the HEAD method
        HttpClient client = new HttpClient();
        HeadMethod hm = new HeadMethod("http://localhost:9000/package/testPDSGetPackage/LATEST.drl");
        client.executeMethod(hm);
        Header lastMod = hm.getResponseHeader("lastModified");
        Thread.sleep(50);
        long now = System.currentTimeMillis();
        long before = Long.parseLong(lastMod.getValue());
        assertTrue(before < now);


        //now lets add a rule
        asset = pkg.addAsset("someRule2", "");
        asset.updateContent("when \n SampleFact() \n then \n System.err.println(43);");
        asset.updateFormat(AssetFormats.DRL);
        asset.checkin("");

        assertNull(impl.buildPackage(pkg.getUUID(), true));



        Thread.sleep(3000);


        kb = ag.getKnowledgeBase();
        assertEquals(1, kb.getKnowledgePackages().size());
        kp = kb.getKnowledgePackages().iterator().next();

        if (kp.getRules().size() != 2) {
            Thread.sleep(2000);
            kb = ag.getKnowledgeBase();
            assertEquals(1, kb.getKnowledgePackages().size());
            kp = kb.getKnowledgePackages().iterator().next();
        }

        if (kp.getRules().size() != 2) {
            Thread.sleep(2000);
            kb = ag.getKnowledgeBase();
            assertEquals(1, kb.getKnowledgePackages().size());
            kp = kb.getKnowledgePackages().iterator().next();
        }
        
        assertEquals(2, kp.getRules().size());

        server.stop();
        repo.logout();

	}

	public void testScenariosAndChangeSet() throws Exception {
		RulesRepository repo = new RulesRepository( TestEnvironmentSessionHelper.getSession( true ) );

		ServiceImplementation impl = new ServiceImplementation();
		impl.repository = repo;

		repo.createPackage("testScenariosURL", "");
		impl.createPackageSnapshot("testScenariosURL", "SNAP1", false, "");

		Base64 enc = new Base64();
        String userpassword = "test" + ":" + "password";
        final String encodedAuthorization = enc.encodeToString( userpassword.getBytes() );      

        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("Authorization", "BASIC " + encodedAuthorization);
            }
        };
		//now run the scenarios
		PackageDeploymentServlet serv = new PackageDeploymentServlet();
		MockHTTPRequest req = new MockHTTPRequest("/package/testScenariosURL/LATEST/SCENARIOS", headers);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MockHTTPResponse res = new MockHTTPResponse(out);
		serv.doGet(req, res);
		String testResult = new String(out.toByteArray());
		assertNotNull(testResult);
		assertEquals("No test scenarios found.", testResult);


		serv = new PackageDeploymentServlet();
		req = new MockHTTPRequest("/package/testScenariosURL/SNAP1/SCENARIOS", headers);
		out = new ByteArrayOutputStream();
		res = new MockHTTPResponse(out);
		serv.doGet(req, res);
		testResult = new String(out.toByteArray());
		assertNotNull(testResult);
		assertEquals("No test scenarios found.", testResult);


        serv = new PackageDeploymentServlet();
        req = new MockHTTPRequest("/package/testScenariosURL/SNAP1/ChangeSet.xml", headers);
        req.url = new StringBuffer("http://foo/ChangeSet.xml");
        out = new ByteArrayOutputStream();
        res = new MockHTTPResponse(out);

        serv.doGet(req, res);
        testResult = new String(out.toByteArray());
        assertNotNull(testResult);
        assertTrue(testResult.indexOf("<resource source='http://foo' type='PKG' />") > 0);


	}

	private void assertSameArray(byte[] bin_, byte[] bin) {
		for (int i = 0; i < bin.length; i++) {
			assertEquals(bin_[i], bin[i]);
		}

	}

}

package org.drools.guvnor.server.files;

import java.io.File;
import java.io.InputStream;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.sf.webdav.ITransaction;

import org.apache.commons.io.IOUtils;
import org.drools.guvnor.server.util.TestEnvironmentSessionHelper;
import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;

public class WebDAVImplTest extends TestCase {

    public void testPath() {
        WebDAVImpl imp = new WebDAVImpl( new File( "" ) );
        String[] path = imp.getPath( "http://goober/whee/webdav/packages/packagename/resource.drl" );
        assertEquals( "packages",
                      path[0] );
        assertEquals( "packagename",
                      path[1] );
        assertEquals( "resource.drl",
                      path[2] );

        path = imp.getPath( "foo/webdav" );
        assertEquals( 0,
                      path.length );

        path = imp.getPath( "/" );
        assertEquals( 0,
                      path.length );

        path = imp.getPath( "/packages/packagename/resource.drl" );
        assertEquals( "packages",
                      path[0] );
        assertEquals( "packagename",
                      path[1] );
        assertEquals( "resource.drl",
                      path[2] );

    }

    public void testBadCopy() throws Exception {
        //OSX does stupid shit when copying in the same directory
        //for instance, it creates the copy as foobar.x copy - totally hosing
        //the file extension.
        WebDAVImpl imp = new WebDAVImpl( new File( "" ) );
        try {
            imp.objectExists( "/foo/webdav/packages/foobar/Something.drl copy 42" );
            fail( "should not be allowed" );
        } catch ( IllegalArgumentException e ) {
            assertNotNull( e.getMessage() );
        }

    }

    public void testListRoot() throws Exception {
        WebDAVImpl imp = new WebDAVImpl( new File( "" ) );
        String[] children = imp.getChildrenNames( new TransactionMock(),
                                                  "foobar/webdav" );
        assertEquals( 2,
                      children.length );
        assertEquals( "packages",
                      children[0] );
        assertEquals( "snapshots",
                      children[1] );
    }

    public void testChildrenNames() throws Exception {
        WebDAVImpl imp = getImpl();
        RulesRepository repo = imp.getRepo();
        String[] children = imp.getChildrenNames( new TransactionMock(),
                                                  "http://goo/webdav/packages" );
        assertTrue( children.length > 0 );
        int packageCount = children.length;

        PackageItem pkg = repo.createPackage( "testWebDavChildNames1",
                                              "" );
        repo.createPackage( "testWebDavChildNames2",
                            "" );
        repo.save();
        children = imp.getChildrenNames( new TransactionMock(),
                                         "http://goo/webdav/packages" );
        assertEquals( packageCount + 2,
                      children.length );
        assertContains( "testWebDavChildNames1",
                        children );
        assertContains( "testWebDavChildNames2",
                        children );

        AssetItem asset = pkg.addAsset( "asset1",
                                        "something" );
        asset.updateFormat( "drl" );
        asset.checkin( "" );
        asset = pkg.addAsset( "asset2",
                              "something" );
        asset.updateFormat( "dsl" );
        asset.checkin( "" );

        children = imp.getChildrenNames( new TransactionMock(),
                                         "foo/webdav/packages/testWebDavChildNames1" );
        assertEquals( 2,
                      children.length );
        assertEquals( "asset1.drl",
                      children[0] );
        assertEquals( "asset2.dsl",
                      children[1] );

        children = imp.getChildrenNames( new TransactionMock(),
                                         "foo/webdav/packages/testWebDavChildNames1/asset1.drl" );
        assertNull( children );

    }

    private WebDAVImpl getImpl() throws Exception {
        return new WebDAVImpl( new RulesRepository( TestEnvironmentSessionHelper.getSession( true ) ) );
    }

    public void testCreateFolder() throws Exception {
        WebDAVImpl imp = getImpl();
        RulesRepository repo = imp.getRepo();
        String[] children = imp.getChildrenNames( new TransactionMock(),
                                                  "http://goo/webdav/packages" );
        int packageCount = children.length;

        imp.createFolder( new TransactionMock(),
                          "foo/bar/webdav/packages/testCreateWebDavFolder" );
        children = imp.getChildrenNames( new TransactionMock(),
                                         "http://goo/webdav/packages" );

        assertEquals( packageCount + 1,
                      children.length );
        assertContains( "testCreateWebDavFolder",
                        children );

        PackageItem pkg = repo.loadPackage( "testCreateWebDavFolder" );
        assertNotNull( pkg );

        pkg.addAsset( "someAsset",
                      "" );

        try {
            imp.createFolder( new TransactionMock(),
                              "foo/bar/webdav/somethingElse" );
            fail( "this should not work !" );
        } catch ( UnsupportedOperationException e ) {
            assertNotNull( e.getMessage() );
        }

    }

    public void testDates() throws Exception {
        String uri = "/foo/webdav";
        WebDAVImpl imp = getImpl();
        assertNotNull( imp.getCreationDate( uri ) );
        assertNotNull( imp.getLastModified( uri ) );

        uri = "/foo/webdav/packages";
        assertNotNull( imp.getCreationDate( uri ) );
        assertNotNull( imp.getLastModified( uri ) );

    }

    public void testCreateResourceAndCreatedDate() throws Exception {
        WebDAVImpl imp = getImpl();
        RulesRepository repo = imp.getRepo();
        imp.createFolder( new TransactionMock(),
                          "foo/bar/webdav/packages/testCreateResourceDAVFolder" );

        Thread.sleep( 100 );

        imp.createResource( new TransactionMock(),
                            "fpp/bar/webdav/packages/testCreateResourceDAVFolder/asset.drl" );

        String[] resources = imp.getChildrenNames( new TransactionMock(),
                                                   "foo/bar/webdav/packages/testCreateResourceDAVFolder" );
        assertEquals( 1,
                      resources.length );
        assertEquals( "asset.drl",
                      resources[0] );

        //should be ignored
        imp.createResource( new TransactionMock(),
                            "fpp/bar/webdav/packages/testCreateResourceDAVFolder/._asset.drl" );
        imp.createResource( new TransactionMock(),
                            "fpp/bar/webdav/packages/.DS_Store" );

        PackageItem pkg = repo.loadPackage( "testCreateResourceDAVFolder" );
        assertFalse( pkg.containsAsset( "._asset" ) );
        assertTrue( pkg.containsAsset( "asset" ) );

        Iterator<AssetItem> it = pkg.getAssets();
        AssetItem ass = it.next();
        assertEquals( "asset",
                      ass.getName() );
        assertEquals( "drl",
                      ass.getFormat() );

        Date create = imp.getCreationDate( "foo/bar/webdav/packages/testCreateResourceDAVFolder" );
        assertNotNull( create );
        assertTrue( create.after( new Date( "10-Jul-1974" ) ) );

        Date assetCreate = imp.getCreationDate( "fpp/bar/webdav/packages/testCreateResourceDAVFolder/asset.drl" );
        assertTrue( assetCreate.after( create ) );

        Date lm = imp.getLastModified( "foo/bar/webdav/packages/testCreateResourceDAVFolder" );
        assertNotNull( lm );
        assertTrue( lm.after( new Date( "10-Jul-1974" ) ) );

        Date alm = imp.getLastModified( "fpp/bar/webdav/packages/testCreateResourceDAVFolder/asset.drl" );
        assertTrue( alm.after( lm ) );

        try {
            imp.createResource( new TransactionMock(),
                                "boo/bar/webdav/hummer.drl" );
            fail( "Shouldn't be able to do this" );
        } catch ( UnsupportedOperationException e ) {
            assertNotNull( e.getMessage() );
        }

    }

    public void testResourceContent() throws Exception {
        WebDAVImpl imp = getImpl();
        RulesRepository repo = imp.getRepo();
        PackageItem pkg = repo.createPackage( "testWebDAVContent",
                                              "" );

        AssetItem asset = pkg.addAsset( "asset",
                                        "something" );
        asset.updateFormat( "drl" );
        asset.updateContent( "Some content" );
        asset.checkin( "" );
        InputStream data = imp.getResourceContent( new TransactionMock(),
                                                   "foo/webdav/packages/testWebDAVContent/asset.drl" );
        assertEquals( "Some content",
                      IOUtils.toString( data ) );

        asset = pkg.addAsset( "asset2",
                              "something" );
        asset.updateFormat( "xls" );
        asset.updateBinaryContentAttachment( IOUtils.toInputStream( "This is binary" ) );
        asset.checkin( "" );

        data = imp.getResourceContent( new TransactionMock(),
                                       "foo/webdav/packages/testWebDAVContent/asset2.xls" );
        assertEquals( "This is binary",
                      IOUtils.toString( data ) );

        AssetItem asset_ = pkg.addAsset( "somethingelse",
                                         "" );
        asset_.updateFormat( "drl" );
        asset_.checkin( "" );

        data = imp.getResourceContent( new TransactionMock(),
                                       "foo/webdav/packages/testWebDAVContent/somethingelse.drl" );
        assertEquals( "",
                      IOUtils.toString( data ) );

    }

    public void testIsFolder() throws Exception {
        WebDAVImpl imp = getImpl();
        assertTrue( imp.isFolder( "/com/foo/webdav" ) );
        assertTrue( imp.isFolder( "/com/foo/webdav/" ) );
        assertTrue( imp.isFolder( "/com/foo/webdav/packages" ) );
        assertTrue( imp.isFolder( "/com/foo/webdav/packages/" ) );
        assertFalse( imp.isFolder( "/com/foo/webdav/packages/somePackage" ) );

        imp.createFolder( new TransactionMock(),
                          "/com/foo/webdav/packages/testDAVIsFolder" );
        assertTrue( imp.isFolder( "/com/foo/webdav/packages/testDAVIsFolder" ) );
        assertFalse( imp.isFolder( "/com/foo/webdav/packages/somePackage/SomeFile.drl" ) );
    }

    public void testIsResource() throws Exception {
        WebDAVImpl imp = getImpl();
        assertFalse( imp.isResource( "/com/foo/webdav/packages" ) );
        assertFalse( imp.isResource( "/com/foo/webdav/packages/somePackage" ) );
        assertFalse( imp.isResource( "/com/foo/webdav/packages/somePackage/SomeFile.drl" ) );

        imp.createFolder( new TransactionMock(),
                          "/com/foo/webdav/packages/testDAVIsResource" );
        imp.createResource( new TransactionMock(),
                            "/com/foo/webdav/packages/testDAVIsResource/SomeFile.drl" );

        assertTrue( imp.isResource( "/com/foo/webdav/packages/testDAVIsResource/SomeFile.drl" ) );

    }

    public void testResourceLength() throws Exception {
        WebDAVImpl imp = getImpl();
        assertEquals( 0,
                      imp.getResourceLength( new TransactionMock(),
                                             "foo/bar/webdav/packages" ) );
        imp.createFolder( new TransactionMock(),
                          "/foo/webdav/packages/testResourceLengthDAV" );
        imp.createResource( new TransactionMock(),
                            "/foo/webdav/packages/testResourceLengthDAV/testResourceLength" );
        assertEquals( 0,
                      imp.getResourceLength( new TransactionMock(),
                                             "/foo/webdav/packages/testResourceLengthDAV/testResourceLength" ) );
        imp.setResourceContent( new TransactionMock(),
                                "/foo/webdav/packages/testResourceLengthDAV/testResourceLength",
                                IOUtils.toInputStream( "some input" ),
                                null,
                                null );
        assertEquals( "some input".getBytes().length,
                      imp.getResourceLength( new TransactionMock(),
                                             "/foo/webdav/packages/testResourceLengthDAV/testResourceLength" ) );

    }

    public void testObjectExists() throws Exception {
        WebDAVImpl imp = getImpl();
        assertFalse( imp.objectExists( "foo/webdav/bar" ) );
        assertTrue( imp.objectExists( "foo/webdav" ) );
        assertTrue( imp.objectExists( "foo/webdav/packages" ) );

        imp.createFolder( new TransactionMock(),
                          "foo/webdav/packages/testDavObjectExists" );
        assertTrue( imp.objectExists( "foo/webdav/packages/testDavObjectExists" ) );
        assertFalse( imp.objectExists( "foo/webdav/packages/testDavObjectExistsXXXX" ) );
        assertFalse( imp.objectExists( "foo/webdav/packages/testDavObjectExists/foobar.drl" ) );
        assertFalse( imp.objectExists( "foo/webdav/packages/testDavObjectExistsXXXX/foobar.drl" ) );
    }

    public void testRemoveObject() throws Exception {
        WebDAVImpl imp = getImpl();
        assertFalse( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectFolder" ) );
        imp.createFolder( new TransactionMock(),
                          "foo/webdav/packages/testDavRemoveObjectFolder" );
        assertTrue( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectFolder" ) );
        imp.removeObject( new TransactionMock(),
                          "foo/webdav/packages/testDavRemoveObjectFolder" );
        assertFalse( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectFolder" ) );

        imp.createFolder( new TransactionMock(),
                          "foo/webdav/packages/testDavRemoveObjectAsset" );
        imp.createResource( new TransactionMock(),
                            "foo/webdav/packages/testDavRemoveObjectAsset/asset.drl" );

        AssetItem as = imp.getRepo().loadPackage( "testDavRemoveObjectAsset" ).loadAsset( "asset" );
        long origVer = as.getVersionNumber();

        assertTrue( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectAsset/asset.drl" ) );
        imp.removeObject( new TransactionMock(),
                          "foo/webdav/packages/testDavRemoveObjectAsset/asset.drl" );
        assertFalse( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectAsset/asset.drl" ) );
        assertTrue( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectAsset" ) );

        imp.createResource( new TransactionMock(),
                            "foo/webdav/packages/testDavRemoveObjectAsset/asset.drl" );
        assertTrue( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectAsset/asset.drl" ) );

        as = imp.getRepo().loadPackage( "testDavRemoveObjectAsset" ).loadAsset( "asset" );
        assertTrue( as.getVersionNumber() > origVer );
        imp.createFolder( new TransactionMock(),
                          "foo/webdav/packages/testDavRemoveObjectFolder" );
        assertTrue( imp.objectExists( "foo/webdav/packages/testDavRemoveObjectFolder" ) );

    }

    public void testSetContent() throws Exception {
        WebDAVImpl imp = getImpl();
        imp.createFolder( new TransactionMock(),
                          "/foo/webdav/packages/testSetDavContent" );
        imp.commit( new TransactionMock() );
        imp = getImpl();
        imp.createResource( new TransactionMock(),
                            "/foo/webdav/packages/testSetDavContent/Something.drl" );
        imp.commit( new TransactionMock() );
        imp = getImpl();
        imp.setResourceContent( new TransactionMock(),
                                "/foo/webdav/packages/testSetDavContent/Something.drl",
                                IOUtils.toInputStream( "some input" ),
                                null,
                                null );
        imp.commit( new TransactionMock() );
        imp = getImpl();
        imp.getResourceContent( new TransactionMock(),
                                "/foo/webdav/packages/testSetDavContent/Something.drl" );
        imp.commit( new TransactionMock() );
        imp = getImpl();

        AssetItem as = imp.getRepo().loadPackage( "testSetDavContent" ).loadAsset( "Something" );
        assertTrue( as.isBinary() );

        String result = IOUtils.toString( imp.getResourceContent( new TransactionMock(),
                                                                  "/foo/webdav/packages/testSetDavContent/Something.drl" ) );
        assertEquals( "some input",
                      result );

        PackageItem pkg = imp.getRepo().loadPackage( "testSetDavContent" );
        AssetItem asset = pkg.loadAsset( "Something" );
        assertEquals( "drl",
                      asset.getFormat() );
        assertEquals( "some input",
                      asset.getContent() );
        assertEquals( "some input",
                      IOUtils.toString( asset.getBinaryContentAttachment() ) );

        imp.setResourceContent( new TransactionMock(),
                                "/foo/webdav/packages/testSetDavContent/Something.drl",
                                IOUtils.toInputStream( "some more input" ),
                                null,
                                null );
        result = IOUtils.toString( imp.getResourceContent( new TransactionMock(),
                                                           "/foo/webdav/packages/testSetDavContent/Something.drl" ) );
        assertEquals( "some more input",
                      result );

    }

    public void testNewAsset() throws Exception {
        //simulating a full lifecycle of a new asset from webdav
        WebDAVImpl imp = getImpl();
        imp.createFolder( new TransactionMock(),
                          "/foo/webdav/packages/testDavNewAsset" );
        imp.commit( new TransactionMock() );
        imp = getImpl();

        assertFalse( imp.objectExists( "/foo/webdav/packages/testDavNewAsset/Blah.drl" ) );
        imp.commit( new TransactionMock() );
        imp = getImpl();
        imp.isFolder( "/packages/testDavNewAsset" );
        imp.isFolder( "/foo/webdav/packages/testDavNewAsset/Blah.drl" );
        assertFalse( imp.objectExists( "/foo/webdav/packages/testDavNewAsset/Blah.drl" ) );
        imp.createResource( new TransactionMock(),
                            "/foo/webdav/packages/testDavNewAsset/Blah.drl" );
        imp.setResourceContent( new TransactionMock(),
                                "/foo/webdav/packages/testDavNewAsset/Blah.drl",
                                IOUtils.toInputStream( "blah blah" ),
                                null,
                                null );
        imp.getResourceLength( new TransactionMock(),
                               "/foo/webdav/packages/testDavNewAsset/Blah.drl" );
        imp.commit( new TransactionMock() );
        imp = getImpl();

        assertTrue( imp.objectExists( "/foo/webdav/packages/testDavNewAsset/Blah.drl" ) );

    }

    public void testSnapshot() throws Exception {
        WebDAVImpl imp = getImpl();
        imp.createFolder( new TransactionMock(),
                          "/foo/webdav/packages/testDavSnapshot" );
        imp.createResource( new TransactionMock(),
                            "/foo/webdav/packages/testDavSnapshot/Something.drl" );
        imp.setResourceContent( new TransactionMock(),
                                "/foo/webdav/packages/testDavSnapshot/Something.drl",
                                IOUtils.toInputStream( "some input" ),
                                null,
                                null );

        RulesRepository repo = imp.getRepo();

        repo.createPackageSnapshot( "testDavSnapshot",
                                    "SNAP1" );
        repo.createPackageSnapshot( "testDavSnapshot",
                                    "SNAP2" );

        String[] packages = imp.getChildrenNames( new TransactionMock(),
                                                  "/foo/webdav/snapshots" );
        assertTrue( packages.length > 0 );
        assertContains( "testDavSnapshot",
                        packages );

        String[] snaps = imp.getChildrenNames( new TransactionMock(),
                                               "/foo/webdav/snapshots/testDavSnapshot" );
        assertEquals( 2,
                      snaps.length );

        assertEquals( "SNAP1",
                      snaps[0] );
        assertEquals( "SNAP2",
                      snaps[1] );

        String[] list = imp.getChildrenNames( new TransactionMock(),
                                              "/foo/webdav/snapshots/testDavSnapshot/SNAP1" );
        assertEquals( 1,
                      list.length );
        assertEquals( "Something.drl",
                      list[0] );

        list = imp.getChildrenNames( new TransactionMock(),
                                     "/foo/webdav/snapshots/testDavSnapshot/SNAP2" );
        assertEquals( 1,
                      list.length );
        assertEquals( "Something.drl",
                      list[0] );

        assertNotNull( imp.getCreationDate( "/foo/webdav/snapshots" ) );
        assertNotNull( imp.getCreationDate( "/foo/webdav/snapshots/testDavSnapshot" ) );
        assertNotNull( imp.getCreationDate( "/foo/webdav/snapshots/testDavSnapshot/SNAP1" ) );
        assertNotNull( imp.getCreationDate( "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" ) );

        assertNotNull( imp.getLastModified( "/foo/webdav/snapshots" ) );
        assertNotNull( imp.getLastModified( "/foo/webdav/snapshots/testDavSnapshot" ) );
        assertNotNull( imp.getLastModified( "/foo/webdav/snapshots/testDavSnapshot/SNAP1" ) );
        assertNotNull( imp.getLastModified( "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" ) );

        createFolderTry( imp,
                         "/foo/webdav/snapshots/randomAss" );
        createFolderTry( imp,
                         "/foo/webdav/snapshots/testDavSnapshot/SNAPX" );
        createFolderTry( imp,
                         "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" );
        createFolderTry( imp,
                         "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Another.drl" );

        createResourceTry( imp,
                           "/foo/webdav/snapshots/randomAss" );
        createResourceTry( imp,
                           "/foo/webdav/snapshots/testDavSnapshot/SNAPX" );
        createResourceTry( imp,
                           "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" );
        createResourceTry( imp,
                           "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Another.drl" );

        InputStream in = imp.getResourceContent( new TransactionMock(),
                                                 "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" );
        assertEquals( "some input",
                      IOUtils.toString( in ) );

        assertEquals( 0,
                      imp.getResourceLength( new TransactionMock(),
                                             "/foo/webdav/snapshots/testDavSnapshot/SNAP1" ) );
        assertEquals( "some input".getBytes().length,
                      imp.getResourceLength( new TransactionMock(),
                                             "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" ) );

        assertTrue( imp.isFolder( "/foo/webdav/snapshots" ) );
        assertTrue( imp.isFolder( "/foo/webdav/snapshots/testDavSnapshot" ) );
        assertTrue( imp.isFolder( "/foo/webdav/snapshots/testDavSnapshot/SNAP2" ) );
        assertFalse( imp.isFolder( "/foo/webdav/snapshots/testDavSnapshot/SNAP2/Something.drl" ) );

        assertFalse( imp.isResource( "/foo/webdav/snapshots" ) );
        assertFalse( imp.isResource( "/foo/webdav/snapshots/testDavSnapshot" ) );
        assertFalse( imp.isResource( "/foo/webdav/snapshots/testDavSnapshot/SNAP2" ) );
        assertTrue( imp.isResource( "/foo/webdav/snapshots/testDavSnapshot/SNAP2/Something.drl" ) );

        assertFalse( imp.isResource( "/foo/webdav/snapshots/testDavSnapshot/SNAP2/DoesNotExist.drl" ) );

        assertTrue( imp.objectExists( "/foo/webdav/snapshots" ) );
        assertFalse( imp.objectExists( "/foo/webdav/snapshots/testDavSnapshotXX" ) );
        assertTrue( imp.objectExists( "/foo/webdav/snapshots/testDavSnapshot" ) );
        assertTrue( imp.objectExists( "/foo/webdav/snapshots/testDavSnapshot/SNAP1" ) );
        assertFalse( imp.objectExists( "/foo/webdav/snapshots/testDavSnapshot/SNAPX" ) );

        assertFalse( imp.objectExists( "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Foo.drl" ) );
        assertTrue( imp.objectExists( "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" ) );

        assertNull( imp.getChildrenNames( new TransactionMock(),
                                          "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" ) );

        try {
            imp.removeObject( new TransactionMock(),
                              "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl" );
            fail( "Should not delete files from snapshots" );
        } catch ( Exception e ) {
            assertNotNull( e.getMessage() );
        }

        try {
            imp.setResourceContent( new TransactionMock(),
                                    "/foo/webdav/snapshots/testDavSnapshot/SNAP1/Something.drl",
                                    null,
                                    null,
                                    null );
            fail( "should not be allowed to update content in snapshots." );
        } catch ( Exception e ) {
            assertNotNull( e.getMessage() );
        }

        assertFalse( imp.objectExists( "/foo/webdav/snapshots/defaultPackage/new file" ) );
        try {
            imp.createResource( new TransactionMock(),
                                "/foo/webdav/snapshots/defaultPackage/new file" );
            fail( "can't touch this" );
        } catch ( UnsupportedOperationException e ) {
            assertNotNull( e.getMessage() );
        }
    }

    private void createResourceTry(WebDAVImpl imp,
                                   String path) {
        try {
            imp.createResource( new TransactionMock(),
                                path );
            fail( "Should not be allowed" );
        } catch ( UnsupportedOperationException e ) {
            assertNotNull( e.getMessage() );
        }
    }

    private void createFolderTry(WebDAVImpl imp,
                                 String path) {
        try {
            imp.createFolder( new TransactionMock(),
                              path );
            fail( "should not be allowed" );
        } catch ( UnsupportedOperationException e ) {
            assertNotNull( e.getMessage() );
        }
    }

    public void testThreadLocal() throws Exception {
        Thread t = new Thread( new Runnable() {
            public void run() {
                WebDAVImpl i = new WebDAVImpl();
                assertNull( i.getRepo() );
                try {
                    i.begin( null );
                } catch ( Exception e ) {
                    fail( "should not happen" );
                }
                assertNotNull( i.getRepo() );
            }
        } );
        t.start();
        t.join();
    }

    private void assertContains(String string,
                                String[] children) {
        for ( int i = 0; i < children.length; i++ ) {
            if ( children[i].equals( string ) ) {
                return;
            }
        }
        Assert.fail( "Array did not contain " + string );
    }

    static class TransactionMock
        implements
        ITransaction {

        public Principal getPrincipal() {
            return null;
        }

    }

}

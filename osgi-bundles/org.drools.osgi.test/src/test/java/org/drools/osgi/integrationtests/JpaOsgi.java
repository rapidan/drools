package org.drools.osgi.integrationtests;


//import bitronix.tm.TransactionManagerServices;
//import bitronix.tm.resource.jdbc.PoolingDataSource;

public class JpaOsgi {

	public void testDummyMethod() {
	}
//    PoolingDataSource            ds1;
//    private EntityManagerFactory emf;
//
//    protected void onSetUp() throws Exception {
//        super.onSetUp();
//        
//        ds1 = new PoolingDataSource();
//        ds1.setUniqueName( "jdbc/testDS1" );
//        ds1.setClassName( "org.h2.jdbcx.JdbcDataSource" );
//        ds1.setMaxPoolSize( 3 );
//        ds1.setAllowLocalTransactions( true );
//        ds1.getDriverProperties().put( "user",
//                                       "sa" );
//        ds1.getDriverProperties().put( "password",
//                                       "sasa" );
//        ds1.getDriverProperties().put( "URL",
//                                       "jdbc:h2:mem:mydb" );
//        ds1.init();
//        //        
//        //        Bundle jpaBundle = null;
//        //         for ( Bundle bundle : bundleContext.getBundles() ) {
//        //             if ( "org.drools.persistence-jpa".equals( bundle.getSymbolicName() ) ) {
//        //                 jpaBundle = bundle;
//        //                 break;
//        //             }
//        //        }
//
//        URL url = getClass().getClassLoader().getResource( "META-INF/persistence.xml" );
//        System.out.println( url );
//        
//        //System.out.println( HibernatePersistence.class );
//        //emf = Persistence.createEntityManagerFactory( "org.drools.persistence.jpa" );
//
//    }
//
//    protected void onTearDown() throws Exception {
//        emf.close();
//        ds1.close();
//        super.onTearDown();
//    }
//
//        public void FIXME_testPersistenceWithUserTransactions() throws Exception {
//        ServiceReference serviceRef = bundleContext.getServiceReference( ServiceRegistry.class.getName() );
//        ServiceRegistry registry = (ServiceRegistry) bundleContext.getService( serviceRef );
//
//        KnowledgeBuilderFactoryService knowledgeBuilderFactoryService = registry.get( KnowledgeBuilderFactoryService.class );
//        KnowledgeBaseFactoryService knowledgeBaseFactoryService = registry.get( KnowledgeBaseFactoryService.class );
//        ResourceFactoryService resourceFactoryService = registry.get( ResourceFactoryService.class );
//
//        String str = "";
//        str += "package org.drools.test\n";
//        str += "global java.util.List list\n";
//        str += "rule rule1\n";
//        str += "when\n";
//        str += "  $i : Integer(intValue > 0)\n";
//        str += "then\n";
//        str += "  list.add( $i );\n";
//        str += "end\n";
//        str += "\n";
//
//        KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder();
//        kbuilder.add( resourceFactoryService.newByteArrayResource( str.getBytes() ),
//                      ResourceType.DRL );
//        KnowledgeBase kbase = knowledgeBaseFactoryService.newKnowledgeBase();
//
//        if ( kbuilder.hasErrors() ) {
//            fail( kbuilder.getErrors().toString() );
//        }
//
//        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
//
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.persistence.jpa" );
//        Environment env = knowledgeBaseFactoryService.newEnvironment();
//        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
//                 emf );
//        env.set( EnvironmentName.TRANSACTION_MANAGER,
//                 TransactionManagerServices.getTransactionManager() );
//        env.set( EnvironmentName.GLOBALS,
//                 new MapGlobalResolver() );
//
//        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        JPAKnowledgeServiceProviderImpl jpaService = new JPAKnowledgeServiceProviderImpl();
//
//        StatefulKnowledgeSession ksession = jpaService.newStatefulKnowledgeSession( kbase,
//                                                                                    null,
//                                                                                    env );
//        ut.commit();
//
//        //      EntityManager em = emf.createEntityManager();
//        //      SessionInfo sInfo = em.find( SessionInfo.class, 1 );
//        //      assertNotNull( sInfo );
//        //      //System.out.println( "session creation : " + sInfo.getVersion() );
//        //      em.close();
//
//        List list = new ArrayList();
//
//        // insert and commit
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession.setGlobal( "list",
//                            list );
//        ksession.insert( 1 );
//        ksession.insert( 2 );
//        ut.commit();
//        //
//        // insert and rollback
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession.insert( 3 );
//        ut.rollback();
//
//        // check we rolled back the state changes from the 3rd insert
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession.fireAllRules();
//        ut.commit();
//        assertEquals( 2,
//                      list.size() );
//
//        // insert and commit
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession.insert( 3 );
//        ksession.insert( 4 );
//        ut.commit();
//
//        // rollback again, this is testing that we can do consequetive rollbacks and commits without issue
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession.insert( 5 );
//        ksession.insert( 6 );
//        ut.rollback();
//
//        ksession.fireAllRules();
//
//        assertEquals( 4,
//                      list.size() );
//
//        // now load the ksession
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession = jpaService.loadStatefulKnowledgeSession( ksession.getId(),
//                                                            kbase,
//                                                            null,
//                                                            env );
//        ut.commit();
//
//        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//        ut.begin();
//        ksession.insert( 7 );
//        ksession.insert( 8 );
//        ut.commit();
//
//        ksession.fireAllRules();
//
//        assertEquals( 6,
//                      list.size() );
//    }

}
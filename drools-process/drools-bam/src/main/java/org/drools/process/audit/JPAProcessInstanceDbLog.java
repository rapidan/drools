package org.drools.process.audit;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class JPAProcessInstanceDbLog {
	
	private EntityManagerFactory emf;
	private EntityManager em;
    
    @SuppressWarnings("unchecked")
	public List<ProcessInstanceLog> findProcessInstances() {
    	List<ProcessInstanceLog> result = getEntityManager()
    		.createQuery("FROM ProcessInstanceLog").getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
	public List<ProcessInstanceLog> findProcessInstances(String processId) {
    	List<ProcessInstanceLog> result = getEntityManager()
    		.createQuery("FROM ProcessInstanceLog p WHERE p.processId = :processId")
    			.setParameter("processId", processId).getResultList();
    	return result;
    }

	public ProcessInstanceLog findProcessInstance(long processInstanceId) {
    	ProcessInstanceLog result = (ProcessInstanceLog) getEntityManager()
			.createQuery("FROM ProcessInstanceLog p WHERE p.processInstanceId = :processInstanceId")
				.setParameter("processInstanceId", processInstanceId).getSingleResult();
		return result;
    }

    @SuppressWarnings("unchecked")
	public List<NodeInstanceLog> findNodeInstances(long processInstanceId) {
    	List<NodeInstanceLog> result = getEntityManager()
			.createQuery("FROM NodeInstanceLog n WHERE n.processInstanceId = :processInstanceId ORDER BY date")
				.setParameter("processInstanceId", processInstanceId).getResultList();
		return result;
    }

    @SuppressWarnings("unchecked")
	public List<NodeInstanceLog> findNodeInstances(long processInstanceId, String nodeId) {
    	List<NodeInstanceLog> result = getEntityManager()
			.createQuery("FROM NodeInstanceLog n WHERE WHERE n.processInstanceId = :processInstanceId AND n.nodeId = :nodeId ORDER BY date")
				.setParameter("processInstanceId", processInstanceId)
				.setParameter("nodeId", nodeId).getResultList();
    	return result;
    }

	@SuppressWarnings("unchecked")
	public void clear() {
	    try {
	        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
	        ut.begin();
	        List<ProcessInstanceLog> processInstances = getEntityManager()
				.createQuery("FROM ProcessInstanceLog").getResultList();
	        for (ProcessInstanceLog processInstance: processInstances) {
	        	getEntityManager().remove(processInstance);
	        }
	    	List<NodeInstanceLog> nodeInstances = getEntityManager()
				.createQuery("FROM NodeInstanceLog").getResultList();
		    for (NodeInstanceLog nodeInstance: nodeInstances) {
		    	getEntityManager().remove(nodeInstance);
		    }
		    em.joinTransaction();
			ut.commit();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (RollbackException e) {
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		}
    }
	
	private EntityManager getEntityManager() {
		if (emf == null) {
			emf = Persistence.createEntityManagerFactory("org.drools.persistence.jpa");
		}
		if (em == null) {
		    em = emf.createEntityManager();
		}
		return em;
	}
	
	public void dispose() {
		em.close();
		emf.close();
	}

}

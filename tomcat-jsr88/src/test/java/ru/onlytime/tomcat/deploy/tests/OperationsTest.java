package ru.onlytime.tomcat.deploy.tests;

import org.junit.Before;
import org.junit.Test;

import ru.onlytime.tomcat.deploy.DeploymentManagerImpl;

public class OperationsTest
{
	DeploymentManagerImpl manager;

	@Test
	public void testAvailableModules() throws Exception
	{
//		TargetModuleID[] result = manager.getAvailableModules(ModuleType.WAR, manager.getTargets());
//		for (TargetModuleID curModule : result) {
//			System.out.println("id: " + curModule.getModuleID() + " context root: " + curModule.getWebURL());
//		}
	}

	@Test
	public void testNonRunningModules() throws Exception
	{
//		TargetModuleID[] result = manager.getNonRunningModules(ModuleType.WAR, manager.getTargets());
//		for (TargetModuleID curModule : result) {
//			System.out.println("id: " + curModule.getModuleID() + " context root: " + curModule.getWebURL());
//		}
	}

	@Test
	public void testRunningModules() throws Exception
	{
//		TargetModuleID[] result = manager.getRunningModules(ModuleType.WAR, manager.getTargets());
//		for (TargetModuleID curModule : result) {
//			System.out.println("id: " + curModule.getModuleID() + " context root: " + curModule.getWebURL());
//		}
	}

	@Test
	public void testDistributeStartStopUndeploy() throws Exception
	{
//		ProgressObject result = manager.distribute(manager.getTargets(), new File("./src/test/resources/sample-webapp.war"), null);
//		System.out.println("deploy result:");
//		System.out.println(result.getDeploymentStatus().getMessage());
//		System.out.println(result.getResultTargetModuleIDs()[0].getModuleID());
//		System.out.println(result.getResultTargetModuleIDs()[0].getWebURL());
//		System.out.println(result.getDeploymentStatus().isCompleted());
//		result = manager.start(result.getResultTargetModuleIDs());
//		System.out.println("start result:");
//		System.out.println(result.getDeploymentStatus().getMessage());
//		System.out.println(result.getResultTargetModuleIDs()[0].getModuleID());
//		System.out.println(result.getResultTargetModuleIDs()[0].getWebURL());
//		System.out.println(result.getDeploymentStatus().isCompleted());
//		result = manager.stop(result.getResultTargetModuleIDs());
//		System.out.println("stop result:");
//		System.out.println(result.getDeploymentStatus().getMessage());
//		System.out.println(result.getResultTargetModuleIDs()[0].getModuleID());
//		System.out.println(result.getResultTargetModuleIDs()[0].getWebURL());
//		System.out.println(result.getDeploymentStatus().isCompleted());
//		
//		result = manager.redeploy(result.getResultTargetModuleIDs(), new File("./src/test/resources/sample-webapp-2.war"), null);
//		System.out.println("redeploy result:");
//		System.out.println(result.getDeploymentStatus().getMessage());
//		System.out.println(result.getResultTargetModuleIDs()[0].getModuleID());
//		System.out.println(result.getResultTargetModuleIDs()[0].getWebURL());
//		System.out.println(result.getDeploymentStatus().isCompleted());
//		
//		result = manager.undeploy(result.getResultTargetModuleIDs());
//		System.out.println("undeploy result:");
//		System.out.println(result.getDeploymentStatus().getMessage());
//		System.out.println(result.getResultTargetModuleIDs()[0].getModuleID());
//		System.out.println(result.getResultTargetModuleIDs()[0].getWebURL());
//		System.out.println(result.getDeploymentStatus().isCompleted());
	}

	@Before
	public void start()
	{
//		manager = new DeploymentManagerImpl("localhost", 8099, "tomcat", "tomcat");
	}

}

package org.onlytime.tomcat.deploy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

public class DeploymentFactoryImpl implements DeploymentFactory
{
	private final static String URI_PREFIX = "deployer:org.onlytime.tomcat:";
	private final static Pattern appPattern = Pattern.compile(URI_PREFIX + "(\\w+):(\\d+)");

	public DeploymentManager getDeploymentManager(String uri, String login, String password) throws DeploymentManagerCreationException
	{
		Matcher m = appPattern.matcher(uri);
		if (!m.find()) {
			throw new DeploymentManagerCreationException("wrong uri. example: deployer:org.onlytime.tomcat:localhost:8080");
		}
		String host = m.group(1);
		String portStr = m.group(2);
		return new DeploymentManagerImpl(host, Integer.parseInt(portStr), login, password);
	}

	public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException
	{
		throw new UnsupportedOperationException("not implemented");
	}

	public String getDisplayName()
	{
		return "Tomcat remote jsr88 deployer";
	}

	public String getProductVersion()
	{
		return "1.0.0";
	}

	public boolean handlesURI(String uri)
	{
		Matcher m = appPattern.matcher(uri);
		if (m.find()) {
			return true;
		}
		return false;
	}

}

package org.onlytime.tomcat.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.onlytime.tomcat.deploy.spi.DeploymentStatusImpl;
import org.onlytime.tomcat.deploy.spi.ProgressObjectImpl;
import org.onlytime.tomcat.deploy.spi.TargetImpl;
import org.onlytime.tomcat.deploy.spi.TargetModuleIDImpl;

public class DeploymentManagerImpl implements DeploymentManager
{
	private final static Log log = LogFactory.getLog(DeploymentManagerImpl.class);

	private HttpClient client = new HttpClient();
	private URL url;
	private String host;

	public DeploymentManagerImpl(String host, Integer port, String login, String password)
	{
		try {
			url = new URL("http://" + host + ":" + port.toString() + "/manager/");
		} catch (MalformedURLException e) {
			log.error("wrong url.", e);
			throw new IllegalArgumentException("couldnt create url", e);
		}
		Credentials defaultcreds = new UsernamePasswordCredentials(login, password);
		client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), defaultcreds);

		this.host = host;
	}

	public ProgressObject distribute(Target[] targets, ModuleType type, InputStream archive, InputStream specPlan) throws IllegalStateException
	{
		if (!type.equals(ModuleType.WAR)) {
			throw new IllegalStateException("Only WAR is supported by tomcat");
		}
		if (targets == null || targets.length == 0) {
			throw new IllegalArgumentException("wrong targets specified");
		}
		String tag = "/jsr88-" + String.valueOf(System.currentTimeMillis());
		TargetModuleIDImpl moduleID = new TargetModuleIDImpl(targets[0], tag);
		
		return deploy(moduleID,archive);
	}

	public ProgressObject distribute(Target[] targets, File archive, File specPlan) throws IllegalStateException
	{
		if (!archive.getName().endsWith(".war")) {
			throw new IllegalArgumentException("Only .war archive is supported");
		}
		InputStream archiveStream;
		try {
			archiveStream = new FileInputStream(archive);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return distribute(targets, ModuleType.WAR, archiveStream, null);
	}

	public TargetModuleID[] getAvailableModules(ModuleType type, Target[] targets) throws TargetException, IllegalStateException
	{
		if (!type.equals(ModuleType.WAR)) {
			throw new IllegalStateException("Only WAR is supported");
		}
		if (targets == null || targets.length == 0) {
			throw new TargetException("wrong targets specified");
		}

		GetMethod list = new GetMethod(url.toString() + "list");
		try {
			int code = client.executeMethod(list);
			if (code != 200) {
				throw new TargetException("cannot obtain modules");
			}

			String response = list.getResponseBodyAsString();
			String[] lines = response.split("\\n");
			if (lines.length < 2) {
				throw new TargetException("cannot obtain modules");
			}

			List<TargetModuleID> result = new LinkedList<TargetModuleID>();

			for (int i = 1; i < lines.length; i++) {

				String[] stats = lines[i].split(":");

				TargetModuleIDImpl curModule = new TargetModuleIDImpl(targets[0], stats[0]);
				if (stats[1].equals("running")) {
					curModule.setRunning(true);
				} else {
					curModule.setRunning(false);
				}

				result.add(curModule);
			}

			list.releaseConnection();
			return result.toArray(new TargetModuleID[0]);

		} catch (Exception e) {
			log.error("cannot obtain modules", e);
			throw new TargetException("cannot obtain modules");
		}
	}

	public TargetModuleID[] getNonRunningModules(ModuleType type, Target[] targets) throws TargetException, IllegalStateException
	{
		TargetModuleID[] allModules = getAvailableModules(type, targets);
		List<TargetModuleID> result = new LinkedList<TargetModuleID>();

		for (TargetModuleID curModule : allModules) {

			if (!((TargetModuleIDImpl) curModule).isRunning()) {
				result.add(curModule);
			}

		}

		return result.toArray(new TargetModuleID[0]);
	}

	public TargetModuleID[] getRunningModules(ModuleType type, Target[] targets) throws TargetException, IllegalStateException
	{
		TargetModuleID[] allModules = getAvailableModules(type, targets);
		List<TargetModuleID> result = new LinkedList<TargetModuleID>();

		for (TargetModuleID curModule : allModules) {

			if (((TargetModuleIDImpl) curModule).isRunning()) {
				result.add(curModule);
			}

		}

		return result.toArray(new TargetModuleID[0]);
	}

	public boolean isRedeploySupported()
	{
		return true;
	}

	public ProgressObject redeploy(TargetModuleID[] modules, File archive, File arg2) throws UnsupportedOperationException, IllegalStateException
	{
		if (!archive.getName().endsWith(".war")) {
			throw new IllegalArgumentException("Only .war archive is supported");
		}
		InputStream archiveStream;
		try {
			archiveStream = new FileInputStream(archive);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return redeploy(modules, archiveStream, null);
	}

	public ProgressObject redeploy(TargetModuleID[] modules, InputStream archive, InputStream arg2) throws UnsupportedOperationException, IllegalStateException
	{
		ProgressObject result = deploy(modules[0], archive);
		if( result.getDeploymentStatus().isFailed() ) {
			DeploymentStatusImpl status = new DeploymentStatusImpl(result.getDeploymentStatus().getMessage(), CommandType.REDEPLOY, true);
			result = new ProgressObjectImpl(status, modules[0]);
		} else {
			DeploymentStatusImpl status = new DeploymentStatusImpl(result.getDeploymentStatus().getMessage(), CommandType.REDEPLOY, false);
			result = new ProgressObjectImpl(status, modules[0]);
		}
		
		return result;
	}

	public ProgressObject start(TargetModuleID[] modules) throws IllegalStateException
	{
		if (modules == null || modules.length == 0) {
			throw new IllegalArgumentException("wrong modules");
		}

		ProgressObjectImpl result;

		GetMethod stop = new GetMethod(url.toString() + "start");
		stop.setQueryString(new NameValuePair[] { new NameValuePair("path", modules[0].getModuleID()) });
		try {
			int code = client.executeMethod(stop);
			if (code != 200) {
				DeploymentStatusImpl status = new DeploymentStatusImpl("wrong response from manager. code: " + code, CommandType.START, true);
				result = new ProgressObjectImpl(status, modules[0]);
				return result;
			}

			String response = stop.getResponseBodyAsString();
			boolean isFail = false;
			if (response.startsWith("FAIL")) {
				isFail = true;
			}

			DeploymentStatusImpl status = new DeploymentStatusImpl(response, CommandType.START, isFail);
			result = new ProgressObjectImpl(status, modules[0]);
			return result;

		} catch (Exception e) {
			DeploymentStatusImpl status = new DeploymentStatusImpl(e.getMessage(), CommandType.START, true);
			result = new ProgressObjectImpl(status, modules[0]);
			return result;
		}
	}

	public ProgressObject stop(TargetModuleID[] modules) throws IllegalStateException
	{
		if (modules == null || modules.length == 0) {
			throw new IllegalArgumentException("wrong modules");
		}

		ProgressObjectImpl result;

		GetMethod stop = new GetMethod(url.toString() + "stop");
		stop.setQueryString(new NameValuePair[] { new NameValuePair("path", modules[0].getModuleID()) });
		try {
			int code = client.executeMethod(stop);
			if (code != 200) {
				DeploymentStatusImpl status = new DeploymentStatusImpl("wrong response from manager. code: " + code, CommandType.STOP, true);
				result = new ProgressObjectImpl(status, modules[0]);
				return result;
			}

			String response = stop.getResponseBodyAsString();
			boolean isFail = false;
			if (response.startsWith("FAIL")) {
				isFail = true;
			}

			DeploymentStatusImpl status = new DeploymentStatusImpl(response, CommandType.STOP, isFail);
			result = new ProgressObjectImpl(status, modules[0]);
			return result;

		} catch (Exception e) {
			DeploymentStatusImpl status = new DeploymentStatusImpl(e.getMessage(), CommandType.STOP, true);
			result = new ProgressObjectImpl(status, modules[0]);
			return result;
		}
	}

	public ProgressObject undeploy(TargetModuleID[] modules) throws IllegalStateException
	{
		if (modules == null || modules.length == 0) {
			throw new IllegalArgumentException("wrong modules");
		}
		
		ProgressObjectImpl result;

		GetMethod stop = new GetMethod(url.toString() + "undeploy");
		stop.setQueryString(new NameValuePair[] { new NameValuePair("path", modules[0].getModuleID()) });
		try {
			int code = client.executeMethod(stop);
			if (code != 200) {
				DeploymentStatusImpl status = new DeploymentStatusImpl("wrong response from manager. code: " + code, CommandType.UNDEPLOY, true);
				result = new ProgressObjectImpl(status, modules[0]);
				return result;
			}

			String response = stop.getResponseBodyAsString();
			boolean isFail = false;
			if (response.startsWith("FAIL")) {
				isFail = true;
			}

			DeploymentStatusImpl status = new DeploymentStatusImpl(response, CommandType.UNDEPLOY, isFail);
			result = new ProgressObjectImpl(status, modules[0]);
			return result;

		} catch (Exception e) {
			DeploymentStatusImpl status = new DeploymentStatusImpl(e.getMessage(), CommandType.UNDEPLOY, true);
			result = new ProgressObjectImpl(status, modules[0]);
			return result;
		}
	}

	private ProgressObject deploy(TargetModuleID module, InputStream archive)
	{
		ProgressObject result;
		PutMethod deploy = new PutMethod(url.toString() + "deploy");
		deploy.setRequestEntity(new InputStreamRequestEntity(archive));
		deploy.setQueryString(new NameValuePair[] { new NameValuePair("tag", module.getModuleID()), new NameValuePair("path", module.getWebURL()), new NameValuePair("update", "true") });
		try {
			int code = client.executeMethod(deploy);
			if (code != 200) {
				DeploymentStatusImpl status = new DeploymentStatusImpl("wrong response code: " + code, CommandType.DISTRIBUTE, true);
				result = new ProgressObjectImpl(status, null);
				return result;
			}

			String response = deploy.getResponseBodyAsString();

			if (response.startsWith("FAIL")) {
				DeploymentStatusImpl status = new DeploymentStatusImpl(response, CommandType.DISTRIBUTE, true);
				result = new ProgressObjectImpl(status, module);
				return result;
			}

			//stop after deploy. tomcat feature

			result = stop(new TargetModuleID[] { module });
			if (result.getDeploymentStatus().isCompleted()) {
				DeploymentStatusImpl status = new DeploymentStatusImpl(response, CommandType.DISTRIBUTE, false);
				result = new ProgressObjectImpl(status, module);
			} else {
				DeploymentStatusImpl status = new DeploymentStatusImpl(result.getDeploymentStatus().getMessage(), CommandType.DISTRIBUTE, true);
				result = new ProgressObjectImpl(status, module);
			}

			return result;

		} catch (Exception e) {
			log.error("cannot obtain modules", e);
			DeploymentStatusImpl status = new DeploymentStatusImpl("cannot process request", CommandType.DISTRIBUTE, true);
			result = new ProgressObjectImpl(status, null);
			return result;
		}

	}
	
	public DeploymentConfiguration createConfiguration(DeployableObject arg0) throws InvalidModuleException
	{
		throw new UnsupportedOperationException();
	}

	public ProgressObject distribute(Target[] arg0, InputStream arg1, InputStream arg2) throws IllegalStateException
	{
		throw new UnsupportedOperationException();
	}

	public Locale getCurrentLocale()
	{
		throw new UnsupportedOperationException();
	}

	public DConfigBeanVersionType getDConfigBeanVersion()
	{
		throw new UnsupportedOperationException();
	}

	public Locale getDefaultLocale()
	{
		throw new UnsupportedOperationException();
	}

	public Locale[] getSupportedLocales()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType arg0)
	{
		return false;
	}

	public void release()
	{
	}

	public boolean isLocaleSupported(Locale arg0)
	{
		return false;
	}

	public void setDConfigBeanVersion(DConfigBeanVersionType arg0) throws DConfigBeanVersionUnsupportedException
	{
		throw new UnsupportedOperationException();
	}

	public void setLocale(Locale arg0) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	public Target[] getTargets() throws IllegalStateException
	{
		return new Target[] { new TargetImpl(host) };
	}
}

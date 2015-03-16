[JSR-88](http://jcp.org/en/jsr/detail?id=88) implementation for tomcat.

Implementation based on Tomcat [Manager](http://tomcat.apache.org/tomcat-5.5-doc/manager-howto.html)

## Implementation note ##

  * In Tomcat remote deploy is equals to deploy+start. Implementation first deploy then stop application.
  * Only ModuleType.WAR is supported for tomcat. All other module types cause IllegalStateException
  * Deploy plan is ignored
  * J2EE-DeploymentFactory-Implementation-Class is ru.onlytime.tomcat.deploy.DeploymentFactoryImpl
  * connection URL: `deployer:ru.onlytime.tomcat:<host>:<port>`
  * redeploy is supported
  * locale is not supported
  * DConfigBeanVersion is not supported

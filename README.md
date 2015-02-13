# Welcome to the Student Application

The Student application allows users to take tests either as a guest student or using the session ID generated by a Proctor. A student may also log in using a Guest session. If using an actual session ID, a request goes to the Proctor for approval (via the Proctor app). Once the Proctor approves the request, the student can begin the test. 

## License ##
This project is licensed under the [AIR Open Source License v1.0](http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf).

## Getting Involved ##
We would be happy to receive feedback on its capabilities, problems, or future enhancements:

* For general questions or discussions, please use the [Forum](http://forum.opentestsystem.org/viewforum.php?f=9).
* Use the **Issues** link to file bugs or enhancement requests.
* Feel free to **Fork** this project and develop your changes!

## Module Overview

### Webapp
The Webapp module contains the Student UI and REST APIs.

### ResourceBundler
The ResourceBundler module provides the classes to parse the xml files in Student's 'Scripts' folder which contains the configuration for loading java scripts in the pages in run time.When the jsf pages' custom tags are executed, the code behind invokes those classes to load appropriate java scripts and css files.

### SpellCheck
The SpellCheck module contains REST APIs related to dictionary and spell check for student tests with passages. It contains integration to the [Hunspell](http://hunspell.sourceforge.net) Engine for spell check.

### IRiS
IRiS (Item Rendering System) provides a web service to render content using the same control paths that the student application uses except that it is very lightweight. This rendered content can be embedded in an iFrame for purposes such as item review.

#### Deploying Content to IRiS
IRiS is a WAR file that can be deployed to its own Tomcat web container. The easiest way to deploy content is to scp the file to the server. Content needs to be deployed to the folder `/usr/local/tomcat/content`.
Once content has been deployed as above, the system should pick it up automatically. However, this feature may not be reliable - especially when deploying massive number of files by copying from a remote location. The only reliable way to make sure the system picks up all newly deployed content is to hit this API endpoint:  `/iris/Pages/API/content/reload`. This is a blocking call and is an initial attempt at providing an API that doesn’t involve restarting the server.

### Student.library
Rendering is now common to three different projects: Proctor, Student and IRiS. All three projects however have different underlying business rules. All such interfaces have been abstracted in student.library and the concrete implementations are provided by each individual application using Spring dependency injection.

## Setup
In general, building the code and deploying the WAR file is a good first step.  The Student application, however, has a number of other steps that need to be performed in order to fully setup the system.

### Config Folder
Within the file system of the deployment (local file system if running locally or within Tomcat file directories), create a configuration folder structure as follows:
```
{CONFIG-FOLDER-NAME}/progman/
example: /my-app-config/progman/
``` 
Within the deepest folder ('/progman/'), place a file named 'pm-client-security.properties' with the following contents:

```
#security props
oauth.access.url={the URL of OAuth2 access token provider}
pm.oauth.client.id={Client ID for program management client, can be shared amongst all client users or application/consumer specific values}
pm.oauth.client.secret={Password for program management client, can be shared amongst all client users or application/consumer specific values}
pm.oauth.batch.account={OAuth Client id configured in OAM to allow get an OAuth token for the ‘batch' web service call to program management(for loading configs during start up)}
pm.oauth.batch.password={OAuth Client secret/password configured in OAM to allow get an OAuth token for the ‘batch' web service call to program management(for loading configs during start up)}
oauth.testreg.client.id={OAuth test client ID for test registration}
oauth.testreg.client.secret={OAuth client secret for test registration}
oauth.testreg.client.granttype={OAuth grant type for test registration}
oauth.testreg.username={OAuth username for test registration}
oauth.testreg.password={OAuth password for test registration} 

working example:
oauth.access.url=https://drc-dev-secure.opentestsystem.org/auth/oauth2/access_token?realm=/sbac
pm.oauth.client.id=pm
pm.oauth.client.secret=OAUTHCLIENTSECRET
pm.oauth.batch.account=test@example.com
pm.oauth.batch.password=<password>
oauth.testreg.client.id=testreg 
oauth.testreg.client.secret=<secret> 
oauth.testreg.client.granttype=password
oauth.testreg.username=testreg@example.org 
oauth.testreg.password=<password>
```
Add environment variable `-DSB11_CONFIG_DIR` to application server startup as shown in Tomcat (Run Configuration).

### Tomcat (Run Configuration)
Like other SBAC applications, Student must be set up with active profiles and program management settings.

* `-Dspring.profiles.active`  - Active profiles should be comma separated. Typical profiles for the `-Dspring.profiles.active` include:
	* `progman.client.impl.integration`  - Use the integrated program management
	* `progman.client.impl.null`  - Use the program management null implementation
	* `mna.client.integration`  - Use the integrated MnA component
	* `mna.client.null`  - Use the null MnA component
* `-Dprogman.baseUri`  - This URI is the base URI where the Program Management REST module is deployed.
*  `-Dprogman.locator`  - The locator variable describes which combinations of name and environment (with optional overlay) should be loaded from Program Management.  For example: ```"component1-urls,dev"``` would look up the name component1-urls for the dev environment at the configured REST endpoint.  Multiple lookups can be performed by using a semicolon to delimit the pairs (or triplets with overlay): ```"component1-urls,dev;component1-other,dev"```
*  `-DSB11_CONFIG_DIR`  - Locator string needed to find the Student properties to load.
*  `-Djavax.net.ssl.trustStore`  - Location of .jks file which contains security certificates for SSO, Program Management and Permissions URL specified inside baseuri and Program Management configuration.
*  `-Djavax.net.ssl.trustStorePassword`  - Password string for the keystore.jks file.

```
 Example:
-Dspring.profiles.active="progman.client.impl.integration,mna.client.integration" 
-Dprogman.baseUri=http://<program-management-url>/programmanagement.rest/ 
-Dprogman.locator="Student,local" 
-DSB11_CONFIG_DIR=<CONFIG-FOLDER-NAME>
-Djavax.net.ssl.trustStore="<filesystem_dir>/saml_keystore.jks" 
-Djavax.net.ssl.trustStorePassword="xxxxxx"
```

* Add the `vvt` mime type to Tomcat’s *web.xml* file for the student site. This enables support for closed-captioning.

## Program Management Properties
Program Management properties need to be set for running Student. Example Student properties at [/Documents/Installation/student-progman-config.txt](https://bitbucket.org/sbacoss/studentdev/src/dc1a542849ebc6c51aceeca2ccc90e6149e4c8c1/Documents/Installation/student-progman-config.txt?at=default)

#### Database Properties
Following parameters need to be configured inside program management for database.

* `datasource.url=jdbc:mysql://localhost:3306/schemaname`  - The JDBC URL of the database from which Connections can and should be acquired.
* `datasource.username=<db-username>`  -  Username that will be used for the DataSource's default getConnection() method. 
* `encrypt:datasource.password=<db-password>`  - Password that will be used for the DataSource's default getConnection() method.
* `datasource.driverClassName=com.mysql.jdbc.Driver`  - The fully-qualified class name of the JDBC driverClass that is expected to provide Connections
* `datasource.minPoolSize=5`  - Minimum number of Connections a pool will maintain at any given time.
* `datasource.acquireIncrement=5`  - Determines how many connections at a time datasource will try to acquire when the pool is exhausted.
* `datasource.maxPoolSize=20`  - Maximum number of Connections a pool will maintain at any given time.
* `datasource.checkoutTimeout=60000`  - The number of milliseconds a client calling getConnection() will wait for a Connection to be checked-in or acquired when the pool is exhausted. Zero means wait indefinitely. Setting any positive value will cause the getConnection() call to timeout and break with an SQLException after the specified number of milliseconds.
* `datasource.maxConnectionAge=0`  - Seconds, effectively a time to live. A Connection older than maxConnectionAge will be destroyed and purged from the pool. This differs from maxIdleTime in that it refers to absolute age. Even a Connection which has not been much idle will be purged from the pool if it exceeds maxConnectionAge. Zero means no maximum absolute age is enforced. 
* `datasource.acquireRetryAttempts=5`  - Defines how many times datasource will try to acquire a new Connection from the database before giving up. If this value is less than or equal to zero, datasource will keep trying to fetch a Connection indefinitely.

#### MNA properties
The following parameters need to be configured inside program management for MNA.

* `mna.mnaUrl=http://<mna-context-url>/mna-rest/`  - URL of the Monitoring and Alerting client server's rest URL.
* `mnaServerName=student_dev`  -  Used by the mna clients to identify which server is sending the log/metrics/alerts.
* `mnaNodeName=dev`  - Used by the mna clients to identify who is sending the log/metrics/alerts. There is a discrete mnaServerName and a node in case say XXX for server name & node1/node2 in a clustered environment giving the ability to search across clustered nodes by server name or specifically for a given node. It’s being stored in the db for metric/log/alert, but not displayed.
* `mna.logger.level=ERROR`  - Used to control what is logged to the Monitoring and Alerting system. The logging levels are ALL - Turn on all logging levels,  TRACE, DEBUG, INFO, WARN, ERROR, OFF - Turn off logging.


#### Student properties
The following parameters need to be configured inside Program Management for Student.

* `student.IsCheckinSite=false` 
* `student.DONOT_Distributed=true` 
* `student.ClientName=SBAC_PT` 
* `student.StateCode=SBAC_PT` 
* `student.ClientQueryString=true` 
* `student.ClientCookie=true`  - If it is turned on, Student will try to get clientname from cookie
* `student.Appkey=Student` 
* `student.EncryptedPassword=true` 
* `student.RecordSystemClient=true`  
* `student.AdminPassword=<password>`  -  Admin Password
* `student.AppName=Student`  - Application name
* `student.SessionType=0`  - Type of the testing supported: 0 is online, 1 is paper-based
* `student.DBDialect=MYSQL`  -  Indicates which database we are using
* `student.TestRegistrationApplicationUrl=http://localhost:8083/`  -  URL to TR(ART) Application
* `student.TDSArchiveDBName=archive`  - Name of the archive schema
* `student.TDSSessionDBName=session`  - Name of the session schema
* `student.TDSConfigsDBName=configs`  - Name of the config schema
* `student.ItembankDBName=itembank`  -  Name of the itembank schema
* `student.Debug.AllowFTP=true` 
* `student.TDSReportsRootDirectory=/usr/local/tomcat/resources/tds/tdsreports/`  - Directory on Student server box where TDS reports generated  after student finished the test are located.
* `student.StudentMaxOpps=2` 

## Build Order
These are the steps that should be taken in order to build all of the Student-related artifacts:

### Pre-Dependencies
* Tomcat 6 or higher
* Maven (mvn) version 3.X or higher installed
* Java 7
* Access to SharedMultiJarDev repository
* Access to ResourceBundler repository
* Access to ItemRendererDev repository
* Access to TDSDLLDev repository
* Access to ItemScoringDev repository
* Access to TDSLoadTester repository
* Access to TestScoringDev repository
* Access to SpellCheckEngine repository
* Access to sb11-shared-build repository
* Access to sb11-shared-code repository
* Access to sb11-security repository
* Access to sb11-rest-api-generator repository
* Access to sb11-program-management repository
* Access to sb11-monitoring-alerting-client repository

### Build Order

If building all components from scratch the following build order is needed:

* SharedMultiJarDev
* ResourceBundler
* ItemRendererDev
* TDSDLLDev
* ItemScoringDev
* TDSLoadTester
* TestScoringDev
* SpellCheckEngine
* SharedBuild
* SharedCode
* RESTApiGenerator
* MonitorindAndAlertingClient
* ProgramManagementClient
* StudentDev


## Dependencies
Student has a number of direct dependencies that are necessary for it to function.  These dependencies are already built into the Maven POM files.

### Compile Time Dependencies
* shared-master
* shared-web
* shared-tr-api
* shared-threading
* shared-test
* tds-dll-api
* tds-dll-mssql
* item-renderer
* ResourceBundler
* tds-itemselection-common
* tds-itemselection-aironline
* item-scoring-api
* item-scoring-engine
* testscoring
* spellcheck
* httpcore
* prog-mgmnt-client
* prog-mgmnt-client-null-impl
* monitoring-alerting.client-null-impl
* monitoring-alerting.client
* sb11-shared-code
* commons-primitives
* commons-collections
* commons-lang
* commons-configuration
* commons-digester
* javax.inject
* servlet-api
* jsp-api
* c3p0


### Test Dependencies
* junit


### Runtime Dependencies
* Servlet API
* Persistence API
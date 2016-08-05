SearchServlet
=============

SearchServlet is a Java-based web application that provides a user
interface for searching and annotating Concrete Communications.

The SearchServlet web application is deployed as a .WAR (Web
application ARchive) to a Java servlet container.  This README
provides instructions for deploying SearchServlet with the Tomcat
servlet container.

SearchServlet uses Thrift RPC calls to communicate with other programs
that implement the Concrete *Search* and *Retriever* services.  The
SearchServlet project comes with "mock" implementations of these
services that allow you to try out the user interfaces for searching
and annotating Communications.  In order to use the SearchServlet UI
with a real search engine or database backend, you will need to modify
a configuration file to specify the hostname and ports of the machines
hosting the search and database backends.


Configuring Search and Retrieve Backends
========================================

Configuration file location and format
--------------------------------------

The search and retrieve backends are specified using a `.conf`
configuration file.  The configuration file uses a
[JSON-like syntax](https://github.com/typesafehub/config#using-hocon-the-json-superset).

The default configuration file is `src/main/java/resources/application.conf`.
It is kept in this git repo and is included in the war that is deployed to Tomcat.

To customize the configuration when using Tomcat, specify the
location of a different `.conf` file by creating an XML
configuration file located at:

    $CATALINA_HOME/conf/[enginename]/[hostname]/CadetSearch.xml

e.g.:

    $CATALINA_HOME/conf/Catalina/localhost/CadetSearch.xml

where $CATALINA_HOME is the root of your Tomcat installation. If using the Debian package for Tomcat, the conf
directory is probably /etc/tomcat or something similar.

The `CadetSearch.xml` file should have the contents:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context reloadable="true" privileged="true">
  <Parameter name="cadet.config" value="[full path to your configuration file]" override="false"/>
</Context>
```


Configuration file settings
---------------------------

The configuration file allows you to specify "providers" for the
*Search* and *Retrieve* Concrete services.  In order to connect
SearchServlet to a real search engine and database, you will need to
edit the configuration file to tell SearchServlet to use the
**RemoteRetrieverProvider** and **RemoteSearchProvider** providers,
and you will need specify the *host* and *port* of the machines
providing these services.

Here is a sample configuration file using **RemoteRetrieverProvider**
and **RemoteSearchProvider**:

```json
retrieve {
    host = "localhost"
    port = 44111
    provider = "edu.jhu.hlt.cadet.retriever.RemoteRetrieverProvider"
}
search {
    host = "localhost"
    port = 8088
    provider = "edu.jhu.hlt.cadet.search.RemoteSearchProvider"
}
```

Currently available providers for the *Retriever* Concrete Service:

- **edu.jhu.hlt.cadet.retriever.MockRetrieverProvider** - Returns
   Communications containing randomly generated "nonsense" sentences
- **edu.jhu.hlt.cadet.retriever.RemoteRetrieverProvider** - Connects
   to remote retrieve service with Thrift
- **edu.jhu.hlt.cadet.retriever.ScionRetrieverProvider** - Connects
   directly to Accumulo to pull communications. Must have direct 
   communication to accumulo to use this provider
- **edu.jhu.hlt.cadet.retriever.FileRetrieverProvider** - 
Returns communications from a directory. See the class for more details.

Currently available providers for the *Search* Concrete Service:

- **edu.jhu.hlt.cadet.search.MockSearchProvider** - Returns
   Communication IDs for "nonsense" sentences
- **edu.jhu.hlt.cadet.search.RemoteSearchProvider** - Connects to
   remote search service and communicates via Thrift definitions given
   in concrete-services project

Using **FileRetrieverProvider**:
--------------------------------

In order to use **FileRetrieverProvider**, you will need to include an
additional configuration setting in your configuration file specifying
the path to the Communication files.

Communication files are assumed to be named using the format
 `<comm id>.concrete`.

```json
files {
    data {
        dir = "/path/to/communication_files"
    }
}
```

Using **ScionRetrieverProvider**:
---------------------------------

In order to use **ScionRetrieverProvider** you need to include an additional
configuration setting in your configuration file as shown below.
Tomcat and accumulo need to be on the same network rather than relying on port forwarding.

```json
scion {
    accumulo {
        instanceName = randomName
        zookeepers = "keeper1,keeper2"
        user = reader
        password = "an accumulo reader"
        write-threads = 1
        query-threads = 4
    }
}
```

For scion configuration:

**accumulo**
+ instanceName - the name of the accumulo instance
+ zookeepers - comma seperated string of zookeeper services on host
+ user - user logging into accumulo
+ password - password for user
+ write-threads - number of writing threads
+ query-threads - number of threads querying for results

Deploying with Docker
=====================

This section assumes you are running docker locally rather than in a shared environment.
In a shared environment, you should use unique docker contexts and ports to avoid collisions.

If you do not want to use the default "mock" *Search* and *Retrieve*
Concrete Service providers, you will need to edit the configuration
file `docker-conf/cadet.conf` to select the respective "remote"
providers and specify their hosts and ports, as described in the
previous section.

Build the Docker container with the command:

```bash
docker build -t hltcoe-tomcat7-jre-8 .
```

Once the container is built, run it using:

```bash
docker run -d -p 8080:8080 hltcoe-tomcat7-jre-8
```

You can point your browser to http://locahost:8080 to verify that
Tomcat is running.

To deploy SearchServlet to the Tomcat instance running in the Docker
container, run:

```bash
mvn clean package tomcat7:redeploy
```

You should now be able to access the SearchServlet UI by going to the
URL http://localhost:8080/CadetSearch


Deploying with Standalone Tomcat
================================

Configuring Tomcat
------------------

Install [Tomcat](http://tomcat.apache.org/), version 7 or higher.  The
procedure for installing Tomcat will depend on your platform.

In order to be able to use Maven commands to deploy SearchServlet to
Tomcat, you will need to edit your `tomcat-users.xml` file.  The
location of this file will vary depending on how you installed Tomcat.
If you installed Tomcat on a Linux machine using a package manager,
the file may be at `/etc/tomcat7/tomcat-users.xml`.  If you
installed Tomcat by downloading a TGZ file, the file may be at
`$CATALINA_HOME/conf/tomcat-users.xml`.

Add the following tags to the `<tomcat-users>` section of the `tomcat-users.xml` file:

```xml
<role rolename="manager-script"/>
<user username="CADET-user" password="tomcat" roles="manager-script"/>
```

The username and password in the `tomcat-users.xml` file are the
same username and password values specified in the `pom.xml` file.

After you have updated `comcat-users.xml`, you will need to
restart Tomcat.  If you have installed Tomcat as a Linux package, you
can do this using:

```bash
sudo service tomcat7 restart
```

If you have installed Tomcat from a TGZ file, you can use:

```bash
$CATALINA_HOME/bin/catalina.sh stop
$CATALINA_HOME/bin/catalina.sh start
```


Building and Deploying SearchServlet
------------------------------------

To build SearchServlet and deploy it to Tomcat, use:

```bash
mvn clean package tomcat7:redeploy
```

You may need to manually install the scion dependency:
```bash
git clone https://gitlab.hltcoe.jhu.edu/concrete/scion.git
git checkout 0.44.0
mvn clean install -DskipTests=true
```

Logging
------------------------------------
Logging from the CADET application goes to `$CATALINA_HOME/logs/cadet.log`.
In the same directory, failures related to loading servlets go to `localhost.[date].log` and other tomcat related logging like loading context configuration goes to `catalina.out`.

To change the log level, edit `src/main/resources/logback.xml` and change the root level.


Working with Feedback
========================================
Relevance feedback is currently stored in memory while the war is running. Redeploy the war and you lose the feedback.
Soon will we persist the feedback to a database.

Dumping Feedback
------------------
You can dump the feedback to a tar.gz file by hitting the DumpFeedback endpoint.
If you deployed the war to http://localhost:8080/CadetSearch, you can hit this endpoint at http://localhost:8080/CadetSearch/DumpFeedback
The tar.gz is named feedback_[date].tar.gz and the directory is controlled through the configuration.
Each file in the tar.gz archive contains a serialized SearchResults object.
The files are named based on the SearchResults.uuid
(e.g. `2e4bf446-0977-e78f-86d0-000004a2c4b2.concrete`).

Configuration
-----------------
In your configuration file, you can specify the directory like so:

```json
feedback {
    dump_dir = "/tmp/"
}
```

The default directory is /tmp/.

Results Server
=========================
The results server accepts search results from the search UI. It interacts with annotation UIs to support annotation sessions.
It sends annotations to be saved in Accumulo. It hosts a server for accepting sorts from active learners.

Configuration
-------------------------
In your configuration file, you can specify the following settings:

```json
send {
    host = localhost
    port = 8888
    provider = "edu.jhu.hlt.cadet.send.MockSenderProvider"
}
learn {
    status = on
    host = localhost
    port = 9999
    provider = "edu.jhu.hlt.cadet.learn.SimpleMockActiveLearningClient"
}
sort {
    port = 9090
}
results {
    plugins = []
}
```

`send` handles saving annotations. `learn` is the active learner. `sort` is the service hosted by the results server. Active learning can be turned on or off through the learn.status option.
Plugins can be registered for the results server with the option: results.plugins. 
The plugins are specified as a comma separated list of class names.

Currently available providers for the *learn* Service:

- **edu.jhu.hlt.cadet.learn.SimpleMockActiveLearningClient** - Logs requests
   to the active learner but does not send them
- **edu.jhu.hlt.cadet.learn.FullMockActiveLearningClient** - Sends new random sorts
   to the sort server every minute.
- **edu.jhu.hlt.cadet.learn.RemoteActiveLearningClient** - Sends requests to
   a remote active learner

Providers for the *send* Service:

- **edu.jhu.hlt.cadet.send.MockSenderProvider** - Logs requests to store annotations
- **edu.jhu.hlt.cadet.send.RemoteSenderProvider** - Sends the annotations to a remote server
- **edu.jhu.hlt.cadet.retriever.FileSenderProvider** - 
Saves communications to a directory. See the class for more details.

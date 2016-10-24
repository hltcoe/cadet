CADET UI and Broker Servlets
=============================

CADET is a Java web application for searching and annotating Concrete communications.

CADET uses a microservices architecture based on Thrift APIs.
The CADET UI talks to a broker that exposes the microservices through Java servlets.
This repository contains the user-facing web applications like the search UI and annotation UI.
It also has the servlets code that wraps the functionality in cadet-broker.

CADET is deployed as a .WAR (Web application ARchive) to a Java servlet container.
This README provides instructions for deploying to a Tomcat servlet container.
It also describes the configuration of the broker to specify the locations of the services and database backends.
Instructions for installing and configuring Tomcat are in tomcat.md.
Instructions for deploying to a docker container are in docker.md.


Building
-----------------------
CADET requires Java 8 and uses maven in the build process.

```
mvn package
```

If you are not using the HLTCOE's maven repository, you may need to manually install dependencies.


Deploying
------------------------
There are 2 ways to deploy to Tomcat. You can use maven like so

```
mvn tomcat7:redeploy
```

This may require adding a profile to the pom file with the Tomcat server information.

Additionally, you can use the Tomcat manager to manually upload and run the web application.
See tomcat.md for further instructions.


Configuration Files
------------------------

The CADET broker configuration is specified using a `.conf` file.
The configuration file uses a [JSON-like syntax](https://github.com/typesafehub/config#using-hocon-the-json-superset).

The default configuration file is `src/main/resources/application.conf`.
It is kept in this git repo and is included in the war that is deployed to Tomcat.

To customize the configuration at runtime when using Tomcat, specify the location of a different `.conf` file by creating an XML
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

Tomcat will load this context configuration file if the war is deployed to /CadetSearch.
If you edit the configuration file that the variable cadet.config points to while the app is running, you will need to reload the app using Tomcat's manager.


Configuration
---------------------------
The broker must be configured with information about each of the microservices and database backends.
All the services have mock implementations that can be used for development and testing.

### Search

The configuration file supports multiple providers for the *Search* Service.

```
cadet {
    search {
        providers {
            name_for_a_search_provider {
                host = "localhost"
                port = 8088
                provider = "edu.jhu.hlt.cadet.search.RemoteSearchProvider"
            }
            name_for_another_search_provider {
                host = "localhost"
                port = 7977
                provider = "edu.jhu.hlt.cadet.search.RemoteSearchProvider"
            }
        }
    }
}
```

Providers for the *Search* Service:

- **edu.jhu.hlt.cadet.search.RemoteSearchProvider** - 
   Connects to remote search service and communicates via Thrift definitions given in concrete-services project
- **edu.jhu.hlt.cadet.search.MockSearchProvider** - 
   Returns hard coded Communication IDs

### Retrieve

```
cadet {
    retrieve {
        host = "localhost"
        port = 44111
        provider = "edu.jhu.hlt.cadet.retriever.RemoteRetrieverProvider"
    }
}
```

Providers for the *Retriever* Service:

- **edu.jhu.hlt.cadet.retriever.RemoteRetrieverProvider** - 
   Connects to remote retrieve service with Thrift
- **edu.jhu.hlt.cadet.retriever.ScionRetrieverProvider** - 
   Connects directly to Accumulo to pull communications. Must have direct communication to accumulo to use this provider.
- **edu.jhu.hlt.cadet.retriever.FileRetrieverProvider** - 
   Returns communications from a directory. See the class for more details.
- **edu.jhu.hlt.cadet.retriever.MockRetrieverProvider** - 
   Returns Communications containing randomly generated "nonsense" sentences

### Send

```
cadet {
    send {
        host = localhost
        port = 8888
        provider = "edu.jhu.hlt.cadet.send.RemoteSenderProvider"
    }
}
```

Providers for the *Send* Service:

- **edu.jhu.hlt.cadet.send.RemoteSenderProvider** - 
   Sends the annotations to a remote server
- **edu.jhu.hlt.cadet.send.ScionSendProvider** - 
   Not integrated yet
- **edu.jhu.hlt.cadet.send.FileSenderProvider** - 
   Saves communications to a directory. See the class for more details.
- **edu.jhu.hlt.cadet.send.MockSenderProvider** - 
   Logs requests to store annotations

### Feedback

```
cadet {
    feedback {
        dump_dir = "/tmp/"
        store = "edu.jhu.hlt.cadet.feedback.store.MemoryFeedbackStore"
    }
}
```

Providers for the feedback store:

- **edu.jhu.hlt.cadet.feedback.store.MemoryFeedbackStore** - 
   Keeps the feedback in memory. Does not persist if the app is restarted on Tomcat.
- **edu.jhu.hlt.cadet.feedback.store.sql.SqlFeedbackStore** - 
   Stores the feedback in a sql database. Defaults to mysql. Not quite ready for use.

### Results Server

The results server wraps a few services. It maintains a list of search results for annotations.
It manages annotation sessions including communication with the active learning service.

```
cadet {
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
}
```

`sort` is a service hosted directly by the results server.
Active learning can be turned on or off through the learn.status option.
Plugins can be registered for the results server with the option: results.plugins. 
The plugins are specified as a comma separated list of class names.

Providers for the *Learn* Service:

- **edu.jhu.hlt.cadet.learn.RemoteActiveLearningClient** - 
   Sends requests to a remote active learner
- **edu.jhu.hlt.cadet.learn.SimpleMockActiveLearningClient** - 
   Logs requests to the active learner but does not send them
- **edu.jhu.hlt.cadet.learn.FullMockActiveLearningClient** - 
   Sends new random sorts to the sort server every minute.


### File-based Providers

In order to use **FileRetrieverProvider** or **FileSendProvider**, you will need to include an
additional configuration setting in your configuration file specifying
the path to the Communication files.

Communication files are assumed to be named using the format `<comm id>.concrete`.

```
cadet {
    files {
        data {
            dir = "/path/to/communication_files"
        }
    }
}
```

### Using Scion Directly

In order to use **ScionRetrieverProvider** or **ScionSendProvider** you need to include an additional
configuration setting in your configuration file as shown below. Note that it is under the `scion` namespace rather than `cadet`.
Tomcat and accumulo need to be on the same network rather than relying on port forwarding.

```
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

+ instanceName - the name of the accumulo instance
+ zookeepers - comma seperated string of zookeeper services on host
+ user - user logging into accumulo
+ password - password for user
+ write-threads - number of writing threads
+ query-threads - number of threads querying for results


Logging
------------------------------------
Logging from the CADET application goes to `$CATALINA_HOME/logs/cadet.log`.
In the same directory, failures related to loading servlets go to `localhost.[date].log` and other tomcat related logging like loading context configuration goes to `catalina.out`.

To change the log level, edit `src/main/resources/logback.xml` and change the root level.


Dumping Feedback
------------------
You can dump the feedback to a tar.gz file by hitting the DumpFeedback endpoint.
If you deployed the war to http://localhost:8080/CadetSearch, you can hit this endpoint at http://localhost:8080/CadetSearch/DumpFeedback
The tar.gz is named feedback_[date].tar.gz and the directory is controlled through the configuration.
Each file in the tar.gz archive contains a serialized SearchResults object.
The files are named based on the SearchResults.uuid
(e.g. `2e4bf446-0977-e78f-86d0-000004a2c4b2.concrete`).


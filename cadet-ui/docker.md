Deploying with Docker
=====================

This section assumes you are running docker locally rather than in a shared environment.
In a shared environment, you should use unique docker contexts and ports to avoid collisions.

To change the default configuration, you will need to edit the configuration file `docker-conf/cadet.conf` before deploying.

Build the Docker container with the command:

```bash
docker build -t hltcoe-tomcat7-jre-8 .
```

Once the container is built, run it using:

```bash
docker run -d -p 8080:8080 hltcoe-tomcat7-jre-8
```

You can point your browser to http://locahost:8080 to verify that Tomcat is running.

To deploy CADET to the Tomcat instance running in the Docker container, run:

```bash
mvn clean tomcat7:redeploy
```

You should now be able to access the Search UI by going to the URL http://localhost:8080/CadetSearch


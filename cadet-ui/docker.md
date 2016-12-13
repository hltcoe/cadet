Deploying with Docker
=====================

This section assumes you are running docker locally rather than in a shared environment.
In a shared environment, you should use unique docker contexts and ports to avoid collisions.

To change the default configuration, you will need to edit the configuration file `docker-conf/cadet.conf` before deploying.

The first step is to build the cadet-ui war file:

```bash
mvn clean package
```

Then build the Docker container with the command:

```bash
docker build -t hltcoe-cadet-ui .
```

Once the container is built, run it using:

```bash
docker run -d -p 8080:8080 hltcoe-cadet-ui
```

You should now be able to access the CADET UI by going to the URL http://localhost:8080/


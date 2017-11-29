Running with Docker
=====================

Building the Docker Image
-------------------------
The Docker image is available from Docker Hub as hltcoe/cadet.
Building the image requires Docker 17.06.1 or newer.

```bash
docker build -t cadet .
```

Using the Docker Image
----------------------
The Docker image runs a Tomcat server on port 8080 with the CADET UI at the root of the server.
The image requires a volume to be mounted at /config and it must contain a configuration file named cadet.conf.
Details on the configuration can be found in the cadet-ui README file.
Assuming that fetch, search, and store services are running and configured in cadet.conf, the dock image can be run:
```
docker run -d -p8080:8080 -v/path/to/dir/:/config hltcoe/cadet 
```
The above uses the image from Docker Hub.

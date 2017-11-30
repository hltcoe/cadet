CADET Instructions
=======================
A communication in Concrete/CADET lingo is a document (tweet, article, blog post, etc.)
CADET consists of two user interfaces: one for searching communications and one for annotating them.
Behind the scenes are services for fetching, searching, sorting, and storing communications.
The communications must be in the [Concrete format](http://hltcoe.github.io/concrete/).
Performing an ingest to create the Concrete files is outside the scope of this document.

Requirements
---------------
1. Java 8
2. Java Web and Container Server (like Tomcat or Jetty)
3. Fetch/store server
4. Search server

For this tutorial, we will assume that you:
 * have a zip file named `comms.zip` of Concrete files named `id.comm` where id is the communication ID.
 * are using [Stretcher](https://github.com/hltcoe/stretcher) as the fetch/store server.
 * are using [CADET Lucene](https://github.com/hltcoe/cadet-search-lucene) as the search server.
 * are using the provided Jetty server to host the CADET application war on port 8080. 


Fetch and Store Services
-------------------------
After cloning and building Stretcher, run it on the default ports:
```
./start.sh --input path/to/comms.zip --output /tmp
```
This will store annotated communications in /tmp.


Search Service
-------------- 
After cloning and building CADET search using Lucene, run it on the default port:
```
./start.sh --fp 9090 --dir some/dir/for/index -b -r
```
This will build the search index over the communications from the fetch server.


Running CADET UI
--------------------
1. Build and install the CADET dependencies: `mvn clean install`.
2. Launch Jetty
  * Change directories to cadet-ui
  * Copy conf/application.conf.sample to conf/application.conf
  * Run `mvn jetty:run`
3. In a web browser go to [localhost:8080/admin.html](http://localhost:8080/admin.html) to check that the services are up.

If there is a checkmark by fetch and store and a search service listed, click the link for Search and perform a search.
Then export the search, click the Results link and select a results set to annotate.

To change the configuration when using Jetty, edit cadet-ui/conf/application.conf.
Details on configuration are found in the cadet-ui README.


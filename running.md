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
2. Tomcat server
3. Fetch/store server
4. Search server

For this tutorial, we will assume that you:
 * have a zip file named `comms.zip` of Concrete files named `id.comm` where id is the communication ID.
 * are using [Stretcher](https://github.com/hltcoe/stretcher) as the fetch/store server.
 * are using [CADET Lucene](https://github.com/hltcoe/cadet-search-lucene) as the search server.
 * are using Tomcat to host the CADET application war on port 8080. 


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


Deploying to Tomcat
--------------------
1. Prepare Tomcat
 * Edit Tomcat's tomcat-users.xml to add a user with a manager-script role.
 * Update your maven settings.xml file to include a server entry for cadet-server.
 * For more details on preparing Tomcat, see `tomcat.md`.
2. Build the war file: `mvn clean install package`.
3. Deploy to Tomcat: `mvn tomcat7:redeploy`.
4. In a web browser go to [localhost:8080/Cadet/admin.html](http://localhost:8080/Cadet/admin.html) to check that the services are up.

If there is a checkmark by fetch and store and a search service listed, click the link for Search and perform a search.
Then export the search, click the Results link and select a results set to annotate.


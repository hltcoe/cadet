CADET Tools
=========================
Command line tools for interacting with and testing the CADET system.

Building
---------------
Use maven to build the tools:
```
mvn clean package
```

Running
---------------
There are shell scripts in this directory for running the individual tools.
They are configured through an application.conf file in this directory.
You can start with the default conf file by running this command:

```bash
cp src/main/resources/reference.conf application.conf
```

**Alive**

The alive script can check any service.
```bash
./alive.sh [host] [port]
```

**About**

The about script can check any service.
```bash
./about.sh [host] [port]
```

**Search**
```bash
./search.sh [query]
```

Example: ./search guinea pigs bolivia
Example: ./search "new york" minute

It has an optional type parameter ("comm", "sent") with a default of "sent":
```bash
./search.sh --type sent albino elephants
```

For help:
```bash
./search.sh --help
```


**Retrieve**
```bash
./retrieve.sh [id1] [id2] ...
```

retrieve has an optional parameter for writing concrete files instead of writing to stdout:
```bash
./retrieve.sh --file 99508872582148096
```

**View Feedback**
```bash
./feedback.sh [tar.gz filename]
```

**Active Learning**

This reads a file of communication IDs and submits them as an task to the active learner server.
It also stands up a server to receive new sorts from the active learner.
```bash
./learn.sh [tsv filename]
```

It has options for language and the amount of time it runs waiting for new sorts.

**File-based Retrieve Server**

Serves concrete communication files in a directory.

```bash
./file_server.sh -d /data/my_comms/ -p 9898
```


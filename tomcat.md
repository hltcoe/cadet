Configuring Tomcat
======================

Install [Tomcat](http://tomcat.apache.org/), version 7 or higher.  The
procedure for installing Tomcat will depend on your platform.

Adding an Admin User
----------------------
In order to be able to use Maven commands to deploy CADET to
Tomcat, you will need to edit your `tomcat-users.xml` file.  The
location of this file will vary depending on how you installed Tomcat.
If you installed Tomcat on a Linux machine using a package manager,
the file may be at `/etc/tomcat7/tomcat-users.xml`.  If you
installed Tomcat by downloading a TGZ file, the file will be at
`$CATALINA_HOME/conf/tomcat-users.xml`.

Add the following tags to the `<tomcat-users>` section of the `tomcat-users.xml` file:

```xml
<role rolename="manager-script"/>
<user username="your username" password="your password" roles="manager-script"/>
```

Restarting Tomcat
------------------
After you have updated `tomcat-users.xml`, you will need to
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

Updating Maven to Deploy to Tomcat
----------------------------------
The username and password in the `tomcat-users.xml` file are the
same username and password values specified in your settings.xml file.
This file should have a section that looks like this:
```
  <servers>
    <server>
      <id>cadet-server</id>
      <username>your username</username>
      <password>your password</password>
    </server>
  </servers>
```


J/SQL-Shell
=========

JSQLShell is a java based command line SQL utility for accessing any database, with a JDBC 4 driver jar.
The connection property file (``jsqlshell.properties``) can be access via classpath, or can be placed in ``user.home``
directory prefixed with a dot (``.jsqlshell.properties``).

With simplicity and pure java 1.6+ in mind, command line usage is limited to System.in InputStream. Therefore
tab completion, or arrow keys are not supported.

**Build/Run**

``mvn install``

``java -cp ".:yourdbdriver.jar:jsqlshell-x.x.x.jar" JSQLShell``
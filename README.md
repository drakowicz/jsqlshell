J/SQL-Shell
===========

JSQLShell is a java based command line SQL utility for accessing any database, with a JDBC 4 driver jar (like
jtds-1.3.1.jar, mysql-connector-java-5.1.26.jar, sqljdbc4.jar, ...)
The connection property file (``jsqlshell.properties``) can be access via classpath, or can be placed in ``user.home``
directory prefixed with a dot (``.jsqlshell.properties``).

This project depends on ``jline`` utility to support up/down arrow key history, as well as left/right ability to change
input content. However, tab completion is not supported yet.

**How to build**

Build with maven

``mvn install``

**How to run**

Download the later JSQLShell zip file. Unzip it. Copy your JDBC driver libraries into jsqlshell folder.
Configure your database driver and parameters (``jsqlshell.properties``).
Using command line ``cd`` into jsqlshell directory and run the shell file with optional dbname parameter.

``./jsqlshell.sh`` or ``./jsqlshell.sh [dbname]``


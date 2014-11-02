package net.rakowicz.jsqlshell;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlShellApp {

    private static PrintStream out = System.out;
    
    public SqlShellApp (String dbName) {
        DbConfig db;
        Connection connection = null;
        Statement statement = null;
        try {
            db = new DbConfig(dbName).loadDriver();
            connection = db.connect();
            DatabaseMetaData dbmd = connection.getMetaData();
            out.print("Connected to: " + dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion());

            statement = connection.createStatement();
            statement.setMaxRows(1000);
            out.println(" (autocommit=" + connection.getAutoCommit() + ", readonly=" + connection.isReadOnly() + ", maxrows=" + statement.getMaxRows() + ")");
            out.println("Type 'help' for more options");

            while (true) {
                String command = CommandReader.readLine("jss> ");

                try {
                    if (connection.isClosed()) {
                        connection = db.connect();
                        out.println("info: Connection expired, re-connected...");
                    }
                    if (statement.isClosed()) {
                        statement = connection.createStatement();
                        out.println("info: Statement was closed, re-created...");
                    }
                    if (CommandHelper.isCommand(command, out, connection, statement)) {
                        continue;
                    }

                    String sql = command;
                    if (!sql.trim().toLowerCase().startsWith("select ")) {
                        int changed = statement.executeUpdate(sql);
                        out.println("info: affected " + changed + " rows");
                    } else {
                        long started = System.currentTimeMillis();
                        ResultSet rset = statement.executeQuery(sql);
                        new DataFormatter(rset).format().printResults(out, started);
                    }
                    if (connection.getWarnings() != null) {
                        out.println("warning: " + connection.getWarnings().getErrorCode() + ":" + connection.getWarnings().getMessage());
                    }
                } catch (SQLException e) {
                    out.println("error: " + e.getMessage());
                }
                connection.clearWarnings();
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException && "exit".equals(e.getMessage())) {
                // exit
            } else {
                e.printStackTrace();
                out.println("Exited...");
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}

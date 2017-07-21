package net.rakowicz.jsqlshell;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class CommandHelper {

    // | TABLE_CAT | TABLE_SCHEM | TABLE_NAME | COLUMN_NAME | DATA_TYPE | TYPE_NAME | COLUMN_SIZE | BUFFER_LENGTH | DECIMAL_DIGITS | NUM_PREC_RADIX | NULLABLE | REMARKS | COLUMN_DEF | SQL_DATA_TYPE | SQL_DATETIME_SUB | CHAR_OCTET_LENGTH | ORDINAL_POSITION | IS_NULLABLE | SCOPE_CATALOG | SCOPE_SCHEMA | SCOPE_TABLE | SOURCE_DATA_TYPE | IS_AUTOINCREMENT | IS_GENERATEDCOLUMN |
    private static final String[] TABLE_DEF_SCHEMA =
        {"TABLE_CAT","TABLE_SCHEM","TABLE_NAME","COLUMN_NAME","TYPE_NAME","COLUMN_SIZE","DECIMAL_DIGITS","IS_NULLABLE","IS_AUTOINCREMENT","ORDINAL_POSITION"};
    private static final String[] TABLE_DEF =
        {"TABLE_NAME","COLUMN_NAME","TYPE_NAME","COLUMN_SIZE","DECIMAL_DIGITS","IS_NULLABLE","IS_AUTOINCREMENT","ORDINAL_POSITION"};
    private static final String[] TABLES_DEF = {"TABLE_NAME"};
    private static Set<String> TABLE_DEF_SCHEMA_SET = new HashSet<String>(Arrays.asList(TABLE_DEF_SCHEMA));
    private static Set<String> TABLE_DEF_SET = new HashSet<String>(Arrays.asList(TABLE_DEF));
    private static Set<String> TABLES_SET = new HashSet<String>(Arrays.asList(TABLES_DEF));

    private static LinkedList<String> history = new LinkedList<String>();

    public static boolean isCommand(String command, PrintStream out, Connection connection, Statement statement) throws Exception {
        
        long started = System.currentTimeMillis();
        if (command == null) {
            return false;
        } else if ("help".equals(command)) {
            printHelp(out);
            return true;
        
        } else if ("history".equals(command)) {
            for (String sql : history) {
                out.println(sql);
            }
            return true;
        
        } else if ("-set".equals(command)) {
            out.println("info: autocommit=" + connection.getAutoCommit());
            out.println("info: readonly=" + connection.isReadOnly());
            out.println("info: maxrows=" + statement.getMaxRows());
            out.println("info: timeout=" + ((statement.getQueryTimeout() == 0) ? "nolimit" : String.valueOf(statement.getQueryTimeout())));
            return true;
        } else if (command.startsWith("-set ")) {
            if (command.startsWith("-set autocommit ")) {
                connection.setAutoCommit("true".equals(command.replaceAll("-set autocommit ", "").toLowerCase().trim()));
                out.println("info: autocommit=" + connection.getAutoCommit());
            } else if (command.startsWith("-set readonly ")) {
                connection.setReadOnly("true".equals(command.replaceAll("-set readonly ", "").toLowerCase().trim()));
                out.println("info: readonly=" + connection.isReadOnly());
            } else if (command.startsWith("-set maxrows ")) {
                try {
                    statement.setMaxRows(new Integer(command.replaceAll("\\D", "")));
                } catch (NumberFormatException ignore) {
                }
                out.println("info: maxrows=" + statement.getMaxRows());
            } else if (command.startsWith("-set timeout ")) {
                try {
                    statement.setQueryTimeout(new Integer(command.replaceAll("\\D", "")));
                } catch (NumberFormatException ignore) {
                }
                out.println("info: timeout=" + ((statement.getQueryTimeout() == 0) ? "nolimit" : String.valueOf(statement.getQueryTimeout())));
            }
            return true;
        
        } else if (command.startsWith("-show ")) {
            if (command.endsWith("schemas")) {
                ResultSet catalogs = connection.getMetaData().getCatalogs();
                new DataFormatter(catalogs).format().printResults(out, started);
                ResultSet schemas = connection.getMetaData().getSchemas();
                new DataFormatter(schemas).format().printResults(out, started);
            
            } else if (command.endsWith("tables")) {
                ResultSet tables = connection.getMetaData().getColumns(null, null, "%", "%");
                new DataFormatter(tables).filter(TABLES_SET).unique().format().printResults(out, started);
            } else {
                String[] names = command.substring("-show ".length()).trim().split("\\.");
                ResultSet tables = null;
                if (names.length == 1) {
                    tables = connection.getMetaData().getColumns(null, null, names[0], "%");
                } else if (names.length == 2) {
                    tables = connection.getMetaData().getColumns(names[0], null, names[1], "%");
                } else if (names.length == 3) {
                    tables = connection.getMetaData().getColumns(names[0], names[1], names[2], "%");
                }
                new DataFormatter(tables).filter(getTableDefColumns(names.length > 1)).format().printResults(out, started);
            }
            return true;
        
        } else if ("exit".equals(command)) {
            throw new RuntimeException("exit");
        }
        
        // else sql query
        history.add(command);
        if (history.size() > 100) {
            history.removeFirst();
        }
        return false;
    }
    
    private static void printHelp(PrintStream out) {
        out.println("     exit - to quit JSQLShell");
        out.println("     help - to see this help");
        out.println("     history - shows last 100 statements");
        out.println("     -set - show all current settings");
        out.println("     -set autocommit [true|false] - to enable/disable autocommit (default=false)");
        out.println("     -set readonly [true|false] - to enable/disable this session to be read only (default=true)");
        out.println("     -set maxrows [number] - to set max of rows return by the result set (default=1000)");
        out.println("     -set timeout [millies] - to set query timeout (default=0 - nolimit)");
        out.println("     -show schemas - show available schemas");
        out.println("     -show tables - show all tables");
        out.println("     -show [table_name || schema.table_name || catalog.shema.table_name] - show table definition, wildcards accepted");
    }

    private static Set<String> getTableDefColumns(boolean schema) {
        return schema ? TABLE_DEF_SCHEMA_SET : TABLE_DEF_SET;
    }

}

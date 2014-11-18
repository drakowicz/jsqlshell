package net.rakowicz.jsqlshell;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataFormatter {

    static final String SPACES = "        ";

    private ResultSet rset;
    private ResultSetMetaData metaData;
    private List<List<String>> results;
    private List<String> columnNames;
    private List<Integer> columnLengths;
    private Set<String> showColumns = Collections.emptySet();
    private boolean unique;

    public DataFormatter(ResultSet rset) throws Exception {
        this.rset = rset;
        this.metaData = rset.getMetaData();
        columnLengths = new ArrayList<Integer>(metaData.getColumnCount());
        columnNames = new ArrayList<String>(columnLengths.size());
        results = new ArrayList<List<String>>();
    }
    
    public DataFormatter format() throws Exception {
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            String colName = metaData.getColumnName(i+1);
            columnNames.add(colName);
            columnLengths.add(colName.length());
        }
        while (rset.next()) {
            List<String> rowData = new ArrayList<String>();
            results.add(rowData);
            for (int i = 0; i < columnLengths.size(); i++) {
                Integer colLen = columnLengths.get(i);
                String value = rset.getString(i+1); // TODO handle LOB in SQL Server
                Integer valLen = value == null ? 0 : value.length();
                columnLengths.set(i, Math.max(colLen, valLen));
                rowData.add(value == null ? "NULL" : value);
            }
        }
        return this;
    }

    public DataFormatter unique() {
        unique = true;
        return this;
    }
    
    public DataFormatter filter(Set<String> columns) {
        showColumns = columns;
        return this;
    }
    
    public DataFormatter printResults(PrintStream out, long started) throws Exception {
        out.println();
        printBoundary(out);
        
        int numOfCols = columnLengths.size();
        int col = 0;
        while (col < numOfCols) {
            String name = columnNames.get(col);
            if (showColumns.isEmpty() || showColumns.contains(name)) {
                out.print("| ");
                out.print(padRight(name, columnLengths.get(col)));
                out.print(" ");
            }
            col++;
        }
        out.println("|");
        
        printBoundary(out);
        
        Set<String> uniqueSet = new HashSet<String>(); 
        outer: for (List<String> rowData : results) {
            col = 0;
            for (String value : rowData) {
                String name = columnNames.get(col);
                if (showColumns.isEmpty() || showColumns.contains(name)) {
                    if (unique && !uniqueSet.add(value)) {
                        continue outer;
                    }
                    out.print("| ");
                    out.print(padRight(value, columnLengths.get(col)));
                    out.print(" ");
                    if (unique) {
                        break;
                    }
                }
                col++;
            }
            out.println("|");
        }
        
        printBoundary(out);
        
        long time = System.currentTimeMillis() - started;
        int size = unique ? uniqueSet.size() : results.size();
        out.println(size + " rows in set (" + time + " ms)");
        out.println();
        return this;
    }
    
    private void printBoundary(PrintStream out) {
        int numOfCols = columnLengths.size();
        for (int col = 0; col < numOfCols; col++) {
            out.print("--");
            for (int x = 0; x < columnLengths.get(col); x++) {
                out.print("-");
            }
            out.print("-");
        }
        
        out.println("-");
    }
    
    private String padRight(String value, int len) {
        if (value.length() == len) {
            return value;
        }
        int needed = (len - value.length()) / SPACES.length();
        StringBuilder pad = new StringBuilder(SPACES);
        while (needed-- > 0) {
            pad.append(SPACES);
        }
        
        return new StringBuilder(value).append(pad).substring(0,len).toString();
    }
    
}

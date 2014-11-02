package net.rakowicz.jsqlshell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.MemoryHistory;

public class CommandReader {

    private static InputStream in;
    private static PrintStream out;
    private static ConsoleReader reader;
    
    static {
        try {
            in = System.in;
            out = System.out;
            reader =  new ConsoleReader();
            init();
        } catch (Exception ignore) {
        }
    }

    
    public static String readLine(String prompt) throws IOException {
        return readLine(prompt, false);
    }

    public static String readPassword(String prompt) throws IOException {
        return readLine(prompt, true);
    }
    
    private static String readLine(String prompt, boolean password) throws IOException {
        if (reader != null) {
            reader.setPrompt(prompt);
            if (password) {
                return reader.readLine('*');
            } else {
                String cmd = reader.readLine();
                if ("exit".equals(cmd)) {
                    if (reader.getHistory() instanceof FileHistory) {
                        ((FileHistory) reader.getHistory()).flush();
                    }
                }
                return cmd;
            }
        } else if (System.console() != null) {
            out.print(prompt);
            if (password) {
                return new String(System.console().readPassword());
            } else {
                return System.console().readLine();
            }
        } else {
            out.print(prompt);
            StringBuilder input = new StringBuilder();
            while (true) {
                char c = (char) in.read();
                if (c == '\n')
                    break;
                input.append(c);
            }
            return input.toString();
        }
    }
    
    private static void init() throws IOException {
        File file = new File(System.getProperty("user.home") + "/.jsqlshell.history");
        if ((file.exists() || file.createNewFile()) && file.canRead() && file.canWrite()) {
            reader.setHistory(new FileHistory(file));
        } else {
            reader.setHistory(new MemoryHistory());
        }
        reader.setHistoryEnabled(true);
    }

}

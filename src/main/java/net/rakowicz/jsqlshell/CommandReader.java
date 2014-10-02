package net.rakowicz.jsqlshell;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class CommandReader {

    private static InputStream in = System.in;
    private static PrintStream out = System.out;
    
    public static String readLine(String prompt) throws IOException {
        return readLine(prompt, false);
    }

    public static String readPassword(String prompt) throws IOException {
        return readLine(prompt, true);
    }
    
    public static String readLine(String prompt, boolean password) throws IOException {
        out.print(prompt);
        if (System.console() != null) {
            if (password) {
                return new String(System.console().readPassword());
            } else {
                return System.console().readLine();
            }
        } else {
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
}

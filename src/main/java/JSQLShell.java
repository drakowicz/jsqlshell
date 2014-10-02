import net.rakowicz.jsqlshell.SqlShellApp;


public class JSQLShell {

    public static void main(String[] args) {
        new SqlShellApp(args.length > 0 ? args[0] : null);
    }

}

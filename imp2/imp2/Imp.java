package imp2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Imp {

    private static boolean hadError, multiline, debug;

    private static Interpreter interpreter;
    private static Debugger debugger;

    public static void main(String[] args) {
        List<String> input = Arrays.asList(args);

        multiline = input.contains("--multiline") || input.contains("-m");
        debug = input.contains("--debug") || input.contains("-d");

        int argcount = (multiline ? 1 : 0) + (debug ? 1 : 0);
        if (argcount == args.length) {
            repl();
        } else {
            System.err.println("Illegal Program Argument(s).");
        }
    }

    private static void repl() {
        reset();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String input = readREPL(reader);
            while (input != null) {
                if (input.startsWith(":")) {
                    runCommand(input);
                } else {
                    run(input);
                }
                input = readREPL(reader);
            }
        } catch (IOException e) {
            log("Input Error. Aborting.");
        }

        System.out.println();
    }

    private static void runCommand(String input) {
        String[] args = input.split("[\\s]+");
        if (args[0].equals(":l") || args[0].equals(":load")) {
            if (args.length < 2) {
                System.err.println("Need to specify at least one file to load.");
                return;
            }
            for (int i = 1; i < args.length; i++) {
                if (args[i].isEmpty()) { continue; }
                runFile(args[i]);
            }
        } else {
            System.err.println("Command not found.");
        }
    }

    private static void runFile(String s) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(s));
            run(new String(bytes, Charset.defaultCharset()));
        } catch (IOException e) {
            System.out.println("for filename " + s + ": " + e.getMessage());
        }
    }

    private static void run(String program) {
        if (program.isEmpty()) {
            return;
        }

        hadError = false;

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.lex(program);

        if (hadError) {
            log("Lexer Error. Aborting.");
            return;
        }

        Parser parser = new Parser();
        Stm tree = parser.parse(tokens);

        if (tree == null || hadError) {
            log("Parsing Error. Aborting.");
            return;
        }

        //Printer printer = new Printer();
        //System.out.println(printer.print(tree));

        if (debug) {
            debugger.debug(tree);
        } else {
            interpreter.interpret(tree);
        }

        if (hadError) {
            log("Runtime Error. Aborting.");
            return;
        }
    }

    private static String readREPL(BufferedReader reader) throws IOException {
        if (!multiline) {
            System.out.print(">> ");
            return reader.readLine();
        } else {
            System.out.print(">> ");
            String res = reader.readLine();
            while (res != null && (res.isEmpty() || res.charAt(res.length() - 1) != '!')) {
                System.out.print(">> ");
                String tmp = reader.readLine();
                if (tmp == null) {
                    res = null;
                } else {
                    res += "\n" + tmp;
                }
            }
            if (res != null) {
                res = res.substring(0, res.length() - 1);
            }
            return res;
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void reset() {
        hadError = false;
        interpreter = new Interpreter();
        debugger = new Debugger();
    }

    private static void markError() {
        hadError = true;
    }

    private static boolean hadError() {
        return hadError;
    }

    static void logDirectError(Token token, String message) {
        System.err.println("\033[31mError at " + token + ": " + message + "\033[0m");
        markError();
    }

    private static String lastOf(String[] a) {
        return a[a.length - 1];
    }
}
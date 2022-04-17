package imp2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Imp {

    private static boolean hadError, multiline, debug;

    private static Interpreter interpreter;
    private static Debugger debugger;

    public static void main(String[] args) {
        multiline = Arrays.asList(args).contains("-multiline") ||
                Arrays.asList(args).contains("-m");

        debug = Arrays.asList(args).contains("-debug") ||
                Arrays.asList(args).contains("-d");

        repl();
    }

    private static void repl() {
        reset();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String input = readREPL(reader);
            while (input != null) {
                run(input);
                input = readREPL(reader);
            }
        } catch (IOException e) {
            log("Input Error. Aborting.");
        }

        System.out.println();
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
}
package imp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Imp {

    private static boolean hadError;

    private static boolean multiline;
    private static Interpreter interpreter;
    
    public static void main(String[] args) {

        multiline = false;
        interpreter = new Interpreter();

        if (Arrays.equals(args, new String[] { "-multiline" })) {
            multiline = true;
        }

        try {
            repl();
        } catch (IOException e) {
            System.err.println("I/O Error. Aborting.");
        }
    }

    private static void repl() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input = getInput(reader);
        while (input != null) {
            System.out.println();
            run(input);
            System.out.println();
            input = getInput(reader);
        }

        System.out.println();
    }

    private static String getInput(BufferedReader reader) throws IOException {
        System.out.print("> ");
        String input = reader.readLine();

        if (!multiline || input == null) { return input; }

        while (!input.endsWith("!")) {
            System.out.print("> ");
            String next = reader.readLine();
            if (next == null) { return null; }
            input += "\n" + next;
        }

        return input.substring(0, input.length() - 1);
    }

    private static void run(String program) {
        hadError = false;

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.lex(program);

        if (hadError) {
            System.err.println("Lexing Error. Aborting.");
            return;
        }

        Parser parser = new Parser();

        Stm tree = null;
        try {
            tree = parser.parse(tokens);
        } catch (RuntimeException e) {
            hadError = true;
        }

        if (hadError) {
            System.err.println("Parsing Error. Aborting.");
            return;
        }

        try {
            tree.accept(interpreter);
        } catch (RuntimeException e) {
            System.err.println("Runtime Error. Aborting.");
        }
    }

    static void logCompileError(Token token, String message) {
        hadError = true;

        System.err.println("Error at " + token + ": " + message);
    }

    static void logRuntimeError(Token token, String message) {
        System.err.println("Error at " + token + ": " + message);
        throw new RuntimeException();
    }
}

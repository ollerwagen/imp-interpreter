package imp2;

import java.util.List;
import java.util.ArrayList;

import static imp2.TokenType.*;

public class Lexer {
    
    private int line, lineindex, index;
    private String program;

    public Lexer() {}

    List<Token> lex(String input) {
        program = input;

        List<Token> res = new ArrayList<>();
        resetHead();

        while (inBounds()) {

            int tmpline = line, tmpindex = lineindex;
            char c = advance();

            switch (c) {
                case ' ': case '\n': case '\t': case '\r':
                    break; // ignore whitespace
                
                case '(':
                    res.add(new Token(LPAREN, tmpline, tmpindex, "("));
                    break;
                case ')':
                    res.add(new Token(RPAREN, tmpline, tmpindex, ")"));
                    break;
                case '+':
                    res.add(new Token(PLUS, tmpline, tmpindex, "+"));
                    break;
                case '-':
                    res.add(new Token(MINUS, tmpline, tmpindex, "-"));
                    break;
                case '*':
                    res.add(new Token(TIMES, tmpline, tmpindex, "*"));
                    break;
                case '=':
                    res.add(new Token(EQUAL, tmpline, tmpindex, "="));
                    break;
                case '#':
                    res.add(new Token(NOT_EQUAL, tmpline, tmpindex, "#"));
                    break;
                case '<':
                    if (peek() == '=') {
                        advance();
                        res.add(new Token(LESS_EQUAL, tmpline, tmpindex, "<="));
                    } else {
                        res.add(new Token(LESS, tmpline, tmpindex, "<"));
                    }
                    break;
                case '>':
                    if (peek() == '=') {
                        advance();
                        res.add(new Token(GREATER_EQUAL, tmpline, tmpindex, ">="));
                    } else {
                        res.add(new Token(GREATER, tmpline, tmpindex, ">"));
                    }
                    break;
                case ':':
                    if (advance() == '=') {
                        res.add(new Token(ASSIGN, tmpline, tmpindex, ":="));
                    } else {
                        logError(new Token(ERROR, tmpline, tmpindex, ":"), "':' character requires ':='.");
                    }
                    break;
                case ';':
                    res.add(new Token(SEMICOLON, tmpline, tmpindex, ";"));
                    break;
                case '|':
                    res.add(new Token(PIPE, tmpline, tmpindex, "|"));
                    break;
                case ',':
                    res.add(new Token(COMMA, tmpline, tmpindex, ","));
                    break;

                default:
                    if (isNum(c)) {
                        res.add(number(c, tmpline, tmpindex));
                    } else if (isAlpha(c)) {
                        res.add(idOrKey(c, tmpline, tmpindex));
                    } else {
                        logError(new Token(ERROR, tmpline, tmpindex, Character.toString(c)), "Unexpected Character.");
                    }
                    break;
            }
        }

        res.add(new Token(EOF, line, index, "<eof>"));

        return res;
    }

    private static boolean isNum(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private void resetHead() {
        line = 1;
        lineindex = 1;
        index = 0;
    }

    private boolean inBounds() {
        return index < program.length();
    }

    private char peek() {
        if (inBounds()) {
            return program.charAt(index);
        } else {
            return '\0';
        }
    }

    private char advance() {
        if (inBounds()) {
            char res = program.charAt(index++);
            if (res == '\n') {
                line++; lineindex = 1;
            } else {
                lineindex++;
            }
            return res;
        } else {
            return '\0';
        }
    }

    private Token number(char first, int tmpline, int tmpindex) {
        String resString = Character.toString(first);
        while (isNum(peek())) {
            resString += advance();
        }
        return new Token(NUMBER, tmpline, tmpindex, resString);
    }

    private Token idOrKey(char first, int tmpline, int tmpindex) {
        String resString = Character.toString(first);
        while (isAlpha(peek()) || isNum(peek())) {
            resString += advance();
        }
        
        TokenType type = IDENTIFIER;
        switch (resString) {
            case "and":       type = AND;       break;
            case "or":        type = OR;        break;
            case "not":       type = NOT;       break;
            case "if":        type = IF;        break;
            case "then":      type = THEN;      break;
            case "else":      type = ELSE;      break;
            case "var":       type = VAR;       break;
            case "in":        type = IN;        break;
            case "abort":     type = ABORT;     break;
            case "true":      type = TRUE;      break;
            case "false":     type = FALSE;     break;
            case "do":        type = DO;        break;
            case "while":     type = WHILE;     break;
            case "skip":      type = SKIP;      break;
            case "print":     type = PRINT;     break;
            case "procedure": type = PROCEDURE; break;
            case "begin":     type = BEGIN;     break;
            case "par":       type = PAR;       break;
            case "end":       type = END;       break;
        }

        return new Token(type, tmpline, tmpindex, resString);
    }

    private void logError(Token token, String message) {
        Imp.logDirectError(token, message);
    }
}
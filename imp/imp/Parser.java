package imp;

import java.util.ArrayList;
import java.util.List;

import static imp.TokenType.*;

class Parser {

    static class ParseException extends RuntimeException {}

    private int index;
    private List<Token> program;

    public Parser() {}

    Stm parse(List<Token> input) {
        program = input;

        try {
            Stm res = parseStm();
            if (index < program.size() - 1) {
                logError(peek(), "Unexpected Token.");
            }
            return res;
        } catch (ParseException e) {
            return new Stm.Skip();
        }
    }

    private void logError(Token token, String message) throws ParseException {
        Imp.logCompileError(token, message);
        throw new ParseException();
    }

    private Stm parseStm() {
        List<Stm> stms = new ArrayList<>();

        while (inBounds()) {
            if (peek().type == EOF) {
                break;
            } else if (peek().type == SKIP) {
                advance();
                stms.add(new Stm.Skip());
            } else if (peek().type == PRINT) {
                advance();
                stms.add(new Stm.Print());
            } else if (peek().type == IDENTIFIER && peekNext().type == ASSIGN) {
                Token left = advance();
                advance(); // ':=' token
                stms.add(new Stm.Assign(left, parseAExp()));
            } else if (peek().type == IF) {
                advance(); // 'if' token
                BExp condition = parseBExp();
                expect(THEN, "Expect 'then' after condition in 'if' statement.");
                Stm taken = parseStm();
                expect(ELSE, "Expect 'else' after list of statements in 'if' block.");
                Stm notTaken = parseStm();
                expect(END, "Expect 'end' after 'else' block in 'if' statement.");
                stms.add(new Stm.IfElse(condition, taken, notTaken));
            } else if (peek().type == WHILE) {
                advance(); // 'while' token
                BExp condition = parseBExp();
                expect(DO, "Expect 'do' after condition in 'while' statement.");
                Stm body = parseStm();
                expect(END, "Expect 'end' after body in 'while' block.");
                stms.add(new Stm.While(condition, body));
            }

            if (peek().type == SEMICOLON) {
                advance();
            } else {
                break; // require semicola as separators of multiple instructions
            }
        }

        return new Stm.Seq(stms);
    }

    // ( BExp [and|or] BExp )
    // Not BExp
    // AExp Rop AExp
    private BExp parseBExp() {
        switch (peek().type) {
            case NOT:
                advance();
                return new BExp.Not(parseBExp());
            default:
                int prev_index = index;
                try {
                    if (advance().type != LPAREN) {
                        throw new ParseException();
                    }
                    BExp left = parseBExp();
                    Token op = advance();
                    BExp right = parseBExp();
                    expect(RPAREN, "Expect ')' after group expression.");
                    if (op.type == AND || op.type == OR) {
                        return new BExp.Binary(left, right, op);
                    } else {
                        throw new ParseException();
                    }
                } catch (ParseException e) {
                    index = prev_index;
                    AExp left = parseAExp();
                    Token op = advance();
                    AExp right = parseAExp();
                    if (op.type == EQUAL || op.type == NOT_EQUAL || op.type == LESS ||
                            op.type == LESS_EQUAL || op.type == GREATER || op.type == GREATER_EQUAL) {
                        return new BExp.Comparison(left, right, op);
                    } else {
                        logError(op, "Illegal Operator.");
                    }
                }
        }

        return null;
    }

    // ( AExp [+|-|*] Aexp )
    // Var | Numeral
    private AExp parseAExp() {
        switch (peek().type) {
            case IDENTIFIER: case NUMBER:
                return new AExp.Literal(advance());
            case LPAREN:
                advance(); // '(' token
                AExp left = parseAExp();
                Token op = advance();
                AExp right = parseAExp();
                expect(RPAREN, "Expect ')' after binary expression.");
                if (op.type == PLUS || op.type == MINUS || op.type == TIMES) {
                    return new AExp.Binary(left, right, op);
                } else {
                    logError(op, "Illegal Operator.");
                }
            default:
                logError(advance(), "Unexpected Token.");
        }

        return null;
    }

    private boolean inBounds() {
        return index < program.size();
    }

    private Token peek() {
        return program.get(index);
    }

    private Token peekNext() {
        return program.get(index + 1);
    }

    private Token advance() {
        return program.get(index++);
    }

    private Token previous() {
        return program.get(index - 1);
    }

    private void expect(TokenType type, String errorMessage) {
        if (advance().type != type) {
            logError(previous(), errorMessage);
        }
    }
}
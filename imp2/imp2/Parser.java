package imp2;

import java.lang.RuntimeException;
import java.util.ArrayList;
import java.util.List;

import imp2.Stm.ProcDef;

import static imp2.TokenType.*;

class Parser {

    private List<Token> tokens;
    private int index;

    public Parser() {}

    Stm parse(List<Token> tokens) {
        this.tokens = tokens;
        reset();

        ParseFail fail = new ParseFail(null, null, -1);

        try {
            Stm result = parseStm();
            if (index < tokens.size() - 1) {
                throw new ParseFail(peek(), "Unexpected Token.", 1);
            }
            return result;
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            Stm result = new Stm.SB(parseBooleanWithErrorProductions());
            if (index < tokens.size() - 1) {
                throw new ParseFail(peek(), "Unexpected Token.", 1);
            }
            return result;
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            Stm result = new Stm.SA(parseArithmeticWithErrorProductions());
            if (index < tokens.size() - 1) {
                throw new ParseFail(peek(), "Unexpected Token.", 1);
            }
            return result;
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
            logError(fail.where, fail.message);
        }

        return null;
    }

    private Stm parseStm() {
        int prev_index = index;

        try {
            List<Stm> stms = new ArrayList<>();
            stms.add(parseSingleInstruction());

            while (peek().type == SEMICOLON) {
                advance(); // ';' token
                try {
                    Stm next = parseSingleInstruction();
                    stms.add(next);
                } catch (ParseFail fail) {
                    break;
                }
            }

            if (stms.size() > 1) {
                return new Stm.Seq(stms);
            } else if (stms.size() == 1) {
                return stms.get(0);
            } else {
                throw new ParseFail(peek(), "Expected Instruction.", 1.0);
            }
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseSingleInstruction() {
        return parseSingleInstruction(true);
    }

    private Stm parseSingleInstruction(boolean allowNd) {
        ParseFail fail = new ParseFail(null, null, -1);

        if (allowNd) {
            try {
                return parseStmNd();
            } catch (ParseFail f) {
                fail = (fail.likelihood >= f.likelihood) ? fail : f;
            }
        }

        try {
            return parseStmSeq();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseStmSingle();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseStmIf();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseStmWhile();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseStmProcDef();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }
        
        try {
            return parseStmProcCall();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseStmVar();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseStmAssign();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
            throw fail;
        }
    }

    private Stm parseStmNd() {
        int prev_index = index;

        try {
            Stm first = parseSingleInstruction(false);
            expect("Expect '|' between nondeterministically separated instructions.", 0.2, PIPE);
            Stm second = parseSingleInstruction(false);
            List<Stm> stms = new ArrayList<>();
            stms.add(first);
            stms.add(second);
            while (peek().type == PIPE) {
                advance(); // '|' token
                stms.add(parseSingleInstruction(false));
            }
            return new Stm.Nd(stms);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmSeq() {
        int prev_index = index;

        try {
            expect("Statement Sequences can be surrounded by parentheses.", -0.01, LPAREN);
            Stm first = parseSingleInstruction();
            expect("Statement Sequences must separate statements by semicola.", 0.95, SEMICOLON);
            Stm second = parseSingleInstruction();
            List<Stm> stms = new ArrayList<>();
            stms.add(first);
            stms.add(second);
            while (peek().type == SEMICOLON) {
                advance(); // ';' token
                if (peek().type == RPAREN) {
                    throw new ParseFail(peek(), "Expected Statement in Statement Sequence.", 0.95);
                }
                stms.add(parseSingleInstruction());
            }
            expect("Statement Sequences that open with a parenthesis must be closed by one.", 0.95, RPAREN);
            return new Stm.Seq(stms);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmSingle() {
        Token t = expect("Expect 'print', 'skip' or 'abort' for single statement.", 0.1, PRINT, SKIP, ABORT);
        switch (t.type) {
            case PRINT: return new Stm.Single(Stm.Single.Type.PRINT);
            case SKIP:  return new Stm.Single(Stm.Single.Type.SKIP);
            default:    return new Stm.Single(Stm.Single.Type.ABORT);
        }
    }

    private Stm parseStmAssign() {
        int prev_index = index;

        try {
            String name = expect("Expect Identifier for Assignment Statement", 0.1, IDENTIFIER).lexeme;
            expect("Expect ':=' as an assignment operator.", 0.1, ASSIGN);
            AExp exp = parseArithmeticWithErrorProductions();
            return new Stm.Assign(name, exp);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmIf() {
        int prev_index = index;

        try {
            expect("Expect 'if' Token in Conditional Statement.", 0, IF);
            BExp condition = parseBooleanWithErrorProductions();
            expect("Expect 'then' Token in 'if' Statement.", 0.95, THEN);
            Stm taken = parseStm();
            Stm notTaken = new Stm.Single(Stm.Single.Type.SKIP);
            if (peek().type == ELSE) {
                advance(); // 'else' token
                notTaken = parseStm();
            }
            expect("Expect 'end' Token in 'if' Statement.", 0.95, END);
            return new Stm.If(condition, taken, notTaken);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmWhile() {
        int prev_index = index;

        try {
            expect("Expect 'while' Token in Loop Statement.", 0, WHILE);
            BExp condition = parseBooleanWithErrorProductions();
            expect("Expect 'do' Token in 'while' Statement.", 0.95, DO);
            Stm body = parseStm();
            expect("Expect 'end' Token in 'while' Statement.", 0.95, END);
            return new Stm.While(condition, body);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmProcDef() {
        int prev_index = index;

        try {
            expect("Expect 'procedure' Token in Procedure Definition.", 0, PROCEDURE);
            Token name = expect("Procedure Definitions require a name.", 0.95, IDENTIFIER);
            expect("Procedure Definitions require an argument list that opens with a parenthesis.", 0.95, LPAREN);
            List<String> in = new ArrayList<>(), out = new ArrayList<>();
            while (peek().type != SEMICOLON) {
                in.add(expect("Procedure Definitions require identifiers as input arguments.", 0.95, IDENTIFIER).lexeme);
                if (expect("Procedure Arguments must be separated by a comma.", 0.95, COMMA, SEMICOLON).type == SEMICOLON) {
                    index--;
                }
            }
            advance(); // ';' token
            while (peek().type != RPAREN) {
                out.add(expect("Procedure Definitions must be closed with a parenthesis ')'.", 0.95, IDENTIFIER).lexeme);
                if (expect("Procedure Return Values must be separated by a comma.", 0.95, COMMA, RPAREN).type == RPAREN) {
                    index--;
                }
            }
            advance(); // ')' token
            expect("Procedure Definitions must begin with a 'begin' keyword.", 0.95, BEGIN);
            Stm body = parseStm();
            expect("Procedure Definitions must be closed with an 'end' token.", 0.95, END);
            return new Stm.ProcDef(name, in, out, body);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmProcCall() {
        int prev_index = index;

        try {
            Token name = expect("Procedure Calls must begin with the procedure's name.", 0, IDENTIFIER);
            expect("Procedure Calls must contain an argument list.", 0.2, LPAREN);
            List<AExp> in = new ArrayList<>();
            if (peek().type != SEMICOLON) {
                in.add(parseArithmeticWithErrorProductions());
            }
            while (peek().type != SEMICOLON) {
                if (expect("Procedure Arguments must be separated by a comma.", 0.95, COMMA, SEMICOLON).type == SEMICOLON) {
                    index--;
                }
                in.add(parseArithmeticWithErrorProductions());
            }
            advance(); // ';' token
            List<String> out = new ArrayList<>();
            if (peek().type != RPAREN) {
                out.add(expect("Procedure Call return value list must be terminated by a closing parenthesis.", 0.95, IDENTIFIER).lexeme);
            }
            while (peek().type != RPAREN) {
                if (expect("Procedure Call return value list must be terminated by a closing parenthesis.", 0.95, COMMA, RPAREN).type == RPAREN) {
                    index--;
                }
                out.add(expect("Procedure Call return value list must be terminated by a closing parenthesis.", 0.95, IDENTIFIER).lexeme);
            }
            advance(); // ')' token
            return new Stm.ProcCall(name, in, out);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private Stm parseStmVar() {
        int prev_index = index;

        try {
            expect("Scope block must start with 'var'.", 0, VAR);
            String name = expect("Scope block must denote a variable name.", 0.95, IDENTIFIER).lexeme;
            expect("Expect ':=' in scope block variable definition.", 0.95, ASSIGN);
            AExp decl = parseArithmeticWithErrorProductions();
            expect("Expect 'in' in scope block definition.", 0.95, IN);
            Stm body = parseStm();
            expect("Expect 'end' at the end of a scope block.", 0.95, END);
            return new Stm.Var(name, decl, body);
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private BExp parseBooleanWithErrorProductions() {
        ParseFail fail = new ParseFail(null, null, -1);

        boolean forcethrow = false;
        int prev_index = index;

        try {
            parseBoolean(); // left
            Token operator = advance();
            parseBoolean(); // right

            if (operator.type == AND || operator.type == OR) {
                forcethrow = true;
                throw new ParseFail(operator, "Binary Boolean Expression must be enclosed by Braces.", 2);
            } else {
                throw fail;
            }
        } catch (ParseFail f) {
            index = prev_index;
            fail = (fail.likelihood >= f.likelihood) ? fail : f;

            if (forcethrow) {
                throw fail;
            }
        }

        try {
            expect("", 0, LPAREN);
            parseBooleanWithErrorProductions();
            expect("", 0, RPAREN);

            forcethrow = true;
            throw new ParseFail(tokens.get(prev_index), "Boolean Expressions cannot be surrounded by additional Parentheses.", 2);
        } catch (ParseFail f) {
            index = prev_index;
            fail = (fail.likelihood >= f.likelihood) ? fail : f;

            if (forcethrow) {
                throw fail;
            }
        }

        try {
            return parseBoolean();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
            throw fail;
        }
    }

    private BExp parseBoolean() {
        ParseFail fail = new ParseFail(null, null, -1);

        if (peek().type == TRUE) {
            advance();
            return new BExp.Atomic(BExp.Atomic.Type.TRUE);
        } else if (peek().type == FALSE) {
            advance();
            return new BExp.Atomic(BExp.Atomic.Type.FALSE);
        }

        try {
            return parseBooleanBinary();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseBooleanNot();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseBooleanComparison();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
            throw fail;
        }
    }

    private BExp parseBooleanComparison() {
        int prev_index = index;

        try {
            AExp left = parseArithmeticWithErrorProductions();
            Token operator = advance();
            AExp right = parseArithmeticWithErrorProductions();
            switch (operator.type) {
                case EQUAL:
                    return new BExp.Comparison(left, right, BExp.Comparison.OpType.EQ);
                case NOT_EQUAL:
                    return new BExp.Comparison(left, right, BExp.Comparison.OpType.NEQ);
                case GREATER:
                    return new BExp.Comparison(left, right, BExp.Comparison.OpType.G);
                case GREATER_EQUAL:
                    return new BExp.Comparison(left, right, BExp.Comparison.OpType.GEQ);
                case LESS:
                    return new BExp.Comparison(left, right, BExp.Comparison.OpType.L);
                case LESS_EQUAL:
                    return new BExp.Comparison(left, right, BExp.Comparison.OpType.LEQ);
                default:
                    throw new ParseFail(operator, "Illegal Operator for Comparison Operator.", 0.75);
            }
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private BExp parseBooleanNot() {
        int prev_index = index;

        try {
            expect("Expect 'not' token for boolean negation operation.", 0, NOT);
            return new BExp.Not(parseBooleanWithErrorProductions());
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private BExp parseBooleanBinary() {
        int prev_index = index;

        try {
            expect("Binary Boolean Expressions must open with a '('.", 0.05, LPAREN);
            BExp left = parseBoolean();
            Token operator = advance();
            BExp right = parseBoolean();
            expect("Expect ')' after Binary Boolean Expression.", 0.9, RPAREN);
            switch (operator.type) {
                case AND:
                    return new BExp.Binary(left, right, BExp.Binary.OpType.AND);
                case OR:
                    return new BExp.Binary(left, right, BExp.Binary.OpType.OR);
                default:
                    throw new ParseFail(operator, "Illegal Operator for Binary Boolean Operation.", 0.7);
            }
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private AExp parseArithmeticWithErrorProductions() {
        ParseFail fail = new ParseFail(null, null, -1);

        boolean forcethrow = false;
        int prev_index = index;

        // No Parentheses
        try {
            parseArithmetic(); // left
            Token operator = advance();
            parseArithmetic(); // right

            if (operator.type == PLUS || operator.type == MINUS || operator.type == TIMES) {
                forcethrow = true;
                throw new ParseFail(operator, "Binary Arithmetic Expression must be enclosed by Braces.", 2);
            } else {
                throw fail;
            }
        } catch (ParseFail f) {
            index = prev_index;
            fail = (fail.likelihood >= f.likelihood) ? fail : f;

            if (forcethrow) {
                throw fail;
            }
        }

        try {
            expect("", 0, LPAREN);
            parseArithmeticWithErrorProductions();
            expect("", 0, RPAREN);

            forcethrow = true;
            throw new ParseFail(tokens.get(prev_index), "Arithmetic Expressions cannot be surrounded by additional Parentheses.", 2);
        } catch (ParseFail f) {
            index = prev_index;
            fail = (fail.likelihood >= f.likelihood) ? fail : f;

            if (forcethrow) {
                throw fail;
            }
        }

        try {
            return parseArithmetic();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
            throw fail;
        }
    }

    private AExp parseArithmetic() {
        ParseFail fail = new ParseFail(null, null, -1);

        try {
            return parseArithmeticBinary();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
        }

        try {
            return parseArithmeticAtomic();
        } catch (ParseFail f) {
            fail = (fail.likelihood >= f.likelihood) ? fail : f;
            throw fail;
        }
    }

    private AExp parseArithmeticBinary() {
        int prev_index = index;

        try {
            expect("Binary Arithmetic Expressions must open with a '('.", 0.1, LPAREN);
            AExp left = parseArithmetic();
            Token operator = advance();
            AExp right = parseArithmetic();
            expect("Expect ')' after Binary Arithmetic Expression.", 0.9, RPAREN);
            switch (operator.type) {
                case PLUS:
                    return new AExp.Binary(left, right, AExp.Binary.OpType.PLUS);
                case MINUS:
                    return new AExp.Binary(left, right, AExp.Binary.OpType.MINUS);
                case TIMES:
                    return new AExp.Binary(left, right, AExp.Binary.OpType.TIMES);
                default:
                    throw new ParseFail(operator, "Illegal Operator for Binary Arithmetic Operation.", 0.8);
            }
        } catch (ParseFail fail) {
            index = prev_index;
            throw fail;
        }
    }

    private AExp parseArithmeticAtomic() {
        return new AExp.Atomic(expect("Unexpected Atomic Token.", 0, IDENTIFIER, NUMBER));
    }

    private Token expect(String errorMessage, double likelihood, TokenType... types) {
        for (TokenType t : types) {
            if (peek().type == t) { return advance(); }
        }
        throw new ParseFail(peek(), errorMessage, likelihood);
    }

    private Token advance() {
        Token t = peek();
        index++;
        return t;
    }

    private Token peek() {
        if (index < tokens.size()) {
            return tokens.get(index);
        } else {
            return new Token(ERROR, 0, 0, "");
        }
    }

    private void reset() {
        index = 0;
    }

    private static class ParseFail extends RuntimeException {
        Token where;
        String message;
        double likelihood;

        public ParseFail(Token where, String message, double likelihood) {
            super();
            this.where = where;
            this.message = message;
            this.likelihood = likelihood;
        }
    }

    private void logError(Token token, String message) {
        Imp.logDirectError(token, message);
    }
}

/* Program Grammar (REPL):
 * 
 * Stm -> "skip" | "print" | Var ":=" AExp | Stm ";" | Stm ";" Stm |
 *          "if" BExp "then" Stm "else" Stm "end" |
 *          "while" BExp "do" Stm "end" |
 *          AExp | BExp
 * 
 * BExp -> "(" BExp [and|or] BExp ")" | "not" BExp | AExp RelOp AExp
 * 
 * AExp -> "(" AExp ArOp AExp ")" | Var | Number
 * 
 * Rules for Error Productions:
 * AExp -> AExp ArOp AExp | "(" AExp ")"
 * BExp -> BExp [and|or] BExp | "(" BExp ")"
 * 
 */

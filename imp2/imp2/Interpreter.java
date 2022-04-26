package imp2;

import java.util.HashMap;
import java.util.Map;

import static imp2.TokenType.*;

class Interpreter implements Stm.Visitor<Void>, BExp.Visitor<Boolean>, AExp.Visitor<Integer> {

    Map<String, Integer> variables;

    public Interpreter() {
        variables = new HashMap<>();
        reset();
    }

    void interpret(Stm tree) {
        try {
            tree.accept(this);
        } catch (InterpreterException e) {
            logError(e.token, e.message);
        }
    }

    private void logError(Token token, String message) {
        Imp.logDirectError(token, message);
    }

    private void reset() {
        variables.clear();
    }

    public Void visitSingle(Stm.Single stm) {
        if (stm.type == Stm.Single.Type.PRINT) {
            System.out.println("Program State");
            for (String s : variables.keySet()) {
                System.out.println("  " + s + " -> " + variables.get(s));
            }
        }
        return null;
    }

    public Void visitAssign(Stm.Assign stm) {
        variables.put(stm.name, stm.exp.accept(this));
        return null;
    }

    public Void visitIf(Stm.If stm) {
        if (stm.condition.accept(this)) {
            stm.taken.accept(this);
        } else {
            stm.notTaken.accept(this);
        }
        return null;
    }

    public Void visitWhile(Stm.While stm) {
        while (stm.condition.accept(this)) {
            stm.body.accept(this);
        }
        return null;
    }

    public Void visitSeq(Stm.Seq stm) {
        for (Stm s : stm.stms) {
            s.accept(this);
        }
        return null;
    }

    public Void visitBExp(Stm.SB stm) {
        System.out.println("==> " + (stm.exp.accept(this) ? "\033[3mtt\033[0m" : "\033[3mff\033[0m"));
        return null;
    }

    public Void visitAExp(Stm.SA stm) {
        System.out.println("==> " + stm.exp.accept(this));
        return null;
    }

    public Boolean visitBinary(BExp.Binary bexp) {
        // IMP doesn't specify whether it uses short-circuit evaluation,
        // but because expressions don't have side-effects, there is no
        // harm in evaluating both sides of a boolean binary expression.
        Boolean left = bexp.left.accept(this), right = bexp.right.accept(this);
        if (bexp.operator == BExp.Binary.OpType.AND) {
            return left && right;
        } else {
            return left || right;
        }
    }

    public Boolean visitNot(BExp.Not bexp) {
        return !bexp.exp.accept(this);
    }

    public Boolean visitComparison(BExp.Comparison bexp) {
        Integer left = bexp.left.accept(this), right = bexp.right.accept(this);

        switch (bexp.operator) {
            case EQ:  return left == right;
            case NEQ: return left != right;
            case G:   return left >  right;
            case GEQ: return left >= right;
            case L:   return left <  right;
            default:  return left <= right; // '<='
        }
    }

    public Boolean visitAtomic(BExp.Atomic bexp) {
        switch (bexp.type) {
            case TRUE: return true;
            default:   return false;
        }
    }

    public Integer visitBinary(AExp.Binary aexp) {
        Integer left = aexp.left.accept(this), right = aexp.right.accept(this);

        switch (aexp.operator) {
            case PLUS:  return left + right;
            case MINUS: return left - right;
            default:    return left * right;
        }
    }

    public Integer visitAtomic(AExp.Atomic aexp) {
        if (aexp.atom.type == IDENTIFIER) {
            if (variables.containsKey(aexp.atom.lexeme)) {
                return variables.get(aexp.atom.lexeme);
            } else {
                throw new InterpreterException(aexp.atom, "Variable Not Defined.");
            }
        } else { // NUMBER
            try {
                return Integer.parseInt(aexp.atom.lexeme);
            } catch (NumberFormatException e) {
                throw new InterpreterException(aexp.atom, "Number Format Error.");
            }
        }
    }

    private static class InterpreterException extends RuntimeException {
        Token token;
        String message;

        public InterpreterException(Token token, String message) {
            super();
            this.token = token;
            this.message = message;
        }
    }
}
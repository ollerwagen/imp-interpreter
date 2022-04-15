package imp;

import java.util.HashMap;
import java.util.Map;

import static imp.TokenType.*;

class Interpreter implements AExp.Visitor<Integer>, BExp.Visitor<Boolean>, Stm.Visitor<Void> {

    Map<String, Integer> variables;

    public Interpreter() {
        variables = new HashMap<>();
    }

    private void logError(Token token, String message) {
        Imp.logRuntimeError(token, message);
    }

    public Integer visitLiteral(AExp.Literal aexp) {
        if (aexp.token.type == NUMBER) {
            try {
                return Integer.parseInt(aexp.token.lexeme);
            } catch (NumberFormatException e) {
                logError(aexp.token, "Illegal Number Format.");
            }
        } else { // aexp.token.type == IDENTIFIER
            if (variables.containsKey(aexp.token.lexeme)) {
                return variables.get(aexp.token.lexeme);
            } else {
                logError(aexp.token, "Variable Not Initialized.");
            }
        }

        return 0; // never actually reached
    }

    public Integer visitBinary(AExp.Binary aexp) {
        Integer left = aexp.left.accept(this), right = aexp.right.accept(this);
        if (aexp.op.type == PLUS) {
            return left + right;
        } else if (aexp.op.type == MINUS) {
            return left - right;
        } else { // aexp.op.type == TIMES (parser guarantee)
            return left * right;
        }
    }

    public Boolean visitNot(BExp.Not bexp) {
        return !bexp.exp.accept(this);
    }

    public Boolean visitBinary(BExp.Binary bexp) {
        Boolean left = bexp.left.accept(this), right = bexp.right.accept(this);
        if (bexp.op.type == AND) {
            return left && right;
        } else { // bexp.op.type == OR
            return left || right;
        }
    }

    public Boolean visitComparison(BExp.Comparison bexp) {
        Integer left = bexp.left.accept(this), right = bexp.right.accept(this);
        switch (bexp.op.type) {
            case EQUAL:      return left == right;
            case NOT_EQUAL:  return left != right;
            case LESS:       return left < right;
            case LESS_EQUAL: return left <= right;
            case GREATER:    return left > right;
            default:         return left >= right;
        }
    }

    public Void visitBExp(Stm.BExp stm) {
        System.out.println("==> " + (stm.bexp.accept(this) ? "true" : "false"));
        return null;
    }

    public Void visitAExp(Stm.AExp stm) {
        System.out.println("==> " + stm.aexp.accept(this));
        return null;
    }

    public Void visitSeq(Stm.Seq stm) {
        for (Stm s : stm.stms) {
            s.accept(this);
        }
        return null;
    }

    public Void visitSkip(Stm.Skip stm) {
        return null;
    }

    public Void visitPrint(Stm.Print stm) {
        System.out.println("Program State:");
        for (String name : variables.keySet()) {
            System.out.println("  " + name + " <- " + variables.get(name));
        }

        return null;
    }

    public Void visitAssign(Stm.Assign stm) {
        variables.put(stm.left.lexeme, stm.right.accept(this));
        return null;
    }

    public Void visitIfElse(Stm.IfElse stm) {
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
}
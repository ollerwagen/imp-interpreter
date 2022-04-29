package imp2;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static imp2.TokenType.*;

class Interpreter implements Stm.Visitor<Void>, BExp.Visitor<Boolean>, AExp.Visitor<Integer> {

    Map<String, Integer> variables;
    Map<String, Stm.ProcDef> procedures;

    public Interpreter() {
        variables = new HashMap<>();
        procedures = new HashMap<>();
        reset();
    }

    void interpret(Stm tree) {
        try {
            tree.accept(this);
        } catch (InterpreterException e) {
            logError(e.token, e.message);
        } catch (InterpreterAbort a) {
            return;
        }
    }

    private void logError(Token token, String message) {
        Imp.logDirectError(token, message);
    }

    private void reset() {
        variables.clear();
        procedures.clear();
    }

    public Void visitNd(Stm.Nd stm) {
        Random r = new Random();
        int selected = (int)(r.nextDouble() * stm.stms.size());
        stm.stms.get(selected).accept(this);
        return null;
    }

    public Void visitSingle(Stm.Single stm) {
        if (stm.type == Stm.Single.Type.PRINT) {
            System.out.println("Program State");
            for (String s : variables.keySet()) {
                System.out.println("  " + s + " -> " + variables.get(s));
            }
        } else if (stm.type == Stm.Single.Type.ABORT) {
            throw new InterpreterAbort();
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

    public Void visitVar(Stm.Var stm) {
        boolean contains = variables.containsKey(stm.name);
        Integer prev = 0;
        if (contains) {
            prev = variables.get(stm.name);
        }
        variables.put(stm.name, stm.decl.accept(this));
        stm.body.accept(this);
        if (contains) {
            variables.put(stm.name, prev);
        } else {
            variables.remove(stm.name);
        }
        return null;
    }

    public Void visitSeq(Stm.Seq stm) {
        for (Stm s : stm.stms) {
            s.accept(this);
        }
        return null;
    }

    public Void visitProcDef(Stm.ProcDef stm) {
        procedures.put(stm.name.lexeme, stm);
        return null;
    }

    public Void visitProcCall(Stm.ProcCall stm) {
        if (!procedures.containsKey(stm.name.lexeme)) {
            throw new InterpreterException(stm.name, "Procedure undefined.");
        }

        Stm.ProcDef proc = procedures.get(stm.name.lexeme);
        if (stm.in.size() != proc.in.size() || stm.out.size() != proc.out.size()) {
            throw new InterpreterException(stm.name, "Argument Lists must match in length.");
        }
        Map<String, Integer> pre_vars = new HashMap<>(variables);
        for (int i = 0; i < proc.in.size(); i++) {
            variables.put(proc.in.get(i), stm.in.get(i).accept(this));
        }
        proc.body.accept(this);
        for (int i = 0; i < proc.out.size(); i++) {
            pre_vars.put(stm.out.get(i), variables.containsKey(proc.out.get(i)) ? variables.get(proc.out.get(i)) : 0);
        }
        variables = pre_vars;
        return null;
    }

    public Void visitBExp(Stm.SB stm) {
        System.out.println("==> \033[3m" + (stm.exp.accept(this) ? "tt" : "ff") + "\033[0m");
        return null;
    }

    public Void visitAExp(Stm.SA stm) {
        System.out.println("==> \033[3m" + stm.exp.accept(this) + "\033[0m");
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
        int left = bexp.left.accept(this), right = bexp.right.accept(this);

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
                return 0;
            }
        } else { // NUMBER
            try {
                return Integer.parseInt(aexp.atom.lexeme);
            } catch (NumberFormatException e) {
                throw new InterpreterException(aexp.atom, "Number Format Error.");
            }
        }
    }

    private static class InterpreterAbort extends RuntimeException {}

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
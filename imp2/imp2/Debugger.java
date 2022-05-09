package imp2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static imp2.TokenType.*;

class Debugger implements Stm.Visitor<Void>, BExp.Visitor<Boolean>, AExp.Visitor<Integer> {

    Map<String, Integer> variables;
    Map<String, Stm.ProcDef> procedures;

    BufferedReader reader;
    Printer printer;

    static final String indent = "~> ";

    public Debugger() {
        variables = new HashMap<>();
        procedures = new HashMap<>();
        reader = new BufferedReader(new InputStreamReader(System.in));
        printer = new Printer();
    }

    void debug(Stm tree) {
        try {
            tree.accept(this);
        } catch (DebugException e) {
            logError(e.token, e.message);
        } catch (DebugAbort a) {
            return;
        }
    }

    void awaitEnter(boolean print) {
        try {
            if (print) {
                System.out.print("   \033[2m<press enter for next instruction>\033[0m");
            }
            reader.readLine();
        } catch (IOException e) {}
    }

    void logError(Token token, String message) {
        Imp.logDirectError(token, message);
    }

    public Void visitNd(Stm.Nd stm) {
        System.out.println(indent + stm.accept(printer));
        Random r = new Random();
        int selected = (int)(r.nextDouble() * stm.stms.size());
        System.out.println(selected + ". instruction selected.");
        stm.stms.get(selected).accept(this);
        return null;
    }

    public Void visitSingle(Stm.Single stm) {
        System.out.println(indent + stm.accept(printer));
        if (stm.type == Stm.Single.Type.PRINT) {
            System.out.println("   Program State:");
            for (String s : variables.keySet()) {
                System.out.println("     " + s + " -> " + variables.get(s));
            }
        }
        awaitEnter(true);
        if (stm.type == Stm.Single.Type.ABORT) {
            throw new DebugAbort();
        } else if (stm.type == Stm.Single.Type.BREAK) {
            throw new DebugBreak();
        }
        return null;
    }

    public Void visitAssign(Stm.Assign stm) {
        System.out.println(indent + stm.accept(printer));
        Integer value = stm.exp.accept(this);
        variables.put(stm.name, value);
        System.out.println("   ==> " + value);
        awaitEnter(true);
        return null;
    }

    public Void visitIf(Stm.If stm) {
        System.out.println(indent + stm.accept(printer));
        System.out.print(indent + "\033[33m" + stm.condition.accept(printer) + "\033[0m");
        awaitEnter(false);
        if (stm.condition.accept(this)) {
            System.out.println("   \033[2m<if condition evaluates to true>\033[0m");
            stm.taken.accept(this);
        } else {
            System.out.println("   \033[2m<if condition evaluates to false>\033[0m");
            stm.notTaken.accept(this);
        }
        return null;
    }

    public Void visitWhile(Stm.While stm) {
        System.out.println(indent + stm.accept(printer));
        try {
            while (stm.condition.accept(this)) {
                System.out.print(indent + "\033[33m" + stm.condition.accept(printer) + "\033[0m");
                awaitEnter(false);
                System.out.println("   \033[2m<loop condition evaluates to true>\033[0m");
                stm.body.accept(this);
            }
        } catch (DebugBreak b) {}

        return null;
    }

    public Void visitFor(Stm.For stm) {
        System.out.println(indent + stm.accept(printer));
        try {
            for (variables.put(stm.loopvar, stm.start.accept(this));
                !variables.get(stm.loopvar).equals(stm.end.accept(this));
                variables.put(stm.loopvar, variables.get(stm.loopvar) + 1)) {

                System.out.println("  \033[2m<loop variable " + stm.loopvar + " set to " + variables.get(stm.loopvar) + ">\033[0m");
                stm.body.accept(this);
            }
        } catch (DebugBreak b) {}
        return null;
    }

    public Void visitVar(Stm.Var stm) {
        System.out.println(indent + stm.accept(printer));
        awaitEnter(false);
        boolean contains = variables.containsKey(stm.name);
        Integer prev = 0;
        if (contains) {
            prev = variables.get(stm.name);
        }
        Integer decl = stm.decl.accept(this);
        System.out.println("  \033[2m<assignment evaluates to " + decl + ">\033[0m");
        awaitEnter(false);
        variables.put(stm.name, decl);
        stm.body.accept(this);
        if (contains) {
            variables.put(stm.name, prev);
            System.out.println("  \033[2m<variable " + stm.name + " set back to " + prev + ">\033[0m");
        } else {
            variables.remove(stm.name);
            System.out.println("  \033[2m<previously undeclared variable " + stm.name + " removed again>\033[0m");
        }
        awaitEnter(false);
        return null;
    }

    public Void visitSeq(Stm.Seq stm) {
        for (Stm s : stm.stms) {
            s.accept(this);
        }
        return null;
    }

    public Void visitProcDef(Stm.ProcDef stm) {
        System.out.println(indent + stm.accept(printer));
        awaitEnter(true);
        procedures.put(stm.name.lexeme, stm);
        return null;
    }

    public Void visitProcCall(Stm.ProcCall stm) {
        if (!procedures.containsKey(stm.name.lexeme)) {
            throw new DebugException(stm.name, "Procedure undefined.");
        }

        Stm.ProcDef proc = procedures.get(stm.name.lexeme);
        if (stm.in.size() != proc.in.size() || stm.out.size() != proc.out.size()) {
            throw new DebugException(stm.name, "Argument Lists must match in length.");
        }
        Map<String, Integer> pre_vars = new HashMap<>(variables);
        for (int i = 0; i < proc.in.size(); i++) {
            Integer arg = stm.in.get(i).accept(this);
            System.out.println("  Arg: " + proc.in.get(i) + " := " + stm.in.get(i).accept(printer) + " (=" + arg + ")");
            variables.put(proc.in.get(i), arg);
            awaitEnter(false);
        }
        proc.body.accept(this);
        for (int i = 0; i < proc.out.size(); i++) {
            Integer val = 0;
            if (variables.containsKey(proc.out.get(i))) {
                val = variables.get(proc.out.get(i));
            }
            pre_vars.put(stm.out.get(i), val);
            System.out.println("  Ret: " + stm.out.get(i) + " := " + proc.out.get(i) + " (=" + val + ")");
            awaitEnter(false);
        }
        variables = pre_vars;
        return null;
    }

    public Void visitBExp(Stm.SB stm) {
        System.out.println(indent + stm.accept(printer));
        System.out.println("==> " + stm.exp.accept(this));
        return null;
    }

    public Void visitAExp(Stm.SA stm) {
        System.out.println(indent + stm.accept(printer));
        System.out.println("==> " + stm.exp.accept(this));
        return null;
    }

    public Boolean visitBinary(BExp.Binary bexp) {
        Boolean left = bexp.left.accept(this), right = bexp.right.accept(this);

        switch (bexp.operator) {
            case AND: return left && right;
            default:  return left || right;
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
            default:  return left <= right;
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
        } else { // aexp.atom.type == NUMBER
            try {
                return Integer.parseInt(aexp.atom.lexeme);
            } catch (NumberFormatException e) {
                throw new DebugException(aexp.atom, "Number Format Error.");
            }
        }
    }

    static class DebugAbort extends RuntimeException {}
    static class DebugBreak extends RuntimeException {}

    static class DebugException extends RuntimeException {
        Token token;
        String message;

        public DebugException(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }
}
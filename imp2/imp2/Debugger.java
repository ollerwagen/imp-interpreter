package imp2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static imp2.TokenType.*;

class Debugger implements Stm.Visitor<Void>, BExp.Visitor<Boolean>, AExp.Visitor<Integer> {

    Map<String, Integer> variables;

    BufferedReader reader;
    Printer printer;

    static final String indent = "~> ";

    public Debugger() {
        variables = new HashMap<>();
        reader = new BufferedReader(new InputStreamReader(System.in));
        printer = new Printer();
    }

    void debug(Stm tree) {
        try {
            tree.accept(this);
        } catch (DebugException e) {
            logError(e.token, e.message);
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

    public Void visitSingle(Stm.Single stm) {
        System.out.println(indent + stm.accept(printer));
        if (stm.type == Stm.Single.Type.PRINT) {
            System.out.println("   Program State:");
            for (String s : variables.keySet()) {
                System.out.println("     " + s + " -> " + variables.get(s));
            }
        }
        awaitEnter(true);
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
        while (stm.condition.accept(this)) {
            System.out.print(indent + "\033[33m" + stm.condition.accept(printer) + "\033[0m");
            awaitEnter(false);
            System.out.println("   \033[2m<loop condition evaluates to true>\033[0m");
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
                throw new DebugException(aexp.atom, "Undefined Variable.");
            }
        } else { // aexp.atom.type == NUMBER
            try {
                return Integer.parseInt(aexp.atom.lexeme);
            } catch (NumberFormatException e) {
                throw new DebugException(aexp.atom, "Number Format Error.");
            }
        }
    }

    static class DebugException extends RuntimeException {
        Token token;
        String message;

        public DebugException(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }
}
package imp2;

class Printer implements Stm.Visitor<String>, BExp.Visitor<String>, AExp.Visitor<String> {

    public Printer() {}

    public String print(Stm stm) {
        return stm.accept(this);
    }

    public String visitNd(Stm.Nd stm) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stm.stms.get(0).accept(this));
        for (int i = 1; i < stm.stms.size(); i++) {
            stringBuilder.append(" | ").append(stm.stms.get(i).accept(this));
        }
        return stringBuilder.toString();
    }

    public String visitSingle(Stm.Single stm) {
        switch (stm.type) {
            case PRINT: return "print";
            case SKIP:  return "skip";
            default:    return "abort";
        }
    }

    public String visitAssign(Stm.Assign stm) {
        return stm.name + " := " + stm.exp.accept(this);
    }

    public String visitIf(Stm.If stm) {
        return "if " + stm.condition.accept(this) + " then " +
            stm.taken.accept(this) + " else " + stm.notTaken.accept(this) + " end";
    }

    public String visitWhile(Stm.While stm) {
        return "while " + stm.condition.accept(this) + " do " +
            stm.body.accept(this) + " end";
    }

    public String visitVar(Stm.Var stm) {
        return "var " + stm.name + " := " + stm.decl.accept(this) + " in " +
            stm.body.accept(this) + " end";
    }

    public String visitSeq(Stm.Seq stm) {
        if (stm.stms.isEmpty()) { return ""; }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stm.stms.size() - 1; i++) {
            stringBuilder.append(stm.stms.get(i).accept(this)).append("; ");
        }
        stringBuilder.append(stm.stms.get(stm.stms.size() - 1).accept(this));
        return stringBuilder.toString();
    }

    public String visitProcDef(Stm.ProcDef stm) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("procedure ").append(stm.name.lexeme).append("(");
        for (int i = 0; i < stm.in.size() - 1; i++) {
            stringBuilder.append(stm.in.get(i) + ", ");
        }
        if (stm.in.size() > 0) {
            stringBuilder.append(stm.in.get(stm.in.size() - 1));
        }
        stringBuilder.append("; ");
        for (int i = 0; i < stm.out.size() - 1; i++) {
            stringBuilder.append(stm.out.get(i) + ", ");
        }
        if (stm.out.size() > 0) {
            stringBuilder.append(stm.out.get(stm.out.size() - 1));
        }
        stringBuilder.append(") begin ").append(stm.body.accept(this)).append(" end");
        return stringBuilder.toString();
    }

    public String visitProcCall(Stm.ProcCall stm) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stm.name.lexeme).append("(");
        for (int i = 0; i < stm.in.size() - 1; i++) {
            stringBuilder.append(stm.in.get(i).accept(this)).append(", ");
        }
        if (stm.in.size() > 0) {
            stringBuilder.append(stm.in.get(stm.in.size() - 1).accept(this));
        }
        stringBuilder.append("; ");
        for (int i = 0; i < stm.out.size() - 1; i++) {
            stringBuilder.append(stm.out.get(i)).append(", ");
        }
        if (stm.out.size() > 0) {
            stringBuilder.append(stm.out.get(stm.out.size() - 1));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String visitBExp(Stm.SB stm) {
        return stm.exp.accept(this);
    }

    public String visitAExp(Stm.SA stm) {
        return stm.exp.accept(this);
    }

    public String visitBinary(BExp.Binary bexp) {
        return "(" + bexp.left.accept(this) + (bexp.operator == BExp.Binary.OpType.AND ? " and " : " or ") + bexp.right.accept(this) + ")";
    }

    public String visitNot(BExp.Not bexp) {
        return "not " + bexp.exp.accept(this);
    }

    public String visitComparison(BExp.Comparison bexp) {
        String op = " = ";
        switch (bexp.operator) {
            case NEQ: op = " # "; break;
            case L:   op = " < "; break;
            case LEQ: op = " <= "; break;
            case G:   op = " > "; break;
            case GEQ: op = " >= "; break;
            default:  break;
        }
        return bexp.left.accept(this) + op + bexp.right.accept(this);
    }

    public String visitAtomic(BExp.Atomic bexp) {
        switch (bexp.type) {
            case TRUE: return "true";
            default:   return "false";
        }
    }

    public String visitBinary(AExp.Binary aexp) {
        String op = " + ";
        switch (aexp.operator) {
            case MINUS: op = " - "; break;
            case TIMES: op = " * "; break;
            default:    break;
        }
        return "(" + aexp.left.accept(this) + op + aexp.right.accept(this) + ")";
    }

    public String visitAtomic(AExp.Atomic aexp) {
        return aexp.atom.lexeme;
    }
}
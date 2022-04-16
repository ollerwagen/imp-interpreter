package imp2;

class Printer implements Stm.Visitor<String>, BExp.Visitor<String>, AExp.Visitor<String> {

    public Printer() {}

    public String print(Stm stm) {
        return stm.accept(this);
    }

    public String visitSingle(Stm.Single stm) {
        if (stm.type == Stm.Single.Type.PRINT) {
            return "print";
        } else {
            return "skip";
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

    public String visitSeq(Stm.Seq stm) {
        if (stm.stms.isEmpty()) { return ""; }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stm.stms.size() - 1; i++) {
            stringBuilder.append(stm.stms.get(i).accept(this)).append("; ");
        }
        stringBuilder.append(stm.stms.get(stm.stms.size() - 1).accept(this));
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
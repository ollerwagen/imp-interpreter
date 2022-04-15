package imp;

class PrettyPrinter implements AExp.Visitor<String>, BExp.Visitor<String>, Stm.Visitor<String> {

    private int indent = 0;

    public String visitLiteral(AExp.Literal aexp) {
        return aexp.token.lexeme;
    }

    public String visitBinary(AExp.Binary aexp) {
        return "(" + aexp.left.accept(this) + " " + aexp.op.lexeme + " " + aexp.right.accept(this) + ")";
    }

    public String visitNot(BExp.Not bexp) {
        return "not " + bexp.exp.accept(this);
    }

    public String visitBinary(BExp.Binary bexp) {
        return "(" + bexp.left.accept(this) + " " + bexp.op.lexeme + " " + bexp.right.accept(this) + ")";
    }

    public String visitComparison(BExp.Comparison bexp) {
        return bexp.left.accept(this) + " " + bexp.op.lexeme + " " + bexp.right.accept(this);
    }

    public String visitSeq(Stm.Seq stm) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Stm s : stm.stms) {
            stringBuilder.append(s.accept(this)).append("\n");
        }
        return stringBuilder.toString();
    }

    public String visitSkip(Stm.Skip stm) {
        return "  ".repeat(indent) + "skip";
    }

    public String visitPrint(Stm.Print stm) {
        return "  ".repeat(indent) + "print";
    }

    public String visitAssign(Stm.Assign stm) {
        return "  ".repeat(indent) + stm.left.lexeme + " := " + stm.right.accept(this);
    }

    public String visitIfElse(Stm.IfElse stm) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("  ".repeat(indent)).append("if ").append(stm.condition.accept(this)).append(" then\n");
        indent++;
        stringBuilder.append(stm.taken.accept(this));
        indent--;
        stringBuilder.append("  ".repeat(indent)).append("else\n");
        indent++;
        stringBuilder.append(stm.notTaken.accept(this));
        indent--;
        stringBuilder.append("  ".repeat(indent)).append("end\n");
        return stringBuilder.toString();
    }

    public String visitWhile(Stm.While stm) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("  ".repeat(indent)).append("while ").
                append(stm.condition.accept(this)).append(" do\n");
        indent++;
        stringBuilder.append(stm.body.accept(this));
        indent--;
        stringBuilder.append("  ".repeat(indent)).append("end\n");
        return stringBuilder.toString();
    }
}

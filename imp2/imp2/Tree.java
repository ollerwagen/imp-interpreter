package imp2;

import java.util.List;

abstract class Stm {

    abstract <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {
        T visitSingle(Single stm);
        T visitAssign(Assign stm);
        T visitIf(If stm);
        T visitWhile(While stm);
        T visitVar(Var stm);
        T visitSeq(Seq stm);
        T visitNd(Nd stm);
        T visitProcDef(ProcDef stm);
        T visitProcCall(ProcCall stm);

        T visitBExp(SB stm);
        T visitAExp(SA stm);
    }

    static class Single extends Stm {
        enum Type { SKIP, PRINT, ABORT }
        Type type;

        Single(Type type) {
            this.type = type;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitSingle(this);
        }
    }

    static class Assign extends Stm {
        String name;
        imp2.AExp exp;

        Assign(String name, imp2.AExp exp) {
            this.name = name;
            this.exp = exp;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssign(this);
        }
    }

    static class If extends Stm {
        BExp condition;
        Stm taken, notTaken;

        If(BExp condition, Stm taken, Stm notTaken) {
            this.condition = condition;
            this.taken = taken;
            this.notTaken = notTaken;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIf(this);
        }
    }

    static class While extends Stm {
        BExp condition;
        Stm body;

        While(BExp condition, Stm body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhile(this);
        }
    }

    static class Var extends Stm {
        String name;
        AExp decl;
        Stm body;

        Var(String name, AExp decl, Stm body) {
            this.name = name;
            this.decl = decl;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVar(this);
        }
    }

    static class Seq extends Stm {
        List<Stm> stms;

        Seq(List<Stm> stms) {
            this.stms = stms;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitSeq(this);
        }
    }

    static class Nd extends Stm {
        List<Stm> stms;

        Nd(List<Stm> stms) {
            this.stms = stms;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitNd(this);
        }
    }

    static class ProcDef extends Stm {
        Token name;
        List<String> in, out;
        Stm body;

        ProcDef(Token name, List<String> in, List<String> out, Stm body) {
            this.name = name;
            this.in = in;
            this.out = out;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitProcDef(this);            
        }
    }

    static class ProcCall extends Stm {
        Token name;
        List<AExp> in;
        List<String> out;
        
        ProcCall(Token name, List<AExp> in, List<String> out) {
            this.name = name;
            this.in = in;
            this.out = out;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitProcCall(this);
        }
    }

    static class SB extends Stm {
        BExp exp;

        SB(BExp exp) {
            this.exp = exp;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBExp(this);
        }
    }

    static class SA extends Stm {
        AExp exp;

        SA(AExp exp) {
            this.exp = exp;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAExp(this);
        }
    }
}

abstract class BExp {

    abstract <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {
        T visitBinary(Binary bexp);
        T visitNot(Not bexp);
        T visitComparison(Comparison bexp);
        T visitAtomic(Atomic bexp);
    }

    static class Binary extends BExp {
        BExp left, right;
        enum OpType { AND, OR }
        OpType operator;

        Binary(BExp left, BExp right, OpType operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinary(this);
        }
    }

    static class Not extends BExp {
        BExp exp;

        Not(BExp exp) {
            this.exp = exp;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitNot(this);
        }
    }

    static class Comparison extends BExp {
        AExp left, right;
        enum OpType { EQ, NEQ, L, LEQ, G, GEQ }
        OpType operator;

        Comparison(AExp left, AExp right, OpType operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitComparison(this);
        }
    }

    static class Atomic extends BExp {
        enum Type { TRUE, FALSE }
        Type type;

        Atomic(Type type) {
            this.type = type;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAtomic(this);
        }
    }
}

abstract class AExp {

    abstract <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {
        T visitBinary(Binary aexp);
        T visitAtomic(Atomic aexp);
    }

    static class Binary extends AExp {
        AExp left, right;
        enum OpType { PLUS, MINUS, TIMES }
        OpType operator;

        Binary(AExp left, AExp right, OpType operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinary(this);
        }
    }

    static class Atomic extends AExp {
        Token atom;

        Atomic(Token atom) {
            this.atom = atom;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAtomic(this);
        }
    }
}
package imp;

import java.util.List;

abstract class AExp {
    abstract <T> T accept(Visitor<T> v);

    interface Visitor<T> {
        T visitLiteral(Literal aexp);
        T visitBinary(Binary aexp);
    }

    static class Literal extends AExp {
        Token token;
        Literal(Token token) {
            this.token = token;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitLiteral(this);
        }
    }

    static class Binary extends AExp {
        AExp left, right;
        Token op;
        Binary(AExp left, AExp right, Token op) {
            this.left = left;
            this.right = right;
            this.op = op;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitBinary(this);
        }
    }
}

abstract class BExp {
    abstract <T> T accept(Visitor<T> v);

    interface Visitor<T> {
        T visitNot(Not bexp);
        T visitBinary(Binary bexp);
        T visitComparison(Comparison bexp);
    }

    static class Not extends BExp {
        BExp exp;
        Not(BExp exp) {
            this.exp = exp;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitNot(this);
        }
    }

    static class Binary extends BExp {
        BExp left, right;
        Token op;
        Binary(BExp left, BExp right, Token op) {
            this.left = left;
            this.right = right;
            this.op = op;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitBinary(this);
        }
    }

    static class Comparison extends BExp {
        AExp left, right;
        Token op;
        Comparison(AExp left, AExp right, Token op) {
            this.left = left;
            this.right = right;
            this.op = op;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitComparison(this);
        }
    }
}

abstract class Stm {
    abstract <T> T accept(Visitor<T> v);

    interface Visitor<T> {
        T visitAExp(AExp aexp);
        T visitBExp(BExp bexp);
        T visitSeq(Seq stm);
        T visitSkip(Skip stm);
        T visitPrint(Print stm);
        T visitAssign(Assign stm);
        T visitIfElse(IfElse stm);
        T visitWhile(While stm);
    }

    static class AExp extends Stm {
        imp.AExp aexp;
        AExp(imp.AExp aexp) {
            this.aexp = aexp;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitAExp(this);
        }
    }

    static class BExp extends Stm {
        imp.BExp bexp;
        BExp(imp.BExp bexp) {
            this.bexp = bexp;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitBExp(this);
        }
    }

    static class Seq extends Stm {
        List<Stm> stms;
        Seq(List<Stm> stms) {
            this.stms = stms;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitSeq(this);
        }
    }

    static class Skip extends Stm {
        Skip() {}

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitSkip(this);
        }
    }

    static class Print extends Stm {
        Print() {}

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitPrint(this);
        }
    }

    static class Assign extends Stm {
        Token left;
        imp.AExp right;
        Assign(Token left, imp.AExp right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitAssign(this);
        }
    }

    static class IfElse extends Stm {
        imp.BExp condition;
        Stm taken, notTaken;
        IfElse(imp.BExp condition, Stm taken, Stm notTaken) {
            this.condition = condition;
            this.taken = taken;
            this.notTaken = notTaken;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitIfElse(this);
        }
    }

    static class While extends Stm {
        imp.BExp condition;
        Stm body;
        While(imp.BExp condition, Stm body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitWhile(this);
        }
    }
}
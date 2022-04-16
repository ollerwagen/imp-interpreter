package imp2;

enum TokenType
{
    IDENTIFIER,
    NUMBER,

    LPAREN,             // (
    RPAREN,             // )

    PLUS,               // +
    MINUS,              // -
    TIMES,              // *

    EQUAL,              // =
    NOT_EQUAL,          // #
    LESS,               // <
    GREATER,            // >
    LESS_EQUAL,         // <=
    GREATER_EQUAL,      // >=

    ASSIGN,             // :=
    SEMICOLON,          // ;

    AND,                // and
    OR,                 // or
    NOT,                // not

    IF,                 // if
    THEN,               // then
    ELSE,               // else

    DO,                 // do
    WHILE,              // while

    SKIP,               // skip
    PRINT,              // print

    END,                // end

    ERROR,              // any error
    EOF
}

class Token {
    TokenType type;
    int line, index;
    String lexeme;

    public Token(TokenType type, int line, int index, String lexeme) {
        this.type = type;
        this.line = line;
        this.index = index;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[").append(type).append("] (").
                append(line).append(":").append(index).
                append(") -> \"").append(lexeme).append("\"");
        
        return stringBuilder.toString();
    }
}
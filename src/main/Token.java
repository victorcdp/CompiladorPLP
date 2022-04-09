package main;

public class Token {

    final public TokenTypes tipo;
    final public String lexema;
    final public Object literal;
    final public int linha;
    final public int coluna;

    public Token(TokenTypes tipo, String lexema, Object literal, int linha, int coluna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.literal = literal;
        this.linha = linha;
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        return tipo + " " + lexema;
    }
}

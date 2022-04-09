package main;

public class VarNode {
    public VarNode next;
    final public TokenTypes tipo;
    public Object literal;
    public int bloco;
    public double valorNumerico = 0;
    public String valorString = "";
    
    public VarNode(){
        tipo = null;
        literal = null;
    }
    
    public VarNode(TokenTypes tipo, Object literal, int bloco){
        this.tipo = tipo;
        this.literal = literal;
        this.bloco = bloco;
    }
    
    public VarNode(TokenTypes tipo, Object literal, int bloco, double valorNumerico){
        this.tipo = tipo;
        this.literal = literal;
        this.bloco = bloco;
        this.valorNumerico = valorNumerico;
    }
    
    public VarNode(TokenTypes tipo, Object literal, int bloco, String valorString){
        this.tipo = tipo;
        this.literal = literal;
        this.bloco = bloco;
        this.valorString = valorString;
    }
    
}


package main;

//OQ FALTA
//Print
//1 comando de entrada (?)
//arrays

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private Token look = null;
    private int tokenIt = 0;
    private int bloco = 0;
    public static ListaVar lista = new ListaVar();
    public TokenTypes tipo;
    

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void nextTk() {
        tokenIt += 1;
        look = tokens.get(tokenIt);
        //System.out.println("novo token: " + look); // só pra teste
    }
    
    public Token lookAhead(int n) {
    	return tokens.get(tokenIt + n);
    }
    

    public boolean match(TokenTypes tipo) {
        return look.tipo == tipo;
    }

    public void error(String msg) {
        System.out.println("Erro!\n" + msg + "\nUltimo Token lido: " + look + " na linha: " + look.linha + " e coluna: " + look.coluna);
        System.exit(0);
    }

    public void programa() {
        look = tokens.get(tokenIt);
        if (match(TokenTypes.INT_ID)) {
            nextTk();
            if (match(TokenTypes.MAIN_ID)) {
                nextTk();
                if (match(TokenTypes.ABRE_PARENTESES)) {
                    nextTk();
                    if (match(TokenTypes.FECHA_PARENTESES)) {
                        nextTk();
                        bloco();
                    } else {
                        error("Não fechou parenteses");
                    }
                } else {
                    error("Não abriu parenteses depois do Main");
                }
            } else {
                error("Não especificou o Main");
            }
        } else {
            error("Não especificou o int inicial");
        }
    }

    private void bloco() {
        if (match(TokenTypes.ABRE_CHAVE)) {
            bloco++;
            //System.out.println("abriu bloco");
            //printLista();
            nextTk();
            while (match(TokenTypes.INT_ID) || match(TokenTypes.FLOAT_ID) || match(TokenTypes.CHAR_ID)) {
                decl_var();
            }
            while (match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.WHILE_ID) || match(TokenTypes.DO_ID) || match(TokenTypes.IF_ID) || match(TokenTypes.FOR_ID) || match(TokenTypes.PRINTF) || match(TokenTypes.SCANF)) {
                comando();
            }
            if (!match(TokenTypes.FECHA_CHAVE)) {
                error("Bloco mal formado");
            }
            //System.out.println("fechou bloco, lista antes de fechar:");
            //printLista();
            lista.removerBloco(bloco);
            //System.out.println("Lista depois de fechar:");
            //printLista();
            bloco--;
        } else {
            error("Bloco mal formado");
        }
    }

    public void decl_var() {
        tipo();
        tipo = look.tipo;
        nextTk();
        id();
        tipo = null;
    }

    public void tipo() {
        if (!(match(TokenTypes.INT_ID) || match(TokenTypes.FLOAT_ID) || match(TokenTypes.CHAR_ID))) {
            error("Especificação do tipo da variavel errada");
        }
    }

    public void id() {
        if (match(TokenTypes.IDENTIFICADOR)) {
            lista.add(new VarNode(tipo, look.lexema, bloco));
            nextTk();
            while (match(TokenTypes.VIRGULA)) {
                nextTk();
                if (match(TokenTypes.IDENTIFICADOR)) {
                    lista.add(new VarNode(tipo, look.lexema, bloco));
                    nextTk();
                } else {
                    error("Objeto inesperado depois da vírgula");
                }
            }
            if(match(TokenTypes.ABRE_COLCHETE)) {
            	nextTk();
            	
            	if(match(TokenTypes.INT) || match(TokenTypes.FLOAT) || match(TokenTypes.IDENTIFICADOR)) {
            		nextTk();
            		if(match(TokenTypes.FECHA_COLCHETE)) {
            			nextTk();
            		}
            	}
            	else if(match(TokenTypes.FECHA_COLCHETE)) {
            		nextTk();
            	}
            }
            if (!(match(TokenTypes.PONTO_VIRGULA))) {
                error("Erro no ponto de virgula");
            }
            nextTk();
        } else {
            error("Identificador (nome da variável) não encontrado!");
        }
    }

    private void comando() {
        if (match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.ABRE_CHAVE)) {
            comando_basico();
        } else if (match(TokenTypes.WHILE_ID) || match(TokenTypes.DO_ID)) {
            iteracao();
        } else if(match(TokenTypes.FOR_ID)) {
        	iteracaoFor();
        } else if(match(TokenTypes.PRINTF)) {
            print();
        } else if(match(TokenTypes.SCANF)) {
            scan();
        }
        else if (match(TokenTypes.IF_ID)) {
            nextTk();
            if (match(TokenTypes.ABRE_PARENTESES)) {
                expr_relacional();
                if (match(TokenTypes.FECHA_PARENTESES)) {
                    nextTk();
                    comando();
                    nextTk();
                    if(match(TokenTypes.ELSE_ID)){
                        nextTk();
                        comando();
                    }
                } else {
                    error("Não fechou parenteses na condição do IF");
                }
            } else {
                error("Não abriu parenteses na condição do IF");
            }
        }
    }
    
    public void comando_basico(){
        if(match(TokenTypes.IDENTIFICADOR)){
            atribuicao(false);
        }
        else if(match(TokenTypes.ABRE_CHAVE)){
            bloco();
        }
    }
    
    public void atribuicao(boolean pularPV){
        if(match(TokenTypes.IDENTIFICADOR)){
            nextTk();
            if(match(TokenTypes.ATRIBUICAO)){
                	op_aritmetica();
                if(match(TokenTypes.PONTO_VIRGULA)){
                    nextTk();
                }
                else if (!pularPV){
                    error("ponto e virgula no lugar errado");
                }
            }
            else{
                error("faltando sinal de atribuição");
            }
        }
        else{
            error("faltando o identificador");
        }
    }
    
  
    
    public void iteracao(){
        if(match(TokenTypes.WHILE_ID)){
            nextTk();
            if(match(TokenTypes.ABRE_PARENTESES)){
                expr_relacional();
                if(match(TokenTypes.FECHA_PARENTESES)){
                    nextTk();
                    comando();
                    nextTk();
                }
                else{
                    error("parentese da condição do while não foi fechada");
                }
            }
            else{
                error("parentese da condição do while não foi aberto");
            }
        }
        else if(match(TokenTypes.DO_ID)){
            nextTk();
            comando();
            if(match(TokenTypes.FECHA_CHAVE)){
                nextTk();
                if(match(TokenTypes.WHILE_ID)){
                    nextTk();
                    if(match(TokenTypes.ABRE_PARENTESES)){
                        expr_relacional();
                        if(match(TokenTypes.FECHA_PARENTESES)){
                            nextTk();
                            if(!match(TokenTypes.PONTO_VIRGULA)){
                                error("falta do ponto-virgula depois do while.");
                            }
                            nextTk();
                        }
                        else{
                            error("não fechou parenteses");
                        }
                    }
                    else{
                        error("não abriu parenteses da condição do while");
                    }
                }
                else{
                    error("não forneceu a condição do DO");
                }
            }
            else{
                error("não fechou o bloco do DO");
            }
        }
    }
    
    public void iteracaoFor() {
    	if(match(TokenTypes.FOR_ID)){
            nextTk();
            if(match(TokenTypes.ABRE_PARENTESES)){
            	nextTk();
                atribuicao(false);
                expr_relacional();
                if(match(TokenTypes.PONTO_VIRGULA)) {
                	nextTk();
                	atribuicao(true);
                	if(match(TokenTypes.FECHA_PARENTESES)) {
                		nextTk();
                		bloco();
                	}
                	else {
                		error("For mal formado");
                	}
                }
                else {
                	error("falta de ponto virgula na condição do for");
                }
            }
            else{
                error("parentese da condição do for não foi aberto");
            }
        }
    }

    public void expr_relacional() {
        op_aritmetica();
        op_relacional();
        op_aritmetica();
    }
    
    public void op_relacional(){
        if(!(match(TokenTypes.MENOR_IGUAL) || match(TokenTypes.MENOR) || match(TokenTypes.MAIOR_IGUAL) || match(TokenTypes.MAIOR) || match(TokenTypes.IGUAL) || match(TokenTypes.DIFERENTE))){
            error("operador relacional não existente.");
        }
    }
    
    public void op_aritmetica() {
        termo();
        somaOuSubtracao();
    }

    public void termo() {
        fator();
        multiplicacaoOuDivisao();
    }
    
    public void fator(){
        nextTk();
        if(match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.INT) || match(TokenTypes.FLOAT) || match(TokenTypes.CHAR)){
            nextTk();
        }
        else if(match(TokenTypes.ABRE_PARENTESES)){
            op_aritmetica();
            if(match(TokenTypes.FECHA_PARENTESES)){
                nextTk();
            }
            else{
                error("não fechou parenteses na expressão aritmetica");
            }
        }
        else if(match(TokenTypes.MULTIPLICACAO) || match(TokenTypes.DIVISAO) || match(TokenTypes.SOMA) || match(TokenTypes.SUBTRACAO) || match(TokenTypes.PONTO_VIRGULA)){
            error("operador inválido na expressão aritmetica");
        }
    }

    public void multiplicacaoOuDivisao(){
        if(match(TokenTypes.MULTIPLICACAO) || match(TokenTypes.DIVISAO)){
            fator();
            multiplicacaoOuDivisao();
        }
        else if(match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.INT) || match(TokenTypes.FLOAT) || match(TokenTypes.CHAR)){
            error("objeto inesperado na expressão aritmetica");
        }
    }

    public void somaOuSubtracao(){
        if(match(TokenTypes.SOMA) || match(TokenTypes.SUBTRACAO)){
            termo();
            somaOuSubtracao();
        }
    }
    
    public static void printLista(){
        System.out.println(lista.toString());
    }

    public void print() {
        nextTk();
        if(match(TokenTypes.ABRE_PARENTESES)) {
            nextTk();
            if(match(TokenTypes.ASPAS)) {
                nextTk();
                while(!match(TokenTypes.ASPAS)) {
                    nextTk();
                }
                if(match(TokenTypes.ASPAS)) {
                    nextTk();
                }
                else {
                    error("não fechou aspas do printf");
                }
            }
            else if(match(TokenTypes.IDENTIFICADOR)) {
                if(lookAhead(1).tipo == TokenTypes.ASPAS) {
                    error("print mal formado");
                }
                nextTk();
            }
            if(match(TokenTypes.FECHA_PARENTESES)) {
                nextTk();
                if(match(TokenTypes.PONTO_VIRGULA)) {
                    nextTk();
                    return;
                }
                else {
                    error("falta de ponto virgula");
                }
            }
            else {
                error("não fechou parenteses do printf");
            }
        }
        else {
            error("não abriu parenteses no print");
        }
    }

    public void scan() {
    	nextTk();
    	if(match(TokenTypes.ABRE_PARENTESES)) {
    		nextTk();
    		if(match(TokenTypes.IDENTIFICADOR)) {
    			VarNode varFound = lista.find(look.lexema, bloco);
    			if(varFound == null) {
    				error("variável do scanf não existe");
    			}
    			nextTk();
    			if(match(TokenTypes.FECHA_PARENTESES)) {
    				nextTk();
    				if(match(TokenTypes.PONTO_VIRGULA)) {
    					nextTk();
    					return;
    				}
    				else {
    					error("falta de ponto virgula");
    				}
    			}
    			else {
    				error("não fechou parenteses do scanf");
    			}
    		}
    		else {
    			error("scanf mal formado");
    		}
    	}
    	else {
    		error("scanf mal formado");
    	}
    }
    
}

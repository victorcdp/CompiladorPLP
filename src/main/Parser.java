package main;

//OQ FALTA
// fazer o comando de while realmente repetir as coisas
//arrays

import java.util.List;
import java.util.Scanner;


public class Parser {

    private final List<Token> tokens;
    private Token look = null;
    private int tokenIt = 0;
    private int bloco = 0;
    public static ListaVar lista = new ListaVar();
    public TokenTypes tipo;
    private int loopInicio = 0;
    private boolean isInLoop = false;
    private double loopIterator = 0;
    private double loopFinal = 0;
    

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
        	loopInicio = tokenIt;
            bloco++;
            nextTk();
            while (match(TokenTypes.INT_ID) || match(TokenTypes.FLOAT_ID) || match(TokenTypes.CHAR_ID)) {
                decl_var();
            }
            while (match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.WHILE_ID) || match(TokenTypes.DO_ID) || match(TokenTypes.IF_ID) || match(TokenTypes.FOR_ID) || match(TokenTypes.PRINTF) || match(TokenTypes.SCANF)/*|| match(ELSE_ID)*/) {
                comando();
            }
            if (!match(TokenTypes.FECHA_CHAVE)) {
                error("Bloco mal formado");
            }
            while(isInLoop) {
            	loopIterator++;
            	tokenIt = loopInicio + 1;
            	look = tokens.get(tokenIt);
            	if(loopIterator == loopFinal) {
            		isInLoop = false;
            	}
            	comando();
            }
            lista.removerBloco(bloco);
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
        }
        else if(match(TokenTypes.PRINTF)) {
        	print();
        }
        else if(match(TokenTypes.SCANF)) {
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
    				    Scanner scanner = new Scanner(System.in);
    					String input = scanner.nextLine();
    					varFound.setValor(input);
    					nextTk();
    					scanner.close();
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
    
    public void print() {
    	String var = "";
    	nextTk();
    	if(match(TokenTypes.ABRE_PARENTESES)) {
    		nextTk();
    		if(match(TokenTypes.ASPAS)) {
    			nextTk();
    			while(!match(TokenTypes.ASPAS)) {
        			var = var + " " + look.lexema;
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
    			var = lista.find(look.lexema, bloco).getString();
    			nextTk();
    		}
    		if(match(TokenTypes.FECHA_PARENTESES)) {
    	    	System.out.println(var);
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
    
    public void atribuicao(boolean pularPV){
        if(match(TokenTypes.IDENTIFICADOR)){
        	Token var = look;
            nextTk();
            if(match(TokenTypes.ATRIBUICAO)){
            	if (!atribuicaoSimples(var)) {
                	op_aritmetica();
            	}
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
    
    public boolean atribuicaoSimples(Token var) {
		VarNode varFound = lista.find(var.lexema, bloco);
    	if(lookAhead(1).tipo == TokenTypes.IDENTIFICADOR || lookAhead(1).tipo == TokenTypes.INT || lookAhead(1).tipo == TokenTypes.FLOAT) {
    		if(lookAhead(2).tipo == TokenTypes.PONTO_VIRGULA) {
    			nextTk();
    			varFound.valorNumerico = Double.parseDouble(look.lexema);
    			nextTk();
    			return true;
    		}
    		else if(lookAhead(2).tipo == TokenTypes.SOMA || lookAhead(2).tipo == TokenTypes.SUBTRACAO) {
    			if(lookAhead(3).tipo == TokenTypes.IDENTIFICADOR || lookAhead(3).tipo == TokenTypes.INT || lookAhead(3).tipo == TokenTypes.FLOAT) {
    				nextTk();
    				VarNode val1 = lista.find(look.lexema, bloco);
    				nextTk();
    				if(match(TokenTypes.SOMA)) {
    					nextTk();
    					if(look.tipo == TokenTypes.IDENTIFICADOR) {
    						VarNode val2 = lista.find(look.lexema, bloco);
    		    			varFound.valorNumerico = val1.valorNumerico + val2.valorNumerico;
    		    			nextTk();
    		    			return true;
    					}
    					else if(look.tipo == TokenTypes.INT || look.tipo == TokenTypes.FLOAT) {
    						double val2 = Double.parseDouble(look.lexema);
    		    			varFound.valorNumerico = val1.valorNumerico + val2;
    		    			nextTk();
    		    			return true;
    					}
    				}
    				else if(match(TokenTypes.SUBTRACAO)) {
    					nextTk();
    					if(look.tipo == TokenTypes.IDENTIFICADOR) {
    						VarNode val2 = lista.find(look.lexema, bloco);
    		    			varFound.valorNumerico = val1.valorNumerico - val2.valorNumerico;
    		    			nextTk();
    		    			return true;
    					}
    					else if(look.tipo == TokenTypes.INT || look.tipo == TokenTypes.FLOAT) {
    						double val2 = Double.parseDouble(look.lexema);
    		    			varFound.valorNumerico = val1.valorNumerico - val2;
    		    			nextTk();
    		    			return true;
    					}
    				}
    			}
    			else if(lookAhead(2).tipo == TokenTypes.SOMA && lookAhead(3).tipo == TokenTypes.SOMA) {
        			nextTk();
        			varFound.valorNumerico = varFound.valorNumerico++;
    			}
    			else if(lookAhead(2).tipo == TokenTypes.SUBTRACAO && lookAhead(3).tipo == TokenTypes.SUBTRACAO) {
    				nextTk();
        			varFound.valorNumerico = varFound.valorNumerico--;
    			}
				nextTk();
    			return true;
    		}
    		else if(lookAhead(2).tipo == TokenTypes.DIVISAO || lookAhead(2).tipo == TokenTypes.MULTIPLICACAO) {
    			if(lookAhead(3).tipo == TokenTypes.IDENTIFICADOR || lookAhead(3).tipo == TokenTypes.INT || lookAhead(3).tipo == TokenTypes.FLOAT) {
    				nextTk();
    				VarNode val1 = lista.find(look.lexema, bloco);
    				nextTk();
    				if(match(TokenTypes.DIVISAO)) {
    					nextTk();
    					if(look.tipo == TokenTypes.IDENTIFICADOR) {
    						VarNode val2 = lista.find(look.lexema, bloco);
    		    			varFound.valorNumerico = val1.valorNumerico / val2.valorNumerico;
    		    			nextTk();
    		    			return true;
    					}
    					else if(look.tipo == TokenTypes.INT || look.tipo == TokenTypes.FLOAT) {
    						double val2 = Double.parseDouble(look.lexema);
    		    			varFound.valorNumerico = val1.valorNumerico / val2;
    		    			nextTk();
    		    			return true;
    					}
    				}
    				else if(match(TokenTypes.MULTIPLICACAO)) {
    					nextTk();
    					if(look.tipo == TokenTypes.IDENTIFICADOR) {
    						VarNode val2 = lista.find(look.lexema, bloco);
    		    			varFound.valorNumerico = val1.valorNumerico * val2.valorNumerico;
    		    			nextTk();
    		    			return true;
    					}
    					else if(look.tipo == TokenTypes.INT || look.tipo == TokenTypes.FLOAT) {
    						double val2 = Double.parseDouble(look.lexema);
    		    			varFound.valorNumerico = val1.valorNumerico * val2;
    		    			nextTk();
    		    			return true;
    					}
    				}
    			}
    		}
    	}
    	return false;
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
            	Token forInicio = look;
                atribuicao(false);
                loopIterator = lista.find(forInicio.lexema, bloco).valorNumerico;
                expr_relacional_simples();
                if(match(TokenTypes.PONTO_VIRGULA)) {
                	nextTk();
                	atribuicao(true);
                	if(match(TokenTypes.FECHA_PARENTESES)) {
                		nextTk();
                		isInLoop = true;
                		bloco();
                		nextTk();
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
    
    public void expr_relacional_simples() {
    	nextTk();
    	if(match(TokenTypes.MENOR)) {
    		nextTk();
    		loopFinal = Double.parseDouble(look.lexema) - 1;
    		nextTk();
    	}
    	else if(match(TokenTypes.MENOR_IGUAL)){
    		nextTk();
    		loopFinal = Double.parseDouble(look.lexema);
    		nextTk();
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
}

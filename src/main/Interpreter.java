package main;

import java.util.List;
import java.util.Scanner;

public class Interpreter {

    private final List<Token> tokens;
    private Token look = null;
    private int tokenIt = 0;
    private int bloco = 0;
    public static ListaVar lista = new ListaVar();
    public TokenTypes tipo;

    Interpreter(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void nextTk() {
        tokenIt += 1;
        look = tokens.get(tokenIt);
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
            while (match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.WHILE_ID) || match(TokenTypes.DO_ID) || match(TokenTypes.IF_ID) || match(TokenTypes.FOR_ID) || match(TokenTypes.PRINTF) || match(TokenTypes.SCANF) || match(TokenTypes.STRLEN) || match(TokenTypes.STRCAT)) {
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
        String id = look.lexema;
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
            	
            	if(match(TokenTypes.INT) || match(TokenTypes.IDENTIFICADOR)) {
                    lista.find(id, bloco).arraySize = Integer.parseInt(look.lexema);
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
        } else if(match(TokenTypes.STRLEN)) {
            strlen(true);
        } else if(match(TokenTypes.STRCAT)) {
            strcat(true);
        }
        else if (match(TokenTypes.IF_ID)) {
            nextTk();
            if (match(TokenTypes.ABRE_PARENTESES)) {
                boolean ifRun = expr_relacional();
                if (match(TokenTypes.FECHA_PARENTESES)) {
                    nextTk();
                    if (ifRun) {
                        comando();
                    }
                    else {
                        skipComando();
                    }
                    nextTk();
                    if(match(TokenTypes.ELSE_ID)){
                        nextTk();
                        if (!ifRun) {
                            comando();
                        }
                        else {
                            skipComando();
                        }
                    }
                } else {
                    error("Não fechou parenteses na condição do IF");
                }
            } else {
                error("Não abriu parenteses na condição do IF");
            }
        }
    }

    private void skipComando(){
        int chave = 1;
        while (chave != 0){
            nextTk();
            if (match(TokenTypes.ABRE_CHAVE)){chave++;}
            if (match(TokenTypes.FECHA_CHAVE)){chave--;}
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
        	Token var = look;
            double valor;
            String valorString = "";
            nextTk();
            if(match(TokenTypes.ABRE_COLCHETE)){
                nextTk();
                if(match(TokenTypes.FECHA_COLCHETE)){
                    nextTk();
                    if(match(TokenTypes.ATRIBUICAO)){
                        nextTk();
                        if(match(TokenTypes.ASPAS)){
                            nextTk();
                            while(!match(TokenTypes.ASPAS)){
                                valorString = valorString + " " + look.lexema;
                                nextTk();
                            }
                            nextTk();
                            if(match(TokenTypes.PONTO_VIRGULA)){
                                nextTk();
                                lista.find(var.lexema, bloco).setValor(valorString);
                                return;
                            }
                        }
                    }
                }
            }
            if(match(TokenTypes.ATRIBUICAO)){
                	valor = op_aritmetica();
                    lista.find(var.lexema, bloco).valorNumerico = valor;
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
        int tokenPosition, finalTokenPosition = 0;
        if(match(TokenTypes.WHILE_ID)){
            nextTk();
            if(match(TokenTypes.ABRE_PARENTESES)){
                tokenPosition = tokenIt;

                while(expr_relacional()){
                    if(match(TokenTypes.FECHA_PARENTESES)){
                        finalTokenPosition = tokenIt;
                        nextTk();
                        comando();
                        nextTk();
                        tokenIt = tokenPosition;
                        look = tokens.get(tokenIt);
                    }
                }
                nextTk();
                skipComando();
                nextTk();
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
        int tokenPosition, finalTokenPosition = 0;

    	if(match(TokenTypes.FOR_ID)){
            nextTk();
            if(match(TokenTypes.ABRE_PARENTESES)){
            	nextTk();
                atribuicao(false);
                tokenPosition = tokenIt - 1;
                while(expr_relacional()) {
                    if(match(TokenTypes.PONTO_VIRGULA)) {
                        nextTk();
                        atribuicao(true);
                        if(match(TokenTypes.FECHA_PARENTESES)) {
                            finalTokenPosition = tokenIt;
                            nextTk();
                            bloco();
                        }
                    }
                    tokenIt = tokenPosition;
                    look = tokens.get(tokenIt);
                }
                tokenIt = finalTokenPosition + 1;
                look = tokens.get(tokenIt);
                skipComando();
                nextTk();
            }
            else{
                error("parentese da condição do for não foi aberto");
            }
        }
    }

    public boolean expr_relacional() {
        double op_ari1 = op_aritmetica();
        TokenTypes tipo = op_relacional();
        double op_ari2 = op_aritmetica();

        switch(tipo){
            case MENOR_IGUAL:
                return (op_ari1 <= op_ari2);
            case MENOR:
                return (op_ari1 < op_ari2);
            case MAIOR_IGUAL:
                return (op_ari1 >= op_ari2);
            case MAIOR:
                return (op_ari1 > op_ari2);
            case IGUAL:
                return (op_ari1 == op_ari2);
            case DIFERENTE:
                return (op_ari1 != op_ari2);
            default:
                return false;
        }

    }
    
    public TokenTypes op_relacional(){
        if(!(match(TokenTypes.MENOR_IGUAL) || match(TokenTypes.MENOR) || match(TokenTypes.MAIOR_IGUAL) || match(TokenTypes.MAIOR) || match(TokenTypes.IGUAL) || match(TokenTypes.DIFERENTE))){
            error("operador relacional não existente.");
        }
        return look.tipo;
    }
    
    public double op_aritmetica() {
        double result;
        double termo_value = termo();
        result = somaOuSubtracao(termo_value);
        return result;
    }

    public double termo() {
        double result;
        double fator_value = fator();
        result = multiplicacaoOuDivisao(fator_value);
        return result;
    }
    
    public double fator(){
        double valor = 0;
        nextTk();
        if(match(TokenTypes.STRLEN)) {
            valor = strlen(false);
        }
        else if(match(TokenTypes.IDENTIFICADOR) || match(TokenTypes.INT) || match(TokenTypes.FLOAT) || match(TokenTypes.CHAR)){
            if (match(TokenTypes.IDENTIFICADOR)){
                valor = lista.find(look.lexema, bloco).valorNumerico;
            }
            else {
                valor = Double.parseDouble(look.lexema);
            }
            nextTk();
            return valor;
        }
        else if(match(TokenTypes.ABRE_PARENTESES)){
            valor = op_aritmetica();
            if(match(TokenTypes.FECHA_PARENTESES)){
                nextTk();
                return valor;
            }
            else{
                error("não fechou parenteses na expressão aritmetica");
            }
        }
        else if(match(TokenTypes.MULTIPLICACAO) || match(TokenTypes.DIVISAO) || match(TokenTypes.SOMA) || match(TokenTypes.SUBTRACAO) || match(TokenTypes.PONTO_VIRGULA)){
            error("operador inválido na expressão aritmetica");
        }
        return valor;
    }

    public double multiplicacaoOuDivisao(double first){
        if(match(TokenTypes.MULTIPLICACAO) || match(TokenTypes.DIVISAO)){
            TokenTypes tipo = look.tipo;
            double second = fator();
            double result = 0;
            if (tipo == TokenTypes.MULTIPLICACAO) {
                result = first * second;
            } else {
                result = first / second;
            }
            return multiplicacaoOuDivisao(result);            
        }
        return first;
    }

    public double somaOuSubtracao(double first){
        if(match(TokenTypes.SOMA) || match(TokenTypes.SUBTRACAO)){
            TokenTypes tipo = look.tipo;
            double second = fator();
            double result = 0;
            if (tipo == TokenTypes.SOMA) {
                result = first + second;
            } else {
                result = first - second;
            }
            return somaOuSubtracao(result);     
        }
        return first;
    }
    
    public static void printLista(){
        System.out.println(lista.toString());
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
            } else if(match(TokenTypes.STRLEN)) {
                var = var + strlen(false);
                //nextTk();

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
    			}
    		}
    	}
    }

    public double strlen(boolean pv) {
        double sizeValue = 0;
        nextTk();
        if(match(TokenTypes.ABRE_PARENTESES)) {
            nextTk();
            if(match(TokenTypes.IDENTIFICADOR)) {
                sizeValue = lista.find(look.lexema, bloco).getArraySize();
                nextTk();
                if (match(TokenTypes.FECHA_PARENTESES)){
                    nextTk();
                    if (pv) {
                        if (match(TokenTypes.PONTO_VIRGULA)){
                            nextTk();
                            return sizeValue;
                        } else {
                            error("falta de ponto virgula");
                        }
                    } else {
                        //nextTk();
                        return sizeValue;
                    }
                }
                else {
                    error("não fechou parenteses do printf");
                }
            }
        }
        else {
            error("não abriu parenteses no print");
        }
        return sizeValue;
    }

    public String strcat(boolean pv) {
        String str1 = "", str2 = "", target = "";
        nextTk();
        if(match(TokenTypes.ABRE_PARENTESES)){
            nextTk();
            if(match(TokenTypes.IDENTIFICADOR)){
                str1 = lista.find(look.lexema, bloco).getString();
                target = look.lexema;
                nextTk();
                if(match(TokenTypes.VIRGULA)){
                    nextTk();
                    if(match(TokenTypes.IDENTIFICADOR)){
                        str2 = lista.find(look.lexema, bloco).getString();
                        nextTk();
                        if(match(TokenTypes.FECHA_PARENTESES)){
                            nextTk();
                            if(pv){
                                if(match(TokenTypes.PONTO_VIRGULA)){
                                    nextTk();
                                    lista.find(target, bloco).setValor(str1 + str2);
                                    return str1 + str2;
                                }
                                else {
                                    error("falta de ponto virgula");
                                }
                            }
                            else {
                                lista.find(target, bloco).setValor(str1 + str2);
                                return str1 + str2;
                            }
                        }
                        else {
                            error("não fechou parenteses do printf");
                        }
                    }
                }
            }
        }
        else {
            error("não abriu parenteses no print");
        }
        return str1 + str2;
    }

}
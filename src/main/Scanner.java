package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.lang.Integer.parseInt;

class Scanner {
	
	

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int comeco = 0;
    private int atual = 0;
    private int linha = 1;
    private int coluna = 0;
    private static final Map<String, TokenTypes> PALRESERVADAS;

    static {
        PALRESERVADAS = new HashMap<>();
        PALRESERVADAS.put("main", TokenTypes.MAIN_ID);
        PALRESERVADAS.put("if", TokenTypes.IF_ID);
        PALRESERVADAS.put("else", TokenTypes.ELSE_ID);
        PALRESERVADAS.put("while", TokenTypes.WHILE_ID);
        PALRESERVADAS.put("do", TokenTypes.DO_ID);
        PALRESERVADAS.put("for", TokenTypes.FOR_ID);
        PALRESERVADAS.put("int", TokenTypes.INT_ID);
        PALRESERVADAS.put("float", TokenTypes.FLOAT_ID);
        PALRESERVADAS.put("char", TokenTypes.CHAR_ID);
        PALRESERVADAS.put("printf", TokenTypes.PRINTF);
        PALRESERVADAS.put("scanf", TokenTypes.SCANF);
        PALRESERVADAS.put("strlen", TokenTypes.STRLEN);
        PALRESERVADAS.put("strcat", TokenTypes.STRCAT);

    }
    
    

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!noFinal()) {
            comeco = atual;
            scanToken();
        }
        tokens.add(new Token(TokenTypes.EOF, "", null, linha, coluna));
        return tokens;
    }

    private boolean noFinal() {
        return atual >= source.length();
    }

    private char advance() {
        atual++;
        coluna++;
        return source.charAt(atual - 1);
    }

    private void addToken(TokenTypes type) {
        addToken(type, null);
    }

    private void addToken(TokenTypes tipo, Object literal) {
        String texto = source.substring(comeco, atual);
        tokens.add(new Token(tipo, texto, literal, linha, coluna));
        //System.out.println("Token Adicionado:" + tipo + " " + linha + " " + coluna + " " + literal); // só pra teste
    }

    private void addTokenChar(TokenTypes tipo, Object literal) {
        String texto = source.substring(comeco + 1, atual - 1);
        tokens.add(new Token(tipo, texto, literal, linha, coluna));
        //System.out.println("Token Adicionado:" + tipo + " " + linha + " " + coluna); // só pra teste
    }

    private boolean match(char expected) {
        if (noFinal()) {
            return false;
        }
        if (source.charAt(atual) != expected) {
            return false;
        }

        atual++;
        coluna++;
        return true;
    }

    private char lookAhead() {
        if (noFinal()) {
            return '\0';
        }
        return source.charAt(atual);
    }

    private char lookAheadNext() {
        if (atual + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(atual + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlpha(char c) {
        return isLetter(c) || c == '_';
    }

    private void identifier() {
        while (isAlphaNumeric(lookAhead())) {
            advance();
        }

        String text = source.substring(comeco, atual);

        TokenTypes type = PALRESERVADAS.get(text);
        if (type == null) {
            type = TokenTypes.IDENTIFICADOR;
        }
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
        	case '"':
        		addToken(TokenTypes.ASPAS);
        		break;
        	case '[':
        		addToken(TokenTypes.ABRE_COLCHETE);
        		break;
        	case ']':
        		addToken(TokenTypes.FECHA_COLCHETE);
        		break;
            case '(':
                addToken(TokenTypes.ABRE_PARENTESES);
                break;
            case ')':
                addToken(TokenTypes.FECHA_PARENTESES);
                break;
            case '{':
                addToken(TokenTypes.ABRE_CHAVE);
                break;
            case '}':
                addToken(TokenTypes.FECHA_CHAVE);
                break;
            case ',':
                addToken(TokenTypes.VIRGULA);
                break;
            case '-':
                addToken(TokenTypes.SUBTRACAO);
                break;
            case '+':
                addToken(TokenTypes.SOMA);
                break;
            case ';':
                addToken(TokenTypes.PONTO_VIRGULA);
                break;
            case '*':
                addToken(TokenTypes.MULTIPLICACAO);
                break;
            case '=':
                addToken(match('=') ? TokenTypes.IGUAL : TokenTypes.ATRIBUICAO);
                break;
            case '<':
                addToken(match('=') ? TokenTypes.MENOR_IGUAL : TokenTypes.MENOR);
                break;
            case '>':
                addToken(match('=') ? TokenTypes.MAIOR_IGUAL : TokenTypes.MAIOR);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenTypes.AND);
                } else {
                    System.out.println("ERRO: (‘&’) não seguida de ‘&’ na linha: " + linha + " e coluna: " + coluna);
                    System.exit(0);
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenTypes.OR);
                } else {
                    System.out.println("ERRO: (‘|’) não seguida de ‘|’ na linha: " + linha + " e coluna: " + coluna);
                    System.exit(0);
                }
                break;
            case '!':
                if (match('=')) {
                    addToken(TokenTypes.DIFERENTE);
                } else {
                    System.out.println("ERRO: Exclamação (‘!’) não seguida de ‘=’ na linha: " + linha + " e coluna: " + coluna);
                    System.exit(0);
                }
                break;
            case '/':
                if (match('/')) {
                    while (lookAhead() != '\n' && !noFinal()) {
                        advance();
                    }
                } else if (match('*')) {
                    int colunaErro = coluna;
                    int start = linha;
                    while (lookAheadNext() != '/' && !noFinal()) {
                        if (lookAhead() == '\n') {
                            linha++;
                            coluna = 0;
                        }
                        advance();
                    }
                    if (noFinal()) {
                        System.out.println("ERRO: Arquivo acabou com um comentário aberto na linha: " + start + " e coluna: " + colunaErro);
                        System.exit(0);
                    }
                    if (lookAhead() == '*' && lookAheadNext() == '/') {
                        advance();
                        advance();
                    }
                } else {
                    addToken(TokenTypes.DIVISAO);
                }
                break;
            case ' ':
                coluna += 0.5;
                break;
            case '\t':
                coluna += 4;
                break;
            case '\r':
            case '\n':
                linha++;
                coluna = 0;
                break;
            case '\'':
                character(source.charAt(comeco + 1));
                break;
            default:
                if (isDigit(c) || c == '.') {
                    number(c);
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.out.println("ERRO: Caracter Inválido(" + c + ") na linha: " + linha + " e coluna: " + coluna);
                    System.exit(0);
                }
                break;
        }
    }

    private void number(char c) {
        while (isDigit(lookAhead()) && c != '.') {
            advance();
        }

        // float que começa com '.'
        if (c == '.') {
            while (isDigit(lookAhead())) {
                advance();

            }
            addToken(TokenTypes.FLOAT, Double.parseDouble(source.substring(comeco, atual)));
            return;
        }

        // float completo
        if ((lookAhead() == '.' && isDigit(lookAheadNext()))) {
            advance();

            while (isDigit(lookAhead())) {
                advance();
            }
            addToken(TokenTypes.FLOAT, Double.parseDouble(source.substring(comeco, atual)));
        } // float mal formado
        else if (lookAhead() == '.' && !isDigit(lookAheadNext())) {
            System.out.println("ERRO!: Float mal formado na linha: " + linha + " e coluna: " + coluna);
            advance();
            System.exit(0);
        } // inteiro
        else {
            addToken(TokenTypes.INT, parseInt(source.substring(comeco, atual)));
        }
    }

    private void character(char c) {
        if (isDigit(lookAhead()) || isLetter(lookAhead()) && lookAheadNext() == '\'') {
            advance();
            advance();
            addTokenChar(TokenTypes.CHAR, c);
        } else {
            System.out.println("Erro! Char mal formado na linha: " + linha + " e coluna: " + coluna);
            System.exit(0);
        }
    }
}

package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



public class Main {
    
    
    public static void main(String[] args) throws IOException {
        //runFile("test.txt");
        if(args.length > 0){
            runFile(args[0]);
            System.out.println("Compilou com sucesso.");
        }
        else{
            System.out.println("Não enviou um PATH válido");
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        Scanner sc = new Scanner(source);
        List<Token> tokens = sc.scanTokens();

        // função pra testar e ver todos os tokens
        /*for (Token token : tokens) {
            System.out.println(token);
        }*/
        
        Parser ps = new Parser(tokens);
        ps.programa();
        Interpreter itp = new Interpreter(tokens);
        itp.programa();
    }
}

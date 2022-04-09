package main;

public class ListaVar {

    VarNode inicio;
    VarNode fim;

    public ListaVar() {
        inicio = null;
        fim = null;
    }

    public void add(VarNode novo) {
        novo.next = inicio;
        inicio = novo;

        if (fim == null) {
            fim = novo;
        }
    }

    public boolean existe(VarNode no) {
        VarNode iterador = inicio;

        do {
            if (iterador.literal.equals(no.literal) && iterador.tipo.equals(no.tipo) && iterador.bloco == no.bloco) {
                return true;
            }

            iterador = iterador.next;
        } while (iterador != null);

        return false;
    }
    
    public VarNode find(Object literal, int bloco) {
    	VarNode iterador = inicio;

        do {
            if (iterador.literal.equals(literal) &&  iterador.bloco <= bloco) {
                return iterador;
            }

            iterador = iterador.next;
        } while (iterador != null);

        return null;
    }

    @Override
    public String toString() {

        String lista;

        VarNode iterador = inicio;

        lista = "";
        while (iterador != null) {
            lista = lista + " " + iterador.tipo + " " + iterador.literal + " " + iterador.bloco + " " + iterador.valorNumerico + " " + iterador.valorString + " \n";
            iterador = iterador.next;
        }
        return lista;
    }

    public void removerBloco(int bloco) {
        VarNode iterador = inicio;
        boolean deletou = false;
        while (deletou == false && iterador != null) {
            deletou = true;
            if (iterador.bloco == bloco) {
                remover(iterador);
                deletou = false;
                iterador = inicio;
                continue;
            }
            iterador = iterador.next;
        }
    }

    public void remover(VarNode no) {
        VarNode anterior = null;
        VarNode iterador = inicio;
        do {
            if (iterador.bloco == no.bloco) {
                if (anterior != null) {
                    anterior.next = iterador.next;
                } else {
                    inicio = iterador.next;
                }
                iterador.next = null;
                return;
            }
            anterior = iterador;
            iterador = iterador.next;
        } while (iterador != null);
    }
}

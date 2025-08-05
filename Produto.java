import java.util.Scanner;

public class Produto {
    String nome;
    double preco;
    int quantidade;

    public Produto(String nome, double preco, int quantidade) {
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public void adicionarProdutos(int quantidade) {
        this.quantidade += quantidade;
        System.out.println("Dados atualizados: " + toString());
    }

    public void removerProdutos(int quantidade) {
        this.quantidade = this.quantidade - quantidade < 0 ? 0 : this.quantidade - quantidade;
        System.out.println("Dados atualizados: " + toString());
    }

    @Override
    public String toString() {
        return """
                Nome: %s, Preço: R$%.2f, Quantidade: %d unidades, Valor em estoque: R$%.2f """
                .formatted(nome, preco, quantidade, quantidade*preco);
    }
}

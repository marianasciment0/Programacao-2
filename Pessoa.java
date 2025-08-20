package ProjetoProgramacao2Maria;

public class Pessoa {
    String nome;
    String tipo;
    double renda;
    double despesas;
    int funcionarios;
    double imposto = 0.00d;

    public Pessoa(String nome, String tipo, double renda, double despesas, int funcionarios) {
        this.nome = nome;
        this.tipo = tipo;
        this.renda = renda;
        this.despesas = despesas;
        this.funcionarios = funcionarios;
    }
}

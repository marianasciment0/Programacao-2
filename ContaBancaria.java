public class Conta {
    String nome;
    int number;
    double valor_inicial;
    double maximo_saque;

    public Conta(String nome, int number, double valor_inicial, double maximo_saque) {
        this.nome = nome;
        this.number = number;
        this.valor_inicial = valor_inicial;
        this.maximo_saque = maximo_saque;
    }

    public void sacar(double valor) {
        if (valor > this.maximo_saque) {
            System.out.println("Saque maior que o limite.");
        } else if (valor > valor_inicial) {
            System.out.println("Não há crédito o suficiente.");
        } else {
            System.out.println("Saque realizado com sucesso!" +
                    "\nValor sacado: " + valor +
                    "\nValor restante na conta: " + (valor_inicial - valor));
        }
    }
}

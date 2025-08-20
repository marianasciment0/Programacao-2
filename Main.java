package ProjetoProgramacao2Maria;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        calculadoraDeImpostos(scanner);
    }

    public static void calculadoraDeImpostos(Scanner s) {
        System.out.print("Quantidade de pagadores de impostos: ");
        int quantidade = Integer.parseInt(s.nextLine().trim());

        ArrayList<Pessoa> lista = new ArrayList<>();

        for (int i = 0; i < quantidade; i++) {
            System.out.printf("Pagador #%d:%n", (i + 1));

            System.out.print("Pessoa física ou jurídica (f/j)? ");
            String tipo = s.nextLine().trim().toLowerCase();

            System.out.print("Nome: ");
            String nome = s.nextLine().trim();

            System.out.print("Renda anual: ");
            double renda = Double.parseDouble(s.nextLine().trim());

            switch (tipo) {
                case "f" -> {
                    System.out.print("Despesas com saúde: ");
                    double despesas = Double.parseDouble(s.nextLine().trim());
                    lista.add(new Pessoa(nome, tipo, renda, despesas, 0));
                }
                case "j" -> {
                    System.out.print("Número de funcionarios: ");
                    int funcionarios = Integer.parseInt(s.nextLine().trim());
                    lista.add(new Pessoa(nome, tipo, renda, 0, funcionarios));
                }
                default -> {
                    System.out.println("Tipo não suportado.");
                    i--;
                }
            }
        }

        System.out.println("\nIMPOSTOS PAGOS");
        for (Pessoa p : lista) {
            if (p.tipo.equalsIgnoreCase("f")) {
                double imposto = p.renda >= 20000.00d ? 0.25 : p.renda <= 0.00 ? 0.00 : 0.15;
                p.imposto = (p.renda * imposto) - (p.despesas <= 0.00 ? 0.00 : p.despesas * (0.5));
            } else {
                p.imposto = p.funcionarios > 10 ? p.renda * (0.14d) : p.renda * (0.16d);
            }
            System.out.println(p.nome + " R$ " + (p.imposto > 0 ? p.imposto : "'Imposto zerado ou negativo'"));
        }
    }
}
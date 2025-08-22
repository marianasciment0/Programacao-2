import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Número da conta: ");
        int numero = Integer.parseInt(scanner.nextLine());

        System.out.println("Nome do proprietário da conta: ");
        String nome = scanner.nextLine();

        System.out.println("Valor inicial: ");
        double vInicial = Double.parseDouble(scanner.nextLine());

        System.out.println("Máximo de saque da conta: ");
        double maxSaque = Double.parseDouble(scanner.nextLine());

        Conta conta = new Conta(nome, numero, vInicial, maxSaque);

        System.out.println("Valor a sacar: ");
        double vSaque = Double.parseDouble(scanner.nextLine());

        conta.sacar(vSaque);
    }
}

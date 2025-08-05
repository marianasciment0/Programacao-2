import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Produto prod = novoProduto(scanner);
        System.out.println(prod.toString());

        adicionar(scanner, prod);
        remover(scanner, prod);

        System.out.println("Estado final do produto: " + prod);

        scanner.close();
    }

    public static Produto novoProduto(Scanner scanner) {
        System.out.print("Nome do produto: ");
        String nomeProduto = scanner.nextLine();

        System.out.print("Preço do produto: ");
        String precoProduto = scanner.nextLine();
        double preco = Double.parseDouble(precoProduto);

        System.out.print("Quantidade do produto: ");
        String quantProduto = scanner.nextLine();
        int quantidade = Integer.parseInt(quantProduto);

        return new Produto(nomeProduto, preco, quantidade);
    }

    public static void adicionar(Scanner scanner, Produto p) {
        System.out.print("Digite o número de produtos a serem adicionados: ");
        String strQuant = scanner.nextLine();
        int quant = Integer.parseInt(strQuant);
        p.adicionarProdutos(quant);
    }

    public static void remover(Scanner scanner, Produto p) {
        System.out.print("Digite o número de produtos a serem removidos: ");
        String strQuant = scanner.nextLine();
        int quant = Integer.parseInt(strQuant);
        p.removerProdutos(quant);
    }
}

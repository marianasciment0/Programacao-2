import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Main {
    static DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Qual quarto deseja? ");
        int quartoDesejado = Integer.parseInt(scanner.nextLine());

        String dataEntrada = validarData("entrada", scanner);
        String dataSaida = validarData("saida", scanner);

        reservar(quartoDesejado, dataEntrada, dataSaida);
    }

    public static String validarData(String tipo, Scanner scanner) {
        LocalDate data;

        while (true) {
            System.out.printf("Data de %s (dd/MM/yyyy): ", tipo);
            String entradaUsuario = scanner.nextLine();
            try {
                data = LocalDate.parse(entradaUsuario, formatador);
                return data.format(formatador);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida! Digite no formato dd/MM/yyyy.");
            }
        }
    }

    public static void reservar(int quarto, String entrada, String saida) {
        if (verificaDatas(entrada, saida)) {
            System.out.printf("%n%sReserva%s%n Quarto: %d%n Entrada: %s%n Saída: %s%n",
                    "-".repeat(8), "-".repeat(8), quarto, entrada, saida);
        } else {
            System.out.println("""
                    Não foi possível realizar sua reserva \uD83D\uDE15.\
                    Motivo: Data de saída menor ou igual à data de entrada\s""");
        }
    }

    public static boolean verificaDatas(String ent, String sai) {
        LocalDate dataEntrada = LocalDate.parse(ent, formatador);
        LocalDate dataSaida = LocalDate.parse(sai, formatador);

        long diferenca = ChronoUnit.DAYS.between(dataEntrada, dataSaida);

        return diferenca > 0;
    }
}
package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.CartaoPonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.TaxaServico;
import br.ufal.ic.p2.wepayu.models.Venda;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class Facade {
    private Map<String, Empregado> empregados = new HashMap<>();
    private int proximoId = 1;
    private static final String DATA_FILE = "sistema_data.xml";

    public Facade() {
        carregarDados();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
        validarNome(nome);
        validarEndereco(endereco);
        validarTipo(tipo);
        Double salarioDouble = validarSalarioString(salario);

        if (tipo.equals("comissionado")) {
            throw new Error("Tipo nao aplicavel.");
        }

        String id = gerarProximoId();
        Empregado emp = new Empregado(nome, endereco, tipo, salarioDouble);
        empregados.put(id, emp);
        salvarDados();

        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        validarNome(nome);
        validarEndereco(endereco);
        validarTipo(tipo);
        Double salarioDouble = validarSalarioString(salario);
        Double comissaoDouble = validarComissaoString(comissao);

        if (!tipo.equals("comissionado")) {
            throw new Error("Tipo nao aplicavel.");
        }

        String id = gerarProximoId();
        Empregado emp = new Empregado(nome, endereco, tipo, salarioDouble, comissaoDouble);
        empregados.put(id, emp);
        salvarDados();

        return id;
    }

    public String getEmpregadoPorNome(String nome, String indice) {
        if (nome == null || nome.isBlank()) {
            throw new Error("Nome nao pode ser nulo.");
        }

        Integer indiceInt;
        try {
            indiceInt = Integer.parseInt(indice);
        } catch (NumberFormatException e) {
            throw new Error("Indice deve ser numerico.");
        }

        if (indiceInt < 1) {
            throw new Error("Indice deve ser positivo.");
        }

        List<String> idsComNome = new ArrayList<>();
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            if (entry.getValue().getNome().equals(nome)) {
                idsComNome.add(entry.getKey());
            }
        }

        if (indiceInt > idsComNome.size()) {
            throw new Error("Nao ha empregado com esse nome.");
        }

        return idsComNome.get(indiceInt - 1);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);

        switch (atributo) {
            case "nome":
                return empregado.getNome();
            case "endereco":
                return empregado.getEndereco();
            case "tipo":
                return empregado.getTipo();
            case "salario":
                return formatarDouble(empregado.getSalario());
            case "sindicalizado":
                return Boolean.toString(empregado.getSindicalizado());
            case "comissao":
                if (!empregado.getTipo().equals("comissionado")) {
                    throw new Error("Empregado nao eh comissionado.");
                }
                return formatarDouble(empregado.getComissao());
            case "metodoPagamento":
                return empregado.getMetodoPagamento();
            case "banco":
                if (!"banco".equals(empregado.getMetodoPagamento())) {
                    throw new Error("Empregado nao recebe em banco.");
                }
                return empregado.getBanco();
            case "agencia":
                if (!"banco".equals(empregado.getMetodoPagamento())) {
                    throw new Error("Empregado nao recebe em banco.");
                }
                return empregado.getAgencia();
            case "contaCorrente":
                if (!"banco".equals(empregado.getMetodoPagamento())) {
                    throw new Error("Empregado nao recebe em banco.");
                }
                return empregado.getContaCorrente();
            case "idSindicato":
                if (!Boolean.TRUE.equals(empregado.getSindicalizado())) {
                    throw new Error("Empregado nao eh sindicalizado.");
                }
                return empregado.getIdSindicato();
            case "taxaSindical":
                if (!Boolean.TRUE.equals(empregado.getSindicalizado())) {
                    throw new Error("Empregado nao eh sindicalizado.");
                }
                return formatarDouble(empregado.getTaxaSindical());
            default:
                throw new Error("Atributo nao existe.");
        }
    }

    public void removerEmpregado(String emp) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        empregados.remove(emp);
        salvarDados();
    }

    private String gerarProximoId() {
        return String.valueOf(proximoId++);
    }

    private String formatarDouble(Double valor) {
        if (valor == null) return "0,00";
        return String.format("%.2f", valor).replace(".", ",");
    }

    public String lancaCartao(String emp, String data, String horas) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"horista".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh horista.");
        }

        Date dataDate = parseData(data);
        if (dataDate == null) {
            throw new Error("Data invalida.");
        }

        double horasDouble = validarHoras(horas);

        CartaoPonto cartao = new CartaoPonto(dataDate, horasDouble);
        empregado.getCartoesPonto().add(cartao);
        salvarDados();

        return "true";
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaHoras(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);
        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        Calendar calFinal = Calendar.getInstance();
        calFinal.setTime(dataFinalDate);
        calFinal.add(Calendar.DAY_OF_MONTH, 1);
        Date dataFinalExclusiva = calFinal.getTime();

        double horasNormais = 0;

        Map<String, Double> horasPorDia = new HashMap<>();

        for (CartaoPonto cartao : empregado.getCartoesPonto()) {
            Date dataCartao = cartao.getData();
            if (dataCartao.compareTo(dataInicialDate) >= 0 && dataCartao.compareTo(dataFinalExclusiva) < 0) {
                String dataKey = formatarData(dataCartao);
                horasPorDia.put(dataKey, horasPorDia.getOrDefault(dataKey, 0.0) + cartao.getHoras());
            }
        }

        for (Double horasDia : horasPorDia.values()) {
            horasNormais += Math.min(horasDia, 8);
        }

        return formatarHoras(horasNormais);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaHoras(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);
        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        Calendar calFinal = Calendar.getInstance();
        calFinal.setTime(dataFinalDate);
        calFinal.add(Calendar.DAY_OF_MONTH, 1);
        Date dataFinalExclusiva = calFinal.getTime();

        double horasExtras = 0;

        if (dataInicialDate.equals(dataFinalDate)) {
            return "0";
        }

        Map<String, Double> horasPorDia = new HashMap<>();

        for (CartaoPonto cartao : empregado.getCartoesPonto()) {
            Date dataCartao = cartao.getData();
            if (dataCartao.compareTo(dataInicialDate) >= 0 && dataCartao.compareTo(dataFinalExclusiva) < 0) {
                String dataKey = formatarData(dataCartao);
                horasPorDia.put(dataKey, horasPorDia.getOrDefault(dataKey, 0.0) + cartao.getHoras());
            }
        }

        for (Double horasDia : horasPorDia.values()) {
            horasExtras += Math.max(0, horasDia - 8);
        }

        return formatarHoras(horasExtras);
    }

    private String formatarData(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(data);
    }

    public String alteraEmpregado(String emp, String atributo, String valor) throws EmpregadoNaoExisteException {
        return alteraEmpregadoImpl(emp, atributo, valor, null, null, null, null);
    }

    public String alteraEmpregado(String emp, String atributo, String valor, String comissao) throws EmpregadoNaoExisteException {
        return alteraEmpregadoImpl(emp, atributo, valor, comissao, null, null, null);
    }

    public String alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws EmpregadoNaoExisteException {
        return alteraEmpregadoImpl(emp, atributo, valor, null, idSindicato, taxaSindical, null);
    }

    public String alteraEmpregado(String emp, String atributo, String valor, String banco, String agencia, String contaCorrente) throws EmpregadoNaoExisteException {
        return alteraEmpregadoImpl(emp, atributo, valor, null, null, null, new String[]{banco, agencia, contaCorrente});
    }

    private String alteraEmpregadoImpl(String emp, String atributo, String valor, String comissao,
                                       String idSindicato, String taxaSindical, String[] dadosBanco) throws EmpregadoNaoExisteException {

        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);

        if (comissao != null) {
            if (!"tipo".equals(atributo) || !"comissionado".equals(valor)) {
                throw new Error("Parametros invalidos.");
            }
            return alteraTipoComissao(empregado, comissao);
        } else if (idSindicato != null && taxaSindical != null) {
            if (!"sindicalizado".equals(atributo) || !"true".equals(valor)) {
                throw new Error("Parametros invalidos.");
            }
            return alteraSindicalizado(empregado, idSindicato, taxaSindical);
        } else if (dadosBanco != null) {
            if (!"metodoPagamento".equals(atributo) || !"banco".equals(valor)) {
                throw new Error("Parametros invalidos.");
            }
            return alteraMetodoPagamentoBanco(empregado, dadosBanco[0], dadosBanco[1], dadosBanco[2]);
        } else {
            return alteraAtributoSimples(empregado, atributo, valor);
        }
    }

    private String alteraMetodoPagamentoBanco(Empregado emp, String banco, String agencia, String contaCorrente) {
        if (banco == null || banco.isEmpty()) {
            throw new Error("Banco nao pode ser nulo.");
        }
        if (agencia == null || agencia.isEmpty()) {
            throw new Error("Agencia nao pode ser nulo.");
        }
        if (contaCorrente == null || contaCorrente.isEmpty()) {
            throw new Error("Conta corrente nao pode ser nulo.");
        }

        emp.setMetodoPagamento("banco");
        emp.setBanco(banco);
        emp.setAgencia(agencia);
        emp.setContaCorrente(contaCorrente);

        salvarDados();
        return "true";
    }

    private String alteraAtributoSimples(Empregado emp, String atributo, String valor) {
        switch (atributo) {
            case "nome":
                validarNome(valor);
                emp.setNome(valor);
                break;
            case "endereco":
                validarEndereco(valor);
                emp.setEndereco(valor);
                break;
            case "salario":
                Double salario = validarSalarioString(valor);
                emp.setSalario(salario);
                break;
            case "comissao":
                if (!"comissionado".equals(emp.getTipo())) {
                    throw new Error("Empregado nao eh comissionado.");
                }
                Double comissao = validarComissaoString(valor);
                emp.setComissao(comissao);
                break;
            case "sindicalizado":
                if ("true".equals(valor) || "false".equals(valor)) {
                    if ("false".equals(valor)) {
                        emp.setSindicalizado(false);
                        emp.setIdSindicato(null);
                        emp.setTaxaSindical(null);
                    } else {
                        throw new Error("Parametros insuficientes para sindicalizacao.");
                    }
                } else {
                    throw new Error("Valor deve ser true ou false.");
                }
                break;
            case "metodoPagamento":
                validarMetodoPagamento(valor);
                emp.setMetodoPagamento(valor);
                if (!"banco".equals(valor)) {
                    emp.setBanco(null);
                    emp.setAgencia(null);
                    emp.setContaCorrente(null);
                }
                break;
            case "tipo":
                validarTipo(valor);
                emp.setTipo(valor);
                if (!"comissionado".equals(valor)) {
                    emp.setComissao(null);
                }
                break;
            default:
                throw new Error("Atributo nao existe.");
        }
        salvarDados();
        return "true";
    }

    private String alteraTipoComissao(Empregado emp, String comissao) {
        Double comissaoDouble = validarComissaoString(comissao);
        emp.setTipo("comissionado");
        emp.setComissao(comissaoDouble);
        salvarDados();
        return "true";
    }

    private String alteraSindicalizado(Empregado emp, String idSindicato, String taxaSindical) {
        if (idSindicato == null || idSindicato.isEmpty()) {
            throw new Error("Identificacao do sindicato nao pode ser nula.");
        }

        if (taxaSindical == null || taxaSindical.isEmpty()) {
            throw new Error("Taxa sindical nao pode ser nula.");
        }

        for (Empregado e : empregados.values()) {
            if (e.getSindicalizado() && e.getIdSindicato() != null &&
                    e.getIdSindicato().equals(idSindicato) && !e.equals(emp)) {
                throw new Error("Ha outro empregado com esta identificacao de sindicato");
            }
        }

        Double taxaSindicalDouble = validarTaxaSindical(taxaSindical);
        emp.setSindicalizado(true);
        emp.setIdSindicato(idSindicato);
        emp.setTaxaSindical(taxaSindicalDouble);

        salvarDados();
        return "true";
    }

    private Double validarTaxaSindical(String taxa) {
        if (taxa == null || taxa.isEmpty()) {
            throw new Error("Taxa sindical nao pode ser nula.");
        }

        try {
            String taxaFormatada = taxa.replace(',', '.');
            double taxaDouble = Double.parseDouble(taxaFormatada);

            if (taxaDouble < 0) {
                throw new Error("Taxa sindical deve ser nao-negativa.");
            }

            return taxaDouble;
        } catch (NumberFormatException e) {
            throw new Error("Taxa sindical deve ser numerica.");
        }
    }

    private void validarMetodoPagamento(String metodo) {
        if (metodo == null || (!"emMaos".equals(metodo) && !"correios".equals(metodo) && !"banco".equals(metodo))) {
            throw new Error("Metodo de pagamento invalido.");
        }
    }

    private void validarDadosBancarios(String banco, String agencia, String contaCorrente) {
        if (banco == null || banco.isEmpty()) {
            throw new Error("Banco nao pode ser nulo.");
        }
        if (agencia == null || agencia.isEmpty()) {
            throw new Error("Agencia nao pode ser nulo.");
        }
        if (contaCorrente == null || contaCorrente.isEmpty()) {
            throw new Error("Conta corrente nao pode ser nulo.");
        }
    }

    private void validarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new Error("Tipo nao pode ser nulo");
        }

        List<String> opcoesValidas = Arrays.asList("horista", "assalariado", "comissionado");
        if (!opcoesValidas.contains(tipo)) {
            throw new Error("Tipo invalido.");
        }
    }

    public String lancaTaxaServico(String membro, String data, String valor) {
        if (membro == null || membro.isEmpty()) {
            throw new Error("Identificacao do membro nao pode ser nula.");
        }

        Empregado empregado = null;
        for (Empregado e : empregados.values()) {
            if (e.getSindicalizado() && e.getIdSindicato() != null &&
                    e.getIdSindicato().equals(membro)) {
                empregado = e;
                break;
            }
        }

        if (empregado == null) {
            throw new Error("Membro nao existe.");
        }

        Date dataDate = parseData(data);
        if (dataDate == null) {
            throw new Error("Data invalida.");
        }

        double valorDouble = validarValorTaxaServico(valor);

        TaxaServico taxa = new TaxaServico(dataDate, valorDouble);

        empregado.getTaxasServico().add(taxa);
        salvarDados();

        return "true";
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaTaxasServico(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);
        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.equals(dataFinalDate)) {
            return "0,00";
        }

        double totalTaxas = 0;
        for (TaxaServico taxa : empregado.getTaxasServico()) {
            Date dataTaxa = taxa.getData();
            if (!dataTaxa.before(dataInicialDate) && dataTaxa.before(dataFinalDate)) {
                totalTaxas += taxa.getValor();
            }
        }

        return formatarDouble(totalTaxas);
    }

    private void validarConsultaTaxasServico(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!Boolean.TRUE.equals(empregado.getSindicalizado())) {
            throw new Error("Empregado nao eh sindicalizado.");
        }

        if (parseData(dataInicial) == null) {
            throw new Error("Data inicial invalida.");
        }

        if (parseData(dataFinal) == null) {
            throw new Error("Data final invalida.");
        }

        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.after(dataFinalDate)) {
            throw new Error("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private double validarValorTaxaServico(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new Error("Valor deve ser positivo.");
        }

        try {
            String valorFormatado = valor.replace(',', '.');
            double valorDouble = Double.parseDouble(valorFormatado);

            if (valorDouble <= 0) {
                throw new Error("Valor deve ser positivo.");
            }

            return valorDouble;
        } catch (NumberFormatException e) {
            throw new Error("Valor deve ser numerica.");
        }
    }

    private void validarConsultaHoras(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"horista".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh horista.");
        }

        if (parseData(dataInicial) == null) {
            throw new Error("Data inicial invalida.");
        }

        if (parseData(dataFinal) == null) {
            throw new Error("Data final invalida.");
        }

        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.after(dataFinalDate)) {
            throw new Error("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private double validarHoras(String horas) {
        if (horas == null || horas.isEmpty()) {
            throw new Error("Horas devem ser positivas.");
        }

        try {
            String horasFormatada = horas.replace(',', '.');
            double horasDouble = Double.parseDouble(horasFormatada);

            if (horasDouble <= 0) {
                throw new Error("Horas devem ser positivas.");
            }

            return horasDouble;
        } catch (NumberFormatException e) {
            throw new Error("Horas devem ser numericas.");
        }
    }

    private Date parseData(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            return sdf.parse(data);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatarHoras(double horas) {
        if (horas == (int) horas) {
            return String.valueOf((int) horas);
        } else {
            return String.format("%.1f", horas).replace(".", ",");
        }
    }

    private void salvarDados() {
        try {
            Map<String, Object> dadosCompletos = new HashMap<>();
            dadosCompletos.put("empregados", empregados);
            dadosCompletos.put("proximoId", proximoId);

            try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
                    new FileOutputStream(DATA_FILE)))) {
                encoder.writeObject(dadosCompletos);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    private void carregarDados() {
        try {
            File dataFile = new File(DATA_FILE);
            if (dataFile.exists()) {
                try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
                        new FileInputStream(dataFile)))) {
                    Object obj = decoder.readObject();
                    if (obj instanceof Map) {
                        Map<String, Object> dadosCompletos = (Map<String, Object>) obj;
                        empregados = (Map<String, Empregado>) dadosCompletos.get("empregados");
                        proximoId = (Integer) dadosCompletos.get("proximoId");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            empregados = new HashMap<>();
            proximoId = 1;
        } catch (Exception e) {
            System.err.println("Erro ao decodificar dados: " + e.getMessage());
            empregados = new HashMap<>();
            proximoId = 1;
        }
    }

    public void zerarSistema() {
        empregados.clear();
        proximoId = 1;
        new File(DATA_FILE).delete();
        new File("empregados.xml").delete();
        new File("proximo_id.xml").delete();
    }

    public void encerrarSistema() {
        salvarDados();
    }

    private Double validarSalarioString(String salario) {
        if (salario == null || salario.isBlank()) {
            throw new Error("Salario nao pode ser nulo.");
        }

        if (!salario.matches("-?\\d+([.,]\\d{1,2})?")) {
            throw new Error("Salario deve ser numerico.");
        }

        String numeroFormatado = salario.replace(',', '.');
        double salarioDouble = Double.parseDouble(numeroFormatado);

        if (salarioDouble < 0) {
            throw new Error("Salario deve ser nao-negativo.");
        }

        return salarioDouble;
    }

    private Double validarComissaoString(String comissao) {
        if (comissao == null || comissao.isEmpty()) {
            throw new Error("Comissao nao pode ser nula.");
        }

        try {
            String numeroFormatado = comissao.replace(',', '.');
            double comissaoDouble = Double.parseDouble(numeroFormatado);

            if (comissaoDouble < 0) {
                throw new Error("Comissao deve ser nao-negativa.");
            }

            return comissaoDouble;
        } catch (NumberFormatException e) {
            throw new Error("Comissao deve ser numerica.");
        }
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new Error("Nome nao pode ser nulo.");
        }

        for (char c : nome.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                throw new Error("Nome deve conter apenas letras e espaços.");
            }
        }
    }

    private void validarEndereco(String endereco) {
        if (endereco == null || endereco.isBlank()) {
            throw new Error("Endereco nao pode ser nulo.");
        }
    }

    public String lancaVenda(String emp, String data, String valor) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"comissionado".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh comissionado.");
        }

        Date dataDate = parseData(data);
        if (dataDate == null) {
            throw new Error("Data invalida.");
        }

        double valorDouble = validarValorVenda(valor);

        Venda venda = new Venda(dataDate, valorDouble);
        empregado.getVendas().add(venda);
        salvarDados();

        return "true";
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaVendas(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);
        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.equals(dataFinalDate)) {
            return "0,00";
        }

        double totalVendas = 0;
        for (Venda venda : empregado.getVendas()) {
            Date dataVenda = venda.getData();
            if (!dataVenda.before(dataInicialDate) && dataVenda.before(dataFinalDate)) {
                totalVendas += venda.getValor();
            }
        }

        return formatarDouble(totalVendas);
    }

    private void validarConsultaVendas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"comissionado".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh comissionado.");
        }

        if (parseData(dataInicial) == null) {
            throw new Error("Data inicial invalida.");
        }

        if (parseData(dataFinal) == null) {
            throw new Error("Data final invalida.");
        }

        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.after(dataFinalDate)) {
            throw new Error("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private double validarValorVenda(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new Error("Valor deve ser positivo.");
        }

        try {
            String valorFormatado = valor.replace(',', '.');
            double valorDouble = Double.parseDouble(valorFormatado);

            if (valorDouble <= 0) {
                throw new Error("Valor deve ser positivo.");
            }

            return valorDouble;
        } catch (NumberFormatException e) {
            throw new Error("Valor deve ser numerico.");
        }
    }

    public String totalFolha(String data) {
        Date dataDate = parseData(data);
        if (dataDate == null) {
            throw new Error("Data invalida.");
        }

        double total = 0;
        for (Empregado emp : empregados.values()) {
            total += calcularPagamentoEmpregado(emp, dataDate);
        }

        if (total == 0) {
            return "0,00";
        }

        return formatarDouble(total);
    }

    public String rodaFolha(String data, String saida) {
        Date dataDate = parseData(data);
        if (dataDate == null) {
            throw new Error("Data invalida.");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(saida))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            writer.println("FOLHA DE PAGAMENTO DO DIA " + sdf.format(dataDate));
            writer.println("====================================");
            writer.println();
            writer.println("===============================================================================================================================");
            writer.println();

            List<String> linhasHoristas = new ArrayList<>();
            List<String> linhasAssalariados = new ArrayList<>();
            List<String> linhasComissionados = new ArrayList<>();

            double totalHoristas = 0;
            double totalAssalariados = 0;
            double totalComissionados = 0;
            double totalFolha = 0;

            for (Empregado emp : empregados.values()) {
                double pagamento = calcularPagamentoEmpregado(emp, dataDate);
                if (pagamento > 0) {
                    String linha = gerarLinhaFolha(emp, pagamento);

                    switch (emp.getTipo()) {
                        case "horista":
                            linhasHoristas.add(linha);
                            totalHoristas += pagamento;
                            break;
                        case "assalariado":
                            linhasAssalariados.add(linha);
                            totalAssalariados += pagamento;
                            break;
                        case "comissionado":
                            linhasComissionados.add(linha);
                            totalComissionados += pagamento;
                            break;
                    }
                    totalFolha += pagamento;
                }
            }

            if (!linhasHoristas.isEmpty()) {
                writer.println("===================== HORISTAS ================================================================================================");
                writer.println();
                Collections.sort(linhasHoristas);
                for (String linha : linhasHoristas) {
                    writer.println(linha);
                }
                writer.println();
            }

            if (!linhasAssalariados.isEmpty()) {
                writer.println("===================== ASSALARIADOS ============================================================================================");
                writer.println();
                Collections.sort(linhasAssalariados);
                for (String linha : linhasAssalariados) {
                    writer.println(linha);
                }
                writer.println();
            }

            if (!linhasComissionados.isEmpty()) {
                writer.println("===================== COMISSIONADOS ============================================================================================");
                writer.println();
                Collections.sort(linhasComissionados);
                for (String linha : linhasComissionados) {
                    writer.println(linha);
                }
                writer.println();
            }

            writer.println("===============================================================================================================================");
            writer.println();

            if (totalHoristas > 0) {
                writer.println("TOTAL HORISTAS: " + formatarDouble(totalHoristas));
            }
            if (totalAssalariados > 0) {
                writer.println("TOTAL ASSALARIADOS: " + formatarDouble(totalAssalariados));
            }
            if (totalComissionados > 0) {
                writer.println("TOTAL COMISSIONADOS: " + formatarDouble(totalComissionados));
            }

            writer.println();
            writer.println("TOTAL: " + formatarDouble(totalFolha));

        } catch (IOException e) {
            throw new Error("Erro ao escrever arquivo de folha: " + e.getMessage());
        }

        return "true";
    }

    private double calcularPagamentoEmpregado(Empregado emp, Date data) {
        if (!ehDiaDePagamento(emp, data)) {
            return 0;
        }

        switch (emp.getTipo()) {
            case "horista":
                return calcularPagamentoHorista(emp, data);
            case "assalariado":
                return calcularPagamentoAssalariado(emp, data);
            case "comissionado":
                return calcularPagamentoComissionado(emp, data);
            default:
                return 0;
        }
    }

    private boolean ehDiaDePagamento(Empregado emp, Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);

        if ("horista".equals(emp.getTipo())) {
            return diaSemana == Calendar.FRIDAY;
        } else if ("assalariado".equals(emp.getTipo())) {
            int ultimoDia = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            return cal.get(Calendar.DAY_OF_MONTH) == ultimoDia;
        } else if ("comissionado".equals(emp.getTipo())) {
            if (diaSemana != Calendar.FRIDAY) return false;

            Date dataContrato = emp.getDataContrato();
            if (dataContrato == null) dataContrato = parseData("1/1/2005");

            long diasDesdeContrato = calcularDiasEntreDatas(dataContrato, data);
            return diasDesdeContrato % 14 == 0;
        }

        return false;
    }

    private double calcularPagamentoHorista(Empregado emp, Date data) {
        String empId = getEmpId(emp);
        if (empId == null) return 0;

        Date dataInicioPeriodo = emp.getDataUltimoPagamento();
        if (dataInicioPeriodo == null) {
            dataInicioPeriodo = encontrarPrimeiroCartao(emp);
            if (dataInicioPeriodo == null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(data);
                cal.add(Calendar.DAY_OF_MONTH, -6);
                dataInicioPeriodo = cal.getTime();
            }
        }

        try {
            String dataInicialStr = formatarData(dataInicioPeriodo);
            String dataFinalStr = formatarData(data);

            String horasNormaisStr = getHorasNormaisTrabalhadas(empId, dataInicialStr, dataFinalStr);
            String horasExtrasStr = getHorasExtrasTrabalhadas(empId, dataInicialStr, dataFinalStr);

            double horasNormais = Double.parseDouble(horasNormaisStr.replace(',', '.'));
            double horasExtras = Double.parseDouble(horasExtrasStr.replace(',', '.'));

            double salarioHora = emp.getSalario() != null ? emp.getSalario() : 0;
            double valorHorasNormais = horasNormais * salarioHora;
            double valorHorasExtras = horasExtras * salarioHora * 1.5;

            double totalBruto = valorHorasNormais + valorHorasExtras;

            long diasTrabalhados = calcularDiasEntreDatas(dataInicioPeriodo, data);
            double taxaSindicalTotal = 0;

            if (Boolean.TRUE.equals(emp.getSindicalizado()) && emp.getTaxaSindical() != null) {
                taxaSindicalTotal = emp.getTaxaSindical() * diasTrabalhados;
                totalBruto -= taxaSindicalTotal;
            }

            totalBruto = Math.max(0, totalBruto);
            emp.setDataUltimoPagamento(data);

            return Math.round(totalBruto * 100.0) / 100.0;
        } catch (Exception e) {
            return 0;
        }
    }

    private double calcularPagamentoComissionado(Empregado emp, Date data) {
        String empId = getEmpId(emp);
        if (empId == null) return 0;

        Date dataInicioPeriodo = emp.getDataUltimoPagamento();
        if (dataInicioPeriodo == null) {
            dataInicioPeriodo = emp.getDataContrato();
            if (dataInicioPeriodo == null) {
                dataInicioPeriodo = parseData("1/1/2005");
            }
        }

        try {
            String dataInicialStr = formatarData(dataInicioPeriodo);
            String dataFinalStr = formatarData(data);

            String vendasStr = getVendasRealizadas(empId, dataInicialStr, dataFinalStr);
            double vendas = Double.parseDouble(vendasStr.replace(',', '.'));

            double salarioBase = emp.getSalario() != null ? emp.getSalario() : 0;
            double salarioQuinzenal = (salarioBase * 12) / 52;

            double comissao = emp.getComissao() != null ? emp.getComissao() : 0;
            double valorComissao = vendas * comissao;

            double totalBruto = salarioQuinzenal + valorComissao;

            long diasTrabalhados = calcularDiasEntreDatas(dataInicioPeriodo, data);
            double taxaSindicalTotal = 0;

            if (Boolean.TRUE.equals(emp.getSindicalizado()) && emp.getTaxaSindical() != null) {
                taxaSindicalTotal = emp.getTaxaSindical() * diasTrabalhados;
                totalBruto -= taxaSindicalTotal;
            }

            totalBruto = Math.max(0, totalBruto);
            emp.setDataUltimoPagamento(data);

            return Math.round(totalBruto * 100.0) / 100.0;
        } catch (Exception e) {
            return 0;
        }
    }

    private double calcularPagamentoAssalariado(Empregado emp, Date data) {
        Date dataInicioPeriodo = emp.getDataUltimoPagamento();
        if (dataInicioPeriodo == null) {
            dataInicioPeriodo = emp.getDataContrato();
            if (dataInicioPeriodo == null) {
                dataInicioPeriodo = parseData("1/1/2005");
            }
        }

        double salario = emp.getSalario() != null ? emp.getSalario() : 0;

        long diasTrabalhados = calcularDiasEntreDatas(dataInicioPeriodo, data);
        double taxaSindicalTotal = 0;

        if (Boolean.TRUE.equals(emp.getSindicalizado()) && emp.getTaxaSindical() != null) {
            taxaSindicalTotal = emp.getTaxaSindical() * diasTrabalhados;
            salario -= taxaSindicalTotal;
        }

        salario = Math.max(0, salario);
        emp.setDataUltimoPagamento(data);

        return Math.round(salario * 100.0) / 100.0;
    }

    private String gerarLinhaFolha(Empregado emp, double pagamento) {
        String metodoPagamento = emp.getMetodoPagamento();

        if ("emMaos".equals(metodoPagamento)) {
            return String.format("%s\t%.2f", emp.getNome(), pagamento);
        } else if ("correios".equals(metodoPagamento)) {
            return String.format("%s\t%s\t%.2f", emp.getNome(), emp.getEndereco(), pagamento);
        } else if ("banco".equals(metodoPagamento)) {
            return String.format("%s\t%s\t%s\t%.2f", emp.getNome(), emp.getBanco(),
                    emp.getAgencia() + "/" + emp.getContaCorrente(), pagamento);
        }

        return String.format("%s\t%.2f", emp.getNome(), pagamento);
    }

    private String getEmpId(Empregado emp) {
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            if (entry.getValue().equals(emp)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Date encontrarPrimeiroCartao(Empregado emp) {
        if (emp.getCartoesPonto() != null && !emp.getCartoesPonto().isEmpty()) {
            List<CartaoPonto> cartoes = new ArrayList<>(emp.getCartoesPonto());
            cartoes.sort(Comparator.comparing(CartaoPonto::getData));
            return cartoes.get(0).getData();
        }
        return null;
    }

    private long calcularDiasEntreDatas(Date inicio, Date fim) {
        long diff = fim.getTime() - inicio.getTime();
        return diff / (24 * 60 * 60 * 1000) + 1;
    }
}
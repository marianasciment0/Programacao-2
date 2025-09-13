package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.Date;

public class CartaoPonto implements Serializable {
    private Date data;
    private double horas;

    // Construtor padrão SEM argumentos (obrigatório para XMLEncoder)
    public CartaoPonto() {
    }

    // Construtor com argumentos
    public CartaoPonto(Date data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    // Getters e Setters (OBRIGATÓRIOS para XMLEncoder)
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public double getHoras() { return horas; }
    public void setHoras(double horas) { this.horas = horas; }
}
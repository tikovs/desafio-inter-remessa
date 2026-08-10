package com.inter.remessa.domain.model;

import com.inter.remessa.adapter.out.persistence.MoneyConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
public class Remessa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "remetente_id", nullable = false)
    private Pessoa remetente;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Pessoa destinatario;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "valor_reais_cents", nullable = false)
    private Money valorReais;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "valor_dolares_cents", nullable = false)
    private Money valorDolares;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal cotacao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Remessa() {}

    public Remessa(Pessoa remetente, Pessoa destinatario, Money valorReais, Money valorDolares, BigDecimal cotacao) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.valorReais = valorReais;
        this.valorDolares = valorDolares;
        this.cotacao = cotacao;
        this.dataHora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
    }

    public Long getId() { return id; }
    public Pessoa getRemetente() { return remetente; }
    public Pessoa getDestinatario() { return destinatario; }
    public Money getValorReais() { return valorReais; }
    public Money getValorDolares() { return valorDolares; }
    public BigDecimal getCotacao() { return cotacao; }
    public LocalDateTime getDataHora() { return dataHora; }
}

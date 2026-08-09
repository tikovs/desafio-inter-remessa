package com.inter.remessa.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Cotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal taxa;

    protected Cotacao() {}

    public Cotacao(LocalDate data, BigDecimal taxa) {
        this.data = data;
        this.taxa = taxa;
    }

    public LocalDate getData() { return data; }
    public BigDecimal getTaxa() { return taxa; }
}

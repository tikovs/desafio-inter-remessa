package com.inter.remessa.domain.model;

import com.inter.remessa.domain.exception.pessoa.InvalidEmailException;
import jakarta.persistence.*;

import java.util.regex.Pattern;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo")
public abstract class Pessoa {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    protected Pessoa() {}

    protected Pessoa(String email, String passwordHash) {
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new InvalidEmailException(email);
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
    }

    public abstract TipoPessoa getType();

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
}

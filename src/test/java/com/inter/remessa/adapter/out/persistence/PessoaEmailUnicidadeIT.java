package com.inter.remessa.adapter.out.persistence;

import com.inter.remessa.domain.model.PessoaFisica;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PessoaEmailUnicidadeIT {

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("Should throw PersistenceException when two Pessoas share the same email")
    void shouldThrowWhenTwoPessoasHaveSameEmail() {
        em.persist(new PessoaFisica("João Silva", "joao@email.com", "$hash", "12345678909"));
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(new PessoaFisica("Maria Souza", "joao@email.com", "$hash", "98765432100"));
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }
}

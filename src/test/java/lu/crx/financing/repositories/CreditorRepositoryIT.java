package lu.crx.financing.repositories;

import lu.crx.financing.BaseIT;
import lu.crx.financing.entities.Creditor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static lu.crx.financing.fixtures.CreditorFixture.aCreditor3;
import static org.assertj.core.api.Assertions.assertThat;

class CreditorRepositoryIT extends BaseIT {
    @Autowired
    protected CreditorRepository creditorRepository;

    @Test
    void testGetByName() {
        Creditor creditor = creditorRepository.saveAndFlush(aCreditor3());
        assertThat(creditorRepository.getByName("Beanstalk"))
                .usingRecursiveComparison()
                .isEqualTo(creditor);
    }

    @Test
    void testGetByName_NotFound() {
        creditorRepository.saveAndFlush(aCreditor3());
        assertThat(creditorRepository.getByName("McDonalds"))
                .isNull();
    }
}

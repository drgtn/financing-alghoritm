package lu.crx.financing.repositories;

import lu.crx.financing.BaseIT;
import lu.crx.financing.entities.Debtor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static lu.crx.financing.fixtures.DebtorFixture.aDebtor3;
import static org.assertj.core.api.Assertions.assertThat;

class DebtorRepositoryIT extends BaseIT {
    @Autowired
    protected DebtorRepository debtorRepository;

    @Test
    void testGetByName() {
        Debtor debtor = debtorRepository.saveAndFlush(aDebtor3());
        assertThat(debtorRepository.getByName("ChocoLoco"))
                .usingRecursiveComparison()
                .isEqualTo(debtor);
    }

    @Test
    void testGetByName_NotFound() {
        debtorRepository.saveAndFlush(aDebtor3());
        assertThat(debtorRepository.getByName("NewDebtor"))
                .isNull();
    }
}

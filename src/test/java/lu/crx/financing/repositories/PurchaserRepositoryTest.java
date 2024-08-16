package lu.crx.financing.repositories;

import lu.crx.financing.BaseIT;
import lu.crx.financing.entities.Purchaser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static lu.crx.financing.fixtures.PurchaserFixture.aPurchaser;
import static org.assertj.core.api.Assertions.assertThat;

class PurchaserRepositoryTest extends BaseIT {
    @Autowired
    private PurchaserRepository purchaserRepository;

    @Test
    void testGetByName() {
        Purchaser purchaser = purchaserRepository.saveAndFlush(aPurchaser());
        assertThat(purchaserRepository.getByName("RichBank"))
                .usingRecursiveComparison()
                .isEqualTo(purchaser);
    }

    @Test
    void testGetByNameNotFound() {
        purchaserRepository.saveAndFlush(aPurchaser());
        assertThat(purchaserRepository.getByName("McDonalds"))
                .isNull();
    }
}

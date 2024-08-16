package lu.crx.financing.repositories;

import lu.crx.financing.BaseIT;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.services.SeedingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static lu.crx.financing.fixtures.PurchaserFinancingSettingsFixture.aPurchaserFinancingSettings;
import static org.assertj.core.api.Assertions.assertThat;

class PurchaserFinancingSettingsRepositoryIT extends BaseIT {
    @Autowired
    protected PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;
    private static final int ANNUAL_RATE_30 = 30;
    private static final int ANNUAL_RATE_50 = 50;
    private static final int ANNUAL_RATE_40 = 40;
    @Autowired
    private SeedingService seedingService;
    @Autowired
    protected CreditorRepository creditorRepository;
    @Autowired
    protected PurchaserRepository purchaserRepository;

    @Test
    public void testFindEligibleSettingsOneOption() {
        seedingService.seedMasterData();
        seedingService.seedInvoices();
        Creditor creditor = creditorRepository.getByName("Coffee Beans LLC");
        Purchaser purchaser = purchaserRepository.getByName("MegaBank");

        List<PurchaserFinancingSettings> eligibleSettings = purchaserFinancingSettingsRepository.findEligibleSettings(creditor, 52);
        assertThat(eligibleSettings)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrder(aPurchaserFinancingSettings(ANNUAL_RATE_30, creditor, purchaser));
    }

    @Test
    public void testFindEligibleSettingsMultipleOptions() {
        seedingService.seedMasterData();
        seedingService.seedInvoices();
        Creditor creditor = creditorRepository.getByName("Coffee Beans LLC");
        Purchaser purchaser1 = purchaserRepository.getByName("RichBank");
        Purchaser purchaser2 = purchaserRepository.getByName("FatBank");
        Purchaser purchaser3 = purchaserRepository.getByName("MegaBank");

        List<PurchaserFinancingSettings> eligibleSettings = purchaserFinancingSettingsRepository.findEligibleSettings(creditor, 33);
        assertThat(eligibleSettings)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrder(aPurchaserFinancingSettings(ANNUAL_RATE_50, creditor, purchaser1),
                        aPurchaserFinancingSettings(ANNUAL_RATE_40, creditor, purchaser2),
                        aPurchaserFinancingSettings(ANNUAL_RATE_30, creditor, purchaser3));
    }

    @Test
    public void testFindEligibleSettings_InvoiceFinancingTermLowerThanPurchaserMinimumFinancingTerm() {
        seedingService.seedMasterData();
        seedingService.seedInvoices();
        Creditor creditor = creditorRepository.getByName("Coffee Beans LLC");

        List<PurchaserFinancingSettings> eligibleSettings = purchaserFinancingSettingsRepository.findEligibleSettings(creditor, 6);
        assertThat(eligibleSettings).isEmpty();
    }

    @Test
    public void testFindEligibleSettings_PurchaserFinancingRateExceedsCreditorMaxRate() {
        seedingService.seedMasterData();
        seedingService.seedInvoices();
        Creditor creditor = creditorRepository.getByName("Coffee Beans LLC");

        List<PurchaserFinancingSettings> eligibleSettings = purchaserFinancingSettingsRepository.findEligibleSettings(creditor, 70);
        assertThat(eligibleSettings).isEmpty();
    }

}

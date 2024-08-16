package lu.crx.financing.services;

import lu.crx.financing.BaseIT;
import lu.crx.financing.entities.*;
import lu.crx.financing.fixtures.InvoiceFixture;
import lu.crx.financing.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.Optional;

import static lu.crx.financing.fixtures.FinancedInvoiceFixture.aFinancedInvoiceFromMultiplePurchaseOption;
import static lu.crx.financing.fixtures.FinancedInvoiceFixture.aFinancedInvoiceFromOnePurchaseOption;
import static org.assertj.core.api.Assertions.assertThat;

class FinancingServiceIT extends BaseIT {
    @Autowired
    private SeedingService seedingService;
    @Autowired
    private FinancingService financingService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private FinancedInvoiceRepository financedInvoiceRepository;
    @Autowired
    private PurchaserRepository purchaserRepository;
    @Autowired
    private CreditorRepository creditorRepository;
    @Autowired
    private DebtorRepository debtorRepository;

    @BeforeEach
    void setUp() {
        seedingService.seedMasterData();
    }

    @Test
    void testFinancedInvoicedSingleEligiblePurchaser() {
        Creditor creditor = creditorRepository.getByName("Coffee Beans LLC");
        Debtor debtor = debtorRepository.getByName("Chocolate Factory");
        Purchaser purchaser = purchaserRepository.getByName("MegaBank");
        Invoice invoice = InvoiceFixture.aInvoice1();
        invoice.setCreditor(creditor);
        invoice.setDebtor(debtor);
        invoiceRepository.saveAndFlush(invoice);
        financingService.finance();
        ExampleMatcher matcher = getExampleMatcherIgnoringId();
        FinancedInvoice actualFinancedInvoice = aFinancedInvoiceFromOnePurchaseOption(invoice, purchaser);
        Optional<FinancedInvoice> expectedFinancedInvoice = financedInvoiceRepository.findOne(Example.of(actualFinancedInvoice, matcher));

        assertThat(expectedFinancedInvoice).isPresent();
        assertThat(expectedFinancedInvoice.get())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(actualFinancedInvoice);
        assertThat(invoice.isFinanced()).isTrue();

    }

    @Test
    void testFinancedInvoicedMultipleEligiblePurchaser_SelectPurchaserWithLowestFinancingRate() {
        Creditor creditor = creditorRepository.getByName("Beanstalk");
        Debtor debtor = debtorRepository.getByName("ChocoLoco");
        Purchaser purchaser = purchaserRepository.getByName("FatBank");
        Invoice invoice = InvoiceFixture.aInvoice12();
        invoice.setCreditor(creditor);
        invoice.setDebtor(debtor);
        invoiceRepository.saveAndFlush(invoice);
        financingService.finance();
        ExampleMatcher matcher = getExampleMatcherIgnoringId();
        FinancedInvoice actualFinancedInvoice = aFinancedInvoiceFromMultiplePurchaseOption(invoice, purchaser);
        Optional<FinancedInvoice> expectedFinancedInvoice = financedInvoiceRepository.findOne(Example.of(actualFinancedInvoice, matcher));

        assertThat(expectedFinancedInvoice).isPresent();
        assertThat(expectedFinancedInvoice.get())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(actualFinancedInvoice);
        assertThat(invoice.isFinanced()).isTrue();
    }

    private static ExampleMatcher getExampleMatcherIgnoringId() {
        return ExampleMatcher.matching().withIgnorePaths("id");
    }

    @Test
    void testFinancedInvoicedNoPurchaserSettingForCreditor() {
        Creditor creditor = creditorRepository.saveAndFlush(Creditor.builder()
                .name("NewCreditor")
                .maxFinancingRateInBps(3)
                .build());
        Debtor debtor = debtorRepository.getByName("Chocolate Factory");
        Invoice invoice = InvoiceFixture.aInvoice1();
        invoice.setCreditor(creditor);
        invoice.setDebtor(debtor);
        invoiceRepository.saveAndFlush(invoice);
        financingService.finance();

        assertThat(financedInvoiceRepository.findAll()).isEmpty();
        assertThat(invoice.isFinanced()).isFalse();
    }

    @Test
    void testFinancedInvoicedMultipleEligiblePurchaser_butAlreadyFinanced() {
        Creditor creditor = creditorRepository.getByName("Beanstalk");
        Debtor debtor = debtorRepository.getByName("ChocoLoco");
        Invoice invoice = InvoiceFixture.aInvoice12();
        invoice.setCreditor(creditor);
        invoice.setDebtor(debtor);
        invoice.setFinanced(true);
        Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
        financingService.finance();
        assertThat(financedInvoiceRepository.findAll()).isEmpty();
        assertThat(savedInvoice.isFinanced()).isTrue();
        assertThat(financedInvoiceRepository.findAll()).isEmpty();
    }
}

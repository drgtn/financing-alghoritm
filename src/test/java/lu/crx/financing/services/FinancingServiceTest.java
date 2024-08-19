package lu.crx.financing.services;

import lu.crx.financing.entities.*;
import lu.crx.financing.repositories.FinancedInvoiceRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.lang.Math.toIntExact;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static lu.crx.financing.fixtures.FinancedInvoiceFixture.aFinancedInvoiceFromMultiplePurchaseOption;
import static lu.crx.financing.fixtures.InvoiceFixture.aInvoice12;
import static lu.crx.financing.fixtures.PurchaserFinancingSettingsFixture.aPurchaserFinancingSettings;
import static lu.crx.financing.fixtures.PurchaserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class FinancingServiceTest {
    private static final int ANNUAL_RATE_30 = 30;
    private static final int ANNUAL_RATE_25 = 25;
    private static final int ANNUAL_RATE_45 = 45;
    @InjectMocks
    private FinancingService financingService;
    @Spy
    private InvoiceRepository invoiceRepository;
    @Mock
    private PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;
    @Mock
    private FinancedInvoiceRepository financedInvoiceRepository;
    @Captor
    private ArgumentCaptor<FinancedInvoice> financedInvoiceArgumentCaptor;

    @BeforeEach
    void setUp() {
        setField(financingService, "batchSize", 10);
    }

    @Test
    void testFinance() {
        Invoice invoice = aInvoice12();
        Creditor creditor = invoice.getCreditor();
        Purchaser purchaser1 = aPurchaser1();
        Purchaser purchaser2 = aPurchaser2();
        Purchaser purchaser3 = aPurchaser3();
        int financingTermInDays = calculateFinancingTermInDays(invoice, LocalDate.now());

        List<PurchaserFinancingSettings> purchaserFinancingSettingsList = asList(
                aPurchaserFinancingSettings(ANNUAL_RATE_30, creditor, purchaser1),
                aPurchaserFinancingSettings(ANNUAL_RATE_25, creditor, purchaser2),
                aPurchaserFinancingSettings(ANNUAL_RATE_45, creditor, purchaser3));

        Page<Invoice> firstPage = new PageImpl<>(of(invoice));
        Page<Invoice> secondPage = new PageImpl<>(emptyList());
        PageableMatcher firstPageable = new PageableMatcher(0, 10);
        PageableMatcher secondPageable = new PageableMatcher(1, 10);

        when(invoiceRepository.getAllByFinancedFalse(argThat(firstPageable))).thenReturn(firstPage);
        when(invoiceRepository.getAllByFinancedFalse(argThat(secondPageable))).thenReturn(secondPage);
        when(purchaserFinancingSettingsRepository.findEligiblePurchaserSetting(creditor, financingTermInDays))
                .thenReturn(purchaserFinancingSettingsList);

        financingService.finance();
        verify(invoiceRepository).getAllByFinancedFalse(argThat(firstPageable));
        verify(invoiceRepository).getAllByFinancedFalse(argThat(secondPageable));
        verify(purchaserFinancingSettingsRepository).findEligiblePurchaserSetting(creditor, financingTermInDays);
        verify(financedInvoiceRepository).save(financedInvoiceArgumentCaptor.capture());

        assertThat(financedInvoiceArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(aFinancedInvoiceFromMultiplePurchaseOption(invoice, purchaser2));
        assertThat(invoice.isFinanced()).isTrue();
    }

    @Test
    void testFinance_InvoicesAlreadyFinanced() {
        Page<Invoice> firstPage = new PageImpl<>(emptyList());
        PageableMatcher firstPageable = new PageableMatcher(0, 10);

        when(invoiceRepository.getAllByFinancedFalse(argThat(firstPageable))).thenReturn(firstPage);

        financingService.finance();

        verify(invoiceRepository).getAllByFinancedFalse(argThat(firstPageable));
        verifyNoInteractions(purchaserFinancingSettingsRepository);
        verifyNoInteractions(financedInvoiceRepository);
        verify(invoiceRepository, never()).save(any());
    }

    private int calculateFinancingTermInDays(Invoice invoice, LocalDate currentDate) {
        return toIntExact(ChronoUnit.DAYS.between(currentDate, invoice.getMaturityDate()));
    }

    private record PageableMatcher(int expectedPage, int expectedSize) implements ArgumentMatcher<Pageable> {
        @Override
        public boolean matches(Pageable pageable) {
            if (pageable == null) {
                return false;
            }
            return pageable.getPageNumber() == expectedPage && pageable.getPageSize() == expectedSize;
        }
    }
}

package lu.crx.financing.services;

import lu.crx.financing.entities.*;
import lu.crx.financing.repositories.FinancedInvoiceRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.toIntExact;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static lu.crx.financing.fixtures.FinancedInvoiceFixture.aFinancedInvoiceFromMultiplePurchaseOption;
import static lu.crx.financing.fixtures.InvoiceFixture.aInvoice12;
import static lu.crx.financing.fixtures.PurchaserFinancingSettingsFixture.aPurchaserFinancingSettings;
import static lu.crx.financing.fixtures.PurchaserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancingServiceTest {
    private static final int ANNUAL_RATE_30 = 30;
    private static final int ANNUAL_RATE_25 = 25;
    private static final int ANNUAL_RATE_45 = 45;
    @InjectMocks
    private FinancingService financingService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;
    @Mock
    private FinancedInvoiceRepository financedInvoiceRepository;
    @Captor
    private ArgumentCaptor<FinancedInvoice> captor;

    @Test
    void testFinance() {
        Invoice invoice = aInvoice12();
        Creditor creditor = invoice.getCreditor();
        Purchaser purchaser1 = aPurchaser1();
        Purchaser purchaser2 = aPurchaser2();
        Purchaser purchaser3 = aPurchaser3();
        int financingTermInDays = calculateFinancingTermInDays(invoice, LocalDate.now());
        List<Invoice> invoices = of(invoice);
        when(invoiceRepository.getAllByFinancedFalse()).thenReturn(invoices);
        List<PurchaserFinancingSettings> purchaserFinancingSettingsList = Arrays.asList(
                aPurchaserFinancingSettings(ANNUAL_RATE_30, creditor, purchaser1),
                aPurchaserFinancingSettings(ANNUAL_RATE_25, creditor, purchaser2),
                aPurchaserFinancingSettings(ANNUAL_RATE_45, creditor, purchaser3));
        when(purchaserFinancingSettingsRepository
                .findEligiblePurchaserSetting(creditor, financingTermInDays)).thenReturn(purchaserFinancingSettingsList);

        financingService.finance();

        verify(invoiceRepository).getAllByFinancedFalse();
        verify(purchaserFinancingSettingsRepository)
                .findEligiblePurchaserSetting(creditor, financingTermInDays);
        verify(financedInvoiceRepository).save(captor.capture());
        verify(invoiceRepository).save(invoice);

        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(aFinancedInvoiceFromMultiplePurchaseOption(invoice, purchaser2));
        assertThat(invoice.isFinanced()).isTrue();
    }

    @Test
    void testFinance_InvoicesAlreadyFinanced() {
        when(invoiceRepository.getAllByFinancedFalse()).thenReturn(emptyList());

        financingService.finance();

        verify(invoiceRepository).getAllByFinancedFalse();
        verifyNoInteractions(purchaserFinancingSettingsRepository);
        verifyNoInteractions(financedInvoiceRepository);
        verify(invoiceRepository, never()).save(any());
    }

    private int calculateFinancingTermInDays(Invoice invoice, LocalDate currentDate) {
        return toIntExact(ChronoUnit.DAYS.between(currentDate, invoice.getMaturityDate()));
    }
}

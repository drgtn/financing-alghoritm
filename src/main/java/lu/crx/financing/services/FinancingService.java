package lu.crx.financing.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.*;
import lu.crx.financing.repositories.FinancedInvoiceRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancingService {
    @Value("${batch.size:1000}")
    private int batchSize;
    private final InvoiceRepository invoiceRepository;
    private final PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;
    private final FinancedInvoiceRepository financedInvoiceRepository;

    @Transactional
    public void finance() {
        Page<Invoice> nonFinancedInvoicesPage;
        Pageable pageable = PageRequest.of(0, batchSize);
        do {
            nonFinancedInvoicesPage = invoiceRepository.getAllByFinancedFalse(pageable);
            if (nonFinancedInvoicesPage.hasContent()) {
                processInvoicesBatch(nonFinancedInvoicesPage.getContent());
                pageable = pageable.next();
            }
        } while (nonFinancedInvoicesPage.hasContent());
    }

    private void processInvoicesBatch(List<Invoice> nonFinancedInvoices) {
        log.info("Financing started");
        LocalDate today = LocalDate.now();
        nonFinancedInvoices.forEach(invoice -> processSingleInvoice(invoice, today));
        log.info("Financing completed");
    }

    private void processSingleInvoice(Invoice invoice, LocalDate today) {
        log.info("Processing invoice: {}", invoice);
        Optional<PurchaserFinancingSettings> bestFinancingOption = findBestFinancingOption(invoice, today);
        bestFinancingOption.ifPresentOrElse(purchaserFinancingSettings -> {
                    int financingTermInDays = calculateFinancingTermInDays(invoice, today);
                    int financingRateInBps = calculateFinancingRateInBps(purchaserFinancingSettings, financingTermInDays);
                    long earlyPaymentAmount = calculateEarlyPaymentAmountInCents(invoice, financingRateInBps);

                    FinancedInvoice financingResult = FinancedInvoice.builder()
                            .invoice(invoice)
                            .purchaser(purchaserFinancingSettings.getPurchaser())
                            .financingTermInDays(financingTermInDays)
                            .financingRateInBps(financingRateInBps)
                            .financingDate(today)
                            .earlyPaymentAmountInCents(earlyPaymentAmount)
                            .build();

                    financedInvoiceRepository.save(financingResult);
                    invoice.setFinanced(true);
                    log.info("Invoice {} financed by Purchaser {} at rate {} bps", invoice,
                            purchaserFinancingSettings.getPurchaser().getName(), financingRateInBps);
                },
                () -> log.info("No eligible Purchaser found for Invoice {}", invoice.getId()));
    }

    private int calculateFinancingTermInDays(Invoice invoice, LocalDate currentDate) {
        return toIntExact(DAYS.between(currentDate, invoice.getMaturityDate()));
    }

    private int calculateFinancingRateInBps(PurchaserFinancingSettings purchaserFinancingSettings, int financingTermInDays) {
        return (int) Math.round((double) (purchaserFinancingSettings.getAnnualRateInBps() * financingTermInDays) / 360);
    }

    private long calculateEarlyPaymentAmountInCents(Invoice invoice, int financingRateInBps) {
        return invoice.getValueInCents() - (invoice.getValueInCents() * financingRateInBps) / 10000;
    }

    private Optional<PurchaserFinancingSettings> findBestFinancingOption(Invoice invoice, LocalDate today) {
        Creditor creditor = invoice.getCreditor();
        int financingTermInDays = calculateFinancingTermInDays(invoice, today);

        List<PurchaserFinancingSettings> eligibleSettings = purchaserFinancingSettingsRepository
                .findEligiblePurchaserSetting(creditor, financingTermInDays);
        log.info("For Creditor: {} found {} Purchasers: {}", creditor,
                eligibleSettings.size(),
                getAllPurchaserFromSettings(eligibleSettings));
        return getPurchaserWithLowestFinancingRate(eligibleSettings, financingTermInDays);
    }

    private static Optional<PurchaserFinancingSettings> getPurchaserWithLowestFinancingRate(List<PurchaserFinancingSettings> eligibleSettings,
                                                                                            int financingTermInDays) {
        return eligibleSettings.stream()
                .min((s1, s2) -> {
                    double rate1 = (double) (s1.getAnnualRateInBps() * financingTermInDays) / 360;
                    double rate2 = (double) (s2.getAnnualRateInBps() * financingTermInDays) / 360;
                    return Double.compare(rate1, rate2);
                });
    }

    private static Set<Purchaser> getAllPurchaserFromSettings(List<PurchaserFinancingSettings> eligibleSettings) {
        return eligibleSettings.stream().map(PurchaserFinancingSettings::getPurchaser).collect(toSet());
    }
}

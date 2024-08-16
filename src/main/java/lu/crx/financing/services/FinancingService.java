package lu.crx.financing.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.FinancedInvoice;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import lu.crx.financing.repositories.FinancedInvoiceRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import lu.crx.financing.repositories.PurchaserFinancingSettingsRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.ceil;
import static java.lang.Math.toIntExact;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancingService {
    private final InvoiceRepository invoiceRepository;
    private final PurchaserFinancingSettingsRepository purchaserFinancingSettingsRepository;
    private final FinancedInvoiceRepository financedInvoiceRepository;

    @Transactional
    public void finance() {
        log.info("Financing started");
        List<Invoice> nonFinancedInvoices = invoiceRepository.getAllByFinancedFalse();
        LocalDate today = LocalDate.now();
        nonFinancedInvoices.forEach(invoice -> {
            Optional<PurchaserFinancingSettings> bestFinancingOption = findBestFinancingOption(invoice, today);
            bestFinancingOption.ifPresent(purchaserFinancingSettings -> {
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
                invoiceRepository.save(invoice);
                log.info("Invoice {} financed by Purchaser {} at rate {} bps", invoice.getId(),
                        purchaserFinancingSettings.getPurchaser().getName(), financingRateInBps);
            });
            log.info("Processing invoice: {}", invoice);
        });
        log.info("Financing completed");
    }

    private int calculateFinancingTermInDays(Invoice invoice, LocalDate currentDate) {
        return toIntExact(ChronoUnit.DAYS.between(currentDate, invoice.getMaturityDate()));
    }

    private int calculateFinancingRateInBps(PurchaserFinancingSettings purchaserFinancingSettings, int financingTermInDays) {
        return (int) ceil((double)(purchaserFinancingSettings.getAnnualRateInBps() * financingTermInDays) / 360);
    }

    private long calculateEarlyPaymentAmountInCents(Invoice invoice, int financingRateInBps) {
        return invoice.getValueInCents() - (invoice.getValueInCents() * financingRateInBps) / 10000;
    }

    private Optional<PurchaserFinancingSettings> findBestFinancingOption(Invoice invoice, LocalDate today) {
        Creditor creditor = invoice.getCreditor();
        int financingTermInDays = calculateFinancingTermInDays(invoice, today);

        List<PurchaserFinancingSettings> eligibleSettings = purchaserFinancingSettingsRepository
                .findEligibleSettings(creditor, financingTermInDays);
        log.info("For Creditor: {} found {} Purchasers: {}", creditor,
                eligibleSettings.size(),
                eligibleSettings.stream().map(PurchaserFinancingSettings::getPurchaser).collect(Collectors.toSet()));
        return getPurchaserWithLowestFinancingRate(eligibleSettings, financingTermInDays);
    }

    private static Optional<PurchaserFinancingSettings> getPurchaserWithLowestFinancingRate(List<PurchaserFinancingSettings> eligibleSettings,
                                                                                            int financingTermInDays) {
        Optional<PurchaserFinancingSettings> purchaserWithLowestFinancingRate = eligibleSettings.stream()
                .min((s1, s2) -> {
                    double rate1 = (double) (s1.getAnnualRateInBps() * financingTermInDays) / 360;
                    double rate2 = (double)(s2.getAnnualRateInBps() * financingTermInDays) / 360;
                    return Double.compare(rate1, rate2);
                });
        log.info("Purchasers with the lowest financing rate is: {} ", purchaserWithLowestFinancingRate);
        return purchaserWithLowestFinancingRate;
    }
}

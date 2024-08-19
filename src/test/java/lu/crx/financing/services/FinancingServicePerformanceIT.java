package lu.crx.financing.services;

import lombok.extern.slf4j.Slf4j;
import lu.crx.financing.BaseIT;
import lu.crx.financing.repositories.FinancedInvoiceRepository;
import lu.crx.financing.repositories.InvoiceRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@Slf4j
public class FinancingServicePerformanceIT extends BaseIT {
    @Autowired
    private SeedingService seedingService;
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private FinancingService financingService;
    @Autowired
    private FinancedInvoiceRepository financedInvoiceRepository;

    @BeforeEach
    void setUp() {
        long startTime = System.currentTimeMillis();
        seedingService.seedMasterData();
        seedInvoicesInBatches(10000, 50);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Persisted {} invoices in {}", invoiceRepository.findAll().size(), duration + " ms");
    }

    @Test
    public void testPerformanceOnLargeDataset() {
        long startTime = System.currentTimeMillis();
        assertTimeout(ofSeconds(30), () -> financingService.finance());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("Processed {} invoices in {} ms", invoiceRepository.findAll().size(), duration);
        Assertions.assertThat(financedInvoiceRepository.findAll()).isNotEmpty();

    }

    public void seedInvoicesInBatches(int totalInvoiceCount, int batchSize) {
        log.info("Seeding {} invoices in batches of {}", totalInvoiceCount, batchSize);
        int invoicesPerRun = 15;
        int totalRuns = (totalInvoiceCount + invoicesPerRun - 1) / invoicesPerRun;

        int invoiceCounter = 0;
        for (int run = 0; run < totalRuns; run++) {
            seedingService.seedInvoices();
            invoiceCounter += invoicesPerRun;
            if (invoiceCounter >= batchSize) {
                entityManager.flush();
                entityManager.clear();
                log.info("Flushed and cleared after seeding {} invoices", invoiceCounter);
                invoiceCounter = 0;
            }
        }
        if (invoiceCounter > 0) {
            entityManager.flush();
            entityManager.clear();
            log.info("Final flush and clear after seeding remaining {} invoices", invoiceCounter);
        }
        log.info("Completed seeding of {} invoices in batches of {}", totalInvoiceCount, batchSize);
    }
}

package lu.crx.financing.fixtures;

import lu.crx.financing.entities.FinancedInvoice;
import lu.crx.financing.entities.Invoice;
import lu.crx.financing.entities.Purchaser;

import java.time.LocalDate;

public class FinancedInvoiceFixture {
    public static FinancedInvoice aFinancedInvoiceFromOnePurchaseOption(Invoice invoice, Purchaser purchaser) {
        return FinancedInvoice.builder()
                .invoice(invoice)
                .purchaser(purchaser)
                .financingTermInDays(52)
                .financingRateInBps(4)
                .earlyPaymentAmountInCents(199920)
                .financingDate(LocalDate.now())
                .build();
    }

    public static FinancedInvoice aFinancedInvoiceFromMultiplePurchaseOption(Invoice invoice, Purchaser purchaser) {
        return FinancedInvoice.builder()
                .invoice(invoice)
                .purchaser(purchaser)
                .financingTermInDays(14)
                .financingRateInBps(1)
                .earlyPaymentAmountInCents(4999500)
                .financingDate(LocalDate.now())
                .build();
    }
}

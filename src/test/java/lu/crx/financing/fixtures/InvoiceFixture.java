package lu.crx.financing.fixtures;

import lu.crx.financing.entities.Invoice;

import java.time.LocalDate;

import static lu.crx.financing.fixtures.CreditorFixture.*;
import static lu.crx.financing.fixtures.DebtorFixture.*;

public class InvoiceFixture {
    public static Invoice aInvoice1() {
        return Invoice.builder()
                .creditor(aCreditor1())
                .debtor(aDebtor1())
                .valueInCents(200000)
                .maturityDate(LocalDate.now().plusDays(52))
                .build();
    }

    public static Invoice aInvoice2() {
        return Invoice.builder()
                .creditor(aCreditor1())
                .debtor(aDebtor2())
                .valueInCents(800000)
                .maturityDate(LocalDate.now().plusDays(33))
                .build();
    }

    public static Invoice aInvoice3() {
        return Invoice.builder()
                .creditor(aCreditor1())
                .debtor(aDebtor3())
                .valueInCents(600000)
                .maturityDate(LocalDate.now().plusDays(43))
                .build();
    }


    public static Invoice aInvoice4() {
        return Invoice.builder()
                .creditor(aCreditor1())
                .debtor(aDebtor1())
                .valueInCents(500000)
                .maturityDate(LocalDate.now().plusDays(80))
                .build();
    }

    public static Invoice aInvoice5() {
        return Invoice.builder()
                .creditor(aCreditor1())
                .debtor(aDebtor2())
                .valueInCents(6000000)
                .maturityDate(LocalDate.now().plusDays(5))
                .build();
    }

    public static Invoice aInvoice6() {
        return Invoice.builder()
                .creditor(aCreditor2())
                .debtor(aDebtor3())
                .valueInCents(500000)
                .maturityDate(LocalDate.now().plusDays(10))
                .build();
    }

    public static Invoice aInvoice7() {
        return Invoice.builder()
                .creditor(aCreditor2())
                .debtor(aDebtor1())
                .valueInCents(800000)
                .maturityDate(LocalDate.now().plusDays(15))
                .build();
    }

    public static Invoice aInvoice8() {
        return Invoice.builder()
                .creditor(aCreditor2())
                .debtor(aDebtor2())
                .valueInCents(9000000)
                .maturityDate(LocalDate.now().plusDays(30))
                .build();
    }

    public static Invoice aInvoice9() {
        return Invoice.builder()
                .creditor(aCreditor2())
                .debtor(aDebtor3())
                .valueInCents(450000)
                .maturityDate(LocalDate.now().plusDays(32))
                .build();
    }

    public static Invoice aInvoice10() {
        return Invoice.builder()
                .creditor(aCreditor2())
                .debtor(aDebtor1())
                .valueInCents(800000)
                .maturityDate(LocalDate.now().plusDays(11))
                .build();
    }

    public static Invoice aInvoice11() {
        return Invoice.builder()
                .creditor(aCreditor3())
                .debtor(aDebtor2())
                .valueInCents(3000000)
                .maturityDate(LocalDate.now().plusDays(10))
                .build();
    }

    public static Invoice aInvoice12() {
        return Invoice.builder()
                .creditor(aCreditor3())
                .debtor(aDebtor3())
                .valueInCents(5000000)
                .maturityDate(LocalDate.now().plusDays(14))
                .build();
    }

    public static Invoice aInvoice13() {
        return Invoice.builder()
                .creditor(aCreditor3())
                .debtor(aDebtor1())
                .valueInCents(9000000)
                .maturityDate(LocalDate.now().plusDays(23))
                .build();
    }

    public static Invoice aInvoice14() {
        return Invoice.builder()
                .creditor(aCreditor3())
                .debtor(aDebtor2())
                .valueInCents(800000)
                .maturityDate(LocalDate.now().plusDays(18))
                .build();
    }

    public static Invoice aInvoice15() {
        return Invoice.builder()
                .creditor(aCreditor3())
                .debtor(aDebtor3())
                .valueInCents(9000000)
                .maturityDate(LocalDate.now().plusDays(50))
                .build();
    }
}

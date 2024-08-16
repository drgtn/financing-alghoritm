package lu.crx.financing.repositories;

import lu.crx.financing.BaseIT;
import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Debtor;
import lu.crx.financing.entities.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static lu.crx.financing.fixtures.CreditorFixture.aCreditor1;
import static lu.crx.financing.fixtures.DebtorFixture.*;
import static lu.crx.financing.fixtures.InvoiceFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;


class InvoiceRepositoryIT extends BaseIT {
    @Autowired
    protected InvoiceRepository invoiceRepository;
    @Autowired
    protected CreditorRepository creditorRepository;

    @Autowired
    protected DebtorRepository debtorRepository;

    @BeforeEach
    public void setUp() {
        Creditor creditor1 = aCreditor1();
        Debtor debtor1 = aDebtor1();
        Debtor debtor2 = aDebtor2();
        Debtor debtor3 = aDebtor3();

        creditorRepository.saveAndFlush(creditor1);
        debtorRepository.saveAllAndFlush(newArrayList(debtor1, debtor2, debtor3));

        Invoice invoice1 = aInvoice1();
        invoice1.setFinanced(true);
        invoice1.setCreditor(creditor1);
        invoice1.setDebtor(debtor1);
        invoiceRepository.saveAndFlush(invoice1);

        Invoice invoice2 = aInvoice2();
        invoice2.setCreditor(creditor1);
        invoice2.setDebtor(debtor2);
        invoiceRepository.saveAndFlush(invoice2);

        Invoice invoice3 = aInvoice3();
        invoice3.setCreditor(creditor1);
        invoice3.setDebtor(debtor3);
        invoiceRepository.saveAndFlush(invoice3);
    }


    @Test
    public void testGetAllByFinancedFalse() {
        assertThat(invoiceRepository.findAll()).hasSize(3);

        assertThat(invoiceRepository.getAllByFinancedFalse()).isNotEmpty().hasSize(2).allSatisfy(invoice -> assertThat(invoice.isFinanced()).isFalse());
    }


}

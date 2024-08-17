package lu.crx.financing.fixtures;

import lu.crx.financing.entities.Creditor;

public class CreditorFixture {
    public static Creditor aCreditor1() {
        return Creditor.builder()
                .name("Coffee Beans LLC")
                .maxFinancingRateInBps(5)
                .build();
    }

    public static Creditor aCreditor2() {
        return Creditor.builder()
                .name("Home Brew")
                .maxFinancingRateInBps(3)
                .build();
    }

    public static Creditor aCreditor3() {
        return Creditor.builder()
                .name("Beanstalk")
                .maxFinancingRateInBps(2)
                .build();
    }
}

package lu.crx.financing.fixtures;

import lu.crx.financing.entities.Debtor;

public class DebtorFixture {
    public static Debtor aDebtor1() {
        return Debtor.builder()
                .name("Chocolate Factory")
                .build();
    }

    public static Debtor aDebtor2() {
        return Debtor.builder()
                .name("Sweets Inc")
                .build();
    }

    public static Debtor aDebtor3() {
        return Debtor.builder()
                .name("ChocoLoco")
                .build();
    }
}

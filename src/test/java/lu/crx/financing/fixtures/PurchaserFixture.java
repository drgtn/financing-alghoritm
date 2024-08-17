package lu.crx.financing.fixtures;

import lu.crx.financing.entities.Purchaser;

import static lu.crx.financing.fixtures.CreditorFixture.*;
import static lu.crx.financing.fixtures.PurchaserFinancingSettingsFixture.aPurchaserFinancingSettings;

public class PurchaserFixture {
    public static Purchaser aPurchaser1() {
        Purchaser richBank = Purchaser.builder()
                .name("RichBank")
                .minimumFinancingTermInDays(10)
                .build();
        richBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(50, aCreditor1()));
        richBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(60, aCreditor2()));
        richBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(30, aCreditor3()));
        return richBank;
    }

    public static Purchaser aPurchaser2() {
        Purchaser fatBank = Purchaser.builder()
                .name("FatBank")
                .minimumFinancingTermInDays(12)
                .build();
        fatBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(40, aCreditor1()));
        fatBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(80, aCreditor2()));
        fatBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(25, aCreditor3()));
        return fatBank;
    }

    public static Purchaser aPurchaser3() {
        Purchaser megaBank = Purchaser.builder()
                .name("MegaBank")
                .minimumFinancingTermInDays(8)
                .build();
        megaBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(30, aCreditor1()));
        megaBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(50, aCreditor2()));
        megaBank.addPurchaserFinancingSettings(aPurchaserFinancingSettings(45, aCreditor3()));
        return megaBank;
    }

    public static Purchaser aPurchaserWithNoSettings() {
        return Purchaser.builder()
                .name("RichBank")
                .minimumFinancingTermInDays(10)
                .build();
    }
}

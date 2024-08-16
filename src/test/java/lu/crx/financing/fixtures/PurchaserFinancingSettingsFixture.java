package lu.crx.financing.fixtures;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.Purchaser;
import lu.crx.financing.entities.PurchaserFinancingSettings;

public class PurchaserFinancingSettingsFixture {
    public static PurchaserFinancingSettings aPurchaserFinancingSettings(int annualRate, Creditor creditor, Purchaser purchaser){
        return PurchaserFinancingSettings.builder()
                .creditor(creditor)
                .purchaser(purchaser)
                .annualRateInBps(annualRate)
                .build();
    }

    public static PurchaserFinancingSettings aPurchaserFinancingSettings(int annualRate, Creditor creditor){
        return PurchaserFinancingSettings.builder()
                .creditor(creditor)
                .annualRateInBps(annualRate)
                .build();
    }
}

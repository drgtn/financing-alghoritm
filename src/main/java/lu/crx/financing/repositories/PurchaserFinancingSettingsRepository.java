package lu.crx.financing.repositories;

import lu.crx.financing.entities.Creditor;
import lu.crx.financing.entities.PurchaserFinancingSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaserFinancingSettingsRepository extends JpaRepository<PurchaserFinancingSettings, Long> {
    @Query("SELECT pfs FROM PurchaserFinancingSettings pfs " +
            "JOIN pfs.purchaser p " +
            "WHERE pfs.creditor = :creditor " +
            "AND p.minimumFinancingTermInDays <= :financingTermInDays " +
            "AND (pfs.annualRateInBps * :financingTermInDays) <= (pfs.creditor.maxFinancingRateInBps * 360)")
    List<PurchaserFinancingSettings> findEligiblePurchaserSetting(Creditor creditor, int financingTermInDays);
}

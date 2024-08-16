package lu.crx.financing.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Purchaser is an entity (usually a bank) that wants to purchase the invoices. I.e. it issues a loan
 * to the creditor for the term and the value of the invoice, according to the rate set up by this purchaser.
 */
@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchaser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    @Basic(optional = false)
    private String name;

    /**
     * The minimum financing term (duration between the financing date and the maturity date of the invoice).
     */
    @Basic(optional = false)
    private int minimumFinancingTermInDays;

    /**
     * The per-creditor settings for financing.
     */
    @OneToMany(mappedBy = "purchaser",
            orphanRemoval = true,
            cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private final Set<PurchaserFinancingSettings> purchaserFinancingSettings = new HashSet<>();

    public void addPurchaserFinancingSettings(PurchaserFinancingSettings purchaserFinancingSetting) {
        purchaserFinancingSettings.add(purchaserFinancingSetting);
        purchaserFinancingSetting.setPurchaser(this);
    }

    public void removePurchaserFinancingSettings(PurchaserFinancingSettings purchaserFinancingSetting) {
        purchaserFinancingSettings.remove(purchaserFinancingSetting);
        purchaserFinancingSetting.setPurchaser(null);
    }

}

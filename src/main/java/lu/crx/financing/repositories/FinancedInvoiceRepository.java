package lu.crx.financing.repositories;

import lu.crx.financing.entities.FinancedInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancedInvoiceRepository extends JpaRepository<FinancedInvoice, Long> {
}
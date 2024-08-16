package lu.crx.financing.repositories;

import lu.crx.financing.entities.Creditor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditorRepository extends JpaRepository<Creditor, Long> {
    Creditor getByName(String name);
}
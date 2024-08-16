package lu.crx.financing.services;

import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FinancingService {

    @Transactional
    public void finance() {
        log.info("Financing started");

        // TODO This is the financing algorithm that needs to be implemented according to the specification.

        log.info("Financing completed");
    }

}

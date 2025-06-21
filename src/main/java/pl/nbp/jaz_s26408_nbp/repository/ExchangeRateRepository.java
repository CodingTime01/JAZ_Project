package pl.nbp.jaz_s26408_nbp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.nbp.jaz_s26408_nbp.model.ExchangeRateRequest;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateRequest, Long> {
}
package pl.nbp.jaz_s26408_nbp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nbp.jaz_s26408_nbp.service.ExchangeRateService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/exchange-rate")
    public ResponseEntity<Double> getExchangeRate(
            @RequestParam String currency,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        double averageRate = exchangeRateService.getAverageExchangeRate(currency, start, end);
        return ResponseEntity.ok(averageRate);
    }
}

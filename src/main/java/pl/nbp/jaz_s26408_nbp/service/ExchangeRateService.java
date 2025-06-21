package pl.nbp.jaz_s26408_nbp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pl.nbp.jaz_s26408_nbp.model.ExchangeRateRequest;
import pl.nbp.jaz_s26408_nbp.repository.ExchangeRateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public double getAverageExchangeRate(String currency, LocalDate startDate, LocalDate endDate) {
        String url = String.format(
                "http://api.nbp.pl/api/exchangerates/rates/A/%s/%s/%s/?format=json",
                currency.toUpperCase(),
                startDate,
                endDate
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode rates = root.path("rates");

            double sum = 0;
            int count = 0;

            for (Iterator<JsonNode> it = rates.elements(); it.hasNext(); ) {
                JsonNode rate = it.next();
                sum += rate.get("mid").asDouble();
                count++;
            }

            if (count == 0) throw new RuntimeException("Brak kursów w odpowiedzi NBP.");

            double average = sum / count;


            ExchangeRateRequest request = new ExchangeRateRequest();
            request.setCurrency(currency.toUpperCase());
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setAverageRate(average);
            request.setRequestDateTime(LocalDateTime.now());

            repository.save(request);

            return average;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono danych dla podanej waluty lub zakresu dat");
            } else if (e.getStatusCode().value() == 400) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nieprawidłowy format danych (np. błędna data lub waluta)");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd HTTP z NBP: " + e.getMessage());
            }
        } catch (RestClientException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd pobierania lub parsowania danych z NBP: " + e.getMessage());
        }
    }
}
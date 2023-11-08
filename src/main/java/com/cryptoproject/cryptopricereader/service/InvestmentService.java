package com.cryptoproject.cryptopricereader.service;

import com.cryptoproject.cryptopricereader.entity.CryptoCoin;
import com.cryptoproject.cryptopricereader.dto.CryptoStatsDTO;
import com.cryptoproject.cryptopricereader.exceptions.TooManyRequestsException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InvestmentService {
    @Autowired

    private CsvReaderService csvReaderService;
    @Value("${crypto.file.names}")

    private String cryptoFileNames;
    private final Map<String, Map<String, CryptoCoin>> cryptoStatsMap = new HashMap<>();
    private final Bucket bucket;
    private static final String OLDEST = "oldest";
    private static final String NEWEST = "newest";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final Logger logger = LoggerFactory.getLogger(InvestmentService.class);

    @Autowired
    public InvestmentService(Bucket bucket) {
        this.bucket = bucket;
    }

    /**
     * Initializes the crypto stats map by calculating statistics from the specified file names.
     *
     * @throws IOException If an I/O error occurs while processing the files.
     */
    @PostConstruct
    public void initialize() throws IOException {
        String[] fileNames = cryptoFileNames.split(",");
        calculateStats(fileNames);
    }

    /**
     * Calculates statistics (oldest, newest, min, max) for each cryptocurrency.
     *
     * @param coinNames An array of cryptocurrency symbols.
     */
    public void calculateStats(String[] coinNames) {
        for (String symbol : coinNames) {
            List<CryptoCoin> cryptoCoins = csvReaderService.getCryptoCoinMap().getOrDefault(symbol, Collections.emptyList());

            if (cryptoCoins != null && !cryptoCoins.isEmpty()) {
                cryptoCoins.sort(Comparator.comparing(CryptoCoin::getTimestamp));
                CryptoCoin oldestCoin = cryptoCoins.get(0);
                CryptoCoin newestCoin = cryptoCoins.get(cryptoCoins.size() - 1);
                CryptoCoin minCoin = cryptoCoins.stream().min(Comparator.comparing(CryptoCoin::getPrice)).orElse(null);
                CryptoCoin maxCoin = cryptoCoins.stream().max(Comparator.comparing(CryptoCoin::getPrice)).orElse(null);

                Map<String, CryptoCoin> result = new HashMap<>();
                result.put(OLDEST, oldestCoin);
                result.put(NEWEST, newestCoin);
                result.put(MIN, minCoin);
                result.put(MAX, maxCoin);
                cryptoStatsMap.put(symbol, result);
            }
        }
    }

    /**
     * Calculates the normalized range for each cryptocurrency and returns a sorted list.
     *
     * @return A sorted list of CryptoStatsDTO objects based on volatility index.
     */
    public List<CryptoStatsDTO> calculateNormalizedRange() {
        if (bucket.tryConsume(1)) {
            List<CryptoStatsDTO> coins = new ArrayList();
            for (String symbol : cryptoFileNames.split(",")) {
                Map<String, CryptoCoin> statsMap = cryptoStatsMap.get(symbol);
                CryptoCoin max = statsMap.get(MAX);
                CryptoCoin min = statsMap.get(MIN);
                double volatilityIndex = (max.getPrice() - min.getPrice()) / min.getPrice();
                coins.add(CryptoStatsDTO.builder().symbol(symbol).volatilityIndex(volatilityIndex).build());
            }
            return coins.stream()
                    .sorted(Comparator.comparing(CryptoStatsDTO::getVolatilityIndex).reversed())
                    .collect(Collectors.toList());
        } else {
            throw new TooManyRequestsException("Rate limit exceeded");
        }
    }

    /**
     * Calculates the cryptocurrency with the highest volatility on a specific day.
     *
     * @param targetDay The target day for calculating volatility.
     * @return A CryptoStatsDTO object representing the cryptocurrency with the highest volatility on the specified day.
     */
    public CryptoStatsDTO calculateHighestVolatilityForDay(Integer targetDay) {
        if (bucket.tryConsume(1)) {
            Map<String, List<CryptoCoin>> coins = csvReaderService.getCryptoCoinMap().values().stream()
                    .flatMap(List::stream)
                    .filter(coin -> coin.getTimestamp().getDayOfMonth() == targetDay)
                    .collect(Collectors.groupingBy(CryptoCoin::getSymbol));

            return coins.entrySet().stream()
                    .map(this::getHighestVolatilityCryptoByDay)
                    .max(Comparator.comparing(CryptoStatsDTO::getVolatilityIndex))
                    .orElse(null);
        } else {
            throw new TooManyRequestsException("Rate limit exceeded");
        }
    }

    /**
     * Retrieves a list of cryptocurrency prices by symbol.
     *
     * @param name The symbol (name) of the cryptocurrency.
     * @return A list of CryptoCoin objects containing price data for the specified cryptocurrency.
     */
    public List<CryptoCoin> getCryptoByName(String name) {
        if (bucket.tryConsume(1)) {
            if (cryptoFileNames.contains(name)) {
                return csvReaderService.getCryptoCoinMap().getOrDefault(name, Collections.emptyList());
            } else {
                throw new RuntimeException("The specified Crypto currency is not supported by the service");
            }
        } else {
            throw new TooManyRequestsException("Rate limit exceeded");
        }
    }

    /**
     * Retrieves statistics (oldest, newest, min, max) for a specific cryptocurrency by symbol.
     *
     * @param name The symbol (name) of the cryptocurrency.
     * @return A map containing statistics for the specified cryptocurrency.
     */
    public Map<String, CryptoCoin> getCryptoStatsByName(String name) {
        if (bucket.tryConsume(1)) {
            if (cryptoFileNames.contains(name)) {
                return cryptoStatsMap.get(name);
            } else {
                throw new RuntimeException("The specified Crypto currency is not supported by the service");
            }
        } else {
            throw new TooManyRequestsException("Rate limit exceeded");
        }
    }

    /**
     * Retrieves a collection of lists, each containing cryptocurrency price data.
     *
     * @return A collection of lists, each containing cryptocurrency price data.
     */
    public Collection<List<CryptoCoin>> getAllPrices() {
        if (bucket.tryConsume(1)) {
            return csvReaderService.getCryptoCoinMap().values();
        } else {
            throw new TooManyRequestsException("Rate limit exceeded");
        }
    }

    /**
     * Retrieves a map containing cryptocurrency symbols as keys and their corresponding statistics as values.
     *
     * @return A map containing cryptocurrency symbols as keys and their corresponding statistics as values.
     */
    public Map<String, Map<String, CryptoCoin>> getAllStats() {
        if (bucket.tryConsume(1)) {
            return cryptoStatsMap;
        } else {
            throw new TooManyRequestsException("Rate limit exceeded");
        }
    }

    /**
     * Gets the highest volatility cryptocurrency for a specific day.
     *
     * @param entry A map entry containing a cryptocurrency symbol and its corresponding list of CryptoCoins.
     * @return A CryptoStatsDTO object representing the cryptocurrency with the highest volatility for the specified day.
     */
    private CryptoStatsDTO getHighestVolatilityCryptoByDay(Map.Entry<String, List<CryptoCoin>> entry) {
        String symbol = entry.getKey();
        double volatilityIndex = 0.0;
        List<CryptoCoin> cryptoCoins = entry.getValue();
        CryptoCoin min = cryptoCoins.stream().min(Comparator.comparing(CryptoCoin::getPrice)).orElse(null);
        CryptoCoin max = cryptoCoins.stream().max(Comparator.comparing(CryptoCoin::getPrice)).orElse(null);

        if (min != null && max != null && min.getPrice() != null) {
            volatilityIndex = (max.getPrice() - min.getPrice()) / min.getPrice();
        }
        return CryptoStatsDTO.builder().symbol(symbol).volatilityIndex(volatilityIndex).build();
    }
}

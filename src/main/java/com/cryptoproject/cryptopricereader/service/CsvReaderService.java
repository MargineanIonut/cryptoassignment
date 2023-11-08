package com.cryptoproject.cryptopricereader.service;

import com.cryptoproject.cryptopricereader.entity.CryptoCoin;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvReaderService {

    @Value("${csv.file.suffix}")
    private String csvFileSuffix;

    @Value("${csv.file.path}")
    private String csvFilePath;

    @Value("${crypto.file.names}")
    private String cryptoFileNames;

    private final Map<String, List<CryptoCoin>> cryptoCoinMap = new HashMap<>();


    private static final Logger logger = LoggerFactory.getLogger(InvestmentService.class);


    /**
     * This method is annotated with @PostConstruct, which means it will be automatically
     * executed after the bean is constructed and all its dependencies are injected.
     * It reads CSV files specified in the 'cryptoFileNames' property, and calculates statistics.
     *
     * @throws IOException If an I/O error occurs while reading the CSV files.
     */
    @PostConstruct
    public void initialize() throws IOException {
        String[] fileNames = cryptoFileNames.split(",");
        readCsv(fileNames);
    }

    /**
     * Reads cryptocurrency data from CSV files and populates the cryptoCoinMap.
     *
     * @param fileNames An array of file names (symbols) to read.
     * @throws IOException If an I/O error occurs while reading the CSV files.
     */
    public void readCsv(String[] fileNames) throws IOException {
        for (String fileName : fileNames) {
            List<CryptoCoin> cryptoCoins = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new FileReader(csvFilePath + fileName + csvFileSuffix))) {
                reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                    long timestampMillis = Long.parseLong(line[0]);
                    Instant instant = Instant.ofEpochMilli(timestampMillis);
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                    CryptoCoin coin = CryptoCoin.builder()
                            .timestamp(dateTime)
                            .symbol(line[1])
                            .price(Double.parseDouble(line[2]))
                            .build();

                    cryptoCoins.add(coin);
                }
            } catch (IOException | CsvException | NumberFormatException e) {
                logger.error("Error occurred while processing CSV: {}", e.getMessage(), e);
            }
            cryptoCoinMap.put(fileName, cryptoCoins);
        }
    }

    /**
     * Retrieves a mapping of String keys to lists of CryptoCoin objects.
     *
     * @return A Map containing String keys and corresponding lists of CryptoCoin objects.
     */
    public Map<String, List<CryptoCoin>> getCryptoCoinMap() {
        return cryptoCoinMap;
    }
}

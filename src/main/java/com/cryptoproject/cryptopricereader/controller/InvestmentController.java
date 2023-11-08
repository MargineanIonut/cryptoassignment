package com.cryptoproject.cryptopricereader.controller;

import com.cryptoproject.cryptopricereader.dto.CryptoStatsDTO;
import com.cryptoproject.cryptopricereader.entity.CryptoCoin;
import com.cryptoproject.cryptopricereader.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crypto")
public class InvestmentController {


    @Autowired
    private InvestmentService investmentService;

    /**
     * Retrieves all cryptocurrency prices.
     *
     * @return A collection of lists, each containing cryptocurrency price data.
     */
    @GetMapping("/all")
    public Collection<List<CryptoCoin>> getAllPrices(){
        return investmentService.getAllPrices();
    }

    /**
     * Retrieves statistics for all cryptocurrencies.
     *
     * @return A map containing cryptocurrency symbols as keys and their corresponding statistics as values.
     */
    @GetMapping("/all/stats")
    public Map<String, Map<String, CryptoCoin>> getAllStats(){
        return investmentService.getAllStats();
    }

    /**
     * Retrieves a sorted list of cryptocurrencies based on their normalized range.
     *
     * @return A list of CryptoStatsDTO objects sorted by volatility index in descending order.
     */
    @GetMapping("/sorted")
    public List<CryptoStatsDTO> getSortedCryptos() {
        return investmentService.calculateNormalizedRange();
    }

    /**
     * Retrieves the cryptocurrency with the highest volatility on a specific day.
     *
     * @param targetDay The target day for calculating volatility.
     * @return A CryptoStatsDTO object representing the cryptocurrency with the highest volatility on the specified day.
     */
    @GetMapping("/highestVolatility/{targetDay}")
    public CryptoStatsDTO getHighestVolatileCoin(@PathVariable Integer targetDay){
        return investmentService.calculateHighestVolatilityForDay(targetDay);
    }

    /**
     * Retrieves cryptocurrency prices by symbol.
     *
     * @param symbol The symbol of the cryptocurrency.
     * @return A list of CryptoCoin objects containing price data for the specified cryptocurrency.
     */
    @GetMapping("/{symbol}/prices")
    public List<CryptoCoin> getCryptoPricesBySymbol(@PathVariable String symbol) {
        return investmentService.getCryptoByName(symbol);
    }


    /**
     * Retrieves statistics for a specific cryptocurrency by symbol.
     *
     * @param symbol The symbol of the cryptocurrency.
     * @return A map containing statistics (oldest, newest, min, max) for the specified cryptocurrency.
     */
    @GetMapping("/{symbol}/stats")
    public Map<String, CryptoCoin> getCryptoStats(@PathVariable String symbol) {
        return investmentService.getCryptoStatsByName(symbol);
    }

}

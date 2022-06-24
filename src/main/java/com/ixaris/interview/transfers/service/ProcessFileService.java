package com.ixaris.interview.transfers.service;

import com.ixaris.interview.transfers.exception.CSVParseException;
import com.ixaris.interview.transfers.model.Transaction;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a service class to parse, process and compute the business logic for the given csv file
 */
@Service
@Log4j2
public class ProcessFileService {

    @Value("${spring.file.headers}")
    private String header;

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    /**
     * Process the input csv file
     *
     * @param fileName
     */
    public void processFile(final String fileName) {
        log.log(Level.INFO, "processing file: {}", fileName);

        final List<String> lines = parseCSV(fileName);

        final List<Transaction> transactions = convertToTransaction(lines);

        final Map<Long, Double> accountBalances = getAccountBalances(transactions);
        System.out.println();
        System.out.println("#Balances");
        final DecimalFormat df = new DecimalFormat("0.00");
        for (final Map.Entry<Long, Double> account : accountBalances.entrySet()) {
            System.out.println(account.getKey() + " - " + df.format(account.getValue()));
        }
        System.out.println();

        final long maxAccountBalance = computeMaxAccountBalance(accountBalances);
        System.out.println("#Bank Account with highest balance");
        System.out.println(maxAccountBalance);
        System.out.println();

        final long frequentSourceAccount = computeFrequentSource(transactions);
        System.out.println("#Frequently used source bank account");
        System.out.println(frequentSourceAccount);
        System.out.println();
    }

    /**
     * Parse the csv file and returns the list of string
     *
     * @param fileName the csv file
     * @return list of strings
     */
    public List<String> parseCSV(final String fileName) {
        log.log(Level.DEBUG, "parsing the file: {}", fileName);

        final URL file = getClass().getClassLoader().getResource(fileName);
        if (file == null)
            throw new CSVParseException("Failed to read file: " + fileName);
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(file.toURI()));
        } catch (final IOException | URISyntaxException e) {
            log.log(Level.ERROR, "Failed to parse csv file with an exception: {}", e.getMessage());
            throw new CSVParseException("Failed to parse csv file with an exception message: " + e.getMessage());
        }
        return lines;
    }

    /**
     * Returns the max account balance
     *
     * @param accountBalances map of account and balances
     * @return account id
     */
    public long computeMaxAccountBalance(final Map<Long, Double> accountBalances) {
        log.log(Level.DEBUG, "computing the maximum account balance");
        return accountBalances.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == Collections.max(accountBalances.values()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1L);
    }

    /**
     * Returns the most frequent source account
     *
     * @param transactions the list of transactions
     * @return account id
     */
    public long computeFrequentSource(final List<Transaction> transactions) {
        log.log(Level.DEBUG, "computing the frequent source account");

        final HashMap<Long, Long> sourceFrequency = new HashMap<>();
        transactions.forEach(transaction -> {
            if (0L != transaction.getSourceAccountID()) {
                sourceFrequency.put(transaction.getSourceAccountID(), sourceFrequency.getOrDefault(transaction.getSourceAccountID(), 0L) + 1L);
            }
        });

        return sourceFrequency.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == Collections.max(sourceFrequency.values()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1L);
    }

    /**
     * Returns the accounts and corresponding balances
     *
     * @param transactions the list of transactions
     * @return map of account and balance
     */
    private static Map<Long, Double> getAccountBalances(final List<Transaction> transactions) {
        log.log(Level.DEBUG, "fetching the accounts and their balances");

        final Map<Long, Double> accountBalance = new TreeMap<>();
        transactions.forEach(transaction -> {
            if (0L != transaction.getSourceAccountID()) {
                accountBalance.put(transaction.getSourceAccountID(), accountBalance.getOrDefault(transaction.getSourceAccountID(), 0.0d) - transaction.getAmount());
            }
            accountBalance.put(transaction.getDestinationAccountID(), accountBalance.getOrDefault(transaction.getDestinationAccountID(), 0.0d) + transaction.getAmount());
        });
        return accountBalance;
    }

    /**
     * Converts the list of line strings to the list of transaction objects
     *
     * @param lines the list of lines
     * @return the list of transaction objects
     */
    private List<Transaction> convertToTransaction(final List<String> lines) {

        final List<Transaction> transactions = new ArrayList<>();
        for (String line : lines) {

            // remove whitespaces
            line = line.replaceAll("\\s+", "");

            final String[] entry = line.split(",");

            // ignore comment and header
            if (entry[0].startsWith("#") || header.equalsIgnoreCase(line)) {
                continue;
            }
            final Transaction transaction = new Transaction();
            transaction.setSourceAccountID(Long.parseLong(entry[0]));
            transaction.setDestinationAccountID(Long.parseLong(entry[1]));
            transaction.setAmount(Double.parseDouble(entry[2]));
            final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            try {
                transaction.setDate(format.parse(entry[3]));
            } catch (final ParseException e) {
                log.log(Level.ERROR, "Failed to parse date with an exception: {}", e.getMessage());
                throw new CSVParseException("Failed to parse date with an exception: " + e.getMessage());
            }
            transaction.setTransferId(Long.parseLong(entry[4]));
            transactions.add(transaction);
        }
        return transactions;
    }

}

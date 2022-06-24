package com.ixaris.interview.transfers;

import com.ixaris.interview.transfers.exception.CSVParseException;
import com.ixaris.interview.transfers.model.Transaction;
import com.ixaris.interview.transfers.service.ProcessFileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Represents a test class to test csv parse and related business logic
 */
public class ProcessFileServiceTest {

    ProcessFileService processFileService = new ProcessFileService();

    /**
     * Tests the csv parse operations
     */
    @Test
    public void testParseCSV() {

        List<String> lines = processFileService.parseCSV("transfers.txt");
        assertNotNull(lines);

        // check the expected number of lines in the parsed csv file
        assertEquals(9, lines.size());

        assertEquals("SOURCE_ACCT, DESTINATION_ACCT, AMOUNT, DATE, TRANSFERID", lines.get(2));
    }

    @Test
    public void testMaxAccountBalance() {

        Map<Long, Double> accountBalances = new HashMap<>();
        accountBalances.put(202L, 5444.0D);
        accountBalances.put(55L, 3444.0D);
        accountBalances.put(454L, 6444.0D);

        long maxAccountBalance = processFileService.computeMaxAccountBalance(accountBalances);

        // check the expected max account balance
        assertEquals(454L, maxAccountBalance);
    }

    @Test
    public void testFrequentSource() {

        List<Transaction> transactions = new ArrayList<>();

        Transaction transaction1 = new Transaction();
        transaction1.setTransferId(202L);
        transaction1.setSourceAccountID(3334L);
        transaction1.setDestinationAccountID(533L);
        transaction1.setAmount(10000D);
        transactions.add(transaction1);

        Transaction transaction2 = new Transaction();
        transaction2.setTransferId(303L);
        transaction2.setSourceAccountID(5553L);
        transaction2.setDestinationAccountID(2998L);
        transaction2.setAmount(3500D);
        transactions.add(transaction2);

        Transaction transaction3 = new Transaction();
        transaction3.setTransferId(404L);
        transaction3.setSourceAccountID(3334L);
        transaction3.setDestinationAccountID(5553L);
        transaction3.setAmount(7000D);
        transactions.add(transaction3);

        long frequentSource = processFileService.computeFrequentSource(transactions);

        // check the expected frequent source
        assertEquals(3334L, frequentSource);
    }


    @Test
    public void testCSVParseException() {

        CSVParseException thrown = assertThrows(CSVParseException.class, () -> processFileService.parseCSV("transfers22.txt"));

        // check the expected exception message
        Assertions.assertEquals("Failed to read file: transfers22.txt", thrown.getMessage());
    }
}

package com.ixaris.interview.transfers.model;


import lombok.Data;

import java.util.Date;

@Data
public class Transaction {
    private long sourceAccountID;
    private long destinationAccountID;
    private double amount;
    private Date date;
    private long transferId;
}

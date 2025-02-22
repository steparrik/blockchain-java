package org.steparrik.client.service.transaction;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.steparrik.client.models.DataType;
import org.steparrik.client.models.transaction.Transaction;
import org.steparrik.client.models.transaction.TransactionInput;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionInputService transactionInputService;
    private final TransactionOutputService transactionOutputService;

    @Autowired
    public TransactionService(TransactionInputService transactionInputService, TransactionOutputService transactionOutputService) {
        this.transactionInputService = transactionInputService;
        this.transactionOutputService = transactionOutputService;
    }


    @SneakyThrows
    public String calculateTransactionHash(Transaction transaction) {
        String dataToHash = transaction.getHash()
                + transaction.getTimestamp()
                + transaction.getFee()
                + transaction.getOutputs()
                + transaction.getInputs();

        MessageDigest digest = null;
        byte[] bytes = null;
        digest = MessageDigest.getInstance("SHA-256");
        bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));

        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            buffer.append(String.format("%02x", b));
        }

        return buffer.toString();
    }

    public Transaction createTransaction(String from, String to, BigDecimal amount, String pubKey, String privateKey){
        List<TransactionInput> transactionInputs = transactionInputService.createInputs(from, amount, pubKey, privateKey);
        if(transactionInputs.isEmpty()){
            System.out.println("Not enough funds");
            return null;
        }
        Transaction transaction = new Transaction(DataType.TRANSACTION,
                "0",
                transactionInputs,
                transactionOutputService.createOutputs(from, to, amount, transactionInputs),
                new Date().getTime(),
                BigDecimal.ZERO
        );
        transaction.setHash(calculateTransactionHash(transaction));
        return transaction;
    }
}

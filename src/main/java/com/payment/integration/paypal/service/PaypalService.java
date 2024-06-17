package com.payment.integration.paypal.service;

import com.payment.integration.paypal.model.request.PaymentReq;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaypalService {

    private final APIContext apiContext;

    @Value("${paypal.success-url}")
    private String S_URL;
    @Value("${paypal.cancel-url}")
    private String C_URL;


    public Payment createPayment(PaymentReq paymentReq) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(paymentReq.getCurrency());
        amount.setTotal(String.format(Locale.forLanguageTag(paymentReq.getCurrency()),"%.2f",paymentReq.getTotal()));

        Transaction transaction = new Transaction();
        transaction.setCustom("Test");
        transaction.setDescription(paymentReq.getDescription());
        transaction.setAmount(amount);

        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(paymentReq.getMethod());

        Payment payment = new Payment();
        payment.setIntent(paymentReq.getIntent());
        payment.setTransactions(transactionList);
        payment.setPayer(payer);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(C_URL);
        redirectUrls.setReturnUrl(S_URL);

        payment.setRedirectUrls(redirectUrls);
        return payment.create(apiContext);


    }

    public Payment executePayment(String paymentId, String payerId) {

        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();

        paymentExecution.setPayerId(payerId);

        Payment execute;
        try {
            execute = payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }
        return execute;
    }
}

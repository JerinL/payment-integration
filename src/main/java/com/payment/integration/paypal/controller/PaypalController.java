package com.payment.integration.paypal.controller;

import com.payment.integration.paypal.model.request.PaymentReq;
import com.payment.integration.paypal.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/paypal-payment")
@RequiredArgsConstructor
public class PaypalController {

    private static final Logger log = LoggerFactory.getLogger(PaypalController.class);
    private final PaypalService paypalService;

    @GetMapping("/success")
    public String home(){
        return "success";
    }

    @GetMapping("/cancel")
    public String error(){
        return "payment Cancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }


//    @PostMapping("/create")
//    public RedirectView createPayment(@RequestBody PaymentReq paymentReq){
//        System.out.println(paymentReq);
//        try{
//            Payment payment = paypalService.createPayment(paymentReq);
//            for(Links links : payment.getLinks()){
//                if(links.equals("approval_url")){
//                    new RedirectView(links.getHref());
//                }
//            }
//        }catch (PayPalRESTException e){
//            log.error("Error Occurred :: ",e);
//        }
//       return  null;
//    }

    @PostMapping("/create")
    public RedirectView createPayment(@RequestBody PaymentReq paymentReq) {
        try {
            log.info("Payment request received: {}", paymentReq);
            Payment payment = paypalService.createPayment(paymentReq);
            for (Links links : payment.getLinks()) {
                if ("approval_url".equals(links.getRel())) {
                    return new RedirectView(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error Occurred :: ", e);
        }
        return new RedirectView("/payment/error");
    }

    @PostMapping("/execute")
    public String executePayment(@RequestParam(value = "payment_id",required = true) String paymentId,
                                 @RequestParam(value = "payer_id",required = true) String payerId){
        Payment payment = paypalService.executePayment(paymentId, payerId);
        if(payment.getState().equals("approved")){
            return "payment successful";
        }
        return "paymentSuccess";
    }
}

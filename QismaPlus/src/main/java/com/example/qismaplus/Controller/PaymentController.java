package com.example.qismaplus.Controller;

import com.example.qismaplus.API.ApiResponse;
import com.example.qismaplus.Model.Payment;
import com.example.qismaplus.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.status(200).body(payments);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return ResponseEntity.status(200).body(new ApiResponse("payment deleted successfully"));
    }

    @PostMapping("/make/{userId}/{paymentId}")
    public ResponseEntity<?> makePayment(@PathVariable Integer userId, @PathVariable Integer paymentId) {
        paymentService.makePayment(userId, paymentId);
        return ResponseEntity.status(200).body(new ApiResponse("payment sent for approval (PENDING)"));
    }

    @PutMapping("/verify/{adminId}/{paymentId}")
    public ResponseEntity<?> verifyPayment(@PathVariable Integer adminId, @PathVariable Integer paymentId) {
        paymentService.verifyPayment(adminId, paymentId);
        return ResponseEntity.status(200).body(new ApiResponse("payment verified successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(paymentService.getPaymentsByUserId(userId));
    }

    @GetMapping("/contribution/{contributionId}")
    public ResponseEntity<?> getPaymentsByContributionId(@PathVariable Integer contributionId) {
        return ResponseEntity.status(200).body(paymentService.getPaymentsByContributionId(contributionId));
    }

    @GetMapping("/unpaid/{userId}")
    public ResponseEntity<?> getUnpaidPaymentsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(paymentService.getUnpaidPaymentsByUserId(userId));
    }

    @GetMapping("/commitment/{userId}/{contributionId}")
    public ResponseEntity<?> getCommitmentRate(@PathVariable Integer userId, @PathVariable Integer contributionId) {
        return ResponseEntity.status(200).body(paymentService.getCommitmentRate(userId, contributionId));
    }

    @GetMapping("/late-users/{groupId}")
    public ResponseEntity<?> getLateUsers(@PathVariable Integer groupId){
        return ResponseEntity.status(200).body(paymentService.getLatePaymentsByGroup(groupId));
    }


    @GetMapping("/summary/{userId}")
    public ResponseEntity<?> getSummary(@PathVariable Integer userId) {
        return ResponseEntity.ok(paymentService.getPaymentSummary(userId));
    }
    @PostMapping("/pay-for-member/{payerId}/{paymentId}")
    public ResponseEntity<?> payForMember(@PathVariable Integer payerId, @PathVariable Integer paymentId) {
        paymentService.payForMember(payerId, paymentId);
        return ResponseEntity.ok(new ApiResponse("payment submitted for another member (PENDING)"));
    }
    @GetMapping("/payment-streak/{userId}")
    public ResponseEntity<?> getPaymentStreak(@PathVariable Integer userId) {
        return ResponseEntity.ok(paymentService.getPaymentStreak(userId));
    }
}
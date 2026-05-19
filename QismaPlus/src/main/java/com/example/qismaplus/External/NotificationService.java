package com.example.qismaplus.External;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WhatsAppService whatsAppService;
    private final EmailService emailService;  // ← أضفها

    public void sendPaymentVerified(String phone, String email, String userName, double amount) {
        String msg = "Hello " + userName + ", your payment of " + amount + " SAR has been verified.";
        whatsAppService.sendMessage(phone, msg);
        emailService.sendEmail(email, "Payment Verified ", msg);  // ← فعّلها
    }

    public void sendPaymentPending(String phone, String email, String userName, double amount) {
        String msg = "Hello " + userName +
                ", your payment of " + amount +
                " SAR has been submitted and is pending admin approval.";

        whatsAppService.sendMessage(phone, msg);
        emailService.sendEmail(email, "Payment Pending Approval", msg);
    }
    public void sendAdminPaymentAlert(String phone, String email, String adminName, String userName, double amount) {

        String msg = "Hello " + adminName +
                ", user " + userName +
                " has made a payment of " + amount +
                " SAR and it is pending your approval.";

        whatsAppService.sendMessage(phone, msg);
        emailService.sendEmail(email, "New Payment Pending Approval", msg);
    }

    public void sendGroupPaymentPaid(String phone, String email, String userName, double amount, String paidBy) {
        String msg = "Hello " + userName +
                ", your payment of " + amount +
                " SAR has been paid by " + paidBy +
                " and is pending admin approval.";

        whatsAppService.sendMessage(phone, msg);
        emailService.sendEmail(email, "Group Payment Submitted", msg);
    }

    public void sendLowBudgetWarning(String phone, String email, String userName, double budget, double required) {
        String msg = "Hello " + userName + ", your budget is " + budget + " SAR which is less than required " + required + " SAR.";
        whatsAppService.sendMessage(phone, msg);
        emailService.sendEmail(email, "Low Budget Warning", msg);
    }
}
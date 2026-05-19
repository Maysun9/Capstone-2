package com.example.qismaplus.External;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;



@Service
public class WhatsAppService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String fromNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }
    public void sendMessage(String toMobile, String body) {
        Message.creator(
                new com.twilio.type.PhoneNumber("whatsapp:+966" + toMobile.substring(1)),
                new PhoneNumber("whatsapp:" + fromNumber),
                body
        ).create();
    }
    }


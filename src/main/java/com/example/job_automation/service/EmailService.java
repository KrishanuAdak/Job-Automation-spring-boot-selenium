package com.example.job_automation.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Value("")
    private String fromEmail;
    @Value("")
    private String toEmail;
    private final String subject="Job Application Update - "+ System.currentTimeMillis();

    public void sendEmail( File excelFile) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.addAttachment("Jobs_Applied_"+System.currentTimeMillis(), excelFile);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText("Please find attached the Excel file containing the details of the jobs applied to.");
            mailSender.send(message);

            // mess.setFrom(fromEmail);
            // message.setRecipients(MimeMessage.RecipientType.TO, toEmail);
            // message.setSubject(subject);
            // // message.setText(body);
            // message.setatta
           // mailSender.send(message);
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

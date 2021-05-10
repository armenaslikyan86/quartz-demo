package com.example.quartzdemo.job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Component
public class EmailJob extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(EmailJob.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("Executing job with key {}", jobExecutionContext.getJobDetail().getKey());

        final JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        String subject = (String) jobDataMap.get("subject");
        String body = (String) jobDataMap.get("body");
        String email = (String) jobDataMap.get("email");

        sendEmail(mailProperties.getUsername(), email, subject, body);

    }

    private void sendEmail(final String fromEmail, final String toEmail, final String subject, final String body) {

        logger.info("Sending email to {}", toEmail);

        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.toString());
        try {
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body);
            mimeMessageHelper.setFrom(fromEmail);
            mimeMessageHelper.setTo(toEmail);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", toEmail);
        }
    }
}

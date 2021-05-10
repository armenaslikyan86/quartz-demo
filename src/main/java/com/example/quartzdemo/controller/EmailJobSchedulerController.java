package com.example.quartzdemo.controller;

import com.example.quartzdemo.job.EmailJob;
import com.example.quartzdemo.payload.ScheduleEmailRequest;
import com.example.quartzdemo.payload.ScheduleEmailResponse;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
public class EmailJobSchedulerController {

    private static final Logger logger = LoggerFactory.getLogger(EmailJobSchedulerController.class);

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/scheduleEmail")
    public ResponseEntity<ScheduleEmailResponse> scheduleEmail(@Valid @RequestBody ScheduleEmailRequest scheduleEmailRequest) {

        final ZonedDateTime dateTime = ZonedDateTime.of(scheduleEmailRequest.getDateTime(), scheduleEmailRequest.getTimeZone());

        if (dateTime.isBefore(ZonedDateTime.now())) {
            final ScheduleEmailResponse scheduleEmailResponse = new ScheduleEmailResponse(false, "dateTime must be after current time");
            return ResponseEntity.badRequest().body(scheduleEmailResponse);
        }

        final JobDetail jobDetail = buildJobDetail(scheduleEmailRequest);

        final Trigger trigger = buildJobTrigger(jobDetail, dateTime);

        try {
            scheduler.scheduleJob(jobDetail, trigger);

            final ScheduleEmailResponse scheduleEmailResponse = new ScheduleEmailResponse(true, jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email sent successfully!");
            return ResponseEntity.ok(scheduleEmailResponse);
        } catch (final SchedulerException ex) {

            logger.error("Error scheduling email", ex);
            final ScheduleEmailResponse scheduleEmailResponse = new ScheduleEmailResponse(false, "Error scheduling email. Please try later!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(scheduleEmailResponse);
        }

    }

    private JobDetail buildJobDetail(final ScheduleEmailRequest scheduleEmailRequest) {

        final JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send email job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(final JobDetail jobDetail, final ZonedDateTime dateTime) {

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send email trigger")
                .startAt(Date.from(dateTime.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}

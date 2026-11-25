package com.trevor.sica.demo;

import com.trevor.sica.demo.DemoData.Person;
import com.trevor.sica.demo.DemoData.Status;

import java.time.LocalDateTime;

public record TemporaryPass(String code, Person person, String reason, LocalDateTime start, LocalDateTime end,
                            String issuer, String reviewer, LocalDateTime reviewedAt, Status status) {
    public boolean isValidAt(LocalDateTime time) {
        return status == Status.APPROVED && !time.isBefore(start) && time.isBefore(end);
    }
}

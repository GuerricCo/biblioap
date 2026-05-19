package com.bibli.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MemberTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Member getMemberSample1() {
        return new Member().id(1L).firstName("firstName1").lastName("lastName1").email("email1").phone("phone1");
    }

    public static Member getMemberSample2() {
        return new Member().id(2L).firstName("firstName2").lastName("lastName2").email("email2").phone("phone2");
    }

    public static Member getMemberRandomSampleGenerator() {
        return new Member()
            .id(longCount.incrementAndGet())
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString());
    }
}

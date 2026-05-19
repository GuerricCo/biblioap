package com.bibli.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class LibraryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Library getLibrarySample1() {
        return new Library().id(1L).name("name1").address("address1").city("city1").phone("phone1").email("email1");
    }

    public static Library getLibrarySample2() {
        return new Library().id(2L).name("name2").address("address2").city("city2").phone("phone2").email("email2");
    }

    public static Library getLibraryRandomSampleGenerator() {
        return new Library()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString());
    }
}

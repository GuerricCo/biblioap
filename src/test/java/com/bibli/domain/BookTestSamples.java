package com.bibli.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BookTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Book getBookSample1() {
        return new Book()
            .id(1L)
            .title("title1")
            .isbn("isbn1")
            .description("description1")
            .language("language1")
            .pages(1)
            .totalCopies(1)
            .availableCopies(1);
    }

    public static Book getBookSample2() {
        return new Book()
            .id(2L)
            .title("title2")
            .isbn("isbn2")
            .description("description2")
            .language("language2")
            .pages(2)
            .totalCopies(2)
            .availableCopies(2);
    }

    public static Book getBookRandomSampleGenerator() {
        return new Book()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .isbn(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .language(UUID.randomUUID().toString())
            .pages(intCount.incrementAndGet())
            .totalCopies(intCount.incrementAndGet())
            .availableCopies(intCount.incrementAndGet());
    }
}

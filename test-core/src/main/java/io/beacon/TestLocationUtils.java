package io.beacon;

import com.github.javafaker.Faker;

public class TestLocationUtils {

    private static final Faker faker = new Faker();

    public static Double generateRandomLatitude() {
        // GEOADD bounds differ from normal latitude bounds https://redis.io/docs/latest/commands/geoadd/
        return (Double.parseDouble((faker.address().latitude()).replace(",",
                                                                        ".")) / 90) * 85;
    }

    public static Double generateRandomLongitude() {
        return Double.valueOf(faker.address().longitude().replace(",", "."));
    }
} 

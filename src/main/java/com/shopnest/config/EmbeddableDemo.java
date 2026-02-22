package com.shopnest.config;

import com.shopnest.entity.Address;
import com.shopnest.entity.DeliveryAddress;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EmbeddableDemo {

    // Runs on startup in dev profile only
    // Remove or comment out after verifying
    @Bean
    @Profile("dev")
    public CommandLineRunner demonstrateEmbeddable() {
        return args -> {
            System.out.println("\n======================================");
            System.out.println("  EC-015A — @Embeddable Demonstration");
            System.out.println("======================================");

            // Address is @Embeddable — no table, stored in users table
            Address userAddress = Address.builder()
                    .street("42, MG Road")
                    .city("Bengaluru")
                    .state("Karnataka")
                    .pincode("560001")
                    .build();

            System.out.println("\n@Embeddable Address:");
            System.out.println("  " + userAddress);
            System.out.println("  Stored as columns in USERS table:");
            System.out.println("  street, city, state, pincode");
            System.out.println("  No separate address table — no JOIN needed");

            // DeliveryAddress — snapshot at time of order
            DeliveryAddress delivery = DeliveryAddress.builder()
                    .street("15, Park Street")
                    .city("Kolkata")
                    .state("West Bengal")
                    .pincode("700016")
                    .build();

            System.out.println("\n@Embeddable DeliveryAddress (snapshot):");
            System.out.println("  " + delivery);
            System.out.println("  Stored as columns in ORDERS table:");
            System.out.println("  delivery_street, delivery_city,");
            System.out.println("  delivery_state, delivery_pincode");
            System.out.println("  Even if user changes address, order keeps original");

            System.out.println("\n@Embeddable vs @Entity:");
            System.out.println("  @Embeddable — no identity, no table, no FK");
            System.out.println("  @Entity     — has ID, has table, has FK");
            System.out.println("  Rule: if it has no meaning without parent → @Embeddable");
            System.out.println("======================================\n");
        };
    }
}
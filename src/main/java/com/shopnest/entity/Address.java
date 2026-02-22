package com.shopnest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * @Embeddable — Value Object pattern
 *
 * KEY CONCEPTS:
 * 1. No @Id — Address has no identity of its own
 * 2. No @Entity — no table created for Address
 * 3. @Embedded in UserEntity — columns added to users table
 * 4. Multiple embeddings — DeliveryAddress reuses same concept in orders
 * 5. Snapshot pattern — DeliveryAddress in Order is frozen at purchase time
 *
 * COLUMNS IN users TABLE:
 *   street VARCHAR(255)
 *   city   VARCHAR(100)
 *   state  VARCHAR(100)
 *   pincode VARCHAR(10)
 *
 * WHY NOT A SEPARATE TABLE?
 *   Address has no meaning without a User.
 *   No other entity references an Address directly.
 *   No need for a JOIN — simpler and faster.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " - " + pincode;
    }
}
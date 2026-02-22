package com.shopnest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

// Snapshot of address at time of order
// Stored as columns in orders table
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {

    @Column(name = "delivery_street", nullable = false, length = 255)
    private String street;

    @Column(name = "delivery_city", nullable = false, length = 100)
    private String city;

    @Column(name = "delivery_state", nullable = false, length = 100)
    private String state;

    @Column(name = "delivery_pincode", nullable = false, length = 10)
    private String pincode;

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " - " + pincode;
    }
}
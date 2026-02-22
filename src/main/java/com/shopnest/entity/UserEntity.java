package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email",  columnList = "email"),
                @Index(name = "idx_users_active", columnList = "active")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)            // store "CUSTOMER" not 0
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "active", nullable = false)
    private boolean active;

    // ── Embedded Address ──────────────────────────────
    // stored as street, city, state, pincode columns in users table
    @Embedded
    private Address address;

    // ── Business helpers ──────────────────────────────
    public String getFullName() {
        return firstName + (lastName != null ? " " + lastName : "");
    }

    public boolean isAdmin()  { return role == UserRole.ADMIN; }
    public boolean isSeller() { return role == UserRole.SELLER; }
}
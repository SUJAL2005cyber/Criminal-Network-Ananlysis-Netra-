package com.sih.demo.entity.mysql;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt hashed

    private String fullName;
    private String badgeNumber;

    @Enumerated(EnumType.STRING)
    private Role role; // INVESTIGATOR, ADMIN, ANALYST

    public enum Role { ADMIN, INVESTIGATOR, ANALYST }
}

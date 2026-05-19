package com.example.qismaplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "name can not be empty")
    @Pattern(regexp = "^[A-Za-z]+(\\s[A-Za-z]+)*$", message = "name must contain letters only")
    @Size(min = 2, max = 25, message = "name must be between 2 and 25 characters")
    @Column(columnDefinition = "varchar(25) not null ")
    private String name;

    @Email(message = "invalid email format")
    @Column(columnDefinition = "varchar(40) unique not null ")
    private String email;

    @Column(columnDefinition = "varchar(40)  not null ")
    private String phoneNumber;

    @NotEmpty(message = "Password can not be empty")
    @Size(min = 8, max = 25, message = "password must be between 8 and 25 characters")
    @Column(columnDefinition = "varchar(25) not null")
    private String password;

    @NotNull(message = "monthly budget can not be null")
    @PositiveOrZero(message = "monthly budget must be 0 or more")
    @Column(columnDefinition = "double not null")
    private Double monthlyBudget;


    @NotNull(message = "monthly income can not be null")
    @Positive(message = "monthly income must be greater than 0")
    @Column(columnDefinition = "double not null")
    private Double monthlyIncome;


    @NotEmpty(message = "role can not be empty")
    @Pattern(regexp = "^(USER|GROUP_ADMIN)$", message = "role must be USER or GROUP_ADMIN")
    @Column(columnDefinition = "varchar(20) not null check (role in ('USER','GROUP_ADMIN'))")
    private String role;
}

package com.example.qismaplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "title can not be empty")
    @Size(min = 4, max = 30, message = "title must be between 4 and 30 characters")
    @Column(columnDefinition = "varchar(30) not null")
    private String title;

    @NotNull(message = "amount can not be null")
    @Positive(message = "amount must be greater than 0")
    @Column(columnDefinition = "double not null")
    private Double amount;

    @NotEmpty(message = "category can not be empty")
    @Pattern(regexp = "^(FOOD|TRANSPORT|RENT|SHOPPING|ENTERTAINMENT|OTHER)$", message = "invalid category")
    @Column(columnDefinition = "varchar(20) not null")
    private String category;

    @NotNull(message = "date can not be null")
    @Column(columnDefinition = "date not null")
    private LocalDate date;

    @NotNull(message = "user id can not be null")
    @Column(columnDefinition = "int not null")
    private Integer userId;

}

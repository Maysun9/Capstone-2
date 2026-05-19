package com.example.qismaplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "title can not be empty")
    @Size(min = 2, max = 30, message = "title must be between 2 and 30 characters")
    @Column(columnDefinition = "varchar(30) not null")
    private String title;

    @NotNull(message = "total amount can not be null")
    @Positive(message = "total amount must be greater than 0")
    @Column(columnDefinition = "double not null")
    private Double totalAmount;

    @NotEmpty(message = "duration type can not be empty")
    @Pattern(regexp = "^(DAY|WEEK|MONTH|YEAR)$", message = "duration type must be DAY, WEEK, MONTH or YEAR")
    @Column(columnDefinition = "varchar(15) not null")
    private String durationType;

    @NotNull(message = "duration value can not be null")
    @Positive(message = "duration value must be greater than 0")
    @Column(columnDefinition = "int not null")
    private Integer durationValue;

    @NotNull(message = "monthly amount can not be null")
    @Positive(message = "monthly amount must be greater than 0")
    @Column(columnDefinition = "double not null")
    private Double monthlyAmount;

    @NotNull(message = "start date can not be null")
    private LocalDate startDate;

    @NotNull(message = "end date can not be null")
    private LocalDate endDate;

    @NotEmpty(message = "status can not be empty")
    @Pattern(regexp = "^(ACTIVE|COMPLETED|CANCELLED)$", message = "status must be ACTIVE, COMPLETED or CANCELLED")
    @Column(columnDefinition = "varchar(20) not null")
    private String status;

    @NotNull(message = "group id can not be null")
    @Column(columnDefinition = "int not null")
    private Integer groupId;


    @ElementCollection
    private List<Integer> participantsIds;}

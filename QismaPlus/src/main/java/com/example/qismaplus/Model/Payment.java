package com.example.qismaplus.Model;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "user id can not be null")
    @Column(columnDefinition = "int not null")
    private Integer userId;

    @NotNull(message = "contribution id can not be null")
    @Column(columnDefinition = "int not null")
    private Integer contributionId;

    @NotEmpty(message = "month can not be empty")
    @Column(columnDefinition = "varchar(20) not null")
    private String month;

    @NotNull(message = "amount can not be null")
    @Positive(message = "amount must be greater than 0")
    @Column(columnDefinition = "double not null")
    private Double amount;

    @NotEmpty(message = "status can not be empty")
    @Pattern(regexp = "^(PAID|UNPAID|PENDING)$", message = "status must be PAID, UNPAID or PENDING")
    @Column(columnDefinition = "varchar(15) not null")
    private String status;

    @Column(columnDefinition = "boolean not null")
    private Boolean verified;

    @Column(columnDefinition = "double")
    private Double latePenalty;

    @NotNull(message = "payment date can not be null")
    @Column(columnDefinition = "date not null")
    private LocalDate paymentDate;
}

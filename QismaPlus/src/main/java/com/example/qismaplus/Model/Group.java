package com.example.qismaplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "group name can not be empty")
    @Size(min = 4, max = 30, message = "group name must be between 4 and 30 characters")
    @Column(columnDefinition = "varchar(30) not null")
    private String name;

    @NotEmpty(message = "group type can not be empty")
    @Pattern(regexp = "^(TRIP|RENT|RESTAURANT|BILL|OTHER)$", message = "type must be TRIP, RENT, RESTAURANT, BILL or OTHER")
    @Column(columnDefinition = "varchar(20) not null")
    private String type;

    @NotNull(message = "start date can not be null")
    @Column(columnDefinition = "date not null")
    private LocalDate startDate;

    @NotNull(message = "end date can not be null")
    @Column(columnDefinition = "date not null")
    private LocalDate endDate;


    @NotNull(message = "UserId can not be null")
    @Column(columnDefinition = "int not null")
    private Integer createdByUserId;

    @ElementCollection
    private List<Integer> membersIds;
}

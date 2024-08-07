package com.loadbalancer.payment_gateway.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "banks")
@Data
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private boolean delFlag;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastUpdateTime;
    private String lastUpdateBy;
}

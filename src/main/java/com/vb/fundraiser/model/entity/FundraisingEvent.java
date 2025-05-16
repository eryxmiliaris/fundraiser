package com.vb.fundraiser.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fundraising_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundraisingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_currency", nullable = false)
    private Currency currency;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal accountBalance;
}

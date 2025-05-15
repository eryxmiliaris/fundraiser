package com.vb.fundraiser.model;

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

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_currency", nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal accountBalance;
}

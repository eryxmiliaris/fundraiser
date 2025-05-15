package com.vb.fundraiser.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "box_currency_amount", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_box", "id_currency"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoxCurrencyAmount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_box")
    private CollectionBox box;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_currency")
    private Currency currency;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;
}

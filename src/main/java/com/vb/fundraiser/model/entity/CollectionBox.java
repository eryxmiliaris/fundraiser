package com.vb.fundraiser.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collection_box", indexes = @Index(name = "box_deleted_index", columnList = "is_deleted"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_event")
    private FundraisingEvent event;

    @Builder.Default
    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxCurrencyAmount> amounts = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;
}

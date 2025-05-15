package com.vb.fundraiser.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collection_box")
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
}

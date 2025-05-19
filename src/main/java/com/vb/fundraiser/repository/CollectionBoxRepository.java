package com.vb.fundraiser.repository;

import com.vb.fundraiser.model.entity.CollectionBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionBoxRepository extends JpaRepository<CollectionBox, Long> {
    Optional<CollectionBox> findByIdAndIsDeletedFalse(Long id);
    Page<CollectionBox> findByIsDeletedFalse(Pageable pageable);
}

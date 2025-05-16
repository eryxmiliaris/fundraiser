package com.vb.fundraiser.repository;

import com.vb.fundraiser.model.entity.FundraisingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundraisingEventRepository extends JpaRepository<FundraisingEvent, Long> {
}

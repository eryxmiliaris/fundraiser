package com.vb.fundraiser.repository;

import com.vb.fundraiser.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
}

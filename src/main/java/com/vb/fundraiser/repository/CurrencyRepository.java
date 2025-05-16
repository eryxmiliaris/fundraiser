package com.vb.fundraiser.repository;

import com.vb.fundraiser.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByNameIgnoreCase(String currencyName);
}

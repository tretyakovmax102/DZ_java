package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Correction;

import java.util.List;

@Repository
public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    boolean existsByTransactionId(Long transactionId);

    void deleteByTransactionId(Long transactionId);

    List<Correction> findAll();
}

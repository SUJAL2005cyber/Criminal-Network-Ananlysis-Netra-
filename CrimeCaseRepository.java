package com.sih.demo.repository.mysql;

import com.sih.demo.entity.mysql.CrimeCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrimeCaseRepository extends JpaRepository<CrimeCase, Long> {
    CrimeCase findByCaseNumber(String caseNumber);
    boolean existsByCaseNumber(String caseNumber);
}

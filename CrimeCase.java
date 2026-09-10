package com.sih.demo.entity.mysql;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crime_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrimeCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caseNumber;   // e.g. FIR-118/2026
    private String title;

    @Column(length = 4000)
    private String rawReportText; // unstructured FIR / report text fed to NLP extractor

    private String status; // OPEN / UNDER_INVESTIGATION / CLOSED
    private LocalDateTime createdAt;
    private String createdBy;
}

package com.sih.demo.controller;

import com.sih.demo.entity.mysql.CrimeCase;
import com.sih.demo.repository.mysql.CrimeCaseRepository;
import com.sih.demo.service.NetworkAnalysisService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CrimeCaseController {

    private final CrimeCaseRepository repository;
    private final NetworkAnalysisService networkAnalysisService;

    @GetMapping
    public List<CrimeCase> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrimeCase> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CrimeCase> create(@RequestBody CreateCaseRequest request) {
        if (request.getCaseNumber() == null || request.getCaseNumber().isBlank()
                || request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (repository.existsByCaseNumber(request.getCaseNumber().trim())) {
            return ResponseEntity.status(409).build();
        }

        CrimeCase crimeCase = CrimeCase.builder()
                .caseNumber(request.getCaseNumber().trim())
                .title(request.getTitle().trim())
                .rawReportText(request.getRawReportText())
                .status("OPEN")
                .createdBy(request.getCreatedBy())
                .build();

        CrimeCase saved = repository.save(crimeCase);

        if (saved.getRawReportText() != null && !saved.getRawReportText().isBlank()) {
            networkAnalysisService.ingestReport(saved.getCaseNumber(), saved.getRawReportText());
        }
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrimeCase> update(@PathVariable Long id,
                                            @RequestBody CreateCaseRequest request) {
        return repository.findById(id).map(existing -> {
            if (request.getTitle() != null) existing.setTitle(request.getTitle().trim());
            if (request.getRawReportText() != null) existing.setRawReportText(request.getRawReportText());
            if (request.getCreatedBy() != null) existing.setCreatedBy(request.getCreatedBy());
            CrimeCase saved = repository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CreateCaseRequest {
        private String caseNumber;
        private String title;
        private String rawReportText;
        private String createdBy;
    }
}

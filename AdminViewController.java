package com.sih.demo.controller;

import com.sih.demo.dto.ExtractedEntitiesDto;
import com.sih.demo.dto.NetworkAnalysisResultDto;
import com.sih.demo.entity.mysql.CrimeCase;
import com.sih.demo.entity.mysql.User;
import com.sih.demo.entity.neo4j.PersonNode;
import com.sih.demo.repository.mysql.CrimeCaseRepository;
import com.sih.demo.repository.mysql.UserRepository;
import com.sih.demo.service.GraphCollectionsEngine;
import com.sih.demo.service.NetworkAnalysisService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Full server-rendered JSP frontend for NETRA — login, registration,
 * dashboard, case management, and network/graph analysis pages.
 *
 * This replaces the separate Angular frontend: everything here is
 * classic Spring MVC (Model + View) using JSP under
 * src/main/webapp/WEB-INF/views, with simple HttpSession-based auth
 * (separate from the JWT-secured /api/** REST layer, which is still
 * available if you want to call it directly e.g. from Postman).
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CrimeCaseRepository crimeCaseRepository;
    private final NetworkAnalysisService networkAnalysisService;
    private final GraphCollectionsEngine graphCollectionsEngine;

    // ---------------------------------------------------------------
    // Auth
    // ---------------------------------------------------------------

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // resolves to /WEB-INF/views/login.jsp
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String fullName,
                              @RequestParam String badgeNumber,
                              @RequestParam(defaultValue = "INVESTIGATOR") String role,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Username and password are required");
            return "register";
        }
        if (userRepository.existsByUsername(username.trim())) {
            model.addAttribute("error", "That username is already taken");
            return "register";
        }

        User.Role parsedRole;
        try {
            parsedRole = User.Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            parsedRole = User.Role.INVESTIGATOR;
        }

        User user = User.builder()
                .username(username.trim())
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .badgeNumber(badgeNumber)
                .role(parsedRole)
                .build();
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Account created. Please sign in.");
        return "redirect:/admin/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ---------------------------------------------------------------
    // Dashboard
    // ---------------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        List<Map.Entry<String, Integer>> topInfluencers =
                graphCollectionsEngine.topInfluencersByDegree(10);

        model.addAttribute("cases", crimeCaseRepository.findAll());
        model.addAttribute("totalCases", crimeCaseRepository.count());
        model.addAttribute("totalPersons", graphCollectionsEngine.totalPersons());
        model.addAttribute("totalRelationships", graphCollectionsEngine.totalRelationships());
        model.addAttribute("openCases", crimeCaseRepository.findAll().stream().filter(c -> "OPEN".equalsIgnoreCase(c.getStatus())).count());
        model.addAttribute("investigations", crimeCaseRepository.findAll().stream().filter(c -> "UNDER_INVESTIGATION".equalsIgnoreCase(c.getStatus())).count());
        model.addAttribute("closedCases", crimeCaseRepository.findAll().stream().filter(c -> "CLOSED".equalsIgnoreCase(c.getStatus())).count());
        model.addAttribute("topInfluencers", topInfluencers);

        return "dashboard";
    }

    // ---------------------------------------------------------------
    // Case management
    // ---------------------------------------------------------------

    @GetMapping("/cases")
    public String listCases(HttpSession session, Model model) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        model.addAttribute("cases", crimeCaseRepository.findAll());
        return "cases";
    }

    @GetMapping("/cases/new")
    public String newCaseForm(HttpSession session, Model model) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        model.addAttribute("mode", "create");
        return "case-form";
    }

    @PostMapping("/cases/new")
    public String createCase(HttpSession session,
                              @RequestParam String caseNumber,
                              @RequestParam String title,
                              @RequestParam(required = false) String rawReportText,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        if (caseNumber == null || caseNumber.isBlank() || title == null || title.isBlank()) {
            model.addAttribute("error", "Case number and title are required");
            model.addAttribute("mode", "create");
            return "case-form";
        }
        if (crimeCaseRepository.existsByCaseNumber(caseNumber.trim())) {
            model.addAttribute("error", "A case with that case number already exists");
            model.addAttribute("mode", "create");
            return "case-form";
        }

        CrimeCase crimeCase = CrimeCase.builder()
                .caseNumber(caseNumber.trim())
                .title(title.trim())
                .rawReportText(rawReportText)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .createdBy((String) session.getAttribute("username"))
                .build();
        CrimeCase saved = crimeCaseRepository.save(crimeCase);

        if (saved.getRawReportText() != null && !saved.getRawReportText().isBlank()) {
            networkAnalysisService.ingestReport(saved.getCaseNumber(), saved.getRawReportText());
        }

        redirectAttributes.addFlashAttribute("success", "Case " + saved.getCaseNumber() + " created");
        return "redirect:/admin/cases/" + saved.getId();
    }

    @GetMapping("/cases/{id}")
    public String caseDetail(@PathVariable Long id, HttpSession session, Model model) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        CrimeCase crimeCase = crimeCaseRepository.findById(id).orElse(null);
        if (crimeCase == null) {
            return "redirect:/admin/cases";
        }
        model.addAttribute("caseItem", crimeCase);
        return "case-detail";
    }

    @PostMapping("/cases/{id}/update")
    public String updateCase(@PathVariable Long id,
                              HttpSession session,
                              @RequestParam String title,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String rawReportText,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        CrimeCase crimeCase = crimeCaseRepository.findById(id).orElse(null);
        if (crimeCase == null) {
            return "redirect:/admin/cases";
        }

        if (title != null && !title.isBlank()) crimeCase.setTitle(title.trim());
        if (status != null && !status.isBlank()) crimeCase.setStatus(status.trim());
        if (rawReportText != null) crimeCase.setRawReportText(rawReportText);
        crimeCaseRepository.save(crimeCase);

        redirectAttributes.addFlashAttribute("success", "Case updated");
        return "redirect:/admin/cases/" + id;
    }

    @PostMapping("/cases/{id}/extract")
    public String extractCase(@PathVariable Long id, HttpSession session, Model model,
                               RedirectAttributes redirectAttributes) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        CrimeCase crimeCase = crimeCaseRepository.findById(id).orElse(null);
        if (crimeCase == null) {
            return "redirect:/admin/cases";
        }
        if (crimeCase.getRawReportText() == null || crimeCase.getRawReportText().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "This case has no report text to extract from");
            return "redirect:/admin/cases/" + id;
        }

        ExtractedEntitiesDto extracted = networkAnalysisService.ingestReport(
                crimeCase.getCaseNumber(), crimeCase.getRawReportText());

        redirectAttributes.addFlashAttribute("success",
                "Extraction complete: " + extracted.getPersons().size() + " person(s), "
                        + extracted.getPhoneNumbers().size() + " phone(s), "
                        + extracted.getLocations().size() + " location(s) merged into the graph");
        return "redirect:/admin/cases/" + id;
    }

    @PostMapping("/cases/{id}/delete")
    public String deleteCase(@PathVariable Long id, HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        if (crimeCaseRepository.existsById(id)) {
            crimeCaseRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Case deleted");
        }
        return "redirect:/admin/cases";
    }

    // ---------------------------------------------------------------
    // Network / graph analysis
    // ---------------------------------------------------------------

    @GetMapping("/network")
    public String networkPage(HttpSession session, Model model,
                               @RequestParam(required = false) String expandName,
                               @RequestParam(required = false) String pathA,
                               @RequestParam(required = false) String pathB) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        model.addAttribute("topInfluencers", graphCollectionsEngine.topInfluencersByDegree(10));

        if (expandName != null && !expandName.isBlank()) {
            model.addAttribute("expandName", expandName);
            model.addAttribute("expandResults", networkAnalysisService.getNetworkAround(expandName.trim()));
            model.addAttribute("directAssociates", toAssociateRows(expandName.trim()));
        }

        if (pathA != null && !pathA.isBlank() && pathB != null && !pathB.isBlank()) {
            model.addAttribute("pathA", pathA);
            model.addAttribute("pathB", pathB);
            List<PersonNode> path = networkAnalysisService.findConnectionPath(pathA.trim(), pathB.trim());
            model.addAttribute("pathResults", path);
        }

        return "network";
    }

    @PostMapping("/network/ingest")
    public String ingestReport(HttpSession session,
                                @RequestParam String caseNumber,
                                @RequestParam String rawText,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        NetworkAnalysisResultDto result =
                networkAnalysisService.analyzeAndPrepareDashboard(caseNumber, rawText);

        redirectAttributes.addFlashAttribute("analysisResult", result);
        redirectAttributes.addFlashAttribute("success",
                "Analysis complete for " + caseNumber + ": "
                        + result.getPersonCount() + " person(s), "
                        + result.getPhoneCount() + " phone(s), "
                        + result.getLocationCount() + " location(s), "
                        + result.getVehicleCount() + " vehicle(s)");
        return "redirect:/admin/network";
    }

    @PostMapping("/network/link")
    public String linkPersons(HttpSession session,
                               @RequestParam String personA,
                               @RequestParam String personB,
                               @RequestParam(required = false) String relationType,
                               @RequestParam(required = false) String evidenceSource,
                               @RequestParam(defaultValue = "0.5") double strength,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        String redirect = requireLogin(session, model);
        if (redirect != null) return redirect;

        networkAnalysisService.linkPersons(personA, personB, relationType, evidenceSource, strength);
        redirectAttributes.addFlashAttribute("success",
                "Linked " + personA + " \u2194 " + personB);
        return "redirect:/admin/network";
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Returns a redirect view name if the session isn't authenticated, else null. */
    private String requireLogin(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("role", session.getAttribute("role"));
        return null;
    }

    private List<Map<String, Object>> toAssociateRows(String name) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (GraphCollectionsEngine.Edge edge : graphCollectionsEngine.getDirectAssociates(name)) {
            rows.add(Map.of(
                    "target", edge.target(),
                    "weight", edge.weight(),
                    "relationType", edge.relationType() == null ? "" : edge.relationType(),
                    "evidenceSource", edge.evidenceSource() == null ? "" : edge.evidenceSource()
            ));
        }
        return rows;
    }
}

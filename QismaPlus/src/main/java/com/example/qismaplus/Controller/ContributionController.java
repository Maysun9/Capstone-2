package com.example.qismaplus.Controller;

import com.example.qismaplus.API.ApiResponse;
import com.example.qismaplus.Model.Contribution;
import com.example.qismaplus.Service.ContributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contribution")
@RequiredArgsConstructor
public class ContributionController {
    private final ContributionService contributionService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllContributions() {
        return ResponseEntity.status(200).body(contributionService.getAllContributions());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addContribution(@RequestBody @Valid Contribution contribution) {
        contributionService.addContribution(contribution);
        return ResponseEntity.status(200).body(new ApiResponse("Contribution added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateContribution(@PathVariable Integer id, @RequestBody @Valid Contribution contribution) {
        contributionService.updateContribution(id, contribution);
        return ResponseEntity.status(200).body(new ApiResponse("Contribution updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteContribution(@PathVariable Integer id) {
        contributionService.deleteContribution(id);
        return ResponseEntity.status(200).body(new ApiResponse("Contribution deleted successfully"));
    }

    // ----------------------------------------------------------END CRUD---------------------------------------------------------------------

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getContributionsByGroupId(@PathVariable Integer groupId) {
        return ResponseEntity.status(200).body(contributionService.getContributionsByGroupId(groupId));
    }

    @PutMapping("/complete/{id}")
    public ResponseEntity<?> completeContribution(@PathVariable Integer id) {
        contributionService.completeContribution(id);
        return ResponseEntity.status(200).body(new ApiResponse("Contribution completed successfully"));
    }
}

package com.example.sqlix.controller;

import com.example.sqlix.dto.request.DatabaseRequest;
import com.example.sqlix.dto.request.ExerciseRequest;
import com.example.sqlix.dto.response.AdminPracticeDatabaseDto;
import com.example.sqlix.dto.response.AdminPracticeExerciseDto;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.service.practice.AdminPracticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/practice")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPracticeController {
    private final AdminPracticeService adminPracticeService;

    @GetMapping("/databases")
    public ApiResponse<List<AdminPracticeDatabaseDto>> getAllDatabases() {
        return ApiResponse.<List<AdminPracticeDatabaseDto>>builder()
                .code(200)
                .result(adminPracticeService.getAllDatabases())
                .build();
    }

    @GetMapping("/databases/{id}")
    public ApiResponse<AdminPracticeDatabaseDto> getDatabaseById(@PathVariable UUID id) {
        return ApiResponse.<AdminPracticeDatabaseDto>builder()
                .code(200)
                .result(adminPracticeService.getDatabaseById(id))
                .build();
    }

    @PostMapping("/databases")
    public ResponseEntity<ApiResponse<UUID>> createDatabase(@Valid @RequestBody DatabaseRequest request) {
        PracticeDatabase database = adminPracticeService.createDatabase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UUID>builder().code(201).result(database.getId()).build());
    }

    @PutMapping("/databases/{id}")
    public ApiResponse<UUID> updateDatabase(
            @PathVariable UUID id,
            @Valid @RequestBody DatabaseRequest request) {
        return ApiResponse.<UUID>builder()
                .code(200)
                .result(adminPracticeService.updateDatabase(id, request).getId())
                .build();
    }

    @DeleteMapping("/databases/{id}")
    public ApiResponse<Boolean> deleteDatabase(@PathVariable UUID id) {
        adminPracticeService.deleteDatabase(id);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }

    @GetMapping("/databases/{dbId}/exercises")
    public ApiResponse<List<AdminPracticeExerciseDto>> getExercisesByDatabaseId(@PathVariable UUID dbId) {
        return ApiResponse.<List<AdminPracticeExerciseDto>>builder()
                .code(200)
                .result(adminPracticeService.getExercisesByDatabaseId(dbId))
                .build();
    }

    @PostMapping("/databases/{dbId}/exercises")
    public ResponseEntity<ApiResponse<UUID>> createExercise(
            @PathVariable UUID dbId,
            @Valid @RequestBody ExerciseRequest request) {
        PracticeExercise exercise = adminPracticeService.createExercise(dbId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UUID>builder().code(201).result(exercise.getId()).build());
    }

    @PutMapping("/exercises/{exerciseId}")
    public ApiResponse<UUID> updateExercise(
            @PathVariable UUID exerciseId,
            @Valid @RequestBody ExerciseRequest request) {
        return ApiResponse.<UUID>builder()
                .code(200)
                .result(adminPracticeService.updateExercise(exerciseId, request).getId())
                .build();
    }

    @DeleteMapping("/exercises/{exerciseId}")
    public ApiResponse<Boolean> deleteExercise(@PathVariable UUID exerciseId) {
        adminPracticeService.deleteExercise(exerciseId);
        return ApiResponse.<Boolean>builder().code(200).result(true).build();
    }
}

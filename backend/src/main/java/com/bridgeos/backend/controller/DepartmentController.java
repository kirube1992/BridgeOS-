package com.bridgeos.backend.controller;

import com.bridgeos.backend.DTO.DepartmentRequest;
import com.bridgeos.backend.DTO.DepartmentResponse;
import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        log.info("POST /api/departments - Creating department: {}", request.getName());
        DepartmentResponse createdDepartment = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        log.info("GET /api/departments - Fetching all departments");
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        log.info("GET /api/departments/{} - Fetching department", id);
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Department> getDepartmentByName(@PathVariable String name) {
        log.info("GET /api/departments/name/{} - Fetching department by name", name);
        return ResponseEntity.ok(departmentService.getDepartmentByName(name));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        log.info("PUT /api/departments/{} - Updating department", id);
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/departments/{} - Deleting department", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}

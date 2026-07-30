package com.bridgeos.backend.controller;

import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    // CREATE - POST /api/departments
    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        log.info("POST /api/departments - Creating department: {}", department.getName());
        Department createdDepartment = departmentService.createDepartment(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    // READ ALL - GET /api/departments
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        log.info("GET /api/departments - Fetching all departments");
        List<Department> departments = departmentService.getAllDepartment();
        return ResponseEntity.ok(departments);
    }

    // READ ONE - GET /api/departments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        log.info("GET /api/departments/{} - Fetching department", id);
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    // READ BY NAME - GET /api/departments/name/{name}
    @GetMapping("/name/{name}")
    public ResponseEntity<Department> getDepartmentByName(@PathVariable String name) {
        log.info("GET /api/departments/name/{} - Fetching department by name", name);
        Department department = departmentService.getDepartmentByName(name);
        return ResponseEntity.ok(department);
    }

    // UPDATE - PUT /api/departments/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department department) {
        log.info("PUT /api/departments/{} - Updating department", id);
        Department updatedDepartment = departmentService.updateDepartment(id, department);
        return ResponseEntity.ok(updatedDepartment);
    }

    // DELETE - DELETE /api/departments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/departments/{} - Deleting department", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
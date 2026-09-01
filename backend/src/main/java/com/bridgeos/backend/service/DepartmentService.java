package com.bridgeos.backend.service;

import com.bridgeos.backend.DTO.DepartmentMemberPreview;
import com.bridgeos.backend.DTO.DepartmentRequest;
import com.bridgeos.backend.DTO.DepartmentResponse;
import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.entity.DepartmentStatus;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.repository.DepartmentRepository;
import com.bridgeos.backend.repository.UserRepository;
import com.bridgeos.backend.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final WorkItemRepository workItemRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        log.info("Creating a department: {}", request.getName());

        if (departmentRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");
        }

        Department department = new Department();
        applyRequest(department, request);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    public List<DepartmentResponse> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DepartmentResponse getDepartmentById(Long id) {
        log.info("Fetching department by id: {}", id);
        return mapToResponse(getDepartmentEntity(id));
    }

    public Department getDepartmentEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with id: " + id));
    }

    public Department getDepartmentByName(String name) {
        log.info("Fetching department by name: {}", name);
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with name: " + name));
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        log.info("Updating department with id: {}", id);

        Department existingDepartment = getDepartmentEntity(id);

        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");
        }

        applyRequest(existingDepartment, request);
        existingDepartment.setUpdatedAt(LocalDateTime.now());
        Department saved = departmentRepository.save(existingDepartment);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department with id: {}", id);
        Department department = getDepartmentEntity(id);

        List<User> members = userRepository.findByDepartmentId(id);
        for (User member : members) {
            member.setDepartment(null);
        }
        userRepository.saveAll(members);

        List<WorkItem> workItems = workItemRepository.findByDepartmentId(id);
        for (WorkItem workItem : workItems) {
            workItem.setDepartment(null);
        }
        workItemRepository.saveAll(workItems);

        departmentRepository.delete(department);
    }

    private void applyRequest(Department department, DepartmentRequest request) {
        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());
        department.setDefaultWorkFlow(request.getDefaultWorkFlow());
        department.setStatus(request.getStatus() != null ? request.getStatus() : DepartmentStatus.ACTIVE);

        if (request.getDepartmentLeadId() != null) {
            User lead = userRepository.findById(request.getDepartmentLeadId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department lead not found"));
            department.setDepartmentLead(lead);
        } else {
            department.setDepartmentLead(null);
        }
    }

    private DepartmentResponse mapToResponse(Department department) {
        Long departmentId = department.getId();
        long memberCount = userRepository.countByDepartmentId(departmentId);
        long workItemCount = workItemRepository.countByDepartmentId(departmentId);
        List<DepartmentMemberPreview> members = userRepository
                .findTop3ByDepartmentIdOrderByNameAsc(departmentId)
                .stream()
                .map(DepartmentMemberPreview::from)
                .toList();

        return DepartmentResponse.from(department, memberCount, workItemCount, members);
    }
}

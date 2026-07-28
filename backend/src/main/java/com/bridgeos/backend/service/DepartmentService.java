package com.bridgeos.backend.service;


import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private  final DepartmentRepository departmentRepository;
    @Transactional
    public Department createDepartment(Department department) {

        log.info("Creating a department: {}",department.getName());

        if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department name already exist");
        }

        return departmentRepository.save(department);

    }

    public List<Department> getAllDepartment(){
        log.info("get all department ");
        return departmentRepository.findAll();
    }


    public Department getDepartmentById(Long id) {
        log.info("get department by id: {}", id);

        return departmentRepository.findById(id)
                .orElseThrow(()->  new RuntimeException("Department not found with id:" + id));
    }

    public Department getDepartmentByName(String name) {
        log.info("get department by name: {}", name);

        return departmentRepository.findByName(name)
                .orElseThrow(()->new RuntimeException("Department not found with this name:" + name));
    }

    @Transactional
    public Department updateDepartment(Long id, Department updateDepartment) {
        log.info("update department with the id of:{}", id);


        Department existingDepartment = getDepartmentById(id);

        existingDepartment.setName(updateDepartment.getName());
        existingDepartment.setDescription(updateDepartment.getDescription());
        existingDepartment.setUpdatedAt(java.time.LocalDateTime.now());
        return  departmentRepository.save(existingDepartment);
    }

    public void deleteDepartment(Long id) {
        log.info("delete the department with the id: {}", id);

        getDepartmentById(id);
        departmentRepository.deleteById(id);
    }
}

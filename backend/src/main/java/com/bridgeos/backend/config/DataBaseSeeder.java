package com.bridgeos.backend.config;

import com.bridgeos.backend.entity.*;
import com.bridgeos.backend.repository.DepartmentRepository;
import com.bridgeos.backend.repository.ProjectRepository;
import com.bridgeos.backend.repository.UserRepository;
import com.bridgeos.backend.service.ProjectService;
import com.bridgeos.backend.service.WorkItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataBaseSeeder  implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final WorkItemService workItemService;

    @Override
    public void run(String... args) throws Exception {
        if(departmentRepository.count() == 0) {
            log.info("Seeding departments...");

            Department engineering  = new Department();
            engineering.setName("engineering");
            engineering.setDescription("software dev team");
            departmentRepository.save(engineering);

            Department sales = new Department();
            sales.setName("Sales");
            sales.setDescription("Sales and business development");
            departmentRepository.save(sales);

            Department operations = new Department();
            operations.setName("Operations");
            operations.setDescription("Operations and logistics");
            departmentRepository.save(operations);

            Department hr = new Department();
            hr.setName("HR");
            hr.setDescription("Human resources");
            departmentRepository.save(hr);

            log.info("Departments seeded successfully!");
        }

        if(projectRepository.count() == 0) {
            log.info("seeding default project...");

            User user  = userRepository.findById(1l).orElse(null);

            if(user != null){
                Project project = new Project();
                project.setName("BridgeOS Development");
                project.setDescription("Main project for BridgeOS platform");
                project.setCreatedBy(user);
                project.setProjectManager(user);
                project = projectRepository.save(project);


            if(workItemService.getAllWorkItem().isEmpty()) {
                WorkItem workItem = new WorkItem();
                workItem.setTitle("Build authentication system");
                workItem.setDescription("Implement JWT-based authentication");
                workItem.setBusinessContextNotes("Users need to securely login to access BridgeOS features");
                workItem.setAcceptanceCriteria("- Users can register\n- Users can login\n- JWT tokens work");

                workItem.setStatus(WorkItemStatus.TODO);

                workItemService.createWorkItem(
                        workItem,
                        project.getId(),
                        user.getId(),
                        user.getId(),
                        null
                );

                log.info("Sample work item seeded!");
            }

            }
        }

    }

}

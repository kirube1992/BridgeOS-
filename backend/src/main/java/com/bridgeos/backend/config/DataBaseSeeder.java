package com.bridgeos.backend.config;

import com.bridgeos.backend.entity.Department;
import com.bridgeos.backend.entity.Priority;
import com.bridgeos.backend.entity.Project;
import com.bridgeos.backend.entity.ProjectStatus;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.entity.WorkItem;
import com.bridgeos.backend.entity.WorkItemStatus;
import com.bridgeos.backend.repository.AuditEventRepository;
import com.bridgeos.backend.repository.DepartmentRepository;
import com.bridgeos.backend.repository.MetricsDailyRepository;
import com.bridgeos.backend.repository.ProjectChatMessageRepository;
import com.bridgeos.backend.repository.ProjectRepository;
import com.bridgeos.backend.repository.RequirementTranslationRepository;
import com.bridgeos.backend.repository.TaskRepository;
import com.bridgeos.backend.repository.UserRepository;
import com.bridgeos.backend.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataBaseSeeder implements CommandLineRunner {
    private final AuditEventRepository auditEventRepository;
    private final DepartmentRepository departmentRepository;
    private final MetricsDailyRepository metricsDailyRepository;
    private final ProjectChatMessageRepository projectChatMessageRepository;
    private final ProjectRepository projectRepository;
    private final RequirementTranslationRepository requirementTranslationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkItemRepository workItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.replace-existing:false}")
    private boolean replaceExisting;

    @Override
    public void run(String... args) {
        if (!replaceExisting && (userRepository.count() > 0 || projectRepository.count() > 0)) return;
        if (replaceExisting) clearExistingData();
        Map<String, User> owners = seedOwners();
        Map<String, Department> departments = seedDepartments();
        seedProjects(owners, departments);
        log.info("Project Tracker data seeded: 22 projects and {} owners", owners.size());
    }

    private void clearExistingData() {
        auditEventRepository.deleteAllInBatch();
        projectChatMessageRepository.deleteAllInBatch();
        requirementTranslationRepository.deleteAllInBatch();
        metricsDailyRepository.deleteAllInBatch();
        workItemRepository.deleteAllInBatch();
        taskRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
    }

    private Map<String, User> seedOwners() {
        Map<String, User> owners = new LinkedHashMap<>();
        owners.put("Kirubel", createOwner("Kirubel", "kirubel@bridgeos.local", "ADMIN"));
        owners.put("Kebron", createOwner("Kebron", "kebron@bridgeos.local", "MEMBER"));
        owners.put("Tigist", createOwner("Tigist", "tigist@bridgeos.local", "MEMBER"));
        owners.put("Bereket", createOwner("Bereket", "bereket@bridgeos.local", "MEMBER"));
        owners.put("Israel", createOwner("Israel", "israel@bridgeos.local", "MEMBER"));
        owners.put("Madot", createOwner("Madot", "madot@bridgeos.local", "MEMBER"));
        return owners;
    }

    private User createOwner(String name, String email, String role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("BridgeOS123!"));
        return userRepository.save(user);
    }

    private Map<String, Department> seedDepartments() {
        Map<String, Department> departments = new LinkedHashMap<>();
        for (String name : new String[]{"Ethiotelecom", "SaaS", "AI", "Bigdata", "Alert", "CEP Devices"}) {
            Department department = new Department();
            department.setName(name);
            department.setDescription("Project Tracker portfolio: " + name);
            departments.put(name, departmentRepository.save(department));
        }
        return departments;
    }

    private void seedProjects(Map<String, User> owners, Map<String, Department> departments) {
        add("TE-01", "Ethiotelecom", "Chat", "Kirubel", "P0", "O&M", "Delivered, PAC received", "Start O&M; open customization contract", "2026-09-15", "1. Agree O&M scope and SLA with technical team\n2. Hold O&M kickoff with customer\n3. Submit customization quotation", "O&M started + customization SOW submitted", owners, departments);
        add("TE-02", "Ethiotelecom", "DMS", "Kirubel / Kebron", "P0", "O&M", "Delivered, PAC received", "Start O&M; open customization contract", "2026-09-15", "1. Agree O&M scope and SLA with technical team\n2. Hold O&M kickoff with customer\n3. Submit customization quotation", "O&M started + customization SOW submitted", owners, departments);
        add("TE-03", "Ethiotelecom", "E-signature", "Kirubel", "P0", "POC done", "POC done; 3 requirements still in development", "Close 3 requirements, then push go-live + contract", "2026-10-15", "1. Get firm delivery date from R&D for the 3 requirements\n2. Fix go-live date with customer\n3. Submit quotation and contract", "3 requirements delivered + contract signed", owners, departments);
        add("TE-04", "Ethiotelecom", "Meeting", "Kirubel / Tigist", "P1", "POC done", "POC finished", "Push contract signing", "2026-09-30", "1. Get written POC acceptance\n2. Submit quotation\n3. Track contract to signature", "Contract signed", owners, departments);
        add("TE-05", "Ethiotelecom", "AI KB", "Bereket / Tigist", "P1", "POC done", "POC finished", "Push contract signing", "2026-09-30", "1. Collect POC results into a 1-page value case\n2. Get written POC acceptance\n3. Submit quotation", "Contract signed", owners, departments);
        add("TE-06", "Ethiotelecom", "Mail Box", "Israel / Bereket", "P1", "Dev", "In development", "Deliver POC by end of September", "2026-09-30", "1. Agree POC acceptance criteria with customer\n2. Finish internal testing\n3. Run POC demo", "POC delivered and accepted", owners, departments);
        add("TE-07", "Ethiotelecom", "Task", "Israel / Bereket", "P2", "Dev", "Under development, no POC date", "Set POC scope and date", "2026-10-31", "1. Get remaining effort estimate from R&D\n2. Set POC scope and date", "POC date agreed and recorded", owners, departments);
        add("SA-01", "SaaS", "Chat", "Tigist / Kebron", "P0", "Quoted", "Quotation submitted (10,000 licenses)", "Clear document issue, then push contract", "2026-09-30", "1. List the outstanding documents and their owners\n2. Escalate with a firm deadline\n3. Deliver documents and push contract", "Documents delivered + contract signed", owners, departments);
        add("SA-02", "SaaS", "DMS", "Tigist / Kebron", "P0", "Quoted", "Quotation submitted (10,000 licenses)", "Clear document issue, then push contract", "2026-09-30", "1. Deliver outstanding documents\n2. Push contract to signature", "Documents delivered + contract signed", owners, departments);
        add("SA-03", "SaaS", "Meeting", "Tigist / Kebron", "P1", "Quoted", "Quotation submitted (10,000 licenses)", "Clear document issue, then push contract", "2026-09-30", "1. Deliver outstanding documents\n2. Push contract to signature", "Documents delivered + contract signed", owners, departments);
        add("SA-04", "SaaS", "Market Place", "Israel / Tigist", "P2", "Not started", "No status, no scope defined", "Decide go / no-go this quarter", "2026-10-15", "1. Get product description and readiness from R&D\n2. Make and record a go / no-go decision", "Written go / no-go decision", owners, departments);
        add("SA-05", "SaaS", "E-signature", "Israel / Tigist", "P2", "On hold", "No quotation - customer budget constraint", "Prepare add-on pricing; track budget cycle", "2026-11-30", "1. Confirm customer's next budget cycle date\n2. Prepare add-on pricing sheet", "Pricing ready when budget opens", owners, departments);
        add("SA-06", "SaaS", "AI KB", "Israel / Tigist", "P2", "On hold", "No quotation - customer budget constraint", "Prepare add-on pricing; track budget cycle", "2026-11-30", "1. Prepare add-on pricing sheet", "Pricing ready when budget opens", owners, departments);
        add("SA-07", "SaaS", "Mail Box", "Israel / Tigist", "P2", "On hold", "No quotation - customer budget constraint", "Prepare add-on pricing; track budget cycle", "2026-11-30", "1. Prepare add-on pricing sheet", "Pricing ready when budget opens", owners, departments);
        add("SA-08", "SaaS", "Task", "Israel / Tigist", "P2", "On hold", "No quotation - customer budget constraint", "Prepare add-on pricing; track budget cycle", "2026-11-30", "1. Prepare add-on pricing sheet", "Pricing ready when budget opens", owners, departments);
        add("AI-01", "AI", "AI Telebirr", "Bereket / Tigist", "P0", "Delivery", "Contract signed; delivery and PAT next", "Clarify PAT process, request resources, start PAT", "2026-09-20", "1. Document PAT process and entry criteria\n2. Request PAT resources in advance\n3. Start PAT and close defects daily\n4. Invoice on PAT acceptance", "PAT passed + milestone invoiced", owners, departments);
        add("AI-02", "AI", "AI OSS", "Israel / Tigist", "P1", "POC", "POC started", "Design use cases with customer (Telebirr approach)", "2026-09-30", "1. Run use-case workshops with customer\n2. Get use-case list signed off\n3. Turn use cases into a dated dev plan", "Use-case list signed off", owners, departments);
        add("AI-03", "AI", "Cowork", "Bereket / Kirubel", "P2", "R&D", "In R&D; to be enabled on Bigdata and AI OSS", "Confirm business scenarios with customer", "2026-10-15", "1. Draft scenarios for Bigdata and AI OSS\n2. Confirm scenarios with customer", "Scenarios confirmed in writing", owners, departments);
        add("BD-01", "Bigdata", "Data Lake / BI", "Kirubel / Madot", "P0", "POC closing", "POC nearing final stage", "Close POC; build local team's data knowledge", "2026-09-30", "1. Document customer business and data structures\n2. Set weekly sync with China team\n3. Get POC acceptance and submit proposal", "POC accepted + proposal submitted", owners, departments);
        add("BD-02", "Bigdata", "Bigdata AI", "Bereket / Madot", "P1", "POC closing", "POC nearing final stage", "Close POC; package proposal with Data Lake", "2026-09-30", "1. Quantify value of each AI use case\n2. Finish POC scenarios with China team\n3. Submit joint proposal with BD-01", "POC accepted + joint proposal submitted", owners, departments);
        add("AL-01", "Alert", "Alert", "Israel / Tigist", "P0", "Contracting", "Preparing contract execution", "Expedite signing, then lock delivery and payment plan", "2026-09-30", "1. Close remaining contract clauses\n2. Track contract through each approval step\n3. Name delivery lead before signature", "Contract signed + delivery plan agreed", owners, departments);
        add("CD-01", "CEP Devices", "CEP Devices", "Israel / Madot", "P1", "Testing", "Preparing for customer testing", "Finish internal testing, submit, track results", "2026-09-30", "1. Build internal test plan covering all cases\n2. Close all critical defects\n3. Submit and track results daily", "Passes customer testing first time", owners, departments);
    }

    private void add(String code, String customer, String product, String ownerNames, String priority, String stage, String currentStatus, String nextAction, String dueDate, String steps, String doneWhen, Map<String, User> owners, Map<String, Department> departments) {
        User primaryOwner = owners.get(ownerNames.split(" / ")[0]);
        Project project = new Project();
        project.setName(customer + " - " + product);
        project.setDescription("Project " + code + " | Stage: " + stage + "\nCurrent status: " + currentStatus);
        project.setClientContext(customer + " | Owners: " + ownerNames + " | Next action: " + nextAction);
        project.setDeadLine(LocalDate.parse(dueDate));
        project.setCreatedBy(primaryOwner);
        project.setProjectManager(primaryOwner);
        project.setStatus("On hold".equalsIgnoreCase(stage) ? ProjectStatus.ON_HOLD : ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        WorkItem item = new WorkItem();
        item.setTitle(nextAction);
        item.setDescription("Project " + code + " - " + product + "\nOwners: " + ownerNames + "\nStage: " + stage);
        item.setBusinessContextNotes(steps);
        item.setAcceptanceCriteria(doneWhen);
        item.setPriority(priorityForTrackerValue(priority));
        item.setStatus(statusForStage(stage));
        item.setDeadline(LocalDate.parse(dueDate));
        item.setProject(savedProject);
        item.setAssignedTo(primaryOwner);
        item.setCreatedBy(primaryOwner);
        item.setDepartment(departments.get(customer));
        workItemRepository.save(item);
    }

    private WorkItemStatus statusForStage(String stage) {
        if ("Delivery".equalsIgnoreCase(stage) || "POC closing".equalsIgnoreCase(stage) || "Testing".equalsIgnoreCase(stage)) return WorkItemStatus.REVIEW;
        if ("O&M".equalsIgnoreCase(stage) || "Dev".equalsIgnoreCase(stage) || "POC done".equalsIgnoreCase(stage) || "POC".equalsIgnoreCase(stage) || "R&D".equalsIgnoreCase(stage) || "Contracting".equalsIgnoreCase(stage)) return WorkItemStatus.IN_PROGRESS;
        return WorkItemStatus.TODO;
    }

    private Priority priorityForTrackerValue(String priority) {
        return switch (priority) {
            case "P0" -> Priority.HIGH;
            case "P1" -> Priority.MEDIUM;
            case "P2" -> Priority.LOW;
            default -> Priority.MEDIUM;
        };
    }
}

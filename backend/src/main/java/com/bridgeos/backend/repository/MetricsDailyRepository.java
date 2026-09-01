package com.bridgeos.backend.repository;


import com.bridgeos.backend.entity.MetricsDaily;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface MetricsDailyRepository extends JpaRepository<MetricsDaily, Long> {

    Optional<MetricsDaily> findByMetricDateAndUserId(LocalDate date, Long userId);

    List<MetricsDaily> findByMetricDateBetween(LocalDate start, LocalDate end);
    List<MetricsDaily>  findByUserIdOrderByMetricDateDesc (Long userId);
    List<MetricsDaily> findByDepartmentIdOrderByMetricDateDesc(Long departmentId);
    List<MetricsDaily> findByMetricDateAndDepartmentId(LocalDate date, Long departmentId);
   List<MetricsDaily> findByMetricDateBetweenAndDepartmentId(LocalDate startDate,LocalDate endDate,Long departmentId);

   List<MetricsDaily> findByUserIdAndMetricDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
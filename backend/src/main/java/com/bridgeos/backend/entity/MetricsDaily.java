package com.bridgeos.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "metric_daily")
public class MetricsDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "items_resolved")
    private Integer itemsResolved = 0;

    @Column(name = "items_created")
    private Integer itemsCreated = 0;

    @Column(name = "avg_resolution_hours")
    private BigDecimal avgResolutionHours;

    @Column(name = "median_resolution_hours")
    private BigDecimal medianResolutionHours;

    @Column(name = "first_response_hours")
    private BigDecimal firstResponseHours;

    @Column(name = "comments_posted")
    private Integer commentsPosted = 0;

    @Column(name = "clarity_avg_score")
    private BigDecimal clarityAvgScore;

    @Column(name = "csat_avg")
    private BigDecimal csatAvg;

    @Column(name = "kudos_received")
    private Integer kudosReceived = 0;

    @Column(name = "off_hours_resolutions")
    private Integer offHoursResolutions = 0;

}

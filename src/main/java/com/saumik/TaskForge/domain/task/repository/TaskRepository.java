package com.saumik.TaskForge.domain.task.repository;

import com.saumik.TaskForge.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TaskRepository extends
        JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

    @Modifying
    @Query("DELETE FROM Task t WHERE t.organizationId = :orgId")
    void deleteByOrganizationId(@Param("orgId") UUID orgId);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
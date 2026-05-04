package com.saumik.TaskForge.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends
        JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

}
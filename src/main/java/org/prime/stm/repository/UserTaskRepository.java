package org.prime.stm.repository;

import java.util.List;

import org.prime.stm.model.Task;
import org.prime.stm.model.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

	@Query("select t.id from UserTask t where t.task.id = :taskId")
	List<Long> findAllByTaskId(Long taskId);

}

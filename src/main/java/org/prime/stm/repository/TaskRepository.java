package org.prime.stm.repository;


import java.util.List;

import org.prime.security.model.User;
import org.prime.stm.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, Long>{
	
	  @Query("select t from Task t where t.project.id = :projectId order by t.lastModefied ASC")
	  List<Task> findAllByProjectId(Long projectId);
	  
	  @Query("select COUNT(t) from Task t where t.project.id = :projectId and t.status = :status")
	  Long countTasksByStatus(Long projectId, Long status);
	  
	  @Query("select COUNT(t) from Task t where t.project.id = :projectId")
	  Long countTasksByProject(Long projectId);
	  
	  @Query("select SUM(t.progress) from Task t where t.project.id = :projectId")
	  Long sumProgressByProject(Long projectId);
	  
	  @Query("select t.user from UserTask t where t.task.id = :taskId and t.user.enabled is true")
	  List<User> getTaskUsers(Long taskId);
	  
	  //return users who not assigned to this task
	  @Query("select u from User u where u.enabled is true and u.id not in "
	  		+ "(select t.user.id from UserTask t where t.task.id = :taskId) ")
	  List<User> getNotTaskUsers(Long taskId);

	  @Query("select SUM(c.progress) from Comment c where c.task.id = :taskId and"
	  		+ " c.createdBy.id  = :userId ")
	  Long sumTaskProgressByUser(Long taskId, Long userId);
	  
	  @Query("select t.task from UserTask t where t.user.id = :userId and t.task.project.id = :projectId")
	  List<Task> getUserTasksByProject(Long userId, Long projectId);
	  
	  @Query("select t.task from UserTask t where t.user.id = :userId")
	  List<Task> getUserTasks(Long userId);
	  
	  @Query("select t.task from UserTask t where t.user.id = :userId")
	  Page<Task> getUserTasks(Long userId, Pageable pageable);
	  
	  @Query("select t.task from UserTask t where t.user.id = :userId")
	  List<Task> getUserTasks(Long userId, Sort sort);
}

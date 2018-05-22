package org.prime.stm.repository;

import java.util.List;

import org.prime.stm.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {


	@Query("select p from Project p where p.id in  "
			+ "( select u.task.project.id from UserTask u where u.user.id = :userId )")
	List<Project> getUserProjects(Long userId);
	
	@Query("select p from Project p where p.id in  "
			+ "( select u.task.project.id from UserTask u where u.user.id = :userId )")
	Page<Project> getUserProjects(Long userId, Pageable pageable);
	
	@Query("select p from Project p where p.id in  "
			+ "( select u.task.project.id from UserTask u where u.user.id = :userId )")
	List<Project> getUserProjects(Long userId, Sort sort);
}

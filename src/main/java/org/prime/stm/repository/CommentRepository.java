package org.prime.stm.repository;


import java.util.Date;
import java.util.List;

import org.prime.stm.model.Comment;
import org.prime.stm.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long>{

	  @Query("select COUNT(c) from Comment c where c.task.id = :taskId")
	  Long countCommentByTask(Long taskId);
	  
	  @Query("select SUM(c.progress) from Comment c where c.task.id = :taskId")
	  Long sumProgressByTask(Long taskId);
	  
	  @Query("select c from Comment c where c.task.id = :taskId order by c.dateCreated ASC")
	  List<Comment> findAllByTaskId(Long taskId);
	  
	  @Query("select SUM(c.progress) from Comment c where c.task.id = :taskId and c.createdBy.id = :userId")
	  Long sumUserProgressByTask(Long taskId, Long userId);
	  
	  @Query("select c from Comment c where c.task.id = :taskId and c.createdBy.id = :userId order by c.dateCreated ASC")
	  List<Comment> findUserCommentsByTask(Long userId, Long taskId);
	  
	  @Query("select c from Comment c where c.createdBy.id = :userId")
	  Page<Comment> findByCreatedBy(Long userId, Pageable pageable);
	  
	  @Query("select c from Comment c where c.createdBy.id = :userId")
	  List<Comment> findByCreatedBy(Long userId, Sort sort);
	  
	  @Query("select SUM(c.progress) from Comment c where c.createdBy.id = :userId and c.dateCreated between :dateFrom and :dateTo")
	  Long sumUserProgressByMonth(@Param("userId") Long userId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);
}

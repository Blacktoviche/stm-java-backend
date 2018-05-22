package org.prime.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import org.prime.security.model.User;


public interface UserRepository extends JpaRepository<User, Long> {
	
    Optional<User> findByUsername(String username);
    
    @Query("select u from User u where u.id in ( select t.user.id from UserTask t where t.task.id = :taskId )")
    List<User> getUsersByTask(Long taskId);
    
    public List<User> findByEnabledTrue();
    
    @Query("select u from User u where u.username like :username and u.id != :userId ")
    Optional<User> findByUsernameExept(String username, Long userId);
    
}

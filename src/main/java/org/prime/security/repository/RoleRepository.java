package org.prime.security.repository;

import java.util.Optional;

import org.prime.security.model.Role;
import org.prime.security.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
	@Query("select r from Role r where r.name like :roleName")
    Role findByRoleName(RoleName roleName);

}

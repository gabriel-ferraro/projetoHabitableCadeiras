package com.br.personniMoveis.repository;

import com.br.personniMoveis.model.UserEntityRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<UserEntityRole, Long> {
    //Role findByName(UserEntityRole name);
}
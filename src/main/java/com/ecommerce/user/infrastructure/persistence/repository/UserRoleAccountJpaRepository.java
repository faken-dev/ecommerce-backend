package com.ecommerce.user.infrastructure.persistence.repository;

import com.ecommerce.user.infrastructure.persistence.entity.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleAccountJpaRepository extends JpaRepository<UserRoleJpaEntity, UUID> {
    Optional<UserRoleJpaEntity> findByName(String name);
    List<UserRoleJpaEntity> findByNameIn(Collection<String> names);
}

package com.ecommerce.system.infrastructure.persistence.repository;

import com.ecommerce.system.infrastructure.persistence.entity.SettingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingJpaRepository extends JpaRepository<SettingJpaEntity, String> {
    List<SettingJpaEntity> findByGroup(String group);
}

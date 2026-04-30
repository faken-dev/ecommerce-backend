package com.ecommerce.inventory.infrastructure.persistence.mapper;

import com.ecommerce.inventory.domain.entity.InventoryItem;
import com.ecommerce.inventory.domain.entity.InventoryLog;
import com.ecommerce.inventory.domain.entity.Slot;
import com.ecommerce.inventory.domain.entity.Warehouse;
import com.ecommerce.inventory.domain.entity.Zone;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryLogJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.entity.SlotJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.entity.WarehouseJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.entity.ZoneJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryDomainMapper {
    
    @Mapping(target = "version", ignore = true)
    InventoryItem toDomain(InventoryItemJpaEntity jpaEntity);
    InventoryItemJpaEntity toJpa(InventoryItem domainEntity);

    Warehouse toDomain(WarehouseJpaEntity jpaEntity);
    @Mapping(target = "deletedAt", ignore = true)
    WarehouseJpaEntity toJpa(Warehouse domainEntity);

    Zone toDomain(ZoneJpaEntity jpaEntity);
    @Mapping(target = "deletedAt", ignore = true)
    ZoneJpaEntity toJpa(Zone domainEntity);

    Slot toDomain(SlotJpaEntity jpaEntity);
    @Mapping(target = "deletedAt", ignore = true)
    SlotJpaEntity toJpa(Slot domainEntity);

    InventoryLog toDomain(InventoryLogJpaEntity jpaEntity);
    @Mapping(target = "version", ignore = true)
    InventoryLogJpaEntity toJpa(InventoryLog domainEntity);
}

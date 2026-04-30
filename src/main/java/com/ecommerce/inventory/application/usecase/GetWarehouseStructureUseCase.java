package com.ecommerce.inventory.application.usecase;

import com.ecommerce.inventory.infrastructure.persistence.repository.SlotJpaRepository;
import com.ecommerce.inventory.infrastructure.persistence.repository.ZoneJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWarehouseStructureUseCase {

    private final ZoneJpaRepository zoneRepository;
    private final SlotJpaRepository slotRepository;

    @Getter
    public static class ZoneStructure {
        private UUID id;
        private String name;
        private String description;
        private List<SlotStructure> slots;

        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<SlotStructure> getSlots() { return slots; }

        public static ZoneStructureBuilder builder() { return new ZoneStructureBuilder(); }
        public static class ZoneStructureBuilder {
            private UUID id;
            private String name;
            private String description;
            private List<SlotStructure> slots;
            public ZoneStructureBuilder id(UUID id) { this.id = id; return this; }
            public ZoneStructureBuilder name(String name) { this.name = name; return this; }
            public ZoneStructureBuilder description(String description) { this.description = description; return this; }
            public ZoneStructureBuilder slots(List<SlotStructure> slots) { this.slots = slots; return this; }
            public ZoneStructure build() {
                ZoneStructure zs = new ZoneStructure();
                zs.id = id; zs.name = name; zs.description = description; zs.slots = slots;
                return zs;
            }
        }
    }

    @Getter
    public static class SlotStructure {
        private UUID id;
        private String name;
        private int capacity;

        public UUID getId() { return id; }
        public String getName() { return name; }
        public int getCapacity() { return capacity; }

        public static SlotStructureBuilder builder() { return new SlotStructureBuilder(); }
        public static class SlotStructureBuilder {
            private UUID id;
            private String name;
            private int capacity;
            public SlotStructureBuilder id(UUID id) { this.id = id; return this; }
            public SlotStructureBuilder name(String name) { this.name = name; return this; }
            public SlotStructureBuilder capacity(int capacity) { this.capacity = capacity; return this; }
            public SlotStructure build() {
                SlotStructure ss = new SlotStructure();
                ss.id = id; ss.name = name; ss.capacity = capacity;
                return ss;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ZoneStructure> execute(UUID warehouseId) {
        return zoneRepository.findByWarehouseIdAndDeletedAtIsNull(warehouseId).stream()
                .map(zone -> ZoneStructure.builder()
                        .id(zone.getId())
                        .name(zone.getName())
                        .description(zone.getDescription())
                        .slots(slotRepository.findByZoneIdAndDeletedAtIsNull(zone.getId()).stream()
                                .map(slot -> SlotStructure.builder()
                                        .id(slot.getId())
                                        .name(slot.getName())
                                        .capacity(slot.getCapacity())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}

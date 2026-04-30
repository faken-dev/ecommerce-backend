package com.ecommerce.admin.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StaticPage extends AuditableEntity {
    private UUID id;
    private String title;
    private String slug;
    private String content;
    private boolean isActive;

    public static StaticPage create(String title, String slug, String content) {
        return StaticPage.builder()
                .id(UUID.randomUUID())
                .title(title)
                .slug(slug)
                .content(content)
                .isActive(true)
                .build();
    }
}

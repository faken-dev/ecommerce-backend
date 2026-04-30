package com.ecommerce.admin.domain.entity;

import com.ecommerce.shared.domain.AuditableEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Banner extends AuditableEntity {
    private String imageUrl;
    private String linkUrl;
    private String title;
    private String status; // ACTIVE, INACTIVE
    private int priority;

    public void updateInfo(String title, String imageUrl, String linkUrl, String status, Integer priority) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.status = status != null ? status : "ACTIVE";
        this.priority = priority != null ? priority : 0;
        this.touchUpdate();
    }
}

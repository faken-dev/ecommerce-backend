package com.ecommerce.system.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_system_settings")
@Getter
@Setter
public class SettingJpaEntity {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value", nullable = false, length = 1000)
    private String value;

    @Column(name = "setting_group")
    private String group;

    private String description;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

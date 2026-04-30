-- V13__admin_cms_and_static_content.sql
-- Banners, Settings, and Static Pages.

CREATE TABLE cms_banners (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    image_url TEXT NOT NULL,
    link_url TEXT,
    priority INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE admin_system_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    setting_value VARCHAR(1000) NOT NULL,
    setting_group VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE admin_dashboard_stats (
    stat_key VARCHAR(50) PRIMARY KEY,
    stat_value DECIMAL(19, 4) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE static_pages (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    content TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cms_banners_status ON cms_banners(status);
CREATE INDEX idx_static_pages_slug ON static_pages(slug);

-- Initial Settings
INSERT INTO admin_system_settings (setting_key, setting_value, setting_group, description) VALUES
('warehousingEnabled', 'true', 'MODULES', 'Bật/tắt tính năng quản lý kho (WMS)'),
('pdfExportEnabled', 'true', 'EXPORTS', 'Cho phép xuất báo cáo PDF'),
('excelExportEnabled', 'true', 'EXPORTS', 'Cho phép xuất báo cáo Excel');

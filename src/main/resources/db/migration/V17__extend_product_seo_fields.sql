-- V17__extend_product_seo_fields.sql
-- Increase lengths for SEO fields to avoid DataException when users enter long descriptions.

ALTER TABLE catalog_products ALTER COLUMN meta_title TYPE VARCHAR(300);
ALTER TABLE catalog_products ALTER COLUMN meta_description TYPE TEXT;

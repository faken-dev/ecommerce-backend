package com.ecommerce.shared.application.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Shared service for exporting data to various formats (PDF, Excel, CSV).
 */
public interface ExportService {

    /**
     * Exports a single object to PDF using a template.
     */
    void exportToPdf(String templateName, Map<String, Object> data, OutputStream outputStream);

    /**
     * Exports a list of maps to Excel.
     */
    void exportToExcel(String sheetName, List<String> headers, List<Map<String, Object>> data, OutputStream outputStream);
}

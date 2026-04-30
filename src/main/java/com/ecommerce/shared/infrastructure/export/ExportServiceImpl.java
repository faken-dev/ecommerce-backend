package com.ecommerce.shared.infrastructure.export;

import com.ecommerce.shared.application.service.ExportService;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {
    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);

    private final SpringTemplateEngine templateEngine;

    @Override
    public void exportToPdf(String templateName, Map<String, Object> data, OutputStream outputStream) {
        try {
            log.info("PDF Export: Processing Thymeleaf template '{}'", templateName);
            Context context = new Context();
            context.setVariables(data);
            String htmlContent = templateEngine.process(templateName, context);

            if (htmlContent == null || htmlContent.isEmpty()) {
                log.error("PDF Export: Generated HTML content is empty for template '{}'", templateName);
                throw new RuntimeException("HTML content is empty");
            }

            log.info("PDF Export: Starting PDF rendering (HTML length: {})", htmlContent.length());
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            try {
                File fontFile = new File("C:/Windows/Fonts/Arial.ttf");
                if (fontFile.exists()) {
                    builder.useFont(fontFile, "Arial");
                    File boldFontFile = new File("C:/Windows/Fonts/arialbd.ttf");
                    if (boldFontFile.exists()) {
                        builder.useFont(boldFontFile, "Arial", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
                    }
                }
            } catch (Exception fontEx) {
                log.warn("Could not load system Arial font: {}", fontEx.getMessage());
            }

            builder.withHtmlContent(htmlContent, null); 
            builder.toStream(outputStream);
            builder.run();
            
            log.info("PDF Export: Rendering completed successfully.");
        } catch (Exception e) {
            log.error("PDF Export: Error during rendering process: {}", e.getMessage(), e);
            throw new RuntimeException("PDF Rendering Failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportToExcel(String sheetName, List<String> headers, List<Map<String, Object>> data, OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);

            CellStyle zebraStyle = workbook.createCellStyle();
            zebraStyle.cloneStyleFrom(dataStyle);
            zebraStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            zebraStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0 \"Ä‚Â¢Ă¢â‚¬ÂĂ‚Â«\""));

            // Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowNum = 1;
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                boolean isZebra = (rowNum % 2 == 0);
                
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    Object value = rowData.get(header);
                    Cell cell = row.createCell(i);
                    
                    if (header.contains("TiÄ‚Â¡Ă‚Â»Ă‚Ân") || header.contains("Giá") || header.contains("PhĂ„â€Ă‚Â­")) {
                        cell.setCellStyle(currencyStyle);
                    } else if (isZebra) {
                        cell.setCellStyle(zebraStyle);
                    } else {
                        cell.setCellStyle(dataStyle);
                    }

                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // Column Widths
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                if (header.contains("Sản Phẩm") || header.contains("Email")) {
                    sheet.setColumnWidth(i, 15000);
                } else {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1200);
                }
            }

            workbook.write(outputStream);
            log.info("Excel export completed successfully.");
        } catch (Exception e) {
            log.error("Excel Export: Error during generation: {}", e.getMessage(), e);
            throw new RuntimeException("Excel Generation Failed: " + e.getMessage(), e);
        }
    }
}

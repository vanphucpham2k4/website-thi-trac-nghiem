package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.GiaoVienKetQuaSinhVienItemDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Tạo file .xlsx danh sách kết quả (giáo viên).
 */
public final class GiaoVienKetQuaExcelExporter {

    private static final String[] HEADERS = {
            "STT", "MSSV", "Họ", "Tên", "Link truy cập", "Mã code đã dùng",
            "Điểm", "Thời gian nộp", "Nguồn", "Ghi chú"
    };

    private GiaoVienKetQuaExcelExporter() {
    }

    public static byte[] taoXlsx(List<GiaoVienKetQuaSinhVienItemDTO> rows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Kết quả");
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < HEADERS.length; c++) {
                headerRow.createCell(c).setCellValue(HEADERS[c]);
            }
            int stt = 1;
            for (GiaoVienKetQuaSinhVienItemDTO r : rows) {
                Row dataRow = sheet.createRow(stt);
                dataRow.createCell(0).setCellValue(stt);
                dataRow.createCell(1).setCellValue(nullToEmpty(r.getMssv()));
                dataRow.createCell(2).setCellValue(nullToEmpty(r.getHo()));
                dataRow.createCell(3).setCellValue(nullToEmpty(r.getTen()));
                dataRow.createCell(4).setCellValue(nullToEmpty(r.getDuongDanTruyCap()));
                dataRow.createCell(5).setCellValue(nullToEmpty(r.getMaTruyCapDaDung()));
                dataRow.createCell(6).setCellValue(nullToEmpty(r.getDiem()));
                dataRow.createCell(7).setCellValue(nullToEmpty(r.getThoiGianNop()));
                dataRow.createCell(8).setCellValue(nullToEmpty(r.getNguon()));
                dataRow.createCell(9).setCellValue(nullToEmpty(r.getGhiChu()));
                stt++;
            }
            for (int c = 0; c < HEADERS.length; c++) {
                sheet.autoSizeColumn(c);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}

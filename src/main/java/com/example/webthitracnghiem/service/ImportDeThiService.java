package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ImportKetQuaDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service — Import đề thi từ file PDF hoặc DOCX.
 *
 * Định dạng file được hỗ trợ (regex parser nhận diện tự động):
 *
 *   Câu 1: [Nội dung câu hỏi]       hoặc   1. [Nội dung câu hỏi]
 *   A. [Lựa chọn A]                         A) [Lựa chọn A]
 *   B. [Lựa chọn B]                         B) [Lựa chọn B]
 *   C. [Lựa chọn C]                         C) [Lựa chọn C]
 *   D. [Lựa chọn D]                         D) [Lựa chọn D]
 *   Đáp án: A                               ĐÁP ÁN: B
 *
 * Thư viện đề xuất cho tích hợp AI (ngoài phạm vi cài đặt hiện tại):
 * - Spring AI (spring-ai-openai-spring-boot-starter) + GPT-4 / Gemini để parse file phức tạp
 * - OpenAI Java SDK: com.theokanning.openai-gpt3-java
 * - LangChain4j: dev.langchain4j:langchain4j-open-ai
 */
@Service
public class ImportDeThiService {

    // Regex nhận diện đầu câu hỏi: "Câu 1:", "Câu 1.", "1.", "1)"
    private static final Pattern PATTERN_CAU_HOI = Pattern.compile(
            "^(?:Câu\\s*\\d+[:.)]?|\\d+[.):]?)\\s*(.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // Regex nhận diện lựa chọn: "A.", "A)", "A:"
    private static final Pattern PATTERN_LUA_CHON = Pattern.compile(
            "^([A-Da-d])[.):]\\s*(.+)$");

    // Regex nhận diện đáp án: "Đáp án: A", "ĐÁP ÁN: B", "Đáp án đúng: C"
    private static final Pattern PATTERN_DAP_AN = Pattern.compile(
            "^(?:Đáp\\s*án|ĐÁP\\s*ÁN)[^:]*:\\s*([A-Da-d])",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // ================================================================
    // 1. PARSE FILE PDF
    // ================================================================

    /**
     * Trích xuất text từ PDF và parse thành danh sách câu hỏi.
     *
     * @param file File PDF upload từ client
     * @return Kết quả parse với danh sách câu hỏi
     */
    public ImportKetQuaDTO parsePDF(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return taoKetQuaLoi("File PDF không hợp lệ hoặc trống!");
        }

        String loaiFile = file.getContentType();
        if (loaiFile == null || !loaiFile.contains("pdf")) {
            return taoKetQuaLoi("File phải có định dạng PDF!");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            return parseText(rawText);
        } catch (IOException e) {
            return taoKetQuaLoi("Không thể đọc file PDF: " + e.getMessage());
        }
    }

    // ================================================================
    // 2. PARSE FILE DOCX
    // ================================================================

    /**
     * Trích xuất text từ DOCX và parse thành danh sách câu hỏi.
     *
     * @param file File DOCX upload từ client
     * @return Kết quả parse với danh sách câu hỏi
     */
    public ImportKetQuaDTO parseDOCX(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return taoKetQuaLoi("File DOCX không hợp lệ hoặc trống!");
        }

        String loaiFile = file.getContentType();
        boolean isDocx = loaiFile != null && (
                loaiFile.contains("wordprocessingml") ||
                loaiFile.contains("msword") ||
                loaiFile.contains("docx")
        );
        // Fallback: kiểm tra theo tên file
        String tenFile = file.getOriginalFilename();
        if (!isDocx && tenFile != null) {
            isDocx = tenFile.toLowerCase().endsWith(".docx") || tenFile.toLowerCase().endsWith(".doc");
        }

        if (!isDocx) {
            return taoKetQuaLoi("File phải có định dạng DOCX hoặc DOC!");
        }

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            return parseText(sb.toString());
        } catch (IOException e) {
            return taoKetQuaLoi("Không thể đọc file DOCX: " + e.getMessage());
        }
    }

    // ================================================================
    // 3. PARSER LOGIC — Nhận diện cấu trúc câu hỏi từ text thô
    // ================================================================

    /**
     * Parse text thô từ file thành danh sách câu hỏi có cấu trúc.
     *
     * Thuật toán:
     * 1. Tách text thành từng dòng
     * 2. Dòng nào khớp pattern câu hỏi → bắt đầu câu hỏi mới
     * 3. Dòng nào khớp pattern lựa chọn (A/B/C/D) → gán vào câu hỏi hiện tại
     * 4. Dòng nào khớp pattern đáp án → gán đáp án đúng
     */
    private ImportKetQuaDTO parseText(String text) {
        ImportKetQuaDTO ketQua = new ImportKetQuaDTO();
        List<ImportKetQuaDTO.CauHoiImportDTO> cauHoiList = new ArrayList<>();

        String[] lines = text.split("\n");
        ImportKetQuaDTO.CauHoiImportDTO cauHoiHienTai = null;
        StringBuilder noiDungBuffer = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            Matcher matcherCauHoi = PATTERN_CAU_HOI.matcher(line);
            Matcher matcherLuaChon = PATTERN_LUA_CHON.matcher(line);
            Matcher matcherDapAn = PATTERN_DAP_AN.matcher(line);

            if (matcherCauHoi.matches() && !matcherLuaChon.matches()) {
                // Lưu câu hỏi trước (nếu có) khi gặp câu hỏi mới
                if (cauHoiHienTai != null) {
                    if (!noiDungBuffer.isEmpty()) {
                        cauHoiHienTai.setNoiDung(noiDungBuffer.toString().trim());
                    }
                    if (cauHoiHienTai.getNoiDung() != null && !cauHoiHienTai.getNoiDung().isBlank()) {
                        cauHoiList.add(cauHoiHienTai);
                    }
                }
                // Bắt đầu câu hỏi mới
                cauHoiHienTai = new ImportKetQuaDTO.CauHoiImportDTO();
                cauHoiHienTai.setStt(cauHoiList.size() + 1);
                noiDungBuffer = new StringBuilder(matcherCauHoi.group(1).trim());

            } else if (matcherLuaChon.matches() && cauHoiHienTai != null) {
                // Gán nội dung câu hỏi trước khi xử lý lựa chọn
                if (!noiDungBuffer.isEmpty()) {
                    cauHoiHienTai.setNoiDung(noiDungBuffer.toString().trim());
                    noiDungBuffer = new StringBuilder();
                }

                String kyHieu = matcherLuaChon.group(1).toUpperCase();
                String noiDungLuaChon = matcherLuaChon.group(2).trim();

                switch (kyHieu) {
                    case "A" -> cauHoiHienTai.setLuaChonA(noiDungLuaChon);
                    case "B" -> cauHoiHienTai.setLuaChonB(noiDungLuaChon);
                    case "C" -> cauHoiHienTai.setLuaChonC(noiDungLuaChon);
                    case "D" -> cauHoiHienTai.setLuaChonD(noiDungLuaChon);
                }

            } else if (matcherDapAn.matches() && cauHoiHienTai != null) {
                // Gán đáp án đúng
                cauHoiHienTai.setDapAnDung(matcherDapAn.group(1).toUpperCase());

            } else if (cauHoiHienTai != null && noiDungBuffer.length() > 0) {
                // Dòng tiếp theo của nội dung câu hỏi (câu hỏi trải dài nhiều dòng)
                noiDungBuffer.append(" ").append(line);
            }
        }

        // Thêm câu hỏi cuối cùng
        if (cauHoiHienTai != null) {
            if (!noiDungBuffer.isEmpty()) {
                cauHoiHienTai.setNoiDung(noiDungBuffer.toString().trim());
            }
            if (cauHoiHienTai.getNoiDung() != null && !cauHoiHienTai.getNoiDung().isBlank()) {
                cauHoiList.add(cauHoiHienTai);
            }
        }

        // Cập nhật STT chính xác
        for (int i = 0; i < cauHoiList.size(); i++) {
            cauHoiList.get(i).setStt(i + 1);
        }

        if (cauHoiList.isEmpty()) {
            ketQua.setSuccess(false);
            ketQua.setMessage("Không tìm thấy câu hỏi nào trong file. Vui lòng kiểm tra định dạng file.");
        } else {
            ketQua.setSuccess(true);
            ketQua.setMessage("Phân tích file thành công! Tìm thấy " + cauHoiList.size() + " câu hỏi.");
        }

        ketQua.setTongSoCauHoi(cauHoiList.size());
        ketQua.setCauHoiList(cauHoiList);
        return ketQua;
    }

    private ImportKetQuaDTO taoKetQuaLoi(String message) {
        ImportKetQuaDTO ketQua = new ImportKetQuaDTO();
        ketQua.setSuccess(false);
        ketQua.setMessage(message);
        ketQua.setTongSoCauHoi(0);
        ketQua.setCauHoiList(new ArrayList<>());
        return ketQua;
    }
}

package com.example.webthitracnghiem.dto;

import java.util.List;

/**
 * Body POST xuất Excel kết quả — cùng cấu trúc hàng như bảng (sau lọc trên UI).
 */
public class GiaoVienKetQuaExportXlsxRequest {

    private String deThiId;
    /** Gợi ý tên file (tên đề), server sẽ sanitize; có thể null */
    private String fileNameHint;
    private List<GiaoVienKetQuaSinhVienItemDTO> rows;

    public String getDeThiId() {
        return deThiId;
    }

    public void setDeThiId(String deThiId) {
        this.deThiId = deThiId;
    }

    public String getFileNameHint() {
        return fileNameHint;
    }

    public void setFileNameHint(String fileNameHint) {
        this.fileNameHint = fileNameHint;
    }

    public List<GiaoVienKetQuaSinhVienItemDTO> getRows() {
        return rows;
    }

    public void setRows(List<GiaoVienKetQuaSinhVienItemDTO> rows) {
        this.rows = rows;
    }
}

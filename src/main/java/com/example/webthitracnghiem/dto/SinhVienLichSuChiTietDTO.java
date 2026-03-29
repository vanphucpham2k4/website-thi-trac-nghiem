package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

public class SinhVienLichSuChiTietDTO {

    private String phienThiId;
    private String tenDeThi;
    private String tenMonHoc;
    private String thoiGianNop;
    private String tongDiem;
    private String diemToiDa;
    private int soCauDung;
    private int tongSoCau;
    private List<SinhVienCauXemLaiDTO> cacCau = new ArrayList<>();

    public String getPhienThiId() {
        return phienThiId;
    }

    public void setPhienThiId(String phienThiId) {
        this.phienThiId = phienThiId;
    }

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public String getThoiGianNop() {
        return thoiGianNop;
    }

    public void setThoiGianNop(String thoiGianNop) {
        this.thoiGianNop = thoiGianNop;
    }

    public String getTongDiem() {
        return tongDiem;
    }

    public void setTongDiem(String tongDiem) {
        this.tongDiem = tongDiem;
    }

    public String getDiemToiDa() {
        return diemToiDa;
    }

    public void setDiemToiDa(String diemToiDa) {
        this.diemToiDa = diemToiDa;
    }

    public int getSoCauDung() {
        return soCauDung;
    }

    public void setSoCauDung(int soCauDung) {
        this.soCauDung = soCauDung;
    }

    public int getTongSoCau() {
        return tongSoCau;
    }

    public void setTongSoCau(int tongSoCau) {
        this.tongSoCau = tongSoCau;
    }

    public List<SinhVienCauXemLaiDTO> getCacCau() {
        return cacCau;
    }

    public void setCacCau(List<SinhVienCauXemLaiDTO> cacCau) {
        this.cacCau = cacCau != null ? cacCau : new ArrayList<>();
    }
}

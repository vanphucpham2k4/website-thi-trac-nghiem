package com.example.webthitracnghiem.dto;

/**
 * Phần thưởng hiển thị dạng thẻ trên trang đổi thưởng.
 */
public class PhanThuongCardDTO {

    private String id;
    private String ten;
    private String moTaNgan;
    private String loai;
    private int diemDoi;
    private int soLuongConLai;
    private String iconClass;
    /** URL ảnh thẻ; có thể null — khi đó dùng iconClass */
    private String anhUrl;
    /** CO_THE_DOI | KHONG_DU_DIEM | HET_HANG */
    private String trangThaiCard;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getMoTaNgan() {
        return moTaNgan;
    }

    public void setMoTaNgan(String moTaNgan) {
        this.moTaNgan = moTaNgan;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public int getDiemDoi() {
        return diemDoi;
    }

    public void setDiemDoi(int diemDoi) {
        this.diemDoi = diemDoi;
    }

    public int getSoLuongConLai() {
        return soLuongConLai;
    }

    public void setSoLuongConLai(int soLuongConLai) {
        this.soLuongConLai = soLuongConLai;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getAnhUrl() {
        return anhUrl;
    }

    public void setAnhUrl(String anhUrl) {
        this.anhUrl = anhUrl;
    }

    public String getTrangThaiCard() {
        return trangThaiCard;
    }

    public void setTrangThaiCard(String trangThaiCard) {
        this.trangThaiCard = trangThaiCard;
    }
}

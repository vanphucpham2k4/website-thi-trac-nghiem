package com.example.webthitracnghiem.config;

import com.example.webthitracnghiem.model.LoaiPhanThuong;
import com.example.webthitracnghiem.model.PhanThuong;
import com.example.webthitracnghiem.repository.PhanThuongRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Nạp danh mục phần thưởng mẫu khi bảng còn trống (4 vật phẩm; 4 loại kia đã bỏ khỏi catalog).
 */
@Component
@Order(40)
public class DoiThuongDataLoader implements ApplicationRunner {

    private static final String ID_BUT = "a1000001-0001-4001-8001-000000000005";
    private static final String ID_SO_TAY = "a1000001-0001-4001-8001-000000000006";
    private static final String ID_BINH = "a1000001-0001-4001-8001-000000000007";
    private static final String ID_USB = "a1000001-0001-4001-8001-000000000008";

    private static final Map<String, String> ANH_MAC_DINH_THEO_ID = Map.of(
            ID_BUT,
            "https://images.unsplash.com/photo-1585336261022-680e295ce3fe?w=400&auto=format&fit=crop&q=80",
            ID_SO_TAY,
            "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400&auto=format&fit=crop&q=80",
            ID_BINH,
            "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400&auto=format&fit=crop&q=80",
            ID_USB,
            "https://images.unsplash.com/photo-1625948515291-69613efd103f?w=400&auto=format&fit=crop&q=80");

    private final PhanThuongRepository phanThuongRepository;

    public DoiThuongDataLoader(PhanThuongRepository phanThuongRepository) {
        this.phanThuongRepository = phanThuongRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (phanThuongRepository.count() == 0) {
            int t = 1;
            phanThuongRepository.save(pt(ID_BUT,
                    "Bút viết", "Bút gel mực xanh, phù hợp ghi chép trong lớp.", LoaiPhanThuong.VAT_PHAM_HOC_TAP, 700, 25,
                    "fas fa-pen", ANH_MAC_DINH_THEO_ID.get(ID_BUT), t++));
            phanThuongRepository.save(pt(ID_SO_TAY,
                    "Sổ tay", "Sổ tay học tập A5, bìa mềm.", LoaiPhanThuong.VAT_PHAM_HOC_TAP, 800, 20,
                    "fas fa-book", ANH_MAC_DINH_THEO_ID.get(ID_SO_TAY), t++));
            phanThuongRepository.save(pt(ID_BINH,
                    "Bình nước", "Bình giữ nhiệt 500ml.", LoaiPhanThuong.VAT_PHAM_HOC_TAP, 1000, 15,
                    "fas fa-bottle-water", ANH_MAC_DINH_THEO_ID.get(ID_BINH), t++));
            phanThuongRepository.save(pt(ID_USB,
                    "USB 16GB", "USB lưu trữ tài liệu môn học.", LoaiPhanThuong.VAT_PHAM_HOC_TAP, 1200, 0,
                    "fas fa-usb", ANH_MAC_DINH_THEO_ID.get(ID_USB), t++));
        }
        boSungAnhMacDinhNeuThieu();
    }

    private void boSungAnhMacDinhNeuThieu() {
        for (Map.Entry<String, String> e : ANH_MAC_DINH_THEO_ID.entrySet()) {
            phanThuongRepository.findById(e.getKey()).ifPresent(p -> {
                if (p.getAnhUrl() == null || p.getAnhUrl().isBlank()) {
                    p.setAnhUrl(e.getValue());
                    phanThuongRepository.save(p);
                }
            });
        }
    }

    private static PhanThuong pt(String id, String ten, String moTa, LoaiPhanThuong loai, int diem, int ton,
                                 String icon, String anhUrl, int thuTu) {
        PhanThuong p = new PhanThuong();
        p.setId(id);
        p.setTen(ten);
        p.setMoTaNgan(moTa);
        p.setLoai(loai);
        p.setDiemDoi(diem);
        p.setSoLuongConLai(ton);
        p.setIconClass(icon);
        p.setAnhUrl(anhUrl);
        p.setHienThi(true);
        p.setThuTu(thuTu);
        return p;
    }
}

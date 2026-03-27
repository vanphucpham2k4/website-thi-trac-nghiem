package com.example.webthitracnghiem.config;

import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.NguoiDungVaiTro;
import com.example.webthitracnghiem.model.VaiTro;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.NguoiDungVaiTroRepository;
import com.example.webthitracnghiem.repository.VaiTroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DataInitializer — Tự động seed dữ liệu mẫu khi ứng dụng khởi động lần đầu.
 *
 * Dữ liệu được tạo (chỉ khi chưa tồn tại):
 *   1. Vai trò: ADMIN, GIAO_VIEN, SINH_VIEN
 *   2. Tài khoản admin mặc định
 *   3. Môn học (6 môn)
 *   4. Chủ đề (3 chủ đề / môn = 18 chủ đề)
 *   5. Giáo viên mẫu (2 tài khoản)
 *   6. Sinh viên mẫu (5 tài khoản)
 *   7. Câu hỏi mẫu (30 câu MCQ với đáp án và độ khó)
 *   8. Đề thi mẫu (4 đề, 10 câu mỗi đề)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    // ─── Repositories ──────────────────────────────────────────────
    private final VaiTroRepository           vaiTroRepository;
    private final NguoiDungRepository        nguoiDungRepository;
    private final NguoiDungVaiTroRepository  nguoiDungVaiTroRepository;
    private final MonHocRepository           monHocRepository;
    private final ChuDeRepository            chuDeRepository;
    private final CauHoiRepository           cauHoiRepository;
    private final DeThiRepository            deThiRepository;
    private final DeThiCauHoiRepository      deThiCauHoiRepository;
    private final PasswordEncoder            passwordEncoder;

    public DataInitializer(VaiTroRepository vaiTroRepository,
                           NguoiDungRepository nguoiDungRepository,
                           NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                           MonHocRepository monHocRepository,
                           ChuDeRepository chuDeRepository,
                           CauHoiRepository cauHoiRepository,
                           DeThiRepository deThiRepository,
                           DeThiCauHoiRepository deThiCauHoiRepository,
                           PasswordEncoder passwordEncoder) {
        this.vaiTroRepository          = vaiTroRepository;
        this.nguoiDungRepository       = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.monHocRepository          = monHocRepository;
        this.chuDeRepository           = chuDeRepository;
        this.cauHoiRepository          = cauHoiRepository;
        this.deThiRepository           = deThiRepository;
        this.deThiCauHoiRepository     = deThiCauHoiRepository;
        this.passwordEncoder           = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Vai trò
        taoVaiTro();

        // 2. Admin
        taoAdmin();

        // 3. Môn học + Chủ đề (chỉ seed khi chưa có)
        if (monHocRepository.count() == 0) {
            Map<String, MonHoc> monHocMap = taoMonHoc();
            Map<String, ChuDe>  chuDeMap  = taoChuDe(monHocMap);

            // 4. Giáo viên
            NguoiDung gv1 = taoNguoiDung("GV001", "Nguyễn", "Văn Minh",
                    "Nguyễn Văn Minh", "giaovien1@gmail.com", "0901234567", "GIAO_VIEN");
            NguoiDung gv2 = taoNguoiDung("GV002", "Trần",    "Thị Hoa",
                    "Trần Thị Hoa",    "giaovien2@gmail.com", "0912345678", "GIAO_VIEN");

            // 5. Sinh viên
            taoNguoiDung("SV001", "Lê",    "Văn An",    "Lê Văn An",    "sinhvien1@gmail.com", "0921111111", "SINH_VIEN");
            taoNguoiDung("SV002", "Phạm",  "Thị Bình",  "Phạm Thị Bình","sinhvien2@gmail.com", "0922222222", "SINH_VIEN");
            taoNguoiDung("SV003", "Hoàng", "Văn Cường", "Hoàng Văn Cường","sinhvien3@gmail.com","0923333333","SINH_VIEN");
            taoNguoiDung("SV004", "Đỗ",    "Thị Duyên", "Đỗ Thị Duyên", "sinhvien4@gmail.com", "0924444444", "SINH_VIEN");
            taoNguoiDung("SV005", "Vũ",    "Văn Em",    "Vũ Văn Em",    "sinhvien5@gmail.com", "0925555555", "SINH_VIEN");

            // 6. Câu hỏi
            List<CauHoi> cauHoiList = taoCauHoi(chuDeMap, gv1, gv2);

            // 7. Đề thi
            taoDeThi(monHocMap, gv1, gv2, cauHoiList);

            log("✅ Seed dữ liệu mẫu hoàn tất!");
        } else {
            log("Dữ liệu mẫu đã tồn tại — bỏ qua seed.");
        }

        log("Khởi tạo dữ liệu hoàn tất!");
    }

    // ════════════════════════════════════════════════════════════════
    // 1. VAI TRÒ
    // ════════════════════════════════════════════════════════════════
    private void taoVaiTro() {
        record VTData(String ten, String moTa) {}
        List<VTData> list = List.of(
            new VTData("ADMIN",     "Quản trị viên — Người quản lý toàn bộ hệ thống"),
            new VTData("GIAO_VIEN", "Giáo viên — Tạo và quản lý đề thi, ngân hàng câu hỏi"),
            new VTData("SINH_VIEN", "Sinh viên — Tham gia thi trắc nghiệm")
        );
        for (var d : list) {
            if (!vaiTroRepository.existsByTenVaiTro(d.ten())) {
                VaiTro v = new VaiTro();
                v.setId(uuid()); v.setTenVaiTro(d.ten()); v.setMoTa(d.moTa());
                vaiTroRepository.save(v);
                log("Đã tạo vai trò: " + d.ten());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 2. ADMIN
    // ════════════════════════════════════════════════════════════════
    private NguoiDung taoAdmin() {
        final String email = "admin@gmail.com";
        if (nguoiDungRepository.existsByEmail(email)) {
            log("Tài khoản admin đã tồn tại: " + email);
            return nguoiDungRepository.findByEmail(email).orElseThrow();
        }
        NguoiDung admin = taoNguoiDung("ADMIN001","Quản","Trị Viên","Quản Trị Viên",
                email,"0000000000","ADMIN");
        log("✅ Admin: " + email + " / 123456");
        return admin;
    }

    // ════════════════════════════════════════════════════════════════
    // 3. MÔN HỌC
    // ════════════════════════════════════════════════════════════════
    private Map<String, MonHoc> taoMonHoc() {
        record MHData(String key, String ten, String moTa) {}
        List<MHData> list = List.of(
            new MHData("toan",    "Toán Học",           "Môn học về số học, đại số, giải tích và hình học"),
            new MHData("vat_ly",  "Vật Lý",             "Khoa học nghiên cứu các hiện tượng tự nhiên và quy luật vật chất"),
            new MHData("hoa_hoc", "Hóa Học",            "Nghiên cứu về thành phần, cấu trúc và tính chất của chất"),
            new MHData("tieng_anh","Tiếng Anh",         "Ngôn ngữ quốc tế — ngữ pháp, từ vựng và kỹ năng giao tiếp"),
            new MHData("lich_su", "Lịch Sử Việt Nam",   "Nghiên cứu quá trình dựng nước và giữ nước của dân tộc Việt Nam"),
            new MHData("tin_hoc", "Tin Học",             "Khoa học máy tính — lập trình, cơ sở dữ liệu và mạng máy tính")
        );

        Map<String, MonHoc> map = new LinkedHashMap<>();
        for (var d : list) {
            MonHoc mh = new MonHoc();
            mh.setId(uuid()); mh.setTen(d.ten()); mh.setMoTa(d.moTa());
            monHocRepository.save(mh);
            map.put(d.key(), mh);
            log("  Môn học: " + d.ten());
        }
        return map;
    }

    // ════════════════════════════════════════════════════════════════
    // 4. CHỦ ĐỀ (3 chủ đề / môn)
    // ════════════════════════════════════════════════════════════════
    private Map<String, ChuDe> taoChuDe(Map<String, MonHoc> mh) {
        Map<String, ChuDe> map = new LinkedHashMap<>();
        // Toán
        map.put("dai_so",       cd("Đại Số",              mh.get("toan")));
        map.put("giai_tich",    cd("Giải Tích",            mh.get("toan")));
        map.put("hinh_hoc",     cd("Hình Học",             mh.get("toan")));
        // Vật Lý
        map.put("co_hoc",       cd("Cơ Học",               mh.get("vat_ly")));
        map.put("dien_hoc",     cd("Điện Học",             mh.get("vat_ly")));
        map.put("quang_hoc",    cd("Quang Học",            mh.get("vat_ly")));
        // Hóa Học
        map.put("hoa_vo_co",    cd("Hóa Vô Cơ",           mh.get("hoa_hoc")));
        map.put("hoa_huu_co",   cd("Hóa Hữu Cơ",          mh.get("hoa_hoc")));
        map.put("phan_ung_hh",  cd("Phản Ứng Hóa Học",    mh.get("hoa_hoc")));
        // Tiếng Anh
        map.put("ngu_phap",     cd("Ngữ Pháp",             mh.get("tieng_anh")));
        map.put("tu_vung",      cd("Từ Vựng",              mh.get("tieng_anh")));
        map.put("doc_hieu",     cd("Đọc Hiểu",             mh.get("tieng_anh")));
        // Lịch Sử
        map.put("ls_co_dai",    cd("Thời Kỳ Cổ Đại",      mh.get("lich_su")));
        map.put("ls_can_dai",   cd("Thời Kỳ Cận Đại",     mh.get("lich_su")));
        map.put("ls_hien_dai",  cd("Thời Kỳ Hiện Đại",    mh.get("lich_su")));
        // Tin Học
        map.put("lap_trinh",    cd("Lập Trình Cơ Bản",    mh.get("tin_hoc")));
        map.put("co_so_dl",     cd("Cơ Sở Dữ Liệu",       mh.get("tin_hoc")));
        map.put("mang_mt",      cd("Mạng Máy Tính",        mh.get("tin_hoc")));
        return map;
    }

    private ChuDe cd(String ten, MonHoc monHoc) {
        ChuDe c = new ChuDe();
        c.setId(uuid()); c.setTen(ten); c.setMonHoc(monHoc);
        return chuDeRepository.save(c);
    }

    // ════════════════════════════════════════════════════════════════
    // 5 & 6. TẠO NGƯỜI DÙNG (GV + SV)
    // ════════════════════════════════════════════════════════════════
    private NguoiDung taoNguoiDung(String ma, String ho, String ten, String hoTen,
                                    String email, String sdt, String tenVaiTro) {
        if (nguoiDungRepository.existsByEmail(email)) {
            return nguoiDungRepository.findByEmail(email).orElseThrow();
        }
        VaiTro vaiTro = vaiTroRepository.findByTenVaiTro(tenVaiTro).orElseThrow();

        NguoiDung nd = new NguoiDung();
        nd.setId(uuid()); nd.setMaNguoiDung(ma);
        nd.setHo(ho); nd.setTen(ten); nd.setHoTen(hoTen);
        nd.setEmail(email); nd.setSoDienThoai(sdt);
        nd.setMatKhau(passwordEncoder.encode("123456"));
        nd = nguoiDungRepository.save(nd);

        NguoiDungVaiTro ndvt = new NguoiDungVaiTro();
        ndvt.setId(uuid()); ndvt.setNguoiDung(nd); ndvt.setVaiTro(vaiTro);
        nguoiDungVaiTroRepository.save(ndvt);

        log("  Tạo " + tenVaiTro + ": " + email + " / 123456");
        return nd;
    }

    // ════════════════════════════════════════════════════════════════
    // 7. CÂU HỎI MẪU (5 câu / môn × 6 môn = 30 câu)
    // ════════════════════════════════════════════════════════════════
    private List<CauHoi> taoCauHoi(Map<String, ChuDe> cd, NguoiDung gv1, NguoiDung gv2) {
        List<CauHoi> all = new ArrayList<>();

        // ── TOÁN HỌC ──────────────────────────────────────────────
        all.add(ch("Phương trình bậc 2: x² - 5x + 6 = 0 có nghiệm là?",
                "x = 2 và x = 3", "x = 1 và x = 6", "x = -2 và x = -3", "x = 3 và x = -2",
                "A", "DE", cd.get("dai_so"), gv1));
        all.add(ch("Đạo hàm của hàm số f(x) = x³ + 2x² - x + 5 là?",
                "3x² + 4x - 1", "3x² + 2x - 1", "x² + 4x - 1", "3x + 4x - 1",
                "A", "TRUNG_BINH", cd.get("giai_tich"), gv1));
        all.add(ch("Diện tích hình tròn bán kính r = 5cm là? (π ≈ 3.14)",
                "78.5 cm²", "31.4 cm²", "25 cm²", "15.7 cm²",
                "A", "DE", cd.get("hinh_hoc"), gv1));
        all.add(ch("Tích phân ∫(2x + 1)dx từ 0 đến 2 bằng?",
                "6", "4", "8", "5",
                "A", "TRUNG_BINH", cd.get("giai_tich"), gv1));
        all.add(ch("Trong tam giác đều cạnh a, chiều cao h bằng?",
                "a√3/2", "a/2", "a√2/2", "a√3",
                "A", "KHO", cd.get("hinh_hoc"), gv1));

        // ── VẬT LÝ ────────────────────────────────────────────────
        all.add(ch("Công thức tính vận tốc trong chuyển động thẳng đều là?",
                "v = s/t", "v = s × t", "v = s + t", "v = s - t",
                "A", "DE", cd.get("co_hoc"), gv1));
        all.add(ch("Định luật Ohm phát biểu: Cường độ dòng điện I tỉ lệ thuận với?",
                "Hiệu điện thế U", "Điện trở R", "Công suất P", "Điện dung C",
                "A", "DE", cd.get("dien_hoc"), gv1));
        all.add(ch("Tốc độ ánh sáng trong chân không xấp xỉ bằng?",
                "3 × 10⁸ m/s", "3 × 10⁶ m/s", "3 × 10¹⁰ m/s", "3 × 10⁴ m/s",
                "A", "DE", cd.get("quang_hoc"), gv1));
        all.add(ch("Công thức tính gia tốc rơi tự do g trên mặt đất gần bằng?",
                "9.8 m/s²", "10.8 m/s²", "8.9 m/s²", "12 m/s²",
                "A", "DE", cd.get("co_hoc"), gv2));
        all.add(ch("Hiện tượng khúc xạ ánh sáng xảy ra khi ánh sáng đi qua?",
                "Ranh giới hai môi trường trong suốt khác nhau",
                "Gương phẳng",
                "Lăng kính quang phổ",
                "Thấu kính hội tụ",
                "A", "TRUNG_BINH", cd.get("quang_hoc"), gv2));

        // ── HÓA HỌC ───────────────────────────────────────────────
        all.add(ch("Nguyên tử khối của Carbon (C) là?",
                "12", "14", "16", "6",
                "A", "DE", cd.get("hoa_vo_co"), gv2));
        all.add(ch("Công thức phân tử của nước là?",
                "H₂O", "H₂O₂", "HO₂", "OH₂",
                "A", "DE", cd.get("hoa_vo_co"), gv2));
        all.add(ch("Phân tử Glucose có công thức hóa học là?",
                "C₆H₁₂O₆", "C₁₂H₂₂O₁₁", "C₆H₆", "CH₃OH",
                "A", "TRUNG_BINH", cd.get("hoa_huu_co"), gv2));
        all.add(ch("Phản ứng nào sau đây là phản ứng oxi hóa - khử?",
                "Fe + CuSO₄ → FeSO₄ + Cu",
                "NaOH + HCl → NaCl + H₂O",
                "CaCO₃ → CaO + CO₂",
                "AgNO₃ + NaCl → AgCl + NaNO₃",
                "A", "TRUNG_BINH", cd.get("phan_ung_hh"), gv2));
        all.add(ch("Số proton trong hạt nhân nguyên tử Natrium (Na) là?",
                "11", "12", "23", "10",
                "A", "KHO", cd.get("hoa_vo_co"), gv2));

        // ── TIẾNG ANH ─────────────────────────────────────────────
        all.add(ch("Choose the correct tense: 'She _____ (study) English for 5 years.'",
                "has been studying", "is studying", "studies", "studied",
                "A", "TRUNG_BINH", cd.get("ngu_phap"), gv1));
        all.add(ch("What is the synonym of 'happy'?",
                "Joyful", "Sad", "Angry", "Tired",
                "A", "DE", cd.get("tu_vung"), gv1));
        all.add(ch("Which sentence is grammatically correct?",
                "He doesn't like coffee.",
                "He don't like coffee.",
                "He not like coffee.",
                "He isn't like coffee.",
                "A", "DE", cd.get("ngu_phap"), gv1));
        all.add(ch("The word 'benevolent' means?",
                "Kind and generous", "Cruel and harsh", "Wise and clever", "Lazy and idle",
                "A", "KHO", cd.get("tu_vung"), gv1));
        all.add(ch("Choose the correct preposition: 'She arrived ___ Monday morning.'",
                "on", "in", "at", "by",
                "A", "TRUNG_BINH", cd.get("ngu_phap"), gv1));

        // ── LỊCH SỬ VIỆT NAM ──────────────────────────────────────
        all.add(ch("Nhà nước Văn Lang do ai sáng lập?",
                "Hùng Vương", "An Dương Vương", "Đinh Bộ Lĩnh", "Lý Thái Tổ",
                "A", "DE", cd.get("ls_co_dai"), gv2));
        all.add(ch("Chiến thắng Điện Biên Phủ năm 1954 kết thúc cuộc kháng chiến chống?",
                "Thực dân Pháp", "Đế quốc Mỹ", "Quân Mông Cổ", "Quân Thanh",
                "A", "DE", cd.get("ls_hien_dai"), gv2));
        all.add(ch("Phong trào Cần Vương cuối thế kỷ XIX do ai lãnh đạo?",
                "Vua Hàm Nghi", "Phan Bội Châu", "Nguyễn Ái Quốc", "Hoàng Hoa Thám",
                "A", "TRUNG_BINH", cd.get("ls_can_dai"), gv2));
        all.add(ch("Năm 1945, Bác Hồ đọc bản Tuyên ngôn Độc lập tại?",
                "Quảng trường Ba Đình, Hà Nội",
                "Hội trường Thống Nhất, TP.HCM",
                "Ngọ Môn, Huế",
                "Đình Bảng, Bắc Ninh",
                "A", "DE", cd.get("ls_hien_dai"), gv2));
        all.add(ch("Triều đại phong kiến nào tồn tại lâu nhất trong lịch sử Việt Nam?",
                "Nhà Nguyễn (1802-1945)", "Nhà Lý (1009-1225)", "Nhà Trần (1225-1400)", "Nhà Lê (1428-1788)",
                "D", "KHO", cd.get("ls_can_dai"), gv2));

        // ── TIN HỌC ───────────────────────────────────────────────
        all.add(ch("Trong Java, từ khóa nào được dùng để kế thừa?",
                "extends", "implements", "inherit", "super",
                "A", "DE", cd.get("lap_trinh"), gv1));
        all.add(ch("Ngôn ngữ SQL được dùng để làm gì?",
                "Truy vấn và quản lý cơ sở dữ liệu",
                "Xây dựng giao diện web",
                "Lập trình hệ thống",
                "Phát triển ứng dụng di động",
                "A", "DE", cd.get("co_so_dl"), gv1));
        all.add(ch("Địa chỉ IP của một máy tính trong mạng LAN thường thuộc dải?",
                "192.168.x.x", "172.16.x.x", "10.0.x.x", "8.8.x.x",
                "A", "TRUNG_BINH", cd.get("mang_mt"), gv1));
        all.add(ch("Độ phức tạp thời gian của thuật toán sắp xếp nổi bọt (Bubble Sort) là?",
                "O(n²)", "O(n log n)", "O(n)", "O(log n)",
                "A", "TRUNG_BINH", cd.get("lap_trinh"), gv1));
        all.add(ch("Giao thức HTTP hoạt động ở tầng nào trong mô hình OSI?",
                "Tầng Ứng dụng (Application Layer - Layer 7)",
                "Tầng Vận chuyển (Transport Layer - Layer 4)",
                "Tầng Mạng (Network Layer - Layer 3)",
                "Tầng Liên kết dữ liệu (Data Link Layer - Layer 2)",
                "A", "KHO", cd.get("mang_mt"), gv1));

        log("  Đã tạo " + all.size() + " câu hỏi mẫu.");
        return all;
    }

    /** Tạo một câu hỏi MCQ và lưu vào DB */
    private CauHoi ch(String noiDung, String a, String b, String c, String d,
                      String dapAn, String doKho, ChuDe chuDe, NguoiDung gv) {
        CauHoi q = new CauHoi();
        q.setId(uuid());
        q.setNoiDung(noiDung);
        q.setLuaChonA(a); q.setLuaChonB(b); q.setLuaChonC(c); q.setLuaChonD(d);
        q.setDapAnDung(dapAn);
        q.setLoaiCauHoi("TRAC_NGHIEM");
        q.setDoKho(doKho);
        q.setChuDe(chuDe);
        q.setNguoiDung(gv);
        return cauHoiRepository.save(q);
    }

    // ════════════════════════════════════════════════════════════════
    // 8. ĐỀ THI MẪU (4 đề, mỗi đề 10 câu)
    // ════════════════════════════════════════════════════════════════
    private void taoDeThi(Map<String, MonHoc> mh, NguoiDung gv1, NguoiDung gv2,
                          List<CauHoi> cauHoiList) {

        // Phân nhóm câu hỏi theo môn học
        Map<String, List<CauHoi>> nhomTheoMon = nhomCauHoiTheoMon(cauHoiList);

        // ── Đề thi 1: Toán học - Giữa kỳ (CÔNG KHAI)
        taoMotDeThi(
            "Kiểm Tra Giữa Kỳ - Toán Học", mh.get("toan"), gv1,
            45, "DE-TOAN01", "Đề kiểm tra giữa kỳ môn Toán Học dành cho sinh viên năm 1.",
            "CONG_KHAI", 2, true, false,
            LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(30),
            layNCauDau(nhomTheoMon.get("toan"), 5)
        );

        // ── Đề thi 2: Vật Lý - Cuối kỳ (NHÁP)
        taoMotDeThi(
            "Kiểm Tra Cuối Kỳ - Vật Lý", mh.get("vat_ly"), gv1,
            60, "DE-VATLY01", "Đề kiểm tra cuối kỳ môn Vật Lý. Bao gồm Cơ học, Điện học và Quang học.",
            "NHAP", 1, true, true,
            null, null,
            layNCauDau(nhomTheoMon.get("vat_ly"), 5)
        );

        // ── Đề thi 3: Tiếng Anh - Giữa kỳ (CÔNG KHAI)
        taoMotDeThi(
            "Kiểm Tra Giữa Kỳ - Tiếng Anh", mh.get("tieng_anh"), gv2,
            30, "DE-ENG01", "Kiểm tra ngữ pháp và từ vựng Tiếng Anh cơ bản. Thời gian 30 phút.",
            "CONG_KHAI", 3, false, false,
            LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(14),
            layNCauDau(nhomTheoMon.get("tieng_anh"), 5)
        );

        // ── Đề thi 4: Tin Học - Cuối kỳ (NHÁP)
        taoMotDeThi(
            "Kiểm Tra Cuối Kỳ - Tin Học", mh.get("tin_hoc"), gv2,
            60, "DE-TINHOC01", "Đề thi cuối kỳ môn Tin Học gồm các chủ đề Lập trình, CSDL và Mạng máy tính.",
            "NHAP", 1, true, true,
            null, null,
            layNCauDau(nhomTheoMon.get("tin_hoc"), 5)
        );

        log("  Đã tạo 4 đề thi mẫu.");
    }







    private DeThi taoMotDeThi(String ten, MonHoc monHoc, NguoiDung gv,
                               int thoiGianPhut, String maDeThi, String moTa,
                               String trangThai, int soLanToiDa,
                               boolean tronCauHoi, boolean tronDapAn,
                               LocalDateTime thoiGianMo, LocalDateTime thoiGianDong,
                               List<CauHoi> cauHoiList) {

        DeThi dt = new DeThi();
        dt.setId(uuid());
        dt.setMaDeThi(maDeThi);
        dt.setTen(ten);
        dt.setMonHoc(monHoc);
        dt.setNguoiDung(gv);
        dt.setThoiGianPhut(thoiGianPhut);
        dt.setMoTa(moTa);
        dt.setTrangThai(trangThai);
        dt.setSoLanThiToiDa(soLanToiDa);
        dt.setTronCauHoi(tronCauHoi);
        dt.setTronDapAn(tronDapAn);
        dt.setThoiGianMo(thoiGianMo);
        dt.setThoiGianDong(thoiGianDong);
        dt.setThoiGianTao(LocalDateTime.now());
        dt = deThiRepository.save(dt);

        // Liên kết câu hỏi vào đề thi
        for (int i = 0; i < cauHoiList.size(); i++) {
            DeThiCauHoi dc = new DeThiCauHoi();
            dc.setId(uuid());
            dc.setDeThi(dt);
            dc.setCauHoi(cauHoiList.get(i));
            dc.setThuTu(i + 1);
            deThiCauHoiRepository.save(dc);
        }

        log("    Đề thi: \"" + ten + "\" (" + trangThai + ") — " + cauHoiList.size() + " câu");
        return dt;
    }

    // ════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ════════════════════════════════════════════════════════════════

    /** Nhóm câu hỏi theo tên môn học */
    private Map<String, List<CauHoi>> nhomCauHoiTheoMon(List<CauHoi> list) {
        Map<String, List<CauHoi>> map = new LinkedHashMap<>();
        Map<String, String> tenToKey = Map.of(
            "Toán Học", "toan", "Vật Lý", "vat_ly", "Hóa Học", "hoa_hoc",
            "Tiếng Anh", "tieng_anh", "Lịch Sử Việt Nam", "lich_su", "Tin Học", "tin_hoc"
        );
        for (CauHoi c : list) {
            String tenMon = c.getChuDe().getMonHoc().getTen();
            String key = tenToKey.getOrDefault(tenMon, tenMon);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    private List<CauHoi> layNCauDau(List<CauHoi> list, int n) {
        if (list == null) return new ArrayList<>();
        return list.subList(0, Math.min(n, list.size()));
    }

    private String uuid() { return UUID.randomUUID().toString(); }

    private void log(String msg) {
        System.out.println("[DataInitializer] " + msg);
    }

}



package com.example.webthitracnghiem;

import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.KetQuaThi;
import com.example.webthitracnghiem.model.MonHoc;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.PhienThi;
import com.example.webthitracnghiem.repository.DeThiRepository;
import com.example.webthitracnghiem.repository.KetQuaThiRepository;
import com.example.webthitracnghiem.repository.MonHocRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.PhienThiRepository;
import com.example.webthitracnghiem.config.JwtAuthFilter;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.JwtService;
import com.example.webthitracnghiem.service.SinhVienThiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử tích hợp lịch sử thi sinh viên: API JWT, thứ tự theo thời gian nộp giảm dần,
 * và endpoint dashboard đồng bộ với {@link SinhVienThiService#layLichSu}.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SinhVienLichSuThiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(jwtAuthFilter)
                .build();
    }

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private MonHocRepository monHocRepository;

    @Autowired
    private DeThiRepository deThiRepository;

    @Autowired
    private PhienThiRepository phienThiRepository;

    @Autowired
    private KetQuaThiRepository ketQuaThiRepository;

    @Test
    void apiLichSuThi_macNhatDungDauVaDashboardCungDuLieu() throws Exception {
        String idGv = UUID.randomUUID().toString();
        String idSv = UUID.randomUUID().toString();
        String idMon = UUID.randomUUID().toString();
        String idDe = UUID.randomUUID().toString();

        NguoiDung gv = new NguoiDung();
        gv.setId(idGv);
        gv.setMaNguoiDung("gv_ls");
        gv.setHoTen("Giáo viên LS");
        gv.setEmail("gv_ls@test");
        nguoiDungRepository.save(gv);

        NguoiDung sv = new NguoiDung();
        sv.setId(idSv);
        sv.setMaNguoiDung("sv_ls");
        sv.setHoTen("Sinh viên LS");
        sv.setEmail("sv_ls@test");
        nguoiDungRepository.save(sv);

        MonHoc mon = new MonHoc();
        mon.setId(idMon);
        mon.setTen("Môn LS");
        monHocRepository.save(mon);

        DeThi de = new DeThi();
        de.setId(idDe);
        de.setMonHoc(mon);
        de.setMaDeThi("DE-LS");
        de.setTen("Đề lịch sử");
        de.setThoiGianPhut(60);
        de.setNguoiDung(gv);
        de.setTrangThai("CONG_KHAI");
        de.setThangDiemToiDa(BigDecimal.TEN);
        deThiRepository.save(de);

        LocalDateTime t1 = LocalDateTime.of(2026, 1, 10, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 20, 14, 30);

        String pid1 = UUID.randomUUID().toString();
        PhienThi p1 = new PhienThi();
        p1.setId(pid1);
        p1.setDeThi(de);
        p1.setNguoiDung(sv);
        p1.setTrangThai(SinhVienThiService.TT_DA_NOP);
        phienThiRepository.save(p1);

        KetQuaThi k1 = new KetQuaThi();
        k1.setId(UUID.randomUUID().toString());
        k1.setPhienThi(p1);
        k1.setTongDiem(new BigDecimal("6.5"));
        k1.setThoiGianNop(t1);
        ketQuaThiRepository.save(k1);

        String pid2 = UUID.randomUUID().toString();
        PhienThi p2 = new PhienThi();
        p2.setId(pid2);
        p2.setDeThi(de);
        p2.setNguoiDung(sv);
        p2.setTrangThai(SinhVienThiService.TT_DA_NOP);
        phienThiRepository.save(p2);

        KetQuaThi k2 = new KetQuaThi();
        k2.setId(UUID.randomUUID().toString());
        k2.setPhienThi(p2);
        k2.setTongDiem(new BigDecimal("8"));
        k2.setThoiGianNop(t2);
        ketQuaThiRepository.save(k2);

        String token = jwtService.taoToken(idSv, "sv_ls@test", AuthService.ROLE_SINH_VIEN);

        mockMvc.perform(get("/api/sinh-vien/lich-su-thi").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].phienThiId").value(pid2))
                .andExpect(jsonPath("$.data[0].tongDiem").value("8"))
                .andExpect(jsonPath("$.data[1].phienThiId").value(pid1))
                .andExpect(jsonPath("$.data[1].tongDiem").value("6.5"))
                .andExpect(jsonPath("$.data[0].tenMonHoc").value("Môn LS"));

        mockMvc.perform(get("/dashboard/api/sinh-vien/lich-su").param("userId", idSv))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].phienThiId").value(pid2));
    }
}

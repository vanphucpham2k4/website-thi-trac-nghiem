package com.example.webthitracnghiem.config;

import com.example.webthitracnghiem.repository.PhanThuongRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ẩn 4 phần thưởng mẫu đầu (huy hiệu bạc/vàng, khung avatar, voucher) khỏi giao diện đổi thưởng.
 */
@Component
@Order(41)
public class PhanThuongAnBoBonMauRunner implements ApplicationRunner {

    public static final List<String> IDS_AN = List.of(
            "a1000001-0001-4001-8001-000000000001",
            "a1000001-0001-4001-8001-000000000002",
            "a1000001-0001-4001-8001-000000000003",
            "a1000001-0001-4001-8001-000000000004");

    private final PhanThuongRepository phanThuongRepository;

    public PhanThuongAnBoBonMauRunner(PhanThuongRepository phanThuongRepository) {
        this.phanThuongRepository = phanThuongRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String id : IDS_AN) {
            phanThuongRepository.findById(id).ifPresent(p -> {
                if (p.isHienThi()) {
                    p.setHienThi(false);
                    phanThuongRepository.save(p);
                }
            });
        }
    }
}

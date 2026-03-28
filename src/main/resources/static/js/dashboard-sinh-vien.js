/**
 * dashboard-sinh-vien.js - File JavaScript xử lý Dashboard SINH VIÊN
 * Xử lý: Tải dữ liệu từ API, hiển thị biểu đồ, cập nhật UI...
 */

// Biến lưu trữ chart instance để có thể destroy/update
let diemChart = null;

// Chọn storage tương ứng: localStorage (ghi nhớ) hoặc sessionStorage
const storage = (localStorage.getItem('token') ? localStorage : sessionStorage);

/**
 * Kiểm tra JWT token có hết hạn chưa.
 * Hết hạn → redirect về trang đăng nhập kèm tham số expired.
 */
function isTokenExpired() {
    const expiresAt = storage.getItem('tokenExpiresAt');
    if (!expiresAt) return true;
    return Date.now() > parseInt(expiresAt);
}

/**
 * Đổi mã vai trò từ API sang nhãn hiển thị tiếng Việt
 */
function layNhanVaiTroHienThi(maVaiTro) {
    const map = {
        SINH_VIEN: 'Sinh viên',
        GIAO_VIEN: 'Giáo viên',
        ADMIN: 'Quản trị viên'
    };
    return map[maVaiTro] || maVaiTro || 'Người dùng';
}

document.addEventListener('DOMContentLoaded', function() {
    // ===== 1. SIDEBAR TOGGLE =====
    setupSidebar();

    // ===== 2. TẢI DỮ LIỆU DASHBOARD =====
    loadDashboardData();

    // ===== 3. ĐĂNG XUẤT =====
    setupLogout();

    // ===== 4. KHỞI TẠO BIỂU ĐỒ =====
    initChart();
});

// ============================================
// 1. SETUP SIDEBAR
// ============================================
function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');

    // Toggle sidebar on mobile
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // Close sidebar
    if (sidebarClose) {
        sidebarClose.addEventListener('click', () => {
            sidebar.classList.remove('active');
        });
    }

    // Highlight active menu item based on URL
    highlightActiveMenu();
}

// ============================================
// 2. TẢI DỮ LIỆU DASHBOARD TỪ API
// ============================================
async function loadDashboardData() {
    const nguoiDung = storage.getItem('nguoiDung');
    const vaiTro = storage.getItem('vaiTro');
    const token = storage.getItem('token');

    if (!nguoiDung || vaiTro !== 'SINH_VIEN' || !token) {
        window.location.href = '/login';
        return;
    }

    if (isTokenExpired()) {
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login?expired=1';
        return;
    }

    // Parse user data
    let userData;
    try {
        userData = JSON.parse(nguoiDung);
    } catch (e) {
        console.error('Lỗi parse user data:', e);
        window.location.href = '/login';
        return;
    }

    // Cập nhật tên + vai trò trên header (trước đây chỉ gán tên nên người dùng không thấy chữ "Sinh viên")
    document.getElementById('displayName').textContent = userData.hoTen || (userData.ho + ' ' + userData.ten);
    const roleEl = document.getElementById('displayRole');
    if (roleEl) {
        roleEl.textContent = layNhanVaiTroHienThi(vaiTro);
    }

    // Gọi API lấy dữ liệu dashboard
    try {
        const response = await fetch(`/dashboard/api/sinh-vien?userId=${userData.id}`, {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        const result = await response.json();

        if (result.success) {
            // Cập nhật UI với dữ liệu từ API
            updateDashboardUI(result.data);

            // Cập nhật biểu đồ
            updateChart(result.data.diemTheoMon);

            // Cập nhật bài thi gần nhất
            updateRecentExam(result.data.baiThiGanNhat);

            // Cập nhật bảng điểm
            updateBangDiem(result.data.diemTheoMon);
        } else {
            // API trả về lỗi - hiển thị dữ liệu mock
            loadMockData();
        }
    } catch (error) {
        console.error('Lỗi khi gọi API dashboard:', error);
        // Hiển thị dữ liệu mock khi có lỗi
        loadMockData();
    }
}

// ============================================
// 3. CẬP NHẬT GIAO DIỆN VỚI DỮ LIỆU
// ============================================
function updateDashboardUI(data) {
    // Cập nhật thẻ thống kê
    document.getElementById('statTongSoLanThi').textContent = data.tongSoLanThi || 0;
    document.getElementById('statBaiThiHoanThanh').textContent = data.soBaiThiHoanThanh || 0;

    // Điểm trung bình - format 2 chữ số thập phân
    const diemTB = data.diemTrungBinh || 0;
    document.getElementById('statDiemTrungBinh').textContent = parseFloat(diemTB).toFixed(2);

    // Phòng thi — tính năng đang phát triển, không lấy từ API xếp hạng
    const elPhong = document.getElementById('statPhongThi');
    if (elPhong) elPhong.textContent = '—';
}

// ============================================
// 4. KHỞI TẠO BIỂU ĐỒ
// ============================================
function initChart() {
    const ctx = document.getElementById('diemTheoMonChart');
    if (!ctx) return;

    diemChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Điểm Cao Nhất',
                    data: [],
                    backgroundColor: 'rgba(72, 187, 120, 0.8)',
                    borderRadius: 6,
                },
                {
                    label: 'Điểm Trung Bình',
                    data: [],
                    backgroundColor: 'rgba(102, 126, 234, 0.8)',
                    borderRadius: 6,
                },
                {
                    label: 'Điểm Thấp Nhất',
                    data: [],
                    backgroundColor: 'rgba(237, 137, 54, 0.8)',
                    borderRadius: 6,
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 10,
                    ticks: {
                        stepSize: 1
                    },
                    grid: {
                        display: true,
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
}

// ============================================
// 5. CẬP NHẬT BIỂU ĐỒ
// ============================================
function updateChart(diemTheoMon) {
    if (!diemTheoMon || diemTheoMon.length === 0) {
        // Không có dữ liệu
        diemChart.data.labels = ['Chưa có dữ liệu'];
        diemChart.data.datasets[0].data = [0];
        diemChart.data.datasets[1].data = [0];
        diemChart.data.datasets[2].data = [0];
        diemChart.update();
        return;
    }

    // Cập nhật dữ liệu biểu đồ
    diemChart.data.labels = diemTheoMon.map(m => m.tenMon);
    diemChart.data.datasets[0].data = diemTheoMon.map(m => m.diemCaoNhat || 0);
    diemChart.data.datasets[1].data = diemTheoMon.map(m => m.diemTrungBinhMon || 0);
    diemChart.data.datasets[2].data = diemTheoMon.map(m => m.diemThapNhat || 0);
    diemChart.update();
}

// ============================================
// 6. CẬP NHẬT BÀI THI GẦN NHẤT
// ============================================
function updateRecentExam(baiThiGanNhat) {
    const container = document.getElementById('recentExamBody');

    if (!baiThiGanNhat) {
        // Không có bài thi nào
        container.innerHTML = `
            <div class="loading-placeholder">
                <i class="fas fa-inbox"></i>
                <span>Chưa có bài thi nào</span>
            </div>
        `;
        return;
    }

    // Xác định icon màu theo môn học
    let iconClass = 'math';
    let icon = 'fa-calculator';
    if (baiThiGanNhat.tenMonHoc) {
        const tenMon = baiThiGanNhat.tenMonHoc.toLowerCase();
        if (tenMon.includes('vật lý') || tenMon.includes('physics')) {
            iconClass = 'physics';
            icon = 'fa-atom';
        } else if (tenMon.includes('hóa') || tenMon.includes('chemistry')) {
            iconClass = 'chemistry';
            icon = 'fa-flask';
        } else if (tenMon.includes('anh') || tenMon.includes('english')) {
            iconClass = 'english';
            icon = 'fa-globe';
        }
    }

    container.innerHTML = `
        <div class="recent-exam-item">
            <div class="recent-exam-icon ${iconClass}">
                <i class="fas ${icon}"></i>
            </div>
            <div class="recent-exam-details">
                <h4>${baiThiGanNhat.tenDeThi || 'Đề thi'}</h4>
                <p>
                    <i class="fas fa-book"></i> ${baiThiGanNhat.tenMonHoc || 'Môn học'}
                </p>
                <p>
                    <i class="fas fa-calendar"></i> ${baiThiGanNhat.ngayThi || 'N/A'}
                </p>
            </div>
            <div class="recent-exam-score">
                <span class="score">${baiThiGanNhat.diem ? parseFloat(baiThiGanNhat.diem).toFixed(2) : 'N/A'}</span>
                <span class="score-label">Điểm</span>
            </div>
        </div>
    `;
}

// ============================================
// 7. CẬP NHẬT BẢNG ĐIỂM
// ============================================
function updateBangDiem(diemTheoMon) {
    const tbody = document.getElementById('bangDiemBody');

    if (!diemTheoMon || diemTheoMon.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 40px;">
                    <i class="fas fa-inbox"></i> Chưa có dữ liệu điểm
                </td>
            </tr>
        `;
        return;
    }

    // Tạo HTML cho từng hàng
    let html = '';
    diemTheoMon.forEach(mon => {
        html += `
            <tr>
                <td><strong>${mon.tenMon}</strong></td>
                <td>${mon.soLanThi} lần</td>
                <td>${mon.diemThapNhat ? parseFloat(mon.diemThapNhat).toFixed(2) : '-'}</td>
                <td>${mon.diemCaoNhat ? parseFloat(mon.diemCaoNhat).toFixed(2) : '-'}</td>
                <td>
                    <span class="badge badge-info">
                        ${mon.diemTrungBinhMon ? parseFloat(mon.diemTrungBinhMon).toFixed(2) : '-'}
                    </span>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

// ============================================
// 8. LOAD MOCK DATA (Dữ liệu mẫu khi chưa có DB)
// ============================================
function loadMockData() {
    // Cập nhật stats với dữ liệu mock
    document.getElementById('statTongSoLanThi').textContent = '15';
    document.getElementById('statBaiThiHoanThanh').textContent = '12';
    document.getElementById('statDiemTrungBinh').textContent = '7.85';
    const elPhong = document.getElementById('statPhongThi');
    if (elPhong) elPhong.textContent = '—';

    // Mock dữ liệu điểm theo môn
    const mockDiemTheoMon = [
        { tenMon: 'Toán Học', soLanThi: 5, diemCaoNhat: 9.5, diemThapNhat: 6.0, diemTrungBinhMon: 7.8 },
        { tenMon: 'Vật Lý', soLanThi: 4, diemCaoNhat: 8.5, diemThapNhat: 5.5, diemTrungBinhMon: 7.2 },
        { tenMon: 'Hóa Học', soLanThi: 3, diemCaoNhat: 9.0, diemThapNhat: 7.0, diemTrungBinhMon: 8.0 },
        { tenMon: 'Tiếng Anh', soLanThi: 3, diemCaoNhat: 8.0, diemThapNhat: 6.5, diemTrungBinhMon: 7.4 }
    ];

    // Cập nhật biểu đồ với mock data
    updateChart(mockDiemTheoMon);

    // Cập nhật bài thi gần nhất với mock data
    const mockBaiThiGanNhat = {
        tenDeThi: 'Đề Thi Giữa Kỳ - Hóa Học',
        tenMonHoc: 'Hóa Học',
        diem: 8.5,
        ngayThi: '20/03/2026 14:30',
        trangThaiCham: 'Đã chấm'
    };
    updateRecentExam(mockBaiThiGanNhat);

    // Cập nhật bảng điểm với mock data
    updateBangDiem(mockDiemTheoMon);
}

// ============================================
// 9. HIGHLIGHT ACTIVE MENU
// ============================================
function highlightActiveMenu() {
    const currentPath = window.location.pathname;
    const menuItems = document.querySelectorAll('.menu-item');

    menuItems.forEach(item => {
        const href = item.getAttribute('href');
        if (href === currentPath) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

// ============================================
// 10. SETUP LOGOUT
// ============================================
function setupLogout() {
    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', async function(e) {
            e.preventDefault();

            // Gọi API đăng xuất
            try {
                await fetch('/api/logout', {
                    method: 'POST'
                });
            } catch (error) {
                console.error('Lỗi khi gọi API logout:', error);
            }

            // Xóa thông tin người dùng + token khỏi storage (localStorage hoặc sessionStorage)
            storage.removeItem('nguoiDung');
            storage.removeItem('vaiTro');
            storage.removeItem('token');
            storage.removeItem('tokenExpiresAt');

            // Chuyển hướng về trang đăng nhập
            window.location.href = '/login';
        });
    }
}

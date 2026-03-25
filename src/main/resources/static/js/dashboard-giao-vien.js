/**
 * dashboard-giao-vien.js - File JavaScript xử lý Dashboard GIÁO VIÊN
 * Xử lý: Tải dữ liệu từ API, hiển thị biểu đồ, cập nhật UI...
 */

// Biến lưu trữ chart instance
let thongKeChart = null;

// Chọn storage tương ứng: localStorage (ghi nhớ) hoặc sessionStorage
const storage = (localStorage.getItem('token') ? localStorage : sessionStorage);

function isTokenExpired() {
    const expiresAt = storage.getItem('tokenExpiresAt');
    if (!expiresAt) return true;
    return Date.now() > parseInt(expiresAt);
}

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

    // Highlight active menu item
    highlightActiveMenu();
}

// ============================================
// 2. TẢI DỮ LIỆU DASHBOARD TỪ API
// ============================================
async function loadDashboardData() {
    const nguoiDung = storage.getItem('nguoiDung');
    const vaiTro = storage.getItem('vaiTro');
    const token = storage.getItem('token');

    if (!nguoiDung || vaiTro !== 'GIAO_VIEN' || !token) {
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

    document.getElementById('displayName').textContent = userData.hoTen || (userData.ho + ' ' + userData.ten);
    const roleEl = document.getElementById('displayRole');
    if (roleEl) {
        roleEl.textContent = layNhanVaiTroHienThi(vaiTro);
    }

    // Gọi API lấy dữ liệu dashboard
    try {
        const response = await fetch(`/dashboard/api/giao-vien?userId=${userData.id}`, {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        const result = await response.json();

        if (result.success) {
            // Cập nhật UI với dữ liệu từ API
            updateDashboardUI(result.data);

            // Cập nhật biểu đồ
            updateChart(result.data.thongKeTheoMon);

            // Cập nhật đề thi gần nhất
            updateRecentExam(result.data.deThiGanNhat);

            // Cập nhật bảng đề thi
            updateDeThiTable(result.data.deThiGanDay);

            // Cập nhật bảng thống kê môn học
            updateMonHocTable(result.data.thongKeTheoMon);
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
    document.getElementById('statTongSoDeThi').textContent = data.tongSoDeThi || 0;
    document.getElementById('statTongSoCauHoi').textContent = data.tongSoCauHoi || 0;
    document.getElementById('statTongSoLuotThi').textContent = data.tongSoLuotThi || 0;
    document.getElementById('statTongSoSinhVien').textContent = data.tongSoSinhVien || 0;

    // Điểm trung bình (nếu có)
    // Note: GiaoVienDashboardDTO không có điểm trung bình trực tiếp, tính từ thongKeTheoMon
}

// ============================================
// 4. KHỞI TẠO BIỂU ĐỒ
// ============================================
function initChart() {
    const ctx = document.getElementById('thongKeMonChart');
    if (!ctx) return;

    thongKeChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: [
                    'rgba(102, 126, 234, 0.8)',
                    'rgba(237, 137, 54, 0.8)',
                    'rgba(72, 187, 120, 0.8)',
                    'rgba(66, 153, 225, 0.8)',
                    'rgba(159, 122, 234, 0.8)',
                    'rgba(245, 101, 101, 0.8)'
                ],
                borderWidth: 0
            }]
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
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${label}: ${value} lượt thi (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// ============================================
// 5. CẬP NHẬT BIỂU ĐỒ
// ============================================
function updateChart(thongKeTheoMon) {
    if (!thongKeTheoMon || thongKeTheoMon.length === 0) {
        // Không có dữ liệu
        thongKeChart.data.labels = ['Chưa có dữ liệu'];
        thongKeChart.data.datasets[0].data = [1];
        thongKeChart.update();
        return;
    }

    // Cập nhật dữ liệu biểu đồ
    thongKeChart.data.labels = thongKeTheoMon.map(m => m.tenMonHoc);
    thongKeChart.data.datasets[0].data = thongKeTheoMon.map(m => m.soLuotThi);
    thongKeChart.update();
}

// ============================================
// 6. CẬP NHẬT ĐỀ THI GẦN NHẤT
// ============================================
function updateRecentExam(deThiGanNhat) {
    const container = document.getElementById('recentExamBody');

    if (!deThiGanNhat) {
        container.innerHTML = `
            <div class="loading-placeholder">
                <i class="fas fa-inbox"></i>
                <span>Chưa có đề thi nào</span>
            </div>
        `;
        return;
    }

    // Xác định màu badge theo trạng thái
    let badgeClass = 'badge-success';
    let badgeText = deThiGanNhat.trangThai || 'Đang mở';
    if (deThiGanNhat.trangThai === 'Đã đóng') {
        badgeClass = 'badge-danger';
    } else if (deThiGanNhat.trangThai === 'Chưa mở') {
        badgeClass = 'badge-warning';
    }

    container.innerHTML = `
        <div class="recent-exam-item">
            <div class="recent-exam-icon math">
                <i class="fas fa-file-alt"></i>
            </div>
            <div class="recent-exam-details">
                <h4>${deThiGanNhat.tenDeThi || 'Đề thi'}</h4>
                <p>
                    <i class="fas fa-book"></i> ${deThiGanNhat.tenMonHoc || 'Môn học'}
                </p>
                <p>
                    <i class="fas fa-clock"></i> ${deThiGanNhat.thoiGianPhut || 0} phút
                </p>
            </div>
            <div class="recent-exam-score">
                <span class="badge ${badgeClass}">${badgeText}</span>
                <p style="font-size: 0.8rem; color: var(--text-muted); margin-top: 5px;">
                    ${deThiGanNhat.soLuotThi || 0} lượt thi
                </p>
            </div>
        </div>
    `;
}

// ============================================
// 7. CẬP NHẬT BẢNG ĐỀ THI
// ============================================
function updateDeThiTable(deThiList) {
    const tbody = document.getElementById('deThiBody');

    if (!deThiList || deThiList.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 40px;">
                    <i class="fas fa-inbox"></i> Chưa có đề thi nào
                </td>
            </tr>
        `;
        return;
    }

    // Tạo HTML cho từng hàng
    let html = '';
    deThiList.forEach(deThi => {
        // Xác định màu badge
        let badgeClass = 'badge-success';
        if (deThi.trangThai === 'Đã đóng') {
            badgeClass = 'badge-danger';
        } else if (deThi.trangThai === 'Chưa mở') {
            badgeClass = 'badge-warning';
        }

        html += `
            <tr>
                <td><code>${deThi.maDeThi || 'N/A'}</code></td>
                <td><strong>${deThi.tenDeThi}</strong></td>
                <td>${deThi.tenMonHoc}</td>
                <td>${deThi.thoiGianPhut || 0} phút</td>
                <td>${deThi.soLuotThi || 0}</td>
                <td><span class="badge ${badgeClass}">${deThi.trangThai}</span></td>
                <td>
                    <button class="btn btn-sm" style="padding: 5px 10px; background: var(--bg-color); border-radius: 4px;">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm" style="padding: 5px 10px; background: var(--bg-color); border-radius: 4px;">
                        <i class="fas fa-edit"></i>
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

// ============================================
// 8. CẬP NHẬT BẢNG THỐNG KÊ MÔN HỌC
// ============================================
function updateMonHocTable(thongKeTheoMon) {
    const tbody = document.getElementById('monHocBody');

    if (!thongKeTheoMon || thongKeTheoMon.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 40px;">
                    <i class="fas fa-inbox"></i> Chưa có dữ liệu
                </td>
            </tr>
        `;
        return;
    }

    // Tạo HTML cho từng hàng
    let html = '';
    thongKeTheoMon.forEach(mon => {
        html += `
            <tr>
                <td><strong>${mon.tenMonHoc}</strong></td>
                <td>${mon.soDeThi || 0}</td>
                <td>${mon.soCauHoi || 0}</td>
                <td>${mon.soLuotThi || 0}</td>
                <td>
                    <span class="badge badge-info">
                        ${mon.diemTrungBinh ? parseFloat(mon.diemTrungBinh).toFixed(2) : '-'}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm" style="padding: 5px 10px; background: var(--bg-color); border-radius: 4px;">
                        <i class="fas fa-chart-bar"></i> Chi tiết
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

// ============================================
// 9. LOAD MOCK DATA (Dữ liệu mẫu khi chưa có DB)
// ============================================
function loadMockData() {
    // Cập nhật stats với dữ liệu mock
    document.getElementById('statTongSoDeThi').textContent = '8';
    document.getElementById('statTongSoCauHoi').textContent = '245';
    document.getElementById('statTongSoLuotThi').textContent = '156';
    document.getElementById('statTongSoSinhVien').textContent = '42';

    // Mock dữ liệu thống kê theo môn
    const mockThongKeMon = [
        { tenMonHoc: 'Toán Học', soDeThi: 3, soCauHoi: 80, soLuotThi: 65, diemTrungBinh: 7.5 },
        { tenMonHoc: 'Vật Lý', soDeThi: 2, soCauHoi: 60, soLuotThi: 42, diemTrungBinh: 7.2 },
        { tenMonHoc: 'Hóa Học', soDeThi: 2, soCauHoi: 55, soLuotThi: 35, diemTrungBinh: 8.0 },
        { tenMonHoc: 'Tiếng Anh', soDeThi: 1, soCauHoi: 50, soLuotThi: 14, diemTrungBinh: 6.8 }
    ];

    // Cập nhật biểu đồ với mock data
    updateChart(mockThongKeMon);

    // Cập nhật đề thi gần nhất với mock data
    const mockDeThiGanNhat = {
        tenDeThi: 'Đề Thi Giữa Kỳ - Toán Học',
        tenMonHoc: 'Toán Học',
        thoiGianPhut: 45,
        soLuotThi: 15,
        trangThai: 'Đang mở'
    };
    updateRecentExam(mockDeThiGanNhat);

    // Mock danh sách đề thi
    const mockDeThiList = [
        {
            maDeThi: 'TH001',
            tenDeThi: 'Đề Thi Giữa Kỳ - Toán Học',
            tenMonHoc: 'Toán Học',
            thoiGianPhut: 45,
            soLuotThi: 15,
            trangThai: 'Đang mở'
        },
        {
            maDeThi: 'VL001',
            tenDeThi: 'Đề Thi Cuối Kỳ - Vật Lý',
            tenMonHoc: 'Vật Lý',
            thoiGianPhut: 60,
            soLuotThi: 10,
            trangThai: 'Đã đóng'
        },
        {
            maDeThi: 'HH001',
            tenDeThi: 'Đề Thi Thường Xuyên - Hóa Học',
            tenMonHoc: 'Hóa Học',
            thoiGianPhut: 30,
            soLuotThi: 20,
            trangThai: 'Đang mở'
        }
    ];
    updateDeThiTable(mockDeThiList);

    // Cập nhật bảng thống kê môn học
    updateMonHocTable(mockThongKeMon);
}

// ============================================
// 10. HIGHLIGHT ACTIVE MENU
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
// 11. SETUP LOGOUT
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

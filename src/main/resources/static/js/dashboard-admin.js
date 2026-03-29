/**
 * dashboard-admin.js - Xử lý dashboard Admin
 * Tải dữ liệu từ API và cập nhật giao diện
 */

document.addEventListener('DOMContentLoaded', function () {
    // ====== 1. KIỂM TRA ĐĂNG NHẬP + TOKEN HẾT HẠN ======
    const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
    const savedUser = storage.getItem('nguoiDung');
    const savedRole = storage.getItem('vaiTro');
    const savedToken = storage.getItem('token');

    function isTokenExpired() {
        const expiresAt = storage.getItem('tokenExpiresAt');
        if (!expiresAt) return true;
        return Date.now() > parseInt(expiresAt);
    }

    if (!savedUser || savedRole !== 'ADMIN' || !savedToken) {
        window.location.href = '/login/admin';
        return;
    }
    if (isTokenExpired()) {
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login/admin?expired=1';
        return;
    }

    // Hiển thị tên admin
    try {
        const userData = JSON.parse(savedUser);
        const displayName = document.getElementById('displayName');
        if (displayName) {
            displayName.textContent = userData.hoTen || (userData.ho + ' ' + userData.ten);
        }
    } catch (e) {
        console.error('Lỗi parse user data:', e);
    }

    // ====== 2. SIDEBAR TOGGLE ======
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');

    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function () {
            sidebar.classList.toggle('active');
        });
    }

    if (sidebarClose) {
        sidebarClose.addEventListener('click', function () {
            sidebar.classList.remove('active');
        });
    }

    // ====== 3. MENU — chuyển trang ======
    const pageDashboard = document.getElementById('pageDashboard');
    const pageNguoiDung = document.getElementById('pageNguoiDung');
    const pageMonHoc = document.getElementById('pageMonHoc');
    const pageQuanLyDeThi = document.getElementById('pageQuanLyDeThi');
    const pageQuanLyCauHoi = document.getElementById('pageQuanLyCauHoi');
    const mainPageTitle = document.getElementById('mainPageTitle');
    const menuItems = document.querySelectorAll('.menu-item[data-page]');

    function hideAllMainPages() {
        if (pageDashboard) pageDashboard.style.display = 'none';
        if (pageNguoiDung) pageNguoiDung.style.display = 'none';
        if (pageMonHoc) pageMonHoc.style.display = 'none';
        if (pageQuanLyDeThi) pageQuanLyDeThi.style.display = 'none';
        if (pageQuanLyCauHoi) pageQuanLyCauHoi.style.display = 'none';
    }

    function showPage(page) {
        if (page === 'dashboard') {
            hideAllMainPages();
            if (pageDashboard) pageDashboard.style.display = '';
            if (mainPageTitle) mainPageTitle.textContent = 'Dashboard Quản Trị';
            return;
        }
        if (page === 'nguoi-dung') {
            hideAllMainPages();
            if (pageNguoiDung) pageNguoiDung.style.display = '';
            if (mainPageTitle) mainPageTitle.textContent = 'Quản Lý Người Dùng';
            loadAdminUsers();
            return;
        }
        if (page === 'mon-hoc') {
            hideAllMainPages();
            if (pageMonHoc) pageMonHoc.style.display = '';
            if (mainPageTitle) mainPageTitle.textContent = 'Quản Lý Môn Học';
            loadAdminMonHoc();
            return;
        }
        if (page === 'de-thi') {
            hideAllMainPages();
            if (pageQuanLyDeThi) pageQuanLyDeThi.style.display = '';
            if (mainPageTitle) mainPageTitle.textContent = 'Quản Lý Đề Thi';
            loadAdminGiaoVienDeThi();
            return;
        }
        if (page === 'cau-hoi') {
            hideAllMainPages();
            if (pageQuanLyCauHoi) pageQuanLyCauHoi.style.display = '';
            if (mainPageTitle) mainPageTitle.textContent = 'Quản Lý Câu Hỏi';
            loadAdminGiaoVienCauHoi();
            return;
        }
        // Fallback — unknown page, do nothing
    }

    menuItems.forEach(function (item) {
        item.addEventListener('click', function (e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            if (!page || this.classList.contains('logout-btn')) return;
            menuItems.forEach(mi => mi.classList.remove('active'));
            this.classList.add('active');
            showPage(page);
        });
    });

    // ----- Quản lý người dùng (API admin) -----
    let usersCache = [];
    let editOriginalVaiTro = '';

    function authHeadersJson() {
        return {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + savedToken
        };
    }

    async function loadAdminUsers() {
        const tbody = document.getElementById('adminUsersTableBody');
        const msg = document.getElementById('adminUsersMsg');
        if (msg) {
            msg.hidden = true;
            msg.textContent = '';
            msg.className = 'admin-inline-msg';
        }
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:24px;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>';
        }
        try {
            const res = await fetch('/api/admin/nguoi-dung', { headers: authHeadersJson() });
            const json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success || !json.data) {
                if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#e53e3e;">' + (json.message || 'Không tải được danh sách') + '</td></tr>';
                return;
            }
            usersCache = json.data;
            renderAdminUsersTable(usersCache);
        } catch (err) {
            console.error(err);
            if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#e53e3e;">Lỗi kết nối</td></tr>';
        }
    }

    function vaiTroLabel(code) {
        if (code === 'ADMIN') return 'Quản trị';
        if (code === 'GIAO_VIEN') return 'Giáo viên';
        if (code === 'SINH_VIEN') return 'Sinh viên';
        return code || '—';
    }

    function renderAdminUsersTable(list) {
        const tbody = document.getElementById('adminUsersTableBody');
        if (!tbody) return;
        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#a0aec0; padding:24px;">Chưa có tài khoản</td></tr>';
            return;
        }
        let html = '';
        list.forEach(function (u) {
            html += '<tr>';
            html += '<td><code>' + (u.maNguoiDung || '') + '</code></td>';
            html += '<td><strong>' + (u.hoTen || '') + '</strong></td>';
            html += '<td>' + (u.email || '') + '</td>';
            html += '<td>' + (u.soDienThoai || '') + '</td>';
            html += '<td><span class="badge badge-admin">' + vaiTroLabel(u.vaiTro) + '</span></td>';
            html += '<td><div class="admin-action-btns">';
            html += '<button type="button" class="btn btn-sm btn-primary btn-edit-user" data-id="' + u.id + '"><i class="fas fa-edit"></i> Sửa</button>';
            html += '<button type="button" class="btn btn-sm btn-outline btn-pw-user" data-id="' + u.id + '"><i class="fas fa-key"></i> Mật khẩu</button>';
            html += '<button type="button" class="btn btn-sm btn-outline btn-del-user" data-id="' + u.id + '" style="color:#e53e3e;border-color:#feb2b2;"><i class="fas fa-trash"></i> Xóa</button>';
            html += '</div></td></tr>';
        });
        tbody.innerHTML = html;

        tbody.querySelectorAll('.btn-edit-user').forEach(function (btn) {
            btn.addEventListener('click', function () {
                openEditModal(this.getAttribute('data-id'));
            });
        });
        tbody.querySelectorAll('.btn-pw-user').forEach(function (btn) {
            btn.addEventListener('click', function () {
                openPwModal(this.getAttribute('data-id'));
            });
        });
        tbody.querySelectorAll('.btn-del-user').forEach(function (btn) {
            btn.addEventListener('click', function () {
                deleteUser(this.getAttribute('data-id'));
            });
        });
    }

    function findUserById(id) {
        return usersCache.find(function (x) { return x.id === id; });
    }

    function openEditModal(id) {
        const u = findUserById(id);
        if (!u) return;
        document.getElementById('editUserId').value = u.id;
        document.getElementById('editHo').value = u.ho || '';
        document.getElementById('editTen').value = u.ten || '';
        document.getElementById('editEmail').value = u.email || '';
        document.getElementById('editSdt').value = u.soDienThoai || '';
        document.getElementById('editVaiTro').value = u.vaiTro || 'SINH_VIEN';
        editOriginalVaiTro = u.vaiTro || '';
        document.getElementById('modalEditUser').style.display = 'flex';
    }

    function closeEditModal() {
        document.getElementById('modalEditUser').style.display = 'none';
    }

    function openPwModal(id) {
        document.getElementById('pwUserId').value = id;
        document.getElementById('pwNew').value = '';
        document.getElementById('modalResetPw').style.display = 'flex';
    }

    function closePwModal() {
        document.getElementById('modalResetPw').style.display = 'none';
    }

    document.getElementById('btnReloadUsers') && document.getElementById('btnReloadUsers').addEventListener('click', function () {
        loadAdminUsers();
    });

    document.getElementById('modalEditClose') && document.getElementById('modalEditClose').addEventListener('click', closeEditModal);
    document.getElementById('btnCancelEdit') && document.getElementById('btnCancelEdit').addEventListener('click', closeEditModal);
    document.getElementById('modalPwClose') && document.getElementById('modalPwClose').addEventListener('click', closePwModal);
    document.getElementById('btnCancelPw') && document.getElementById('btnCancelPw').addEventListener('click', closePwModal);

    document.getElementById('btnSaveEdit') && document.getElementById('btnSaveEdit').addEventListener('click', async function () {
        const id = document.getElementById('editUserId').value;
        const body = {
            ho: document.getElementById('editHo').value.trim(),
            ten: document.getElementById('editTen').value.trim(),
            email: document.getElementById('editEmail').value.trim(),
            soDienThoai: document.getElementById('editSdt').value.trim()
        };
        const vaiTroMoi = document.getElementById('editVaiTro').value;
        try {
            let res = await fetch('/api/admin/nguoi-dung/' + encodeURIComponent(id), {
                method: 'PUT',
                headers: authHeadersJson(),
                body: JSON.stringify(body)
            });
            let json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success) {
                alert(json.message || 'Lỗi cập nhật');
                return;
            }
            if (vaiTroMoi !== editOriginalVaiTro) {
                res = await fetch('/api/admin/nguoi-dung/' + encodeURIComponent(id) + '/vai-tro', {
                    method: 'PUT',
                    headers: authHeadersJson(),
                    body: JSON.stringify({ maVaiTro: vaiTroMoi })
                });
                json = await res.json();
                if (!json.success) {
                    alert(json.message || 'Lỗi đổi vai trò');
                    return;
                }
            }
            closeEditModal();
            loadAdminUsers();
        } catch (e) {
            console.error(e);
            alert('Lỗi kết nối');
        }
    });

    document.getElementById('btnSavePw') && document.getElementById('btnSavePw').addEventListener('click', async function () {
        const id = document.getElementById('pwUserId').value;
        const matKhauMoi = document.getElementById('pwNew').value;
        if (!matKhauMoi || matKhauMoi.length < 6) {
            alert('Mật khẩu mới tối thiểu 6 ký tự.');
            return;
        }
        try {
            const res = await fetch('/api/admin/nguoi-dung/' + encodeURIComponent(id) + '/mat-khau', {
                method: 'PUT',
                headers: authHeadersJson(),
                body: JSON.stringify({ matKhauMoi: matKhauMoi })
            });
            const json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success) {
                alert(json.message || 'Lỗi đặt mật khẩu');
                return;
            }
            closePwModal();
            alert('Đã đặt lại mật khẩu.');
        } catch (e) {
            console.error(e);
            alert('Lỗi kết nối');
        }
    });

    async function deleteUser(id) {
        if (!confirm('Xóa người dùng này? Thao tác không hoàn tác.')) return;
        try {
            const res = await fetch('/api/admin/nguoi-dung/' + encodeURIComponent(id), {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + savedToken }
            });
            const json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success) {
                alert(json.message || 'Không xóa được');
                return;
            }
            loadAdminUsers();
        } catch (e) {
            console.error(e);
            alert('Lỗi kết nối');
        }
    }

    // ----- Quản lý môn học -----
    let monHocCache = [];
    let monHocModalMode = 'create';

    function escapeHtml(s) {
        if (s == null || s === '') return '';
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function truncateText(s, maxLen) {
        if (s == null || s === '') return '';
        const t = String(s);
        if (t.length <= maxLen) return t;
        return t.substring(0, maxLen) + '…';
    }

    async function loadAdminMonHoc() {
        const tbody = document.getElementById('adminMonHocTableBody');
        const msg = document.getElementById('adminMonHocMsg');
        if (msg) {
            msg.hidden = true;
            msg.textContent = '';
            msg.className = 'admin-inline-msg';
        }
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; padding:24px;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>';
        }
        try {
            const res = await fetch('/api/admin/mon-hoc', { headers: authHeadersJson() });
            const json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success || !json.data) {
                if (tbody) {
                    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#e53e3e;">' + escapeHtml(json.message || 'Không tải được danh sách') + '</td></tr>';
                }
                return;
            }
            monHocCache = json.data;
            renderAdminMonHocTable(monHocCache);
        } catch (err) {
            console.error(err);
            if (tbody) tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#e53e3e;">Lỗi kết nối</td></tr>';
        }
    }

    function renderAdminMonHocTable(list) {
        const tbody = document.getElementById('adminMonHocTableBody');
        if (!tbody) return;
        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#a0aec0; padding:24px;">Chưa có môn học</td></tr>';
            return;
        }
        let html = '';
        list.forEach(function (m) {
            const desc = m.moTa ? truncateText(m.moTa, 120) : '';
            const chuDes = Array.isArray(m.danhSachTenChuDe) ? m.danhSachTenChuDe : [];
            const chuDeHtml = chuDes.length
                ? '<span class="admin-mon-hoc-chu-de">' + chuDes.map(function (t) { return escapeHtml(t); }).join('<span class="admin-mon-hoc-chu-de-sep"> · </span>') + '</span>'
                : '<span style="color:#a0aec0;font-size:0.88rem;">Chưa có chủ đề</span>';
            html += '<tr>';
            html += '<td><strong>' + escapeHtml(m.ten || '') + '</strong></td>';
            html += '<td><span class="admin-mon-hoc-desc">' + (desc ? escapeHtml(desc) : '<span style="color:#a0aec0;">—</span>') + '</span></td>';
            html += '<td>' + chuDeHtml + '</td>';
            html += '<td><div class="admin-action-btns">';
            const safeId = m.id ? String(m.id).replace(/"/g, '') : '';
            html += '<button type="button" class="btn btn-sm btn-outline btn-view-mon-hoc" data-id="' + safeId + '"><i class="fas fa-eye"></i> Xem</button>';
            html += '<button type="button" class="btn btn-sm btn-primary btn-edit-mon-hoc" data-id="' + safeId + '"><i class="fas fa-edit"></i> Sửa</button>';
            html += '<button type="button" class="btn btn-sm btn-outline btn-del-mon-hoc" data-id="' + safeId + '" style="color:#e53e3e;border-color:#feb2b2;"><i class="fas fa-trash"></i> Xóa</button>';
            html += '</div></td></tr>';
        });
        tbody.innerHTML = html;

        tbody.querySelectorAll('.btn-view-mon-hoc').forEach(function (btn) {
            btn.addEventListener('click', function () {
                openMonHocModal('view', this.getAttribute('data-id'));
            });
        });
        tbody.querySelectorAll('.btn-edit-mon-hoc').forEach(function (btn) {
            btn.addEventListener('click', function () {
                openMonHocModal('edit', this.getAttribute('data-id'));
            });
        });
        tbody.querySelectorAll('.btn-del-mon-hoc').forEach(function (btn) {
            btn.addEventListener('click', function () {
                deleteMonHoc(this.getAttribute('data-id'));
            });
        });
    }

    function findMonHocById(id) {
        return monHocCache.find(function (x) { return x.id === id; });
    }

    function setMonHocModalReadonly(ro) {
        const ten = document.getElementById('monHocTen');
        const moTa = document.getElementById('monHocMoTa');
        const chuDeTa = document.getElementById('monHocChuDeTen');
        const btnSave = document.getElementById('btnSaveMonHoc');
        if (ten) {
            if (ro) ten.setAttribute('readonly', 'readonly');
            else ten.removeAttribute('readonly');
        }
        if (moTa) {
            if (ro) moTa.setAttribute('readonly', 'readonly');
            else moTa.removeAttribute('readonly');
        }
        if (chuDeTa) {
            if (ro) chuDeTa.setAttribute('readonly', 'readonly');
            else chuDeTa.removeAttribute('readonly');
        }
        if (btnSave) btnSave.style.display = ro ? 'none' : '';
    }

    function openMonHocModal(mode, id) {
        monHocModalMode = mode;
        const titleEl = document.getElementById('modalMonHocTitle');
        const idEl = document.getElementById('monHocId');
        const tenEl = document.getElementById('monHocTen');
        const moTaEl = document.getElementById('monHocMoTa');
        const chuDeTa = document.getElementById('monHocChuDeTen');
        if (mode === 'create') {
            if (titleEl) titleEl.textContent = 'Thêm môn học';
            if (idEl) idEl.value = '';
            if (tenEl) tenEl.value = '';
            if (moTaEl) moTaEl.value = '';
            if (chuDeTa) chuDeTa.value = '';
            setMonHocModalReadonly(false);
        } else {
            const m = findMonHocById(id);
            if (!m) return;
            if (titleEl) titleEl.textContent = mode === 'view' ? 'Chi tiết môn học' : 'Sửa môn học';
            if (idEl) idEl.value = m.id;
            if (tenEl) tenEl.value = m.ten || '';
            if (moTaEl) moTaEl.value = m.moTa || '';
            if (chuDeTa) {
                const lines = Array.isArray(m.danhSachTenChuDe) ? m.danhSachTenChuDe : [];
                chuDeTa.value = lines.join('\n');
            }
            setMonHocModalReadonly(mode === 'view');
        }
        document.getElementById('modalMonHoc').style.display = 'flex';
    }

    function closeMonHocModal() {
        document.getElementById('modalMonHoc').style.display = 'none';
        setMonHocModalReadonly(false);
    }

    document.getElementById('btnReloadMonHoc') && document.getElementById('btnReloadMonHoc').addEventListener('click', function () {
        loadAdminMonHoc();
    });
    document.getElementById('btnAddMonHoc') && document.getElementById('btnAddMonHoc').addEventListener('click', function () {
        openMonHocModal('create');
    });
    document.getElementById('modalMonHocClose') && document.getElementById('modalMonHocClose').addEventListener('click', closeMonHocModal);
    document.getElementById('btnCancelMonHoc') && document.getElementById('btnCancelMonHoc').addEventListener('click', closeMonHocModal);

    document.getElementById('btnSaveMonHoc') && document.getElementById('btnSaveMonHoc').addEventListener('click', async function () {
        if (monHocModalMode === 'view') return;
        const id = document.getElementById('monHocId').value;
        const body = {
            ten: document.getElementById('monHocTen').value.trim(),
            moTa: document.getElementById('monHocMoTa').value.trim(),
            tenChuDeTheoDong: document.getElementById('monHocChuDeTen')
                ? document.getElementById('monHocChuDeTen').value
                : ''
        };
        const isCreate = !id;
        const url = isCreate ? '/api/admin/mon-hoc' : '/api/admin/mon-hoc/' + encodeURIComponent(id);
        try {
            const res = await fetch(url, {
                method: isCreate ? 'POST' : 'PUT',
                headers: authHeadersJson(),
                body: JSON.stringify(body)
            });
            const json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success) {
                alert(json.message || 'Lỗi lưu');
                return;
            }
            closeMonHocModal();
            loadAdminMonHoc();
        } catch (e) {
            console.error(e);
            alert('Lỗi kết nối');
        }
    });

    async function deleteMonHoc(id) {
        if (!confirm('Xóa môn học này? Chỉ xóa được khi không còn chủ đề và đề thi liên quan.')) return;
        try {
            const res = await fetch('/api/admin/mon-hoc/' + encodeURIComponent(id), {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + savedToken }
            });
            const json = await res.json();
            if (res.status === 401) {
                window.location.href = '/login/admin?expired=1';
                return;
            }
            if (!json.success) {
                alert(json.message || 'Không xóa được');
                return;
            }
            loadAdminMonHoc();
        } catch (e) {
            console.error(e);
            alert('Lỗi kết nối');
        }
    }

    // ====== 4. LOGOUT ======
    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', function (e) {
            e.preventDefault();
            // Xóa session (dùng storage tương ứng với lúc đăng nhập)
            storage.removeItem('nguoiDung');
            storage.removeItem('vaiTro');
            storage.removeItem('token');
            storage.removeItem('tokenExpiresAt');
            // Chuyển về trang login admin
            window.location.href = '/login/admin';
        });
    }

    // ====== 5. TẢI DỮ LIỆU DASHBOARD ======
    loadDashboardData();

    async function loadDashboardData() {
        try {
            const userData = JSON.parse(savedUser);
            const userId = userData.id;

            const response = await fetch(`/api/admin/dashboard?userId=${userId}`, {
                headers: {
                    'Authorization': 'Bearer ' + savedToken
                }
            });
            const result = await response.json();

            if (result.success) {
                renderDashboard(result.data);
            } else {
                console.warn('Lỗi tải dashboard:', result.message);
                renderMockData();
            }
        } catch (error) {
            console.error('Lỗi kết nối API dashboard:', error);
            renderMockData();
        }
    }

    /**
     * Render dữ liệu dashboard từ API
     */
    function renderDashboard(data) {
        // Stats cards
        setStatText('statTongNguoiDung', data.tongSoNguoiDung);
        setStatText('statTongSinhVien', data.tongSoSinhVien);
        setStatText('statTongGiaoVien', data.tongSoGiaoVien);
        setStatText('statTongLuotThi', data.tongSoLuotThi);
        setStatText('statTongDeThi', data.tongSoDeThi);
        setStatText('statTongMonHoc', data.tongSoMonHoc);

        // Điểm
        setStatText('statDiemTB', formatDiem(data.diemTrungBinhHeThong));
        setStatText('statTiLeDo', formatPhanTram(data.tiLeDo));
        setStatText('statCauHoi', data.tongSoCauHoi);

        // Biểu đồ
        renderMonthlyChart(data.thongKeTheoThang);

        // Top giáo viên
        renderTopGiaoVien(data.topGiaoVien);

        // Top sinh viên
        renderTopSinhVien(data.topSinhVien);

        // Người dùng mới
        renderNguoiDungMoi(data.nguoiDungMoi);

        // Đề thi mới
        renderDeThiMoi(data.deThiMoi);
    }

    /**
     * Render dữ liệu mock khi API lỗi
     */
    function renderMockData() {
        setStatText('statTongNguoiDung', 1250);
        setStatText('statTongSinhVien', 1080);
        setStatText('statTongGiaoVien', 165);
        setStatText('statTongLuotThi', 15600);
        setStatText('statTongDeThi', 320);
        setStatText('statTongMonHoc', 12);
        setStatText('statDiemTB', '7.52');
        setStatText('statTiLeDo', '78.5%');
        setStatText('statCauHoi', 8400);

        renderMonthlyChart(null);
        renderTopGiaoVien(null);
        renderTopSinhVien(null);
        renderNguoiDungMoi(null);
        renderDeThiMoi(null);
    }

    /**
     * Biểu đồ lượt thi theo tháng
     */
    function renderMonthlyChart(data) {
        const ctx = document.getElementById('monthlyChart');
        if (!ctx) return;

        // Dữ liệu mặc định
        let labels, thiData, diemData;

        if (data && data.length > 0) {
            labels = data.map(d => d.thang);
            thiData = data.map(d => d.soLuotThi);
            diemData = data.map(d => d.diemTrungBinh);
        } else {
            // Mock data
            labels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
            thiData = [620, 780, 950, 820, 1100, 1340, 1280, 1450, 1620, 1380, 1200, 1060];
            diemData = [7.1, 7.3, 7.5, 7.2, 7.8, 7.6, 7.9, 7.4, 7.7, 8.0, 7.5, 7.6];
        }

        if (window.adminChart) {
            window.adminChart.destroy();
        }

        window.adminChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Lượt thi',
                        data: thiData,
                        backgroundColor: 'rgba(102, 126, 234, 0.7)',
                        borderColor: 'rgba(102, 126, 234, 1)',
                        borderWidth: 1,
                        borderRadius: 6,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Điểm TB',
                        data: diemData,
                        type: 'line',
                        borderColor: 'rgba(237, 137, 54, 1)',
                        backgroundColor: 'rgba(237, 137, 54, 0.1)',
                        borderWidth: 2,
                        pointRadius: 4,
                        pointBackgroundColor: '#ed8936',
                        tension: 0.4,
                        fill: true,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            font: { family: 'Poppins', size: 12 },
                            color: '#718096',
                            usePointStyle: true,
                            padding: 20
                        }
                    },
                    tooltip: {
                        backgroundColor: '#1a202c',
                        titleFont: { family: 'Poppins', size: 13 },
                        bodyFont: { family: 'Poppins', size: 12 },
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { font: { family: 'Poppins', size: 11 }, color: '#718096' }
                    },
                    y: {
                        type: 'linear',
                        position: 'left',
                        grid: { color: 'rgba(0,0,0,0.05)' },
                        ticks: { font: { family: 'Poppins', size: 11 }, color: '#718096' },
                        title: { display: true, text: 'Lượt thi', font: { family: 'Poppins', size: 11 }, color: '#667eea' }
                    },
                    y1: {
                        type: 'linear',
                        position: 'right',
                        grid: { display: false },
                        ticks: {
                            font: { family: 'Poppins', size: 11 },
                            color: '#ed8936',
                            callback: function (value) { return value.toFixed(1); }
                        },
                        title: { display: true, text: 'Điểm TB', font: { family: 'Poppins', size: 11 }, color: '#ed8936' }
                    }
                }
            }
        });
    }

    /**
     * Render top giáo viên
     */
    function renderTopGiaoVien(data) {
        const tbody = document.getElementById('topGiaoVienTable');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#a0aec0; padding:30px;">Chưa có dữ liệu</td></tr>';
            return;
        }

        let html = '';
        data.forEach((gv, index) => {
            html += `
                <tr>
                    <td><span class="rank-badge">${index + 1}</span></td>
                    <td>
                        <div class="user-cell">
                            <div class="user-avatar-small" style="background: linear-gradient(135deg, #ed8936, #dd6b20);">
                                <i class="fas fa-chalkboard-teacher" style="color:#fff; font-size:0.9rem;"></i>
                            </div>
                            <span>${gv.hoTen || 'N/A'}</span>
                        </div>
                    </td>
                    <td>${gv.email || 'N/A'}</td>
                    <td><span class="badge badge-admin"><i class="fas fa-file-alt"></i> ${gv.soDeThi}</span></td>
                    <td><span class="badge badge-info"><i class="fas fa-clipboard-check"></i> ${gv.soLuotThi || 0}</span></td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    /**
     * Render top sinh viên
     */
    function renderTopSinhVien(data) {
        const tbody = document.getElementById('topSinhVienTable');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#a0aec0; padding:30px;">Chưa có dữ liệu</td></tr>';
            return;
        }

        let html = '';
        data.forEach((sv, index) => {
            const rankColors = ['#f6ad55', '#a0aec0', '#ed8936'];
            const rankBg = ['rgba(237,137,54,0.15)', 'rgba(160,174,192,0.15)', 'rgba(237,137,54,0.1)'];
            html += `
                <tr>
                    <td><span class="rank-badge" style="background:${rankBg[index] || 'rgba(160,174,192,0.1)'}; color:${rankColors[index] || '#718096'};">#${sv.xepHang || (index + 1)}</span></td>
                    <td>
                        <div class="user-cell">
                            <div class="user-avatar-small" style="background: linear-gradient(135deg, #48bb78, #38a169);">
                                <i class="fas fa-user" style="color:#fff; font-size:0.9rem;"></i>
                            </div>
                            <span>${sv.hoTen || 'N/A'}</span>
                        </div>
                    </td>
                    <td>${sv.email || 'N/A'}</td>
                    <td><span class="badge badge-success">${formatDiem(sv.diemTrungBinh)}</span></td>
                    <td><span class="badge badge-info">${sv.soLanThi} lần</span></td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    /**
     * Render người dùng mới
     */
    function renderNguoiDungMoi(data) {
        const tbody = document.getElementById('nguoiDungMoiTable');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#a0aec0; padding:30px;">Chưa có dữ liệu</td></tr>';
            return;
        }

        let html = '';
        data.forEach(nd => {
            const isGV = nd.vaiTro === 'GIAO_VIEN';
            const badgeClass = isGV ? 'badge-admin' : 'badge-info';
            const badgeText = isGV ? 'Giáo Viên' : 'Sinh Viên';
            const badgeIcon = isGV ? 'fa-chalkboard-teacher' : 'fa-user-graduate';
            html += `
                <tr>
                    <td>
                        <div class="user-cell">
                            <div class="user-avatar-small" style="background: linear-gradient(135deg, ${isGV ? '#ed8936, #dd6b20' : '#667eea, #764ba2'});">
                                <i class="fas ${badgeIcon}" style="color:#fff; font-size:0.9rem;"></i>
                            </div>
                            <span>${nd.hoTen || 'N/A'}</span>
                        </div>
                    </td>
                    <td>${nd.email || 'N/A'}</td>
                    <td>${nd.soDienThoai || 'N/A'}</td>
                    <td><span class="badge ${badgeClass}"><i class="fas ${badgeIcon}"></i>${badgeText}</span></td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    /**
     * Render đề thi mới
     */
    function renderDeThiMoi(data) {
        const tbody = document.getElementById('deThiMoiTable');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#a0aec0; padding:30px;">Chưa có dữ liệu</td></tr>';
            return;
        }

        let html = '';
        data.forEach(dt => {
            let statusClass = 'badge-info';
            let statusIcon = 'fa-clock';
            if (dt.trangThai === 'Đang mở') {
                statusClass = 'badge-success';
                statusIcon = 'fa-play-circle';
            } else if (dt.trangThai === 'Đã đóng') {
                statusClass = 'badge-warning';
                statusIcon = 'fa-lock';
            }
            html += `
                <tr>
                    <td><strong>${dt.tenDeThi || 'N/A'}</strong></td>
                    <td>${dt.tenMonHoc || 'N/A'}</td>
                    <td>${dt.tenGiaoVien || 'N/A'}</td>
                    <td><span class="badge badge-info"><i class="fas fa-clipboard-check"></i> ${dt.soLuotThi}</span></td>
                    <td><span class="badge ${statusClass}"><i class="fas ${statusIcon}"></i>${dt.trangThai || 'N/A'}</span></td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    // ====== 6. HÀM HỖ TRỢ ======

    /**
     * Đặt text cho phần tử stat
     */
    function setStatText(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value !== null && value !== undefined ? value : '--';
    }

    /**
     * Format điểm
     */
    function formatDiem(diem) {
        if (diem === null || diem === undefined) return '--';
        if (typeof diem === 'number') return diem.toFixed(2);
        return diem;
    }

    /**
     * Format phần trăm
     */
    function formatPhanTram(giaTri) {
        if (giaTri === null || giaTri === undefined) return '--';
        const num = typeof giaTri === 'number' ? giaTri : parseFloat(giaTri);
        if (isNaN(num)) return '--';
        return num.toFixed(1) + '%';
    }

    // ====== 7. COUNTER ANIMATION ======
    animateCounters();

    function animateCounters() {
        const statNumbers = document.querySelectorAll('.stat-number');
        statNumbers.forEach(function (el) {
            const target = parseInt(el.textContent.replace(/[^0-9]/g, '')) || 0;
            if (target === 0) return;

            let current = 0;
            const increment = Math.ceil(target / 60);
            const suffix = el.textContent.replace(/[0-9]/g, '');

            const timer = setInterval(function () {
                current += increment;
                if (current >= target) {
                    current = target;
                    clearInterval(timer);
                }
                el.textContent = current + suffix;
            }, 16);
        });
    }
    // ====== 8. ADMIN QUẢN LÝ ĐỀ THI ======
    let adminDeThiGvCache = [];
    let adminDeThiCurrentGvId = '';
    let adminDeThiCurrentGvName = '';
    let adminDeThiCache = [];
    let adminDeThiPage = 1;
    const ADMIN_DETHI_PER_PAGE = 20;
    let adminDeThiDebounceTimer = null;

    async function loadAdminGiaoVienDeThi() {
        const tbody = document.getElementById('adminDeThiGvTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:24px;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>';
        // Ensure sub-view 1 is visible
        document.getElementById('deThiGvListView').style.display = '';
        document.getElementById('deThiDetailView').style.display = 'none';
        try {
            const res = await fetch('/api/admin/de-thi/giao-vien', { headers: authHeadersJson() });
            if (res.status === 401) { window.location.href = '/login/admin?expired=1'; return; }
            const json = await res.json();
            if (!json.success) { if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e53e3e;">' + (json.message || 'Lỗi') + '</td></tr>'; return; }
            adminDeThiGvCache = json.data || [];
            renderAdminDeThiGvTable(adminDeThiGvCache);
            // Update stats
            let tongDeThi = 0, nhap = 0, ck = 0;
            adminDeThiGvCache.forEach(gv => { tongDeThi += gv.tongDeThi; nhap += gv.soDeThiNhap; ck += gv.soDeThiCongKhai; });
            setStatText('adminStatTongDeThi', tongDeThi);
            setStatText('adminStatDeThiNhap', nhap);
            setStatText('adminStatDeThiCK', ck);
            setStatText('adminStatDeThiGV', adminDeThiGvCache.length);
        } catch (e) { console.error(e); if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e53e3e;">Lỗi kết nối</td></tr>'; }
    }

    function renderAdminDeThiGvTable(list) {
        const tbody = document.getElementById('adminDeThiGvTableBody');
        if (!tbody) return;
        if (!list || list.length === 0) { tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#a0aec0;padding:30px;">Chưa có giảng viên nào</td></tr>'; return; }
        let html = '';
        list.forEach((gv, i) => {
            html += '<tr style="cursor:pointer;" onclick="drillDownDeThiGiaoVien(\'' + gv.nguoiDungId + '\', \'' + escapeHtml(gv.hoTen || '') + '\')">';
            html += '<td>' + (i + 1) + '</td>';
            html += '<td><strong>' + escapeHtml(gv.hoTen || '') + '</strong></td>';
            html += '<td>' + escapeHtml(gv.email || '') + '</td>';
            html += '<td><span class="badge badge-admin">' + gv.tongDeThi + '</span></td>';
            html += '<td>' + gv.soDeThiNhap + '</td>';
            html += '<td>' + gv.soDeThiCongKhai + '</td>';
            html += '<td>' + gv.soMonHoc + '</td>';
            html += '<td><i class="fas fa-chevron-right" style="color:#a0aec0;"></i></td>';
            html += '</tr>';
        });
        tbody.innerHTML = html;
    }

    window.filterAdminDeThiGvList = function() {
        const kw = (document.getElementById('adminDeThiGvSearch').value || '').toLowerCase();
        const filtered = adminDeThiGvCache.filter(gv => (gv.hoTen || '').toLowerCase().includes(kw) || (gv.email || '').toLowerCase().includes(kw));
        renderAdminDeThiGvTable(filtered);
    };

    window.drillDownDeThiGiaoVien = async function(gvId, gvName) {
        adminDeThiCurrentGvId = gvId;
        adminDeThiCurrentGvName = gvName;
        document.getElementById('deThiGvListView').style.display = 'none';
        document.getElementById('deThiDetailView').style.display = '';
        document.getElementById('deThiDetailTitle').textContent = 'Đề thi của: ' + gvName;
        adminDeThiPage = 1;
        // Load filter options
        loadAdminDeThiFilterOptions();
        // Load exams
        await loadDeThiCuaGV();
    };

    window.quayLaiDanhSachGV_DeThi = function() {
        document.getElementById('deThiGvListView').style.display = '';
        document.getElementById('deThiDetailView').style.display = 'none';
    };

    async function loadAdminDeThiFilterOptions() {
        try {
            const res = await fetch('/api/admin/de-thi/mon-hoc', { headers: authHeadersJson() });
            const json = await res.json();
            const sel = document.getElementById('adminDeThiFilterMH');
            if (sel && json.success && json.data) {
                sel.innerHTML = '<option value="">Tất cả môn học</option>';
                json.data.forEach(mh => { sel.innerHTML += '<option value="' + mh.id + '">' + escapeHtml(mh.ten) + '</option>'; });
            }
        } catch(e) { console.error(e); }
    }

    async function loadDeThiCuaGV() {
        const tbody = document.getElementById('adminDeThiTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;padding:40px;color:#a0aec0;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>';
        const monHocId = document.getElementById('adminDeThiFilterMH').value;
        const trangThai = document.getElementById('adminDeThiFilterTT').value;
        const keyword = document.getElementById('adminDeThiFilterKW').value;
        let url = '/api/admin/de-thi/giao-vien/' + encodeURIComponent(adminDeThiCurrentGvId) + '?';
        if (monHocId) url += 'monHocId=' + encodeURIComponent(monHocId) + '&';
        if (trangThai) url += 'trangThai=' + encodeURIComponent(trangThai) + '&';
        if (keyword) url += 'keyword=' + encodeURIComponent(keyword) + '&';
        try {
            const res = await fetch(url, { headers: authHeadersJson() });
            if (res.status === 401) { window.location.href = '/login/admin?expired=1'; return; }
            const json = await res.json();
            if (!json.success) { if (tbody) tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;color:#e53e3e;">' + (json.message || 'Lỗi') + '</td></tr>'; return; }
            adminDeThiCache = json.data || [];
            adminDeThiPage = 1;
            renderAdminDeThiTable();
        } catch(e) { console.error(e); if (tbody) tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;color:#e53e3e;">Lỗi kết nối</td></tr>'; }
    }

    function renderAdminDeThiTable() {
        const tbody = document.getElementById('adminDeThiTableBody');
        if (!tbody) return;
        const total = adminDeThiCache.length;
        document.getElementById('adminDeThiCount').textContent = '(' + total + ' đề thi)';
        if (total === 0) { tbody.innerHTML = '<tr><td colspan="10" style="text-align:center;color:#a0aec0;padding:30px;">Không có đề thi nào</td></tr>'; document.getElementById('adminDeThiPagInfo').textContent = ''; document.getElementById('adminDeThiPagBtns').innerHTML = ''; return; }
        const start = (adminDeThiPage - 1) * ADMIN_DETHI_PER_PAGE;
        const end = Math.min(start + ADMIN_DETHI_PER_PAGE, total);
        const pageItems = adminDeThiCache.slice(start, end);
        let html = '';
        pageItems.forEach((dt, i) => {
            const tt = dt.trangThai === 'CONG_KHAI' ? '<span class="badge badge-success"><i class="fas fa-globe"></i> Công khai</span>' : '<span class="badge badge-warning"><i class="fas fa-pencil-alt"></i> Nháp</span>';
            const ngayTao = dt.thoiGianTao ? new Date(dt.thoiGianTao).toLocaleDateString('vi-VN') : '—';
            html += '<tr>';
            html += '<td>' + (start + i + 1) + '</td>';
            html += '<td><strong>' + escapeHtml(dt.tenDeThi || '') + '</strong></td>';
            html += '<td><code>' + escapeHtml(dt.maDeThi || '') + '</code></td>';
            html += '<td>' + escapeHtml(dt.tenMonHoc || '') + '</td>';
            html += '<td>' + (dt.thoiGianPhut || '—') + ' phút</td>';
            html += '<td>' + dt.soCauHoi + '</td>';
            html += '<td>' + dt.soLuotThi + '</td>';
            html += '<td>' + tt + '</td>';
            html += '<td>' + ngayTao + '</td>';
            html += '<td><div class="admin-action-btns">';
            html += '<button class="btn btn-sm btn-outline" onclick="xemChiTietDeThiAdmin(\'' + dt.id + '\')"><i class="fas fa-eye"></i></button>';
            html += '<button class="btn btn-sm btn-outline" style="color:#e53e3e;border-color:#feb2b2;" onclick="moModalXoaDeThi(\'' + dt.id + '\')"><i class="fas fa-trash"></i></button>';
            html += '</div></td></tr>';
        });
        tbody.innerHTML = html;
        // Pagination
        const totalPages = Math.ceil(total / ADMIN_DETHI_PER_PAGE);
        document.getElementById('adminDeThiPagInfo').textContent = 'Hiển thị ' + (start + 1) + '–' + end + ' / ' + total;
        let pagHtml = '';
        for (let p = 1; p <= totalPages; p++) {
            pagHtml += '<button style="padding:4px 10px;border:1px solid ' + (p === adminDeThiPage ? '#667eea' : '#e2e8f0') + ';border-radius:6px;background:' + (p === adminDeThiPage ? '#667eea' : '#fff') + ';color:' + (p === adminDeThiPage ? '#fff' : '#4a5568') + ';cursor:pointer;font-size:0.85rem;" onclick="adminDeThiGoToPage(' + p + ')">' + p + '</button>';
        }
        document.getElementById('adminDeThiPagBtns').innerHTML = pagHtml;
    }

    window.adminDeThiGoToPage = function(p) { adminDeThiPage = p; renderAdminDeThiTable(); };
    window.applyAdminDeThiFilter = function() { loadDeThiCuaGV(); };
    window.debounceAdminDeThi = function() { clearTimeout(adminDeThiDebounceTimer); adminDeThiDebounceTimer = setTimeout(loadDeThiCuaGV, 400); };

    window.xemChiTietDeThiAdmin = async function(id) {
        const body = document.getElementById('adminDeThiChiTietBody');
        body.innerHTML = '<p style="text-align:center;color:#a0aec0;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</p>';
        document.getElementById('modalAdminChiTietDeThi').style.display = 'flex';
        try {
            const res = await fetch('/api/admin/de-thi/' + encodeURIComponent(id), { headers: authHeadersJson() });
            const json = await res.json();
            if (!json.success) { body.innerHTML = '<p style="color:#e53e3e;text-align:center;">' + (json.message || 'Lỗi') + '</p>'; return; }
            const dt = json.data;
            const tt = dt.trangThai === 'CONG_KHAI' ? '🌐 Công khai' : '📝 Nháp';
            let html = '<div style="display:grid;grid-template-columns:1fr 1fr;gap:12px 24px;margin-bottom:20px;">';
            html += '<div><strong style="color:#718096;">Tên đề:</strong><br>' + escapeHtml(dt.tenDeThi) + '</div>';
            html += '<div><strong style="color:#718096;">Mã đề:</strong><br><code>' + escapeHtml(dt.maDeThi || '') + '</code></div>';
            html += '<div><strong style="color:#718096;">Môn học:</strong><br>' + escapeHtml(dt.tenMonHoc) + '</div>';
            html += '<div><strong style="color:#718096;">Trạng thái:</strong><br>' + tt + '</div>';
            html += '<div><strong style="color:#718096;">Thời gian:</strong><br>' + (dt.thoiGianPhut || '—') + ' phút</div>';
            html += '<div><strong style="color:#718096;">Giáo viên:</strong><br>' + escapeHtml(dt.tenNguoiTao) + '</div>';
            html += '<div><strong style="color:#718096;">Số câu hỏi:</strong><br>' + dt.soCauHoi + '</div>';
            html += '<div><strong style="color:#718096;">Lượt thi:</strong><br>' + dt.soLuotThi + '</div>';
            if (dt.moTa) html += '<div style="grid-column:1/3;"><strong style="color:#718096;">Mô tả:</strong><br>' + escapeHtml(dt.moTa) + '</div>';
            html += '</div>';
            // Question list
            if (dt.danhSachCauHoi && dt.danhSachCauHoi.length > 0) {
                html += '<h4 style="margin:16px 0 8px;color:#2d3748;"><i class="fas fa-list-ol"></i> Danh sách câu hỏi (' + dt.danhSachCauHoi.length + ')</h4>';
                html += '<table style="width:100%;border-collapse:collapse;font-size:0.88rem;"><thead><tr style="background:#f7fafc;"><th style="padding:8px;text-align:left;border-bottom:1px solid #e2e8f0;">#</th><th style="padding:8px;text-align:left;border-bottom:1px solid #e2e8f0;">Nội dung</th><th style="padding:8px;border-bottom:1px solid #e2e8f0;">Độ khó</th><th style="padding:8px;border-bottom:1px solid #e2e8f0;">Đáp án</th></tr></thead><tbody>';
                dt.danhSachCauHoi.forEach((ch, idx) => {
                    const doKhoLabel = ch.doKho === 'DE' ? '😊 Dễ' : ch.doKho === 'KHO' ? '😤 Khó' : '😐 TB';
                    html += '<tr><td style="padding:6px 8px;border-bottom:1px solid #edf2f7;">' + (idx + 1) + '</td>';
                    html += '<td style="padding:6px 8px;border-bottom:1px solid #edf2f7;">' + escapeHtml(truncateText(ch.noiDung, 80)) + '</td>';
                    html += '<td style="padding:6px 8px;border-bottom:1px solid #edf2f7;text-align:center;">' + doKhoLabel + '</td>';
                    html += '<td style="padding:6px 8px;border-bottom:1px solid #edf2f7;text-align:center;"><strong>' + escapeHtml(ch.dapAnDung || '') + '</strong></td></tr>';
                });
                html += '</tbody></table>';
            }
            body.innerHTML = html;
        } catch(e) { console.error(e); body.innerHTML = '<p style="color:#e53e3e;text-align:center;">Lỗi kết nối</p>'; }
    };

    window.moModalXoaDeThi = function(id) {
        document.getElementById('modalAdminXoaDeThi').style.display = 'flex';
        document.getElementById('btnAdminXoaDeThi').onclick = function() { xoaDeThiAdmin(id); };
    };

    async function xoaDeThiAdmin(id) {
        document.getElementById('modalAdminXoaDeThi').style.display = 'none';
        try {
            const res = await fetch('/api/admin/de-thi/' + encodeURIComponent(id), { method: 'DELETE', headers: authHeadersJson() });
            const json = await res.json();
            if (!json.success) { alert(json.message || 'Không xóa được'); return; }
            await loadDeThiCuaGV();
        } catch(e) { console.error(e); alert('Lỗi kết nối'); }
    }

    // ====== 9. ADMIN QUẢN LÝ CÂU HỎI ======
    let adminCauHoiGvCache = [];
    let adminCauHoiCurrentGvId = '';
    let adminCauHoiCurrentGvName = '';
    let adminCauHoiCache = [];
    let adminCauHoiPage = 1;
    const ADMIN_CAUHOI_PER_PAGE = 20;
    let adminCauHoiDebounceTimer = null;

    async function loadAdminGiaoVienCauHoi() {
        const tbody = document.getElementById('adminCauHoiGvTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:24px;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>';
        document.getElementById('cauHoiGvListView').style.display = '';
        document.getElementById('cauHoiDetailView').style.display = 'none';
        try {
            const res = await fetch('/api/admin/cau-hoi/giao-vien', { headers: authHeadersJson() });
            if (res.status === 401) { window.location.href = '/login/admin?expired=1'; return; }
            const json = await res.json();
            if (!json.success) { if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e53e3e;">' + (json.message || 'Lỗi') + '</td></tr>'; return; }
            adminCauHoiGvCache = json.data || [];
            renderAdminCauHoiGvTable(adminCauHoiGvCache);
            let tong = 0, de = 0, tb = 0, kho = 0;
            adminCauHoiGvCache.forEach(gv => { tong += gv.tongCauHoi; de += gv.soCauDe; tb += gv.soCauTrungBinh; kho += gv.soCauKho; });
            setStatText('adminStatTongCauHoi', tong);
            setStatText('adminStatCauHoiDe', de);
            setStatText('adminStatCauHoiTB', tb);
            setStatText('adminStatCauHoiKho', kho);
        } catch(e) { console.error(e); if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e53e3e;">Lỗi kết nối</td></tr>'; }
    }

    function renderAdminCauHoiGvTable(list) {
        const tbody = document.getElementById('adminCauHoiGvTableBody');
        if (!tbody) return;
        if (!list || list.length === 0) { tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#a0aec0;padding:30px;">Chưa có giảng viên nào</td></tr>'; return; }
        let html = '';
        list.forEach((gv, i) => {
            html += '<tr style="cursor:pointer;" onclick="drillDownCauHoiGiaoVien(\'' + gv.nguoiDungId + '\', \'' + escapeHtml(gv.hoTen || '') + '\')">';
            html += '<td>' + (i + 1) + '</td>';
            html += '<td><strong>' + escapeHtml(gv.hoTen || '') + '</strong></td>';
            html += '<td>' + escapeHtml(gv.email || '') + '</td>';
            html += '<td><span class="badge badge-admin">' + gv.tongCauHoi + '</span></td>';
            html += '<td>' + gv.soCauDe + '</td>';
            html += '<td>' + gv.soCauTrungBinh + '</td>';
            html += '<td>' + gv.soCauKho + '</td>';
            html += '<td><i class="fas fa-chevron-right" style="color:#a0aec0;"></i></td>';
            html += '</tr>';
        });
        tbody.innerHTML = html;
    }

    window.filterAdminCauHoiGvList = function() {
        const kw = (document.getElementById('adminCauHoiGvSearch').value || '').toLowerCase();
        const filtered = adminCauHoiGvCache.filter(gv => (gv.hoTen || '').toLowerCase().includes(kw) || (gv.email || '').toLowerCase().includes(kw));
        renderAdminCauHoiGvTable(filtered);
    };

    window.drillDownCauHoiGiaoVien = async function(gvId, gvName) {
        adminCauHoiCurrentGvId = gvId;
        adminCauHoiCurrentGvName = gvName;
        document.getElementById('cauHoiGvListView').style.display = 'none';
        document.getElementById('cauHoiDetailView').style.display = '';
        document.getElementById('cauHoiDetailTitle').textContent = 'Câu hỏi của: ' + gvName;
        adminCauHoiPage = 1;
        loadAdminCauHoiFilterOptions();
        await loadCauHoiCuaGV();
    };

    window.quayLaiDanhSachGV_CauHoi = function() {
        document.getElementById('cauHoiGvListView').style.display = '';
        document.getElementById('cauHoiDetailView').style.display = 'none';
    };

    async function loadAdminCauHoiFilterOptions() {
        try {
            const res = await fetch('/api/admin/cau-hoi/mon-hoc', { headers: authHeadersJson() });
            const json = await res.json();
            const sel = document.getElementById('adminCauHoiFilterMH');
            if (sel && json.success && json.data) {
                sel.innerHTML = '<option value="">Tất cả môn học</option>';
                json.data.forEach(mh => { sel.innerHTML += '<option value="' + mh.id + '">' + escapeHtml(mh.ten) + '</option>'; });
            }
        } catch(e) { console.error(e); }
    }

    window.onAdminCauHoiMonHocChange = async function() {
        const monHocId = document.getElementById('adminCauHoiFilterMH').value;
        const cdSel = document.getElementById('adminCauHoiFilterCD');
        cdSel.innerHTML = '<option value="">Tất cả chủ đề</option>';
        if (monHocId) {
            try {
                const res = await fetch('/api/admin/cau-hoi/chu-de?monHocId=' + encodeURIComponent(monHocId), { headers: authHeadersJson() });
                const json = await res.json();
                if (json.success && json.data) {
                    json.data.forEach(cd => { cdSel.innerHTML += '<option value="' + cd.id + '">' + escapeHtml(cd.ten) + '</option>'; });
                }
            } catch(e) { console.error(e); }
        }
        applyAdminCauHoiFilter();
    };

    async function loadCauHoiCuaGV() {
        const tbody = document.getElementById('adminCauHoiTableBody');
        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:40px;color:#a0aec0;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>';
        const monHocId = document.getElementById('adminCauHoiFilterMH').value;
        const chuDeId = document.getElementById('adminCauHoiFilterCD').value;
        const doKho = document.getElementById('adminCauHoiFilterDK').value;
        const keyword = document.getElementById('adminCauHoiFilterKW').value;
        let url = '/api/admin/cau-hoi/giao-vien/' + encodeURIComponent(adminCauHoiCurrentGvId) + '?';
        if (monHocId) url += 'monHocId=' + encodeURIComponent(monHocId) + '&';
        if (chuDeId) url += 'chuDeId=' + encodeURIComponent(chuDeId) + '&';
        if (doKho) url += 'doKho=' + encodeURIComponent(doKho) + '&';
        if (keyword) url += 'keyword=' + encodeURIComponent(keyword) + '&';
        try {
            const res = await fetch(url, { headers: authHeadersJson() });
            if (res.status === 401) { window.location.href = '/login/admin?expired=1'; return; }
            const json = await res.json();
            if (!json.success) { if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e53e3e;">' + (json.message || 'Lỗi') + '</td></tr>'; return; }
            adminCauHoiCache = json.data || [];
            adminCauHoiPage = 1;
            renderAdminCauHoiTable();
        } catch(e) { console.error(e); if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#e53e3e;">Lỗi kết nối</td></tr>'; }
    }

    function renderAdminCauHoiTable() {
        const tbody = document.getElementById('adminCauHoiTableBody');
        if (!tbody) return;
        const total = adminCauHoiCache.length;
        document.getElementById('adminCauHoiCount').textContent = '(' + total + ' câu hỏi)';
        if (total === 0) { tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#a0aec0;padding:30px;">Không có câu hỏi nào</td></tr>'; document.getElementById('adminCauHoiPagInfo').textContent = ''; document.getElementById('adminCauHoiPagBtns').innerHTML = ''; return; }
        const start = (adminCauHoiPage - 1) * ADMIN_CAUHOI_PER_PAGE;
        const end = Math.min(start + ADMIN_CAUHOI_PER_PAGE, total);
        const pageItems = adminCauHoiCache.slice(start, end);
        let html = '';
        pageItems.forEach((ch, i) => {
            const doKhoLabel = ch.doKho === 'DE' ? '<span style="color:#48bb78;">😊 Dễ</span>' : ch.doKho === 'KHO' ? '<span style="color:#e53e3e;">😤 Khó</span>' : '<span style="color:#ed8936;">😐 TB</span>';
            const loaiLabel = ch.loaiCauHoi === 'DUNG_SAI' ? 'Đ/S' : 'MCQ';
            html += '<tr>';
            html += '<td>' + (start + i + 1) + '</td>';
            html += '<td style="max-width:280px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">' + escapeHtml(truncateText(ch.noiDung, 60)) + '</td>';
            html += '<td><small>' + escapeHtml(ch.tenMonHoc || '') + '<br><em>' + escapeHtml(ch.tenChuDe || '') + '</em></small></td>';
            html += '<td>' + loaiLabel + '</td>';
            html += '<td>' + doKhoLabel + '</td>';
            html += '<td><strong>' + escapeHtml(ch.dapAnDung || '') + '</strong></td>';
            html += '<td>' + ch.soDeThiSuDung + ' đề</td>';
            html += '<td><div class="admin-action-btns">';
            html += '<button class="btn btn-sm btn-outline" onclick="xemChiTietCauHoiAdmin(\'' + ch.id + '\')"><i class="fas fa-eye"></i></button>';
            html += '<button class="btn btn-sm btn-outline" style="color:#e53e3e;border-color:#feb2b2;" onclick="moModalXoaCauHoi(\'' + ch.id + '\')"><i class="fas fa-trash"></i></button>';
            html += '</div></td></tr>';
        });
        tbody.innerHTML = html;
        // Pagination
        const totalPages = Math.ceil(total / ADMIN_CAUHOI_PER_PAGE);
        document.getElementById('adminCauHoiPagInfo').textContent = 'Hiển thị ' + (start + 1) + '–' + end + ' / ' + total;
        let pagHtml = '';
        for (let p = 1; p <= totalPages; p++) {
            pagHtml += '<button style="padding:4px 10px;border:1px solid ' + (p === adminCauHoiPage ? '#667eea' : '#e2e8f0') + ';border-radius:6px;background:' + (p === adminCauHoiPage ? '#667eea' : '#fff') + ';color:' + (p === adminCauHoiPage ? '#fff' : '#4a5568') + ';cursor:pointer;font-size:0.85rem;" onclick="adminCauHoiGoToPage(' + p + ')">' + p + '</button>';
        }
        document.getElementById('adminCauHoiPagBtns').innerHTML = pagHtml;
    }

    window.adminCauHoiGoToPage = function(p) { adminCauHoiPage = p; renderAdminCauHoiTable(); };
    window.applyAdminCauHoiFilter = function() { loadCauHoiCuaGV(); };
    window.debounceAdminCauHoi = function() { clearTimeout(adminCauHoiDebounceTimer); adminCauHoiDebounceTimer = setTimeout(loadCauHoiCuaGV, 400); };
    window.resetAdminCauHoiFilter = function() {
        document.getElementById('adminCauHoiFilterMH').value = '';
        document.getElementById('adminCauHoiFilterCD').innerHTML = '<option value="">Tất cả chủ đề</option>';
        document.getElementById('adminCauHoiFilterDK').value = '';
        document.getElementById('adminCauHoiFilterKW').value = '';
        loadCauHoiCuaGV();
    };

    window.xemChiTietCauHoiAdmin = async function(id) {
        const body = document.getElementById('adminCauHoiChiTietBody');
        body.innerHTML = '<p style="text-align:center;color:#a0aec0;"><i class="fas fa-spinner fa-spin"></i> Đang tải...</p>';
        document.getElementById('modalAdminChiTietCauHoi').style.display = 'flex';
        try {
            const res = await fetch('/api/admin/cau-hoi/' + encodeURIComponent(id), { headers: authHeadersJson() });
            const json = await res.json();
            if (!json.success) { body.innerHTML = '<p style="color:#e53e3e;text-align:center;">' + (json.message || 'Lỗi') + '</p>'; return; }
            const ch = json.data;
            const doKhoLabel = ch.doKho === 'DE' ? '😊 Dễ' : ch.doKho === 'KHO' ? '😤 Khó' : '😐 TB';
            let html = '<div style="margin-bottom:16px;">';
            html += '<div style="display:flex;gap:16px;margin-bottom:12px;flex-wrap:wrap;">';
            html += '<span class="badge badge-admin">' + escapeHtml(ch.tenMonHoc || '') + '</span>';
            html += '<span class="badge badge-info">' + escapeHtml(ch.tenChuDe || '') + '</span>';
            html += '<span>' + doKhoLabel + '</span>';
            html += '<span class="badge">' + (ch.loaiCauHoi === 'DUNG_SAI' ? 'Đúng/Sai' : 'Trắc nghiệm') + '</span>';
            html += '</div>';
            html += '<p style="font-size:0.82rem;color:#718096;">Giảng viên: <strong>' + escapeHtml(ch.tenNguoiTao || '') + '</strong> (' + escapeHtml(ch.emailNguoiTao || '') + ') · Dùng trong ' + ch.soDeThiSuDung + ' đề</p>';
            html += '</div>';
            html += '<div style="background:#f7fafc;border-radius:8px;padding:16px;margin-bottom:16px;border:1px solid #e2e8f0;"><strong>Nội dung:</strong><br>' + escapeHtml(ch.noiDung || '') + '</div>';
            // Choices
            const choices = [['A', ch.luaChonA], ['B', ch.luaChonB], ['C', ch.luaChonC], ['D', ch.luaChonD]];
            html += '<div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;">';
            choices.forEach(([label, val]) => {
                if (!val) return;
                const isCorrect = ch.dapAnDung && ch.dapAnDung.toUpperCase() === label;
                html += '<div style="padding:10px 14px;border-radius:8px;border:1.5px solid ' + (isCorrect ? '#48bb78' : '#e2e8f0') + ';background:' + (isCorrect ? '#f0fff4' : '#fff') + ';">';
                html += '<strong style="color:' + (isCorrect ? '#38a169' : '#718096') + ';">' + label + '.</strong> ' + escapeHtml(val);
                if (isCorrect) html += ' <i class="fas fa-check-circle" style="color:#48bb78;"></i>';
                html += '</div>';
            });
            html += '</div>';
            body.innerHTML = html;
        } catch(e) { console.error(e); body.innerHTML = '<p style="color:#e53e3e;text-align:center;">Lỗi kết nối</p>'; }
    };

    window.moModalXoaCauHoi = function(id) {
        document.getElementById('modalAdminXoaCauHoi').style.display = 'flex';
        document.getElementById('btnAdminXoaCauHoi').onclick = function() { xoaCauHoiAdmin(id); };
    };

    async function xoaCauHoiAdmin(id) {
        document.getElementById('modalAdminXoaCauHoi').style.display = 'none';
        try {
            const res = await fetch('/api/admin/cau-hoi/' + encodeURIComponent(id), { method: 'DELETE', headers: authHeadersJson() });
            const json = await res.json();
            if (!json.success) { alert(json.message || 'Không xóa được'); return; }
            await loadCauHoiCuaGV();
        } catch(e) { console.error(e); alert('Lỗi kết nối'); }
    }

});

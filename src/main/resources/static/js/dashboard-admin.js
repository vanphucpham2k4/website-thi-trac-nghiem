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
    const pagePlaceholder = document.getElementById('pagePlaceholder');
    const mainPageTitle = document.getElementById('mainPageTitle');
    const menuItems = document.querySelectorAll('.menu-item[data-page]');

    const PLACEHOLDER_MENU_TITLES = {
        'de-thi': 'Quản Lý Đề Thi',
        'cau-hoi': 'Quản Lý Câu Hỏi'
    };

    function hideAllMainPages() {
        if (pageDashboard) pageDashboard.style.display = 'none';
        if (pageNguoiDung) pageNguoiDung.style.display = 'none';
        if (pageMonHoc) pageMonHoc.style.display = 'none';
        if (pagePlaceholder) pagePlaceholder.style.display = 'none';
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
        if (PLACEHOLDER_MENU_TITLES[page]) {
            hideAllMainPages();
            if (pagePlaceholder) pagePlaceholder.style.display = '';
            if (mainPageTitle) mainPageTitle.textContent = PLACEHOLDER_MENU_TITLES[page];
            return;
        }
        alert('Chức năng đang được phát triển.');
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
});

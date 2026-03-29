/**
 * Quản lý lớp học (giáo viên): CRUD + modal chọn nhiều sinh viên.
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
const API_LOP = '/api/giao-vien/lop-hoc';
const API_SV = '/api/giao-vien/sinh-vien';

let danhSachSinhVien = [];
let cheDoModal = 'tao';
let lopIdDangSua = null;
let lopIdXoa = null;

function getToken() {
    return storage.getItem('token');
}

function isTokenExpired() {
    const exp = storage.getItem('tokenExpiresAt');
    return !exp || Date.now() > parseInt(exp, 10);
}

function escHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function showToast(message, type = 'error') {
    const c = document.getElementById('toastContainer');
    if (!c) return;
    const el = document.createElement('div');
    el.className = type === 'error' ? 'toast toast-error' : 'toast toast-info';
    el.innerHTML = `<i class="fas fa-${type === 'error' ? 'times-circle' : 'info-circle'}"></i> ${escHtml(message)}`;
    c.appendChild(el);
    setTimeout(() => {
        el.style.opacity = '0';
        setTimeout(() => el.remove(), 300);
    }, 3200);
}

function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const toggle = document.getElementById('sidebarToggle');
    const close = document.getElementById('sidebarClose');
    if (toggle && sidebar) toggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    if (close && sidebar) close.addEventListener('click', () => sidebar.classList.remove('active'));
}

function setupLogout() {
    document.getElementById('btnLogout')?.addEventListener('click', async (e) => {
        e.preventDefault();
        try {
            await fetch('/api/logout', { method: 'POST' });
        } catch (_) {}
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login';
    });
}

function hienThiTen() {
    try {
        const u = JSON.parse(storage.getItem('nguoiDung') || '{}');
        const el = document.getElementById('displayName');
        if (el) el.textContent = u.hoTen || `${u.ho || ''} ${u.ten || ''}`.trim() || '—';
    } catch (_) {}
}

function moModal(id) {
    document.getElementById(id)?.classList.add('active');
}

function dongModal(id) {
    document.getElementById(id)?.classList.remove('active');
}

function formatThoiGian(iso) {
    if (!iso) return '—';
    try {
        const d = new Date(String(iso).replace(' ', 'T'));
        if (Number.isNaN(d.getTime())) return escHtml(iso);
        return d.toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
    } catch (_) {
        return escHtml(iso);
    }
}

function napSelectSinhVien(selectedIds) {
    const sel = document.getElementById('selectSinhVien');
    if (!sel) return;
    const set = new Set(selectedIds || []);
    sel.innerHTML = danhSachSinhVien
        .map((sv) => {
            const id = sv.id;
            const ten = sv.hoTen || sv.maNguoiDung || id;
            const ma = sv.maNguoiDung ? ` (${escHtml(sv.maNguoiDung)})` : '';
            return `<option value="${escHtml(id)}"${set.has(id) ? ' selected' : ''}>${escHtml(ten)}${ma}</option>`;
        })
        .join('');
}

async function taiDanhSachSinhVien() {
    const res = await fetch(API_SV, { headers: { Authorization: `Bearer ${getToken()}` } });
    const json = await res.json();
    if (!json.success) {
        showToast(json.message || 'Không tải được danh sách sinh viên.', 'error');
        danhSachSinhVien = [];
        return;
    }
    danhSachSinhVien = json.data || [];
}

function layIdDaChon() {
    const sel = document.getElementById('selectSinhVien');
    if (!sel) return [];
    return Array.from(sel.selectedOptions).map((o) => o.value);
}

function datChonTatCa(chon) {
    const sel = document.getElementById('selectSinhVien');
    if (!sel) return;
    for (let i = 0; i < sel.options.length; i++) {
        sel.options[i].selected = chon;
    }
}

async function taiBangLop() {
    const body = document.getElementById('bangLopHocBody');
    if (!body) return;
    body.innerHTML = '<tr><td colspan="6" class="empty-state"><i class="fas fa-spinner fa-spin"></i><br>Đang tải…</td></tr>';
    const res = await fetch(API_LOP, { headers: { Authorization: `Bearer ${getToken()}` } });
    const json = await res.json();
    if (!json.success) {
        showToast(json.message || 'Không tải được lớp học.', 'error');
        body.innerHTML = `<tr><td colspan="6" class="empty-state"><i class="fas fa-exclamation-circle"></i><br>${escHtml(json.message || 'Lỗi tải dữ liệu')}</td></tr>`;
        return;
    }
    const rows = json.data || [];
    if (!rows.length) {
        body.innerHTML = '<tr><td colspan="6" class="empty-state"><i class="fas fa-school"></i><br>Chưa có lớp học nào. Nhấn &quot;Tạo lớp học mới&quot; để bắt đầu.</td></tr>';
        return;
    }
    body.innerHTML = rows
        .map((r, i) => {
            const id = escHtml(r.id);
            const ten = escHtml(r.tenLop);
            const ss = typeof r.soSinhVien === 'number' ? r.soSinhVien : 0;
            const chu = escHtml(r.tenChuTri || '—');
            const tg = formatThoiGian(r.thoiGianTao);
            return `<tr>
                <td>${i + 1}</td>
                <td>${ten}</td>
                <td style="text-align:center;font-weight:600;">${ss}</td>
                <td>${chu}</td>
                <td>${tg}</td>
                <td style="text-align:center;white-space:nowrap;">
                    <button type="button" class="btn-icon btn-icon-edit" title="Sửa" data-action="sua" data-id="${id}"><i class="fas fa-pen"></i></button>
                    <button type="button" class="btn-icon btn-icon-del" title="Xóa" data-action="xoa" data-id="${id}" data-ten="${ten}"><i class="fas fa-trash-alt"></i></button>
                    <button type="button" class="btn-icon btn-icon-notify" title="Thông báo (sắp có)" data-action="thongbao"><i class="fas fa-bell"></i></button>
                </td>
            </tr>`;
        })
        .join('');

    body.querySelectorAll('[data-action="sua"]').forEach((btn) => {
        btn.addEventListener('click', () => moSuaLop(btn.getAttribute('data-id')));
    });
    body.querySelectorAll('[data-action="xoa"]').forEach((btn) => {
        btn.addEventListener('click', () => {
            lopIdXoa = btn.getAttribute('data-id');
            const t = btn.getAttribute('data-ten') || '';
            const p = document.getElementById('modalXoaLopText');
            if (p) p.textContent = `Bạn có chắc muốn xóa lớp "${t}"? Thao tác không thể hoàn tác.`;
            moModal('modalXoaLop');
        });
    });
    body.querySelectorAll('[data-action="thongbao"]').forEach((btn) => {
        btn.addEventListener('click', () =>
            showToast('Tính năng thông báo real-time tới sinh viên trong lớp sẽ được phát triển sau.', 'info')
        );
    });
}

function moTaoLop() {
    cheDoModal = 'tao';
    lopIdDangSua = null;
    document.getElementById('modalLopHocTitle').innerHTML =
        '<i class="fas fa-plus-circle" style="color:#667eea;margin-right:8px;"></i>Tạo lớp học mới';
    document.getElementById('inputTenLop').value = '';
    napSelectSinhVien([]);
    moModal('modalLopHoc');
}

async function moSuaLop(lopId) {
    cheDoModal = 'sua';
    lopIdDangSua = lopId;
    document.getElementById('modalLopHocTitle').innerHTML =
        '<i class="fas fa-pen" style="color:#667eea;margin-right:8px;"></i>Sửa lớp học';
    const res = await fetch(`${API_LOP}/${encodeURIComponent(lopId)}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    const json = await res.json();
    if (!json.success || !json.data) {
        showToast(json.message || 'Không tải được chi tiết lớp.', 'error');
        return;
    }
    const d = json.data;
    document.getElementById('inputTenLop').value = d.tenLop || '';
    napSelectSinhVien(d.sinhVienIds || []);
    moModal('modalLopHoc');
}

async function luuLop() {
    const ten = (document.getElementById('inputTenLop')?.value || '').trim();
    if (!ten) {
        showToast('Vui lòng nhập tên lớp.', 'error');
        return;
    }
    const sinhVienIds = layIdDaChon();
    const payload = { tenLop: ten, sinhVienIds };
    let url = API_LOP;
    let method = 'POST';
    if (cheDoModal === 'sua' && lopIdDangSua) {
        url = `${API_LOP}/${encodeURIComponent(lopIdDangSua)}`;
        method = 'PUT';
    }
    const res = await fetch(url, {
        method,
        headers: {
            Authorization: `Bearer ${getToken()}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (!json.success) {
        showToast(json.message || 'Lưu thất bại.', 'error');
        return;
    }
    showToast(json.message || 'Đã lưu.', 'info');
    dongModal('modalLopHoc');
    await taiBangLop();
}

async function xacNhanXoaLop() {
    if (!lopIdXoa) return;
    const res = await fetch(`${API_LOP}/${encodeURIComponent(lopIdXoa)}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    const json = await res.json();
    lopIdXoa = null;
    dongModal('modalXoaLop');
    if (!json.success) {
        showToast(json.message || 'Xóa thất bại.', 'error');
        return;
    }
    showToast(json.message || 'Đã xóa.', 'info');
    await taiBangLop();
}

document.addEventListener('DOMContentLoaded', async () => {
    const vaiTro = storage.getItem('vaiTro');
    const token = getToken();
    if (!token || vaiTro !== 'GIAO_VIEN' || isTokenExpired()) {
        window.location.href = '/login';
        return;
    }

    setupSidebar();
    setupLogout();
    hienThiTen();

    document.getElementById('btnTaoLop')?.addEventListener('click', moTaoLop);
    document.getElementById('modalLopHocClose')?.addEventListener('click', () => dongModal('modalLopHoc'));
    document.getElementById('modalLopHocHuy')?.addEventListener('click', () => dongModal('modalLopHoc'));
    document.getElementById('modalLopHocLuu')?.addEventListener('click', () => luuLop());
    document.getElementById('btnThemTatCaSv')?.addEventListener('click', () => datChonTatCa(true));
    document.getElementById('btnBoChonSv')?.addEventListener('click', () => datChonTatCa(false));

    document.getElementById('modalXoaLopClose')?.addEventListener('click', () => dongModal('modalXoaLop'));
    document.getElementById('modalXoaLopHuy')?.addEventListener('click', () => dongModal('modalXoaLop'));
    document.getElementById('modalXoaLopOk')?.addEventListener('click', () => xacNhanXoaLop());

    document.getElementById('modalLopHoc')?.addEventListener('click', (e) => {
        if (e.target.id === 'modalLopHoc') dongModal('modalLopHoc');
    });
    document.getElementById('modalXoaLop')?.addEventListener('click', (e) => {
        if (e.target.id === 'modalXoaLop') dongModal('modalXoaLop');
    });

    await taiDanhSachSinhVien();
    await taiBangLop();
});

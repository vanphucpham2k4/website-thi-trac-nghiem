/**
 * Sinh viên — Lớp/Phòng Thi: danh sách lớp đã được thêm vào.
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
const API_LOP = '/api/sinh-vien/lop-phong-thi';
/** localStorage: map lopId -> fingerprint danh sách đề (polling realtime) */
const LS_LOP_DE_FP = 'sv_lop_de_fp_v1';

function isTokenExpired() {
    const expiresAt = storage.getItem('tokenExpiresAt');
    if (!expiresAt) return true;
    return Date.now() > parseInt(expiresAt, 10);
}

function layNhanVaiTroHienThi(maVaiTro) {
    const map = { SINH_VIEN: 'Sinh viên', GIAO_VIEN: 'Giáo viên', ADMIN: 'Quản trị viên' };
    return map[maVaiTro] || maVaiTro || 'Người dùng';
}

function getToken() {
    return storage.getItem('token');
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
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    }
    if (sidebarClose && sidebar) {
        sidebarClose.addEventListener('click', () => sidebar.classList.remove('active'));
    }
}

function setupLogout() {
    const btnLogout = document.getElementById('btnLogout');
    if (!btnLogout) return;
    btnLogout.addEventListener('click', async function (e) {
        e.preventDefault();
        try {
            await fetch('/api/logout', { method: 'POST' });
        } catch (err) {
            console.error(err);
        }
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login';
    });
}

function docFpMap() {
    try {
        return JSON.parse(localStorage.getItem(LS_LOP_DE_FP) || '{}');
    } catch {
        return {};
    }
}
function luuFpMap(m) {
    localStorage.setItem(LS_LOP_DE_FP, JSON.stringify(m));
}
/** Chuỗi đại diện danh sách đề trong lớp (để so sánh khi GV xuất bản) */
function vanTayDeThiTrongLop(arr) {
    return (arr || [])
        .map((d) => `${d.deThiId || ''}|${d.thoiGianXuatBan || ''}`)
        .sort()
        .join(';');
}
function chopNhayLopCard(lopId) {
    const id = typeof CSS !== 'undefined' && CSS.escape ? CSS.escape(lopId) : lopId.replace(/"/g, '');
    const el = document.querySelector(`.lop-card[data-lop-id="${id}"]`);
    if (!el) return;
    el.classList.remove('lop-card-flash');
    void el.offsetWidth;
    el.classList.add('lop-card-flash');
    setTimeout(() => el.classList.remove('lop-card-flash'), 1400);
}
let daKhoiTaoFpLop = false;
/** Chữ ký danh sách lớp (chỉ render lại grid khi thay đổi — tránh chớp màn khi poll) */
function chuoiKyLop(list) {
    return (list || [])
        .map((r) => r.lopId)
        .filter(Boolean)
        .sort()
        .join(',');
}
/** Gọi sau khi đã có danh sách lớp: so fingerprint đề, nhấp nháy card nếu có thay đổi */
async function capNhatFingerprintDeThiTheoLop(list) {
    const map = docFpMap();
    for (const row of list || []) {
        const lid = row.lopId;
        if (!lid) continue;
        try {
            const res = await fetch(`${API_LOP}/${encodeURIComponent(lid)}/de-thi`, {
                headers: { Authorization: `Bearer ${getToken()}` }
            });
            const json = await res.json();
            if (!json.success) continue;
            const fpMoi = vanTayDeThiTrongLop(json.data);
            const fpCu = map[lid];
            if (daKhoiTaoFpLop && fpCu !== undefined && fpCu !== fpMoi) {
                chopNhayLopCard(lid);
            }
            map[lid] = fpMoi;
        } catch (e) {
            console.error(e);
        }
    }
    luuFpMap(map);
    daKhoiTaoFpLop = true;
}

function renderLopGrid(list) {
    const loading = document.getElementById('lopGridLoading');
    const grid = document.getElementById('lopGrid');
    if (!grid) return;
    if (loading) loading.style.display = 'none';
    grid.style.display = 'grid';
    if (!list.length) {
        grid.innerHTML =
            '<div class="empty-state" style="grid-column:1/-1;"><i class="fas fa-inbox"></i><br>Bạn chưa được thêm vào lớp nào. Khi giáo viên tạo lớp và chọn bạn, lớp sẽ hiện tại đây.</div>';
        return;
    }
    grid.innerHTML = list
        .map((row) => {
            const id = escHtml(row.lopId);
            const ten = escHtml(row.tenLop || 'Lớp');
            const chu = escHtml(row.tenChuTri || '—');
            return `<button type="button" class="lop-card" data-lop-id="${id}">
                <p class="lop-card-title"><i class="fas fa-chalkboard"></i>${ten}</p>
                <p class="lop-card-meta">Chủ trì (giáo viên): <strong>${chu}</strong></p>
            </button>`;
        })
        .join('');
    grid.querySelectorAll('.lop-card').forEach((btn) => {
        btn.addEventListener('click', () => {
            const lid = btn.getAttribute('data-lop-id');
            if (lid) window.location.href = `/dashboard/sinh-vien/phong-thi/${encodeURIComponent(lid)}`;
        });
    });
    grid.dataset.lopSig = chuoiKyLop(list);
}

/** Poll nhẹ: không vẽ lại card lớp nếu danh sách lớp không đổi */
async function pollPhongThi() {
    const res = await fetch(API_LOP, { headers: { Authorization: `Bearer ${getToken()}` } });
    const json = await res.json();
    if (res.status === 401) {
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login?expired=1';
        return;
    }
    if (!json.success) return;
    const data = json.data || [];
    const grid = document.getElementById('lopGrid');
    const sig = chuoiKyLop(data);
    if (grid && grid.dataset.lopSig !== sig) {
        renderLopGrid(data);
    }
    await capNhatFingerprintDeThiTheoLop(data);
}

async function taiDanhSachLop() {
    const res = await fetch(API_LOP, { headers: { Authorization: `Bearer ${getToken()}` } });
    const json = await res.json();
    if (res.status === 401 || !json.success) {
        if (res.status === 401) {
            storage.removeItem('nguoiDung');
            storage.removeItem('vaiTro');
            storage.removeItem('token');
            storage.removeItem('tokenExpiresAt');
            window.location.href = '/login?expired=1';
            return;
        }
        showToast(json.message || 'Không tải được danh sách lớp.', 'error');
        renderLopGrid([]);
        return;
    }
    const data = json.data || [];
    renderLopGrid(data);
    capNhatFingerprintDeThiTheoLop(data);
}

document.addEventListener('DOMContentLoaded', function () {
    const vaiTro = storage.getItem('vaiTro');
    const token = storage.getItem('token');
    const nguoiDung = storage.getItem('nguoiDung');

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

    try {
        const userData = JSON.parse(nguoiDung);
        const nameEl = document.getElementById('displayName');
        if (nameEl) nameEl.textContent = userData.hoTen || `${userData.ho || ''} ${userData.ten || ''}`.trim() || '—';
        const roleEl = document.getElementById('displayRole');
        if (roleEl) roleEl.textContent = layNhanVaiTroHienThi(vaiTro);
    } catch (e) {
        console.error(e);
    }

    setupSidebar();
    setupLogout();

    if (document.getElementById('lopGrid')) {
        taiDanhSachLop();
        setInterval(pollPhongThi, 12000);
    }
});

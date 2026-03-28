/**
 * Sinh viên — chi tiết lớp: tiêu đề + placeholder danh sách đề thi.
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;

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

function showToast(message) {
    const c = document.getElementById('toastContainer');
    if (!c) return;
    const el = document.createElement('div');
    el.className = 'toast toast-error';
    el.innerHTML = `<i class="fas fa-times-circle"></i> ${escHtml(message)}`;
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

async function taiChiTietLop(lopId) {
    const res = await fetch(`/api/sinh-vien/lop-phong-thi/${encodeURIComponent(lopId)}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    const json = await res.json();
    if (res.status === 401) {
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login?expired=1';
        return;
    }
    if (!json.success || !json.data) {
        showToast(json.message || 'Không tải được thông tin lớp.');
        document.getElementById('tieuDeLop').textContent = 'Lỗi';
        return;
    }
    const d = json.data;
    const ten = d.tenLop || 'Lớp';
    const chu = d.tenChuTri || '—';
    document.getElementById('tieuDeLop').textContent = ten;
    document.getElementById('breadcrumbTenLop').textContent = ten;
    document.getElementById('dongChuTri').textContent = `Chủ trì (giáo viên): ${chu}`;
    document.title = `${ten} - ThiTracNghiem`;
}

document.addEventListener('DOMContentLoaded', function () {
    const vaiTro = storage.getItem('vaiTro');
    const token = storage.getItem('token');
    const nguoiDung = storage.getItem('nguoiDung');
    const lopId = document.getElementById('lopIdHidden')?.value?.trim();

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
    if (!lopId) {
        window.location.href = '/dashboard/sinh-vien/phong-thi';
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
    taiChiTietLop(lopId);
});

/**
 * Trang placeholder sinh viên (Phòng thi, Môn học, …) — sidebar + xác thực JWT.
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
});

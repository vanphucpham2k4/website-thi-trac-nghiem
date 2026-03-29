/**
 * Lịch sử thi — danh sách bài đã nộp; xem lại / chi tiết.
 * 中文说明：调用 lich-su-thi API 并渲染表格与跳转链接。
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
const API_LS = '/api/sinh-vien/lich-su-thi';

/** Dữ liệu gốc từ API (lọc client-side theo ô tìm kiếm) */
let lichSuCached = [];

function getToken() {
    return storage.getItem('token');
}

function isTokenExpired() {
    const t = storage.getItem('tokenExpiresAt');
    return !t || Date.now() > parseInt(t, 10);
}

function escHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/** Chuỗi so khớp tìm kiếm: bỏ dấu + chữ thường */
function chuanHoaTimKiem(s) {
    return String(s || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}

/** ISO_LOCAL_DATE_TIME (YYYY-MM-DDTHH:mm:ss) → hiển thị theo locale Việt Nam */
function formatThoiGianNop(iso) {
    if (!iso || iso === '—') return '—';
    const ms = Date.parse(iso);
    if (Number.isNaN(ms)) return escHtml(iso);
    return new Date(ms).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
}

function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    if (sidebarToggle && sidebar) sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    if (sidebarClose && sidebar) sidebarClose.addEventListener('click', () => sidebar.classList.remove('active'));
}

function setupLogout() {
    const btnLogout = document.getElementById('btnLogout');
    if (!btnLogout) return;
    btnLogout.addEventListener('click', async (e) => {
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

function renderTable(rows, emptyHtml) {
    const body = document.getElementById('lichSuBody');
    if (!body) return;
    if (!rows.length) {
        const msg =
            emptyHtml ||
            '<tr><td colspan="6" style="text-align:center;padding:2rem;color:#718096;">Chưa có bài thi đã nộp.</td></tr>';
        body.innerHTML = msg;
        return;
    }
    body.innerHTML = rows
        .map((r) => {
            const pid = encodeURIComponent(r.phienThiId);
            const diem = `${escHtml(r.tongDiem != null ? r.tongDiem : '—')} / ${escHtml(r.diemToiDa != null ? r.diemToiDa : '—')}`;
            return `<tr>
                <td>${escHtml(r.tenDeThi || '—')}</td>
                <td>${escHtml(r.tenMonHoc || '—')}</td>
                <td>${formatThoiGianNop(r.thoiGianNop)}</td>
                <td>${diem}</td>
                <td>${r.soCauDung != null ? r.soCauDung : 0}/${r.tongSoCau != null ? r.tongSoCau : 0}</td>
                <td>
                    <a class="link-xem" href="/dashboard/sinh-vien/ket-qua/${pid}">Xem kết quả</a>
                    ·
                    <a class="link-xem" href="/dashboard/sinh-vien/lich-su-thi/${pid}">Chi tiết</a>
                </td>
            </tr>`;
        })
        .join('');
}

function locTheoTuKhoa(rows, qChuan) {
    if (!qChuan) return rows;
    return rows.filter(
        (r) =>
            chuanHoaTimKiem(r.tenDeThi).includes(qChuan) ||
            chuanHoaTimKiem(r.tenMonHoc).includes(qChuan)
    );
}

function apDungLocVaHienThi() {
    const input = document.getElementById('timDeThi');
    const q = chuanHoaTimKiem(input ? input.value : '');
    const filtered = locTheoTuKhoa(lichSuCached, q);
    if (!lichSuCached.length) {
        renderTable([], null);
        return;
    }
    if (!filtered.length) {
        renderTable(
            [],
            '<tr><td colspan="6" style="text-align:center;padding:2rem;color:#718096;">Không có bài thi khớp tìm kiếm.</td></tr>'
        );
        return;
    }
    renderTable(filtered, null);
}

function setupTimKiemDe() {
    const input = document.getElementById('timDeThi');
    if (!input) return;
    input.addEventListener('input', apDungLocVaHienThi);
}

async function taiDanhSach() {
    const input = document.getElementById('timDeThi');
    if (input) input.disabled = true;
    try {
        const res = await fetch(API_LS, { headers: { Authorization: `Bearer ${getToken()}` } });
        const json = await res.json();
        if (res.status === 401) {
            window.location.href = '/login?expired=1';
            return;
        }
        if (!json.success) {
            lichSuCached = [];
            renderTable([]);
            return;
        }
        lichSuCached = Array.isArray(json.data) ? json.data : [];
        apDungLocVaHienThi();
    } finally {
        if (input) input.disabled = false;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken() || storage.getItem('vaiTro') !== 'SINH_VIEN') {
        window.location.href = '/login';
        return;
    }
    if (isTokenExpired()) {
        window.location.href = '/login?expired=1';
        return;
    }
    try {
        const u = JSON.parse(storage.getItem('nguoiDung') || '{}');
        const nameEl = document.getElementById('displayName');
        if (nameEl) nameEl.textContent = u.hoTen || '—';
    } catch (e) {
        console.error(e);
    }
    setupSidebar();
    setupLogout();
    setupTimKiemDe();
    taiDanhSach();
});

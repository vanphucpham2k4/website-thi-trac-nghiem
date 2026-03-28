/**
 * Trang Quản lý sinh viên (giáo viên) — tải danh sách qua API JWT,
 * tìm kiếm cục bộ xếp theo độ khớp (gần đúng → khớp nhất).
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
const API_URL = '/api/giao-vien/sinh-vien';

let timKiemTimer = null;
/** Toàn bộ sinh viên từ API (một lần tải). */
let duLieuGanNhat = [];

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

/** Chuẩn hóa để so khớp: bỏ dấu, đ/Đ → d, chữ thường. */
function chuanHoaChuoi(s) {
    return String(s || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd')
        .replace(/Đ/g, 'd')
        .toLowerCase()
        .trim();
}

/**
 * Điểm khớp một trường với từ khóa đã chuẩn hóa (càng cao càng sát).
 */
function diemKhopTruong(giaTri, q) {
    if (!q) return 0;
    const t = chuanHoaChuoi(giaTri);
    if (!t) return 0;
    if (t === q) return 1000;
    if (t.startsWith(q)) return 880;
    if (t.includes(q)) return 720;
    const tokens = q.split(/\s+/).filter(Boolean);
    if (tokens.length > 1) {
        if (tokens.every((tok) => t.includes(tok))) {
            let pos = 0;
            let theoThuTu = true;
            for (const tok of tokens) {
                const idx = t.indexOf(tok, pos);
                if (idx === -1) {
                    theoThuTu = false;
                    break;
                }
                pos = idx + tok.length;
            }
            return theoThuTu ? 680 : 640;
        }
    }
    let ti = 0;
    let matched = 0;
    for (let i = 0; i < q.length && ti <= t.length; i++) {
        const idx = t.indexOf(q[i], ti);
        if (idx === -1) break;
        matched++;
        ti = idx + 1;
    }
    if (matched === q.length) return 520;
    if (matched > 0) return 200 + Math.round(300 * (matched / q.length));
    return 0;
}

/** Điểm tối đa trên các cột tìm được. */
function diemDong(row, q) {
    if (!q) return 0;
    return Math.max(
        diemKhopTruong(row.maNguoiDung, q),
        diemKhopTruong(row.hoTen, q),
        diemKhopTruong(row.email, q),
        diemKhopTruong(row.soDienThoai, q)
    );
}

/** Lọc + sắp: khớp nhất trước, cùng điểm thì sort tên. */
function locVaSapXepTheoDoKhop(tatCa, rawKeyword) {
    const qRaw = String(rawKeyword || '').trim();
    const q = chuanHoaChuoi(qRaw);
    if (!q) {
        return [...tatCa].sort((a, b) =>
            String(a.hoTen || '').localeCompare(String(b.hoTen || ''), 'vi', { sensitivity: 'base' })
        );
    }
    return tatCa
        .map((row) => ({ row, score: diemDong(row, q) }))
        .filter((x) => x.score > 0)
        .sort((a, b) => {
            if (b.score !== a.score) return b.score - a.score;
            return String(a.row.hoTen || '').localeCompare(String(b.row.hoTen || ''), 'vi', { sensitivity: 'base' });
        })
        .map((x) => x.row);
}

function apDungLoc() {
    const kw = document.getElementById('inputTimKiem')?.value ?? '';
    const list = locVaSapXepTheoDoKhop(duLieuGanNhat, kw);
    const dangLoc = String(kw).trim() !== '';
    renderBang(list, { tongGoc: duLieuGanNhat.length, dangLoc });
}

function renderBang(list, meta = {}) {
    const body = document.getElementById('bangSinhVienBody');
    const label = document.getElementById('labelTongSo');
    const tongGoc = typeof meta.tongGoc === 'number' ? meta.tongGoc : list.length;
    const dangLoc = !!meta.dangLoc;

    if (!body) return;

    if (label) {
        if (dangLoc) {
            label.innerHTML = `Hiển thị <strong>${list.length}</strong> / <strong>${tongGoc}</strong> sinh viên <span style="font-weight:400;color:#a0aec0;">(ưu tiên khớp nhất)</span>`;
        } else if (tongGoc > 0) {
            label.innerHTML = `Tổng: <strong>${tongGoc}</strong> sinh viên`;
        } else {
            label.innerHTML = '<strong>0</strong> sinh viên';
        }
    }

    if (!list.length) {
        const msg =
            tongGoc === 0
                ? 'Chưa có sinh viên trong hệ thống.'
                : dangLoc
                  ? 'Không có sinh viên nào khớp tìm kiếm.'
                  : 'Không có dữ liệu.';
        body.innerHTML = `
            <tr><td colspan="6" class="empty-state">
                <i class="fas fa-user-slash"></i><br>
                ${escHtml(msg)}
            </td></tr>`;
        return;
    }
    body.innerHTML = list
        .map((row, i) => {
            const ma = row.maNguoiDung || '—';
            const ten = row.hoTen || '—';
            const mail = row.email || '—';
            const sdt = row.soDienThoai || '—';
            const luot = typeof row.soLuotThiVoiGiaoVien === 'number' ? row.soLuotThiVoiGiaoVien : 0;
            return `<tr>
                <td>${i + 1}</td>
                <td>${escHtml(ma)}</td>
                <td>${escHtml(ten)}</td>
                <td>${escHtml(mail)}</td>
                <td>${escHtml(sdt)}</td>
                <td style="text-align:center;font-weight:600;color:#4a5568;">${luot}</td>
            </tr>`;
        })
        .join('');
}

async function taiDanhSachDayDu() {
    const token = getToken();
    const res = await fetch(API_URL, {
        headers: { Authorization: `Bearer ${token}` }
    });
    const json = await res.json();
    if (!json.success) {
        showToast(json.message || 'Không tải được danh sách.', 'error');
        duLieuGanNhat = [];
        renderBang([], { tongGoc: 0, dangLoc: false });
        return;
    }
    duLieuGanNhat = json.data || [];
    apDungLoc();
}

document.addEventListener('DOMContentLoaded', () => {
    const vaiTro = storage.getItem('vaiTro');
    const token = getToken();
    if (!token || vaiTro !== 'GIAO_VIEN' || isTokenExpired()) {
        window.location.href = '/login';
        return;
    }

    setupSidebar();
    setupLogout();
    hienThiTen();

    taiDanhSachDayDu();

    document.getElementById('inputTimKiem')?.addEventListener('input', () => {
        clearTimeout(timKiemTimer);
        timKiemTimer = setTimeout(() => apDungLoc(), 120);
    });
});

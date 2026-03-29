/**
 * Sinh viên — chi tiết lớp: đề đã xuất bản, Tham gia làm bài, polling + nhấp nháy khi có đề mới.
 * 中文说明：班级详情页加载试卷列表、轮询指纹变化并触发动画、开始考试跳转。
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
const API_LOP = '/api/sinh-vien/lop-phong-thi';

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

function khoaLopChiTiet() {
    return document.getElementById('lopIdHidden')?.value?.trim() || '';
}

function vanTayDeThi(arr) {
    return (arr || [])
        .map((d) => `${d.deThiId || ''}|${d.thoiGianXuatBan || ''}`)
        .sort()
        .join(';');
}

function layFpLuutruChiTiet(lopId) {
    try {
        return localStorage.getItem(`sv_chitiet_de_fp_${lopId}`) || null;
    } catch {
        return null;
    }
}

function luuFpChiTiet(lopId, fp) {
    try {
        localStorage.setItem(`sv_chitiet_de_fp_${lopId}`, fp);
    } catch (e) {
        console.warn(e);
    }
}

let daKhoiTaoFpChiTiet = false;

function chopNhayTheNoiDung() {
    const card = document.getElementById('examListCard');
    if (!card) return;
    card.classList.remove('exam-list-flash');
    void card.offsetWidth;
    card.classList.add('exam-list-flash');
    setTimeout(() => card.classList.remove('exam-list-flash'), 1400);
}

async function taiChiTietLop(lopId) {
    const res = await fetch(`${API_LOP}/${encodeURIComponent(lopId)}`, {
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

function hienThiDanhSachDeThi(list) {
    const empty = document.getElementById('khoiDeThiTrong');
    const wrap = document.getElementById('khoiDanhSachDeThi');
    if (!empty || !wrap) return;
    if (!list.length) {
        empty.style.display = '';
        empty.querySelector('p').textContent = 'Hiện tại chưa có đề thi nào dành cho bạn';
        wrap.style.display = 'none';
        wrap.innerHTML = '';
        return;
    }
    empty.style.display = 'none';
    wrap.style.display = 'block';
    const lopId = khoaLopChiTiet();
    wrap.innerHTML = list
        .map((d) => {
            const tid = escHtml(d.deThiId);
            const ten = escHtml(d.tenDeThi || 'Đề thi');
            const mon = escHtml(d.tenMonHoc || '');
            const phut = d.thoiGianPhut != null ? d.thoiGianPhut : '—';
            const soCau = d.soCauHoi != null ? d.soCauHoi : 0;
            const dangLam = d.coPhienDangLam && d.phienThiIdDangLam;
            const label = dangLam ? 'Tiếp tục' : 'Tham gia';
            return `<div class="de-thi-row" data-de-thi-id="${tid}">
                <div>
                    <h4>${ten}</h4>
                    <p class="meta">${mon ? mon + ' · ' : ''}${phut} phút · ${soCau} câu hỏi</p>
                </div>
                <button type="button" class="btn-tham-gia" data-de-thi-id="${tid}" data-co-phien="${dangLam ? '1' : '0'}" data-phien-id="${escHtml(d.phienThiIdDangLam || '')}">
                    <i class="fas fa-play-circle"></i> ${label}
                </button>
            </div>`;
        })
        .join('');

    wrap.querySelectorAll('.btn-tham-gia').forEach((btn) => {
        btn.addEventListener('click', async () => {
            const deThiId = btn.getAttribute('data-de-thi-id');
            const coPhien = btn.getAttribute('data-co-phien') === '1';
            const phienCu = btn.getAttribute('data-phien-id');
            if (coPhien && phienCu) {
                window.location.href = `/dashboard/sinh-vien/lam-bai/${encodeURIComponent(phienCu)}`;
                return;
            }
            await batDauHoacLoi(deThiId, lopId, btn);
        });
    });
}

async function batDauHoacLoi(deThiId, lopId, btn) {
    btn.disabled = true;
    try {
        const res = await fetch(`/api/sinh-vien/thi/lop/${encodeURIComponent(lopId)}/de-thi/${encodeURIComponent(deThiId)}/bat-dau`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${getToken()}`, 'Content-Type': 'application/json' }
        });
        const json = await res.json();
        if (res.status === 401) {
            window.location.href = '/login?expired=1';
            return;
        }
        if (!json.success || !json.data?.phienThiId) {
            showToast(json.message || 'Không bắt đầu được bài thi.');
            btn.disabled = false;
            return;
        }
        window.location.href = `/dashboard/sinh-vien/lam-bai/${encodeURIComponent(json.data.phienThiId)}`;
    } catch (e) {
        console.error(e);
        showToast('Lỗi kết nối.');
        btn.disabled = false;
    }
}

async function taiVaSoSanhDeThi(lopId) {
    const res = await fetch(`${API_LOP}/${encodeURIComponent(lopId)}/de-thi`, {
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
    if (!json.success) {
        showToast(json.message || 'Không tải danh sách đề thi.');
        return;
    }
    const list = json.data || [];
    const fpMoi = vanTayDeThi(list);
    const fpCu = layFpLuutruChiTiet(lopId);
    if (daKhoiTaoFpChiTiet && fpCu != null && fpCu !== fpMoi) {
        chopNhayTheNoiDung();
    }
    luuFpChiTiet(lopId, fpMoi);
    daKhoiTaoFpChiTiet = true;
    hienThiDanhSachDeThi(list);
}

document.addEventListener('DOMContentLoaded', function () {
    const vaiTro = storage.getItem('vaiTro');
    const token = storage.getItem('token');
    const nguoiDung = storage.getItem('nguoiDung');
    const lopId = khoaLopChiTiet();

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
    taiVaSoSanhDeThi(lopId);
    setInterval(() => taiVaSoSanhDeThi(lopId), 12000);
});

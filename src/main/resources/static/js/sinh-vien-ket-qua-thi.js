/**
 * Trang kết quả sau khi nộp bài: điểm, lưới đúng/sai, liên kết chi tiết.
 * Chế độ ẩn danh: chỉ dùng lamBaiToken, không vào dashboard.
 */
const thiAnDanh = document.body?.dataset?.thiAnDanh === 'true';
const storage = thiAnDanh ? null : (localStorage.getItem('token') ? localStorage : sessionStorage);
const phienThiId = document.getElementById('phienThiIdHidden')?.value?.trim();

function getToken() {
    if (thiAnDanh) return sessionStorage.getItem('lamBaiToken');
    return storage.getItem('token');
}

function escHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
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

function duongDanChiTiet() {
    if (thiAnDanh) return `/thi-mo/chi-tiet/${encodeURIComponent(phienThiId)}`;
    return `/dashboard/sinh-vien/lich-su-thi/${encodeURIComponent(phienThiId)}`;
}

function render(kq) {
    const root = document.getElementById('kqRoot');
    if (!root) return;
    const meta = [kq.ngayThi, kq.maHocPhan, kq.tenGiaoVien].filter(Boolean).join(' · ');
    const phutGioiHan = kq.thoiGianGioiHanPhut != null ? kq.thoiGianGioiHanPhut : 0;
    const baseCt = duongDanChiTiet();
    const grid = (kq.trangThaiCacCau || [])
        .map((o) => {
            const cls = o.dung ? 'dung' : 'sai';
            const href = `${baseCt}#cau-${o.stt}`;
            return `<a class="kq-o ${cls}" href="${href}" title="Câu ${o.stt}">${o.stt}</a>`;
        })
        .join('');

    root.innerHTML = `
        <h2 class="kq-title">${escHtml(kq.tenDeThi || 'Bài thi')}</h2>
        <p class="kq-meta">${escHtml(meta)}</p>
        <div class="kq-cards">
            <div class="kq-card">
                <div class="lab">Điểm số</div>
                <div class="val">${escHtml(kq.diemDat != null ? kq.diemDat : '—')} / ${escHtml(kq.diemToiDa != null ? kq.diemToiDa : '—')}</div>
            </div>
            <div class="kq-card">
                <div class="lab">Thời gian làm bài</div>
                <div class="val">${escHtml(kq.thoiGianLamBai || '—')}</div>
                <div class="sub">Giới hạn làm bài: ${phutGioiHan} phút</div>
            </div>
            <div class="kq-card">
                <div class="lab">Số câu đúng</div>
                <div class="val">${kq.soCauDung != null ? kq.soCauDung : 0} / ${kq.tongSoCau != null ? kq.tongSoCau : 0}</div>
            </div>
        </div>
        <div class="kq-actions">
            <button type="button" class="btn-thoat" id="btnThoatVeTrangChu"><i class="fas fa-home"></i> Thoát</button>
            <a class="link-chi-tiet" href="${baseCt}">Xem lại chi tiết từng câu →</a>
        </div>
        <div class="kq-grid-wrap">
            <h3>Bản đồ câu hỏi</h3>
            <div class="kq-grid">${grid}</div>
            <div class="kq-legend">
                <span style="color:#38a169">●</span> Câu trả lời đúng &nbsp;
                <span style="color:#e53e3e">●</span> Câu trả lời sai
            </div>
        </div>
        <p style="font-size:0.8rem;color:#a0aec0;">© 2026 Hệ thống quản lý thi trực tuyến - Hutech</p>
    `;

    document.getElementById('btnThoatVeTrangChu')?.addEventListener('click', () => {
        if (thiAnDanh) {
            sessionStorage.removeItem('lamBaiToken');
            sessionStorage.removeItem('lamBaiTokenExpiresAt');
            sessionStorage.removeItem('lamBaiHoTen');
            window.location.href = '/';
        } else {
            window.location.href = '/dashboard/sinh-vien';
        }
    });
}

async function taiKetQua() {
    if (!phienThiId) {
        window.location.href = thiAnDanh ? '/' : '/dashboard/sinh-vien';
        return;
    }
    const url = thiAnDanh
        ? `/api/thi-an-danh/phien/${encodeURIComponent(phienThiId)}/ket-qua`
        : `/api/sinh-vien/thi/phien/${encodeURIComponent(phienThiId)}/ket-qua`;
    const res = await fetch(url, {
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    const json = await res.json();
    if (res.status === 401) {
        if (thiAnDanh) {
            const ma = sessionStorage.getItem('thiMoMaTruyCap');
            window.location.href = ma ? '/thi-mo/' + encodeURIComponent(ma) : '/';
        } else {
            window.location.href = '/login?expired=1';
        }
        return;
    }
    const root = document.getElementById('kqRoot');
    if (!json.success || !json.data) {
        if (root) root.innerHTML = `<p style="color:#e53e3e;">${escHtml(json.message || 'Không tải được kết quả.')}</p>`;
        return;
    }
    render(json.data);
}

document.addEventListener('DOMContentLoaded', () => {
    if (thiAnDanh) {
        if (!getToken()) {
            const ma = sessionStorage.getItem('thiMoMaTruyCap');
            window.location.href = ma ? '/thi-mo/' + encodeURIComponent(ma) : '/';
            return;
        }
        taiKetQua();
        return;
    }
    if (!getToken() || storage.getItem('vaiTro') !== 'SINH_VIEN') {
        window.location.href = '/login';
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
    taiKetQua();
});

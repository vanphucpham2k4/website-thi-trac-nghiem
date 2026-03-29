const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
const phienThiId = document.getElementById('phienThiIdHidden')?.value?.trim();

function escHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function nhanDangLuaChon(ch, a, b, c, d) {
    const map = { A: a, B: b, C: c, D: d };
    return map[ch] != null ? String(map[ch]) : ch || '—';
}

function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    if (sidebarToggle && sidebar) sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    if (sidebarClose && sidebar) sidebarClose.addEventListener('click', () => sidebar.classList.remove('active'));
}

function setupLogout() {
    const btn = document.getElementById('btnLogout');
    if (!btn) return;
    btn.addEventListener('click', async (e) => {
        e.preventDefault();
        try {
            await fetch('/api/logout', { method: 'POST' });
        } catch (_) {
        }
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login';
    });
}

function renderChiTietDaNop(d) {
    const root = document.getElementById('chiTietRoot');
    if (!root) return;
    const meta = [d.thoiGianNop, d.tenMonHoc, `Điểm: ${d.tongDiem || '—'}/${d.diemToiDa || '—'}`].filter(Boolean).join(' · ');
    const blocks = (d.cacCau || [])
        .map((c) => {
            const cls = c.dung ? 'dung' : 'sai';
            const loai = c.loaiCauHoi || 'TRAC_NGHIEM';
            let chonTxt = c.daChon || '—';
            let dungTxt = c.dapAnDung || '—';
            if (loai !== 'DUNG_SAI') {
                chonTxt = nhanDangLuaChon(c.daChon, c.luaChonA, c.luaChonB, c.luaChonC, c.luaChonD);
                dungTxt = nhanDangLuaChon(c.dapAnDung, c.luaChonA, c.luaChonB, c.luaChonC, c.luaChonD);
            } else {
                if (c.daChon === 'DUNG') chonTxt = 'Đúng';
                if (c.daChon === 'SAI') chonTxt = 'Sai';
                if (c.dapAnDung === 'DUNG') dungTxt = 'Đúng';
                if (c.dapAnDung === 'SAI') dungTxt = 'Sai';
            }
            return `<div class="cau-block ${cls}" id="cau-${c.stt}">
                <div class="stt">Câu ${c.stt} — <span class="${c.dung ? 'badge-ok' : 'badge-bad'}">${c.dung ? 'Đúng' : 'Sai'}</span></div>
                <div class="noi-dung">${escHtml(c.noiDung || '')}</div>
                <div class="dap-line"><strong>Thí sinh chọn:</strong> ${escHtml(chonTxt)}</div>
                <div class="dap-line"><strong>Đáp án đúng:</strong> ${escHtml(dungTxt)}</div>
            </div>`;
        })
        .join('');

    root.innerHTML = `
        <div class="ct-head">
            <h2 class="ct-title">${escHtml(d.tenDeThi || 'Bài thi')}</h2>
            <p class="ct-meta">${escHtml(meta)}</p>
            <p class="ct-meta" style="margin-top:8px;color:#dd6b20;"><i class="fas fa-info-circle"></i> Đã nộp bài — xem chấm tự động</p>
        </div>
        ${blocks}
    `;
}

function renderDangLam(bai, hoTenSv, maSv) {
    const root = document.getElementById('chiTietRoot');
    if (!root) return;
    const list = [...(bai.cauHoi || [])].sort((a, b) => (a.thuTu || 0) - (b.thuTu || 0));
    const sub = [hoTenSv, maSv ? `Mã: ${maSv}` : '', bai.thoiGianHetHan ? `Hết hạn: ${bai.thoiGianHetHan}` : '']
        .filter(Boolean)
        .join(' · ');
    const blocks = list
        .map((c, idx) => {
            const stt = c.thuTu || idx + 1;
            const loai = c.loaiCauHoi || 'TRAC_NGHIEM';
            let chonTxt = 'Chưa chọn';
            if (c.daChon != null && String(c.daChon).trim() !== '') {
                if (loai === 'DUNG_SAI') {
                    chonTxt = c.daChon === 'DUNG' ? 'Đúng' : c.daChon === 'SAI' ? 'Sai' : String(c.daChon);
                } else {
                    chonTxt = nhanDangLuaChon(c.daChon, c.luaChonA, c.luaChonB, c.luaChonC, c.luaChonD);
                }
            }
            return `<div class="cau-block dang-lam">
                <div class="stt">Câu ${stt}</div>
                <div class="noi-dung">${escHtml(c.noiDung || '')}</div>
                <div class="dap-line"><strong>Đáp án đang chọn:</strong> ${escHtml(chonTxt)}</div>
            </div>`;
        })
        .join('');

    root.innerHTML = `
        <div class="ct-head">
            <h2 class="ct-title">${escHtml(bai.tenDeThi || 'Đề thi')}</h2>
            <p class="ct-meta">${escHtml(sub)}</p>
            <p class="ct-meta" style="margin-top:8px;color:#3182ce;"><i class="fas fa-pen"></i> Sinh viên đang làm / chưa nộp — chỉ xem tiến độ (không hiện đáp án đúng).</p>
        </div>
        ${blocks}
    `;
}

async function taiDuLieu() {
    const root = document.getElementById('chiTietRoot');
    if (!phienThiId) {
        if (root) root.innerHTML = '<p style="color:#e53e3e;">Thiếu mã phiên thi.</p>';
        return;
    }
    const token = storage.getItem('token');
    const res = await fetch(`/api/giao-vien/theo-doi-thi/phien/${encodeURIComponent(phienThiId)}/xem`, {
        headers: { Authorization: `Bearer ${token}` }
    });
    const json = await res.json();
    if (res.status === 401) {
        window.location.href = '/login?expired=1';
        return;
    }
    if (!json.success || !json.data) {
        if (root) root.innerHTML = `<p style="color:#e53e3e;">${escHtml(json.message || 'Không tải được dữ liệu.')}</p>`;
        return;
    }
    const d = json.data;
    if (d.daNop && d.chiTietDaNop) {
        renderChiTietDaNop(d.chiTietDaNop);
    } else if (d.noiDungDangLam) {
        renderDangLam(d.noiDungDangLam, d.hoTenSinhVien, d.maNguoiDungSinhVien);
    } else if (root) {
        root.innerHTML = '<p style="color:#e53e3e;">Không có nội dung hiển thị.</p>';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const token = storage.getItem('token');
    if (!token || storage.getItem('vaiTro') !== 'GIAO_VIEN') {
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
    taiDuLieu();
});

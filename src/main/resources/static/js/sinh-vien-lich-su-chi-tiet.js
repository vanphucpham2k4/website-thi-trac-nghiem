/**
 * Chi tiết xem lại bài đã nộp: từng câu, đúng/sai, đáp án chọn vs đáp án đúng.
 * Ẩn danh: API /api/thi-an-danh/phien/.../chi-tiet
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

function render(d) {
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
                <div class="dap-line"><strong>Bạn chọn:</strong> ${escHtml(chonTxt)}</div>
                <div class="dap-line"><strong>Đáp án đúng:</strong> ${escHtml(dungTxt)}</div>
            </div>`;
        })
        .join('');

    root.innerHTML = `
        <div class="ct-head">
            <h2 class="ct-title">${escHtml(d.tenDeThi || 'Bài thi')}</h2>
            <p class="ct-meta">${escHtml(meta)}</p>
        </div>
        ${blocks}
    `;

    const hash = window.location.hash.replace(/^#/, '');
    if (hash) {
        requestAnimationFrame(() => {
            document.getElementById(hash)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }
}

async function taiChiTiet() {
    if (!phienThiId) {
        window.location.href = thiAnDanh ? '/' : '/dashboard/sinh-vien/lich-su-thi';
        return;
    }
    const url = thiAnDanh
        ? `/api/thi-an-danh/phien/${encodeURIComponent(phienThiId)}/chi-tiet`
        : `/api/sinh-vien/lich-su-thi/${encodeURIComponent(phienThiId)}/chi-tiet`;
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
    const root = document.getElementById('chiTietRoot');
    if (!json.success || !json.data) {
        if (root) root.innerHTML = `<p style="color:#e53e3e;">${escHtml(json.message || 'Không tải được chi tiết.')}</p>`;
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
        taiChiTiet();
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
    taiChiTiet();
});

/**
 * Trang làm bài thi (sinh viên): đếm ngược, lưới câu, lưu có debounce + lưu tay, nộp bài.
 * Chế độ ẩn danh (/thi-mo/lam-bai): JWT riêng trong sessionStorage (lamBaiToken).
 */
const thiAnDanh = document.body?.dataset?.thiAnDanh === 'true';
const storage = thiAnDanh ? null : (localStorage.getItem('token') ? localStorage : sessionStorage);

const phienThiId = document.getElementById('phienThiIdHidden')?.value?.trim();
const API_BASE = thiAnDanh ? '/api/thi-an-danh/phien' : '/api/sinh-vien/thi/phien';
const API_PHIEN = (id) => `${API_BASE}/${encodeURIComponent(id)}`;

let baiThi = null;
/** @type {Record<string, string>} */
let traLoi = {};
/** @type {Set<string>} */
let danhDauXemLai = new Set();
let chiSoCau = 0;
let luuTimer = null;
let chuaLuu = false;
let dongHoTimer = null;
let hetHan = false;

function getToken() {
    if (thiAnDanh) return sessionStorage.getItem('lamBaiToken');
    return storage.getItem('token');
}

function isTokenExpired() {
    if (thiAnDanh) {
        const t = sessionStorage.getItem('lamBaiTokenExpiresAt');
        return !t || Date.now() > parseInt(t, 10);
    }
    const t = storage.getItem('tokenExpiresAt');
    return !t || Date.now() > parseInt(t, 10);
}

function redirectVeThiMoNeuCoMa() {
    const ma = sessionStorage.getItem('thiMoMaTruyCap');
    if (ma) window.location.href = '/thi-mo/' + encodeURIComponent(ma);
    else window.location.href = '/';
}

function escHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function keyXemLai() {
    return `sv_xem_lai_${phienThiId}`;
}

function docXemLai() {
    try {
        const raw = localStorage.getItem(keyXemLai());
        if (!raw) return new Set();
        const arr = JSON.parse(raw);
        return new Set(Array.isArray(arr) ? arr : []);
    } catch {
        return new Set();
    }
}

function luuXemLai() {
    try {
        localStorage.setItem(keyXemLai(), JSON.stringify([...danhDauXemLai]));
    } catch (e) {
        console.warn(e);
    }
}

function cauDaSapXep() {
    if (!baiThi?.cauHoi) return [];
    return [...baiThi.cauHoi].sort((a, b) => (a.thuTu || 0) - (b.thuTu || 0));
}

function demDaTraLoi() {
    const list = cauDaSapXep();
    return list.filter((c) => traLoi[c.id] && String(traLoi[c.id]).trim() !== '').length;
}

function parseHetHanMs() {
    const s = baiThi?.thoiGianHetHan;
    if (!s) return null;
    const d = Date.parse(s.length === 19 ? s.replace(' ', 'T') : s);
    return Number.isNaN(d) ? null : d;
}

function capNhatDongHo() {
    const el = document.getElementById('dongHoConLai');
    if (!el) return;
    const end = parseHetHanMs();
    if (end == null) {
        el.textContent = '--:--';
        return;
    }
    let sec = Math.floor((end - Date.now()) / 1000);
    if (sec <= 0) {
        sec = 0;
        hetHan = true;
        el.classList.add('het-gio');
        el.textContent = '00:00';
        return;
    }
    el.classList.remove('het-gio');
    if (sec <= 300) el.classList.add('canh-bao');
    else el.classList.remove('canh-bao');
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    el.textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function veLuoiCau() {
    const wrap = document.getElementById('luoiCau');
    const tienDo = document.getElementById('tienDoText');
    if (!wrap || !tienDo) return;
    const list = cauDaSapXep();
    const tong = list.length;
    const da = demDaTraLoi();
    tienDo.textContent = `${da}/${tong} câu`;
    wrap.innerHTML = list
        .map((c, idx) => {
            const co = traLoi[c.id] && String(traLoi[c.id]).trim() !== '';
            const xemLai = danhDauXemLai.has(c.id);
            let cls = 'o-cau';
            if (idx === chiSoCau) cls += ' dang-chon';
            else if (co) cls += ' da-tra-loi';
            if (xemLai) cls += ' xem-lai';
            return `<button type="button" class="${cls}" data-idx="${idx}">${String(c.thuTu || idx + 1).padStart(2, '0')}</button>`;
        })
        .join('');
    wrap.querySelectorAll('.o-cau').forEach((btn) => {
        btn.addEventListener('click', () => {
            const i = parseInt(btn.getAttribute('data-idx'), 10);
            if (!Number.isNaN(i)) chonChiSo(i);
        });
    });
}

function chonChiSo(i) {
    const list = cauDaSapXep();
    if (i < 0 || i >= list.length) return;
    chiSoCau = i;
    veLuoiCau();
    veCauHienTai();
}

function veCauHienTai() {
    const khoi = document.getElementById('khoiNoiDungCau');
    if (!khoi || !baiThi) return;
    const list = cauDaSapXep();
    const c = list[chiSoCau];
    if (!c) {
        khoi.innerHTML = '<p>Không có câu hỏi.</p>';
        return;
    }
    const daChon = traLoi[c.id] || '';
    const loai = c.loaiCauHoi || 'TRAC_NGHIEM';
    const dangXemLai = danhDauXemLai.has(c.id);

    let phanLuaChon = '';
    if (loai === 'DUNG_SAI') {
        const opts = [
            { v: 'DUNG', t: 'Đúng' },
            { v: 'SAI', t: 'Sai' }
        ];
        phanLuaChon = opts
            .map(
                (o) => `
            <label class="lua-chon-item ${daChon === o.v ? 'chon' : ''}">
                <input type="radio" name="tl" value="${o.v}" ${daChon === o.v ? 'checked' : ''}>
                ${escHtml(o.t)}
            </label>`
            )
            .join('');
    } else {
        const labels = [
            ['A', c.luaChonA],
            ['B', c.luaChonB],
            ['C', c.luaChonC],
            ['D', c.luaChonD]
        ];
        phanLuaChon = labels
            .filter((x) => x[1] != null && String(x[1]).trim() !== '')
            .map(
                ([ch, text]) => `
            <label class="lua-chon-item ${daChon === ch ? 'chon' : ''}">
                <input type="radio" name="tl" value="${ch}" ${daChon === ch ? 'checked' : ''}>
                <strong>${ch}.</strong> ${escHtml(text)}
            </label>`
            )
            .join('');
    }

    khoi.innerHTML = `
        <div class="cau-head">
            <span class="stt">Câu hỏi ${c.thuTu || chiSoCau + 1}</span>
            <button type="button" class="btn-danh-dau ${dangXemLai ? 'active' : ''}" id="btnDanhDau"><i class="fas fa-flag"></i> Đánh dấu xem lại</button>
        </div>
        <div class="noi-dung-cau">${escHtml(c.noiDung || '')}</div>
        <div id="hopLuaChon">${phanLuaChon}</div>
        <div class="dieu-huong-cau">
            ${chiSoCau > 0 ? `<a href="#" class="link-cau" id="lnkCauTruoc">← Câu trước</a>` : '<span></span>'}
            <div style="display:flex;gap:10px;align-items:center;">
                <button type="button" class="btn-bo-chon" id="btnBoChon">Bỏ chọn</button>
                ${
                    chiSoCau < list.length - 1
                        ? '<button type="button" class="btn-cau-sau" id="lnkCauSau">Câu sau →</button>'
                        : '<span style="color:#718096;font-size:0.85rem;">Câu cuối</span>'
                }
            </div>
        </div>
    `;

    const hop = document.getElementById('hopLuaChon');
    if (hop) {
        hop.querySelectorAll('input[name="tl"]').forEach((inp) => {
            inp.addEventListener('change', () => {
                traLoi[c.id] = inp.value;
                chuaLuu = true;
                veLuoiCau();
                luuCoDebounce();
            });
        });
    }
    document.getElementById('btnDanhDau')?.addEventListener('click', () => {
        if (danhDauXemLai.has(c.id)) danhDauXemLai.delete(c.id);
        else danhDauXemLai.add(c.id);
        luuXemLai();
        veLuoiCau();
        veCauHienTai();
    });
    document.getElementById('btnBoChon')?.addEventListener('click', () => {
        delete traLoi[c.id];
        chuaLuu = true;
        veLuoiCau();
        veCauHienTai();
        luuCoDebounce();
    });
    document.getElementById('lnkCauTruoc')?.addEventListener('click', (e) => {
        e.preventDefault();
        chonChiSo(chiSoCau - 1);
    });
    document.getElementById('lnkCauSau')?.addEventListener('click', () => chonChiSo(chiSoCau + 1));
}

function taoBodyLuu() {
    const o = {};
    for (const [k, v] of Object.entries(traLoi)) {
        if (v != null && String(v).trim() !== '') o[k] = v;
    }
    return { traLoi: o };
}

async function luuLenMayChu(force) {
    const status = document.getElementById('trangThaiLuu');
    if (!phienThiId) return false;
    try {
        const res = await fetch(`${API_PHIEN(phienThiId)}/luu-tra-loi`, {
            method: 'PUT',
            headers: { Authorization: `Bearer ${getToken()}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(taoBodyLuu())
        });
        const json = await res.json();
        if (res.status === 401) {
            if (thiAnDanh) redirectVeThiMoNeuCoMa();
            else window.location.href = '/login?expired=1';
            return false;
        }
        if (!json.success) {
            if (status) {
                status.textContent = json.message || 'Lưu thất bại';
                status.className = 'trang-thai-luu err';
            }
            return false;
        }
        chuaLuu = false;
        if (status) {
            const t = new Date();
            status.textContent = `Đã lưu ${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}:${String(t.getSeconds()).padStart(2, '0')}`;
            status.className = 'trang-thai-luu ok';
        }
        return true;
    } catch (e) {
        console.error(e);
        if (status) {
            status.textContent = 'Lỗi mạng khi lưu';
            status.className = 'trang-thai-luu err';
        }
        return false;
    }
}

function luuCoDebounce() {
    if (luuTimer) clearTimeout(luuTimer);
    luuTimer = setTimeout(() => luuLenMayChu(false), 750);
}

function moModalNop() {
    document.getElementById('modalNopBai')?.classList.add('active');
}

function dongModalNop() {
    document.getElementById('modalNopBai')?.classList.remove('active');
}

async function xacNhanNopBai() {
    await luuLenMayChu(true);
    try {
        const res = await fetch(`${API_PHIEN(phienThiId)}/nop-bai`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${getToken()}`, 'Content-Type': 'application/json' }
        });
        const json = await res.json();
        if (res.status === 401) {
            if (thiAnDanh) redirectVeThiMoNeuCoMa();
            else window.location.href = '/login?expired=1';
            return;
        }
        if (!json.success) {
            alert(json.message || 'Không nộp được bài.');
            return;
        }
        window.location.href = thiAnDanh
            ? `/thi-mo/ket-qua/${encodeURIComponent(phienThiId)}`
            : `/dashboard/sinh-vien/ket-qua/${encodeURIComponent(phienThiId)}`;
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối khi nộp bài.');
    }
}

async function taiNoiDung() {
    if (!phienThiId) {
        if (thiAnDanh) redirectVeThiMoNeuCoMa();
        else window.location.href = '/dashboard/sinh-vien/phong-thi';
        return;
    }
    if (isTokenExpired()) {
        if (thiAnDanh) redirectVeThiMoNeuCoMa();
        else window.location.href = '/login?expired=1';
        return;
    }
    const res = await fetch(`${API_PHIEN(phienThiId)}/noi-dung`, {
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    const json = await res.json();
    if (res.status === 401) {
        if (thiAnDanh) redirectVeThiMoNeuCoMa();
        else window.location.href = '/login?expired=1';
        return;
    }
    if (!json.success) {
        document.getElementById('khoiNoiDungCau').innerHTML = `<p style="color:#e53e3e;">${escHtml(json.message || 'Không tải được đề.')}</p>`;
        return;
    }
    baiThi = json.data;
    danhDauXemLai = docXemLai();
    traLoi = {};
    for (const c of baiThi.cauHoi || []) {
        if (c.daChon) traLoi[c.id] = c.daChon;
    }
    const tenMon = baiThi.tenMonHoc || '';
    const ma = baiThi.maHocPhan || '';
    document.getElementById('headerTenDe').textContent =
        `${baiThi.tenDeThi || 'Đề thi'}${ma ? ` (${ma})` : ''}${tenMon ? ` — ${tenMon}` : ''}`;

    capNhatDongHo();
    if (dongHoTimer) clearInterval(dongHoTimer);
    dongHoTimer = setInterval(capNhatDongHo, 1000);

    veLuoiCau();
    chonChiSo(0);
}

document.addEventListener('DOMContentLoaded', () => {
    if (thiAnDanh) {
        if (!getToken() || isTokenExpired()) {
            redirectVeThiMoNeuCoMa();
            return;
        }
        const ten = sessionStorage.getItem('lamBaiHoTen') || 'Ẩn danh';
        document.getElementById('headerHoTen').textContent = ten;
        document.getElementById('headerMssv').textContent = 'Thi qua link công khai';
    } else {
        const vaiTro = storage.getItem('vaiTro');
        const token = storage.getItem('token');
        if (!token || vaiTro !== 'SINH_VIEN') {
            window.location.href = '/login';
            return;
        }
        try {
            const u = JSON.parse(storage.getItem('nguoiDung') || '{}');
            document.getElementById('headerHoTen').textContent = u.hoTen || `${u.ho || ''} ${u.ten || ''}`.trim() || '—';
            document.getElementById('headerMssv').textContent = u.maNguoiDung ? `Mã SV: ${u.maNguoiDung}` : u.id ? `ID: ${u.id.slice(0, 8)}…` : '—';
        } catch (e) {
            console.error(e);
        }
    }

    document.getElementById('btnMoModalNop')?.addEventListener('click', () => {
        if (hetHan) {
            alert('Đã hết thời gian làm bài. Vui lòng nộp bài nếu hệ thống cho phép.');
        }
        moModalNop();
    });
    document.getElementById('btnHuyNop')?.addEventListener('click', dongModalNop);
    document.getElementById('btnXacNhanNop')?.addEventListener('click', xacNhanNopBai);
    document.getElementById('btnLuuNgay')?.addEventListener('click', () => luuLenMayChu(true));

    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden' && chuaLuu) luuLenMayChu(false);
    });
    window.addEventListener('beforeunload', (e) => {
        if (chuaLuu) {
            e.preventDefault();
            e.returnValue = '';
        }
    });

    taiNoiDung();
});

/**
 * Trang chỉnh sửa câu hỏi — văn bản thô + xem trước (đồng bộ quy tắc với DeThiCauHoiVanBanCodec).
 */
const API_BASE = '/api/giao-vien/de-thi';
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;

const DONG_SO_CAU = /^\d+\.\s*(.*)$/;
const DONG_LUA_CHON = /^(\*)?([A-Da-d])\.\s*(.*)$/;

let deThiId = '';
let lastSavedVanBan = '';
/** Số câu cố định trong đề (từ server), dùng chia thang điểm. */
let soCauTrongDe = 0;
/** Thang điểm đã lưu (số), mặc định 10. */
let lastSavedThangDiem = 10;
let lastGoodParsed = [];
let previewTimer = null;

function getToken() {
    return storage.getItem('token');
}

function isTokenExpired() {
    const exp = storage.getItem('tokenExpiresAt');
    return !exp || Date.now() > parseInt(exp, 10);
}

function kiemTraXacThuc() {
    const token = getToken();
    const vaiTro = storage.getItem('vaiTro');
    if (!token || vaiTro !== 'GIAO_VIEN' || isTokenExpired()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

function escHtml(str) {
    return String(str || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function flushStem(cur, stem) {
    if (stem.length === 0) return;
    const chunk = stem.join(' ').trim();
    if (!cur.noiDung) {
        cur.noiDung = chunk;
    } else {
        cur.noiDung = `${cur.noiDung} ${chunk}`.trim();
    }
    stem.length = 0;
}

function ketThucCau(cur, stem, nhieuDapAn) {
    flushStem(cur, stem);
    if (nhieuDapAn) {
        return 'Mỗi câu chỉ được đánh dấu một đáp án đúng (một dòng có dấu *).';
    }
    if (!cur.noiDung || !String(cur.noiDung).trim()) {
        return 'Có câu hỏi thiếu nội dung.';
    }
    if (!cur.luaChonA || !String(cur.luaChonA).trim() || !cur.luaChonB || !String(cur.luaChonB).trim()) {
        return 'Mỗi câu cần ít nhất lựa chọn A và B.';
    }
    if (!cur.dapAnDung || !String(cur.dapAnDung).trim()) {
        return 'Mỗi câu cần đánh dấu đáp án đúng bằng * trước chữ cái (vd: *B.).';
    }
    return null;
}

/**
 * @param {string} raw
 * @param {Array<Object>} out  { noiDung, luaChonA..D, dapAnDung }
 * @returns {string|null} lỗi hoặc null
 */
function parseVanBanTho(raw, out) {
    out.length = 0;
    if (raw == null || String(raw).trim() === '') {
        return null;
    }
    const lines = String(raw).split(/\r\n|\n|\r/);
    let cur = null;
    let stem = [];
    let nhieuDapAn = false;

    for (const line of lines) {
        const t = line.trim();
        if (t === '') continue;

        const mq = t.match(DONG_SO_CAU);
        if (mq) {
            if (cur) {
                const err = ketThucCau(cur, stem, nhieuDapAn);
                if (err) return err;
                out.push(cur);
            }
            cur = {
                noiDung: '',
                luaChonA: '',
                luaChonB: '',
                luaChonC: '',
                luaChonD: '',
                dapAnDung: ''
            };
            stem = [mq[1].trim()];
            nhieuDapAn = false;
            continue;
        }

        const mo = t.match(DONG_LUA_CHON);
        if (mo && cur) {
            flushStem(cur, stem);
            const letter = mo[2].toUpperCase();
            const noiDungOpt = mo[3].trim();
            const star = mo[1] && mo[1].length > 0;
            if (star) {
                if (cur.dapAnDung) nhieuDapAn = true;
                cur.dapAnDung = letter;
            }
            if (letter === 'A') cur.luaChonA = noiDungOpt;
            else if (letter === 'B') cur.luaChonB = noiDungOpt;
            else if (letter === 'C') cur.luaChonC = noiDungOpt;
            else if (letter === 'D') cur.luaChonD = noiDungOpt;
            continue;
        }

        if (!cur) continue;
        stem.push(t);
    }

    if (cur) {
        const err = ketThucCau(cur, stem, nhieuDapAn);
        if (err) return err;
        out.push(cur);
    }

    if (out.length === 0) {
        return 'Không tìm thấy câu hỏi hợp lệ. Mỗi câu bắt đầu bằng số thứ tự (vd: 1. Nội dung...).';
    }
    return null;
}

function updateLineGutter() {
    const ta = document.getElementById('vanBanTho');
    const g = document.getElementById('lineGutter');
    if (!ta || !g) return;
    const n = Math.max(1, String(ta.value).split(/\r\n|\n|\r/).length);
    g.innerHTML = Array.from({ length: n }, (_, i) => `<div>${i + 1}</div>`).join('');
}

function renderPreview(list) {
    const body = document.getElementById('previewBody');
    if (!body) return;

    if (!list || list.length === 0) {
        body.innerHTML = `
            <div class="preview-empty">
                <i class="fas fa-eye"></i>
                Chưa có nội dung để xem trước.<br>
                <small>Nhập câu hỏi theo định dạng bên phải (1. …, A. …, *B. …).</small>
            </div>`;
        return;
    }

    body.innerHTML = list
        .map((q, idx) => {
            const dung = (q.dapAnDung || '').toUpperCase();
            const opts = ['A', 'B', 'C', 'D'].map((L) => {
                const key = `luaChon${L}`;
                const txt = q[key] != null ? String(q[key]).trim() : '';
                if (!txt) return '';
                const ok = L === dung;
                return `
                    <div class="q-opt ${ok ? 'correct' : ''}">
                        <span class="fake-radio"></span>
                        <span class="q-opt-letter">${L}.</span>
                        <span>${escHtml(txt)}</span>
                        ${ok ? '<span class="q-opt-checkico"><i class="fas fa-check"></i></span>' : ''}
                    </div>`;
            }).join('');
            return `
                <article class="q-card">
                    <div class="q-card-stem">
                        <span class="q-num">Q${idx + 1}</span>
                        <p class="q-text">${escHtml(q.noiDung || '')}</p>
                    </div>
                    ${opts}
                </article>`;
        })
        .join('');
}

function capNhatXemTruoc() {
    const ta = document.getElementById('vanBanTho');
    const warn = document.getElementById('previewWarning');
    const warnText = document.getElementById('previewWarningText');
    if (!ta) return;

    const raw = ta.value;
    const temp = [];
    const err = parseVanBanTho(raw, temp);

    if (!err) {
        lastGoodParsed = temp.slice();
        if (warn) warn.style.display = 'none';
        renderPreview(lastGoodParsed);
    } else {
        if (warn && warnText) {
            warn.style.display = 'block';
            warnText.textContent = err;
        }
        renderPreview(lastGoodParsed.length ? lastGoodParsed : []);
    }
    updateLineGutter();
}

function schedulePreview() {
    clearTimeout(previewTimer);
    previewTimer = setTimeout(capNhatXemTruoc, 120);
}

function dongBoNgay() {
    capNhatXemTruoc();
}

function hienThiTenNguoiDung() {
    try {
        const nd = JSON.parse(storage.getItem('nguoiDung') || '{}');
        const el = document.getElementById('displayName');
        if (el) el.textContent = nd.hoTen || nd.ho || '—';
    } catch (_) {}
}

function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const toggle = document.getElementById('sidebarToggle');
    const close = document.getElementById('sidebarClose');
    if (toggle) toggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    if (close) close.addEventListener('click', () => sidebar.classList.remove('active'));
}

function setupLogout() {
    document.getElementById('btnLogout')?.addEventListener('click', () => {
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = '/login';
    });
}

async function apiGet(url) {
    const res = await fetch(url, { headers: { Authorization: `Bearer ${getToken()}` } });
    return res.json();
}

async function apiPut(url, body) {
    const res = await fetch(url, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${getToken()}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return res.json();
}

function badgeTrangThai(tt) {
    if (tt === 'CONG_KHAI') return { cls: 'cong-khai', label: 'Công khai' };
    return { cls: 'nhap', label: 'Nháp' };
}

function setLoading(on) {
    let el = document.getElementById('globalLoadOverlay');
    if (!on) {
        if (el) el.remove();
        return;
    }
    if (!el) {
        el = document.createElement('div');
        el.id = 'globalLoadOverlay';
        el.className = 'global-load-overlay';
        el.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang tải...';
        document.body.appendChild(el);
    }
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const icons = { success: 'fas fa-check-circle', error: 'fas fa-times-circle', info: 'fas fa-info-circle' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<i class="${icons[type] || icons.info}"></i> ${escHtml(message)}`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(40px)';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

/** Đọc thang điểm từ ô nhập (null nếu không hợp lệ). */
function docThangDiemTuInput() {
    const inp = document.getElementById('inputThangDiem');
    if (!inp) return null;
    const v = parseFloat(String(inp.value).replace(',', '.'));
    return Number.isFinite(v) ? v : null;
}

function giongThangDiem(a, b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;
    return Math.abs(a - b) < 1e-5;
}

/** Hiển thị điểm mỗi câu = thang ÷ số câu (chia đều). */
function capNhatHienThiDiemChia() {
    const lbl = document.getElementById('labelDiemMoiCau');
    const lblSo = document.getElementById('labelSoCauCham');
    if (lblSo) lblSo.textContent = String(soCauTrongDe);
    if (!lbl) return;
    const thang = docThangDiemTuInput();
    if (soCauTrongDe <= 0 || thang == null || thang <= 0) {
        lbl.textContent = '—';
        return;
    }
    const moi = thang / soCauTrongDe;
    lbl.textContent = formatSoDiemViet(moi);
}

/** Rút gọn phần thập phân (vd: 0.25, 0.333333). */
function formatSoDiemViet(x) {
    const s = x.toFixed(6).replace(/\.?0+$/, '');
    return s;
}

function isDirty() {
    const ta = document.getElementById('vanBanTho');
    const vb = ta && ta.value !== lastSavedVanBan;
    const th = !giongThangDiem(docThangDiemTuInput(), lastSavedThangDiem);
    return Boolean(vb || th);
}

function quayLaiDanhSach() {
    if (isDirty()) {
        const ok = window.confirm('Bạn có thay đổi chưa lưu. Bỏ qua và quay lại?');
        if (!ok) return;
    }
    window.location.href = '/dashboard/giao-vien/de-thi';
}

async function taiDuLieu() {
    setLoading(true);
    try {
        const res = await apiGet(`${API_BASE}/${encodeURIComponent(deThiId)}/cau-hoi/van-ban-tho`);
        if (!res.success) {
            showToast(res.message || 'Không tải được dữ liệu.', 'error');
            setTimeout(() => {
                window.location.href = '/dashboard/giao-vien/de-thi';
            }, 1600);
            return;
        }
        const d = res.data || {};
        document.getElementById('pageTitleDe').textContent = d.tenDeThi || 'Đề thi';
        const ma = d.maDeThi || '';
        const mh = d.tenMonHoc || '';
        const sc = typeof d.soCau === 'number' ? d.soCau : 0;
        soCauTrongDe = sc;
        document.getElementById('pageMetaDe').textContent = [ma, mh, sc ? `${sc} câu` : '']
            .filter(Boolean)
            .join(' · ');

        const thangTuApi = d.thangDiemToiDa != null ? Number(d.thangDiemToiDa) : 10;
        lastSavedThangDiem = Number.isFinite(thangTuApi) && thangTuApi > 0 ? thangTuApi : 10;
        const inpThang = document.getElementById('inputThangDiem');
        if (inpThang) inpThang.value = String(lastSavedThangDiem);
        capNhatHienThiDiemChia();

        const b = badgeTrangThai(d.trangThai);
        const badgeEl = document.getElementById('badgeTrangThai');
        badgeEl.textContent = b.label;
        badgeEl.className = `badge-trang-thai ${b.cls}`;

        const ta = document.getElementById('vanBanTho');
        ta.value = d.vanBan != null ? d.vanBan : '';
        lastSavedVanBan = ta.value;
        lastGoodParsed = [];
        const tmp = [];
        if (!parseVanBanTho(ta.value, tmp)) {
            lastGoodParsed = tmp.slice();
        }
        capNhatXemTruoc();
    } catch (e) {
        showToast('Lỗi kết nối.', 'error');
    } finally {
        setLoading(false);
    }
}

async function luuVanBan() {
    const ta = document.getElementById('vanBanTho');
    const raw = ta.value;
    const chk = [];
    const err = parseVanBanTho(raw, chk);
    if (err) {
        showToast(err, 'error');
        return;
    }

    const thang = docThangDiemTuInput();
    if (thang == null || thang < 0.01 || thang > 1000) {
        showToast('Thang điểm phải từ 0,01 đến 1000.', 'error');
        return;
    }

    setLoading(true);
    try {
        const res = await apiPut(`${API_BASE}/${encodeURIComponent(deThiId)}/cau-hoi/van-ban-tho`, {
            vanBan: raw,
            thangDiemToiDa: thang
        });
        if (!res.success) {
            showToast(res.message || 'Lưu thất bại.', 'error');
            return;
        }
        lastSavedVanBan = raw;
        lastSavedThangDiem = thang;
        showToast('Đã lưu thay đổi.', 'success');
        const foot = document.getElementById('footerLastEdit');
        if (foot) foot.textContent = `Chỉnh sửa lần cuối: vừa xong (${new Date().toLocaleString('vi-VN')})`;
    } catch (_) {
        showToast('Lỗi kết nối.', 'error');
    } finally {
        setLoading(false);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!kiemTraXacThuc()) return;

    deThiId = (document.getElementById('deThiId') || {}).value || '';
    if (!deThiId) {
        showToast('Thiếu mã đề thi.', 'error');
        return;
    }

    setupSidebar();
    setupLogout();
    hienThiTenNguoiDung();

    document.getElementById('btnHuyBo')?.addEventListener('click', quayLaiDanhSach);
    document.getElementById('btnLuuNhap')?.addEventListener('click', luuVanBan);
    document.getElementById('btnQuayLai')?.addEventListener('click', quayLaiDanhSach);
    document.getElementById('btnDongBo')?.addEventListener('click', dongBoNgay);
    document.getElementById('vanBanTho')?.addEventListener('input', schedulePreview);
    document.getElementById('inputThangDiem')?.addEventListener('input', capNhatHienThiDiemChia);
    document.getElementById('btnPrintPreview')?.addEventListener('click', () => window.print());

    taiDuLieu();
});

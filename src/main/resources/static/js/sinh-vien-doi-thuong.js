/**
 * Sinh viên — Đổi thưởng (gọi API + modal).
 * Cần tải sau sinh-vien-phong-thi.js (sidebar / đăng xuất / tên hiển thị).
 */
const API_DT = '/api/sinh-vien/doi-thuong';
const storageDt = localStorage.getItem('token') ? localStorage : sessionStorage;

const LOAI_LABEL = {
    HUY_HIEU: 'Huy hiệu',
    VAT_PHAM_HOC_TAP: 'Vật phẩm học tập',
    VOUCHER: 'Voucher',
    QUYEN_LOI_HE_THONG: 'Quyền lợi hệ thống'
};

const TT_LABEL = {
    CHO_DUYET: 'Chờ duyệt',
    DA_DUYET: 'Đã duyệt',
    DA_NHAN_QUA: 'Đã nhận quà',
    DA_HUY: 'Đã hủy'
};

const CARD_LABEL = {
    CO_THE_DOI: 'Có thể đổi',
    KHONG_DU_DIEM: 'Không đủ điểm',
    HET_HANG: 'Hết hàng'
};

function getTokenDt() {
    return storageDt.getItem('token');
}

function escDt(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function showToastDt(message, type = 'error') {
    const c = document.getElementById('toastContainer');
    if (!c) return;
    const el = document.createElement('div');
    el.className = type === 'error' ? 'toast toast-error' : 'toast toast-info';
    el.innerHTML = `<i class="fas fa-${type === 'error' ? 'times-circle' : 'info-circle'}"></i> ${escDt(message)}`;
    c.appendChild(el);
    setTimeout(() => {
        el.style.opacity = '0';
        setTimeout(() => el.remove(), 300);
    }, 3200);
}

let tongQuanCache = { diemHienTai: 0, tongLuotDoiThanhCong: 0 };
let pendingReward = null;
let historyRows = [];
let searchDebounce;

function openOverlay(id) {
    const el = document.getElementById(id);
    if (el) {
        el.classList.add('dt-open');
        el.setAttribute('aria-hidden', 'false');
    }
}

function closeOverlay(id) {
    const el = document.getElementById(id);
    if (el) {
        el.classList.remove('dt-open');
        el.setAttribute('aria-hidden', 'true');
    }
}

async function fetchJsonDt(url, options = {}) {
    const headers = Object.assign(
        { Authorization: `Bearer ${getTokenDt()}` },
        options.headers || {}
    );
    if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }
    const res = await fetch(url, Object.assign({}, options, { headers }));
    const json = await res.json().catch(() => ({}));
    if (res.status === 401) {
        storageDt.removeItem('nguoiDung');
        storageDt.removeItem('vaiTro');
        storageDt.removeItem('token');
        storageDt.removeItem('tokenExpiresAt');
        window.location.href = '/login?expired=1';
        throw new Error('401');
    }
    return { res, json };
}

async function taiTongQuan() {
    const { res, json } = await fetchJsonDt(`${API_DT}/tong-quan`);
    if (!res.ok || !json.success) {
        showToastDt(json.message || 'Không tải được tổng quan.');
        return;
    }
    tongQuanCache = json.data || tongQuanCache;
    const diem = tongQuanCache.diemHienTai ?? 0;
    const luot = tongQuanCache.tongLuotDoiThanhCong ?? 0;
    const elD = document.getElementById('dtStatDiem');
    const elL = document.getElementById('dtStatLuot');
    if (elD) elD.textContent = `${diem} điểm`;
    if (elL) elL.textContent = String(luot);
}

function queryRewardFilters() {
    const q = document.getElementById('dtFilterSearch')?.value?.trim() || '';
    const loai = document.getElementById('dtFilterLoai')?.value || '';
    const mucDiem = document.getElementById('dtFilterMucDiem')?.value || '';
    const loc = document.getElementById('dtFilterCardTrangThai')?.value || '';
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    if (loai && loai !== 'TAT_CA') p.set('loai', loai);
    if (mucDiem && mucDiem !== 'TAT_CA') p.set('mucDiem', mucDiem);
    if (loc && loc !== 'TAT_CA') p.set('locTrangThai', loc);
    const qs = p.toString();
    return qs ? `?${qs}` : '';
}

async function taiPhanThuong() {
    const grid = document.getElementById('rewardGrid');
    const empty = document.getElementById('rewardEmpty');
    if (!grid) return;
    grid.innerHTML =
        '<p style="grid-column:1/-1;text-align:center;color:#718096;padding:2rem;"><i class="fas fa-spinner fa-spin"></i> Đang tải…</p>';
    const { res, json } = await fetchJsonDt(`${API_DT}/phan-thuong${queryRewardFilters()}`);
    if (!res.ok || !json.success) {
        grid.innerHTML = '';
        showToastDt(json.message || 'Không tải danh sách phần thưởng.');
        if (empty) empty.hidden = false;
        return;
    }
    const list = json.data || [];
    if (list.length === 0) {
        grid.innerHTML = '';
        if (empty) empty.hidden = false;
        return;
    }
    if (empty) empty.hidden = true;
    grid.innerHTML = list.map(renderRewardCard).join('');
    grid.querySelectorAll('[data-action="doi"]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const ten = btn.getAttribute('data-ten');
            const diem = parseInt(btn.getAttribute('data-diem'), 10);
            moModalXacNhan({ id, ten, diemDoi: diem });
        });
    });
}

function renderRewardCard(row) {
    const loaiTxt = LOAI_LABEL[row.loai] || row.loai;
    const tt = row.trangThaiCard;
    const badgeClass =
        tt === 'HET_HANG' ? 'dt-badge-het' : tt === 'KHONG_DU_DIEM' ? 'dt-badge-du' : 'dt-badge-stock';
    const can = tt === 'CO_THE_DOI';
    const btnLabel = tt === 'HET_HANG' ? 'Hết hàng' : tt === 'KHONG_DU_DIEM' ? 'Không đủ điểm' : 'Đổi thưởng';
    const icon = escDt(row.iconClass || 'fas fa-gift');
    const anh = row.anhUrl ? String(row.anhUrl).trim() : '';
    const visualInner = anh
        ? `<img src="${escDt(anh)}" alt="${escDt(row.ten)}" class="dt-reward-img" loading="lazy" decoding="async" width="400" height="300">`
        : `<i class="${icon}"></i>`;
    return `<article class="dt-reward-card ${can ? '' : 'dt-card-muted'}" data-id="${escDt(row.id)}">
      <div class="dt-reward-visual">${visualInner}</div>
      <div class="dt-reward-body">
        <h3>${escDt(row.ten)}</h3>
        <p class="dt-reward-desc">${escDt(row.moTaNgan || '')}</p>
        <div class="dt-reward-meta">
          <span class="dt-price">${row.diemDoi} điểm</span>
          <span class="dt-badge ${badgeClass}">${CARD_LABEL[tt] || tt}</span>
        </div>
        <p class="dt-stock-line"><i class="fas fa-box-open" style="margin-right:6px;opacity:.6;"></i>Còn lại: <strong>${row.soLuongConLai}</strong> · ${escDt(loaiTxt)}</p>
        <button type="button" class="dt-btn dt-btn-primary" style="width:100%;justify-content:center;"
          data-action="doi" data-id="${escDt(row.id)}" data-ten="${escDt(row.ten)}" data-diem="${row.diemDoi}"
          ${can ? '' : 'disabled'}>${escDt(btnLabel)}</button>
      </div>
    </article>`;
}

function queryHistoryFilters() {
    const q = document.getElementById('dtHistSearch')?.value?.trim() || '';
    const trangThai = document.getElementById('dtHistTrangThai')?.value || '';
    const khoang = document.getElementById('dtHistKhoang')?.value || '';
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    if (trangThai && trangThai !== 'TAT_CA') p.set('trangThai', trangThai);
    if (khoang && khoang !== 'TAT_CA') p.set('khoang', khoang);
    const qs = p.toString();
    return qs ? `?${qs}` : '';
}

function formatThoiGianDt(iso) {
    if (!iso) return '—';
    try {
        const d = new Date(iso);
        if (Number.isNaN(d.getTime())) return iso;
        return d.toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
    } catch {
        return iso;
    }
}

function badgeClassForTt(tt) {
    switch (tt) {
        case 'CHO_DUYET':
            return 'dt-badge-tt-cho';
        case 'DA_DUYET':
            return 'dt-badge-tt-duyet';
        case 'DA_NHAN_QUA':
            return 'dt-badge-tt-nhan';
        case 'DA_HUY':
            return 'dt-badge-tt-huy';
        default:
            return '';
    }
}

async function taiLichSu() {
    const tbody = document.getElementById('dtHistBody');
    const empty = document.getElementById('historyEmpty');
    if (!tbody) return;
    tbody.innerHTML =
        '<tr><td colspan="7" style="text-align:center;color:#718096;padding:1.5rem;"><i class="fas fa-spinner fa-spin"></i> Đang tải…</td></tr>';
    const { res, json } = await fetchJsonDt(`${API_DT}/lich-su${queryHistoryFilters()}`);
    if (!res.ok || !json.success) {
        tbody.innerHTML = '';
        showToastDt(json.message || 'Không tải lịch sử.');
        if (empty) empty.hidden = false;
        return;
    }
    historyRows = json.data || [];
    if (historyRows.length === 0) {
        tbody.innerHTML = '';
        if (empty) empty.hidden = false;
        return;
    }
    if (empty) empty.hidden = true;
    tbody.innerHTML = historyRows
        .map((r) => {
            const tt = r.trangThai;
            const bcls = badgeClassForTt(tt);
            const canHuy = tt === 'CHO_DUYET';
            return `<tr data-id="${escDt(r.id)}">
        <td><code style="font-size:0.8rem;">${escDt(r.maDoi)}</code></td>
        <td>${escDt(r.tenPhanThuong)}</td>
        <td>${r.diemDaDung}</td>
        <td>${escDt(formatThoiGianDt(r.thoiGian))}</td>
        <td><span class="dt-badge ${bcls}">${escDt(TT_LABEL[tt] || tt)}</span></td>
        <td>${escDt(r.ghiChu || '—')}</td>
        <td>
          <button type="button" class="dt-link" data-hist-detail="${escDt(r.id)}">Xem chi tiết</button>
          ${canHuy ? `<button type="button" class="dt-link dt-link-danger" data-hist-huy="${escDt(r.id)}">Hủy yêu cầu</button>` : ''}
        </td>
      </tr>`;
        })
        .join('');

    tbody.querySelectorAll('[data-hist-detail]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-hist-detail');
            const row = historyRows.find((x) => x.id === id);
            if (row) moChiTiet(row);
        });
    });
    tbody.querySelectorAll('[data-hist-huy]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-hist-huy');
            huyYeuCau(id);
        });
    });
}

function moModalXacNhan(r) {
    pendingReward = r;
    const diemHt = tongQuanCache.diemHienTai ?? 0;
    const conLai = Math.max(0, diemHt - r.diemDoi);
    document.getElementById('dtCfTen').textContent = r.ten;
    document.getElementById('dtCfDiemCan').textContent = `${r.diemDoi} điểm`;
    document.getElementById('dtCfDiemHt').textContent = `${diemHt} điểm`;
    document.getElementById('dtCfDiemCl').textContent = `${conLai} điểm`;
    openOverlay('modalConfirmDoi');
}

function moChiTiet(row) {
    const body = document.getElementById('dtDetailBody');
    if (!body) return;
    const tt = row.trangThai;
    body.innerHTML = `
    <div class="dt-modal-dl">
      <div><dt>Mã đổi thưởng (6 ký tự)</dt><dd><strong style="letter-spacing:0.06em;">${escDt(row.maDoi)}</strong></dd></div>
      <div><dt>Tên phần thưởng</dt><dd>${escDt(row.tenPhanThuong)}</dd></div>
      <div><dt>Mô tả</dt><dd>${escDt(row.moTaNgan || '—')}</dd></div>
      <div><dt>Điểm đã dùng</dt><dd>${row.diemDaDung}</dd></div>
      <div><dt>Thời gian</dt><dd>${escDt(formatThoiGianDt(row.thoiGian))}</dd></div>
      <div><dt>Trạng thái</dt><dd>${escDt(TT_LABEL[tt] || tt)}</dd></div>
      <div><dt>Ghi chú</dt><dd>${escDt(row.ghiChu || '—')}</dd></div>
    </div>`;
    openOverlay('modalDetailLs');
}

async function xacNhanDoi() {
    if (!pendingReward) return;
    const btn = document.getElementById('btnCfDoi');
    if (btn) btn.disabled = true;
    try {
        const { res, json } = await fetchJsonDt(`${API_DT}/doi`, {
            method: 'POST',
            body: JSON.stringify({ phanThuongId: pendingReward.id })
        });
        closeOverlay('modalConfirmDoi');
        if (!res.ok || !json.success) {
            document.getElementById('modalErrText').textContent =
                json.message || 'Không thể đổi thưởng. Vui lòng thử lại.';
            openOverlay('modalErrorDoi');
            return;
        }
        const maEl = document.getElementById('dtSuccessMaDoi');
        if (maEl) maEl.textContent = json.data && json.data.maDoi ? json.data.maDoi : '—';
        openOverlay('modalSuccessDoi');
        await Promise.all([taiTongQuan(), taiPhanThuong(), taiLichSu()]);
    } catch (e) {
        if (e.message !== '401') showToastDt('Lỗi kết nối.');
    } finally {
        pendingReward = null;
        if (btn) btn.disabled = false;
    }
}

async function huyYeuCau(id) {
    if (!confirm('Bạn có chắc muốn hủy yêu cầu đổi thưởng này?')) return;
    const { res, json } = await fetchJsonDt(`${API_DT}/huy/${encodeURIComponent(id)}`, { method: 'POST' });
    if (!res.ok || !json.success) {
        showToastDt(json.message || 'Không hủy được yêu cầu.');
        return;
    }
    showToastDt('Đã hủy yêu cầu.', 'info');
    await Promise.all([taiTongQuan(), taiPhanThuong(), taiLichSu()]);
}

function resetFiltersDt() {
    ['dtFilterLoai', 'dtFilterMucDiem', 'dtFilterCardTrangThai', 'dtHistTrangThai', 'dtHistKhoang'].forEach((id) => {
        const el = document.getElementById(id);
        if (el) el.value = 'TAT_CA';
    });
    const s = document.getElementById('dtFilterSearch');
    if (s) s.value = '';
    const hs = document.getElementById('dtHistSearch');
    if (hs) hs.value = '';
    taiPhanThuong();
    taiLichSu();
}

function initDoiThuongPage() {
    if (!document.getElementById('rewardGrid')) return;

    document.getElementById('btnCfHuy')?.addEventListener('click', () => {
        closeOverlay('modalConfirmDoi');
        pendingReward = null;
    });
    document.getElementById('btnCfDoi')?.addEventListener('click', () => xacNhanDoi());
    document.getElementById('btnSuccessClose')?.addEventListener('click', () => closeOverlay('modalSuccessDoi'));
    document.getElementById('btnErrClose')?.addEventListener('click', () => closeOverlay('modalErrorDoi'));
    document.getElementById('btnDetailClose')?.addEventListener('click', () => closeOverlay('modalDetailLs'));

    ['dtFilterLoai', 'dtFilterMucDiem', 'dtFilterCardTrangThai'].forEach((id) => {
        document.getElementById(id)?.addEventListener('change', () => taiPhanThuong());
    });
    document.getElementById('dtFilterSearch')?.addEventListener('input', () => {
        clearTimeout(searchDebounce);
        searchDebounce = setTimeout(() => taiPhanThuong(), 320);
    });

    ['dtHistTrangThai', 'dtHistKhoang'].forEach((id) => {
        document.getElementById(id)?.addEventListener('change', () => taiLichSu());
    });
    document.getElementById('dtHistSearch')?.addEventListener('input', () => {
        clearTimeout(searchDebounce);
        searchDebounce = setTimeout(() => taiLichSu(), 320);
    });

    document.getElementById('btnResetFiltersDt')?.addEventListener('click', () => resetFiltersDt());

    document.querySelectorAll('.dt-modal-overlay').forEach((ov) => {
        ov.addEventListener('click', (e) => {
            if (e.target === ov && ov.id !== 'modalConfirmDoi') {
                ov.classList.remove('dt-open');
                ov.setAttribute('aria-hidden', 'true');
            }
        });
    });

    taiTongQuan().then(() => {
        taiPhanThuong();
        taiLichSu();
    });
}

document.addEventListener('DOMContentLoaded', initDoiThuongPage);

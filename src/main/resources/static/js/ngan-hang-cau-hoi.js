/**
 * ngan-hang-cau-hoi.js — JavaScript cho trang Ngân Hàng Câu Hỏi
 *
 * Chức năng:
 *  - Tải & hiển thị danh sách câu hỏi với phân trang
 *  - Lọc theo môn học, chủ đề, độ khó, từ khóa
 *  - Thêm / Sửa / Xóa câu hỏi qua modal
 *  - Quản lý chủ đề (Tạo mới, xem theo môn)
 *  - Toast notification cho mọi thao tác
 */

// ============================================================
// CONSTANTS & STATE
// ============================================================
const API_BASE   = '/api/giao-vien/ngan-hang-cau-hoi';
const storage    = localStorage.getItem('token') ? localStorage : sessionStorage;

let allCauHoi      = [];   // Toàn bộ dữ liệu từ server
let filteredCauHoi = [];   // Sau khi lọc
let allMonHoc      = [];   // Danh sách môn học
let editingId      = null; // ID câu hỏi đang sửa
let pendingDeleteId = null;
let filterTimer    = null;

// Pagination state
let currentPage  = 1;
let pageSize     = 20;

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', async () => {
    setupSidebar();
    setupLogout();
    hienThiTenNguoiDung();
    await taiMonHoc();
    await taiDanhSachCauHoi();

    document.getElementById('pageSize').addEventListener('change', e => {
        pageSize = parseInt(e.target.value);
        currentPage = 1;
        renderTable();
    });
});

// ============================================================
// SIDEBAR & LOGOUT
// ============================================================
function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    document.getElementById('sidebarToggle')?.addEventListener('click', () => sidebar.classList.toggle('active'));
    document.getElementById('sidebarClose')?.addEventListener('click',  () => sidebar.classList.remove('active'));
}

function setupLogout() {
    document.getElementById('btnLogout')?.addEventListener('click', () => {
        localStorage.clear(); sessionStorage.clear(); window.location.href = '/login';
    });
}

function hienThiTenNguoiDung() {
    try {
        const nd = JSON.parse(storage.getItem('nguoiDung') || '{}');
        document.getElementById('displayName').textContent = nd.hoTen || nd.ho || '—';
    } catch (_) {}
}

function getToken() { return storage.getItem('token'); }
function isTokenExpired() {
    const exp = storage.getItem('tokenExpiresAt');
    return !exp || Date.now() > parseInt(exp);
}
function kiemTraXacThuc() {
    if (!getToken() || storage.getItem('vaiTro') !== 'GIAO_VIEN' || isTokenExpired()) {
        window.location.href = '/login'; return false;
    }
    return true;
}

// ============================================================
// API
// ============================================================
async function apiGet(url) {
    const r = await fetch(url, { headers: { 'Authorization': 'Bearer ' + getToken() } });
    return r.json();
}
async function apiPost(url, body) {
    const r = await fetch(url, {
        method: 'POST', headers: { 'Authorization': 'Bearer ' + getToken(), 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return r.json();
}
async function apiPut(url, body) {
    const r = await fetch(url, {
        method: 'PUT', headers: { 'Authorization': 'Bearer ' + getToken(), 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return r.json();
}
async function apiDelete(url) {
    const r = await fetch(url, { method: 'DELETE', headers: { 'Authorization': 'Bearer ' + getToken() } });
    return r.json();
}

// ============================================================
// LOAD DATA
// ============================================================
async function taiMonHoc() {
    if (!kiemTraXacThuc()) return;
    try {
        const res = await apiGet(API_BASE + '/mon-hoc');
        if (!res.success) return;
        allMonHoc = res.data || [];

        // Populate all mon-hoc selects
        const ids = ['filterMonHoc', 'inputMonHoc', 'newChuDeMonHoc', 'chuDeViewMonHoc', 'nhanh_monHoc'];
        ids.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            // Keep first option (placeholder)
            const first = el.options[0];
            el.innerHTML = '';
            el.appendChild(first);
            allMonHoc.forEach(mh => el.appendChild(new Option(mh.ten, mh.id)));
        });
    } catch (err) { console.error(err); }
}

async function taiDanhSachCauHoi() {
    if (!kiemTraXacThuc()) return;
    try {
        const res = await apiGet(API_BASE);
        if (res.success) {
            allCauHoi = res.data || [];
            filteredCauHoi = [...allCauHoi];
            capNhatThongKe();
            currentPage = 1;
            renderTable();
        }
    } catch (err) { showToast('Lỗi kết nối server!', 'error'); console.error(err); }
}

async function taiChuDe(monHocId, targetSelectId) {
    if (!monHocId) {
        const el = document.getElementById(targetSelectId);
        if (el) { el.innerHTML = '<option value="">Tất cả chủ đề</option>'; }
        return;
    }
    try {
        const res = await apiGet(`${API_BASE}/chu-de?monHocId=${encodeURIComponent(monHocId)}`);
        if (!res.success) return;
        const el = document.getElementById(targetSelectId);
        if (!el) return;
        const first = el.options[0];
        el.innerHTML = '';
        el.appendChild(first);
        (res.data || []).forEach(cd => el.appendChild(new Option(cd.ten, cd.id)));
    } catch (err) { console.error(err); }
}

// ============================================================
// STATS
// ============================================================
function capNhatThongKe() {
    document.getElementById('statTong').textContent = allCauHoi.length;
    document.getElementById('statDe').textContent   = allCauHoi.filter(c => c.doKho === 'DE').length;
    document.getElementById('statTB').textContent   = allCauHoi.filter(c => c.doKho === 'TRUNG_BINH').length;
    document.getElementById('statKho').textContent  = allCauHoi.filter(c => c.doKho === 'KHO').length;
}

// ============================================================
// FILTER
// ============================================================
function onMonHocChange() {
    const monHocId = document.getElementById('filterMonHoc').value;
    taiChuDe(monHocId, 'filterChuDe');
    applyFilter();
}

function debounceFilter() {
    clearTimeout(filterTimer);
    filterTimer = setTimeout(applyFilter, 300);
}

async function applyFilter() {
    if (!kiemTraXacThuc()) return;
    const monHocId = document.getElementById('filterMonHoc').value   || null;
    const chuDeId  = document.getElementById('filterChuDe').value    || null;
    const doKho    = document.getElementById('filterDoKho').value    || null;
    const keyword  = document.getElementById('filterKeyword').value  || null;

    // If all filters are empty, use cached data
    const hasFilter = monHocId || chuDeId || doKho || (keyword && keyword.trim());
    if (!hasFilter) {
        filteredCauHoi = [...allCauHoi];
        currentPage = 1;
        renderTable();
        return;
    }

    try {
        let url = API_BASE + '?';
        if (monHocId) url += `monHocId=${encodeURIComponent(monHocId)}&`;
        if (chuDeId)  url += `chuDeId=${encodeURIComponent(chuDeId)}&`;
        if (doKho)    url += `doKho=${encodeURIComponent(doKho)}&`;
        if (keyword)  url += `keyword=${encodeURIComponent(keyword.trim())}&`;

        const res = await apiGet(url);
        if (res.success) {
            filteredCauHoi = res.data || [];
            currentPage = 1;
            renderTable();
        }
    } catch (err) { console.error(err); }
}

function resetFilter() {
    document.getElementById('filterMonHoc').value  = '';
    document.getElementById('filterChuDe').value   = '';
    document.getElementById('filterDoKho').value   = '';
    document.getElementById('filterKeyword').value = '';
    filteredCauHoi = [...allCauHoi];
    currentPage = 1;
    renderTable();
}

// ============================================================
// RENDER TABLE (with pagination)
// ============================================================
function renderTable() {
    const tbody = document.getElementById('cauHoiBody');
    const total = filteredCauHoi.length;

    document.getElementById('questionCount').textContent = `(${total} câu hỏi)`;

    if (total === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:40px;color:#a0aec0;">
            <i class="fas fa-inbox" style="font-size:2rem;display:block;margin-bottom:10px;"></i>
            Không tìm thấy câu hỏi nào.
        </td></tr>`;
        renderPagination(0, 0, 0);
        return;
    }

    const totalPages = Math.ceil(total / pageSize);
    if (currentPage > totalPages) currentPage = totalPages;
    const start = (currentPage - 1) * pageSize;
    const end   = Math.min(start + pageSize, total);
    const page  = filteredCauHoi.slice(start, end);

    tbody.innerHTML = page.map((c, i) => `
        <tr>
            <td>${start + i + 1}</td>
            <td>
                <div class="question-preview" title="${escHtml(c.noiDung || '')}">
                    ${escHtml(truncate(c.noiDung || '', 80))}
                </div>
            </td>
            <td>
                <div style="font-weight:500;font-size:0.85rem;">${escHtml(c.tenMonHoc || '')}</div>
                <div style="font-size:0.78rem;color:#718096;">${escHtml(c.tenChuDe || '')}</div>
            </td>
            <td>${badgeLoai(c.loaiCauHoi)}</td>
            <td>${badgeDoKho(c.doKho)}</td>
            <td>
                <span style="font-weight:700;color:#667eea;font-size:1rem;">${escHtml(c.dapAnDung || '')}</span>
            </td>
            <td>
                ${c.soDeThiSuDung > 0
                    ? `<span style="font-size:0.82rem;color:#38a169;">${c.soDeThiSuDung} đề thi</span>`
                    : '<span style="font-size:0.82rem;color:#a0aec0;">Chưa dùng</span>'}
            </td>
            <td>
                <button class="btn-icon btn-icon-edit" title="Sửa" onclick="moModalSua('${c.id}')">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-icon btn-icon-delete" title="Xóa" onclick="moConfirmXoa('${c.id}')">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
        </tr>
    `).join('');

    renderPagination(total, currentPage, totalPages);
    document.getElementById('paginationInfo').textContent =
        `Hiển thị ${start + 1}–${end} trong tổng ${total} câu hỏi`;
}

function renderPagination(total, cur, totalPages) {
    const container = document.getElementById('paginationBtns');
    if (totalPages <= 1) { container.innerHTML = ''; return; }

    let html = '';
    const btnStyle = 'padding:6px 12px;border-radius:8px;border:1px solid #e2e8f0;background:#fff;cursor:pointer;font-size:0.85rem;';
    const activeStyle = 'padding:6px 12px;border-radius:8px;border:1px solid #667eea;background:#667eea;color:#fff;cursor:pointer;font-size:0.85rem;';

    if (cur > 1) html += `<button style="${btnStyle}" onclick="goPage(${cur - 1})">‹</button>`;
    for (let p = Math.max(1, cur - 2); p <= Math.min(totalPages, cur + 2); p++) {
        html += `<button style="${p === cur ? activeStyle : btnStyle}" onclick="goPage(${p})">${p}</button>`;
    }
    if (cur < totalPages) html += `<button style="${btnStyle}" onclick="goPage(${cur + 1})">›</button>`;

    container.innerHTML = html;
}

function goPage(p) { currentPage = p; renderTable(); }

// ============================================================
// MODAL: THÊM CÂU HỎI
// ============================================================
function moModalCauHoi(existingData) {
    editingId = null;
    document.getElementById('modalCauHoiTitle').innerHTML =
        '<i class="fas fa-plus-circle" style="color:#667eea;margin-right:8px;"></i>Thêm Câu Hỏi Mới';
    resetFormCauHoi();
    moModal('modalCauHoi');
}

// ============================================================
// MODAL: SỬA CÂU HỎI
// ============================================================
async function moModalSua(cauHoiId) {
    editingId = cauHoiId;
    document.getElementById('modalCauHoiTitle').innerHTML =
        '<i class="fas fa-edit" style="color:#667eea;margin-right:8px;"></i>Chỉnh Sửa Câu Hỏi';

    const cached = allCauHoi.find(c => c.id === cauHoiId);
    if (cached) {
        await dienVaoFormCauHoi(cached);
        moModal('modalCauHoi');
    } else {
        try {
            const res = await apiGet(`${API_BASE}/${cauHoiId}`);
            if (res.success) { await dienVaoFormCauHoi(res.data); moModal('modalCauHoi'); }
            else showToast(res.message || 'Không tìm thấy câu hỏi!', 'error');
        } catch { showToast('Lỗi kết nối!', 'error'); }
    }
}

async function dienVaoFormCauHoi(c) {
    document.getElementById('editCauHoiId').value = c.id;
    document.getElementById('inputNoiDung').value = c.noiDung || '';
    document.getElementById('inputLoai').value    = c.loaiCauHoi || 'TRAC_NGHIEM';
    document.getElementById('inputDoKho').value   = c.doKho || 'DE';

    // Load chủ đề theo môn
    if (c.monHocId) {
        document.getElementById('inputMonHoc').value = c.monHocId;
        await loadChuDeModal();
        document.getElementById('inputChuDe').value = c.chuDeId || '';
    }

    // Lựa chọn
    document.getElementById('inputA').value = c.luaChonA || '';
    document.getElementById('inputB').value = c.luaChonB || '';
    document.getElementById('inputC').value = c.luaChonC || '';
    document.getElementById('inputD').value = c.luaChonD || '';

    // Đáp án
    const loai = c.loaiCauHoi;
    if (loai === 'TRAC_NGHIEM') {
        const rd = document.querySelector(`input[name="dapAnMCQ"][value="${c.dapAnDung}"]`);
        if (rd) rd.checked = true;
    } else if (loai === 'DUNG_SAI') {
        document.getElementById('inputDungSai').value = c.dapAnDung || 'Đúng';
    } else {
        document.getElementById('inputTuLuan').value = c.dapAnDung || '';
    }

    onLoaiChange();
}

function resetFormCauHoi() {
    document.getElementById('editCauHoiId').value  = '';
    document.getElementById('inputNoiDung').value  = '';
    document.getElementById('inputMonHoc').value   = '';
    document.getElementById('inputChuDe').value    = '';
    document.getElementById('inputLoai').value     = 'TRAC_NGHIEM';
    document.getElementById('inputDoKho').value    = 'DE';
    ['inputA','inputB','inputC','inputD'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('inputDungSai').value  = 'Đúng';
    document.getElementById('inputTuLuan').value   = '';
    document.getElementById('rdA').checked = true;
    ['errNoiDung','errMonHoc','errChuDe'].forEach(id => {
        const el = document.getElementById(id); if (el) el.textContent = '';
    });
    onLoaiChange();
}

// ============================================================
// LOẠI CÂU HỎI CHANGE
// ============================================================
function onLoaiChange() {
    const loai = document.getElementById('inputLoai').value;
    document.getElementById('mcqSection').style.display     = loai === 'TRAC_NGHIEM' ? 'block' : 'none';
    document.getElementById('dungSaiSection').style.display = loai === 'DUNG_SAI'    ? 'block' : 'none';
    document.getElementById('tuLuanSection').style.display  = loai === 'TU_LUAN'     ? 'block' : 'none';
}

// ============================================================
// LOAD CHỦ ĐỀ TRONG MODAL
// ============================================================
async function loadChuDeModal() {
    const monHocId = document.getElementById('inputMonHoc').value;
    await taiChuDe(monHocId, 'inputChuDe');
}

// ============================================================
// LƯU CÂU HỎI
// ============================================================
async function luuCauHoi() {
    if (!validateFormCauHoi()) return;

    const loai = document.getElementById('inputLoai').value;
    let dapAnDung;
    if (loai === 'TRAC_NGHIEM') {
        const rd = document.querySelector('input[name="dapAnMCQ"]:checked');
        dapAnDung = rd ? rd.value : 'A';
    } else if (loai === 'DUNG_SAI') {
        dapAnDung = document.getElementById('inputDungSai').value;
    } else {
        dapAnDung = document.getElementById('inputTuLuan').value.trim();
    }

    const payload = {
        noiDung:   document.getElementById('inputNoiDung').value.trim(),
        chuDeId:   document.getElementById('inputChuDe').value,
        loaiCauHoi: loai,
        doKho:     document.getElementById('inputDoKho').value,
        dapAnDung,
        luaChonA:  document.getElementById('inputA').value.trim() || null,
        luaChonB:  document.getElementById('inputB').value.trim() || null,
        luaChonC:  document.getElementById('inputC').value.trim() || null,
        luaChonD:  document.getElementById('inputD').value.trim() || null,
    };

    try {
        let res;
        if (editingId) {
            res = await apiPut(`${API_BASE}/${editingId}`, payload);
        } else {
            res = await apiPost(API_BASE, payload);
        }

        if (res.success) {
            showToast(res.message || 'Lưu câu hỏi thành công!', 'success');
            dongModal('modalCauHoi');
            await taiDanhSachCauHoi();
        } else {
            showToast(res.message || 'Lưu thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
}

function validateFormCauHoi() {
    let valid = true;
    document.getElementById('errNoiDung').textContent = '';
    document.getElementById('errMonHoc').textContent  = '';
    document.getElementById('errChuDe').textContent   = '';

    if (!document.getElementById('inputNoiDung').value.trim()) {
        document.getElementById('errNoiDung').textContent = 'Nội dung không được để trống'; valid = false;
    }
    if (!document.getElementById('inputMonHoc').value) {
        document.getElementById('errMonHoc').textContent = 'Vui lòng chọn môn học'; valid = false;
    }
    if (!document.getElementById('inputChuDe').value) {
        document.getElementById('errChuDe').textContent = 'Vui lòng chọn chủ đề'; valid = false;
    }
    return valid;
}

// ============================================================
// XÓA CÂU HỎI
// ============================================================
function moConfirmXoa(id) {
    pendingDeleteId = id;
    moModal('modalConfirmXoa');
}

async function xacNhanXoa() {
    if (!pendingDeleteId) return;
    try {
        const res = await apiDelete(`${API_BASE}/${pendingDeleteId}`);
        if (res.success) {
            showToast('Xóa câu hỏi thành công!', 'success');
            dongModal('modalConfirmXoa');
            await taiDanhSachCauHoi();
        } else {
            showToast(res.message || 'Xóa thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
    pendingDeleteId = null;
}

// ============================================================
// QUẢN LÝ CHỦ ĐỀ
// ============================================================
function moModalChuDe() {
    populateSelectFromAllMonHoc(['newChuDeMonHoc', 'chuDeViewMonHoc']);
    moModal('modalChuDe');
}

function populateSelectFromAllMonHoc(ids) {
    ids.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        const first = el.options[0];
        el.innerHTML = '';
        el.appendChild(first);
        allMonHoc.forEach(mh => el.appendChild(new Option(mh.ten, mh.id)));
    });
}

async function loadChuDeList() {
    const monHocId = document.getElementById('chuDeViewMonHoc').value
                  || document.getElementById('newChuDeMonHoc').value;
    const list = document.getElementById('chuDeList');

    if (!monHocId) {
        list.innerHTML = '<span style="color:#a0aec0;font-size:0.85rem;">Chọn môn học để xem chủ đề...</span>';
        return;
    }

    try {
        const res = await apiGet(`${API_BASE}/chu-de?monHocId=${encodeURIComponent(monHocId)}`);
        if (res.success && res.data.length > 0) {
            list.innerHTML = res.data.map(cd => `
                <span class="topic-chip"><i class="fas fa-tag"></i>${escHtml(cd.ten)}</span>
            `).join('');
        } else {
            list.innerHTML = '<span style="color:#a0aec0;font-size:0.85rem;">Chưa có chủ đề nào.</span>';
        }
    } catch { list.innerHTML = '<span style="color:#f56565;">Lỗi tải dữ liệu.</span>'; }
}

async function taoChuDe() {
    const ten      = document.getElementById('newChuDeTen').value.trim();
    const monHocId = document.getElementById('newChuDeMonHoc').value;

    if (!ten || !monHocId) { showToast('Vui lòng nhập đủ thông tin!', 'error'); return; }

    try {
        const res = await apiPost(`${API_BASE}/chu-de`, { ten, monHocId });
        if (res.success) {
            showToast('Tạo chủ đề thành công!', 'success');
            document.getElementById('newChuDeTen').value = '';
            await loadChuDeList();
        } else {
            showToast(res.message || 'Tạo thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
}

// ============================================================
// TẠO CHỦ ĐỀ NHANH (từ modal câu hỏi)
// ============================================================
function moModalTaoChuDeNhanh() {
    // Tự động điền môn đang chọn
    const monHocId = document.getElementById('inputMonHoc').value;
    populateSelectFromAllMonHoc(['nhanh_monHoc']);
    document.getElementById('nhanh_monHoc').value = monHocId;
    document.getElementById('nhanh_ten').value = '';
    moModal('modalTaoChuDeNhanh');
}

async function luuChuDeNhanh() {
    const ten      = document.getElementById('nhanh_ten').value.trim();
    const monHocId = document.getElementById('nhanh_monHoc').value;

    if (!ten || !monHocId) { showToast('Vui lòng nhập đủ thông tin!', 'error'); return; }

    try {
        const res = await apiPost(`${API_BASE}/chu-de`, { ten, monHocId });
        if (res.success) {
            showToast('Tạo chủ đề thành công!', 'success');
            dongModal('modalTaoChuDeNhanh');
            // Reload chu-de select trong modal câu hỏi
            document.getElementById('inputMonHoc').value = monHocId;
            await loadChuDeModal();
            document.getElementById('inputChuDe').value = res.data.id;
        } else {
            showToast(res.message || 'Tạo thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
}

// ============================================================
// MODAL HELPERS
// ============================================================
function moModal(id)   { document.getElementById(id).classList.add('active'); }
function dongModal(id) { document.getElementById(id).classList.remove('active'); }

document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('active');
});

// ============================================================
// TOAST
// ============================================================
function showToast(message, type = 'info', duration = 3500) {
    const container = document.getElementById('toastContainer');
    const icons = { success: 'fas fa-check-circle', error: 'fas fa-times-circle', info: 'fas fa-info-circle' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<i class="${icons[type] || icons.info}"></i> ${escHtml(message)}`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0'; toast.style.transform = 'translateX(40px)';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// ============================================================
// UTILITIES
// ============================================================
function escHtml(str) {
    return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function truncate(str, maxLen) {
    return str.length > maxLen ? str.slice(0, maxLen) + '…' : str;
}

function badgeDoKho(doKho) {
    const map = { DE: ['badge-de','😊 Dễ'], TRUNG_BINH: ['badge-tb','😐 T.Bình'], KHO: ['badge-kho','😤 Khó'] };
    const [cls, label] = map[doKho] || ['badge-de','—'];
    return `<span class="badge ${cls}">${label}</span>`;
}

function badgeLoai(loai) {
    const map = {
        TRAC_NGHIEM: ['badge-tracnghiem','Trắc nghiệm'],
        DUNG_SAI:    ['badge-dungsai','Đúng/Sai'],
        TU_LUAN:     ['','Tự luận']
    };
    const [cls, label] = map[loai] || ['','—'];
    return cls ? `<span class="badge ${cls}">${label}</span>` : label;
}

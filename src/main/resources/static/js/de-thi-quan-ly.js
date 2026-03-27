/**
 * de-thi-quan-ly.js — JavaScript cho trang Quản Lý Đề Thi
 *
 * Chức năng:
 *  - Tải danh sách đề thi (đang hoạt động + thùng rác)
 *  - Tạo / Sửa đề thi qua modal
 *  - Xóa mềm / Khôi phục / Xóa hẳn
 *  - Import đề thi từ file PDF / DOCX
 *  - Toast notification cho mọi thao tác
 */

// ============================================================
// CONSTANTS & STATE
// ============================================================
const API_BASE = '/api/giao-vien/de-thi';
const storage  = localStorage.getItem('token') ? localStorage : sessionStorage;

let danhSachDeThi  = [];   // Active exams (server data)
let danhSachTrash  = [];   // Trashed exams (server data)
let filteredList   = [];   // After client-side filter
let currentTab     = 'active';
let editingDeThiId = null; // ID đang sửa (null = tạo mới)
let pendingDeleteId = null;
let importedQuestions = []; // Câu hỏi từ file import

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
    setupSidebar();
    setupLogout();
    khoiTaoDanhSachMonHoc();
    taiDanhSachDeThi();
    hienThiTenNguoiDung();

    // Import drag-drop
    const dropZone = document.getElementById('importDropZone');
    if (dropZone) {
        dropZone.addEventListener('dragover',  e => { e.preventDefault(); dropZone.classList.add('drag-over'); });
        dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
        dropZone.addEventListener('drop', e => {
            e.preventDefault(); dropZone.classList.remove('drag-over');
            const file = e.dataTransfer.files[0];
            if (file) xuLyFileImport(file);
        });
    }

    // Nút toolbar
    document.getElementById('btnTaoDeThi').addEventListener('click', () => moModalTao());
    document.getElementById('btnImport').addEventListener('click', () => moModalImport());
});

// ============================================================
// SIDEBAR & LOGOUT
// ============================================================
function setupSidebar() {
    const sidebar  = document.getElementById('sidebar');
    const toggle   = document.getElementById('sidebarToggle');
    const close    = document.getElementById('sidebarClose');
    if (toggle) toggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    if (close)  close.addEventListener('click',  () => sidebar.classList.remove('active'));
}

function setupLogout() {
    document.getElementById('btnLogout')?.addEventListener('click', () => {
        localStorage.clear(); sessionStorage.clear();
        window.location.href = '/login';
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
    const token  = getToken();
    const vaiTro = storage.getItem('vaiTro');
    if (!token || vaiTro !== 'GIAO_VIEN' || isTokenExpired()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// ============================================================
// API CALLS
// ============================================================
async function apiGet(url) {
    const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + getToken() } });
    return res.json();
}

async function apiPost(url, body) {
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + getToken(), 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiPut(url, body) {
    const res = await fetch(url, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + getToken(), 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiDelete(url) {
    const res = await fetch(url, { method: 'DELETE', headers: { 'Authorization': 'Bearer ' + getToken() } });
    return res.json();
}

async function apiPatch(url) {
    const res = await fetch(url, { method: 'PATCH', headers: { 'Authorization': 'Bearer ' + getToken() } });
    return res.json();
}

// ============================================================
// LOAD DATA
// ============================================================
async function taiDanhSachDeThi() {
    if (!kiemTraXacThuc()) return;
    try {
        const [resActive, resTrash] = await Promise.all([
            apiGet(API_BASE),
            apiGet(API_BASE + '/thung-rac')
        ]);

        danhSachDeThi = resActive.success  ? (resActive.data  || []) : [];
        danhSachTrash = resTrash.success   ? (resTrash.data   || []) : [];

        capNhatThongKe();
        renderTable();
    } catch (err) {
        showToast('Lỗi kết nối server!', 'error');
        console.error(err);
    }
}

async function khoiTaoDanhSachMonHoc() {
    if (!kiemTraXacThuc()) return;
    try {
        const res = await apiGet(API_BASE + '/mon-hoc');
        if (!res.success) return;
        const monHocList = res.data || [];

        // Populate filter dropdown
        const filterEl = document.getElementById('filterMonHoc');
        monHocList.forEach(mh => {
            const opt = new Option(mh.ten, mh.id);
            filterEl.appendChild(opt.cloneNode(true));
        });

        // Populate form dropdown (same list)
        const formEl = document.getElementById('inputMonHoc');
        monHocList.forEach(mh => {
            const opt = new Option(mh.ten, mh.id);
            formEl.appendChild(opt);
        });
    } catch (err) { console.error('Lỗi tải môn học:', err); }
}

// ============================================================
// STATS
// ============================================================
function capNhatThongKe() {
    document.getElementById('statTong').textContent      = danhSachDeThi.length;
    document.getElementById('statNhap').textContent      = danhSachDeThi.filter(d => d.trangThai === 'NHAP').length;
    document.getElementById('statCongKhai').textContent  = danhSachDeThi.filter(d => d.trangThai === 'CONG_KHAI').length;
    document.getElementById('statDaXoa').textContent     = danhSachTrash.length;
}

// ============================================================
// TAB SWITCHING
// ============================================================
function chuyenTab(tab) {
    currentTab = tab;
    document.getElementById('tabActive').classList.toggle('active', tab === 'active');
    document.getElementById('tabTrash').classList.toggle('active',  tab === 'trash');
    document.getElementById('tableTitle').innerHTML = tab === 'active'
        ? '<i class="fas fa-list"></i> Danh Sách Đề Thi'
        : '<i class="fas fa-trash-alt"></i> Thùng Rác';
    renderTable();
}

// ============================================================
// FILTER & RENDER TABLE
// ============================================================
function applyFilter() {
    const monHoc   = document.getElementById('filterMonHoc').value;
    const trangThai = document.getElementById('filterTrangThai')?.value || '';
    const keyword  = (document.getElementById('searchInput').value || '').toLowerCase();
    const source   = currentTab === 'active' ? danhSachDeThi : danhSachTrash;

    filteredList = source.filter(d => {
        const matchMon = !monHoc    || d.monHocId === monHoc;
        const matchTT  = !trangThai || d.trangThai === trangThai;
        const matchKW  = !keyword   || (d.tenDeThi || '').toLowerCase().includes(keyword);
        return matchMon && matchTT && matchKW;
    });

    renderTable();
}

function renderTable() {
    const source = currentTab === 'active' ? danhSachDeThi : danhSachTrash;
    // Re-apply filter each time
    applyFilterInternal(source);

    const tbody = document.getElementById('deThiBody');
    if (filteredList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:40px;color:#a0aec0;">
            <i class="fas fa-inbox" style="font-size:2rem;display:block;margin-bottom:10px;"></i>
            Không có đề thi nào.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = filteredList.map((d, i) => `
        <tr>
            <td>${i + 1}</td>
            <td>
                <div style="font-weight:500;color:#2d3748;">${escHtml(d.tenDeThi || '')}</div>
                <div style="font-size:0.78rem;color:#718096;">${escHtml(d.maDeThi || '')}</div>
            </td>
            <td>${escHtml(d.tenMonHoc || '')}</td>
            <td>${d.thoiGianPhut || 0} phút</td>
            <td>${d.soCauHoi || 0}</td>
            <td>${d.soLuotThi || 0}</td>
            <td>${badgeTrangThai(d.trangThai, d.daBiXoa)}</td>
            <td style="font-size:0.8rem;color:#718096;">${formatDate(d.thoiGianTao)}</td>
            <td>${renderActions(d)}</td>
        </tr>
    `).join('');
}

function applyFilterInternal(source) {
    const monHoc    = document.getElementById('filterMonHoc').value;
    const trangThai = document.getElementById('filterTrangThai')?.value || '';
    const keyword   = (document.getElementById('searchInput').value || '').toLowerCase();

    filteredList = source.filter(d => {
        const matchMon = !monHoc    || d.monHocId === monHoc;
        const matchTT  = !trangThai || d.trangThai === trangThai;
        const matchKW  = !keyword   || (d.tenDeThi || '').toLowerCase().includes(keyword);
        return matchMon && matchTT && matchKW;
    });
}

function renderActions(d) {
    if (currentTab === 'trash') {
        return `
            <button class="btn-icon btn-icon-restore" title="Khôi phục" onclick="khoiPhuc('${d.id}')">
                <i class="fas fa-undo"></i>
            </button>
            <button class="btn-icon btn-icon-hard" title="Xóa hẳn" onclick="moConfirmXoaHan('${d.id}','${escHtml(d.tenDeThi)}')">
                <i class="fas fa-times-circle"></i>
            </button>`;
    }
    return `
        <button class="btn-icon btn-icon-edit" title="Chỉnh sửa" onclick="moModalSua('${d.id}')">
            <i class="fas fa-edit"></i>
        </button>
        <button class="btn-icon btn-icon-delete" title="Xóa mềm" onclick="moConfirmXoa('${d.id}','${escHtml(d.tenDeThi)}')">
            <i class="fas fa-trash-alt"></i>
        </button>`;
}

function badgeTrangThai(trangThai, daBiXoa) {
    if (daBiXoa) return '<span class="badge badge-deleted">Đã xóa</span>';
    if (trangThai === 'CONG_KHAI') return '<span class="badge badge-public">🌐 Công khai</span>';
    return '<span class="badge badge-draft">📝 Nháp</span>';
}

// ============================================================
// MODAL: TẠO ĐỀ THI
// ============================================================
function moModalTao() {
    editingDeThiId = null;
    document.getElementById('modalDeThiTitle').innerHTML =
        '<i class="fas fa-plus-circle" style="color:#667eea;margin-right:8px;"></i>Tạo Đề Thi Mới';
    resetFormDeThi();
    moModal('modalDeThi');
}

// ============================================================
// MODAL: SỬA ĐỀ THI
// ============================================================
async function moModalSua(deThiId) {
    editingDeThiId = deThiId;
    document.getElementById('modalDeThiTitle').innerHTML =
        '<i class="fas fa-edit" style="color:#667eea;margin-right:8px;"></i>Chỉnh Sửa Đề Thi';

    // Tìm từ cache local
    const d = danhSachDeThi.find(x => x.id === deThiId);
    if (d) {
        dienvaoFormDeThi(d);
        moModal('modalDeThi');
    } else {
        try {
            const res = await apiGet(`${API_BASE}/${deThiId}`);
            if (res.success) { dienvaoFormDeThi(res.data); moModal('modalDeThi'); }
            else showToast(res.message || 'Không tìm thấy đề thi!', 'error');
        } catch { showToast('Lỗi kết nối!', 'error'); }
    }
}

function dienvaoFormDeThi(d) {
    document.getElementById('editDeThiId').value     = d.id;
    document.getElementById('inputTenDeThi').value   = d.tenDeThi || '';
    document.getElementById('inputMonHoc').value     = d.monHocId || '';
    document.getElementById('inputThoiGian').value   = d.thoiGianPhut || '';
    document.getElementById('inputMoTa').value       = d.moTa || '';
    document.getElementById('inputTrangThai').value  = d.trangThai || 'NHAP';
    document.getElementById('inputSoLan').value      = d.soLanThiToiDa || '';
    document.getElementById('inputThoiGianMo').value  = localDateTimeStr(d.thoiGianMo);
    document.getElementById('inputThoiGianDong').value = localDateTimeStr(d.thoiGianDong);
    document.getElementById('inputTronCauHoi').checked = !!d.tronCauHoi;
    document.getElementById('inputTronDapAn').checked  = !!d.tronDapAn;
}

function resetFormDeThi() {
    ['inputTenDeThi','inputThoiGian','inputMoTa','inputSoLan','inputThoiGianMo','inputThoiGianDong']
        .forEach(id => document.getElementById(id).value = '');
    document.getElementById('inputMonHoc').value    = '';
    document.getElementById('inputTrangThai').value = 'NHAP';
    document.getElementById('inputTronCauHoi').checked = false;
    document.getElementById('inputTronDapAn').checked  = false;
    clearErrors();
}

function clearErrors() {
    ['errTenDeThi','errMonHoc','errThoiGian'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = '';
    });
}

// ============================================================
// LƯU ĐỀ THI (TẠO hoặc SỬA)
// ============================================================
async function luuDeThi() {
    if (!validateFormDeThi()) return;

    const payload = {
        tenDeThi:     document.getElementById('inputTenDeThi').value.trim(),
        monHocId:     document.getElementById('inputMonHoc').value,
        thoiGianPhut: parseInt(document.getElementById('inputThoiGian').value),
        moTa:         document.getElementById('inputMoTa').value.trim(),
        trangThai:    document.getElementById('inputTrangThai').value,
        soLanThiToiDa: parseInt(document.getElementById('inputSoLan').value) || null,
        thoiGianMo:   document.getElementById('inputThoiGianMo').value  || null,
        thoiGianDong: document.getElementById('inputThoiGianDong').value || null,
        tronCauHoi:   document.getElementById('inputTronCauHoi').checked,
        tronDapAn:    document.getElementById('inputTronDapAn').checked
    };

    const btn = document.getElementById('btnLuuDeThi');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';

    try {
        let res;
        if (editingDeThiId) {
            res = await apiPut(`${API_BASE}/${editingDeThiId}`, payload);
        } else {
            res = await apiPost(API_BASE, payload);
        }

        if (res.success) {
            showToast(res.message || 'Lưu đề thi thành công!', 'success');
            dongModal('modalDeThi');
            await taiDanhSachDeThi();
        } else {
            showToast(res.message || 'Lưu đề thi thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối server!', 'error'); }
    finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-save"></i> Lưu Đề Thi';
    }
}

function validateFormDeThi() {
    let valid = true;
    clearErrors();

    const ten = document.getElementById('inputTenDeThi').value.trim();
    if (!ten) { document.getElementById('errTenDeThi').textContent = 'Tên đề thi không được để trống'; valid = false; }

    const monHoc = document.getElementById('inputMonHoc').value;
    if (!monHoc) { document.getElementById('errMonHoc').textContent = 'Vui lòng chọn môn học'; valid = false; }

    const thoiGian = parseInt(document.getElementById('inputThoiGian').value);
    if (!thoiGian || thoiGian < 1) { document.getElementById('errThoiGian').textContent = 'Thời gian phải ít nhất 1 phút'; valid = false; }

    return valid;
}

// ============================================================
// XÓA MỀM
// ============================================================
function moConfirmXoa(id, ten) {
    pendingDeleteId = id;
    document.getElementById('confirmXoaTen').textContent = ten;
    moModal('modalConfirmXoa');
}

async function xacNhanXoaMem() {
    if (!pendingDeleteId) return;
    try {
        const res = await apiDelete(`${API_BASE}/${pendingDeleteId}/soft`);
        if (res.success) {
            showToast(res.message || 'Đã chuyển vào thùng rác!', 'success');
            dongModal('modalConfirmXoa');
            await taiDanhSachDeThi();
        } else {
            showToast(res.message || 'Xóa thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
    pendingDeleteId = null;
}

// ============================================================
// KHÔI PHỤC
// ============================================================
async function khoiPhuc(id) {
    try {
        const res = await apiPatch(`${API_BASE}/${id}/restore`);
        if (res.success) {
            showToast('Khôi phục đề thi thành công!', 'success');
            await taiDanhSachDeThi();
        } else {
            showToast(res.message || 'Khôi phục thất bại!', 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
}

// ============================================================
// XÓA HẲN
// ============================================================
function moConfirmXoaHan(id, ten) {
    pendingDeleteId = id;
    document.getElementById('confirmXoaHanTen').textContent = ten;
    moModal('modalConfirmXoaHan');
}

async function xacNhanXoaHan() {
    if (!pendingDeleteId) return;
    try {
        const res = await apiDelete(`${API_BASE}/${pendingDeleteId}/hard`);
        if (res.success) {
            showToast('Đã xóa hẳn đề thi!', 'success');
            dongModal('modalConfirmXoaHan');
            await taiDanhSachDeThi();
        } else {
            showToast(res.message || 'Không thể xóa: ' + res.message, 'error');
        }
    } catch { showToast('Lỗi kết nối!', 'error'); }
    pendingDeleteId = null;
}

// ============================================================
// IMPORT FILE
// ============================================================
function moModalImport() {
    document.getElementById('importPreview').style.display   = 'none';
    document.getElementById('importLoading').style.display   = 'none';
    document.getElementById('btnLuuImport').style.display    = 'none';
    document.getElementById('importDropZone').style.display  = 'block';
    importedQuestions = [];
    moModal('modalImport');
}

function handleImportFile(input) {
    const file = input.files[0];
    if (file) xuLyFileImport(file);
}

async function xuLyFileImport(file) {
    const allowedExts = ['.pdf', '.docx', '.doc'];
    const ext = file.name.toLowerCase().slice(file.name.lastIndexOf('.'));
    if (!allowedExts.includes(ext)) {
        showToast('Chỉ hỗ trợ file PDF và DOCX!', 'error'); return;
    }

    document.getElementById('importDropZone').style.display  = 'none';
    document.getElementById('importLoading').style.display   = 'block';
    document.getElementById('importPreview').style.display   = 'none';

    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await fetch(`${API_BASE}/import`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + getToken() },
            body: formData
        });
        const data = await res.json();

        document.getElementById('importLoading').style.display = 'none';

        if (data.success && data.data) {
            const ketQua = data.data;
            importedQuestions = ketQua.cauHoiList || [];
            hienThiPreviewImport(ketQua);
        } else {
            showToast(data.message || 'Phân tích file thất bại!', 'error');
            document.getElementById('importDropZone').style.display = 'block';
        }
    } catch (err) {
        document.getElementById('importLoading').style.display  = 'none';
        document.getElementById('importDropZone').style.display = 'block';
        showToast('Lỗi kết nối server!', 'error');
        console.error(err);
    }
}

function hienThiPreviewImport(ketQua) {
    const previewDiv  = document.getElementById('importPreview');
    const previewList = document.getElementById('importPreviewList');
    const titleEl     = document.getElementById('importPreviewTitle');
    const countEl     = document.getElementById('importPreviewCount');

    titleEl.textContent  = ketQua.message || '';
    countEl.textContent  = ketQua.tongSoCauHoi || 0;

    previewList.innerHTML = (ketQua.cauHoiList || []).map(ch => `
        <div class="import-preview-item">
            <h4>Câu ${ch.stt}: ${escHtml(ch.noiDung || '')}</h4>
            ${ch.luaChonA ? `<div class="import-option-row"><span class="option-label">A.</span><span>${escHtml(ch.luaChonA)}</span></div>` : ''}
            ${ch.luaChonB ? `<div class="import-option-row"><span class="option-label">B.</span><span>${escHtml(ch.luaChonB)}</span></div>` : ''}
            ${ch.luaChonC ? `<div class="import-option-row"><span class="option-label">C.</span><span>${escHtml(ch.luaChonC)}</span></div>` : ''}
            ${ch.luaChonD ? `<div class="import-option-row"><span class="option-label">D.</span><span>${escHtml(ch.luaChonD)}</span></div>` : ''}
            ${ch.dapAnDung ? `<div style="margin-top:6px;font-size:0.82rem;"><span class="correct-answer">✓ Đáp án: ${escHtml(ch.dapAnDung)}</span></div>` : ''}
        </div>
    `).join('');

    previewDiv.style.display = 'block';
    document.getElementById('btnLuuImport').style.display = ketQua.tongSoCauHoi > 0 ? 'inline-flex' : 'none';
}

// Mở modal cấu hình lưu → thay vì redirect, cho user chọn môn/chủ đề rồi lưu thực sự
async function luuCauHoiImport() {
    if (importedQuestions.length === 0) {
        showToast('Không có câu hỏi nào để lưu!', 'error');
        return;
    }

    // Hiện số lượng câu sẽ lưu
    document.getElementById('importSummaryCount').textContent = importedQuestions.length;

    // Reset form + progress
    document.getElementById('importMonHoc').value = '';
    document.getElementById('importChuDe').value  = '';
    document.getElementById('importChuDe').disabled = true;
    document.getElementById('importDoKho').value  = 'TRUNG_BINH';
    document.getElementById('importAutoFillAnswer').checked = true;
    document.getElementById('errImportMonHoc').textContent  = '';
    document.getElementById('errImportChuDe').textContent   = '';
    document.getElementById('importProgressWrap').style.display = 'none';
    document.getElementById('importProgressResult').innerHTML   = '';
    document.getElementById('btnXacNhanLuuImport').disabled = false;
    document.getElementById('btnXacNhanLuuImport').innerHTML = '<i class="fas fa-save"></i> Lưu Câu Hỏi';
    document.getElementById('btnHuyLuuImport').disabled = false;

    // Nạp danh sách môn học vào select (dùng API ngân hàng câu hỏi)
    await taiMonHocChoImport();

    moModal('modalLuuImport');
}

async function taiMonHocChoImport() {
    try {
        const res = await apiGet('/api/giao-vien/ngan-hang-cau-hoi/mon-hoc');
        if (!res.success) return;
        const sel = document.getElementById('importMonHoc');
        // Giữ placeholder đầu tiên
        sel.innerHTML = '<option value="">-- Chọn môn học --</option>';
        (res.data || []).forEach(mh => sel.appendChild(new Option(mh.ten, mh.id)));
    } catch (err) { console.error('Lỗi tải môn học:', err); }
}

async function onImportMonHocChange() {
    const monHocId = document.getElementById('importMonHoc').value;
    const chuDeEl  = document.getElementById('importChuDe');
    document.getElementById('errImportMonHoc').textContent = '';

    if (!monHocId) {
        chuDeEl.innerHTML  = '<option value="">-- Chọn chủ đề --</option>';
        chuDeEl.disabled   = true;
        return;
    }

    try {
        const res = await apiGet(`/api/giao-vien/ngan-hang-cau-hoi/chu-de?monHocId=${encodeURIComponent(monHocId)}`);
        chuDeEl.innerHTML  = '<option value="">-- Chọn chủ đề --</option>';
        chuDeEl.disabled   = false;
        if (res.success) {
            (res.data || []).forEach(cd => chuDeEl.appendChild(new Option(cd.ten, cd.id)));
        }
    } catch (err) {
        chuDeEl.disabled = false;
        console.error('Lỗi tải chủ đề:', err);
    }
}

async function xacNhanLuuImport() {
    // Validate
    const chuDeId  = document.getElementById('importChuDe').value;
    const monHocId = document.getElementById('importMonHoc').value;
    const doKho    = document.getElementById('importDoKho').value;
    const autoFill = document.getElementById('importAutoFillAnswer').checked;

    document.getElementById('errImportMonHoc').textContent = '';
    document.getElementById('errImportChuDe').textContent  = '';

    if (!monHocId) {
        document.getElementById('errImportMonHoc').textContent = 'Vui lòng chọn môn học';
        return;
    }
    if (!chuDeId) {
        document.getElementById('errImportChuDe').textContent = 'Vui lòng chọn chủ đề';
        return;
    }

    const total = importedQuestions.length;

    // Khoá nút, hiện progress
    const btnLuu = document.getElementById('btnXacNhanLuuImport');
    const btnHuy = document.getElementById('btnHuyLuuImport');
    btnLuu.disabled = true;
    btnHuy.disabled = true;
    btnLuu.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';

    const progressWrap = document.getElementById('importProgressWrap');
    const progressBar  = document.getElementById('importProgressBar');
    const progressText = document.getElementById('importProgressText');
    const progressResult = document.getElementById('importProgressResult');
    progressWrap.style.display = 'block';
    progressResult.innerHTML   = '';

    let saved = 0;
    let failed = 0;
    const errors = [];

    for (let i = 0; i < total; i++) {
        const ch = importedQuestions[i];

        // Đảm bảo đáp án hợp lệ
        let dapAn = (ch.dapAnDung || '').toUpperCase();
        if (!['A','B','C','D'].includes(dapAn)) {
            dapAn = autoFill ? 'A' : null;
        }

        if (!dapAn) {
            failed++;
            errors.push(`Câu ${ch.stt}: thiếu đáp án — bỏ qua`);
            continue;
        }

        const payload = {
            noiDung:    ch.noiDung || `Câu hỏi ${ch.stt}`,
            chuDeId:    chuDeId,
            loaiCauHoi: 'TRAC_NGHIEM',
            doKho:      doKho,
            dapAnDung:  dapAn,
            luaChonA:   ch.luaChonA || null,
            luaChonB:   ch.luaChonB || null,
            luaChonC:   ch.luaChonC || null,
            luaChonD:   ch.luaChonD || null,
        };

        try {
            const res = await apiPost('/api/giao-vien/ngan-hang-cau-hoi', payload);
            if (res.success) {
                saved++;
            } else {
                failed++;
                errors.push(`Câu ${ch.stt}: ${res.message || 'Lỗi không xác định'}`);
            }
        } catch (e) {
            failed++;
            errors.push(`Câu ${ch.stt}: Lỗi kết nối`);
        }

        // Cập nhật progress
        const pct = Math.round(((i + 1) / total) * 100);
        progressBar.style.width  = pct + '%';
        progressText.textContent = `${i + 1} / ${total}`;
    }

    // Hiện kết quả
    let resultHtml = '';
    if (saved > 0) {
        resultHtml += `<div style="color:#276749;font-weight:600;"><i class="fas fa-check-circle"></i> Đã lưu thành công: ${saved} câu hỏi</div>`;
    }
    if (failed > 0) {
        resultHtml += `<div style="color:#9b2c2c;font-weight:600;margin-top:6px;"><i class="fas fa-times-circle"></i> Lỗi: ${failed} câu hỏi</div>`;
        if (errors.length > 0) {
            resultHtml += `<ul style="margin:6px 0 0 16px;font-size:0.8rem;color:#718096;">` +
                errors.map(e => `<li>${escHtml(e)}</li>`).join('') + `</ul>`;
        }
    }
    progressResult.innerHTML = resultHtml;

    // Reset nút
    btnLuu.disabled = false;
    btnHuy.disabled = false;
    btnLuu.innerHTML = '<i class="fas fa-check"></i> Hoàn Tất';
    btnLuu.onclick = () => {
        dongModal('modalLuuImport');
        dongModal('modalImport');
        // Reset file input
        const fi = document.getElementById('importFileInput');
        if (fi) fi.value = '';
        importedQuestions = [];
        if (saved > 0) {
            showToast(`Đã lưu ${saved} câu hỏi vào ngân hàng thành công!`, 'success');
        }
    };
    btnHuy.onclick = () => dongModal('modalLuuImport');

    // Toast tổng kết
    if (saved === total) {
        showToast(`Lưu thành công tất cả ${saved} câu hỏi!`, 'success');
    } else if (saved > 0) {
        showToast(`Lưu ${saved}/${total} câu — ${failed} câu thất bại.`, 'info');
    } else {
        showToast('Không lưu được câu hỏi nào. Kiểm tra lại!', 'error');
    }
}

// ============================================================
// MODAL HELPERS
// ============================================================
function moModal(id)    { document.getElementById(id).classList.add('active'); }
function dongModal(id)  { document.getElementById(id).classList.remove('active'); }

// Close modal on overlay click
document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});

// ============================================================
// TOAST NOTIFICATION
// ============================================================
function showToast(message, type = 'info', duration = 3500) {
    const container = document.getElementById('toastContainer');
    const icons = { success: 'fas fa-check-circle', error: 'fas fa-times-circle', info: 'fas fa-info-circle' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<i class="${icons[type] || icons.info}"></i> ${escHtml(message)}`;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(40px)'; setTimeout(() => toast.remove(), 300); }, duration);
}

// ============================================================
// UTILITIES
// ============================================================
function escHtml(str) {
    return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    try {
        const d = new Date(dateStr);
        return `${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getFullYear()}`;
    } catch { return dateStr; }
}

function localDateTimeStr(dateStr) {
    if (!dateStr) return '';
    try {
        const d = new Date(dateStr);
        const pad = n => n.toString().padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    } catch { return ''; }
}

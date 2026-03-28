/**
 * de-thi-quan-ly.js — JavaScript cho trang Quản Lý Đề Thi
 *
 * Chức năng:
 *  - Tải danh sách đề thi (đang hoạt động + thùng rác)
 *  - Tạo / Sửa đề thi qua modal
 *  - Quản lý câu hỏi: chuyển sang trang chỉnh sửa văn bản thô (/dashboard/.../chinh-sua-cau-hoi)
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

const THOI_GIAN_PHUT_OPTIONS = [15, 30, 45, 60, 90, 120, 180];

function chonThoiGianPhutHopLe(phut) {
    const v = parseInt(phut, 10);
    return THOI_GIAN_PHUT_OPTIONS.includes(v) ? String(v) : '60';
}

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
    setupSidebar();
    setupLogout();
    khoiTaoDanhSachMonHoc();
    taiDanhSachDeThi();
    hienThiTenNguoiDung();

    document.getElementById('inputMonHoc').addEventListener('change', async () => {
        await taiChuDeChoFormDeThi();
        const preview = document.getElementById('modalImportPreview');
        if (preview && preview.style.display !== 'none' && importedQuestions.length > 0) {
            syncImportChuDeSelectFromForm();
        }
    });

    document.getElementById('inputChuDeDeThi').addEventListener('change', () => {
        const preview = document.getElementById('modalImportPreview');
        if (preview && preview.style.display !== 'none' && importedQuestions.length > 0) {
            syncImportChuDeSelectFromForm();
        }
    });

    const modalImpChuDe = document.getElementById('modalImportChuDe');
    if (modalImpChuDe) {
        modalImpChuDe.addEventListener('change', () => {
            const src = document.getElementById('inputChuDeDeThi');
            const v = modalImpChuDe.value;
            if (src && !src.disabled && v && [...src.options].some(o => o.value === v)) {
                src.value = v;
            }
        });
    }

    // Nút toolbar
    document.getElementById('btnTaoDeThi').addEventListener('click', () => moModalTao());

    // Modal drag-drop (inside #modalDeThi)
    const dropZone = document.getElementById('modalImportDropZone');
    if (dropZone) {
        dropZone.addEventListener('dragover',  e => { e.preventDefault(); dropZone.classList.add('drag-over'); });
        dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
        dropZone.addEventListener('drop', e => {
            e.preventDefault(); dropZone.classList.remove('drag-over');
            const file = e.dataTransfer.files[0];
            if (file) handleModalImportFile({ files: [file] });
        });
    }
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
        <button class="btn-icon btn-icon-question" title="Quản lý câu hỏi" type="button"
                onclick="window.location.href='/dashboard/giao-vien/de-thi/${d.id}/chinh-sua-cau-hoi'">
            <i class="fas fa-list-ol"></i>
        </button>
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
    // Hiện khu vực import trong modal
    resetModalImportSection();
    document.getElementById('importSection').style.display = '';
    moModal('modalDeThi');
}

// ============================================================
// MODAL: SỬA ĐỀ THI
// ============================================================
async function moModalSua(deThiId) {
    editingDeThiId = deThiId;
    resetModalImportSection();
    const impSec = document.getElementById('importSection');
    if (impSec) impSec.style.display = 'none';

    document.getElementById('modalDeThiTitle').innerHTML =
        '<i class="fas fa-edit" style="color:#667eea;margin-right:8px;"></i>Chỉnh Sửa Đề Thi';

    // Tìm từ cache local
    const d = danhSachDeThi.find(x => x.id === deThiId);
    if (d) {
        dienvaoFormDeThi(d);
        await taiChuDeChoFormDeThi();
        moModal('modalDeThi');
    } else {
        try {
            const res = await apiGet(`${API_BASE}/${deThiId}`);
            if (res.success) {
                dienvaoFormDeThi(res.data);
                await taiChuDeChoFormDeThi();
                moModal('modalDeThi');
            } else showToast(res.message || 'Không tìm thấy đề thi!', 'error');
        } catch { showToast('Lỗi kết nối!', 'error'); }
    }
}

function dienvaoFormDeThi(d) {
    document.getElementById('editDeThiId').value     = d.id;
    document.getElementById('inputTenDeThi').value   = d.tenDeThi || '';
    document.getElementById('inputMonHoc').value     = d.monHocId || '';
    document.getElementById('inputThoiGian').value   = chonThoiGianPhutHopLe(d.thoiGianPhut);
    document.getElementById('inputMoTa').value       = d.moTa || '';
    document.getElementById('inputTrangThai').value  = d.trangThai || 'NHAP';
    document.getElementById('inputSoLan').value      = d.soLanThiToiDa || '';
    document.getElementById('inputThoiGianMo').value  = localDateTimeStr(d.thoiGianMo);
    document.getElementById('inputThoiGianDong').value = localDateTimeStr(d.thoiGianDong);
}

function resetFormDeThi() {
    ['inputTenDeThi','inputMoTa','inputSoLan','inputThoiGianMo','inputThoiGianDong']
        .forEach(id => document.getElementById(id).value = '');
    document.getElementById('inputThoiGian').value  = '60';
    document.getElementById('inputMonHoc').value    = '';
    const cdForm = document.getElementById('inputChuDeDeThi');
    if (cdForm) {
        cdForm.innerHTML = '<option value="">-- Chọn môn học trước --</option>';
        cdForm.disabled = true;
    }
    document.getElementById('inputTrangThai').value = 'NHAP';
    clearErrors();
}

function resetModalImportSection() {
    importedQuestions = [];
    const dropZone = document.getElementById('modalImportDropZone');
    const loading  = document.getElementById('modalImportLoading');
    const preview  = document.getElementById('modalImportPreview');
    if (dropZone) dropZone.style.display = '';
    if (loading)  loading.style.display  = 'none';
    if (preview)  preview.style.display  = 'none';
    const fileInput = document.getElementById('modalImportFileInput');
    if (fileInput) fileInput.value = '';
    const chuDeSel = document.getElementById('modalImportChuDe');
    if (chuDeSel) {
        chuDeSel.innerHTML = '<option value="">-- Chọn chủ đề --</option>';
        chuDeSel.disabled = true;
    }
    const errChuDe = document.getElementById('errModalImportChuDe');
    if (errChuDe) errChuDe.textContent = '';
    document.getElementById('modalImportDoKho').value = 'TRUNG_BINH';
    document.getElementById('modalImportCauHoiList').innerHTML = '';
}

function clearErrors() {
    ['errTenDeThi','errMonHoc','errThoiGian'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = '';
    });
}

// ============================================================
// LƯU ĐỀ THI — Tạo mới hoặc Cập nhật + gắn câu import
// ============================================================
async function luuDeThi() {
    if (!validateFormDeThi()) return;

    // Câu import: môn học dùng inputMonHoc (đã kiểm tra ở validateFormDeThi); bắt buộc chọn chủ đề
    if (importedQuestions.length > 0) {
        const chuDeId = document.getElementById('modalImportChuDe').value;
        const errChuDe = document.getElementById('errModalImportChuDe');
        if (errChuDe) errChuDe.textContent = '';
        if (!chuDeId) {
            if (errChuDe) errChuDe.textContent = 'Vui lòng chọn chủ đề cho câu import';
            showToast('Vui lòng chọn chủ đề cho câu hỏi import.', 'error');
            return;
        }
    }

    const payload = {
        tenDeThi:     document.getElementById('inputTenDeThi').value.trim(),
        monHocId:     document.getElementById('inputMonHoc').value,
        thoiGianPhut: parseInt(document.getElementById('inputThoiGian').value),
        moTa:         document.getElementById('inputMoTa').value.trim(),
        trangThai:    document.getElementById('inputTrangThai').value,
        soLanThiToiDa: parseInt(document.getElementById('inputSoLan').value) || null,
        thoiGianMo:   document.getElementById('inputThoiGianMo').value  || null,
        thoiGianDong: document.getElementById('inputThoiGianDong').value || null
    };

    const btn = document.getElementById('btnLuuDeThi');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';

    try {
        let deThiId;
        let deThiCreatedData;

        if (editingDeThiId) {
            // ── SỬA: chỉ gửi PUT, không import thêm ──
            const res = await apiPut(`${API_BASE}/${editingDeThiId}`, payload);
            if (!res.success) {
                showToast(res.message || 'Lưu đề thi thất bại!', 'error');
                return;
            }
            deThiId = editingDeThiId;
            deThiCreatedData = res.data;
        } else {
            // ── TẠO MỚI ──
            const res = await apiPost(API_BASE, payload);
            if (!res.success) {
                showToast(res.message || 'Lưu đề thi thất bại!', 'error');
                return;
            }
            deThiId = res.data?.id;
            deThiCreatedData = res.data;

            // ── Lưu câu hỏi import (nếu có) ──
            if (importedQuestions.length > 0 && deThiId) {
                const chuDeId  = document.getElementById('modalImportChuDe').value;
                const doKho    = document.getElementById('modalImportDoKho').value;

                const savedIds = [];
                let failedImport = 0;

                for (const ch of importedQuestions) {
                    let dapAn = (ch.dapAnDung || '').toUpperCase();
                    if (!['A','B','C','D'].includes(dapAn)) dapAn = 'A';

                    const cauPayload = {
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
                        const r = await apiPost('/api/giao-vien/ngan-hang-cau-hoi', cauPayload);
                        if (r.success && r.data?.id) {
                            savedIds.push(r.data.id);
                        } else {
                            failedImport++;
                        }
                    } catch {
                        failedImport++;
                    }
                }

                // ── Gắn câu vào đề ──
                if (savedIds.length > 0) {
                    await apiPost(`${API_BASE}/${deThiId}/cau-hoi`, { cauHoiIds: savedIds });
                }

                if (failedImport > 0) {
                    showToast(`Đã tạo đề. Có ${failedImport} câu import thất bại — vui lòng thêm thủ công.`, 'warn');
                }
            }
        }

        showToast(editingDeThiId ? 'Cập nhật đề thi thành công!' : 'Tạo đề thi thành công!', 'success');
        dongModal('modalDeThi');
        importedQuestions = [];
        resetModalImportSection();
        await taiDanhSachDeThi();

    } catch (e) {
        console.error(e);
        showToast('Lỗi kết nối server!', 'error');
    } finally {
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
// IMPORT — XỬ LÝ TRONG MODAL TẠO ĐỀ
// ============================================================

function handleModalImportFile(input) {
    const file = input.files ? input.files[0] : input;
    if (!file) return;
    xuLyModalImport(file);
}

async function xuLyModalImport(file) {
    const allowedExts = ['.pdf', '.docx', '.doc'];
    const ext = file.name.toLowerCase().slice(file.name.lastIndexOf('.'));
    if (!allowedExts.includes(ext)) {
        showToast('Chỉ hỗ trợ file PDF và DOCX!', 'error');
        return;
    }

    document.getElementById('modalImportDropZone').style.display = 'none';
    document.getElementById('modalImportLoading').style.display = 'block';
    document.getElementById('modalImportPreview').style.display = 'none';

    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await fetch(`${API_BASE}/import`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + getToken() },
            body: formData
        });
        const data = await res.json();

        document.getElementById('modalImportLoading').style.display = 'none';

        if (data.success && data.data) {
            importedQuestions = data.data.cauHoiList || [];
            hienThiModalImportPreview(data.data);
        } else {
            showToast(data.message || 'Phân tích file thất bại!', 'error');
            document.getElementById('modalImportDropZone').style.display = '';
        }
    } catch (err) {
        document.getElementById('modalImportLoading').style.display = 'none';
        document.getElementById('modalImportDropZone').style.display = '';
        showToast('Lỗi kết nối server!', 'error');
        console.error(err);
    }
}

function hienThiModalImportPreview(ketQua) {
    const count = importedQuestions.length;
    document.getElementById('modalImportCount').textContent = count;
    document.getElementById('modalImportPreview').style.display = '';

    taiChuDeVaoModalImport().catch(err => console.error(err));

    // Preview
    const listEl = document.getElementById('modalImportCauHoiList');
    listEl.innerHTML = importedQuestions.map((ch, idx) => `
        <div style="margin-bottom:10px;padding-bottom:10px;border-bottom:1px solid #e2e8f0;${idx === importedQuestions.length - 1 ? 'border-bottom:none;' : ''}">
            <div style="font-size:0.85rem;font-weight:500;color:#2d3748;">Câu ${ch.stt || (idx + 1)}: ${escHtml(ch.noiDung || '')}</div>
            ${ch.luaChonA ? '<div style="font-size:0.78rem;color:#4a5568;padding-left:10px;">A. ' + escHtml(ch.luaChonA) + '</div>' : ''}
            ${ch.luaChonB ? '<div style="font-size:0.78rem;color:#4a5568;padding-left:10px;">B. ' + escHtml(ch.luaChonB) + '</div>' : ''}
            ${ch.luaChonC ? '<div style="font-size:0.78rem;color:#4a5568;padding-left:10px;">C. ' + escHtml(ch.luaChonC) + '</div>' : ''}
            ${ch.luaChonD ? '<div style="font-size:0.78rem;color:#4a5568;padding-left:10px;">D. ' + escHtml(ch.luaChonD) + '</div>' : ''}
            ${ch.dapAnDung ? '<div style="font-size:0.75rem;color:#38a169;margin-top:3px;">&#10003; Đáp án: ' + escHtml(ch.dapAnDung) + '</div>' : ''}
        </div>
    `).join('');
}

function huyModalImport() {
    importedQuestions = [];
    document.getElementById('modalImportPreview').style.display = 'none';
    document.getElementById('modalImportDropZone').style.display = '';
    const fi = document.getElementById('modalImportFileInput');
    if (fi) fi.value = '';
}

/** Dropdown chủ đề trên form đề thi — theo môn học đã chọn */
async function taiChuDeChoFormDeThi() {
    const monHocId = document.getElementById('inputMonHoc').value;
    const sel = document.getElementById('inputChuDeDeThi');
    if (!sel) return;

    if (!monHocId) {
        sel.innerHTML = '<option value="">-- Chọn môn học trước --</option>';
        sel.disabled = true;
        return;
    }

    const prev = sel.value;
    try {
        const res = await apiGet(`/api/giao-vien/ngan-hang-cau-hoi/chu-de?monHocId=${encodeURIComponent(monHocId)}`);
        sel.innerHTML = '<option value="">-- Chọn chủ đề --</option>';
        sel.disabled = false;
        if (res.success) {
            (res.data || []).forEach(cd => sel.appendChild(new Option(cd.ten, cd.id)));
        }
        if (prev && [...sel.options].some(o => o.value === prev)) {
            sel.value = prev;
        }
    } catch (err) {
        sel.disabled = false;
        console.error('Lỗi tải chủ đề:', err);
    }
}

/** Đồng bộ select chủ đề trong khu import với form phía trên */
function syncImportChuDeSelectFromForm() {
    const src = document.getElementById('inputChuDeDeThi');
    const dst = document.getElementById('modalImportChuDe');
    if (!src || !dst) return;
    const v = src.value;
    dst.innerHTML = src.innerHTML;
    dst.disabled = src.disabled;
    if (v && [...dst.options].some(o => o.value === v)) {
        dst.value = v;
    }
}

/** Tải chủ đề lên form rồi copy sang ô import (một nguồn dữ liệu) */
async function taiChuDeVaoModalImport() {
    await taiChuDeChoFormDeThi();
    syncImportChuDeSelectFromForm();
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

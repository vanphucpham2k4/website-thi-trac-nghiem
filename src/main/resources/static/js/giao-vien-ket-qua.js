/**
 * giao-vien-ket-qua.js — Xem kết quả chi tiết (Giáo viên)
 * Luồng 1: Lớp → Đề thi → Kết quả (lớp + ẩn danh)
 * Luồng 2: Tất cả đề thi → Kết quả toàn bộ
 */
(function () {
    'use strict';

    // ===== Constants =====
    const API_BASE = '/api/giao-vien/ket-qua';
    const PAGE_SIZE = 15;
    const COL_SPAN = 10; // 9 cột cũ + 1 cột nguồn

    // ===== Storage =====
    var storage = localStorage.getItem('token') ? localStorage : sessionStorage;
    function isTokenExpired() {
        var expiresAt = storage.getItem('tokenExpiresAt');
        if (!expiresAt) return true;
        return Date.now() > parseInt(expiresAt, 10);
    }
    function authHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + storage.getItem('token')
        };
    }

    // ===== State =====
    let allKetQua = [];
    let filteredKetQua = [];
    let currentPage = 1;
    let currentLopId = '';
    let currentLopTen = '';
    let currentDeThiId = '';
    let currentDeThiTen = '';
    let currentTab = 'lop'; // 'lop' | 'deThi'

    // ===== DOM refs =====
    const breadcrumb      = document.getElementById('breadcrumb');
    const viewLop         = document.getElementById('viewLop');
    const viewDeThi       = document.getElementById('viewDeThi');
    const viewKetQua      = document.getElementById('viewKetQua');
    const lopCardGrid     = document.getElementById('lopCardGrid');
    const deThiCardGrid   = document.getElementById('deThiCardGrid');
    const allDeThiCardGrid = document.getElementById('allDeThiCardGrid');
    const bangKetQuaBody  = document.getElementById('bangKetQuaBody');
    const paginationEl    = document.getElementById('pagination');
    const searchInput     = document.getElementById('searchInput');
    const btnExportXlsx   = document.getElementById('btnExportXlsx');
    const displayName     = document.getElementById('displayName');
    const tabLop          = document.getElementById('tabLop');
    const tabAllDeThi     = document.getElementById('tabAllDeThi');

    // ===== Init =====
    document.addEventListener('DOMContentLoaded', function () {
        var nguoiDung = storage.getItem('nguoiDung');
        var vaiTro = storage.getItem('vaiTro');
        var token = storage.getItem('token');
        if (!nguoiDung || vaiTro !== 'GIAO_VIEN' || !token) {
            window.location.href = '/login'; return;
        }
        if (isTokenExpired()) {
            storage.removeItem('nguoiDung'); storage.removeItem('vaiTro');
            storage.removeItem('token'); storage.removeItem('tokenExpiresAt');
            window.location.href = '/login?expired=1'; return;
        }

        initSidebar();
        initLogout();
        loadUserName();
        loadDanhSachLop();
        initTabs();

        searchInput.addEventListener('input', function () {
            var q = this.value.trim().toLowerCase();
            filteredKetQua = q ? allKetQua.filter(function (r) {
                return (r.mssv || '').toLowerCase().includes(q) ||
                       (r.ho || '').toLowerCase().includes(q) ||
                       (r.ten || '').toLowerCase().includes(q);
            }) : allKetQua.slice();
            currentPage = 1;
            renderBangKetQua();
        });

        btnExportXlsx.addEventListener('click', exportXlsx);
    });

    // ===== Tabs =====
    function initTabs() {
        document.querySelectorAll('#kqTabs .kq-tab').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var tab = this.dataset.tab;
                currentTab = tab;
                document.querySelectorAll('#kqTabs .kq-tab').forEach(function (b) { b.classList.remove('active'); });
                this.classList.add('active');

                if (tab === 'lop') {
                    tabLop.style.display = '';
                    tabAllDeThi.style.display = 'none';
                } else {
                    tabLop.style.display = 'none';
                    tabAllDeThi.style.display = '';
                    loadDanhSachDeThiAll();
                }
            });
        });
    }

    // ===== View switching =====
    function showView(name) {
        viewLop.classList.toggle('active', name === 'lop');
        viewDeThi.classList.toggle('active', name === 'deThi');
        viewKetQua.classList.toggle('active', name === 'ketQua');
    }

    function updateBreadcrumb(step) {
        var html = '';
        if (step === 'lop') {
            html = '<span class="current"><i class="fas fa-chart-bar"></i> Thống Kê</span>';
        } else if (step === 'deThi') {
            html = '<a href="#" id="bcLop"><i class="fas fa-chart-bar"></i> Thống Kê</a>' +
                   '<span class="sep"><i class="fas fa-chevron-right"></i></span>' +
                   '<span class="current">' + escHtml(currentLopTen) + '</span>';
        } else if (step === 'ketQua') {
            html = '<a href="#" id="bcLop"><i class="fas fa-chart-bar"></i> Thống Kê</a>' +
                   '<span class="sep"><i class="fas fa-chevron-right"></i></span>';
            if (currentLopId) {
                // Luồng 1: Lớp → Đề → Kết quả
                html += '<a href="#" id="bcDeThi">' + escHtml(currentLopTen) + '</a>' +
                        '<span class="sep"><i class="fas fa-chevron-right"></i></span>' +
                        '<span class="current">' + escHtml(currentDeThiTen) + '</span>';
            } else {
                // Luồng 2: Đề thi → Kết quả
                html += '<span class="current">' + escHtml(currentDeThiTen) + '</span>';
            }
        }
        breadcrumb.innerHTML = html;

        var bcLop = document.getElementById('bcLop');
        if (bcLop) bcLop.addEventListener('click', function (e) { e.preventDefault(); goToLop(); });
        var bcDeThi = document.getElementById('bcDeThi');
        if (bcDeThi) bcDeThi.addEventListener('click', function (e) { e.preventDefault(); goToDeThi(); });
    }

    function goToLop() { showView('lop'); updateBreadcrumb('lop'); }
    function goToDeThi() { showView('deThi'); updateBreadcrumb('deThi'); }

    // ===== Luồng 1 — Bước 1: Danh sách lớp =====
    function loadDanhSachLop() {
        lopCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-spinner fa-spin"></i><br>Đang tải…</div>';
        fetchApi(API_BASE + '/lop', 'GET').then(function (res) {
            if (!res || !res.success || !res.data || res.data.length === 0) {
                lopCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-inbox"></i><br>Chưa có lớp học nào.</div>';
                return;
            }
            var html = '';
            res.data.forEach(function (lop) {
                html += '<div class="kq-card" data-lop-id="' + lop.id + '" data-lop-ten="' + escAttr(lop.tenLop) + '">' +
                        '<h4><i class="fas fa-school"></i> ' + escHtml(lop.tenLop) + '</h4>' +
                        '<div class="kq-card-stats">' +
                        '<span><i class="fas fa-user-graduate"></i> ' + lop.soSinhVien + ' SV</span>' +
                        '<span><i class="fas fa-file-alt"></i> ' + lop.soDeThiXuatBan + ' đề</span>' +
                        '</div></div>';
            });
            lopCardGrid.innerHTML = html;
            lopCardGrid.querySelectorAll('.kq-card').forEach(function (card) {
                card.addEventListener('click', function () {
                    currentLopId = this.dataset.lopId;
                    currentLopTen = this.dataset.lopTen;
                    loadDanhSachDeThi(currentLopId);
                });
            });
        }).catch(function () {
            lopCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Lỗi tải dữ liệu.</div>';
        });
    }

    // ===== Luồng 1 — Bước 2: Đề thi trong lớp =====
    function loadDanhSachDeThi(lopId) {
        showView('deThi');
        updateBreadcrumb('deThi');
        deThiCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-spinner fa-spin"></i><br>Đang tải…</div>';
        fetchApi(API_BASE + '/lop/' + lopId + '/de-thi', 'GET').then(function (res) {
            if (!res || !res.success || !res.data || res.data.length === 0) {
                deThiCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-inbox"></i><br>Chưa có đề thi nào xuất bản cho lớp này.</div>';
                return;
            }
            deThiCardGrid.innerHTML = renderDeThiCards(res.data, true);
            deThiCardGrid.querySelectorAll('.kq-card').forEach(function (card) {
                card.addEventListener('click', function () {
                    currentDeThiId = this.dataset.deThiId;
                    currentDeThiTen = this.dataset.deThiTen;
                    loadKetQuaSinhVien(currentLopId, currentDeThiId);
                });
            });
        }).catch(function () {
            deThiCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Lỗi tải dữ liệu.</div>';
        });
    }

    // ===== Luồng 1 — Bước 3: Kết quả (lớp + ẩn danh) =====
    function loadKetQuaSinhVien(lopId, deThiId) {
        showView('ketQua');
        updateBreadcrumb('ketQua');
        resetTable();
        fetchApi(API_BASE + '/lop/' + lopId + '/de-thi/' + deThiId, 'GET').then(function (res) {
            if (!res || !res.success || !res.data || res.data.length === 0) {
                allKetQua = []; filteredKetQua = [];
                bangKetQuaBody.innerHTML = '<tr><td colspan="' + COL_SPAN + '" class="empty-state"><i class="fas fa-inbox"></i><br>Chưa có kết quả nào.</td></tr>';
                return;
            }
            allKetQua = res.data;
            filteredKetQua = allKetQua.slice();
            currentPage = 1;
            renderBangKetQua();
        }).catch(function () {
            bangKetQuaBody.innerHTML = '<tr><td colspan="' + COL_SPAN + '" class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Lỗi tải dữ liệu.</td></tr>';
        });
    }

    // ===== Luồng 2 — Tất cả đề thi =====
    function loadDanhSachDeThiAll() {
        allDeThiCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-spinner fa-spin"></i><br>Đang tải…</div>';
        fetchApi(API_BASE + '/de-thi', 'GET').then(function (res) {
            if (!res || !res.success || !res.data || res.data.length === 0) {
                allDeThiCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-inbox"></i><br>Chưa có đề thi nào.</div>';
                return;
            }
            allDeThiCardGrid.innerHTML = renderDeThiCards(res.data, false);
            allDeThiCardGrid.querySelectorAll('.kq-card').forEach(function (card) {
                card.addEventListener('click', function () {
                    currentDeThiId = this.dataset.deThiId;
                    currentDeThiTen = this.dataset.deThiTen;
                    currentLopId = ''; // Luồng 2: không qua lớp
                    currentLopTen = 'Tất cả đề thi';
                    loadKetQuaTheoDeThiId(currentDeThiId);
                });
            });
        }).catch(function () {
            allDeThiCardGrid.innerHTML = '<div class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Lỗi tải dữ liệu.</div>';
        });
    }

    // ===== Luồng 2 — Kết quả toàn bộ theo đề =====
    function loadKetQuaTheoDeThiId(deThiId) {
        showView('ketQua');
        updateBreadcrumb('ketQua');
        resetTable();
        fetchApi(API_BASE + '/de-thi/' + deThiId, 'GET').then(function (res) {
            if (!res || !res.success || !res.data || res.data.length === 0) {
                allKetQua = []; filteredKetQua = [];
                bangKetQuaBody.innerHTML = '<tr><td colspan="' + COL_SPAN + '" class="empty-state"><i class="fas fa-inbox"></i><br>Chưa có kết quả nào.</td></tr>';
                return;
            }
            allKetQua = res.data;
            filteredKetQua = allKetQua.slice();
            currentPage = 1;
            renderBangKetQua();
        }).catch(function () {
            bangKetQuaBody.innerHTML = '<tr><td colspan="' + COL_SPAN + '" class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Lỗi tải dữ liệu.</td></tr>';
        });
    }

    // ===== Render đề thi cards (dùng chung 2 luồng) =====
    function renderDeThiCards(data, isLopFlow) {
        var html = '';
        data.forEach(function (dt) {
            var ten = dt.tenDeThi || dt.maDeThi || 'Đề thi';
            html += '<div class="kq-card" data-de-thi-id="' + dt.deThiId + '" data-de-thi-ten="' + escAttr(ten) + '">' +
                    '<h4><i class="fas fa-file-alt"></i> ' + escHtml(ten) + '</h4>' +
                    '<div class="kq-card-stats">' +
                    '<span><i class="fas fa-book"></i> ' + escHtml(dt.tenMonHoc || '—') + '</span>' +
                    '<span><i class="fas fa-clock"></i> ' + (dt.thoiGianPhut || '—') + ' phút</span>' +
                    '<span><i class="fas fa-pen"></i> ' + dt.soLuotThi + ' lượt</span>';
            if (!isLopFlow && dt.soLuotThiAnDanh > 0) {
                html += '<span><i class="fas fa-user-secret"></i> ' + dt.soLuotThiAnDanh + ' ẩn danh</span>';
            }
            html += '</div></div>';
        });
        return html;
    }

    // ===== Render bảng kết quả =====
    function resetTable() {
        searchInput.value = '';
        bangKetQuaBody.innerHTML = '<tr><td colspan="' + COL_SPAN + '" class="empty-state"><i class="fas fa-spinner fa-spin"></i><br>Đang tải…</td></tr>';
        paginationEl.innerHTML = '';
    }

    function renderBangKetQua() {
        var total = filteredKetQua.length;
        var totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;
        var start = (currentPage - 1) * PAGE_SIZE;
        var pageData = filteredKetQua.slice(start, start + PAGE_SIZE);

        if (total === 0) {
            bangKetQuaBody.innerHTML = '<tr><td colspan="' + COL_SPAN + '" class="empty-state"><i class="fas fa-search"></i><br>Không tìm thấy kết quả.</td></tr>';
            paginationEl.innerHTML = '';
            return;
        }

        var html = '';
        pageData.forEach(function (r, idx) {
            var stt = start + idx + 1;
            var nguonClass = (r.nguon === 'Link công khai') ? 'link' : 'lop';
            html += '<tr>' +
                '<td>' + stt + '</td>' +
                '<td>' + escHtml(r.mssv) + '</td>' +
                '<td>' + escHtml(r.ho) + '</td>' +
                '<td>' + escHtml(r.ten) + '</td>' +
                '<td>' + (r.duongDanTruyCap ? '<a href="' + escAttr(r.duongDanTruyCap) + '" target="_blank" style="color:#667eea;">' + escHtml(r.duongDanTruyCap) + '</a>' : '—') + '</td>' +
                '<td>' + escHtml(r.maTruyCapDaDung || '—') + '</td>' +
                '<td style="text-align:center;">' +
                    '<span class="editable-diem" data-diem-id="' + r.ketQuaThiId + '" data-val="' + escAttr(r.diem || '') + '" title="Nhấn để sửa điểm">' +
                        escHtml(r.diem || '—') +
                    '</span>' +
                '</td>' +
                '<td>' + escHtml(r.thoiGianNop) + '</td>' +
                '<td><span class="badge-nguon ' + nguonClass + '">' + escHtml(r.nguon || '—') + '</span></td>' +
                '<td>' +
                    '<span class="editable-note" data-kq-id="' + r.ketQuaThiId + '" data-val="' + escAttr(r.ghiChu || '') + '" title="Nhấn để sửa ghi chú">' +
                        (r.ghiChu ? escHtml(r.ghiChu) : '<i class="placeholder-text">Nhập ghi chú…</i>') +
                    '</span>' +
                '</td>' +
                '</tr>';
        });
        bangKetQuaBody.innerHTML = html;
        renderPagination(totalPages);

        // Click-to-edit: Điểm
        bangKetQuaBody.querySelectorAll('.editable-diem').forEach(function (span) {
            span.addEventListener('click', function () {
                startEditDiem(this);
            });
        });

        // Click-to-edit: Ghi chú
        bangKetQuaBody.querySelectorAll('.editable-note').forEach(function (span) {
            span.addEventListener('click', function () {
                startEditNote(this);
            });
        });
    }

    // --- Click-to-edit: Điểm ---
    function startEditDiem(span) {
        if (span.querySelector('input')) return; // đang edit rồi
        var kqId = span.dataset.diemId;
        var oldVal = span.dataset.val;
        var input = document.createElement('input');
        input.type = 'number';
        input.step = '0.01';
        input.min = '0';
        input.value = oldVal;
        input.style.cssText = 'width:70px;text-align:center;font-weight:600;padding:3px 6px;border:1.5px solid #667eea;border-radius:6px;font-size:0.88rem;font-family:inherit;';
        span.textContent = '';
        span.appendChild(input);
        input.focus();
        input.select();

        function finish() {
            var newVal = input.value.trim();
            if (newVal !== oldVal) {
                saveDiem(kqId, newVal);
                span.dataset.val = newVal;
            }
            span.removeChild(input);
            span.textContent = newVal || '—';
        }
        input.addEventListener('blur', finish);
        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); input.blur(); }
            if (e.key === 'Escape') { input.value = oldVal; input.blur(); }
        });
    }

    // --- Click-to-edit: Ghi chú ---
    function startEditNote(span) {
        if (span.querySelector('input')) return;
        var kqId = span.dataset.kqId;
        var oldVal = span.dataset.val;
        var input = document.createElement('input');
        input.type = 'text';
        input.value = oldVal;
        input.placeholder = 'Nhập ghi chú…';
        input.style.cssText = 'width:100%;padding:3px 8px;border:1.5px solid #667eea;border-radius:6px;font-size:0.82rem;font-family:inherit;';
        span.innerHTML = '';
        span.appendChild(input);
        input.focus();
        input.select();

        function finish() {
            var newVal = input.value.trim();
            if (newVal !== oldVal) {
                saveGhiChu(kqId, newVal);
                span.dataset.val = newVal;
            }
            span.removeChild(input);
            span.innerHTML = newVal ? escHtml(newVal) : '<i class="placeholder-text">Nhập ghi chú…</i>';
        }
        input.addEventListener('blur', finish);
        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); input.blur(); }
            if (e.key === 'Escape') { input.value = oldVal; input.blur(); }
        });
    }

    function renderPagination(totalPages) {
        if (totalPages <= 1) { paginationEl.innerHTML = ''; return; }
        var html = '';
        html += '<button ' + (currentPage === 1 ? 'disabled' : '') + ' data-page="' + (currentPage - 1) + '"><i class="fas fa-chevron-left"></i></button>';
        var startP = Math.max(1, currentPage - 2);
        var endP = Math.min(totalPages, currentPage + 2);
        if (startP > 1) html += '<button data-page="1">1</button><span class="page-info">…</span>';
        for (var p = startP; p <= endP; p++) {
            html += '<button data-page="' + p + '"' + (p === currentPage ? ' class="active"' : '') + '>' + p + '</button>';
        }
        if (endP < totalPages) html += '<span class="page-info">…</span><button data-page="' + totalPages + '">' + totalPages + '</button>';
        html += '<button ' + (currentPage === totalPages ? 'disabled' : '') + ' data-page="' + (currentPage + 1) + '"><i class="fas fa-chevron-right"></i></button>';
        html += '<span class="page-info">' + filteredKetQua.length + ' kết quả</span>';
        paginationEl.innerHTML = html;
        paginationEl.querySelectorAll('button[data-page]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var pg = parseInt(this.dataset.page, 10);
                if (pg >= 1 && pg <= totalPages && pg !== currentPage) {
                    currentPage = pg;
                    renderBangKetQua();
                }
            });
        });
    }

    // ===== Ghi chú =====
    function saveGhiChu(kqId, ghiChu) {
        fetchApi(API_BASE + '/ghi-chu/' + kqId, 'PUT', { ghiChu: ghiChu }).then(function (res) {
            if (res && res.success) {
                showToast('Đã lưu ghi chú.', 'success');
                allKetQua.forEach(function (r) { if (r.ketQuaThiId === kqId) r.ghiChu = ghiChu; });
                filteredKetQua.forEach(function (r) { if (r.ketQuaThiId === kqId) r.ghiChu = ghiChu; });
            } else {
                showToast((res && res.message) || 'Lỗi', 'error');
            }
        }).catch(function () { showToast('Lỗi kết nối.', 'error'); });
    }

    // ===== Điểm =====
    function saveDiem(kqId, diem) {
        if (diem !== '' && (isNaN(parseFloat(diem)) || parseFloat(diem) < 0)) {
            showToast('Điểm không hợp lệ.', 'error');
            return;
        }
        fetchApi(API_BASE + '/diem/' + kqId, 'PUT', { diem: diem }).then(function (res) {
            if (res && res.success) {
                showToast('Đã lưu điểm.', 'success');
                allKetQua.forEach(function (r) { if (r.ketQuaThiId === kqId) r.diem = diem || '—'; });
                filteredKetQua.forEach(function (r) { if (r.ketQuaThiId === kqId) r.diem = diem || '—'; });
            } else {
                showToast((res && res.message) || 'Lỗi', 'error');
            }
        }).catch(function () { showToast('Lỗi kết nối.', 'error'); });
    }

    // ===== Xuất Excel (.xlsx) — server Apache POI, đúng bộ lọc hiện tại =====
    function exportXlsx() {
        if (!filteredKetQua || filteredKetQua.length === 0) {
            showToast('Không có dữ liệu để xuất.', 'info');
            return;
        }
        if (!currentDeThiId) {
            showToast('Thiếu mã đề thi.', 'error');
            return;
        }
        fetch(API_BASE + '/export-xlsx', {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({
                deThiId: currentDeThiId,
                fileNameHint: currentDeThiTen || '',
                rows: filteredKetQua
            })
        }).then(function (res) {
            if (res.status === 401) {
                storage.removeItem('nguoiDung');
                storage.removeItem('vaiTro');
                storage.removeItem('token');
                storage.removeItem('tokenExpiresAt');
                window.location.href = '/login?expired=1';
                return;
            }
            if (!res.ok) {
                return res.json().then(function (j) {
                    showToast((j && j.message) || 'Lỗi xuất file.', 'error');
                });
            }
            var disp = res.headers.get('Content-Disposition');
            var fname = 'ket-qua-export.xlsx';
            if (disp) {
                var m = /filename="([^"]+)"/.exec(disp);
                if (m) fname = m[1];
            }
            return res.blob().then(function (blob) {
                var url = URL.createObjectURL(blob);
                var a = document.createElement('a');
                a.href = url;
                a.download = fname;
                a.click();
                URL.revokeObjectURL(url);
                showToast('Đã xuất file Excel.', 'success');
            });
        }).catch(function () {
            showToast('Lỗi kết nối.', 'error');
        });
    }

    // ===== Fetch helper =====
    function fetchApi(url, method, body) {
        var opts = { method: method || 'GET', headers: authHeaders() };
        if (body) opts.body = JSON.stringify(body);
        return fetch(url, opts).then(function (r) {
            if (r.status === 401) {
                storage.removeItem('nguoiDung'); storage.removeItem('vaiTro');
                storage.removeItem('token'); storage.removeItem('tokenExpiresAt');
                window.location.href = '/login?expired=1';
                return {};
            }
            return r.json();
        });
    }

    // ===== User name =====
    function loadUserName() {
        try {
            var u = JSON.parse(storage.getItem('nguoiDung') || '{}');
            if (u && (u.hoTen || u.ho || u.ten)) {
                displayName.textContent = u.hoTen || ((u.ho || '') + ' ' + (u.ten || '')).trim();
            }
        } catch (_) {}
    }

    // ===== Sidebar =====
    function initSidebar() {
        var sidebar = document.getElementById('sidebar');
        var toggle = document.getElementById('sidebarToggle');
        var close = document.getElementById('sidebarClose');
        if (toggle) toggle.addEventListener('click', function () { sidebar.classList.toggle('active'); });
        if (close) close.addEventListener('click', function () { sidebar.classList.remove('active'); });
    }

    // ===== Logout =====
    function initLogout() {
        var btn = document.getElementById('btnLogout');
        if (btn) btn.addEventListener('click', function (e) {
            e.preventDefault();
            try { fetch('/api/logout', { method: 'POST' }); } catch (_) {}
            storage.removeItem('nguoiDung'); storage.removeItem('vaiTro');
            storage.removeItem('token'); storage.removeItem('tokenExpiresAt');
            window.location.href = '/login';
        });
    }

    // ===== Toast =====
    function showToast(msg, type) {
        var container = document.getElementById('toastContainer');
        var toast = document.createElement('div');
        toast.className = 'toast toast-' + (type || 'info');
        toast.innerHTML = '<i class="fas fa-' + (type === 'success' ? 'check-circle' : type === 'error' ? 'times-circle' : 'info-circle') + '"></i> ' + escHtml(msg);
        container.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 4000);
    }

    // ===== Util =====
    function escHtml(str) { if (!str) return ''; var d = document.createElement('div'); d.textContent = str; return d.innerHTML; }
    function escAttr(str) { if (!str) return ''; return str.replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/'/g,'&#39;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
})();

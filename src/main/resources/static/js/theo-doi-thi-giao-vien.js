const storage = (localStorage.getItem('token') ? localStorage : sessionStorage);

function layBadge(trangThai) {
    if (trangThai === 'DANG_THI') return '<span class="status-badge st-dang-thi">Đang thi</span>';
    if (trangThai === 'DA_NOP_BAI') return '<span class="status-badge st-da-nop">Đã nộp bài</span>';
    if (trangThai === 'DA_VAO_CHUA_NOP') return '<span class="status-badge st-da-vao">Đã vào chưa nộp</span>';
    return '<span class="status-badge st-chua-vao">Chưa vào thi</span>';
}

function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    }
    if (sidebarClose) {
        sidebarClose.addEventListener('click', () => sidebar.classList.remove('active'));
    }
}

async function fetchJson(url, token) {
    const response = await fetch(url, {
        headers: { Authorization: 'Bearer ' + token }
    });
    return response.json();
}

function buildQuery(params) {
    const search = new URLSearchParams();
    Object.keys(params).forEach((key) => {
        const val = params[key];
        if (val !== null && val !== undefined && val !== '') {
            search.append(key, val);
        }
    });
    return search.toString();
}

async function taiDanhSachDeThi(token) {
    const select = document.getElementById('deThiSelect');
    const result = await fetchJson('/api/giao-vien/theo-doi-thi/de-thi', token);
    if (!result.success || !Array.isArray(result.data) || result.data.length === 0) {
        select.innerHTML = '<option value="">Chưa có đề thi</option>';
        return null;
    }
    select.innerHTML = result.data.map((d) => {
        const title = d.maDeThi ? `${d.maDeThi} - ${d.tenDeThi}` : d.tenDeThi;
        return `<option value="${d.id}">${title}</option>`;
    }).join('');
    return select.value;
}

function renderThongKe(data) {
    document.getElementById('stTong').textContent = data.tongSo || 0;
    document.getElementById('stChuaVao').textContent = data.soChuaVaoThi || 0;
    document.getElementById('stDangThi').textContent = data.soDangThi || 0;
    document.getElementById('stDaNop').textContent = data.soDaNopBai || 0;
    document.getElementById('stDaVao').textContent = data.soDaVaoChuaNop || 0;
}

function renderBang(data) {
    const tbody = document.getElementById('trackingTableBody');
    const list = (data && Array.isArray(data.danhSach)) ? data.danhSach : [];
    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">Không có dữ liệu phù hợp.</td></tr>';
        return;
    }
    tbody.innerHTML = list.map((item) => `
        <tr>
            <td>${item.maNguoiDung || ''}</td>
            <td>${item.hoTen || ''}</td>
            <td>${item.email || ''}</td>
            <td>${layBadge(item.nhomTrangThai)}</td>
            <td>${item.thoiGianBatDau || '-'}</td>
            <td>${item.thoiGianNop || '-'}</td>
        </tr>
    `).join('');
}

async function taiDanhSachTheoDoi(token, deThiId) {
    const trangThai = document.getElementById('trangThaiSelect').value;
    const keyword = document.getElementById('keywordInput').value;
    const query = buildQuery({ deThiId, nhomTrangThai: trangThai, keyword });
    const result = await fetchJson(`/api/giao-vien/theo-doi-thi?${query}`, token);
    if (!result.success) {
        renderThongKe({});
        renderBang({});
        return;
    }
    renderThongKe(result.data || {});
    renderBang(result.data || {});
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

document.addEventListener('DOMContentLoaded', async function () {
    setupSidebar();
    setupLogout();

    const token = storage.getItem('token');
    const vaiTro = storage.getItem('vaiTro');
    const nguoiDungRaw = storage.getItem('nguoiDung');
    if (!token || vaiTro !== 'GIAO_VIEN' || !nguoiDungRaw) {
        window.location.href = '/login';
        return;
    }

    try {
        const user = JSON.parse(nguoiDungRaw);
        const display = document.getElementById('displayName');
        if (display) {
            display.textContent = user.hoTen || '';
        }
    } catch (_) {
        window.location.href = '/login';
        return;
    }

    let deThiId = await taiDanhSachDeThi(token);
    if (!deThiId) {
        return;
    }

    await taiDanhSachTheoDoi(token, deThiId);

    document.getElementById('deThiSelect').addEventListener('change', async (e) => {
        deThiId = e.target.value;
        await taiDanhSachTheoDoi(token, deThiId);
    });
    document.getElementById('btnRefresh').addEventListener('click', async () => {
        await taiDanhSachTheoDoi(token, deThiId);
    });
    document.getElementById('trangThaiSelect').addEventListener('change', async () => {
        await taiDanhSachTheoDoi(token, deThiId);
    });
    document.getElementById('keywordInput').addEventListener('keydown', async (e) => {
        if (e.key === 'Enter') {
            await taiDanhSachTheoDoi(token, deThiId);
        }
    });
});

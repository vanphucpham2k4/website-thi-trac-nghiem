/**
 * Trang hồ sơ sinh viên — tải/cập nhật thông tin và đổi mật khẩu qua API JWT.
 */
const storage = localStorage.getItem('token') ? localStorage : sessionStorage;

function isTokenExpired() {
    const expiresAt = storage.getItem('tokenExpiresAt');
    if (!expiresAt) return true;
    return Date.now() > parseInt(expiresAt, 10);
}

function authHeaders() {
    const token = storage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    };
}

function redirectLogin() {
    window.location.href = '/login';
}

function showMessage(el, text, isError) {
    if (!el) return;
    el.hidden = false;
    el.textContent = text;
    el.classList.toggle('form-message-error', !!isError);
    el.classList.toggle('form-message-success', !isError);
}

function clearMessage(el) {
    if (!el) return;
    el.hidden = true;
    el.textContent = '';
    el.classList.remove('form-message-error', 'form-message-success');
}

function syncStorageUser(dto) {
    if (!dto) return;
    const raw = storage.getItem('nguoiDung');
    if (!raw) return;
    try {
        const u = JSON.parse(raw);
        u.ho = dto.ho;
        u.ten = dto.ten;
        u.hoTen = dto.hoTen;
        u.email = dto.email;
        u.soDienThoai = dto.soDienThoai;
        storage.setItem('nguoiDung', JSON.stringify(u));
    } catch (e) {
        console.error(e);
    }
}

function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    }
    if (sidebarClose && sidebar) {
        sidebarClose.addEventListener('click', () => sidebar.classList.remove('active'));
    }
}

function setupLogout() {
    const btnLogout = document.getElementById('btnLogout');
    if (!btnLogout) return;
    btnLogout.addEventListener('click', async function (e) {
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

async function loadProfile() {
    const msgProfile = document.getElementById('msgProfile');
    clearMessage(msgProfile);

    try {
        const res = await fetch('/api/sinh-vien/ho-so', { headers: authHeaders() });
        const json = await res.json();

        if (res.status === 401) {
            storage.removeItem('nguoiDung');
            storage.removeItem('vaiTro');
            storage.removeItem('token');
            storage.removeItem('tokenExpiresAt');
            window.location.href = '/login?expired=1';
            return;
        }

        if (!json.success || !json.data) {
            showMessage(msgProfile, json.message || 'Không tải được hồ sơ.', true);
            return;
        }

        const d = json.data;
        const maEl = document.getElementById('maNguoiDung');
        if (maEl) maEl.value = d.maNguoiDung || '';
        document.getElementById('ho').value = d.ho || '';
        document.getElementById('ten').value = d.ten || '';
        document.getElementById('email').value = d.email || '';
        document.getElementById('soDienThoai').value = d.soDienThoai || '';
    } catch (err) {
        console.error(err);
        showMessage(msgProfile, 'Lỗi kết nối. Vui lòng thử lại.', true);
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const nguoiDung = storage.getItem('nguoiDung');
    const vaiTro = storage.getItem('vaiTro');
    const token = storage.getItem('token');

    if (!nguoiDung || vaiTro !== 'SINH_VIEN' || !token) {
        redirectLogin();
        return;
    }
    if (isTokenExpired()) {
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
        window.location.href = '/login?expired=1';
        return;
    }

    let userData;
    try {
        userData = JSON.parse(nguoiDung);
    } catch (e) {
        redirectLogin();
        return;
    }

    document.getElementById('displayName').textContent =
        userData.hoTen || ((userData.ho || '') + ' ' + (userData.ten || '')).trim() || '—';

    setupSidebar();
    setupLogout();
    loadProfile();

    const formProfile = document.getElementById('formProfile');
    const msgProfile = document.getElementById('msgProfile');

    formProfile.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearMessage(msgProfile);

        const body = {
            ho: document.getElementById('ho').value.trim(),
            ten: document.getElementById('ten').value.trim(),
            email: document.getElementById('email').value.trim(),
            soDienThoai: document.getElementById('soDienThoai').value.trim()
        };

        const btn = document.getElementById('btnSaveProfile');
        btn.disabled = true;

        try {
            const res = await fetch('/api/sinh-vien/ho-so', {
                method: 'PUT',
                headers: authHeaders(),
                body: JSON.stringify(body)
            });
            const json = await res.json();

            if (res.status === 401) {
                window.location.href = '/login?expired=1';
                return;
            }

            if (json.success && json.data) {
                showMessage(msgProfile, json.message || 'Đã lưu thông tin.', false);
                syncStorageUser(json.data);
                document.getElementById('displayName').textContent =
                    json.data.hoTen || (json.data.ho + ' ' + json.data.ten);
            } else {
                showMessage(msgProfile, json.message || 'Cập nhật thất bại.', true);
            }
        } catch (err) {
            console.error(err);
            showMessage(msgProfile, 'Lỗi kết nối. Vui lòng thử lại.', true);
        } finally {
            btn.disabled = false;
        }
    });

    const formPassword = document.getElementById('formPassword');
    const msgPassword = document.getElementById('msgPassword');

    formPassword.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearMessage(msgPassword);

        const matKhauCu = document.getElementById('matKhauCu').value;
        const matKhauMoi = document.getElementById('matKhauMoi').value;
        const matKhauMoi2 = document.getElementById('matKhauMoi2').value;

        if (matKhauMoi !== matKhauMoi2) {
            showMessage(msgPassword, 'Mật khẩu mới nhập lại không khớp.', true);
            return;
        }

        const btn = document.getElementById('btnSavePassword');
        btn.disabled = true;

        try {
            const res = await fetch('/api/sinh-vien/ho-so/doi-mat-khau', {
                method: 'POST',
                headers: authHeaders(),
                body: JSON.stringify({ matKhauCu, matKhauMoi })
            });
            const json = await res.json();

            if (res.status === 401) {
                window.location.href = '/login?expired=1';
                return;
            }

            if (json.success) {
                showMessage(msgPassword, json.message || 'Đã đổi mật khẩu.', false);
                formPassword.reset();
            } else {
                showMessage(msgPassword, json.message || 'Đổi mật khẩu thất bại.', true);
            }
        } catch (err) {
            console.error(err);
            showMessage(msgPassword, 'Lỗi kết nối. Vui lòng thử lại.', true);
        } finally {
            btn.disabled = false;
        }
    });
});

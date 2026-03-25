/**
 * login-admin.js - Xử lý form đăng nhập Admin
 * Gửi API login với vai trò ADMIN, lưu session và chuyển hướng
 */

document.addEventListener('DOMContentLoaded', function () {
    // Các phần tử DOM
    const form = document.getElementById('adminLoginForm');
    const messageBox = document.getElementById('messageBox');
    const messageText = messageBox.querySelector('.message-text');
    const messageIcon = messageBox.querySelector('.message-icon');
    const btnLogin = document.getElementById('btnLogin');
    const btnText = btnLogin.querySelector('.btn-text');
    const btnLoader = btnLogin.querySelector('.btn-loader');

    // Các trường input
    const taiKhoanInput = document.getElementById('taiKhoan');
    const matKhauInput = document.getElementById('matKhau');
    const captchaAnswerInput = document.getElementById('captchaAnswer');
    const captchaIdInput = document.getElementById('captchaId');
    const captchaQuestionEl = document.getElementById('captchaQuestion');
    const btnRefreshCaptcha = document.getElementById('btnRefreshCaptcha');

    // ====== 1. TẢI CAPTCHA KHI TRANG LOAD ======
    loadCaptcha();

    async function loadCaptcha() {
        try {
            const response = await fetch('/api/captcha');
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            // API trả về CaptchaDTO trực tiếp (captchaId, captchaQuestion) — giống login.js, không bọc ApiResponse
            const captcha = await response.json();
            if (captcha.captchaId && captcha.captchaQuestion) {
                captchaQuestionEl.textContent = captcha.captchaQuestion;
                captchaIdInput.value = captcha.captchaId;
            } else {
                captchaQuestionEl.innerHTML = '<span class="error-text">Lỗi tải CAPTCHA</span>';
            }
        } catch (error) {
            console.error('Lỗi tải CAPTCHA:', error);
            captchaQuestionEl.innerHTML = '<span class="error-text">Lỗi kết nối</span>';
        }
    }

    // ====== 2. LÀM MỚI CAPTCHA ======
    btnRefreshCaptcha.addEventListener('click', function () {
        captchaQuestionEl.innerHTML = '<span class="loading-text">Đang tải...</span>';
        loadCaptcha();
    });

    // ====== 3. XỬ LÝ FORM SUBMIT ======
    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        // Lấy dữ liệu form
        const taiKhoan = taiKhoanInput.value.trim();
        const matKhau = matKhauInput.value;
        const captchaAnswer = captchaAnswerInput.value.trim();
        const captchaId = captchaIdInput.value;
        const ghiNho = document.getElementById('ghiNho')?.checked ?? false;

        // Validate đầu vào
        if (!taiKhoan) {
            showFieldError(taiKhoanInput, 'Vui lòng nhập tài khoản');
            return;
        }
        clearFieldError(taiKhoanInput);

        if (!matKhau) {
            showFieldError(matKhauInput, 'Vui lòng nhập mật khẩu');
            return;
        }
        clearFieldError(matKhauInput);

        if (!captchaAnswer) {
            showCaptchaError('Vui lòng nhập kết quả CAPTCHA');
            return;
        }
        clearCaptchaError();

        // Hiện trạng thái loading
        setLoading(true);
        hideMessage();

        // Gọi API đăng nhập
        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    taiKhoan: taiKhoan,
                    matKhau: matKhau,
                    captchaAnswer: parseInt(captchaAnswer),
                    captchaId: captchaId,
                    ghiNho: ghiNho
                })
            });

            const result = await response.json();

            if (result.success) {
                // Lưu JWT token + user info — dùng localStorage nếu ghiNho, sessionStorage nếu không
                const storage = ghiNho ? localStorage : sessionStorage;
                if (result.data) {
                    storage.setItem('token', result.data.token || '');
                    storage.setItem('tokenExpiresAt', result.data.expiresAt || 0);
                    storage.setItem('nguoiDung', JSON.stringify(result.data.nguoiDung));
                    storage.setItem('vaiTro', result.data.nguoiDung?.vaiTro || '');
                }

                // Kiểm tra vai trò - chỉ cho phép ADMIN
                if (result.data.nguoiDung?.vaiTro === 'ADMIN') {
                    showMessage('success', 'Đăng nhập thành công! Đang chuyển hướng...');
                    setTimeout(() => {
                        window.location.href = '/admin';
                    }, 800);
                } else {
                    // Xóa session nếu không phải admin
                    storage.removeItem('nguoiDung');
                    storage.removeItem('vaiTro');
                    storage.removeItem('token');
                    storage.removeItem('tokenExpiresAt');
                    showMessage('error', 'Tài khoản này không có quyền truy cập trang Admin.');
                    refreshCaptchaOnError();
                    setLoading(false);
                }
            } else {
                // Hiện lỗi từ server
                showMessage('error', result.message || 'Đăng nhập thất bại');
                refreshCaptchaOnError();
                setLoading(false);
            }
        } catch (error) {
            console.error('Lỗi đăng nhập:', error);
            showMessage('error', 'Đã xảy ra lỗi kết nối. Vui lòng thử lại.');
            refreshCaptchaOnError();
            setLoading(false);
        }
    });

    // ====== 4. HÀM HỖ TRỢ ======

    /**
     * Hiện trạng thái loading trên nút đăng nhập
     */
    function setLoading(isLoading) {
        if (isLoading) {
            btnLogin.disabled = true;
            btnLogin.classList.add('loading');
        } else {
            btnLogin.disabled = false;
            btnLogin.classList.remove('loading');
        }
    }

    /**
     * Hiện thông báo (thành công / lỗi)
     */
    function showMessage(type, text) {
        messageBox.className = 'message-box ' + type;
        messageText.textContent = text;
        messageBox.style.display = 'block';
    }

    /**
     * Ẩn thông báo
     */
    function hideMessage() {
        messageBox.style.display = 'none';
    }

    /**
     * Hiện lỗi dưới field input
     */
    function showFieldError(input, message) {
        input.classList.add('error');
        const fieldMsg = input.closest('.form-group').querySelector('.field-message');
        if (fieldMsg) fieldMsg.textContent = message;
    }

    /**
     * Xóa lỗi field input
     */
    function clearFieldError(input) {
        input.classList.remove('error');
        const fieldMsg = input.closest('.form-group').querySelector('.field-message');
        if (fieldMsg) fieldMsg.textContent = '';
    }

    /**
     * Hiện lỗi CAPTCHA
     */
    function showCaptchaError(message) {
        const captchaMsg = document.querySelector('.captcha-message');
        if (captchaMsg) captchaMsg.textContent = message;
    }

    /**
     * Xóa lỗi CAPTCHA
     */
    function clearCaptchaError() {
        const captchaMsg = document.querySelector('.captcha-message');
        if (captchaMsg) captchaMsg.textContent = '';
    }

    /**
     * Làm mới CAPTCHA khi đăng nhập thất bại
     */
    function refreshCaptchaOnError() {
        captchaAnswerInput.value = '';
        loadCaptcha();
    }

    // ====== 5. TOGGLE PASSWORD ======
    const togglePasswordBtns = document.querySelectorAll('.toggle-password');
    togglePasswordBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const input = matKhauInput;
            const icon = btn.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });

    // ====== 6. KIỂM TRA ĐĂNG NHẬP SẴN CÓ ======
    function isTokenExpired() {
        const expiresAt = localStorage.getItem('tokenExpiresAt') || sessionStorage.getItem('tokenExpiresAt');
        if (!expiresAt) return true;
        return Date.now() > parseInt(expiresAt);
    }

    function getStorage() {
        // Ưu tiên localStorage nếu có token (ghi nhớ), không thì dùng sessionStorage
        return localStorage.getItem('token') ? localStorage : sessionStorage;
    }

    const storage = getStorage();
    const savedUser = storage.getItem('nguoiDung');
    const savedRole = storage.getItem('vaiTro');
    const savedToken = storage.getItem('token');

    if (savedUser && savedRole === 'ADMIN' && savedToken && !isTokenExpired()) {
        window.location.href = '/admin';
    } else if (savedUser && isTokenExpired()) {
        // Token hết hạn -> xóa và quay lại trang đăng nhập
        storage.removeItem('nguoiDung');
        storage.removeItem('vaiTro');
        storage.removeItem('token');
        storage.removeItem('tokenExpiresAt');
    }
});

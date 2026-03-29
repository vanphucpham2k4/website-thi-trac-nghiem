/**
 * login.js - File JavaScript xử lý trang ĐĂNG NHẬP
 * Xử lý submit form, CAPTCHA, gọi API, hiển thị thông báo lỗi/thành công
 */

// ============================================
// 1. DOM ELEMENTS
// ============================================
// Lấy các element cần thao tác trong DOM

/** Chỉ cho phép đường dẫn nội bộ (tránh open redirect). */
function layRedirectNoiBoAnToan() {
    const raw = new URLSearchParams(window.location.search).get('redirect');
    if (!raw || typeof raw !== 'string') return null;
    const p = raw.trim();
    if (!p.startsWith('/') || p.startsWith('//')) return null;
    if (p.includes('://')) return null;
    if (p.includes('\\')) return null;
    return p;
}

document.addEventListener('DOMContentLoaded', function() {
    // Form đăng nhập
    const loginForm = document.getElementById('loginForm');

    // Input fields
    const taiKhoanInput = document.getElementById('taiKhoan');
    const matKhauInput = document.getElementById('matKhau');
    const captchaAnswerInput = document.getElementById('captchaAnswer');

    // CAPTCHA elements
    const captchaQuestionEl = document.getElementById('captchaQuestion');
    const captchaIdInput = document.getElementById('captchaId');
    const btnRefreshCaptcha = document.getElementById('btnRefreshCaptcha');

    // Nút toggle mật khẩu
    const togglePasswordBtn = document.querySelector('.toggle-password');

    // Nút submit
    const btnLogin = document.getElementById('btnLogin');

    // Message box
    const messageBox = document.getElementById('messageBox');

    // ============================================
    // 2. TẢI CAPTCHA KHI TRANG LOAD
    // ============================================
    // Gọi API để lấy CAPTCHA mới

    loadCaptcha();

    /**
     * Tải CAPTCHA mới từ server
     * Gọi API GET /api/captcha
     */
    async function loadCaptcha() {
        try {
            const response = await fetch('/api/captcha');
            const captcha = await response.json();

            // Hiển thị câu hỏi CAPTCHA
            captchaQuestionEl.textContent = captcha.captchaQuestion;

            // Lưu CAPTCHA ID vào input ẩn
            captchaIdInput.value = captcha.captchaId;

            // Xóa giá trị input đáp án
            captchaAnswerInput.value = '';

        } catch (error) {
            console.error('Lỗi khi tải CAPTCHA:', error);
            captchaQuestionEl.innerHTML = '<span class="error-text">Lỗi tải CAPTCHA</span>';
        }
    }

    // ============================================
    // 3. NÚT LÀM MỚI CAPTCHA
    // ============================================
    // Xử lý click nút refresh CAPTCHA

    if (btnRefreshCaptcha) {
        btnRefreshCaptcha.addEventListener('click', function() {
            // Hiệu ứng quay cho icon
            const icon = this.querySelector('i');
            icon.classList.add('fa-spin');

            // Tải CAPTCHA mới
            loadCaptcha().finally(() => {
                icon.classList.remove('fa-spin');
            });
        });
    }

    // ============================================
    // 4. TOGGLE PASSWORD VISIBILITY
    // ============================================
    // Xử lý hiện/ẩn mật khẩu khi click vào icon mắt

    if (togglePasswordBtn) {
        togglePasswordBtn.addEventListener('click', function() {
            // Toggle type giữa 'password' và 'text'
            const type = matKhauInput.getAttribute('type') === 'password' ? 'text' : 'password';
            matKhauInput.setAttribute('type', type);

            // Đổi icon giữa 'fa-eye' và 'fa-eye-slash'
            const icon = this.querySelector('i');
            if (type === 'text') {
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    }

    // ============================================
    // 5. FORM VALIDATION ON INPUT
    // ============================================
    // Kiểm tra validation khi người dùng nhập liệu

    // Remove error state khi user bắt đầu nhập
    taiKhoanInput.addEventListener('input', function() {
        this.classList.remove('error');
        clearFieldError(this);
    });

    matKhauInput.addEventListener('input', function() {
        this.classList.remove('error');
        clearFieldError(this);
    });

    captchaAnswerInput.addEventListener('input', function() {
        this.classList.remove('error');
        clearFieldError(this);
    });

    // ============================================
    // 6. FORM SUBMIT HANDLER
    // ============================================
    // Xử lý submit form đăng nhập

    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            // Ngăn chặn submit form mặc định (chuyển trang)
            e.preventDefault();

            // Lấy giá trị từ form
            const taiKhoan = taiKhoanInput.value.trim();
            const matKhau = matKhauInput.value;
            const captchaAnswer = captchaAnswerInput.value;
            const captchaId = captchaIdInput.value;
            const ghiNho = document.getElementById('ghiNho')?.checked ?? false;

            // ===== Validation phía client =====

            // Kiểm tra tài khoản không trống
            if (!taiKhoan) {
                showFieldError(taiKhoanInput, 'Vui lòng nhập tài khoản (email hoặc số điện thoại)');
                taiKhoanInput.classList.add('error');
                taiKhoanInput.focus();
                return;
            }

            // Kiểm tra mật khẩu không trống
            if (!matKhau) {
                showFieldError(matKhauInput, 'Vui lòng nhập mật khẩu');
                matKhauInput.classList.add('error');
                matKhauInput.focus();
                return;
            }

            // Kiểm tra CAPTCHA không trống
            if (!captchaAnswer) {
                showFieldError(captchaAnswerInput, 'Vui lòng nhập kết quả CAPTCHA');
                captchaAnswerInput.classList.add('error');
                captchaAnswerInput.focus();
                return;
            }

            // ===== Hiệu ứng loading =====
            showLoading(btnLogin);

            // ===== Gọi API đăng nhập =====
            try {
                const response = await fetch('/api/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        taiKhoan: taiKhoan,
                        matKhau: matKhau,
                        captchaId: captchaId,
                        captchaAnswer: parseInt(captchaAnswer),
                        ghiNho: ghiNho
                    })
                });

                // Parse JSON response
                const result = await response.json();

                // ===== Xử lý kết quả =====
                if (result.success) {
                    // Đăng nhập thành công
                    showMessage('success', 'Đăng nhập thành công! Đang chuyển hướng...');

                    // Lưu JWT token vào localStorage (hoặc sessionStorage nếu không chọn "ghi nhớ")
                    // → khi ghiNho = false: token vẫn tồn tại trong 1 tab (sessionStorage), hết 30p thì hết hiệu lực
                    // → khi ghiNho = true:  token lưu lâu hơn trên máy (localStorage), hết 30p thì hết hiệu lực
                    const storage = ghiNho ? localStorage : sessionStorage;
                    if (result.data) {
                        // Token + thời điểm hết hạn (epoch ms)
                        storage.setItem('token', result.data.token || '');
                        storage.setItem('tokenExpiresAt', result.data.expiresAt || 0);
                        // Thông tin người dùng
                        storage.setItem('nguoiDung', JSON.stringify(result.data.nguoiDung));
                        storage.setItem('vaiTro', result.data.nguoiDung?.vaiTro || '');
                    }

                    // Chuyển hướng sau 1.5 giây
                    setTimeout(() => {
                        const vaiTro = result.data?.nguoiDung?.vaiTro;
                        const redirect = layRedirectNoiBoAnToan();
                        if (vaiTro === 'SINH_VIEN' && redirect) {
                            window.location.href = redirect;
                            return;
                        }
                        if (vaiTro === 'GIAO_VIEN') {
                            window.location.href = '/dashboard/giao-vien';
                        } else if (vaiTro === 'SINH_VIEN') {
                            window.location.href = '/dashboard/sinh-vien';
                        } else {
                            window.location.href = '/';
                        }
                    }, 1500);

                } else {
                    // Đăng nhập thất bại - hiển thị lỗi theo errorCode
                    hideLoading(btnLogin);
                    handleLoginError(result);

                    // Làm mới CAPTCHA khi đăng nhập thất bại
                    loadCaptcha();
                }

            } catch (error) {
                // Lỗi kết nối server
                console.error('Lỗi khi gọi API:', error);
                hideLoading(btnLogin);
                showMessage('error', 'Không thể kết nối đến máy chủ! Vui lòng thử lại sau.');
            }
        });
    }

    // ============================================
    // 7. HÀM XỬ LÝ LỖI ĐĂNG NHẬP
    // ============================================

    /**
     * Xử lý các mã lỗi đăng nhập và hiển thị thông báo phù hợp
     * @param {Object} result - Response từ API
     */
    function handleLoginError(result) {
        const errorCode = result.errorCode;
        let errorMessage = result.message || 'Đăng nhập thất bại!';
        let focusInput = null;

        switch (errorCode) {
            case 1:
                // Tài khoản không tồn tại
                errorMessage = 'Tài khoản không tồn tại! Vui lòng kiểm tra lại email hoặc số điện thoại.';
                focusInput = taiKhoanInput;
                taiKhoanInput.classList.add('error');
                showFieldError(taiKhoanInput, errorMessage);
                break;

            case 2:
                // Sai mật khẩu
                errorMessage = 'Sai mật khẩu! Vui lòng nhập lại.';
                focusInput = matKhauInput;
                matKhauInput.classList.add('error');
                matKhauInput.value = ''; // Xóa mật khẩu đã nhập
                matKhauInput.focus();
                showFieldError(matKhauInput, errorMessage);
                break;

            case 3:
                // CAPTCHA sai
                errorMessage = 'Mã CAPTCHA không đúng! Vui lòng nhập lại.';
                focusInput = captchaAnswerInput;
                captchaAnswerInput.classList.add('error');
                captchaAnswerInput.value = ''; // Xóa đáp án đã nhập
                captchaAnswerInput.focus();
                showFieldError(captchaAnswerInput, errorMessage);
                break;

            case 6:
                // ADMIN bị chặn - dùng message từ server
                errorMessage = result.message || 'Tài khoản Admin không được đăng nhập tại đây!';
                break;

            case 7:
                // Lỗi hệ thống
                errorMessage = 'Đã xảy ra lỗi hệ thống! Vui lòng thử lại sau.';
                break;

            default:
                errorMessage = result.message || 'Đã xảy ra lỗi không xác định!';
        }

        // Hiển thị message box
        showMessage('error', errorMessage);

        // Focus vào input nếu có
        if (focusInput) {
            setTimeout(() => focusInput.focus(), 100);
        }
    }

    // ============================================
    // 8. UI HELPER FUNCTIONS
    // ============================================

    /**
     * Hiển thị thông báo lỗi dưới input field
     * @param {HTMLElement} input - Input element
     * @param {string} message - Thông báo lỗi
     */
    function showFieldError(input, message) {
        // Tìm element hiển thị lỗi (sibling của input hoặc parent)
        const formGroup = input.closest('.form-group');
        if (formGroup) {
            const errorElement = formGroup.querySelector('.field-message');
            if (errorElement) {
                errorElement.textContent = message;
                errorElement.style.display = 'block';
            }
        }
    }

    /**
     * Xóa thông báo lỗi dưới input field
     * @param {HTMLElement} input - Input element
     */
    function clearFieldError(input) {
        const formGroup = input.closest('.form-group');
        if (formGroup) {
            const errorElement = formGroup.querySelector('.field-message');
            if (errorElement) {
                errorElement.textContent = '';
                errorElement.style.display = 'none';
            }
        }
    }

    /**
     * Hiệu ứng loading trên nút submit
     * @param {HTMLElement} button - Nút submit
     */
    function showLoading(button) {
        button.classList.add('loading');
        button.disabled = true;
    }

    /**
     * Bỏ hiệu ứng loading trên nút submit
     * @param {HTMLElement} button - Nút submit
     */
    function hideLoading(button) {
        button.classList.remove('loading');
        button.disabled = false;
    }

    /**
     * Hiển thị message box với kiểu và nội dung tương ứng
     * @param {string} type - Loại message: 'success', 'error', 'warning'
     * @param {string} text - Nội dung thông báo
     */
    function showMessage(type, text) {
        messageBox.style.display = 'block';
        messageBox.className = 'message-box ' + type;

        const messageContent = messageBox.querySelector('.message-content');
        const messageText = messageBox.querySelector('.message-text');

        messageText.textContent = text;

        // Đặt icon phù hợp với loại message
        const messageIcon = messageBox.querySelector('.message-icon');
        if (type === 'success') {
            messageIcon.className = 'message-icon fas fa-check-circle';
        } else if (type === 'error') {
            messageIcon.className = 'message-icon fas fa-exclamation-circle';
        } else {
            messageIcon.className = 'message-icon fas fa-info-circle';
        }

        // Auto hide sau 5 giây (chỉ cho success message)
        if (type === 'success') {
            setTimeout(() => {
                messageBox.style.display = 'none';
            }, 5000);
        }
    }

    // ============================================
    // 9. ENTER KEY HANDLER
    // ============================================
    // Xử lý nhấn Enter để submit form

    document.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            if (document.activeElement.tagName === 'INPUT') {
                loginForm.dispatchEvent(new Event('submit'));
            }
        }
    });
});

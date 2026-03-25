/**
 * register.js - File JavaScript xử lý trang ĐĂNG KÝ
 * Xử lý: Tải CAPTCHA, Validate form, Gọi API đăng ký
 */

// ============================================
// 1. DOM ELEMENTS
// ============================================
// Lấy các element cần thao tác trong DOM

document.addEventListener('DOMContentLoaded', function() {
    // Form đăng ký
    const registerForm = document.getElementById('registerForm');

    // Input fields
    const emailInput = document.getElementById('email');
    const hoInput = document.getElementById('ho');
    const tenInput = document.getElementById('ten');
    const soDienThoaiInput = document.getElementById('soDienThoai');
    const matKhauInput = document.getElementById('matKhau');
    const captchaAnswerInput = document.getElementById('captchaAnswer');

    // CAPTCHA elements
    const captchaQuestionEl = document.getElementById('captchaQuestion');
    const captchaIdInput = document.getElementById('captchaId');
    const btnRefreshCaptcha = document.getElementById('btnRefreshCaptcha');

    // Nút toggle mật khẩu
    const togglePasswordBtn = document.querySelector('.toggle-password');

    // Nút submit
    const btnRegister = document.getElementById('btnRegister');

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
    // Xử lý hiện/ẩn mật khẩu

    if (togglePasswordBtn) {
        togglePasswordBtn.addEventListener('click', function() {
            const type = matKhauInput.getAttribute('type') === 'password' ? 'text' : 'password';
            matKhauInput.setAttribute('type', type);

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
    // 5. VALIDATION REALTIME (Kiểm tra khi nhập)
    // ============================================
    // Kiểm tra email và số điện thoại khi user nhập

    // Debounce function - trì hoãn việc gọi API
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Kiểm tra email khi blur (rời khỏi field)
    emailInput.addEventListener('blur', async function() {
        const email = this.value.trim();

        if (!email) return;

        // Kiểm tra định dạng email cơ bản
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showFieldError(this, 'Email không đúng định dạng');
            this.classList.add('error');
            return;
        }

        // Gọi API kiểm tra email đã tồn tại chưa
        try {
            const response = await fetch(`/api/check-email?email=${encodeURIComponent(email)}`);
            const result = await response.json();

            if (!result.data) { // data = false nghĩa là email đã tồn tại
                showFieldError(this, 'Email đã được đăng ký! Vui lòng sử dụng email khác.');
                this.classList.add('error');
                this.classList.remove('valid');
            } else {
                clearFieldError(this);
                this.classList.remove('error');
                this.classList.add('valid');
            }
        } catch (error) {
            console.error('Lỗi kiểm tra email:', error);
        }
    });

    // Kiểm tra số điện thoại khi blur
    soDienThoaiInput.addEventListener('blur', async function() {
        const sdt = this.value.trim();

        if (!sdt) return;

        // Kiểm tra định dạng số điện thoại Việt Nam (10 số, bắt đầu bằng 0)
        const sdtRegex = /^0[0-9]{9}$/;
        if (!sdtRegex.test(sdt)) {
            showFieldError(this, 'Số điện thoại không đúng định dạng (phải có 10 số, bắt đầu bằng 0)');
            this.classList.add('error');
            return;
        }

        // Gọi API kiểm tra số điện thoại đã tồn tại chưa
        try {
            const response = await fetch(`/api/check-sdt?sdt=${encodeURIComponent(sdt)}`);
            const result = await response.json();

            if (!result.data) { // data = false nghĩa là số điện thoại đã tồn tại
                showFieldError(this, 'Số điện thoại đã được đăng ký! Vui lòng sử dụng số khác.');
                this.classList.add('error');
                this.classList.remove('valid');
            } else {
                clearFieldError(this);
                this.classList.remove('error');
                this.classList.add('valid');
            }
        } catch (error) {
            console.error('Lỗi kiểm tra số điện thoại:', error);
        }
    });

    // Xóa lỗi khi user bắt đầu nhập lại
    [emailInput, hoInput, tenInput, soDienThoaiInput, matKhauInput].forEach(input => {
        input.addEventListener('input', function() {
            this.classList.remove('error');
            clearFieldError(this);
        });
    });

    // ============================================
    // 6. FORM SUBMIT HANDLER
    // ============================================
    // Xử lý submit form đăng ký

    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            // Ngăn chặn submit form mặc định
            e.preventDefault();

            // Lấy giá trị từ form
            const email = emailInput.value.trim();
            const ho = hoInput.value.trim();
            const ten = tenInput.value.trim();
            const soDienThoai = soDienThoaiInput.value.trim();
            const matKhau = matKhauInput.value;
            const vaiTro = document.querySelector('input[name="vaiTro"]:checked').value;
            const captchaId = captchaIdInput.value;
            const captchaAnswer = captchaAnswerInput.value;

            // ===== Validation phía client =====

            // Kiểm tra email
            if (!email) {
                showFieldError(emailInput, 'Email không được trống');
                emailInput.classList.add('error');
                emailInput.focus();
                return;
            }

            // Kiểm tra họ
            if (!ho) {
                showFieldError(hoInput, 'Họ không được trống');
                hoInput.classList.add('error');
                hoInput.focus();
                return;
            }

            // Kiểm tra tên
            if (!ten) {
                showFieldError(tenInput, 'Tên không được trống');
                tenInput.classList.add('error');
                tenInput.focus();
                return;
            }

            // Kiểm tra số điện thoại
            if (!soDienThoai) {
                showFieldError(soDienThoaiInput, 'Số điện thoại không được trống');
                soDienThoaiInput.classList.add('error');
                soDienThoaiInput.focus();
                return;
            }

            // Kiểm tra mật khẩu
            if (!matKhau) {
                showFieldError(matKhauInput, 'Mật khẩu không được trống');
                matKhauInput.classList.add('error');
                matKhauInput.focus();
                return;
            }

            // Kiểm tra CAPTCHA
            if (!captchaAnswer) {
                showFieldError(captchaAnswerInput, 'Vui lòng nhập kết quả CAPTCHA');
                captchaAnswerInput.classList.add('error');
                captchaAnswerInput.focus();
                return;
            }

            // ===== Hiệu ứng loading =====
            showLoading(btnRegister);

            // ===== Gọi API đăng ký =====
            try {
                const response = await fetch('/api/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        email: email,
                        ho: ho,
                        ten: ten,
                        soDienThoai: soDienThoai,
                        matKhau: matKhau,
                        vaiTro: vaiTro,
                        captchaId: captchaId,
                        captchaAnswer: parseInt(captchaAnswer)
                    })
                });

                // Parse JSON response
                const result = await response.json();

                // ===== Xử lý kết quả =====
                if (result.success) {
                    // Đăng ký thành công
                    showMessage('success', 'Đăng ký thành công! Đang chuyển hướng đến trang đăng nhập...');

                    // Chuyển hướng sau 2 giây
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);

                } else {
                    // Đăng ký thất bại
                    hideLoading(btnRegister);
                    handleRegisterError(result);
                }

            } catch (error) {
                // Lỗi kết nối server
                console.error('Lỗi khi gọi API:', error);
                hideLoading(btnRegister);
                showMessage('error', 'Không thể kết nối đến máy chủ! Vui lòng thử lại sau.');
            }
        });
    }

    // ============================================
    // 7. HÀM XỬ LÝ LỖI ĐĂNG KÝ
    // ============================================

    /**
     * Xử lý các mã lỗi đăng ký và hiển thị thông báo phù hợp
     * @param {Object} result - Response từ API
     */
    function handleRegisterError(result) {
        const errorCode = result.errorCode;
        let errorMessage = result.message || 'Đăng ký thất bại!';
        let focusInput = null;
        let captchaMessage = document.querySelector('.captcha-message');

        switch (errorCode) {
            case 3:
                // CAPTCHA sai
                errorMessage = 'Mã CAPTCHA không đúng! Vui lòng nhập lại.';
                focusInput = captchaAnswerInput;
                captchaAnswerInput.classList.add('error');
                captchaAnswerInput.value = '';
                captchaAnswerInput.focus();

                // Làm mới CAPTCHA
                loadCaptcha();
                break;

            case 4:
                // Email đã tồn tại
                errorMessage = 'Email đã được đăng ký! Vui lòng sử dụng email khác.';
                focusInput = emailInput;
                emailInput.classList.add('error');
                showFieldError(emailInput, errorMessage);
                break;

            case 5:
                // Số điện thoại đã tồn tại
                errorMessage = 'Số điện thoại đã được đăng ký! Vui lòng sử dụng số khác.';
                focusInput = soDienThoaiInput;
                soDienThoaiInput.classList.add('error');
                showFieldError(soDienThoaiInput, errorMessage);
                break;

            case 6:
                // Vai trò không hợp lệ
                errorMessage = 'Vai trò không hợp lệ! Chỉ có thể đăng ký là Sinh Viên hoặc Giáo Viên.';
                break;

            case 7:
                // Lỗi hệ thống
                errorMessage = 'Đã xảy ra lỗi hệ thống! Vui lòng thử lại sau.';
                break;

            case 8:
                // Dữ liệu không hợp lệ
                errorMessage = result.message || 'Dữ liệu không hợp lệ! Vui lòng kiểm tra lại.';
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
     */
    function showFieldError(input, message) {
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
     */
    function showLoading(button) {
        button.classList.add('loading');
        button.disabled = true;
    }

    /**
     * Bỏ hiệu ứng loading trên nút submit
     */
    function hideLoading(button) {
        button.classList.remove('loading');
        button.disabled = false;
    }

    /**
     * Hiển thị message box với kiểu và nội dung tương ứng
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
    // 9. ROLE SELECTION ANIMATION
    // ============================================
    // Animation cho việc chọn vai trò

    const roleOptions = document.querySelectorAll('.role-option input[type="radio"]');
    roleOptions.forEach(radio => {
        radio.addEventListener('change', function() {
            // Remove animation class from all
            roleOptions.forEach(opt => {
                const card = opt.nextElementSibling;
                card.style.transform = 'scale(1)';
            });

            // Add animation to selected
            if (this.checked) {
                const selectedCard = this.nextElementSibling;
                selectedCard.style.transform = 'scale(1.05)';
                setTimeout(() => {
                    selectedCard.style.transform = '';
                }, 200);
            }
        });
    });
});

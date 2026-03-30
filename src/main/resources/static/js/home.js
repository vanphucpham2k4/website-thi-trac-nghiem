/**
 * home.js - File JavaScript cho trang chủ website thi trắc nghiệm
 * Xử lý các sự kiện tương tác như: menu mobile, smooth scroll, form liên hệ...
 */

// ============================================
// 1. MOBILE MENU TOGGLE (Menu di động)
// ============================================
// Xử lý bật/tắt menu hamburger trên thiết bị di động

document.addEventListener('DOMContentLoaded', function() {
    // Lấy element menu toggle và menu chính
    const menuToggle = document.querySelector('.menu-toggle');
    const navMenu = document.querySelector('.nav-menu');
    
    // Thêm sự kiện click cho nút menu toggle
    if (menuToggle && navMenu) {
        menuToggle.addEventListener('click', function() {
            // Toggle class 'active' để hiện/ẩn menu
            navMenu.classList.toggle('active');
            
            // Đổi icon giữa bars và times (X)
            const icon = menuToggle.querySelector('i');
            if (navMenu.classList.contains('active')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
    }
    
    // ============================================
    // 2. ACTIVE MENU HIGHLIGHT (Đánh dấu menu active)
    // ============================================
    // Highlight menu item dựa trên URL hiện tại
    
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-menu a');
    
    navLinks.forEach(link => {
        // Kiểm tra nếu href của link khớp với path hiện tại
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
    
    // ============================================
    // 3. SMOOTH SCROLL FOR ANCHOR LINKS (Cuộn mượt)
    // ============================================
    // Hiệu ứng cuộn mượt khi click vào các liên kết neo (anchor links)
    
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            // Lấy ID của phần tử target từ href
            const targetId = this.getAttribute('href');
            
            // Bỏ qua nếu chỉ là "#"
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            
            if (targetElement) {
                e.preventDefault(); // Ngăn chặn hành vi mặc định
                
                // Tính toán vị trí scroll với offset cho navbar cố định
                const navbarHeight = document.querySelector('.navbar').offsetHeight;
                const targetPosition = targetElement.offsetTop - navbarHeight;
                
                // Cuộn đến vị trí target với animation mượt
                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
                
                // Đóng menu mobile nếu đang mở
                if (navMenu && navMenu.classList.contains('active')) {
                    navMenu.classList.remove('active');
                    const icon = menuToggle.querySelector('i');
                    icon.classList.remove('fa-times');
                    icon.classList.add('fa-bars');
                }
            }
        });
    });
    
    // ============================================
    // 4. NAVBAR SCROLL EFFECT (Hiệu ứng navbar khi cuộn)
    // ============================================
    // Thay đổi style navbar khi người dùng cuộn trang
    
    let lastScroll = 0; // Vị trí scroll trước đó
    const navbar = document.querySelector('.navbar');
    
    window.addEventListener('scroll', function() {
        const currentScroll = window.pageYOffset;
        
        // Thêm shadow khi cuộn xuống
        if (currentScroll > 50) {
            navbar.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
        } else {
            navbar.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
        }
        
        lastScroll = currentScroll;
    });
    
    // ============================================
    // 5. CONTACT FORM HANDLING (Xử lý form liên hệ)
    // ============================================
    // Xử lý sự kiện submit form liên hệ
    
    const contactForm = document.querySelector('.contact-form form');
    
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault(); // Ngăn chặn submit mặc định
            
            // Lấy dữ liệu từ form
            const formData = new FormData(this);
            
            // Kiểm tra dữ liệu (validation đơn giản)
            const name = formData.get('name');
            const email = formData.get('email');
            const subject = formData.get('subject');
            const message = formData.get('message');
            
            // Kiểm tra các trường không được trống
            if (!name || !email || !subject || !message) {
                alert('Vui lòng điền đầy đủ thông tin!');
                return;
            }
            
            // Kiểm tra định dạng email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert('Email không hợp lệ!');
                return;
            }
            
            // Trong thực tế, gửi data đến server ở đây
            // Ví dụ: fetch('/api/contact', { method: 'POST', body: formData })
            
            // Hiển thị thông báo thành công
            alert('Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất có thể.');
            
            // Reset form sau khi gửi thành công
            this.reset();
        });
    }
    
    // ============================================
    // 6. ANIMATION ON SCROLL (Animation khi scroll)
    // ============================================
    // Hiệu ứng xuất hiện cho các phần tử khi scroll vào viewport
    
    const observerOptions = {
        root: null, // Sử dụng viewport làm root
        rootMargin: '0px',
        threshold: 0.1 // Kích hoạt khi 10% của element xuất hiện
    };
    
    // Tạo Intersection Observer để theo dõi các phần tử
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                // Thêm class 'visible' khi phần tử xuất hiện trong viewport
                entry.target.classList.add('visible');
                
                // Ngừng theo dõi sau khi đã kích hoạt
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Quan sát các feature cards và subject cards
    document.querySelectorAll('.feature-card, .subject-card, .step').forEach(card => {
        // Thêm class ban đầu cho animation
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        
        observer.observe(card);
    });
    
    // ============================================
    // 7. COUNTER ANIMATION (Animation bộ đếm)
    // ============================================
    // Hiệu ứng đếm số từ 0 đến giá trị cuối cùng
    
    const counters = document.querySelectorAll('.stat-number');
    let counterStarted = false; // Đảm bảo animation chỉ chạy 1 lần
    
    function animateCounters() {
        counters.forEach(counter => {
            const target = parseInt(counter.textContent.replace(/[^0-9]/g, ''));
            const suffix = counter.textContent.replace(/[0-9]/g, '');
            const duration = 2000; // Thời gian animation 2 giây
            const step = target / (duration / 16); // 60fps
            
            let current = 0;
            
            const updateCounter = () => {
                current += step;
                if (current < target) {
                    counter.textContent = Math.floor(current) + suffix;
                    requestAnimationFrame(updateCounter);
                } else {
                    counter.textContent = target + suffix;
                }
            };
            
            updateCounter();
        });
    }
    
    // Khởi động animation counter khi hero section visible
    const heroSection = document.querySelector('.hero');
    if (heroSection) {
        const heroObserver = new IntersectionObserver(function(entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting && !counterStarted) {
                    counterStarted = true;
                    animateCounters();
                    heroObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0.3 });
        
        heroObserver.observe(heroSection);
    }
    
    // ============================================
    // 8. KEYBOARD NAVIGATION (Điều hướng bằng bàn phím)
    // ============================================
    // Hỗ trợ điều hướng bằng bàn phím cho accessibility
    
    document.addEventListener('keydown', function(e) {
        // ESC key đóng menu mobile
        if (e.key === 'Escape' && navMenu && navMenu.classList.contains('active')) {
            navMenu.classList.remove('active');
            const icon = menuToggle.querySelector('i');
            icon.classList.remove('fa-times');
            icon.classList.add('fa-bars');
        }
    });
    
    // ============================================
    // 9. PREVENT DOUBLE SUBMIT (Ngăn double submit)
    // ============================================
    // Ngăn người dùng click nút submit nhiều lần
    
    const submitButtons = document.querySelectorAll('button[type="submit"], .btn-submit');
    submitButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Disable button sau khi click
            this.disabled = true;
            this.style.opacity = '0.7';
            this.style.pointerEvents = 'none';
            
            // Re-enable sau 3 giây (trong trường hợp có lỗi)
            setTimeout(() => {
                this.disabled = false;
                this.style.opacity = '1';
                this.style.pointerEvents = 'auto';
            }, 3000);
        });
    });
    
    // ============================================
    // 10. ADD VISIBLE CLASS FOR ANIMATIONS
    // ============================================
    // Style cho animation khi scroll
    
    const style = document.createElement('style');
    style.textContent = `
        .feature-card.visible,
        .subject-card.visible,
        .step.visible {
            opacity: 1 !important;
            transform: translateY(0) !important;
        }
    `;
    document.head.appendChild(style);

    // ============================================
    // 11. AUTHENTICATION STATE (Trạng thái đăng nhập)
    // ============================================
    // Kiểm tra và hiển thị trạng thái đăng nhập trên navbar

    const authButtons = document.getElementById('authButtons');
    const userMenu = document.getElementById('userMenu');
    const userNameEl = document.getElementById('userName');
    const dropdownRole = document.getElementById('dropdownRole');
    const btnLogout = document.getElementById('btnLogout');

    // Chọn storage: ưu tiên localStorage nếu có token, không thì sessionStorage
    function getStorage() {
        return localStorage.getItem('token') ? localStorage : sessionStorage;
    }

    function isTokenExpired() {
        const expiresAt = getStorage().getItem('tokenExpiresAt');
        if (!expiresAt) return true;
        return Date.now() > parseInt(expiresAt);
    }

    /**
     * Kiểm tra trạng thái đăng nhập và cập nhật UI
     * Đồng thời tự động xóa session nếu token đã hết hạn
     */
    function checkAuthState() {
        const st = getStorage();
        const nguoiDung = st.getItem('nguoiDung');
        const vaiTro = st.getItem('vaiTro');
        const token = st.getItem('token');

        // Nếu có token nhưng đã hết hạn → xóa và coi như chưa đăng nhập
        if (token && isTokenExpired()) {
            st.removeItem('nguoiDung');
            st.removeItem('vaiTro');
            st.removeItem('token');
            st.removeItem('tokenExpiresAt');
            window.location.href = '/login?expired=1';
            return;
        }

        if (nguoiDung) {
            // Đã đăng nhập - Hiển thị thông tin người dùng
            try {
                const user = JSON.parse(nguoiDung);

                // Ẩn nút đăng nhập/đăng ký
                if (authButtons) {
                    authButtons.style.display = 'none';
                }

                // Hiển thị menu người dùng
                if (userMenu) {
                    userMenu.style.display = 'block';
                }

                // Cập nhật tên người dùng
                if (userNameEl) {
                    userNameEl.textContent = user.hoTen || user.ho + ' ' + user.ten;
                }

                // Cập nhật vai trò và link dashboard
                if (dropdownRole) {
                    let roleText = '';
                    let dashboardLink = '#';

                    switch (vaiTro) {
                        case 'ADMIN':
                            roleText = 'Quản trị viên';
                            dashboardLink = '/admin';
                            break;
                        case 'GIAO_VIEN':
                            roleText = 'Giáo viên';
                            dashboardLink = '/dashboard/giao-vien';
                            break;
                        case 'SINH_VIEN':
                            roleText = 'Sinh viên';
                            dashboardLink = '/dashboard/sinh-vien';
                            break;
                        default:
                            roleText = vaiTro || 'Người dùng';
                            dashboardLink = '/';
                    }
                    dropdownRole.textContent = roleText;

                    // Cập nhật link Dashboard
                    const dashboardLinkEl = document.getElementById('dashboardLink');
                    if (dashboardLinkEl) {
                        dashboardLinkEl.href = dashboardLink;
                    }

                    const profileLinkEl = document.getElementById('profileLink');
                    if (profileLinkEl) {
                        if (vaiTro === 'GIAO_VIEN') {
                            profileLinkEl.href = '/dashboard/giao-vien/ho-so';
                        } else if (vaiTro === 'SINH_VIEN') {
                            profileLinkEl.href = '/dashboard/sinh-vien/ho-so';
                        } else if (vaiTro === 'ADMIN') {
                            profileLinkEl.href = '/admin#ho-so';
                        } else {
                            profileLinkEl.href = '#';
                        }
                    }

                    // Ẩn "Lịch sử thi" đối với quản trị viên
                    if (vaiTro === 'ADMIN') {
                        const lichSuLink = document.getElementById('lichSuThiLink');
                        if (lichSuLink) {
                            lichSuLink.style.display = 'none';
                        }
                    }
                }

            } catch (e) {
                console.error('Lỗi parse user data:', e);
            }
        } else {
            // Chưa đăng nhập - Hiển thị nút đăng nhập/đăng ký
            if (authButtons) {
                authButtons.style.display = 'flex';
            }
            if (userMenu) {
                userMenu.style.display = 'none';
            }
        }
    }

    // Kiểm tra auth state khi trang load
    checkAuthState();

    // Cập nhật auth state khi storage thay đổi (từ tab khác)
    window.addEventListener('storage', function(e) {
        if (['nguoiDung', 'vaiTro', 'token', 'tokenExpiresAt'].includes(e.key)) {
            checkAuthState();
        }
    });

    // Xử lý click vào user menu để hiện/ẩn dropdown
    const userInfo = document.querySelector('.user-info');
    const userDropdown = document.getElementById('userDropdown');

    if (userInfo && userDropdown) {
        userInfo.addEventListener('click', function(e) {
            e.stopPropagation();
            userDropdown.classList.toggle('show');
        });

        // Đóng dropdown khi click ra ngoài
        document.addEventListener('click', function() {
            userDropdown.classList.remove('show');
        });
    }

    // Xử lý đăng xuất
    if (btnLogout) {
        btnLogout.addEventListener('click', async function(e) {
            e.preventDefault();

            // Gọi API đăng xuất
            try {
                await fetch('/api/logout', {
                    method: 'POST'
                });
            } catch (error) {
                console.error('Lỗi khi gọi API logout:', error);
            }

            // Xóa token + user info khỏi storage
            const st = getStorage();
            st.removeItem('nguoiDung');
            st.removeItem('vaiTro');
            st.removeItem('token');
            st.removeItem('tokenExpiresAt');

            // Cập nhật UI
            checkAuthState();

            // Chuyển hướng về trang chủ
            window.location.href = '/';
        });
    }
});

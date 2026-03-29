/**
 * Giao diện làm bài thi — một trang, cuộn dọc.
 * Gọi API đề thi sau, map dữ liệu vào hienThiDeThi() theo cấu trúc DeThiCauHoiDTO (backend).
 */

(function () {
    'use strict';

    const elTenDe = document.getElementById('examTenDe');
    const elMeta = document.getElementById('examMeta');
    const elTimer = document.getElementById('examTimerDisplay');
    const elList = document.getElementById('examCauHoiList');
    const elLoading = document.getElementById('examLoading');
    const btnNop = document.getElementById('btnNopBai');

    let countdownInterval = null;
    let secondsLeft = null;

    function escapeHtml(s) {
        if (s == null) return '';
        const d = document.createElement('div');
        d.textContent = s;
        return d.innerHTML;
    }

    function formatTime(totalSec) {
        const m = Math.floor(totalSec / 60);
        const s = totalSec % 60;
        return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
    }

    function stopCountdown() {
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
    }

    function startCountdown(thoiGianPhut) {
        stopCountdown();
        if (thoiGianPhut == null || thoiGianPhut <= 0) {
            secondsLeft = null;
            if (elTimer) elTimer.textContent = '—';
            return;
        }
        secondsLeft = Math.floor(thoiGianPhut * 60);
        if (elTimer) elTimer.textContent = formatTime(secondsLeft);
        countdownInterval = setInterval(function () {
            secondsLeft -= 1;
            if (elTimer) elTimer.textContent = formatTime(Math.max(0, secondsLeft));
            if (secondsLeft <= 0) {
                stopCountdown();
                elTimer.textContent = '00:00';
            }
        }, 1000);
    }

    /**
     * @param {object} meta - { ten?: string, thoiGianPhut?: number, moTa?: string }
     */
    function capNhatTieuDe(meta) {
        if (!meta) return;
        if (elTenDe) elTenDe.textContent = meta.ten || 'Làm bài thi';
        if (elMeta) {
            const parts = [];
            if (meta.thoiGianPhut != null) parts.push(meta.thoiGianPhut + ' phút');
            if (meta.moTa) parts.push(meta.moTa);
            elMeta.textContent = parts.length ? parts.join(' · ') : '';
        }
    }

    /**
     * Một phần tử câu hỏi (tương thích DeThiCauHoiDTO JSON từ API):
     * { cauHoiId, thuTu, noiDung, loaiCauHoi, luaChonA, luaChonB, luaChonC, luaChonD }
     */
    function renderMotCau(item, index) {
        const thuTu = item.thuTu != null ? item.thuTu : index + 1;
        const id = item.cauHoiId || ('cau-' + index);
        const loai = (item.loaiCauHoi || 'TRAC_NGHIEM').toUpperCase();
        const noiDung = item.noiDung || '';

        const wrap = document.createElement('article');
        wrap.className = 'exam-question-card';
        wrap.id = 'cau-' + id;

        const head = document.createElement('div');
        head.className = 'question-head';
        head.innerHTML =
            '<span class="question-num">' + escapeHtml(String(thuTu)) + '</span>' +
            '<div class="question-body">' +
            '<div class="question-content">' + noiDung + '</div></div>';

        if (loai === 'TRAC_NGHIEM') {
            const labels = ['A', 'B', 'C', 'D'];
            const keys = ['luaChonA', 'luaChonB', 'luaChonC', 'luaChonD'];
            const ul = document.createElement('ul');
            ul.className = 'options-list';
            keys.forEach(function (key, i) {
                const text = item[key];
                if (text == null || String(text).trim() === '') return;
                const li = document.createElement('li');
                const inputId = 'q-' + id + '-' + labels[i];
                li.innerHTML =
                    '<label class="option-row" for="' + escapeHtml(inputId) + '">' +
                    '<input type="radio" name="cau-' + escapeHtml(id) + '" id="' + escapeHtml(inputId) + '" value="' + labels[i] + '">' +
                    '<span class="option-label">' + labels[i] + '.</span>' +
                    '<span class="option-text">' + escapeHtml(String(text)) + '</span>' +
                    '</label>';
                ul.appendChild(li);
            });
            head.querySelector('.question-body').appendChild(ul);
        } else {
            const note = document.createElement('div');
            note.className = 'question-other';
            note.textContent = 'Dạng câu hỏi này sẽ được bổ sung khi API hỗ trợ.';
            head.querySelector('.question-body').appendChild(note);
        }

        wrap.appendChild(head);
        return wrap;
    }

    /**
     * Hiển thị toàn bộ câu trên một trang (cuộn dọc).
     * @param {object} meta - { ten?, thoiGianPhut?, moTa? }
     * @param {Array<object>} cauHoiList - mảng DeThiCauHoiDTO
     */
    function hienThiDeThi(meta, cauHoiList) {
        if (!elList) return;
        capNhatTieuDe(meta);
        startCountdown(meta && meta.thoiGianPhut);
        elList.innerHTML = '';
        if (elLoading) elLoading.style.display = 'none';

        if (!cauHoiList || cauHoiList.length === 0) {
            elList.innerHTML = '<p class="exam-empty-hint">Chưa có câu hỏi. Hãy nạp dữ liệu từ API.</p>';
            return;
        }

        cauHoiList.forEach(function (cau, idx) {
            elList.appendChild(renderMotCau(cau, idx));
        });
    }

    /**
     * @returns {Object<string, string>} cauHoiId -> 'A'|'B'|'C'|'D'
     */
    function layTraLoiTracNghiem() {
        const out = {};
        if (!elList) return out;
        elList.querySelectorAll('input[type="radio"]:checked').forEach(function (inp) {
            const name = inp.getAttribute('name');
            if (!name || !name.startsWith('cau-')) return;
            const cauId = name.replace(/^cau-/, '');
            out[cauId] = inp.value;
        });
        return out;
    }

    function demoNeuCan() {
        const params = new URLSearchParams(window.location.search);
        if (params.get('demo') !== '1') return;
        hienThiDeThi(
            { ten: 'Đề thi mẫu (demo)', thoiGianPhut: 45, moTa: 'Xem trước giao diện' },
            [
                {
                    cauHoiId: 'demo-1',
                    thuTu: 1,
                    noiDung: '<p>Chọn phát biểu đúng về HTML.</p>',
                    loaiCauHoi: 'TRAC_NGHIEM',
                    luaChonA: 'HTML là ngôn ngữ lập trình',
                    luaChonB: 'HTML mô tả cấu trúc trang web',
                    luaChonC: 'HTML chỉ chạy trên server',
                    luaChonD: 'HTML thay thế hoàn toàn CSS',
                },
                {
                    cauHoiId: 'demo-2',
                    thuTu: 2,
                    noiDung: '<p>HTTP status 404 nghĩa là gì?</p>',
                    loaiCauHoi: 'TRAC_NGHIEM',
                    luaChonA: 'Thành công',
                    luaChonB: 'Không tìm thấy tài nguyên',
                    luaChonC: 'Lỗi server',
                    luaChonD: 'Chưa đăng nhập',
                },
            ]
        );
    }

    if (btnNop) {
        btnNop.addEventListener('click', function () {
            const traLoi = layTraLoiTracNghiem();
            console.log('[LamBaiThi] Bản ghi trả lời (cauHoiId -> đáp án):', traLoi);
            alert('Đã ghi nhận trên console. Kết nối API nộp bài tại đây.');
        });
    }

    window.LamBaiThi = {
        hienThiDeThi: hienThiDeThi,
        layTraLoiTracNghiem: layTraLoiTracNghiem,
        capNhatTieuDe: capNhatTieuDe,
        batDongHo: startCountdown,
    };

    function khoiTaoTrang() {
        demoNeuCan();
        const params = new URLSearchParams(window.location.search);
        if (params.get('demo') === '1') return;
        if (elLoading) elLoading.style.display = 'none';
        if (elList && !elList.querySelector('.exam-question-card')) {
            elList.innerHTML =
                '<p class="exam-empty-hint">Chưa có dữ liệu đề. Sau khi gọi API, dùng ' +
                '<code style="background:#e2e8f0;padding:2px 6px;border-radius:4px;font-size:0.85em;">LamBaiThi.hienThiDeThi(meta, cauHoiList)</code> ' +
                'để hiển thị câu hỏi.</p>';
        }
    }

    document.addEventListener('DOMContentLoaded', khoiTaoTrang);
})();

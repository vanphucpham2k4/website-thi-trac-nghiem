/**
 * Trang /thi-mo/{mã}: nhập họ tên → bắt đầu thi ẩn danh (JWT lamBaiToken).
 */
function escHtml(s) {
    return String(s || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function hienThiLoi(message) {
    document.getElementById('thiMoLoading').style.display = 'none';
    const el = document.getElementById('thiMoError');
    el.style.display = 'block';
    el.innerHTML = `<div class="thi-mo-err">${escHtml(message || 'Không tải được đề thi.')}</div>`;
}

document.addEventListener('DOMContentLoaded', () => {
    const maEl = document.getElementById('maTruyCapHidden');
    const ma = (maEl && maEl.value) ? maEl.value.trim() : '';
    if (!ma) {
        hienThiLoi('Liên kết không hợp lệ.');
        return;
    }
    sessionStorage.setItem('thiMoMaTruyCap', ma);

    const btn = document.getElementById('btnBatDauThiMo');
    const inputHoTen = document.getElementById('hoTenAnDanh');

    (async () => {
        try {
            const res = await fetch(`/api/public/de-thi-link/${encodeURIComponent(ma)}/thong-tin`);
            const json = await res.json();
            if (!json.success || !json.data) {
                hienThiLoi(json.message || 'Không tìm thấy đề hoặc link đã bị thu hồi.');
                return;
            }
            const thongTin = json.data;
            document.getElementById('thiMoLoading').style.display = 'none';
            document.getElementById('thiMoContent').style.display = 'block';
            document.getElementById('thiMoTenDe').textContent = thongTin.tenDeThi || 'Đề thi';
            const phut = thongTin.thoiGianPhut != null ? thongTin.thoiGianPhut : 60;
            const soCau = thongTin.soCauHoi != null ? thongTin.soCauHoi : 0;
            const mon = thongTin.tenMonHoc || '—';
            document.getElementById('thiMoMeta').innerHTML =
                `<strong>Môn:</strong> ${escHtml(mon)}<br>` +
                `<strong>Thời gian:</strong> ${phut} phút · <strong>Số câu:</strong> ${soCau}`;
        } catch (e) {
            console.error(e);
            hienThiLoi('Lỗi kết nối máy chủ.');
        }
    })();

    btn?.addEventListener('click', async () => {
        const hoTen = (inputHoTen?.value || '').trim();
        if (hoTen.length < 2) {
            alert('Vui lòng nhập họ và tên (ít nhất 2 ký tự).');
            inputHoTen?.focus();
            return;
        }
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang mở đề…';
        try {
            const res = await fetch(`/api/public/de-thi-link/${encodeURIComponent(ma)}/bat-dau-an-danh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ hoTen })
            });
            const json = await res.json();
            if (!json.success || !json.data) {
                alert(json.message || 'Không thể bắt đầu làm bài.');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-play"></i> Bắt đầu thi';
                return;
            }
            const d = json.data;
            sessionStorage.setItem('lamBaiToken', d.lamBaiToken);
            sessionStorage.setItem('lamBaiTokenExpiresAt', String(d.expiresAt != null ? d.expiresAt : 0));
            sessionStorage.setItem('lamBaiHoTen', hoTen);
            window.location.href = '/thi-mo/lam-bai/' + encodeURIComponent(d.phienThiId);
        } catch (e) {
            console.error(e);
            alert('Lỗi kết nối.');
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-play"></i> Bắt đầu thi';
        }
    });
});

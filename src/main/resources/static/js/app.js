const SarprasApp = (() => {
  const API_BASE = '/api';
  const USER_STORAGE_KEY = 'sarpraskost_user';

  function getCurrentUser() {
    try {
      const raw = localStorage.getItem(USER_STORAGE_KEY);
      if (!raw) return null;
      const user = JSON.parse(raw);
      return user && user.id ? user : null;
    } catch {
      return null;
    }
  }

  function saveUser(user) {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  }

  function logout() {
    localStorage.removeItem(USER_STORAGE_KEY);
    window.location.href = 'login.html';
  }

  function getDashboardUrlByRole(role) {
    const normalized = (role || '').toUpperCase();
    if (normalized === 'OWNER') return 'owner-home.html';
    if (normalized === 'MEKANIK') return 'mekanik-home.html';
    return 'home.html';
  }

  function requireAuth() {
    const user = getCurrentUser();
    if (!user) {
      window.location.href = 'login.html';
      return null;
    }
    return user;
  }

  function requireOwnerAuth() {
    const user = getCurrentUser();
    if (!user) {
      window.location.href = 'login.html';
      return null;
    }
    if ((user.role || '').toUpperCase() !== 'OWNER') {
      window.location.href = getDashboardUrlByRole(user.role);
      return null;
    }
    return user;
  }

  function requireMekanikAuth() {
    const user = getCurrentUser();
    if (!user) {
      window.location.href = 'login.html';
      return null;
    }
    if ((user.role || '').toUpperCase() !== 'MEKANIK') {
      window.location.href = getDashboardUrlByRole(user.role);
      return null;
    }
    return user;
  }

  function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function getGreetingByHour() {
    const hour = new Date().getHours();
    if (hour < 11) return 'Selamat pagi';
    if (hour < 15) return 'Selamat siang';
    if (hour < 18) return 'Selamat sore';
    return 'Selamat malam';
  }

  function formatDateIndo(isoString) {
    if (!isoString) return '-';
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleDateString('id-ID', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  function formatDateTimeIndo(isoString) {
    if (!isoString) return '-';
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return '-';
    const datePart = date.toLocaleDateString('id-ID', {
      day: 'numeric',
      month: 'short'
    });
    const hours = String(date.getHours()).padStart(2, '0');
    const mins = String(date.getMinutes()).padStart(2, '0');
    return `${datePart}, ${hours}.${mins}`;
  }

  function deriveTitle(laporan) {
    const desc = (laporan.deskripsi || '').trim();
    if (!desc) return laporan.kategori_fasilitas || 'Laporan Kerusakan';
    const dotIdx = desc.indexOf('.');
    const firstSentence = dotIdx > 0 && dotIdx <= 56 ? desc.slice(0, dotIdx) : desc;
    return firstSentence.length > 56 ? firstSentence.slice(0, 53) + '...' : firstSentence;
  }

  function getStatusMeta(status) {
    switch ((status || '').toUpperCase()) {
      case 'DIPROSES':
        return { label: 'Diproses', className: 'status-diproses' };
      case 'SELESAI':
        return { label: 'Selesai', className: 'status-selesai' };
      case 'DITOLAK':
        return { label: 'Ditolak', className: 'status-ditolak' };
      case 'MENUNGGU':
      default:
        return { label: 'Menunggu', className: 'status-menunggu' };
    }
  }

  function getCategoryIconSvg(kategori) {
    const k = (kategori || '').toLowerCase();
    if (k.includes('air') || k.includes('sanitasi') || k.includes('kamar mandi')) {
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M10.4665 5.49932C11.0397 4.5836 11.446 3.57353 11.6665 2.51599C12.0832 4.59932 13.3332 6.59932 14.9999 7.93266C16.6665 9.26599 17.4999 10.8493 17.4999 12.516C17.5046 13.6679 17.1673 14.7953 16.5305 15.7552C15.8938 16.7152 14.9864 17.4645 13.9233 17.9081C12.8602 18.3517 11.6894 18.4697 10.5592 18.2471C9.42898 18.0244 8.39031 17.4713 7.57487 16.6577M5.83333 13.5829C7.66667 13.5829 9.16667 12.0579 9.16667 10.2079C9.16667 9.24122 8.69167 8.32455 7.74167 7.54955C6.79167 6.77455 6.075 5.62455 5.83333 4.41622C5.59167 5.62455 4.88333 6.78289 3.925 7.54955C2.96667 8.31622 2.5 9.24955 2.5 10.2079C2.5 12.0579 4 13.5829 5.83333 13.5829Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>`;
    }
    if (k.includes('listrik') || k.includes('lampu') || k.includes('pencahayaan')) {
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12.5 11.6668C12.6667 10.8334 13.0833 10.25 13.75 9.58328C14.5833 8.83322 15 7.7498 15 6.66638C15 5.3402 14.4732 4.06833 13.5355 3.13057C12.5979 2.19281 11.3261 1.66599 10 1.66599C8.67392 1.66599 7.40215 2.19281 6.46447 3.13057C5.52678 4.06833 5 5.3402 5 6.66638C5 7.49978 5.16667 8.49986 6.25 9.58328C6.83333 10.1667 7.33333 10.8334 7.5 11.6668M7.5 15.0004H12.5M8.33333 18.334H11.6667" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>`;
    }
    if (k.includes('pintu') || k.includes('kunci') || k.includes('bangunan') || k.includes('furnitur')) {
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M9.16662 16.6675H4.99962M9.16662 16.6675V17.2669C9.16665 17.3935 9.19552 17.5184 9.25104 17.6321C9.30656 17.7459 9.38727 17.8455 9.48704 17.9234C9.58681 18.0013 9.70302 18.0555 9.82685 18.0818C9.95068 18.1081 10.0789 18.1058 10.2017 18.0752L15.8338 16.6677M9.16662 16.6675V3.80243C9.16678 3.54925 9.22463 3.29859 9.33577 3.0711C9.44691 2.84361 9.60843 2.64442 9.80805 2.48867C10.0077 2.33292 10.2402 2.22469 10.4879 2.1722C10.7356 2.11971 10.992 2.12434 11.2376 2.18574L14.5712 3.01908C14.9318 3.10921 15.2519 3.31728 15.4807 3.61021C15.7095 3.90313 15.8338 4.26412 15.8338 4.63578V16.6677M4.99962 16.6675H1.66602M4.99962 16.6675V5.00063C4.99962 4.55859 5.17522 4.13466 5.48781 3.82209C5.8004 3.50952 6.22435 3.33393 6.66642 3.33393H9.16662M15.8338 16.6677L18.334 16.6675M11.6668 10.0007H11.6751" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>`;
    }
    return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" transform="scale(0.82) translate(1,1)"/>
    </svg>`;
  }

  function renderReportCard(laporan) {
    const statusMeta = getStatusMeta(laporan.status);
    const title = deriveTitle(laporan);
    const tanggal = formatDateIndo(laporan.tanggal_lapor);
    return `
      <a class="report-card" href="detail-laporan.html?id=${encodeURIComponent(laporan.id)}">
        <div class="report-card-top">
          <span class="report-code">${escapeHtml(laporan.kode_laporan)}</span>
          <span class="status-badge ${statusMeta.className}">
            <span class="status-dot"></span>
            ${escapeHtml(statusMeta.label)}
          </span>
        </div>
        <div class="report-card-body">
          <div class="report-cat-icon">
            ${getCategoryIconSvg(laporan.kategori_fasilitas)}
          </div>
          <div class="report-card-text">
            <div class="report-title">${escapeHtml(title)}</div>
            <div class="report-sub">${escapeHtml(laporan.lokasi_kamar)} &bull; ${escapeHtml(tanggal)}</div>
          </div>
          <svg width="17" height="17" viewBox="0 0 17 17" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6.375 12.75L10.625 8.5L6.375 4.25" stroke="#94A3B8" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </a>
    `;
  }

  function attachSheetControls() {
    document.querySelectorAll('[data-close-sheet]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const targetId = btn.getAttribute('data-close-sheet');
        const el = document.getElementById(targetId);
        if (el) el.classList.remove('open');
      });
    });

    document.querySelectorAll('.sheet-backdrop').forEach((backdrop) => {
      backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) backdrop.classList.remove('open');
      });
    });
  }

  function openSheet(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('open');
  }

  function closeSheet(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('open');
  }

  function getKondisiMeta(kondisi) {
    switch ((kondisi || '').toUpperCase()) {
      case 'PERLU_PERAWATAN':
        return { label: 'Perlu Perawatan', className: 'status-menunggu' };
      case 'RUSAK':
        return { label: 'Rusak', className: 'status-ditolak' };
      case 'BAIK':
      default:
        return { label: 'Baik', className: 'status-selesai' };
    }
  }

  async function requestJson(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }
    const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
    const text = await res.text();
    let data = {};
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = { message: text };
      }
    }
    if (!res.ok) {
      throw new Error(data.message || `Permintaan gagal (${res.status}).`);
    }
    return data;
  }

  function populateAccountSheet(user) {
    const nameEl = document.getElementById('akunNama');
    const emailEl = document.getElementById('akunEmail');
    const kamarEl = document.getElementById('akunKamar');
    const roleEl = document.getElementById('akunRole');
    if (nameEl) nameEl.textContent = user.nama || '-';
    if (emailEl) emailEl.textContent = user.email || '-';
    if (kamarEl) kamarEl.textContent = user.nomor_kamar ? `Kamar ${user.nomor_kamar}` : '-';
    if (roleEl) roleEl.textContent = user.role || 'PENGHUNI';

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', logout);
    }
  }

  function populateNotifSheet(reports) {
    const container = document.getElementById('notifList');
    if (!container) return;
    if (!reports || reports.length === 0) {
      container.innerHTML = '<div class="empty-state">Belum ada pembaruan laporan.</div>';
      return;
    }
    container.innerHTML = reports.slice(0, 4).map((item) => {
      const meta = getStatusMeta(item.status);
      const timeRef = item.tanggal_selesai || item.tanggal_diproses || item.tanggal_lapor;
      return `
        <a href="detail-laporan.html?id=${encodeURIComponent(item.id)}" class="report-card" style="padding:12px;">
          <div class="report-card-top">
            <span class="report-code">${escapeHtml(item.kode_laporan)}</span>
            <span class="status-badge ${meta.className}"><span class="status-dot"></span>${escapeHtml(meta.label)}</span>
          </div>
          <div style="font-size:13px;font-weight:600;color:#172554;">${escapeHtml(deriveTitle(item))}</div>
          <div style="font-size:11px;color:#64748B;">${escapeHtml(item.catatan_penanganan || 'Menunggu verifikasi pengelola kost.')} &bull; ${escapeHtml(formatDateTimeIndo(timeRef))}</div>
        </a>
      `;
    }).join('');
  }

  async function fetchPenghuniLaporan(penghuniId, status = '', query = '') {
    const params = new URLSearchParams();
    if (status && status !== 'SEMUA') params.set('status', status);
    if (query && query.trim()) params.set('q', query.trim());
    const qs = params.toString();
    const url = `${API_BASE}/laporan/penghuni/${encodeURIComponent(penghuniId)}${qs ? '?' + qs : ''}`;
    const res = await fetch(url);
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Gagal memuat data laporan.');
    }
    return data;
  }

  async function fetchOwnerDashboard() {
    return requestJson('/owner/dashboard');
  }

  async function fetchOwnerLaporan(status = '', query = '') {
    const params = new URLSearchParams();
    if (status && status !== 'SEMUA') params.set('status', status);
    if (query && query.trim()) params.set('q', query.trim());
    const qs = params.toString();
    return requestJson(`/owner/laporan${qs ? '?' + qs : ''}`);
  }

  async function fetchMekanikList() {
    return requestJson('/users/mekanik');
  }

  async function fetchMekanikDashboard(mekanikId) {
    return requestJson(`/mekanik/${encodeURIComponent(mekanikId)}/dashboard`);
  }

  async function fetchMekanikTugas(mekanikId, status = '', query = '') {
    const params = new URLSearchParams();
    if (status && status !== 'SEMUA') params.set('status', status);
    if (query && query.trim()) params.set('q', query.trim());
    const qs = params.toString();
    return requestJson(`/mekanik/${encodeURIComponent(mekanikId)}/tugas${qs ? '?' + qs : ''}`);
  }

  function getPriorityMeta(prioritas) {
    const p = (prioritas || '').trim().toLowerCase();
    if (p === 'darurat' || p === 'mendesak' || p === 'tinggi') {
      return { label: prioritas, className: 'high' };
    }
    if (p === 'sedang') {
      return { label: prioritas, className: 'medium' };
    }
    return { label: prioritas || 'Rendah', className: 'low' };
  }

  function renderAssignedReportCard(laporan, detailPage, extraLineHtml) {
    const statusMeta = getStatusMeta(laporan.status);
    const title = deriveTitle(laporan);
    const tanggal = formatDateIndo(laporan.tanggal_lapor);
    const residentLabel = laporan.nama_penghuni
      ? `${laporan.nama_penghuni}${laporan.nomor_kamar_penghuni ? ' (Kamar ' + laporan.nomor_kamar_penghuni + ')' : ''}`
      : laporan.lokasi_kamar;
    const priorityHtml = laporan.prioritas
      ? `<span class="priority-pill ${getPriorityMeta(laporan.prioritas).className}">${escapeHtml(laporan.prioritas)}</span>`
      : '';

    return `
      <a class="report-card" href="${detailPage}?id=${encodeURIComponent(laporan.id)}">
        <div class="report-card-top">
          <div style="display:flex;align-items:center;gap:6px;min-width:0;">
            <span class="report-code">${escapeHtml(laporan.kode_laporan)}</span>
            ${priorityHtml}
          </div>
          <span class="status-badge ${statusMeta.className}">
            <span class="status-dot"></span>
            ${escapeHtml(statusMeta.label)}
          </span>
        </div>
        <div class="report-card-body">
          <div class="report-cat-icon">
            ${getCategoryIconSvg(laporan.kategori_fasilitas)}
          </div>
          <div class="report-card-text">
            <div class="report-title">${escapeHtml(title)}</div>
            <div class="report-sub">${escapeHtml(residentLabel)} &bull; ${escapeHtml(tanggal)}</div>
            ${extraLineHtml || ''}
          </div>
          <svg width="17" height="17" viewBox="0 0 17 17" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6.375 12.75L10.625 8.5L6.375 4.25" stroke="#94A3B8" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </a>
    `;
  }

  function renderOwnerReportCard(laporan) {
    const mechanicLine = laporan.nama_mekanik
      ? `<div class="report-sub" style="color:#2563EB;font-weight:500;">Teknisi: ${escapeHtml(laporan.nama_mekanik)}</div>`
      : '';
    return renderAssignedReportCard(laporan, 'owner-detail-laporan.html', mechanicLine);
  }

  function renderMekanikTaskCard(laporan) {
    const locationLine = laporan.lokasi_kamar
      ? `<div class="report-sub" style="color:#2563EB;font-weight:500;">Lokasi: ${escapeHtml(laporan.lokasi_kamar)}</div>`
      : '';
    return renderAssignedReportCard(laporan, 'mekanik-detail-tugas.html', locationLine);
  }

  function populateRoleNotifSheet(reports, detailPage, emptyText) {
    const container = document.getElementById('notifList');
    if (!container) return;
    if (!reports || reports.length === 0) {
      container.innerHTML = `<div class="empty-state">${escapeHtml(emptyText)}</div>`;
      return;
    }
    container.innerHTML = reports.slice(0, 5).map((item) => {
      const meta = getStatusMeta(item.status);
      const timeRef = item.tanggal_selesai || item.tanggal_diproses || item.tanggal_lapor;
      const roomInfo = item.nomor_kamar_penghuni ? `Kamar ${item.nomor_kamar_penghuni}` : item.lokasi_kamar;
      return `
        <a href="${detailPage}?id=${encodeURIComponent(item.id)}" class="report-card" style="padding:12px;">
          <div class="report-card-top">
            <span class="report-code">${escapeHtml(item.kode_laporan)}</span>
            <span class="status-badge ${meta.className}"><span class="status-dot"></span>${escapeHtml(meta.label)}</span>
          </div>
          <div style="font-size:13px;font-weight:600;color:#172554;">${escapeHtml(deriveTitle(item))}</div>
          <div style="font-size:11px;color:#64748B;">${escapeHtml(item.nama_penghuni || 'Penghuni')} &bull; ${escapeHtml(roomInfo || '-')} &bull; ${escapeHtml(formatDateTimeIndo(timeRef))}</div>
        </a>
      `;
    }).join('');
  }

  function populateOwnerNotifSheet(reports) {
    populateRoleNotifSheet(reports, 'owner-detail-laporan.html', 'Belum ada laporan masuk.');
  }

  function populateMekanikNotifSheet(reports) {
    populateRoleNotifSheet(reports, 'mekanik-detail-tugas.html', 'Belum ada tugas yang ditugaskan.');
  }

  function populateRoleAccountSheet(user, defaultRoleLabel) {
    const nameEl = document.getElementById('akunNama');
    const emailEl = document.getElementById('akunEmail');
    const roleEl = document.getElementById('akunRole');
    const specEl = document.getElementById('akunSpesialisasi');
    if (nameEl) nameEl.textContent = user.nama || '-';
    if (emailEl) emailEl.textContent = user.email || '-';
    if (roleEl) roleEl.textContent = user.role || defaultRoleLabel;
    if (specEl) specEl.textContent = user.nomor_kamar || 'Teknisi Kost Cendana';

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.onclick = logout;
    }
  }

  function populateOwnerAccountSheet(user) {
    populateRoleAccountSheet(user, 'OWNER');
  }

  function populateMekanikAccountSheet(user) {
    populateRoleAccountSheet(user, 'MEKANIK');
  }

  return {
    API_BASE,
    getCurrentUser,
    saveUser,
    logout,
    getDashboardUrlByRole,
    requireAuth,
    requireOwnerAuth,
    requireMekanikAuth,
    escapeHtml,
    getGreetingByHour,
    formatDateIndo,
    formatDateTimeIndo,
    deriveTitle,
    getStatusMeta,
    getCategoryIconSvg,
    renderReportCard,
    attachSheetControls,
    openSheet,
    closeSheet,
    getKondisiMeta,
    requestJson,
    populateAccountSheet,
    populateNotifSheet,
    fetchPenghuniLaporan,
    fetchOwnerDashboard,
    fetchOwnerLaporan,
    fetchMekanikList,
    fetchMekanikDashboard,
    fetchMekanikTugas,
    getPriorityMeta,
    renderOwnerReportCard,
    renderMekanikTaskCard,
    populateOwnerNotifSheet,
    populateMekanikNotifSheet,
    populateOwnerAccountSheet,
    populateMekanikAccountSheet
  };
})();

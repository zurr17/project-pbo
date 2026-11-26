const SarprasApp = (() => {
  const API_BASE = '/api';
  const USER_STORAGE_KEY = 'sarpraskost_user';

  function getCurrentUser() {
    try {
      const raw = localStorage.getItem(USER_STORAGE_KEY);
      if (!raw) return null;
      const user = JSON.parse(raw);
      return user && user.id_pengguna ? user : null;
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
    if (normalized === 'OWNER' || normalized === 'PENGELOLA') return 'owner-home.html';
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

  function requirePenghuniAuth() {
    const user = requireAuth();
    if (!user) return null;
    if ((user.role || '').toUpperCase() !== 'PENGHUNI') {
      window.location.href = getDashboardUrlByRole(user.role);
      return null;
    }
    return user;
  }

  function requireOwnerAuth() {
    const user = requireAuth();
    if (!user) return null;
    const roleUpper = (user.role || '').toUpperCase();
    if (roleUpper !== 'OWNER' && roleUpper !== 'PENGELOLA') {
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

  function formatRupiah(value) {
    if (value === null || value === undefined || value === '') return '-';
    const num = Number(value);
    if (Number.isNaN(num)) return '-';
    return 'Rp ' + num.toLocaleString('id-ID');
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

  function getKondisiMeta(kondisi) {
    switch ((kondisi || '').toUpperCase().replace(/\s+/g, '_')) {
      case 'RUSAK_RINGAN':
      case 'PERLU_PERAWATAN':
        return { label: 'Rusak Ringan', className: 'status-menunggu' };
      case 'DALAM_PERBAIKAN':
        return { label: 'Dalam Perbaikan', className: 'status-diproses' };
      case 'RUSAK_BERAT':
      case 'RUSAK':
        return { label: 'Rusak Berat', className: 'status-ditolak' };
      case 'BAIK':
      default:
        return { label: 'Baik', className: 'status-selesai' };
    }
  }

  function getUrgensiMeta(urgensi, isUrgent) {
    const key = isUrgent ? 'DARURAT' : (urgensi || 'SEDANG').toUpperCase();
    switch (key) {
      case 'DARURAT':
        return { label: 'DARURAT', className: 'high' };
      case 'TINGGI':
        return { label: 'Tinggi', className: 'high' };
      case 'RENDAH':
        return { label: 'Rendah', className: 'low' };
      case 'SEDANG':
      default:
        return { label: 'Sedang', className: 'medium' };
    }
  }

  function getStatusHunianMeta(status) {
    switch ((status || '').toUpperCase()) {
      case 'TERISI':
        return { label: 'Terisi', className: 'status-diproses' };
      case 'PERBAIKAN':
        return { label: 'Perbaikan', className: 'status-menunggu' };
      case 'PEMBANGUNAN':
        return { label: 'On Progress Pembangunan', className: 'status-menunggu' };
      case 'KOSONG':
      default:
        return { label: 'Kosong', className: 'status-selesai' };
    }
  }

  function getCategoryIconSvg(kategori) {
    const k = (kategori || '').toLowerCase();
    if (k.includes('air') || k.includes('sanitasi') || k.includes('kamar mandi')) {
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M10.4665 5.49932C11.0397 4.5836 11.446 3.57353 11.6665 2.51599C12.0832 4.59932 13.3332 6.59932 14.9999 7.93266C16.6665 9.26599 17.4999 10.8493 17.4999 12.516C17.5046 13.6679 17.1673 14.7953 16.5305 15.7552C15.8938 16.7152 14.9864 17.4645 13.9233 17.9081C12.8602 18.3517 11.6894 18.4697 10.5592 18.2471C9.42898 18.0244 8.39031 17.4713 7.57487 16.6577M5.83333 13.5829C7.66667 13.5829 9.16667 12.0579 9.16667 10.2079C9.16667 9.24122 8.69167 8.32455 7.74167 7.54955C6.79167 6.77455 6.075 5.62455 5.83333 4.41622C5.59167 5.62455 4.88333 6.78289 3.925 7.54955C2.96667 8.31622 2.5 9.24955 2.5 10.2079C2.5 12.0579 4 13.5829 5.83333 13.5829Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>`;
    }
    if (k.includes('elektronik') || k.includes('listrik') || k.includes('lampu') || k.includes('ac') || k.includes('wifi')) {
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
    const statusMeta = getStatusMeta(laporan.status_tiket);
    const statusUpper = (laporan.status_tiket || '').toUpperCase();
    const isActiveUrgent = laporan.is_urgent && (statusUpper === 'MENUNGGU' || statusUpper === 'DIPROSES');
    const tanggal = formatDateIndo(laporan.tgl_lapor);
    const lokasiLabel = laporan.is_umum
      ? 'Fasilitas Umum'
      : `${laporan.gedung ? laporan.gedung + ' \u2022 ' : ''}Kamar ${laporan.nomor_kamar || '-'}${laporan.lantai ? ' \u2022 Lt. ' + laporan.lantai : ''}`;
    const assetLine = `${laporan.nama_fasilitas || '-'} \u2022 ${lokasiLabel}`;
    const urgentPill = laporan.is_urgent
      ? '<span class="priority-pill high">DARURAT</span>'
      : '';
    const terdampakCount = Number(laporan.jumlah_terdampak || 1);
    const metaTags = [];
    if (laporan.is_umum || terdampakCount > 1) {
      metaTags.push(`<span class="meta-chip-flat info">${terdampakCount} terdampak</span>`);
    }
    if (!laporan.is_umum && laporan.is_urgent) {
      metaTags.push(
        laporan.izin_masuk_kamar
          ? '<span class="meta-chip-flat ok">Izin Masuk: Disetujui</span>'
          : '<span class="meta-chip-flat warn">Tanpa Izin Masuk</span>'
      );
    }
    if (laporan.jadwal_pengerjaan) {
      metaTags.push(`<span class="meta-chip-flat info">Jadwal: ${escapeHtml(formatDateTimeIndo(laporan.jadwal_pengerjaan))} WIB</span>`);
    }
    const metaRow = metaTags.length > 0 ? `<div class="card-meta-line">${metaTags.join('')}</div>` : '';

    const riwayatList = Array.isArray(laporan.riwayat) ? laporan.riwayat : [];
    const previewSteps = riwayatList.slice(-2);
    const miniTimelineHtml = previewSteps.length > 0
      ? `
        <div class="card-mini-timeline">
          ${previewSteps.map((step) => {
            const st = (step.status_baru || 'MENUNGGU').toUpperCase();
            const dotClass = st === 'SELESAI' ? 'done' : (st === 'DITOLAK' ? 'rejected' : (st === 'MENUNGGU' ? 'wait' : ''));
            const stLabel = getStatusMeta(st).label;
            return `
              <div class="mini-tl-item">
                <span class="mini-tl-dot ${dotClass}"></span>
                <div class="mini-tl-body">
                  <div class="mini-tl-head">
                    <span>${escapeHtml(stLabel)}</span>
                    <span class="mini-tl-time">${escapeHtml(formatDateTimeIndo(step.waktu_update))}</span>
                  </div>
                  ${step.catatan_penanganan ? `<div class="mini-tl-note">${escapeHtml(step.catatan_penanganan)}</div>` : ''}
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `
      : '';

    return `
      <a class="report-card ${isActiveUrgent ? 'urgent-card' : ''}" href="detail-laporan.html?id=${encodeURIComponent(laporan.id_laporan)}">
        <div class="report-card-top">
          <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap;min-width:0;">
            <span class="report-code">${escapeHtml(laporan.kode_tiket)}</span>
            ${urgentPill}
          </div>
          <span class="status-badge ${statusMeta.className}">
            <span class="status-dot"></span>
            ${escapeHtml(statusMeta.label)}
          </span>
        </div>
        <div class="report-card-body">
          <div class="report-cat-icon" aria-hidden="true">
            ${getCategoryIconSvg(laporan.nama_kategori)}
          </div>
          <div class="report-card-text">
            <div class="report-title">${escapeHtml(laporan.judul_laporan)}</div>
            <div class="report-sub">${escapeHtml(assetLine)} &bull; ${escapeHtml(tanggal)}</div>
            ${metaRow}
          </div>
          <svg width="17" height="17" viewBox="0 0 17 17" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path d="M6.375 12.75L10.625 8.5L6.375 4.25" stroke="#526075" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        ${miniTimelineHtml}
      </a>
    `;
  }

  function renderOwnerReportCard(laporan) {
    const statusMeta = getStatusMeta(laporan.status_tiket);
    const statusUpper = (laporan.status_tiket || '').toUpperCase();
    const isActiveUrgent = laporan.is_urgent && (statusUpper === 'MENUNGGU' || statusUpper === 'DIPROSES');
    const tanggal = formatDateIndo(laporan.tgl_lapor);
    const kamarLabel = laporan.is_umum
      ? 'Fasilitas Umum'
      : `${laporan.gedung ? laporan.gedung + ' \u2022 ' : ''}Kamar ${laporan.nomor_kamar || '-'}${laporan.lantai ? ' \u2022 Lt. ' + laporan.lantai : ''}`;
    const pelapor = laporan.nama_pelapor
      ? `${laporan.nama_pelapor} (${kamarLabel})`
      : kamarLabel;

    const urgentPill = laporan.is_urgent
      ? '<span class="priority-pill high">DARURAT</span>'
      : (String(laporan.tingkat_urgensi || '').toUpperCase() === 'TINGGI'
          ? '<span class="priority-pill medium">Prioritas Tinggi</span>'
          : '');
    const terdampakCount = Number(laporan.jumlah_terdampak || 1);
    const metaTags = [];
    if (laporan.is_umum || terdampakCount > 1) {
      metaTags.push(`<span class="meta-chip-flat info">${terdampakCount} terdampak</span>`);
    }
    if (!laporan.is_umum && (laporan.is_urgent || laporan.izin_masuk_kamar)) {
      metaTags.push(
        laporan.izin_masuk_kamar
          ? '<span class="meta-chip-flat ok">Izin Masuk: Disetujui</span>'
          : '<span class="meta-chip-flat warn">Tanpa Izin Masuk</span>'
      );
    }
    if (laporan.biaya_perbaikan && Number(laporan.biaya_perbaikan) > 0) {
      metaTags.push(`<span class="meta-chip-flat">Biaya Internal: ${escapeHtml(formatRupiah(laporan.biaya_perbaikan))}</span>`);
    }
    if (laporan.jadwal_pengerjaan) {
      metaTags.push(`<span class="meta-chip-flat info">Jadwal: ${escapeHtml(formatDateTimeIndo(laporan.jadwal_pengerjaan))} WIB</span>`);
    }
    const metaRow = metaTags.length > 0 ? `<div class="card-meta-line">${metaTags.join('')}</div>` : '';

    return `
      <a class="report-card ${isActiveUrgent ? 'urgent-card' : ''}" href="owner-detail-laporan.html?id=${encodeURIComponent(laporan.id_laporan)}">
        <div class="report-card-top">
          <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap;min-width:0;">
            <span class="report-code">${escapeHtml(laporan.kode_tiket)}</span>
            ${urgentPill}
          </div>
          <span class="status-badge ${statusMeta.className}">
            <span class="status-dot"></span>
            ${escapeHtml(statusMeta.label)}
          </span>
        </div>
        <div class="report-card-body">
          <div class="report-cat-icon" aria-hidden="true">
            ${getCategoryIconSvg(laporan.nama_kategori)}
          </div>
          <div class="report-card-text">
            <div class="report-title">${escapeHtml(laporan.judul_laporan)}</div>
            <div class="report-sub">${escapeHtml(pelapor)} &bull; ${escapeHtml(tanggal)}</div>
            <div class="report-sub" style="color:#1D4ED8;font-weight:500;">${escapeHtml(laporan.nama_fasilitas || '-')} (${escapeHtml(laporan.kode_aset || '-')})</div>
            ${metaRow}
          </div>
          <svg width="17" height="17" viewBox="0 0 17 17" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path d="M6.375 12.75L10.625 8.5L6.375 4.25" stroke="#526075" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </a>
    `;
  }

  function renderRiwayatTimeline(riwayatList, optionsOrEmptyText = {}) {
    const opts = typeof optionsOrEmptyText === 'string'
      ? { emptyText: optionsOrEmptyText }
      : (optionsOrEmptyText || {});
    const emptyText = opts.emptyText || 'Belum ada riwayat penanganan.';
    const showBiaya = Boolean(opts.showBiaya);

    if (!riwayatList || riwayatList.length === 0) {
      return `<div class="empty-state">${escapeHtml(emptyText)}</div>`;
    }
    const checkSvg = '<svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="M9.99969 3L4.50024 8.4996L2.00049 5.99978" stroke="#2563EB" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>';
    const closeSvg = '<svg width="11" height="11" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="M9 3L3 9M3 3L9 9" stroke="#B91C1C" stroke-width="1.6" stroke-linecap="round"/></svg>';

    return riwayatList.map((entry, index) => {
      const isLast = index === riwayatList.length - 1;
      const statusUpper = (entry.status_baru || 'MENUNGGU').toUpperCase();
      let dotState = 'done';
      let dotContent = checkSvg;
      if (statusUpper === 'DITOLAK') {
        dotState = 'rejected';
        dotContent = closeSvg;
      } else if (isLast && statusUpper !== 'SELESAI') {
        dotState = 'active';
        dotContent = '';
      }

      const statusLabelMap = {
        MENUNGGU: 'Laporan Dibuat (Menunggu)',
        DIPROSES: 'Diproses Pengelola',
        SELESAI: 'Perbaikan Selesai',
        DITOLAK: 'Laporan Ditolak'
      };
      const labelText = statusLabelMap[statusUpper] || statusUpper;
      const biayaHtml = showBiaya && entry.biaya_perbaikan && Number(entry.biaya_perbaikan) > 0
        ? `<span class="timeline-cost-pill">Biaya Internal: ${escapeHtml(formatRupiah(entry.biaya_perbaikan))}</span>`
        : '';
      const photoHtml = entry.foto_sesudah_url
        ? `<div class="photo-box has-image" style="height:126px;margin-top:6px;"><img class="photo-img" width="600" height="252" src="${escapeHtml(entry.foto_sesudah_url)}" alt="Foto sesudah perbaikan" onerror="this.closest('.photo-box').style.display='none'" /></div>`
        : '';

      return `
        <div class="timeline-item">
          <div class="timeline-rail">
            <div class="timeline-dot ${dotState}">${dotContent}</div>
            ${isLast ? '' : '<div class="timeline-line" style="min-height:28px;height:100%;"></div>'}
          </div>
          <div class="timeline-content" style="${isLast ? 'padding-bottom:0;' : ''}">
            <div class="timeline-top-row">
              <span class="timeline-label ${isLast ? 'bold' : ''}">${escapeHtml(labelText)}</span>
              <span class="timeline-time">${escapeHtml(formatDateTimeIndo(entry.waktu_update))}</span>
            </div>
            ${entry.catatan_penanganan ? `<div class="timeline-note">${escapeHtml(entry.catatan_penanganan)}</div>` : ''}
            <div class="timeline-meta">
              <span>Oleh: ${escapeHtml(entry.nama_pengguna || '-')} (${escapeHtml(entry.role_pengguna || '-')})</span>
              ${biayaHtml}
            </div>
            ${photoHtml}
          </div>
        </div>
      `;
    }).join('');
  }

  let lastSheetTrigger = null;

  function syncMobileShellSheetState() {
    const shell = document.querySelector('.mobile-shell');
    if (!shell) return;
    const anyOpen = Boolean(document.querySelector('.sheet-backdrop.open'));
    shell.classList.toggle('sheet-open', anyOpen);
  }

  function ensureGlobalSheetEscapeListener() {
    if (typeof document === 'undefined' || window.__sarprasSheetEscapeReady) return;
    window.__sarprasSheetEscapeReady = true;
    document.addEventListener('keydown', (e) => {
      if (e.key !== 'Escape') return;
      const openSheets = document.querySelectorAll('.sheet-backdrop.open');
      if (!openSheets.length) return;
      openSheets.forEach((sheet) => {
        if (sheet.id) {
          closeSheet(sheet.id);
        } else {
          sheet.classList.remove('open');
          syncMobileShellSheetState();
        }
      });
    });
  }

  function attachSheetControls() {
    ensureGlobalSheetEscapeListener();
    document.querySelectorAll('[data-close-sheet]').forEach((btn) => {
      if (btn.dataset.sheetCloseBound === '1') return;
      btn.dataset.sheetCloseBound = '1';
      btn.addEventListener('click', () => {
        const targetId = btn.getAttribute('data-close-sheet');
        closeSheet(targetId);
      });
    });

    document.querySelectorAll('.sheet-backdrop').forEach((backdrop) => {
      if (backdrop.dataset.sheetBackdropBound === '1') return;
      backdrop.dataset.sheetBackdropBound = '1';
      backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) {
          if (backdrop.id) closeSheet(backdrop.id);
          else {
            backdrop.classList.remove('open');
            syncMobileShellSheetState();
          }
        }
      });
    });
  }

  function openSheet(id) {
    ensureGlobalSheetEscapeListener();
    const el = document.getElementById(id);
    if (!el) return;
    const active = document.activeElement;
    if (active && typeof active.focus === 'function' && !el.contains(active)) {
      lastSheetTrigger = active;
    }
    el.classList.add('open');
    syncMobileShellSheetState();
    requestAnimationFrame(() => {
      const panel = el.querySelector('.sheet-panel');
      const focusTarget = el.querySelector('input:not([type="hidden"]):not(:disabled), select:not(:disabled), textarea:not(:disabled), button:not(:disabled), a[href]') || panel;
      if (focusTarget) {
        if (!focusTarget.hasAttribute('tabindex') && focusTarget === panel) {
          focusTarget.setAttribute('tabindex', '-1');
        }
        focusTarget.focus({ preventScroll: true });
      }
    });
  }

  function closeSheet(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('open');
    syncMobileShellSheetState();
    if (!document.querySelector('.sheet-backdrop.open') && lastSheetTrigger && typeof lastSheetTrigger.focus === 'function') {
      const triggerToFocus = lastSheetTrigger;
      lastSheetTrigger = null;
      requestAnimationFrame(() => triggerToFocus.focus({ preventScroll: true }));
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

  function populateOwnerAccountSheet(user) {
    const nameEl = document.getElementById('akunNama');
    const emailEl = document.getElementById('akunEmail');
    const hpEl = document.getElementById('akunNoHp');
    const roleEl = document.getElementById('akunRole');
    if (nameEl) nameEl.textContent = user.nama_lengkap || '-';
    if (emailEl) emailEl.textContent = user.email || '-';
    if (hpEl) hpEl.textContent = user.no_hp || '-';
    if (roleEl) roleEl.textContent = user.role || 'OWNER';

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
      logoutBtn.onclick = logout;
    }
  }

  async function updateBellUnreadBadge(idPengguna, badgeElId = 'bellUnreadBadge') {
    const badge = document.getElementById(badgeElId);
    if (!badge || !idPengguna) return;
    try {
      const data = await requestJson(`/notifikasi/${encodeURIComponent(idPengguna)}/unread-count`);
      const count = Number(data.unread || 0);
      if (count > 0) {
        const prevText = badge.textContent;
        badge.textContent = count > 99 ? '99+' : String(count);
        badge.classList.add('visible');
        if (prevText !== badge.textContent) {
          const btn = badge.closest('.icon-btn');
          if (btn) {
            btn.classList.remove('bell-ring');
            void btn.offsetWidth;
            btn.classList.add('bell-ring');
          }
        }
      } else {
        badge.textContent = '';
        badge.classList.remove('visible');
      }
    } catch {
      // ignore badge fetch errors
    }
  }

  async function fetchKamar() {
    return requestJson('/kamar');
  }

  async function fetchKategori() {
    return requestJson('/kategori');
  }

  async function fetchFasilitas(kamarId = '', kategoriId = '') {
    const params = new URLSearchParams();
    if (kamarId) params.set('kamarId', kamarId);
    if (kategoriId) params.set('kategoriId', kategoriId);
    const qs = params.toString();
    return requestJson(`/fasilitas${qs ? '?' + qs : ''}`);
  }

  async function fetchFasilitasByPenghuni(idPengguna) {
    return requestJson(`/fasilitas/penghuni/${encodeURIComponent(idPengguna)}`);
  }

  async function fetchActiveFasilitasUmum() {
    return requestJson('/laporan/fasilitas-umum/aktif');
  }

  async function tambahTerdampak(idLaporan) {
    return requestJson(`/laporan/${encodeURIComponent(idLaporan)}/terdampak`, { method: 'POST' });
  }

  async function fetchPenghuniLaporan(idPengguna, status = '', query = '') {
    const params = new URLSearchParams();
    if (status && status !== 'SEMUA') params.set('status', status);
    if (query && query.trim()) params.set('q', query.trim());
    const qs = params.toString();
    return requestJson(`/laporan/penghuni/${encodeURIComponent(idPengguna)}${qs ? '?' + qs : ''}`);
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

  async function fetchNotifikasi(idPengguna) {
    return requestJson(`/notifikasi/${encodeURIComponent(idPengguna)}`);
  }

  async function markNotifikasiRead(idNotif) {
    return requestJson(`/notifikasi/${encodeURIComponent(idNotif)}/baca`, { method: 'PUT' });
  }

  async function markAllNotifikasiRead(idPengguna) {
    return requestJson(`/notifikasi/baca-semua/${encodeURIComponent(idPengguna)}`, { method: 'PUT' });
  }

  function createSkeletonMarkup(count = 3) {
    return `<div class="skeleton-stack" role="status" aria-busy="true" aria-label="Memuat data">${Array.from({ length: count }).map(() => `
      <div class="skeleton-card" aria-hidden="true">
        <div class="skeleton-line w-40"></div>
        <div class="skeleton-line w-65"></div>
        <div class="skeleton-line w-85"></div>
      </div>
    `).join('')}<span class="sr-only">Memuat data...</span></div>`;
  }

  function upgradeLoadingStatesToSkeletons() {
    document.querySelectorAll('.loading-state').forEach((el) => {
      if (el.dataset.skeletonUpgraded === '1') return;
      el.dataset.skeletonUpgraded = '1';
      el.style.padding = '0';
      el.innerHTML = createSkeletonMarkup(el.closest('.report-list') ? 3 : 2);
    });
  }

  function initSegmentedPills() {
    document.querySelectorAll('.segment-tabs').forEach((container) => {
      if (container.dataset.pillReady === '1') return;
      container.dataset.pillReady = '1';
      container.classList.add('has-pill-indicator');

      const pill = document.createElement('span');
      pill.className = 'segment-pill-indicator';
      pill.setAttribute('aria-hidden', 'true');
      container.prepend(pill);

      const syncPill = () => {
        const active = container.querySelector('.segment-tab.active') || container.querySelector('.segment-tab');
        if (!active) return;
        const left = active.offsetLeft;
        const width = active.offsetWidth;
        if (width > 0) {
          pill.style.setProperty('--seg-x', `${left}px`);
          pill.style.setProperty('--seg-w', `${width}px`);
          pill.style.opacity = '1';
        }
      };

      requestAnimationFrame(syncPill);
      window.addEventListener('resize', syncPill);

      const observer = new MutationObserver(() => {
        requestAnimationFrame(syncPill);
      });
      container.querySelectorAll('.segment-tab').forEach((tab) => {
        observer.observe(tab, { attributes: true, attributeFilter: ['class'] });
        tab.addEventListener('click', () => requestAnimationFrame(syncPill));
      });
    });
  }

  function animateCardListStagger(container) {
    if (!container) return;
    const cards = container.querySelectorAll(
      ':scope > .report-card, :scope > .facility-card, :scope > .notif-card, :scope > .umum-alert-card, :scope > .timeline-item'
    );
    cards.forEach((card, index) => {
      if (index < 8) {
        card.style.setProperty('--stagger-i', String(index));
        card.classList.add('ios-enter-card');
      } else {
        card.classList.remove('ios-enter-card');
        card.style.removeProperty('--stagger-i');
      }
    });
  }

  function initPageTransitions() {
    if (typeof window === 'undefined' || window.__sarprasTransitionsReady) return;
    window.__sarprasTransitionsReady = true;

    const NAV_DIR_KEY = 'sarpraskost_nav_dir';
    const rootEl = document.documentElement;
    const reducedMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const supportsCrossDocVt = 'onpagereveal' in window;

    const clearVtClasses = () => {
      rootEl.classList.remove('vt-push', 'vt-back', 'vt-tab');
      const shell = document.querySelector('.mobile-shell');
      if (shell) {
        shell.classList.remove('ios-page-leaving', 'dir-back', 'dir-tab');
      }
    };

    try {
      const pendingDir = sessionStorage.getItem(NAV_DIR_KEY);
      if (pendingDir && ['push', 'back', 'tab'].includes(pendingDir)) {
        rootEl.classList.add(`vt-${pendingDir}`);
      }
    } catch {
      // ignore storage errors
    }

    if (supportsCrossDocVt) {
      window.addEventListener('pagereveal', (e) => {
        if (!e.viewTransition) {
          try { sessionStorage.removeItem(NAV_DIR_KEY); } catch {}
          clearVtClasses();
          return;
        }
        try {
          const dir = sessionStorage.getItem(NAV_DIR_KEY) || 'push';
          sessionStorage.removeItem(NAV_DIR_KEY);
          clearVtClasses();
          rootEl.classList.add(`vt-${dir}`);
        } catch {
          // ignore
        }
        if (e.viewTransition.ready) {
          e.viewTransition.ready.catch(() => {});
        }
        e.viewTransition.finished.catch(() => {}).finally(clearVtClasses);
      });

      if ('onpageswap' in window) {
        window.addEventListener('pageswap', (e) => {
          try {
            if (!sessionStorage.getItem(NAV_DIR_KEY)) {
              const navType = e.activation && e.activation.navigationType;
              const fallbackDir = navType === 'traverse' ? 'back' : 'push';
              sessionStorage.setItem(NAV_DIR_KEY, fallbackDir);
              rootEl.classList.remove('vt-push', 'vt-back', 'vt-tab');
              rootEl.classList.add(`vt-${fallbackDir}`);
            }
          } catch {
            // ignore
          }
        });
      }
    } else {
      clearVtClasses();
    }

    window.addEventListener('pageshow', (e) => {
      if (e.persisted) {
        try { sessionStorage.removeItem(NAV_DIR_KEY); } catch {}
      }
      setTimeout(clearVtClasses, 400);
    });

    document.addEventListener('click', (e) => {
      if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
        return;
      }
      const link = e.target.closest('a[href]');
      if (!link) return;

      const rawHref = link.getAttribute('href') || '';
      if (
        !rawHref ||
        rawHref.startsWith('#') ||
        rawHref.startsWith('javascript:') ||
        rawHref.startsWith('mailto:') ||
        rawHref.startsWith('tel:') ||
        link.target === '_blank' ||
        link.hasAttribute('download')
      ) {
        return;
      }

      let dest;
      try {
        dest = new URL(link.href, window.location.href);
      } catch {
        return;
      }
      if (dest.origin !== window.location.origin) return;

      if (dest.href === window.location.href) {
        e.preventDefault();
        window.scrollTo({ top: 0, behavior: reducedMotion ? 'auto' : 'smooth' });
        return;
      }

      let dir = 'push';
      if (link.closest('.bottom-nav')) {
        dir = 'tab';
      } else if (link.classList.contains('back-btn') || link.dataset.navDir === 'back') {
        dir = 'back';
      }

      try {
        sessionStorage.setItem(NAV_DIR_KEY, dir);
      } catch {
        // ignore
      }
      rootEl.classList.remove('vt-push', 'vt-back', 'vt-tab');
      rootEl.classList.add(`vt-${dir}`);

      if (supportsCrossDocVt || reducedMotion) {
        return;
      }

      const shell = document.querySelector('.mobile-shell');
      if (shell) {
        e.preventDefault();
        shell.classList.add('ios-page-leaving');
        if (dir === 'back') shell.classList.add('dir-back');
        if (dir === 'tab') shell.classList.add('dir-tab');
        setTimeout(() => {
          window.location.href = dest.href;
        }, 130);
      }
    });
  }

  function initDynamicMotionObservers() {
    initPageTransitions();
    attachSheetControls();
    upgradeLoadingStatesToSkeletons();
    initSegmentedPills();
    syncMobileShellSheetState();

    const listSelectors = '.report-list, .umum-banner-section, .timeline';
    document.querySelectorAll(listSelectors).forEach((listEl) => {
      animateCardListStagger(listEl);
      const listObserver = new MutationObserver(() => {
        upgradeLoadingStatesToSkeletons();
        animateCardListStagger(listEl);
      });
      listObserver.observe(listEl, { childList: true });
    });

    document.querySelectorAll('.sheet-backdrop').forEach((sheet) => {
      const sheetObserver = new MutationObserver(syncMobileShellSheetState);
      sheetObserver.observe(sheet, { attributes: true, attributeFilter: ['class'] });
    });

    ['panelKamar', 'panelKategori', 'panelFasilitas', 'fasilitasLainnyaGroup', 'assetPreview'].forEach((panelId) => {
      const panel = document.getElementById(panelId);
      if (!panel) return;
      const panelObserver = new MutationObserver(() => {
        if (panel.style.display && panel.style.display !== 'none') {
          panel.classList.remove('ios-reveal-panel');
          void panel.offsetWidth;
          panel.classList.add('ios-reveal-panel');
        }
      });
      panelObserver.observe(panel, { attributes: true, attributeFilter: ['style'] });
    });
  }

  if (typeof document !== 'undefined') {
    initPageTransitions();
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', initDynamicMotionObservers);
    } else {
      initDynamicMotionObservers();
    }
  }

  return {
    API_BASE,
    getCurrentUser,
    saveUser,
    logout,
    getDashboardUrlByRole,
    requireAuth,
    requirePenghuniAuth,
    requireOwnerAuth,
    escapeHtml,
    getGreetingByHour,
    formatDateIndo,
    formatDateTimeIndo,
    formatRupiah,
    getStatusMeta,
    getKondisiMeta,
    getUrgensiMeta,
    getStatusHunianMeta,
    getCategoryIconSvg,
    renderReportCard,
    renderOwnerReportCard,
    renderRiwayatTimeline,
    attachSheetControls,
    openSheet,
    closeSheet,
    requestJson,
    populateOwnerAccountSheet,
    updateBellUnreadBadge,
    fetchKamar,
    fetchKategori,
    fetchFasilitas,
    fetchFasilitasByPenghuni,
    fetchActiveFasilitasUmum,
    tambahTerdampak,
    fetchPenghuniLaporan,
    fetchOwnerDashboard,
    fetchOwnerLaporan,
    fetchNotifikasi,
    markNotifikasiRead,
    markAllNotifikasiRead
  };
})();

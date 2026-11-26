const CACHE_NAME = 'sarpraskost-pwa-v3';
const PRECACHE_URLS = [
  '/',
  '/index.html',
  '/login.html',
  '/register.html',
  '/home.html',
  '/buat-laporan.html',
  '/detail-laporan.html',
  '/fasilitas.html',
  '/notifikasi.html',
  '/profil.html',
  '/riwayat.html',
  '/sukses.html',
  '/owner-home.html',
  '/owner-laporan.html',
  '/owner-detail-laporan.html',
  '/owner-kamar-fasilitas.html',
  '/owner-konfirmasi.html',
  '/owner-proses.html',
  '/owner-tolak.html',
  '/css/style.css?v=20261125v3',
  '/js/app.js?v=20261125v3',
  '/manifest.json',
  '/icons/icon-192.png',
  '/icons/icon-512.png',
  '/icons/logo-sarpraskost.svg'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(PRECACHE_URLS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;

  const url = new URL(event.request.url);
  if (url.origin !== self.location.origin) return;
  if (url.pathname.startsWith('/api/') || url.pathname.startsWith('/uploads/') || url.pathname.startsWith('/.well-known/')) {
    return;
  }

  event.respondWith(
    caches.open(CACHE_NAME).then(async (cache) => {
      const cached = await cache.match(event.request);
      const networkPromise = fetch(event.request)
        .then((response) => {
          if (response && response.ok && response.type === 'basic') {
            cache.put(event.request, response.clone());
          }
          return response;
        })
        .catch(() => cached);

      return cached || networkPromise;
    })
  );
});

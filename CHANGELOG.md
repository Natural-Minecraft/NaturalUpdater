# Changelog - NaturalUpdater 📥

Dokumentasi riwayat pembaruan, perbaikan bug, dan rilis fitur untuk plugin **NaturalUpdater** (Automatic Update & Sync Plugin).

---

## [v1.1.0] - Pack Sync & Direct Download Update
### ✨ Fitur Baru
- **Geyser Pack Sync (NaturalPacks)**: Integrasi sinkronisasi resource pack Geyser (Bedrock) secara otomatis ke file konfigurasi Velocity dan penamaan ulang berkas mapping agar seragam.
- **`/updater pack` Command Alias**: Menambahkan alias command khusus untuk melakukan proses unggah (upload) resource pack Geyser dengan cepat.
- **Direct External URL Downloads**: Dukungan penuh untuk mengunduh library dan file jar secara langsung dari tautan eksternal (HTTP/HTTPS) tanpa batasan platform.

### 🐛 Perbaikan Bug
- **Platform Keyword Strict Match**: Memperbaiki logika pencocokan nama file agar secara ketat membedakan platform `velocity` dan `paper` sebelum mengunduh berkas jar guna menghindari kesalahan instalasi silang.

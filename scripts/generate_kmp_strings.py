#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import os

def parse_xml(path):
    if not os.path.exists(path):
        return {}
    try:
        tree = ET.parse(path)
        root = tree.getroot()
        res = {}
        for item in root.findall('string'):
            name = item.get('name')
            text = ''.join(item.itertext())
            if name and text:
                text = text.replace("\\'", "'").replace('\\"', '"')
                res[name] = text
        for item in root.findall('plurals'):
            name = item.get('name')
            if name:
                other_item = item.find("item[@quantity='other']")
                if other_item is not None:
                    text = ''.join(other_item.itertext()).replace("\\'", "'").replace('\\"', '"')
                    res[name] = text
                    if name == 'chapter_verse_count':
                        res['verse_count_format'] = text
        return res
    except Exception as e:
        print(f"Error reading {path}: {e}")
        return {}

def escape_kotlin(s):
    return s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n').replace('$', '\\$')

def escape_strings_file(s):
    return s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')

# Additional comprehensive UI keys
extra_strings = {
    "tool_qibla": {"id": "Arah Kiblat", "ms": "Arah Kiblat", "en": "Qibla Direction"},
    "tool_encyclopedia": {"id": "Ensiklopedia Islam", "ms": "Ensiklopedia Islam", "en": "Islamic Encyclopedia"},
    "tool_manzil": {"id": "Manzil", "ms": "Manzil", "en": "Manzil"},
    "nav_reflect": {"id": "Refleksi", "ms": "Refleksi", "en": "Reflection"},
    "nav_tools": {"id": "Alat Ibadah", "ms": "Alat Ibadah", "en": "Islamic Tools"},
    "nav_other": {"id": "Lainnya", "ms": "Lainnya", "en": "Other"},
    "qibla_ar_mode": {"id": "Mode Kamera AR", "ms": "Mod Kamera AR", "en": "AR Camera Mode"},
    "qibla_compass_mode": {"id": "Mode Kompas", "ms": "Mod Kompas", "en": "Compass Mode"},
    "qibla_locating": {"id": "Mendeteksi Lokasi…", "ms": "Mengesan Lokasi…", "en": "Detecting Location…"},
    "qibla_location_required": {"id": "Izin lokasi diperlukan untuk menghitung arah kiblat.", "ms": "Kebenaran lokasi diperlukan untuk mengira arah kiblat.", "en": "Location permission is required to calculate Qibla direction."},
    "open_settings": {"id": "Buka Pengaturan", "ms": "Buka Tetapan", "en": "Open Settings"},
    "qibla_bearing_format": {"id": "%1$.1f° dari Utara", "ms": "%1$.1f° dari Utara", "en": "%1$.1f° from North"},
    "qibla_aligned": {"id": "Kiblat Tepat", "ms": "Kiblat Tepat", "en": "Qibla Aligned"},
    "qibla_rotate_phone": {"id": "Putar HP Anda", "ms": "Pusingkan Telefon Anda", "en": "Rotate Your Phone"},
    "qibla_heading": {"id": "Arah Ponsel", "ms": "Arah Telefon", "en": "Phone Heading"},
    "qibla_distance": {"id": "Jarak ke Ka'bah", "ms": "Jarak ke Kaabah", "en": "Distance to Kaaba"},
    "encyclopedia_topics_tab": {"id": "Topik", "ms": "Topik", "en": "Topics"},
    "encyclopedia_glossary_tab": {"id": "Glosarium", "ms": "Glosari", "en": "Glossary"},
    "search_placeholder": {"id": "Cari topik atau istilah…", "ms": "Cari topik atau istilah…", "en": "Search topics or terms…"},
    "quran_references_title": {"id": "Referensi Al-Qur'an", "ms": "Rujukan Al-Quran", "en": "Quran References"},
    "manzil_about": {"id": "Tentang Manzil", "ms": "Tentang Manzil", "en": "About Manzil"},
    "manzil_about_sub": {"id": "Sejarah pembagian 7 hari bacaan Al-Qur'an", "ms": "Sejarah pembahagian 7 hari bacaan Al-Quran", "en": "History of the 7-day Quran reading portions"},
    "manzil_what_is": {"id": "Apa itu Manzil?", "ms": "Apakah itu Manzil?", "en": "What is Manzil?"},
    "manzil_what_is_desc": {"id": "Manzil adalah metode pembagian Al-Qur'an menjadi 7 bagian setara agar khatam dalam seminggu, sebagaimana diamalkan para sahabat Nabi ﷺ.", "ms": "Manzil adalah kaedah pembahagian Al-Quran kepada 7 bahagian setara agar khatam dalam seminggu, seperti yang diamalkan oleh para sahabat Nabi ﷺ.", "en": "Manzil is the traditional method of dividing the Quran into 7 equal portions to complete recitation in a week, as practiced by the Companions ﷺ."},
    "manzil_fami": {"id": "Rumus Fami Bi Syawq (فَمِي بِشَوْقٍ)", "ms": "Formula Fami Bi Syawq (فَمِي بِشَوْقٍ)", "en": "Fami Bi Shawq Mnemonic (فَمِي بِشَوْقٍ)"},
    "manzil_7day": {"id": "Jadwal 7 Hari Khatam", "ms": "Jadual 7 Hari Khatam", "en": "7-Day Completion Schedule"},
    "manzil_column": {"id": "Manzil", "ms": "Manzil", "en": "Manzil"},
    "manzil_surah_range": {"id": "Cakupan Surah", "ms": "Liputan Surah", "en": "Surah Range"},
    "manzil_how_to": {"id": "Cara Mengamalkan", "ms": "Cara Mengamalkan", "en": "How to Practice"},
    "manzil_how_to_desc": {"id": "Bacalah satu manzil setiap hari mulai dari hari Ahad/Jumat secara istiqamah.", "ms": "Bacalah satu manzil setiap hari bermula hari Ahad/Jumaat secara istiqamah.", "en": "Recite one manzil daily starting from Sunday/Friday with steadfast consistency."},
    "manzil_virtues": {"id": "Keutamaan Manzil", "ms": "Keutamaan Manzil", "en": "Virtues of Manzil"},
    "manzil_benefit_1": {"id": "Menjaga hafalan dan kedekatan dengan kalamullah", "ms": "Menjaga hafazan dan kedekatan dengan kalamullah", "en": "Preserves Quran retention and closeness to Allah's words"},
    "manzil_benefit_2": {"id": "Sunnah para sahabat Nabi ﷺ", "ms": "Sunnah para sahabat Nabi ﷺ", "en": "Sunnah of the noble Companions ﷺ"},
    "manzil_benefit_3": {"id": "Perlindungan dan ketenangan jiwa", "ms": "Perlindungan dan ketenangan jiwa", "en": "Spiritual protection and inner peace"},
    "manzil_benefit_4": {"id": "Membentuk disiplin tilawah harian", "ms": "Membina disiplin tilawah harian", "en": "Builds daily Quran recitation discipline"},
    "manzil_benefit_5": {"id": "Memahami tema besar ayat-ayat Al-Qur'an", "ms": "Memahami tema besar ayat-ayat Al-Quran", "en": "Understanding overarching themes of Quranic verses"},
    "zakat_live_gold_price": {"id": "Harga Emas Terkini", "ms": "Harga Emas Terkini", "en": "Current Gold Price"},
    "zakat_live_price_source": {"id": "Sumber harga pasar logam mulia", "ms": "Sumber harga pasaran logam berharga", "en": "Precious metals market price source"},
    "zakat_nisab_gold": {"id": "Nisab Emas (85 gr)", "ms": "Nisab Emas (85 g)", "en": "Gold Nisab (85g)"},
    "zakat_nisab_silver": {"id": "Nisab Perak (595 gr)", "ms": "Nisab Perak (595 g)", "en": "Silver Nisab (595g)"},
    "dhikr_subtitle": {"id": "Hitung zikir harian Anda", "ms": "Kira zikir harian anda", "en": "Count your daily dhikr"},
    "toast_marked_completed": {"id": "%1$s berhasil dicatat", "ms": "%1$s berjaya dicatat", "en": "%1$s marked completed"},
    "habit_qiyamul_lail": {"id": "Qiyamul Lail", "ms": "Qiyamul Lail", "en": "Qiyam al-Layl"},
    "habit_monday_thursday_fast": {"id": "Puasa Senin-Kamis", "ms": "Puasa Isnin-Khamis", "en": "Monday & Thursday Fasting"},
    "habit_ayyamul_bidh_fast": {"id": "Puasa Ayyamul Bidh", "ms": "Puasa Ayyamul Bidh", "en": "Ayyam al-Bidh Fasting"},
    "prayer_tracker_history": {"id": "Riwayat Shalat", "ms": "Sejarah Solat", "en": "Prayer History"},
    "prayer_tracker_history_sub": {"id": "Pantau konsistensi ibadah harian Anda", "ms": "Pantau konsistensi ibadah harian anda", "en": "Monitor your daily worship consistency"},
    "current_streak": {"id": "Streak Saat Ini", "ms": "Streak Semasa", "en": "Current Streak"},
    "best_streak": {"id": "Streak Terbaik", "ms": "Streak Terbaik", "en": "Best Streak"},
    "juz": {"id": "Juz", "ms": "Juzuk", "en": "Juz"},
    "surah": {"id": "Surah", "ms": "Surah", "en": "Surah"},
    "verse": {"id": "Ayat", "ms": "Ayat", "en": "Verse"},
    "verses": {"id": "Ayat", "ms": "Ayat", "en": "Verses"},
    "verse_of_the_day": {"id": "Ayat Hari Ini", "ms": "Ayat Hari Ini", "en": "Verse of the Day"},
    "qari_label": {"id": "Qari", "ms": "Qari", "en": "Reciter"},
    "tab_quran": {"id": "Al-Qur'an", "ms": "Al-Quran", "en": "Quran"},
    "discovering_location": {"id": "Mencari lokasi…", "ms": "Mencari lokasi…", "en": "Locating…"},
    "search_quran_a11y": {"id": "Kolom pencarian Al-Qur'an", "ms": "Ruang carian Al-Quran", "en": "Quran search bar"},
    "reciters_unavailable": {"id": "Daftar Qari tidak tersedia", "ms": "Senarai Qari tidak tersedia", "en": "Reciters list unavailable"},
    "show_tajweed": {"id": "Tampilkan Tajwid Berwarna", "ms": "Papar Tajwid Berwarna", "en": "Show Colored Tajweed"},
    "memorization_mode": {"id": "Mode Menghafal", "ms": "Mod Menghafal", "en": "Memorization Mode"},
    "arabic_translation_header": {"id": "Tampilan & Pembacaan", "ms": "Paparan & Bacaan", "en": "Display & Reading"},
    "font_small": {"id": "Kecil", "ms": "Kecil", "en": "Small"},
    "font_medium": {"id": "Sedang", "ms": "Sederhana", "en": "Medium"},
    "font_large": {"id": "Besar", "ms": "Besar", "en": "Large"},
    "font_extra_large": {"id": "Sangat Besar", "ms": "Sangat Besar", "en": "Extra Large"},
    "no_chapters": {"id": "Tidak ada surah ditemukan", "ms": "Tiada surah dijumpai", "en": "No surahs found"},
    "tap_to_begin": {"id": "Ketuk untuk Membaca", "ms": "Ketuk untuk Membaca", "en": "Tap to Begin Reading"},
    "swipe_up_intro": {"id": "Geser untuk membaca ayat pertama", "ms": "Leret untuk membaca ayat pertama", "en": "Swipe to read the first ayah"},
    "quran_ayah_options": {"id": "Pilihan Ayat", "ms": "Pilihan Ayat", "en": "Ayah Options"},
    "time_just_now": {"id": "Baru saja", "ms": "Baru sahaja", "en": "Just now"},
    "time_minutes_ago": {"id": "%1$d menit lalu", "ms": "%1$d minit lalu", "en": "%1$d minutes ago"},
    "time_hours_ago": {"id": "%1$d jam lalu", "ms": "%1$d jam lalu", "en": "%1$d hours ago"},
    "time_yesterday": {"id": "Kemarin", "ms": "Semalam", "en": "Yesterday"},
    "time_days_ago": {"id": "%1$d hari lalu", "ms": "%1$d hari lalu", "en": "%1$d days ago"},
    "reflect_contributor": {"id": "Kontributor", "ms": "Penyumbang", "en": "Contributor"},
    "reflect_recent_comment": {"id": "Komentar terbaru", "ms": "Komen terkini", "en": "Recent comment"},
    "sign_in_to_reflect": {"id": "Masuk untuk Menulis Refleksi", "ms": "Log masuk untuk Menulis Refleksi", "en": "Sign in to Post Reflection"},
    "quran_reflect_desc": {"id": "Bagikan tadabbur dan renungan hikmah ayat suci Al-Qur'an bersama sesama Muslim.", "ms": "Kongsi tadabbur dan renungan hikmah ayat suci Al-Quran bersama sesama Muslim.", "en": "Share reflections and contemplation on holy Quran verses with fellow Muslims."},
    "post_reflection": {"id": "Kirim Refleksi", "ms": "Hantar Refleksi", "en": "Post Reflection"},
    "reflect_on_verse": {"id": "Refleksi Ayat", "ms": "Refleksi Ayat", "en": "Reflect on Verse"},
    "your_reflection": {"id": "Tulis refleksi Anda…", "ms": "Tulis refleksi anda…", "en": "Write your reflection…"},
    "reflect_community": {"id": "Komunitas", "ms": "Komuniti", "en": "Community"},
    "reflect_empty_mine": {"id": "Belum ada refleksi yang Anda tulis.", "ms": "Belum ada refleksi yang anda tulis.", "en": "You haven't written any reflections yet."},
    "reflect_empty_all": {"id": "Belum ada refleksi ayat yang dibagikan.", "ms": "Belum ada refleksi ayat yang dikongsi.", "en": "No verse reflections shared yet."},
    "faraidh_case_format": {"id": "Kasus: %1$s", "ms": "Kes: %1$s", "en": "Case: %1$s"},
    "faraidh_distributions": {"id": "Pembagian Waris", "ms": "Pembahagian Waris", "en": "Inheritance Distribution"},
    "faraidh_fallback_baitulmal": {"id": "Sisa bagian dialihkan ke Baitul Mal", "ms": "Baki bahagian diserahkan kepada Baitul Mal", "en": "Surplus transferred to Baitul Mal"},
    "faraidh_heir_count_plural": {"id": "%1$d Orang", "ms": "%1$d Orang", "en": "%1$d Persons"},
    "faraidh_heir_count_singular": {"id": "1 Orang", "ms": "1 Orang", "en": "1 Person"},
    "faraidh_type_asabah": {"id": "Ashabah (Sisa Waris)", "ms": "Ashabah (Baki Waris)", "en": "Asabah (Residue)"},
    "faraidh_type_fixed": {"id": "Ashabul Furudh (Bagian Pasti)", "ms": "Ashabul Furudh (Bahagian Pasti)", "en": "Ashab al-Furud (Fixed Shares)"},
    "faraidh_aul": {"id": "'Awl", "ms": "'Awl", "en": "ʿAwl"},
    "faraidh_radd": {"id": "Radd", "ms": "Radd", "en": "Radd"},
    "faraidh_aul_desc": {"id": "Total bagian pasti melebihi harta (100%), seluruh bagian diskalakan secara proporsional.", "ms": "Jumlah bahagian pasti melebihi harta (100%), semua bahagian diskalakan secara berkadaran.", "en": "Total fixed shares exceed the estate (100%), all shares scaled proportionally."},
    "faraidh_radd_desc": {"id": "Terdapat sisa harta setelah dibagikan, kelebihan dikembalikan kepada ahli waris nasab.", "ms": "Terdapat baki harta selepas diagihkan, lebihan dikembalikan kepada waris nasab.", "en": "Surplus remains after fixed shares, excess redistributed to blood heirs."},
    "faraidh_no_blocked": {"id": "Tidak ada ahli waris yang terhalang (mahjub).", "ms": "Tiada waris yang terhalang (mahjub).", "en": "No heirs are excluded (mahjub)."},
    "faraidh_silsilah": {"id": "Silsilah Ahli Waris", "ms": "Silsilah Ahli Waris", "en": "Genealogy & Heirs"},
    "faraidh_indiv_share": {"id": "Bagian per Orang", "ms": "Bahagian setiap Orang", "en": "Individual Share"},
    "faraidh_heir_wives": {"id": "Istri", "ms": "Isteri", "en": "Wives"},
    "faraidh_heir_sons": {"id": "Anak Laki-laki", "ms": "Anak Lelaki", "en": "Sons"},
    "faraidh_heir_daughters": {"id": "Anak Perempuan", "ms": "Anak Perempuan", "en": "Daughters"},
    "faraidh_heir_grandsons": {"id": "Cucu Laki-laki (dari anak laki)", "ms": "Cucu Lelaki (dari anak lelaki)", "en": "Grandsons (paternal)"},
    "faraidh_heir_granddaughters": {"id": "Cucu Perempuan (dari anak laki)", "ms": "Cucu Perempuan (dari anak lelaki)", "en": "Granddaughters (paternal)"},
    "faraidh_heir_full_brothers": {"id": "Saudara Laki-laki Sekandung", "ms": "Saudara Lelaki Seibu Sebapa", "en": "Full Brothers"},
    "faraidh_heir_full_sisters": {"id": "Saudara Perempuan Sekandung", "ms": "Saudara Perempuan Seibu Sebapa", "en": "Full Sisters"},
    "faraidh_heir_paternal_brothers": {"id": "Saudara Laki-laki Seayah", "ms": "Saudara Lelaki Sebapa", "en": "Paternal Brothers"},
    "faraidh_heir_paternal_sisters": {"id": "Saudara Perempuan Seayah", "ms": "Saudara Perempuan Sebapa", "en": "Paternal Sisters"},
    "faraidh_heir_maternal_brothers": {"id": "Saudara Laki-laki Seibu", "ms": "Saudara Lelaki Seibu", "en": "Maternal Brothers"},
    "faraidh_heir_maternal_sisters": {"id": "Saudara Perempuan Seibu", "ms": "Saudara Perempuan Seibu", "en": "Maternal Sisters"},
    "faraidh_heir_baitul_mal_or_excluded": {"id": "Baitul Mal / Terhalang", "ms": "Baitul Mal / Terhalang", "en": "Baitul Mal / Excluded"},
    "faraidh_deceased": {"id": "Pewaris (Meninggal)", "ms": "Si Mati", "en": "Deceased"},
    "faraidh_reason_by_son": {"id": "Terhalang karena adanya anak laki-laki", "ms": "Terhalang kerana adanya anak lelaki", "en": "Excluded by presence of a son"},
    "faraidh_reason_by_children": {"id": "Terhalang karena adanya anak/cucu", "ms": "Terhalang kerana adanya anak/cucu", "en": "Excluded by presence of children/grandchildren"},
    "faraidh_reason_by_father": {"id": "Terhalang karena adanya ayah", "ms": "Terhalang kerana adanya bapa", "en": "Excluded by presence of the father"},
    "faraidh_reason_by_grandfather": {"id": "Terhalang karena adanya kakek", "ms": "Terhalang kerana adanya datuk", "en": "Excluded by presence of the grandfather"},
    "faraidh_reason_excluded": {"id": "Terhalang (Hajib)", "ms": "Terhalang (Hajib)", "en": "Excluded (Mahjub)"},
    "faraidh_reason_gender_mismatch": {"id": "Tidak memenuhi syarat jenis kelamin", "ms": "Tidak memenuhi syarat jantina", "en": "Gender condition not met"},
    "faraidh_reason_no_remainder": {"id": "Tidak ada sisa harta ashabah", "ms": "Tiada baki harta ashabah", "en": "No residue remaining"},
    "faraidh_reason_out_of_wedlock": {"id": "Status hubungan non-waris", "ms": "Status hubungan bukan waris", "en": "Non-heir relation status"},
    "faraidh_reason_homicide": {"id": "Gugur hak waris", "ms": "Gugur hak waris", "en": "Inheritance right forfeited"},
    "faraidh_reason_religion": {"id": "Perbedaan agama", "ms": "Perbezaan agama", "en": "Difference in religion"},
    "faraidh_reason_simultaneous": {"id": "Meninggal bersamaan", "ms": "Meninggal dunia serentak", "en": "Simultaneous death"},
    "faraidh_case_minbariyah": {"id": "Al-Minbariyah", "ms": "Al-Minbariyah", "en": "Al-Minbariyah"},
    "faraidh_case_akdariyah": {"id": "Al-Akdariyah", "ms": "Al-Akdariyah", "en": "Al-Akdariyah"},
    "faraidh_case_marwaniyah": {"id": "Al-Marwaniyah (Al-Musytarakah)", "ms": "Al-Marwaniyah (Al-Musytarakah)", "en": "Al-Marwaniyah (Al-Mushtaraka)"},
    "faraidh_case_umariyatain": {"id": "Al-'Umariyatain (Gharrawain)", "ms": "Al-'Umariyatain (Gharrawain)", "en": "Al-ʿUmariyatain (Gharrawain)"},
    "tool_faraidh": {"id": "Kalkulator Waris (Faraidh)", "ms": "Kalkulator Faraid", "en": "Inheritance Calculator (Faraidh)"},
    "faraidh_profile": {"id": "Profil Almarhum / Almarhumah", "ms": "Profil Si Mati", "en": "Deceased Profile"},
    "faraidh_name": {"id": "Nama Almarhum / Almarhumah", "ms": "Nama Si Mati", "en": "Name of Deceased"},
    "faraidh_gender": {"id": "Jenis Kelamin", "ms": "Jantina", "en": "Gender"},
    "faraidh_male": {"id": "Laki-laki", "ms": "Lelaki", "en": "Male"},
    "faraidh_female": {"id": "Perempuan", "ms": "Perempuan", "en": "Female"},
    "faraidh_madhhab": {"id": "Mazhab Fikih", "ms": "Mazhab Fiqh", "en": "Jurisprudence School (Madhhab)"},
    "faraidh_shafii": {"id": "Syafi'i", "ms": "Syafi'i", "en": "Shafi'i"},
    "faraidh_hanafi": {"id": "Hanafi", "ms": "Hanafi", "en": "Hanafi"},
    "faraidh_maliki": {"id": "Maliki", "ms": "Maliki", "en": "Maliki"},
    "faraidh_hanbali": {"id": "Hanbali", "ms": "Hanbali", "en": "Hanbali"},
    "faraidh_estate": {"id": "Rincian Harta Peninggalan", "ms": "Perincian Harta Pusaka", "en": "Estate Assets Details"},
    "faraidh_cash": {"id": "Uang Tunai / Tabungan", "ms": "Wang Tunai / Simpanan", "en": "Cash & Savings"},
    "faraidh_gold": {"id": "Emas & Logam Mulia", "ms": "Emas & Logam Berharga", "en": "Gold & Precious Metals"},
    "faraidh_property": {"id": "Properti & Tanah", "ms": "Hartanah & Tanah", "en": "Real Estate & Land"},
    "faraidh_business": {"id": "Saham & Bisnis", "ms": "Saham & Perniagaan", "en": "Stocks & Business"},
    "faraidh_other": {"id": "Aset Lainnya", "ms": "Aset Lain", "en": "Other Assets"},
    "faraidh_funeral": {"id": "Biaya Pengurusan Jenazah", "ms": "Kos Pengurusan Jenazah", "en": "Funeral & Burial Costs"},
    "faraidh_debts": {"id": "Hutang / Kewajiban Tertunggak", "ms": "Hutang / Liabiliti Tertunggak", "en": "Outstanding Debts & Liabilities"},
    "faraidh_wasiat": {"id": "Wasiat (Maks 1/3 Harta Bersih)", "ms": "Wasiat (Maks 1/3 Harta Bersih)", "en": "Bequest/Will (Max 1/3 Net Estate)"},
    "faraidh_zakat": {"id": "Zakat yang Belum Ditunaikan", "ms": "Zakat Belum Ditunaikan", "en": "Unpaid Zakat"},
    "faraidh_net_estate": {"id": "Total Harta Bersih Siap Waris", "ms": "Jumlah Harta Bersih Sedia Faraid", "en": "Net Distributable Estate"},
    "faraidh_heirs": {"id": "Daftar Ahli Waris", "ms": "Senarai Waris", "en": "List of Heirs"},
    "faraidh_out_wedlock": {"id": "Anak Luar Nikah / Anak Angkat", "ms": "Anak Luar Nikah / Anak Angkat", "en": "Children Outside Marriage / Adopted"},
    "faraidh_btn_calc": {"id": "Hitung Pembagian Waris", "ms": "Kira Pembahagian Pusaka", "en": "Calculate Inheritance Distribution"},
    "faraidh_tab_form": {"id": "Formulir", "ms": "Borang", "en": "Form"},
    "faraidh_tab_shares": {"id": "Hasil Pembagian", "ms": "Hasil Bahagian", "en": "Shares"},
    "faraidh_tab_proofs": {"id": "Dalil & Penjelasan", "ms": "Dalil & Penerangan", "en": "Proofs & Explanations"},
    "basmalah_translation": {"id": "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang", "ms": "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang", "en": "In the name of Allah, the Entirely Merciful, the Especially Merciful"},
    "city_name": {"id": "Nama Kota", "ms": "Nama Bandar", "en": "City Name"},
    "daily_verse_reminder_desc": {"id": "Dapatkan inspirasi ayat Al-Qur'an harian langsung di layar Anda.", "ms": "Dapatkan inspirasi ayat Al-Quran harian terus di skrin anda.", "en": "Receive daily Quranic inspiration directly on your screen."},
    "reminder_time": {"id": "Waktu Pengingat", "ms": "Waktu Peringatan", "en": "Reminder Time"},
    "morning_reminder": {"id": "Pengingat Pagi", "ms": "Peringatan Pagi", "en": "Morning Reminder"},
    "tool_qiyam": {"id": "Panduan Qiyam & Tahajud", "ms": "Panduan Qiyam & Tahajud", "en": "Qiyam & Tahajjud Guide"},
    "qiyam_tab_readings": {"id": "Panduan Doa & Zikir", "ms": "Panduan Doa & Zikir", "en": "Prayers & Dhikr"},
    "qiyam_tab_tracker": {"id": "Pelacak Shalat Malam", "ms": "Penjejak Solat Malam", "en": "Night Prayer Tracker"},
    "qiyam_tracker_subtitle": {"id": "Istiqamahkan ibadah malam Anda", "ms": "Istiqamahkan ibadah malam anda", "en": "Keep steadfast in your night prayers"},
    "qiyam_what_is": {"id": "Apa itu Qiyamul Lail?", "ms": "Apakah itu Qiyamul Lail?", "en": "What is Qiyam al-Layl?"},
    "qiyam_readings_desc": {"id": "Kumpulan tata cara, niat, bacaan, dan doa shalat sunnah malam.", "ms": "Himpunan tata cara, niat, bacaan, dan doa solat sunat malam.", "en": "Comprehensive guide for night prayers, intentions, and supplications."},
    "qiyam_prayed_tonight": {"id": "Sudah Shalat Malam Ini?", "ms": "Sudah Solat Malam Ini?", "en": "Prayed Tonight?"},
    "qiyam_streak": {"id": "Streak Qiyam", "ms": "Streak Qiyam", "en": "Qiyam Streak"},
    "qiyam_this_month": {"id": "Bulan Ini", "ms": "Bulan Ini", "en": "This Month"},
    "qiyam_last_7_days": {"id": "7 Hari Terakhir", "ms": "7 Hari Terakhir", "en": "Last 7 Days"},
    "qiyam_private_tracker": {"id": "Catatan Pribadi", "ms": "Rekod Peribadi", "en": "Private Log"},
    "qiyam_tracker_desc": {"id": "Catatan ini disimpan aman di perangkat Anda untuk menjaga keikhlasan amal ibadah.", "ms": "Rekod ini disimpan selamat pada peranti anda untuk memelihara keikhlasan amal.", "en": "This record is stored privately on your device to preserve sincere intentions."},
    "qiyam_cat_prep": {"id": "Persiapan & Niat", "ms": "Persediaan & Niat", "en": "Preparation & Intention"},
    "qiyam_cat_prayer": {"id": "Tata Cara Shalat", "ms": "Tatacara Solat", "en": "Prayer Procedure"},
    "qiyam_cat_witr": {"id": "Shalat Witir", "ms": "Solat Witir", "en": "Witr Prayer"},
    "qiyam_cat_closing": {"id": "Doa & Penutup", "ms": "Doa & Penutup", "en": "Supplications & Closing"},
    "tahajud_niat_title": {"id": "Niat Shalat Tahajud", "ms": "Niat Solat Tahajud", "en": "Intention for Tahajjud"},
    "tahajud_niat_body": {"id": "أُصَلِّي سُنَّةَ التَّهَجُّدِ رَكْعَتَيْنِ لِلّٰهِ تَعَالَى\\n(Ushallii sunnatat-tahajjudi rak'ataini lillaahi Ta'aalaa)", "ms": "أُصَلِّي سُنَّةَ التَّهَجُّدِ رَكْعَتَيْنِ لِلّٰهِ تَعَالَى\\n(Ushallii sunnatat-tahajjudi rak'ataini lillaahi Ta'aalaa)", "en": "Ushalli sunnatat-tahajjudi rak'ataini lillahi Ta'ala (I intend to pray two units of Tahajjud sunnah for Allah the Almighty)"},
    "tahajud_takbir_title": {"id": "Takbiratul Ihram", "ms": "Takbiratul Ihram", "en": "Takbiratul Ihram"},
    "tahajud_takbir_body": {"id": "Mengangkat kedua tangan sejajar telinga/bahu seraya membaca Allāhu Akbar.", "ms": "Mengangkat kedua-dua belah tangan paras telinga/bahu sambil membaca Allāhu Akbar.", "en": "Raise hands up to shoulders/ears saying Allahu Akbar."},
    "tahajud_iftitah_title": {"id": "Doa Iftitah", "ms": "Doa Iftitah", "en": "Opening Supplication (Iftitah)"},
    "tahajud_iftitah_body": {"id": "Membaca doa istiftah seperti yang diajarkan Rasulullah ﷺ.", "ms": "Membaca doa istiftah sebagaimana diajarkan oleh Rasulullah ﷺ.", "en": "Recite the opening supplication as taught by the Prophet ﷺ."},
    "tahajud_fatihah_title": {"id": "Surah Al-Fatihah", "ms": "Surah Al-Fatihah", "en": "Surah Al-Fatihah"},
    "tahajud_fatihah_body": {"id": "Membaca Surah Al-Fatihah dengan tartil dan penghayatan.", "ms": "Membaca Surah Al-Fatihah secara tartil dan penuh penghayatan.", "en": "Recite Surah Al-Fatihah with measured reflection."},
    "tahajud_surah_title": {"id": "Membaca Surah Pilihan", "ms": "Membaca Surah Pilihan", "en": "Selected Surah Recitation"},
    "tahajud_surah_body": {"id": "Disunnahkan membaca surah panjang atau surah yang mudah dihafal secara khusyuk.", "ms": "Disunatkan membaca surah yang panjang atau surah yang mudah dihafal dengan khusyuk.", "en": "Recommended to recite longer surahs or what is easy with tranquility."},
    "tahajud_ruku_title": {"id": "Rukuk & Thuma'ninah", "ms": "Rukuk & Thuma'ninah", "en": "Bowing (Ruku) & Stillness"},
    "tahajud_ruku_body": {"id": "Rukuk dengan memanjangkan bacaan tasbih: سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ", "ms": "Rukuk dengan memanjangkan tasbih: سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ", "en": "Bow and prolong glorification: Subhana Rabbiyal Azimi wa Bihamdihi"},
    "tahajud_sujud_title": {"id": "Sujud & Memperbanyak Doa", "ms": "Sujud & Memperbanyakkan Doa", "en": "Prostration (Sujud) & Supplication"},
    "tahajud_sujud_body": {"id": "Sujud adalah keadaan paling dekat antara hamba dan Rabb-nya. Perbanyak doa dalam sujud.", "ms": "Sujud ialah saat paling hampir antara hamba dan Penciptanya. Perbanyakkan doa ketika sujud.", "en": "Prostration is when a servant is closest to Allah. Make abundant supplications."},
    "tahajud_witr_title": {"id": "Penutup Witir", "ms": "Penutup Witir", "en": "Witr Completion"},
    "tahajud_witr_body": {"id": "Tutuplah shalat malam dengan shalat Witir ganjil (1 atau 3 rakaat).", "ms": "Tutuplah solat malam dengan solat Witir bilangan ganjil (1 atau 3 rakaat).", "en": "Conclude your night prayers with odd-numbered Witr (1 or 3 rak'ahs)."},
    "tahajud_qunut_title": {"id": "Qunut Witir (Separuh Akhir Ramadhan)", "ms": "Qunut Witir (Separuh Akhir Ramadhan)", "en": "Qunut in Witr (Second Half of Ramadan)"},
    "tahajud_qunut_body": {"id": "Membaca doa Qunut pada rakaat terakhir witir.", "ms": "Membaca doa Qunut pada rakaat terakhir witir.", "en": "Recite Qunut supplication in the final rak'ah of Witr."},
    "tahajud_dhikr_title": {"id": "Zikir Setelah Shalat Malam", "ms": "Zikir Selepas Solat Malam", "en": "Dhikr After Night Prayer"},
    "tahajud_dhikr_body": {"id": "Membaca Istighfar 100x dan Sayyidul Istighfar di waktu sahur.", "ms": "Membaca Istighfar 100x dan Sayyidul Istighfar pada waktu sahur.", "en": "Recite Istighfar 100 times and Sayyid al-Istighfar during pre-dawn (sahur)."},
    "tahajud_dua_title": {"id": "Doa Tahajud Rasulullah ﷺ", "ms": "Doa Tahajud Rasulullah ﷺ", "en": "Prophet's ﷺ Tahajjud Supplication"},
    "tahajud_dua_body": {"id": "اللّٰهُمَّ لَكَ الحَمْدُ أَنْتَ نُورُ السَّمَاوَاتِ وَالأَرْضِ وَمَنْ فِيهِنَّ...", "ms": "اللّٰهُمَّ لَكَ الحَمْدُ أَنْتَ نُورُ السَّمَاوَاتِ وَالأَرْضِ وَمَنْ فِيهِنَّ...", "en": "Allahumma lakal hamdu Anta noorus-samawati wal-ardi wa man feehinna..."},
    "tahajud_when_title": {"id": "Waktu Terbaik Tahajud", "ms": "Waktu Terbaik Tahajud", "en": "Best Time for Tahajjud"},
    "tahajud_when_body": {"id": "Sepertiga malam terakhir (sekitar pukul 02.00 hingga sebelum Subuh).", "ms": "Sepertiga malam terakhir (kira-kira jam 02.00 hingga sebelum Subuh).", "en": "The last third of the night (around 2:00 AM until before Fajr)."},
    "location_source": {"id": "Sumber Lokasi", "ms": "Sumber Lokasi", "en": "Location Source"},
    "gps_status": {"id": "Status GPS", "ms": "Status GPS", "en": "GPS Status"},
    "gps_active": {"id": "GPS Aktif", "ms": "GPS Aktif", "en": "GPS Active"},
    "latitude": {"id": "Lintang (Latitude)", "ms": "Latitud", "en": "Latitude"},
    "longitude": {"id": "Bujur (Longitude)", "ms": "Longitud", "en": "Longitude"},
    "set_manually": {"id": "Atur Manual", "ms": "Tetapkan Secara Manual", "en": "Set Manually"},
    "preset_cities": {"id": "Pilihan Kota", "ms": "Pilihan Bandar", "en": "Preset Cities"},
    "save_location": {"id": "Simpan Lokasi", "ms": "Simpan Lokasi", "en": "Save Location"},
    "font_size": {"id": "Ukuran Huruf", "ms": "Saiz Fon", "en": "Font Size"},
    "sample_arabic_typography": {"id": "Pratinjau Tipografi Arab", "ms": "Pratonton Tipografi Arab", "en": "Arabic Typography Preview"},
    "select_translator": {"id": "Pilih Penerjemah", "ms": "Pilih Penterjemah", "en": "Select Translator"},
    "search_translators_placeholder": {"id": "Cari nama penerjemah atau bahasa…", "ms": "Cari nama penterjemah atau bahasa…", "en": "Search translator or language…"},
    "loading_translators": {"id": "Memuat daftar penerjemah…", "ms": "Memuatkan senarai penterjemah…", "en": "Loading translators…"},
    "sign_in": {"id": "Masuk", "ms": "Log Masuk", "en": "Sign In"},
    "sign_in_prompt": {"id": "Masuk untuk mencadangkan data dan menyelaraskan catatan ibadah antar perangkat.", "ms": "Log masuk untuk menyandar data dan menyelaraskan catatan ibadah antara peranti.", "en": "Sign in to backup data and sync reflections across your devices."},
    "sync_reflections": {"id": "Sinkronisasi Refleksi", "ms": "Penyelarasan Refleksi", "en": "Sync Reflections"}
}

base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '../app/src/main/res'))

# English (default values)
en_strings = parse_xml(os.path.join(base_dir, 'values/strings.xml'))
en_strings.update(parse_xml(os.path.join(base_dir, 'values/share_format_strings.xml')))
en_strings['revelation_place_makkah'] = 'Makkah'
en_strings['relevation_place_makah'] = 'Makkah'
en_strings['revelation_place_madinah'] = 'Madinah'
if 'verse_count_format' not in en_strings:
    en_strings['verse_count_format'] = '%d verses'

# Indonesian (values-in)
id_strings = parse_xml(os.path.join(base_dir, 'values-in/strings.xml'))
id_strings.update(parse_xml(os.path.join(base_dir, 'values-in/strings_features.xml')))
id_strings.update(parse_xml(os.path.join(base_dir, 'values-in/share_format_strings.xml')))
id_strings['revelation_place_makkah'] = 'Makkah'
id_strings['relevation_place_makah'] = 'Makkah'
id_strings['revelation_place_madinah'] = 'Madinah'
if 'verse_count_format' not in id_strings:
    id_strings['verse_count_format'] = '%d ayat'

# Malay (values-ms)
ms_strings = parse_xml(os.path.join(base_dir, 'values-ms/strings.xml'))
ms_strings.update(parse_xml(os.path.join(base_dir, 'values-ms/strings_features.xml')))
ms_strings.update(parse_xml(os.path.join(base_dir, 'values-ms/share_format_strings.xml')))
ms_strings['revelation_place_makkah'] = 'Makkah'
ms_strings['relevation_place_makah'] = 'Makkah'
ms_strings['revelation_place_madinah'] = 'Madinah'
if 'verse_count_format' not in ms_strings:
    ms_strings['verse_count_format'] = '%d ayat'

for k, trans in extra_strings.items():
    if k not in en_strings:
        en_strings[k] = trans["en"]
    if k not in id_strings:
        id_strings[k] = trans["id"]
    if k not in ms_strings:
        ms_strings[k] = trans["ms"]

# Also add manzil section descriptions 1-7
for sec_id in range(1, 8):
    sec_key = f"manzil_sec_{sec_id}_desc"
    if sec_key not in id_strings:
        id_strings[sec_key] = f"Bacaan Manzil Bagian {sec_id}"
        ms_strings[sec_key] = f"Bacaan Manzil Bahagian {sec_id}"
        en_strings[sec_key] = f"Manzil Portion {sec_id} Recitation"



all_keys = sorted(list(set(list(en_strings.keys()) + list(id_strings.keys()) + list(ms_strings.keys()))))
print(f"Total unique keys: {len(all_keys)}")
print(f"EN: {len(en_strings)}, ID: {len(id_strings)}, MS: {len(ms_strings)}")

# 1. KMP Shared Strings
out_kmp_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '../shared/src/commonMain/kotlin/app/kamy/saatApp/shared/localization'))
os.makedirs(out_kmp_dir, exist_ok=True)

lang_kt = """package app.kamy.saatApp.shared.localization

enum class SharedLanguage(val code: String, val displayName: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    MALAY("ms", "Bahasa Melayu"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): SharedLanguage = when (code.lowercase()) {
            "id", "in", "indonesia", "indonesian" -> INDONESIAN
            "ms", "my", "melayu", "malay" -> MALAY
            else -> ENGLISH
        }
    }
}
"""

with open(os.path.join(out_kmp_dir, 'SharedLanguage.kt'), 'w', encoding='utf-8') as f:
    f.write(lang_kt)

kt_content = """package app.kamy.saatApp.shared.localization

object SharedStrings {

    fun get(key: String, language: SharedLanguage = SharedLanguage.INDONESIAN): String {
        val langMap = when (language) {
            SharedLanguage.INDONESIAN -> idStrings
            SharedLanguage.MALAY -> msStrings
            SharedLanguage.ENGLISH -> enStrings
        }
        return langMap[key] ?: idStrings[key] ?: enStrings[key] ?: key
    }

    fun get(key: String, langCode: String): String {
        return get(key, SharedLanguage.fromCode(langCode))
    }

    private val idStrings: Map<String, String> by lazy {
        val map = HashMap<String, String>(""" + str(len(all_keys)) + """)\n"""

for k in all_keys:
    val = id_strings.get(k, en_strings.get(k, ""))
    if val:
        kt_content += f'        map["{k}"] = "{escape_kotlin(val)}"\n'

kt_content += """        map
    }

    private val msStrings: Map<String, String> by lazy {
        val map = HashMap<String, String>(""" + str(len(all_keys)) + """)\n"""

for k in all_keys:
    val = ms_strings.get(k, id_strings.get(k, en_strings.get(k, "")))
    if val:
        kt_content += f'        map["{k}"] = "{escape_kotlin(val)}"\n'

kt_content += """        map
    }

    private val enStrings: Map<String, String> by lazy {
        val map = HashMap<String, String>(""" + str(len(all_keys)) + """)\n"""

for k in all_keys:
    val = en_strings.get(k, id_strings.get(k, ""))
    if val:
        kt_content += f'        map["{k}"] = "{escape_kotlin(val)}"\n'

kt_content += """        map
    }
}
"""

with open(os.path.join(out_kmp_dir, 'SharedStrings.kt'), 'w', encoding='utf-8') as f:
    f.write(kt_content)

# 2. iOS Localizable.strings
ios_res_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '../iosApp/Saat/Resources'))
os.makedirs(os.path.join(ios_res_dir, 'en.lproj'), exist_ok=True)
os.makedirs(os.path.join(ios_res_dir, 'id.lproj'), exist_ok=True)
os.makedirs(os.path.join(ios_res_dir, 'ms.lproj'), exist_ok=True)

def write_strings_file(path, data, fallback):
    with open(path, 'w', encoding='utf-8') as f:
        for k in all_keys:
            v = data.get(k, fallback.get(k, ""))
            f.write(f'"{k}" = "{escape_strings_file(v)}";\n')

write_strings_file(os.path.join(ios_res_dir, 'id.lproj/Localizable.strings'), id_strings, en_strings)
write_strings_file(os.path.join(ios_res_dir, 'ms.lproj/Localizable.strings'), ms_strings, id_strings)
write_strings_file(os.path.join(ios_res_dir, 'en.lproj/Localizable.strings'), en_strings, id_strings)

# 3. iOS AppLanguageManager.swift
swift_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../iosApp/Saat/Core/AppLanguageManager.swift'))
swift_content = """//
//  AppLanguageManager.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation
import Combine
import shared

enum AppLanguage: String, CaseIterable, Identifiable {
    case english = "en"
    case indonesian = "id"
    case malay = "ms"
    
    var id: String { rawValue }
    var localeIdentifier: String { rawValue }
    
    var displayName: String {
        switch self {
        case .english: return "English"
        case .indonesian: return "Bahasa Indonesia"
        case .malay: return "Bahasa Melayu"
        }
    }
}

extension Notification.Name {
    static let appLanguageDidChange = Notification.Name("appLanguageDidChange")
}

@MainActor
class AppLanguageManager: ObservableObject {
    static let shared = AppLanguageManager()
    static let storageKey = "selected_app_language"
    
    @Published var currentLanguage: AppLanguage = .indonesian {
        didSet {
            UserDefaults.standard.set(currentLanguage.rawValue, forKey: Self.storageKey)
            NotificationCenter.default.post(name: .appLanguageDidChange, object: nil)
        }
    }
    
    private init() {
        if let raw = UserDefaults.standard.string(forKey: Self.storageKey),
           let lang = AppLanguage(rawValue: raw) {
            currentLanguage = lang
        } else {
            let localeLang = Locale.preferredLanguages.first ?? "id"
            if localeLang.hasPrefix("ms") {
                currentLanguage = .malay
            } else if localeLang.hasPrefix("en") {
                currentLanguage = .english
            } else {
                currentLanguage = .indonesian
            }
        }
    }
    
    func localize(_ key: String) -> String {
        return SharedStrings.shared.get(key: key, langCode: currentLanguage.rawValue)
    }
    
    func localizeFormatted(_ key: String, _ args: Any...) -> String {
        let format = localize(key)
        return formatString(format, with: args)
    }
    
    func formatString(_ format: String, with args: [Any]) -> String {
        guard !args.isEmpty else { return format }
        var result = format
        
        // 1. Replace positional specifiers: %1$s, %1$d, %1$f, %1$@, %1$ld, etc.
        for (index, arg) in args.enumerated() {
            let position = index + 1
            let argString = String(describing: arg)
            let specifiers = [
                "%\\\\(position)$s", "%\\\\(position)$d", "%\\\\(position)$f", 
                "%\\\\(position)$@", "%\\\\(position)$ld", "%\\\\(position)$lf"
            ]
            for spec in specifiers {
                result = result.replacingOccurrences(of: spec, with: argString)
            }
        }
        
        // 2. Sequential specifiers (%s, %d, %@, %f, etc.) replaced in order
        let regexPattern = #"%(?:[0-9]+\\$)?(@|s|d|f|ld|lf)"#
        if let regex = try? NSRegularExpression(pattern: regexPattern, options: []) {
            var argIndex = 0
            while argIndex < args.count {
                let range = NSRange(result.startIndex..<result.endIndex, in: result)
                guard let match = regex.firstMatch(in: result, options: [], range: range),
                      let matchRange = Range(match.range, in: result) else {
                    break
                }
                let argString = String(describing: args[argIndex])
                result.replaceSubrange(matchRange, with: argString)
                argIndex += 1
            }
        }
        
        return result
    }
}
"""

with open(swift_path, 'w', encoding='utf-8') as f:
    f.write(swift_content)

print(f"Successfully synchronized {len(all_keys)} keys across KMP SharedStrings, iOS AppLanguageManager & Localizable.strings!")


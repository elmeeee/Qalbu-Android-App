package app.kamy.saatApp.infrastructure.local

import app.kamy.saatApp.core.locale.AppLanguage

data class ImportantDayDetail(
    val description: String,
    val sunnah: String
)

object ImportantDayRegistry {

    fun getLocalizedEventName(rawEvent: String, language: AppLanguage): String {
        return when (rawEvent) {
            "Awal Ramadhan" -> when (language) {
                AppLanguage.ENGLISH -> "Beginning of Ramadan"
                AppLanguage.INDONESIAN -> "Awal Ramadhan"
                AppLanguage.MALAY -> "Awal Ramadan"
            }
            "Ayyamul Bidh" -> when (language) {
                AppLanguage.ENGLISH -> "Ayyamul Bidh"
                AppLanguage.INDONESIAN -> "Ayyamul Bidh"
                AppLanguage.MALAY -> "Ayyamul Bidh"
            }
            "Hari Arafah" -> when (language) {
                AppLanguage.ENGLISH -> "Day of Arafah"
                AppLanguage.INDONESIAN -> "Hari Arafah"
                AppLanguage.MALAY -> "Hari Arafah"
            }
            "Hari Asyuro" -> when (language) {
                AppLanguage.ENGLISH -> "Day of Ashura"
                AppLanguage.INDONESIAN -> "Hari Asyura"
                AppLanguage.MALAY -> "Hari Asyura"
            }
            "Hari Idul Adha" -> when (language) {
                AppLanguage.ENGLISH -> "Eid al-Adha"
                AppLanguage.INDONESIAN -> "Hari Raya Idul Adha"
                AppLanguage.MALAY -> "Hari Raya Aidiladha"
            }
            "Hari Idul Fitri" -> when (language) {
                AppLanguage.ENGLISH -> "Eid al-Fitr"
                AppLanguage.INDONESIAN -> "Hari Raya Idul Fitri"
                AppLanguage.MALAY -> "Hari Raya Aidilfitri"
            }
            "Hari Isra Mi'raj" -> when (language) {
                AppLanguage.ENGLISH -> "Isra' and Mi'raj"
                AppLanguage.INDONESIAN -> "Isra Mi'raj"
                AppLanguage.MALAY -> "Israk Mikraj"
            }
            "Hari Maulid Nabi" -> when (language) {
                AppLanguage.ENGLISH -> "Prophet's Birthday (Mawlid)"
                AppLanguage.INDONESIAN -> "Maulid Nabi Muhammad SAW"
                AppLanguage.MALAY -> "Maulid Nabi Muhammad SAW"
            }
            "Hari Nuzulul Quran" -> when (language) {
                AppLanguage.ENGLISH -> "Nuzulul Quran"
                AppLanguage.INDONESIAN -> "Nuzulul Quran"
                AppLanguage.MALAY -> "Nuzulul Quran"
            }
            "Hari Tahun baru Islam (Hijriah)" -> when (language) {
                AppLanguage.ENGLISH -> "Islamic New Year"
                AppLanguage.INDONESIAN -> "Tahun Baru Islam (1 Muharram)"
                AppLanguage.MALAY -> "Tahun Baru Islam (Maal Hijrah)"
            }
            "Hari Tasua" -> when (language) {
                AppLanguage.ENGLISH -> "Tasua Day"
                AppLanguage.INDONESIAN -> "Hari Tasua"
                AppLanguage.MALAY -> "Hari Tasyua"
            }
            "Hari Tasyrik" -> when (language) {
                AppLanguage.ENGLISH -> "Days of Tashreeq"
                AppLanguage.INDONESIAN -> "Hari Tasyrik"
                AppLanguage.MALAY -> "Hari Tasyrik"
            }
            else -> rawEvent
        }
    }

    fun getLocalizedSectionHeaders(language: AppLanguage): Triple<String, String, String> {
        return when (language) {
            AppLanguage.ENGLISH -> Triple("IMPORTANT DAY", "About This Day", "Recommended Sunnah")
            AppLanguage.INDONESIAN -> Triple("HARI PENTING", "Tentang Hari Ini", "Amalan Sunnah")
            AppLanguage.MALAY -> Triple("HARI PENTING", "Tentang Hari Ini", "Amalan Sunnah")
        }
    }

    fun getImportantDayDetail(rawEvent: String, language: AppLanguage): ImportantDayDetail? {
        return when (rawEvent) {
            "Awal Ramadhan" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The beginning of the holy month of Ramadan, the 9th month of the Islamic calendar, in which Muslims perform fasting from dawn until sunset.",
                    sunnah = "Setting sincere intentions for fasting, sighting the crescent moon (hilal), reading the moon-sighting supplication, increasing Quran recitation, and performing Taraweeh prayers."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Permulaan bulan suci Ramadhan, bulan ke-9 dalam kalender Hijriah di mana umat Islam diwajibkan untuk menunaikan ibadah puasa dari terbit fajar hingga terbenam matahari.",
                    sunnah = "Mempersiapkan niat puasa secara mantap, memantau hilal (rukyatul hilal), membaca doa melihat hilal, memperbanyak bacaan Al-Quran, serta melaksanakan shalat sunnah Tarawih."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Permulaan bulan suci Ramadan, bulan ke-9 dalam kalendar Hijriah di mana umat Islam diwajibkan untuk menunaikan ibadah puasa dari terbit fajar hingga terbenam matahari.",
                    sunnah = "Menetapkan niat puasa dengan mantap, melihat anak bulan (rukyatul hilal), membaca doa melihat anak bulan, memperbanyakkan bacaan Al-Quran, serta mendirikan solat sunat Tarawih."
                )
            }
            "Ayyamul Bidh" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The 'White Days' referring to the 13th, 14th, and 15th days of every lunar Hijri month, named so because the moon is full and at its brightest.",
                    sunnah = "Fasting on these three consecutive days. The Prophet Muhammad (PBUH) recommended this fast, stating its reward is equivalent to fasting the entire year."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari-hari putih yang merujuk pada tanggal 13, 14, dan 15 di setiap bulan Hijriah. Dinamakan demikian karena bulan bersinar paling terang (bulan purnama).",
                    sunnah = "Melaksanakan puasa sunnah Ayyamul Bidh selama tiga hari berturut-turut tersebut, yang keutamaannya setara dengan puasa sepanjang tahun."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari-hari putih yang merujuk pada tarikh 13, 14, dan 15 pada setiap bulan Hijriah. Dinamakan sedemikian kerana bulan bersinar paling terang (bulan purnama).",
                    sunnah = "Melakukan puasa sunat Ayyamul Bidh selama tiga hari berturut-turut tersebut, yang kelebihannya menyamai berpuasa sepanjang tahun."
                )
            }
            "Hari Arafah" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The 9th day of Dhu al-Hijjah, marking the peak of the Hajj pilgrimage where pilgrims gather at the plain of Arafah to pray and seek forgiveness.",
                    sunnah = "For non-pilgrims, fasting on this day is highly recommended as it expiates the sins of the past year and the coming year. It is also sunnah to make abundant Dua, Dhikr, and recite the Takbeer."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari ke-9 di bulan Dzulhijjah, bertepatan dengan puncak ibadah haji ketika para jemaah melakukan wukuf di Padang Arafah untuk berdoa dan memohon ampunan.",
                    sunnah = "Bagi yang tidak sedang melaksanakan ibadah haji, sangat dianjurkan untuk berpuasa sunnah Arafah (menghapus dosa setahun lalu dan setahun depan), serta memperbanyak doa, dzikir, dan bertakbir."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari ke-9 di bulan Zulhijjah, bertepatan dengan kemuncak ibadah haji apabila para jemaah melakukan wukuf di Padang Arafah untuk berdoa dan memohon keampunan.",
                    sunnah = "Bagi mereka yang tidak mengerjakan haji, sangat disunnahkan untuk berpuasa sunat Arafah (menghapuskan dosa setahun lalu dan setahun akan datang), serta memperbanyakkan doa, zikir, dan bertakbir."
                )
            }
            "Hari Asyuro" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The 10th day of Muharram. A historical day when Allah saved Prophet Musa (Moses) and the Children of Israel from the Pharaoh by parting the Red Sea.",
                    sunnah = "Fasting on the day of Ashura, which expiates the sins of the preceding year. It is recommended to fast the 9th (Tasua) along with it to differ from the Jewish custom."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari ke-10 di bulan Muharram. Hari bersejarah di mana Allah menyelamatkan Nabi Musa AS dan Bani Israil dari kejaran Firaun dengan membelah Laut Merah.",
                    sunnah = "Melaksanakan puasa sunnah Asyura yang keutamaannya menghapus dosa setahun yang lalu. Dianjurkan menggabungkannya dengan puasa hari ke-9 (Tasua) untuk menyelisihi kebiasaan Yahudi."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari ke-10 di bulan Muharram. Hari bersejarah di mana Allah menyelamatkan Nabi Musa AS dan Bani Israil daripada kejar Firaun dengan membelah Laut Merah.",
                    sunnah = "Mendirikan puasa sunat Asyura yang kelebihannya menghapuskan dosa setahun yang lalu. Disunnahkan menggabungkannya dengan puasa hari ke-9 (Tasyua) untuk membezakan diri daripada amalan Yahudi."
                )
            }
            "Hari Idul Adha" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The Festival of Sacrifice, celebrated on the 10th of Dhu al-Hijjah to honor Prophet Ibrahim's (Abraham) willingness to sacrifice his son Ishmael in obedience to Allah.",
                    sunnah = "Reciting the Takbeer, performing the Eid prayer, slaughtering the sacrificial animal (Qurbani/Udhiyah), wearing one's best clothes, taking a bath before the prayer, and taking different routes to and from the prayer place."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari Raya Kurban yang jatuh pada 10 Dzulhijjah, memperingati kepatuhan Nabi Ibrahim AS yang ikhlas mengurbankan putranya Ismail demi menaati perintah Allah.",
                    sunnah = "Mengumandangkan takbir, melaksanakan shalat Idul Adha, menyembelih hewan qurban, mandi sebelum shalat, mengenakan pakaian terbaik, serta melewati jalan pergi dan pulang yang berbeda."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari Raya Korban yang jatuh pada 10 Zulhijjah, memperingati kepatuhan Nabi Ibrahim AS yang ikhlas mengorbankan putranya Ismail demi mentaati perintah Allah.",
                    sunnah = "Mengumandangkan takbir, mendirikan solat Aidiladha, menyembelih binatang korban, mandi sunat sebelum solat, memakai pakaian terbaik, serta melalui jalan pergi dan balik yang berbeza."
                )
            }
            "Hari Idul Fitri" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The Festival of Breaking the Fast, celebrated on the 1st of Shawwal to mark the successful completion of the month-long daily fasting of Ramadan.",
                    sunnah = "Paying Zakat al-Fitr before the Eid prayer, eating dates (in an odd number) before heading out, bathing, wearing one's best clothes, reciting the Takbeer, and performing the Eid prayer."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari raya kemenangan umat Islam yang dirayakan pada 1 Syawal setelah menunaikan ibadah puasa sebulan penuh di bulan Ramadhan.",
                    sunnah = "Menunaikan zakat fitrah sebelum shalat Id, makan kurma (jumlah ganjil) sebelum berangkat shalat, mandi, memakai pakaian terbaik, bertakbir sejak malam hari, serta melaksanakan shalat Id."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari raya kemenangan umat Islam yang dirayakan pada 1 Syawal selepas menunaikan ibadah puasa sebulan penuh di bulan Ramadan.",
                    sunnah = "Menunaikan zakat fitrah sebelum solat Id, makan kurma (jumlah ganjil) sebelum berangkat solat, mandi sunat, memakai pakaian terbaik, bertakbir sejak malam raya, serta mendirikan solat Id."
                )
            }
            "Hari Isra Mi'raj" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The miraculous night journey of Prophet Muhammad (PBUH) from Makkah to Jerusalem (Isra') and his ascension to the heavens (Mi'raj), where the command for five daily prayers was received.",
                    sunnah = "Reflecting on the significance of the five daily prayers, improving the quality of one's prayers, studying the history of the Prophet's journey to strengthen faith, and increasing good deeds."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Mukjizat perjalanan malam Nabi Muhammad SAW dari Masjidil Haram ke Masjidil Aqsa (Isra) lalu naik ke langit ketujuh hingga Sidratul Muntaha (Mi'raj), tempat diterimanya perintah shalat 5 waktu.",
                    sunnah = "Merenungkan kembali keutamaan shalat 5 waktu dan meningkatkan kualitas shalat kita, mempelajari sejarah perjalanan mulia ini untuk memperkuat keimanan, serta meningkatkan amal kebajikan."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Mukjizat perjalanan malam Nabi Muhammad SAW dari Masjidil Haram ke Masjidil Aqsa (Israk) lalu naik ke langit ketujuh hingga Sidratul Muntaha (Mikraj), tempat diterimanya perintah solat 5 waktu.",
                    sunnah = "Merenungkan kembali keutamaan solat 5 waktu dan memperbaiki kualiti solat kita, mempelajari sejarah perjalanan mulia ini untuk memperkuat keimanan, serta meningkatkan amalan kebajikan."
                )
            }
            "Hari Maulid Nabi" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The anniversary of the birth of Prophet Muhammad (PBUH), born on the 12th of Rabi' al-Awwal.",
                    sunnah = "Increasing salawat (blessings) upon the Prophet, reading and studying his Seerah (biography) to emulate his noble character, feeding others, and fasting (the Prophet used to fast on Mondays because he was born on a Monday)."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari kelahiran Nabi Muhammad SAW yang diperingati pada tanggal 12 Rabiul Awal sebagai wujud rasa syukur atas diutusnya beliau ke dunia.",
                    sunnah = "Memperbanyak shalawat, membaca dan merenungkan sirah (sejarah hidup) Nabi SAW untuk meneladani akhlak mulia beliau, bersedekah makanan, serta berpuasa sunnah (seperti puasa hari Senin)."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari keputeraan Nabi Muhammad SAW yang diperingati pada tanggal 12 Rabiulawal sebagai tanda kesyukuran atas diutusnya baginda ke dunia.",
                    sunnah = "Memperbanyakkan selawat, membaca dan merenungkan sirah (sejarah hidup) Nabi SAW untuk meneladani akhlak mulia baginda, bersedekah makanan, serta berpuasa sunat (seperti puasa hari Isnin)."
                )
            }
            "Hari Nuzulul Quran" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The commemoration of the night when the first verses of the Holy Quran were revealed to the Prophet Muhammad (PBUH) in the Cave of Hira, traditionally observed on the 17th of Ramadan.",
                    sunnah = "Increasing recitation and reflection (tadabbur) of the Quran, studying its tafsir, making sincere supplications, and performing night prayers (Qiyam)."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Peristiwa diturunkannya wahyu pertama Al-Quran kepada Nabi Muhammad SAW di Gua Hira, yang secara tradisi diperingati pada tanggal 17 Ramadhan.",
                    sunnah = "Meningkatkan interaksi dengan Al-Quran melalui tilawah, hafalan, dan tadabbur (memahami makna); memperbanyak doa, serta menghidupkan malam dengan shalat malam."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Peristiwa diturunkannya wahyu pertama Al-Quran kepada Nabi Muhammad SAW di Gua Hira, yang secara tradisi diperingati pada tarikh 17 Ramadan.",
                    sunnah = "Meningkatkan interaksi dengan Al-Quran melalui tilawah, hafalan, dan tadabbur (memahami makna); memperbanyakkan doa, serta menghidupkan malam dengan solat malam."
                )
            }
            "Hari Tahun baru Islam (Hijriah)" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The start of the Islamic lunar calendar (1 Muharram), commemorating the historical migration (Hijrah) of the Prophet Muhammad (PBUH) from Makkah to Madinah.",
                    sunnah = "Reflecting on the past year's deeds (muhasabah), establishing intentions for spiritual growth, reciting prayers of gratitude, and preparing to fast in the sacred month of Muharram."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari pertama bulan Muharram menandai pergantian tahun baru kalender Hijriah, mengenang sejarah peristiwa Hijrah Rasulullah SAW dari Makkah ke Madinah.",
                    sunnah = "Melakukan muhasabah (introspeksi diri), memperbarui niat untuk menjadi pribadi yang lebih baik, membaca doa akhir dan awal tahun, serta bersiap melaksanakan puasa sunnah di bulan Muharram."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari pertama bulan Muharram menandai pertukaran tahun baru kalendar Hijriah, mengenang sejarah peristiwa Hijrah Rasulullah SAW dari Makkah ke Madinah.",
                    sunnah = "Melakukan muhasabah (introspeksi diri), memperbaharui niat untuk menjadi individu yang lebih baik, membaca doa akhir dan awal tahun, serta bersiap sedia melaksanakan puasa sunat di bulan Muharram."
                )
            }
            "Hari Tasua" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The 9th day of Muharram, preceding Ashura. It is observed as a day of preparation and fasting.",
                    sunnah = "Fasting on the 9th of Muharram along with the 10th of Muharram (Ashura) to distinguish the Islamic fast from other non-Islamic traditions."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Hari ke-9 di bulan Muharram, satu hari sebelum hari Asyura. Hari persiapan dan ibadah puasa.",
                    sunnah = "Melaksanakan puasa sunnah pada 9 Muharram (diiringi dengan puasa Asyura pada 10 Muharram) untuk membedakan ibadah puasa kaum Muslimin dengan kaum Yahudi."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Hari ke-9 di bulan Muharram, sehari sebelum hari Asyura. Hari persediaan dan ibadah puasa.",
                    sunnah = "Melakukan puasa sunat pada 9 Muharram (diiringi dengan puasa Asyura pada 10 Muharram) untuk membezakan ibadah puasa kaum Muslimin daripada kaum Yahudi."
                )
            }
            "Hari Tasyrik" -> when (language) {
                AppLanguage.ENGLISH -> ImportantDayDetail(
                    description = "The three days following Eid al-Adha (11th, 12th, and 13th of Dhu al-Hijjah). They are designated for eating, drinking, and remembering Allah.",
                    sunnah = "Reciting the Takbeer (especially after prayers), completing the slaughter of sacrificial animals (Qurbani), and making abundant Dhikr. Note: Fasting is prohibited on these days."
                )
                AppLanguage.INDONESIAN -> ImportantDayDetail(
                    description = "Tiga hari setelah Hari Raya Idul Adha (tanggal 11, 12, dan 13 Dzulhijjah). Merupakan hari-hari makan, minum, dan berdzikir kepada Allah.",
                    sunnah = "Mengumandangkan takbir muqayyad di setiap selesai shalat fardhu, menyelesaikan penyembelihan hewan qurban, dan memperbanyak dzikir. Catatan: Diharamkan berpuasa pada hari-hari ini."
                )
                AppLanguage.MALAY -> ImportantDayDetail(
                    description = "Tiga hari selepas Hari Raya Aidiladha (tarikh 11, 12, dan 13 Zulhijjah). Merupakan hari-hari makan, minum, dan berzikir kepada Allah.",
                    sunnah = "Mengumandangkan takbir muqayyad setiap kali selesai solat fardu, menyelesaikan penyembelihan binatang korban, dan memperbanyakkan zikir. Catatan: Diharamkan berpuasa pada hari-hari ini."
                )
            }
            else -> null
        }
    }
}

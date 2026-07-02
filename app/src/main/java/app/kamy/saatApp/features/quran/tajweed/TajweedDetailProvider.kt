package app.kamy.saatApp.features.quran.tajweed

import androidx.compose.ui.graphics.Color

data class TajweedExample(
    val arabic: String,
    val transliteration: String,
    val explanation: String
)

data class TajweedDetail(
    val type: TajweedType,
    val title: String,
    val description: String,
    val letters: String,
    val howToRead: String,
    val color: Color,
    val examples: List<TajweedExample>
)

object TajweedDetailProvider {

    fun getDetail(type: TajweedType, languageCode: String): TajweedDetail {
        return when (languageCode) {
            "in", "id" -> getIndonesianDetail(type)
            "ms" -> getMalayDetail(type)
            else -> getEnglishDetail(type)
        }
    }

    private fun getIndonesianDetail(type: TajweedType): TajweedDetail {
        return when (type) {
            TajweedType.GHUNNA -> TajweedDetail(
                type = type,
                title = "Ghunnah",
                description = "Membaca huruf dengan berdengung karena adanya huruf Nun (ن) atau Mim (م) yang memiliki tanda tasydid (ّ).",
                letters = "نّ   مّ",
                howToRead = "Suara ditahan masuk ke rongga hidung selama 2 harakat (ketukan) dengan dengung yang jelas sebelum masuk ke harakat berikutnya.",
                color = Color(0xFFFF7E1E),
                examples = listOf(
                    TajweedExample("إِنَّ", "Inna", "Terdapat Nun bertasydid, suara berdengung ditahan selama 2 harakat."),
                    TajweedExample("عَمَّ", "'Amma", "Terdapat Mim bertasydid, suara berdengung ditahan selama 2 harakat."),
                    TajweedExample("فِي الْجِنَّةِ وَالنَّاسِ", "Fil jinnati wan-nās", "Dengung ditahan pada kata Al-Jinnati (نّ) dan An-Nās (نّ).")
                )
            )
            TajweedType.QALQALAH -> TajweedDetail(
                type = type,
                title = "Qalqalah",
                description = "Memantulkan bunyi huruf apabila huruf tersebut sukun (mati asli) atau diwaqafkan (dihentikan di akhir kalimat).",
                letters = "ق   ط   ب   ج   د",
                howToRead = "Huruf dipantulkan dengan jelas. Jika di tengah kata pantulannya ringan (Sughra). Jika di akhir kalimat atau karena waqaf pantulannya lebih kuat (Kubra).",
                color = Color(0xFFDD0008),
                examples = listOf(
                    TajweedExample("قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul huwallāhu aḥad", "Pantulan kuat (Kubra) pada huruf Dal (د) di akhir ayat karena waqaf."),
                    TajweedExample("يَجْعَلُونَ", "Yaj'alūna", "Pantulan ringan (Sughra) pada huruf Jim (جْ) sukun di tengah kata."),
                    TajweedExample("فِي صَدْرِكَ", "Fī ṣadrika", "Pantulan ringan (Sughra) pada huruf Dal (دْ) sukun di tengah kata.")
                )
            )
            TajweedType.IQLAB -> TajweedDetail(
                type = type,
                title = "Iqlab",
                description = "Mengubah bunyi huruf Nun sukun (نْ) atau Tanwin menjadi bunyi Mim (مْ) disertai dengung apabila bertemu huruf Ba (ب).",
                letters = "ب",
                howToRead = "Mengubah bunyi 'N' menjadi 'M' dengan merapatkan kedua bibir secara ringan (tanpa ditekan kuat) dan ditahan berdengung selama 2 harakat.",
                color = Color(0xFF26BFFD),
                examples = listOf(
                    TajweedExample("مِنْ بَعْدِ", "Mim ba'di", "Nun sukun (نْ) bertemu Ba (ب), dibaca menjadi 'mim ba'di' dengan dengung."),
                    TajweedExample("سَمِيعٌ بَصِيرٌ", "Samī'um baṣīr", "Tanwin dhummah bertemu Ba (ب), dibaca menjadi 'samī'um baṣīr' dengan dengung.")
                )
            )
            TajweedType.IKHFA -> TajweedDetail(
                type = type,
                title = "Ikhfa Haqiqi",
                description = "Menyamarkan bunyi Nun sukun (نْ) atau Tanwin apabila bertemu salah satu dari 15 huruf Ikhfa.",
                letters = "ت   ث   ج   د   ذ   ز   س   ش   ص   ض   ط   ظ   ف   ق   ك",
                howToRead = "Bunyi Nun sukun atau Tanwin dibaca samar-samar menjurus ke makhraj huruf berikutnya, disertai dengan dengung yang ditahan selama 2 harakat.",
                color = Color(0xFF9400A8),
                examples = listOf(
                    TajweedExample("مِنْ قَبْلِ", "Ming qabli", "Nun sukun bertemu Qaf (ق), dibaca samar menjurus ke bunyi 'ng' disertai dengung."),
                    TajweedExample("أَنْفُسَكُمْ", "Angfusakum", "Nun sukun bertemu Fa (ف), dibaca samar menjurus ke bunyi 'f' disertai dengung."),
                    TajweedExample("شَيْءٍ كَبِIRٍ", "Syai-ing kabīr", "Tanwin kasrah bertemu Kaf (ك), dibaca samar menjurus ke bunyi 'ng' disertai dengung.")
                )
            )
            TajweedType.IKHFA_SYAFAWI -> TajweedDetail(
                type = type,
                title = "Ikhfa Syafawi",
                description = "Menyamarkan bunyi Mim sukun (مْ) apabila bertemu dengan huruf Ba (ب).",
                letters = "ب",
                howToRead = "Bunyi Mim sukun disamarkan di bibir disertai dengung yang ditahan selama 2 harakat.",
                color = Color(0xFFD500B7),
                examples = listOf(
                    TajweedExample("تَرْمِيهِمْ بِحِجَارَةٍ", "Tarmīhim biḥijārah", "Mim sukun (مْ) bertemu Ba (ب), dibaca samar-samar dengan merapatkan bibir ringan disertai dengung."),
                    TajweedExample("وَمَا هُمْ بِمُؤْمِنِينَ", "Wa mā hum bimu'minīn", "Mim sukun (مْ) bertemu Ba (ب) dibaca samar disertai dengung.")
                )
            )
            TajweedType.IDGHAM_WITH_GHUNNA -> TajweedDetail(
                type = type,
                title = "Idgham Bighunnah",
                description = "Memasukkan bunyi Nun sukun (نْ) atau Tanwin ke dalam salah satu huruf Idgham Bighunnah disertai dengung.",
                letters = "ي   ن   م   و",
                howToRead = "Meleburkan bunyi Nun sukun atau Tanwin sepenuhnya ke dalam huruf berikutnya sehingga terdengar bertasydid, disertai dengung ditahan 2 harakat.",
                color = Color(0xFF169200),
                examples = listOf(
                    TajweedExample("مَنْ يَقُولُ", "May yaqūlu", "Nun sukun melebur ke huruf Ya (ي) dibaca 'mayyaqūlu' disertai dengung."),
                    TajweedExample("مِنْ وَالٍ", "Miw wālin", "Nun sukun melebur ke huruf Waw (و) dibaca 'miwwālin' disertai dengung."),
                    TajweedExample("عَذَابٌ مُقِيمٌ", "Adzābum muqīm", "Tanwin dhummah melebur ke huruf Mim (م) dibaca 'adzābum-muqīm' disertai dengung.")
                )
            )
            TajweedType.IDGHAM_WITHOUT_GHUNNA -> TajweedDetail(
                type = type,
                title = "Idgham Bilaghunnah",
                description = "Memasukkan bunyi Nun sukun (نْ) atau Tanwin ke dalam huruf Lam (ل) atau Ra (ر) tanpa disertai dengung.",
                letters = "ل   ر",
                howToRead = "Meleburkan bunyi Nun sukun atau Tanwin sepenuhnya ke dalam huruf Lam atau Ra tanpa dengung (langsung dibaca cepat tanpa ditahan).",
                color = Color(0xFFA1A1A1),
                examples = listOf(
                    TajweedExample("مِنْ رَبِّهِمْ", "Mir rabbihim", "Nun sukun melebur ke huruf Ra (ر) dibaca 'mir-rabbihim' tanpa ditahan berdengung."),
                    TajweedExample("أَنْ لَمْ يَرَهُ", "Al lam yarahu", "Nun sukun melebur ke huruf Lam (ل) dibaca 'al-lam yarahu' tanpa ditahan berdengung.")
                )
            )
            TajweedType.IDGHAM_MIMI -> TajweedDetail(
                type = type,
                title = "Idgham Mimi (Mutamasilain)",
                description = "Memasukkan bunyi Mim sukun (مْ) ke dalam huruf Mim (م) berikutnya disertai dengung.",
                letters = "م",
                howToRead = "Menggabungkan dua huruf Mim menjadi satu huruf Mim bertasydid, dibaca berdengung dan ditahan selama 2 harakat.",
                color = Color(0xFF169200),
                examples = listOf(
                    TajweedExample("لَهُمْ مَا يَشَاءُونَ", "Lahum mā yasyā'ūn", "Mim sukun melebur ke huruf Mim berikutnya dibaca berdengung ditahan 2 harakat."),
                    TajweedExample("فِي قُلُوبِهِمْ مَرَضٌ", "Fī qulūbihim marad", "Mim sukun melebur ke huruf Mim berikutnya dibaca berdengung ditahan 2 harakat.")
                )
            )
        }
    }

    private fun getMalayDetail(type: TajweedType): TajweedDetail {
        return when (type) {
            TajweedType.GHUNNA -> TajweedDetail(
                type = type,
                title = "Ghunnah",
                description = "Membaca huruf dengan berdengung kerana terdapat huruf Nun (ن) atau Mim (م) yang mempunyai tanda sabdu/tasydid (ّ).",
                letters = "نّ   مّ",
                howToRead = "Bunyi ditahan di dalam rongga hidung selama 2 harakat (ketukan) dengan dengung yang jelas sebelum menyebut baris seterusnya.",
                color = Color(0xFFFF7E1E),
                examples = listOf(
                    TajweedExample("إِنَّ", "Inna", "Nun bertasydid, bunyi berdengung ditahan selama 2 harakat."),
                    TajweedExample("عَمَّ", "'Amma", "Mim bertasydid, bunyi berdengung ditahan selama 2 harakat."),
                    TajweedExample("فِي الْجِنَّةِ وَالنَّاسِ", "Fil jinnati wan-nās", "Dengung ditahan pada perkataan Al-Jinnati (نّ) dan An-Nās (نّ).")
                )
            )
            TajweedType.QALQALAH -> TajweedDetail(
                type = type,
                title = "Qalqalah",
                description = "Memantulkan bunyi huruf apabila huruf tersebut bertanda sukun (mati) atau diwaqafkan (dihentikan bacaan di akhir kalimah).",
                letters = "ق   ط   ب   ج   د",
                howToRead = "Huruf dipantulkan dengan jelas. Jika di tengah perkataan pantulannya adalah kecil/ringan (Sughra). Jika di akhir kalimah atau kerana waqaf pantulannya lebih besar/kuat (Kubra).",
                color = Color(0xFFDD0008),
                examples = listOf(
                    TajweedExample("قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul huwallāhu aḥad", "Pantulan kuat (Kubra) pada huruf Dal (د) di akhir ayat kerana waqaf."),
                    TajweedExample("يَجْعَلُونَ", "Yaj'alūna", "Pantulan ringan (Sughra) pada huruf Jim (جْ) sukun di tengah perkataan."),
                    TajweedExample("فِي صَدْرِكَ", "Fī ṣadrika", "Pantulan ringan (Sughra) pada huruf Dal (دْ) sukun di tengah perkataan.")
                )
            )
            TajweedType.IQLAB -> TajweedDetail(
                type = type,
                title = "Iqlab",
                description = "Menukarkan bunyi huruf Nun sukun (نْ) atau Tanwin menjadi bunyi Mim (مْ) berserta dengung apabila bertemu huruf Ba (ب).",
                letters = "ب",
                howToRead = "Menukarkan bunyi 'N' menjadi 'M' dengan merapatkan kedua-dua bibir secara ringan (tanpa ditekan kuat) dan ditahan berdengung selama 2 harakat.",
                color = Color(0xFF26BFFD),
                examples = listOf(
                    TajweedExample("مِنْ بَعْدِ", "Mim ba'di", "Nun sukun (نْ) bertemu Ba (ب), dibaca menjadi 'mim ba'di' secara berdengung."),
                    TajweedExample("سَمِيعٌ بَصِيرٌ", "Samī'um baṣīr", "Tanwin bertemu Ba (ب), dibaca menjadi 'samī'um baṣīr' secara berdengung.")
                )
            )
            TajweedType.IKHFA -> TajweedDetail(
                type = type,
                title = "Ikhfa Haqiqi",
                description = "Menyembunyikan/menyamarkan bunyi Nun sukun (نْ) atau Tanwin apabila bertemu salah satu daripada 15 huruf Ikhfa.",
                letters = "ت   ث   ج   د   ذ   ز   س   ش   ص   ض   ط   ظ   ف   ق   ك",
                howToRead = "Bunyi Nun sukun atau Tanwin dibaca samar-samar menghampiri sebutan huruf berikutnya, berserta dengan dengung yang ditahan selama 2 harakat.",
                color = Color(0xFF9400A8),
                examples = listOf(
                    TajweedExample("مِنْ قَبْلِ", "Ming qabli", "Nun sukun bertemu Qaf (ق), dibaca samar menghampiri bunyi 'ng' berserta dengung."),
                    TajweedExample("أَنْفُسَكُمْ", "Angfusakum", "Nun sukun bertemu Fa (ف), dibaca samar menghampiri bunyi 'f' berserta dengung."),
                    TajweedExample("شَيْءٍ كَبِيرٍ", "Syai-ing kabīr", "Tanwin bertemu Kaf (ك), dibaca samar menghampiri sebutan 'ng' berserta dengung.")
                )
            )
            TajweedType.IKHFA_SYAFAWI -> TajweedDetail(
                type = type,
                title = "Ikhfa Syafawi",
                description = "Menyembunyikan/menyamarkan bunyi Mim sukun (مْ) apabila bertemu dengan huruf Ba (ب).",
                letters = "ب",
                howToRead = "Bunyi Mim sukun disamarkan di bibir berserta dengung yang ditahan selama 2 harakat.",
                color = Color(0xFFD500B7),
                examples = listOf(
                    TajweedExample("تَرْمِيهِمْ بِحِجَارَةٍ", "Tarmīhim biḥijārah", "Mim sukun (مْ) bertemu Ba (ب), dibaca samar-samar dengan merapatkan bibir secara ringan berserta dengung."),
                    TajweedExample("وَمَا هُمْ بِمُؤْمِنِينَ", "Wa mā hum bimu'minīn", "Mim sukun (مْ) bertemu Ba (ب) dibaca samar berserta dengung.")
                )
            )
            TajweedType.IDGHAM_WITH_GHUNNA -> TajweedDetail(
                type = type,
                title = "Idgham Bighunnah",
                description = "Memasukkan sebutan Nun sukun (نْ) atau Tanwin ke dalam salah satu huruf Idgham Bighunnah berserta dengung.",
                letters = "ي   ن   م   و",
                howToRead = "Meleburkan sebutan Nun sukun atau Tanwin sepenuhnya ke dalam huruf berikutnya sehingga terdengar seperti bertasydid/sabdu, berserta dengung ditahan selama 2 harakat.",
                color = Color(0xFF169200),
                examples = listOf(
                    TajweedExample("مَنْ يَقُولُ", "May yaqūlu", "Nun sukun melebur ke huruf Ya (ي) dibaca 'mayyaqūlu' berserta dengung."),
                    TajweedExample("مِنْ وَالٍ", "Miw wālin", "Nun sukun melebur ke huruf Waw (و) dibaca 'miwwālin' berserta dengung."),
                    TajweedExample("عَذَابٌ مُقِيمٌ", "Adzābum muqīm", "Tanwin melebur ke huruf Mim (م) dibaca 'adzābum-muqīm' berserta dengung.")
                )
            )
            TajweedType.IDGHAM_WITHOUT_GHUNNA -> TajweedDetail(
                type = type,
                title = "Idgham Bilaghunnah",
                description = "Memasukkan sebutan Nun sukun (نْ) atau Tanwin ke dalam huruf Lam (ل) atau Ra (ر) tanpa berserta dengung.",
                letters = "ل   ر",
                howToRead = "Meleburkan sebutan Nun sukun atau Tanwin sepenuhnya ke dalam huruf Lam atau Ra tanpa dengung (langsung dibaca cepat tanpa ditahan dengungnya).",
                color = Color(0xFFA1A1A1),
                examples = listOf(
                    TajweedExample("مِنْ رَبِّهِمْ", "Mir rabbihim", "Nun sukun melebur ke huruf Ra (ر) dibaca 'mir-rabbihim' tanpa ditahan dengungnya."),
                    TajweedExample("أَنْ لَمْ يَرَهُ", "Al lam yarahu", "Nun sukun melebur ke huruf Lam (ل) dibaca 'al-lam yarahu' tanpa ditahan dengungnya.")
                )
            )
            TajweedType.IDGHAM_MIMI -> TajweedDetail(
                type = type,
                title = "Idgham Mimi (Mutamasilain)",
                description = "Memasukkan sebutan Mim sukun (مْ) ke dalam huruf Mim (م) berikutnya berserta dengung.",
                letters = "م",
                howToRead = "Menggabungkan dua huruf Mim menjadi satu sebutan Mim yang bersabdu/bertasydid, dibaca berdengung dan ditahan selama 2 harakat.",
                color = Color(0xFF169200),
                examples = listOf(
                    TajweedExample("لَهُمْ مَا يَشَاءُونَ", "Lahum mā yasyā'ūn", "Mim sukun melebur ke huruf Mim berikutnya dibaca berdengung ditahan selama 2 harakat."),
                    TajweedExample("فِي قُلُوبِهِمْ مَرَضٌ", "Fī qulūbihim marad", "Mim sukun melebur ke huruf Mim berikutnya dibaca berdengung ditahan selama 2 harakat.")
                )
            )
        }
    }

    private fun getEnglishDetail(type: TajweedType): TajweedDetail {
        return when (type) {
            TajweedType.GHUNNA -> TajweedDetail(
                type = type,
                title = "Ghunnah",
                description = "Pronouncing with a nasal sound because of a doubled (shaddah) Nun (ن) or Mim (م).",
                letters = "نّ   مّ",
                howToRead = "Hold the nasal sound through the nose for 2 counts (beats) before moving to the next vowel.",
                color = Color(0xFFFF7E1E),
                examples = listOf(
                    TajweedExample("إِنَّ", "Inna", "Doubled Nun, hold with nasal sound for 2 counts."),
                    TajweedExample("عَمَّ", "'Amma", "Doubled Mim, hold with nasal sound for 2 counts.")
                )
            )
            TajweedType.QALQALAH -> TajweedDetail(
                type = type,
                title = "Qalqalah",
                description = "Echoing or bouncing the sound of the letter when it has a sukoon (silent marker) or is stopped due to pausing (waqf).",
                letters = "ق   ط   ب   ج   د",
                howToRead = "Bounce the sound clearly. Light bounce (Sughra) in the middle of a word, strong bounce (Kubra) at the end of a verse due to stopping.",
                color = Color(0xFFDD0008),
                examples = listOf(
                    TajweedExample("قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul huwallāhu aḥad", "Strong bounce (Kubra) on Dal (د) at the end of the verse."),
                    TajweedExample("يَجْعَلُونَ", "Yaj'alūna", "Light bounce (Sughra) on Jim (جْ) in the middle of the word.")
                )
            )
            TajweedType.IQLAB -> TajweedDetail(
                type = type,
                title = "Iqlab",
                description = "Converting the sound of Nun Sakinah (نْ) or Tanween into a Mim (مْ) with a nasal sound when followed by Ba (ب).",
                letters = "ب",
                howToRead = "Close the lips gently (without pressure), turning the 'N' sound into a hidden 'M' sound, and hold for 2 counts.",
                color = Color(0xFF26BFFD),
                examples = listOf(
                    TajweedExample("مِنْ بَعْدِ", "Mim ba'di", "Nun Sakinah followed by Ba is pronounced as a hidden Mim with nasalization."),
                    TajweedExample("سَمِيعٌ بَصِيرٌ", "Samī'um baṣīr", "Tanween followed by Ba is pronounced as a hidden Mim with nasalization.")
                )
            )
            TajweedType.IKHFA -> TajweedDetail(
                type = type,
                title = "Ikhfa Haqiqi",
                description = "Hiding the sound of Nun Sakinah (نْ) or Tanween when followed by one of the 15 Ikhfa letters.",
                letters = "ت   ث   ج   د   ذ   ز   س   ش   ص   ض   ط   ظ   ف   ق   ك",
                howToRead = "Partially hide the 'N' sound by preparing your mouth/tongue for the next letter, accompanied by a nasal sound held for 2 counts.",
                color = Color(0xFF9400A8),
                examples = listOf(
                    TajweedExample("مِنْ قَبْلِ", "Ming qabli", "Nun Sakinah followed by Qaf, nasalized and hidden towards Qaf."),
                    TajweedExample("أَنْفُسَكُمْ", "Angfusakum", "Nun Sakinah followed by Fa, nasalized and hidden towards Fa.")
                )
            )
            TajweedType.IKHFA_SYAFAWI -> TajweedDetail(
                type = type,
                title = "Ikhfa Syafawi",
                description = "Hiding the Mim Sakinah (مْ) sound at the lips with nasalization when followed by Ba (ب).",
                letters = "ب",
                howToRead = "Pronounce the Mim lightly at the lips with a nasal sound, holding it for 2 counts.",
                color = Color(0xFFD500B7),
                examples = listOf(
                    TajweedExample("تَرْمِيهِمْ بِحِجَارَةٍ", "Tarmīhim biḥijārah", "Mim Sakinah followed by Ba is hidden with a nasal sound.")
                )
            )
            TajweedType.IDGHAM_WITH_GHUNNA -> TajweedDetail(
                type = type,
                title = "Idgham Bighunnah",
                description = "Merging the sound of Nun Sakinah (نْ) or Tanween into one of the four letters of Idgham Bighunnah with nasalization.",
                letters = "ي   ن   م   و",
                howToRead = "Merge the 'N' sound completely into the next letter so it sounds doubled, and hold the nasal sound for 2 counts.",
                color = Color(0xFF169200),
                examples = listOf(
                    TajweedExample("مَنْ يَقُولُ", "May yaqūlu", "Nun Sakinah is merged into Ya, holding with nasal sound."),
                    TajweedExample("مِنْ وَالٍ", "Miw wālin", "Nun Sakinah is merged into Waw, holding with nasal sound.")
                )
            )
            TajweedType.IDGHAM_WITHOUT_GHUNNA -> TajweedDetail(
                type = type,
                title = "Idgham Bilaghunnah",
                description = "Merging the sound of Nun Sakinah (نْ) or Tanween into Lam (ل) or Ra (ر) without nasalization.",
                letters = "ل   ر",
                howToRead = "Merge the 'N' sound completely into Lam or Ra without holding or nasalizing.",
                color = Color(0xFFA1A1A1),
                examples = listOf(
                    TajweedExample("مِنْ رَبِّهِمْ", "Mir rabbihim", "Nun Sakinah merges into Ra with no nasal sound."),
                    TajweedExample("أَنْ لَمْ يَرَهُ", "Al lam yarahu", "Nun Sakinah merges into Lam with no nasal sound.")
                )
            )
            TajweedType.IDGHAM_MIMI -> TajweedDetail(
                type = type,
                title = "Idgham Mimi (Mutamasilayn)",
                description = "Merging Mim Sakinah (مْ) into the following Mim (م) with nasalization.",
                letters = "م",
                howToRead = "Combine the two Mim letters into one doubled Mim, holding it with a nasal sound for 2 counts.",
                color = Color(0xFF169200),
                examples = listOf(
                    TajweedExample("لَهُمْ مَا يَشَاءُونَ", "Lahum mā yasyā'ūn", "Mim Sakinah merges into the next Mim with a nasal sound.")
                )
            )
        }
    }
}

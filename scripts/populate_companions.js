const fs = require('fs');
const zlib = require('zlib');
const { execSync } = require('child_process');

const dbGzPath = 'app/src/main/assets/quran/qurannew.db.gz';
const dbTempPath = '/tmp/quran_companion.db';

console.log('Decompressing quran database...');
fs.writeFileSync(dbTempPath, zlib.gunzipSync(fs.readFileSync(dbGzPath)));

function getVerseData(surah, start, end) {
  const sql = `SELECT text, indonesian, translation_en, malay, transliteration_id, transliteration_en FROM ayas WHERE sura=${surah} AND aya BETWEEN ${start} AND ${end} ORDER BY aya;`;
  const raw = execSync(`sqlite3 "${dbTempPath}" "${sql}"`).toString().trim();
  const lines = raw.split('\n').filter(Boolean);

  const arList = [];
  const idList = [];
  const enList = [];
  const msList = [];
  const latinList = [];
  const latinEnList = [];

  lines.forEach(line => {
    const parts = line.split('|');
    if (parts.length >= 6) {
      arList.push(parts[0].trim());
      idList.push(parts[1].trim());
      enList.push(parts[2].trim());
      msList.push(parts[3].trim());
      latinList.push(parts[4].trim());
      latinEnList.push(parts[5].trim());
    }
  });

  return {
    verseTextAr: arList.join(' • '),
    verseTextTranslation: idList.join(' '),
    verseTextTranslationEn: enList.join(' '),
    verseTextTranslationMs: msList.join(' ') || idList.join(' '),
    verseTextLatin: latinList.join(' '),
    verseTextLatinEn: latinEnList.join(' ') || latinList.join(' ')
  };
}

const surahNames = {
  2: { id: "Al-Baqarah", en: "Al-Baqarah", ms: "Al-Baqarah" },
  3: { id: "Ali 'Imran", en: "Ali 'Imran", ms: "Ali 'Imran" },
  4: { id: "An-Nisa'", en: "An-Nisa", ms: "An-Nisa'" },
  6: { id: "Al-An'am", en: "Al-An'am", ms: "Al-An'am" },
  8: { id: "Al-Anfal", en: "Al-Anfal", ms: "Al-Anfal" },
  9: { id: "At-Taubah", en: "At-Tawbah", ms: "At-Taubah" },
  15: { id: "Al-Hijr", en: "Al-Hijr", ms: "Al-Hijr" },
  16: { id: "An-Nahl", en: "An-Nahl", ms: "An-Nahl" },
  24: { id: "An-Nur", en: "An-Nur", ms: "An-Nur" },
  31: { id: "Luqman", en: "Luqman", ms: "Luqman" },
  33: { id: "Al-Ahzab", en: "Al-Ahzab", ms: "Al-Ahzab" },
  35: { id: "Fatir", en: "Fatir", ms: "Fatir" },
  39: { id: "Az-Zumar", en: "Az-Zumar", ms: "Az-Zumar" },
  48: { id: "Al-Fath", en: "Al-Fath", ms: "Al-Fath" },
  49: { id: "Al-Hujurat", en: "Al-Hujurat", ms: "Al-Hujurat" },
  55: { id: "Ar-Rahman", en: "Ar-Rahman", ms: "Ar-Rahman" },
  58: { id: "Al-Mujadilah", en: "Al-Mujadila", ms: "Al-Mujadilah" },
  59: { id: "Al-Hasyr", en: "Al-Hashr", ms: "Al-Hasyr" },
  62: { id: "Al-Jumu'ah", en: "Al-Jumu'ah", ms: "Al-Jumu'ah" },
  75: { id: "Al-Qiyamah", en: "Al-Qiyamah", ms: "Al-Qiyamah" },
  76: { id: "Al-Insan", en: "Al-Insan", ms: "Al-Insan" },
  92: { id: "Al-Lail", en: "Al-Layl", ms: "Al-Lail" },
  93: { id: "Adh-Dhuha", en: "Ad-Duha", ms: "Adh-Dhuha" },
  94: { id: "Al-Insyirah", en: "Ash-Sharh", ms: "Al-Insyirah" }
};

function createRef(surahNum, range) {
  const parts = range.split(/[-:–—]/);
  const start = parseInt(parts[0].replace(/\D/g, '')) || 1;
  const end = parts.length > 1 ? (parseInt(parts[1].replace(/\D/g, '')) || start) : start;
  const sn = surahNames[surahNum] || { id: `Surah ${surahNum}`, en: `Surah ${surahNum}`, ms: `Surah ${surahNum}` };
  const verse = getVerseData(surahNum, start, end);

  return {
    surahNumber: surahNum,
    surahName: sn.id,
    surahNameEn: sn.en,
    surahNameMs: sn.ms,
    ayahRange: range,
    verseTextAr: verse.verseTextAr,
    verseTextTranslation: verse.verseTextTranslation,
    verseTextTranslationEn: verse.verseTextTranslationEn,
    verseTextTranslationMs: verse.verseTextTranslationMs,
    verseTextLatin: verse.verseTextLatin,
    verseTextLatinEn: verse.verseTextLatinEn
  };
}

console.log('Generating companions list...');

const companions = [
  // 1. Abu Bakar Ash-Shiddiq
  {
    id: "sahabat-abu-bakar",
    categoryId: "companions",
    title: "Abu Bakar Ash-Shiddiq R.A.",
    titleEn: "Abu Bakr As-Siddiq (R.A.)",
    titleMs: "Abu Bakar Ash-Shiddiq R.A.",
    subtitle: "Sahabat Sejati, Orang Kedua di Gua Tsur & Khalifah Pertama",
    subtitleEn: "The Truthful Friend, Companion in the Cave & First Caliph",
    subtitleMs: "Sahabat Sejati, Teman di Gua Tsur & Khalifah Pertama",
    icon: "ic_faraidh_people",
    readTimeMinutes: 12,
    summary: "Kisah Abu Bakar Ash-Shiddiq R.A., lelaki dewasa pertama yang memeluk Islam, mendampingi Rasulullah di Gua Tsur, mengorbankan seluruh hartanya di jalan Allah, dan memimpin umat dengan keteguhan iman pasca wafatnya Nabi.",
    summaryEn: "The life of Abu Bakr As-Siddiq (R.A.), the first adult male to accept Islam, loyal companion of the Prophet in the Cave of Thawr, who sacrificed all his wealth for the faith and led the Ummah as the first Caliph.",
    summaryMs: "Kisah Abu Bakar Ash-Shiddiq R.A., lelaki dewasa pertama memeluk Islam, teman setia Rasulullah di Gua Tsur, mengorbankan seluruh hartanya demi Islam dan memimpin umat sebagai khalifah pertama.",
    content: `Keislaman Awal dan Gelar Ash-Shiddiq

Abu Bakar bernama asli Abdullah bin Abi Quhafah dari kabilah Bani Taim. Sebelum Islam datang, ia dikenal sebagai saudagar kain yang terpandang, berakhlak mulia, jujur, dan tidak pernah menyembah berhala serta tidak pernah meneguk khamr. Ketika Rasulullah SAW menerima wahyu dan mengajaknya memeluk Islam, Abu Bakar menerima seruan tersebut tanpa sedikit pun keraguan atau kebimbangan di dalam hatinya.

Gelar agung 'Ash-Shiddiq' (yang membenarkan) diperolehnya saat peristiwa agung Isra' Mi'raj. Kala kaum musyrikin Quraisy mencemooh dan menganggap perjalanan satu malam Nabi ke Baitul Maqdis dan Sidratul Muntaha sebagai bualan mustahil, Abu Bakar dengan tegas menyatakan: "Jika Rasulullah yang mengatakannya, maka sungguh ia benar, bahkan jika lebih jauh daripada itu aku pasti mempercayainya."

Pendamping Setia Hijrah di Gua Tsur

Dalam momentum hijrah yang penuh marabahaya, Allah memilih Abu Bakar menjadi pendamping tunggal Rasulullah SAW. Ketika kaum kafir Quraisy mengepung dan berdiri tepat di mulut Gua Tsur hingga ujung kaki mereka terlihat, Abu Bakar merasa sangat cemas demi keselamatan nyawa sang Nabi tercinta: "Wahai Rasulullah, sekiranya seorang dari mereka melihat ke bawah kakinya, niscaya mereka akan melihat kita."

Rasulullah SAW menenangkannya dengan penuh kemantapan tauhid yang diabadikan Allah di dalam Al-Qur'an: "Wahai Abu Bakar, apa prasangkamu terhadap dua orang di mana Allah menjadi pihak ketiganya? Janganlah bersedih, sesungguhnya Allah bersama kita." Peristiwa ini mengabadikan kedudukan Abu Bakar sebagai 'Sahabat Gua' sepanjang masa.

Pengorbanan Harta Tanpa Sisa

Abu Bakar adalah teladan tertinggi kedermawanan dalam sejarah peradaban Islam. Beliau menebus dan memerdekakan para budak yang disiksa secara keji karena mempertahankan akidah, di antaranya Bilal bin Rabah dan Amir bin Fuhairah. 

Pada saat persiapan Perang Tabuk yang memerlukan dana besar di musim paceklik yang terik, Umar bin Khattab datang membawa separuh hartanya dan mengira telah mengungguli Abu Bakar. Namun, Abu Bakar datang menyerahkan seluruh harta miliknya tanpa tersisa. Ketika Rasulullah bertanya: "Apa yang engkau tinggalkan untuk keluargamu, wahai Abu Bakar?", ia menjawab dengan penuh tawakal: "Aku tinggalkan bagi mereka Allah dan Rasul-Nya."

Keteguhan Khalifah Pertama dan Wafatnya

Pasca wafatnya Rasulullah SAW yang mengguncang seluruh Madinah, Abu Bakar tampil sebagai penegak ketenangan dengan khutbah legendarisnya: "Barang siapa menyembah Muhammad, ketahuilah Muhammad telah wafat. Namun barang siapa menyembah Allah, ketahuilah Allah Maha Hidup dan tidak akan pernah mati."

Sebagai khalifah pertama, ia memimpin dengan ketegasan luar biasa menghadapi fitnah nabi palsu dan gerakan enggan membayar zakat dalam Perang Riddah. Ia pula yang memprakarsai kodifikasi pengumpulan lembaran Al-Qur'an menjadi satu mushaf resmi atas usulan Umar bin Khattab. Abu Bakar wafat pada usia 63 tahun dan dimakamkan berdampingan di sisi makam Rasulullah SAW di Madinah.

Hikmah dan Pelajaran Berharga

1. Totalitas Iman dan Pembenaran: Gelar Ash-Shiddiq membuktikan bahwa keimanan sejati adalah kepasrahan tanpa kompromi terhadap petunjuk wahyu ilahi.
2. Tawakal Penuh dalam Segala Keadaan: Peristiwa Gua Tsur mengajarkan bahwa tatkala seluruh jalan duniawi tertutup, pertolongan dan penyertaan Allah adalah jaminan mutlak.
3. Kedermawanan Tanpa Batas: Harta duniawi adalah amanah titipan; membelanjakannya demi tegaknya agama Allah adalah investasi abadi yang tiada merugi.`,
    contentEn: `Early Islam and the Title of As-Siddiq

Abu Bakr's original name was Abdullah ibn Abi Quhafa from the Banu Taym tribe. Before Islam, he was a respected merchant, known for integrity, truthful speech, and never partaking in idol worship or alcohol. When the Prophet Muhammad (peace be upon him) invited him to Islam, Abu Bakr embraced faith instantly without a single moment of doubt.

He earned the noble title 'As-Siddiq' (The Truthful) during the miraculous event of Al-Isra and Al-Mi'raj. While the Quraysh polytheists mocked the Prophet's journey, Abu Bakr declared with unshakeable conviction: "If he said it, then it is undeniably the truth, and I would believe him even beyond that."

Loyal Companion in the Cave of Thawr

During the perilous migration (Hijrah) to Madinah, Allah honored Abu Bakr to be the sole companion of the Messenger. When Quraysh trackers stood right at the mouth of the cave, Abu Bakr feared deeply for the Prophet's safety.

The Prophet calmed his heart with divine reassurance immortalized in the Qur'an: "What do you think of two companions where Allah is their third? Do not grieve; indeed, Allah is with us."

Unmatched Generosity for the Sake of Allah

Abu Bakr spent his fortune purchasing and freeing oppressed enslaved Muslims, including Bilal ibn Rabah. During the Tabuk campaign, Umar brought half of his wealth, hoping to surpass Abu Bakr. Yet Abu Bakr arrived with everything he possessed. When asked what he had left for his family, he replied: "I have left them Allah and His Messenger."

Leadership as Caliph and Final Legacy

Following the Prophet's passing, Abu Bakr stood as a pillar of fortitude, declaring: "Whoever worshipped Muhammad, Muhammad has died. But whoever worships Allah, Allah is Ever-Living and never dies." As the first Caliph, he unified the Muslim community, defeated apostate rebellions, and initiated the official compilation of the Holy Qur'an into a unified mushaf.

Key Lessons and Takeaways

1. Absolute Sincerity in Faith: Sincere conviction accepts divine truth with peace and wholehearted affirmation.
2. Unwavering Trust in Divine Companionship: When physical doors close, Allah's grace and presence provide absolute sanctuary.
3. Radical Generosity: Sacrificing worldly wealth for divine purpose yields eternal honor.`,
    contentMs: `Pengislaman Awal dan Gelaran Ash-Shiddiq

Nama asal Abu Bakar ialah Abdullah bin Abi Quhafah daripada kabilah Bani Taim. Beliau terkenal sebagai saudagar kain yang amanah dan berakhlak mulia. Ketika diajak kepada Islam oleh Rasulullah SAW, beliau menerimanya tanpa sebarang keraguan.

Beliau beroleh gelaran 'Ash-Shiddiq' atas keteguhan membenarkan peristiwa Isra' dan Mi'raj tatkala kaum musyrikin memperlekehkan kebenaran risalah Nabi SAW.

Teman Setia di Gua Tsur

Dalam peristiwa Hijrah, Abu Bakar dipilih mendampingi Baginda SAW. Ketika pihak musuh berada tepat di hadapan pintu gua, Baginda menenangkan kebimbangan Abu Bakar: "Janganlah berdukacita, sesungguhnya Allah bersama kita."

Kedermawanan dan Pengorbanan Harta

Abu Bakar menebus ramai hamba yang diseksa termasuk Bilal bin Rabah. Dalam persiapan Perang Tabuk, beliau menginfakkan seluruh hartanya dan menyerahkan nasib keluarganya kepada perlindungan Allah dan Rasul-Nya semata-mata.

Kepimpinan Khalifah Pertama

Selepas kewafatan Nabi SAW, Abu Bakar memimpin umat dengan ketegasan membasmi golongan murtad serta memulakan pengumpulan mashaf Al-Quran pertama. Beliau wafat pada usia 63 tahun dan disemadikan di sebelah makam Rasulullah SAW.

Hikmah dan Iktibar Utama

1. Kebenaran Iman Sejati: Membenarkan kebenaran tanpa syarat adalah tanda kemuncak keimanan seorang mukmin.
2. Keyakinan kepada Pertolongan Allah: Tiada ketakutan bagi orang yang sentiasa meletakkan pergantungan kepada Allah.
3. Keikhlasan Menafkahkan Harta: Menyerahkan harta demi agama menjamin kedudukan terpuji di akhirat.`,
    quranReferences: [
      createRef(9, "40"),
      createRef(92, "17-21")
    ]
  },

  // 2. Umar bin Khattab
  {
    id: "sahabat-umar-bin-khattab",
    categoryId: "companions",
    title: "Umar bin Khattab R.A.",
    titleEn: "Umar ibn Al-Khattab (R.A.)",
    titleMs: "Umar bin Khattab R.A.",
    subtitle: "Al-Faruq, Pemimpin Keadilan & Pembuka Baitul Maqdis",
    subtitleEn: "Al-Faruq, The Just Caliph & Conqueror of Jerusalem",
    subtitleMs: "Al-Faruq, Pemimpin Keadilan & Pembuka Baitul Maqdis",
    icon: "ic_faraidh_people",
    readTimeMinutes: 12,
    summary: "Kisah Umar bin Khattab R.A., singa padang pasir yang ditakuti musuh, pembela dakwah Islam, khalifah kedua yang meletakkan dasar keadilan hukum, dan pemimpin bersahaja yang membimbing pembebasan Baitul Maqdis.",
    summaryEn: "The life of Umar ibn Al-Khattab (R.A.), Al-Faruq who distinguished truth from falsehood, second Caliph known for absolute justice, who established state welfare and liberated Jerusalem in humility.",
    summaryMs: "Kisah Umar bin Khattab R.A., Al-Faruq yang membezakan kebenaran dan kebatilan, khalifah kedua penegak keadilan undang-undang, serta pembebas kota suci Baitul Maqdis.",
    content: `Hidayah Menggetarkan Singa Quraisy

Sebelum masuk Islam, Umar bin Khattab adalah sosok pendekar Quraisy yang disegani, berbadan tegap, perkasa, dan sangat keras menentang pengikut Rasulullah SAW. Suatu hari dengan pedang terhunus, ia bertekad membunuh Nabi SAW untuk memadamkan dakwah Islam. Namun langkahnya berbelok ketika mengetahui saudara perempuannya, Fatimah binti Khattab, dan suaminya telah memeluk Islam.

Ketika mendengar lantunan ayat-ayat suci Surah Thaha yang dibacakan di rumah saudaranya, kekerasan hati Umar luluh seketika oleh keagungan firman ilahi. Ia segera bergegas menuju Darul Arqam dan bersujud mengikrarkan dua kalimat syahadat di hadapan Rasulullah SAW. Doa Nabi agar Islam dikuatkan oleh salah satu dari dua Umar pun dikabulkan Allah.

Al-Faruq dan Terbukanya Syiar Islam

Keislaman Umar membawa perubahan drastis bagi kaum muslimin di Makkah. Bila sebelumnya kaum mukminin beribadah secara sembunyi-sembunyi, Umar dengan lantang memimpin barisan menuju Ka'bah untuk shalat secara terbuka. Oleh Rasulullah SAW, ia diberi gelar 'Al-Faruq' (sang pembeda antara yang haq dan yang bathil).

Umar memiliki ketajaman nurani dan kecerdasan ruhiyah yang sangat istimewa hingga pandangannya beberapa kali bertepatan dengan turunnya wahyu Al-Qur'an (Muwafaqat Umar), di antaranya saran menjadikan Maqam Ibrahim sebagai tempat shalat, anjuran berhijab bagi para istri Nabi, dan perlakuan atas tawanan Perang Badar.

Kepemimpinan Khalifah yang Menangis Memikirkan Rakyat

Di masa kekhalifahannya selama 10 tahun, wilayah Islam meluas meliputi Syam, Persia, dan Mesir. Namun kemegahan imperium tidak sedikit pun mengubah kezuhudan hidupnya. Umar mengenakan jubah sederhana dengan belasan tambalan kain, tidur beralaskan pelepah kurma di bawah pohon kurma, dan kerap meronda malam hari memikul sendiri karung gandum untuk diserahkan kepada keluarga janda dan anak-anak yang kelaparan.

Prinsip keadilannya yang tersohor: "Seandainya seekor keledai terperosok di pinggiran sungai Eufrat di Irak, aku takut Allah akan menuntutku kelak di hari kiamat mengapa Umar tidak meratakan jalannya."

Pembebasan Damai Baitul Maqdis dan Akhir Hayat

Ketika pasukan Islam berhasil membebaskan kota suci Baitul Maqdis (Yerusalem), Uskup Sophronius menolak menyerahkan kunci kota kecuali kepada Khalifah Umar secara langsung. Umar datang menempuh perjalanan gurun hanya bersama seorang pelayan, menunggangi satu unta secara bergantian. Saat tiba di gerbang Yerusalem, giliran sang pelayanlah yang menunggang unta sementara sang Khalifah agung menuntun talinya dengan pakaian sederhana bersahaja.

Umar wafat syahid akibat ditikam oleh Abu Lu'lu'ah al-Majusi tatkala beliau sedang mengimami shalat Subuh di Masjid Nabawi. Beliau dimakamkan di samping Rasulullah SAW dan sahabat karibnya, Abu Bakar Ash-Shiddiq.

Hikmah dan Pelajaran Berharga

1. Kekuatan Doa dan Hidayah: Tidak ada hati yang terlalu keras bagi hidayah Allah; doa tulus dapat mengubah musuh paling garang menjadi pembela paling setia.
2. Amanah Kepemimpinan Adalah Beban Berat: Kekuasaan dalam Islam bukan kenikmatan fasilitas, melainkan pertanggungjawaban nurani di hadapan Allah SWT.
3. Keadilan Mengalahkan Kemegahan Duniawi: Kehormatan sejati diraih melalui kerendahan hati dan penegakan hukum yang adil tanpa pandang bulu.`,
    contentEn: `The Miraculous Conversion of the Lion of Quraysh

Umar ibn Al-Khattab was initially among the fiercest opponents of Islam. One day, sword drawn, he set out intending to kill the Prophet. However, learning that his sister Fatimah had embraced Islam, he turned to confront her. Upon hearing verses from Surah Taha recited in her home, the divine majesty of the words shattered his hostility, and he accepted Islam at Darul Arqam, answering the Prophet's supplication.

Al-Faruq: Bold Proclamation of Faith

Umar's conversion changed the tide in Makkah, allowing Muslims to pray openly around the Kaaba for the first time. The Prophet titled him 'Al-Faruq', the one who distinguishes truth from falsehood. Several times, Umar's intuitive counsel was confirmed directly by Qur'anic revelations, such as adopting the Station of Abraham for prayer.

The Just Shepherd Who Wept for His Flock

As Caliph, Umar oversaw the expansion of Islam while living in utter asceticism, wearing patched cloaks and patrolling streets by night to deliver flour personally to starving families. He proclaimed: "If a mule stumbles on the banks of the Euphrates, I fear Allah will ask Umar why he did not pave the road."

Liberation of Jerusalem and Martyrdom

Umar entered Jerusalem peacefully to receive the keys from Patriarch Sophronius, walking on foot while his servant rode the camel. Umar was martyred while leading the dawn prayer, stabbed by Abu Lu'lu'ah, and buried beside the Prophet and Abu Bakr.

Key Lessons and Takeaways

1. Transformative Power of Guidance: Sincere openness to divine truth turns fierce adversaries into steadfast champions of faith.
2. Accountability in Leadership: True rulers view authority as an inescapable moral trust before the Creator.
3. Humility Transcends Earthly Power: The highest honor lies in modesty, integrity, and absolute justice.`,
    contentMs: `Hidayah Membuka Hati Umar

Umar bin Khattab pada asalnya seorang musuh Islam yang digeruni. Niatnya untuk membunuh Rasulullah terhenti selepas mendengar bacaan surah Thaha di rumah adiknya. Keagungan firman Allah mencairkan ketegasan hatinya dan beliau memeluk Islam serta-merta.

Al-Faruq dan Kekuatan Umat

Dengan keislaman Umar, umat Islam mula beribadah secara terang-terangan di Masjidil Haram. Baginda SAW menggelarkannya 'Al-Faruq' kerana keteguhannya membezakan antara kebenaran dan kebatilan. Pandangan Umar beberapa kali dipersetujui oleh wahyu Al-Quran.

Keadilan Khalifah yang Bersahaja

Semasa memimpin empayar Islam yang luas, Umar kekal hidup zuhud dengan baju bertampal dan menyusuri lorong-lorong Madinah pada waktu malam bagi menghantar bantuan gandum kepada rakyat yang kelaparan.

Pembukaan Baitul Maqdis

Umar menerima penyerahan kota Baitul Maqdis dengan penuh kerendahan hati dengan menuntun untanya bergilir-gilir bersama hambanya. Beliau syahid ditikam semasa mengimamkan solat Subuh di Masjid Nabawi.

Hikmah dan Iktibar Utama

1. Kuasa Hidayah Ilahi: Firman Allah mampu melembutkan hati yang paling keras sekalipun.
2. Amanah Berat Kepimpinan: Pemimpin bertanggungjawab atas setiap kebajikan rakyat di bawah jagaannya.
3. Keadilan Teras Kemakmuran: Integriti dan keadilan adalah ukuran martabat kepimpinan sejati.`,
    quranReferences: [
      createRef(2, "125"),
      createRef(8, "67")
    ]
  },

  // 3. Utsman bin Affan
  {
    id: "sahabat-utsman-bin-affan",
    categoryId: "companions",
    title: "Utsman bin Affan R.A.",
    titleEn: "Uthman ibn Affan (R.A.)",
    titleMs: "Utsman bin Affan R.A.",
    subtitle: "Dzun Nurain, Saudagar Dermawan & Penghimpun Mushaf Al-Qur'an",
    subtitleEn: "Possessor of Two Lights, Generous Patron & Compiler of the Mushaf",
    subtitleMs: "Dzun Nurain, Saudagar Dermawan & Penghimpun Mushaf Al-Quran",
    icon: "ic_faraidh_people",
    readTimeMinutes: 11,
    summary: "Kisah Utsman bin Affan R.A., pemilik dua cahaya yang menikahi dua putri Nabi, teladan sifat pemalu dan dermawan, penyumbang sumur Rumah dan bala tentara Tabuk, serta khalifah pemersatu bacaan Al-Qur'an.",
    summaryEn: "The life of Uthman ibn Affan (R.A.), Dzun-Nurayn who married two daughters of the Prophet, renowned for boundless charity, modesty, and the standardization of the unified Qur'anic Mushaf.",
    summaryMs: "Kisah Utsman bin Affan R.A., pemilik dua cahaya yang mengahwini dua puteri Rasulullah, ikon sifat pemalu dan pemurah, serta khalifah yang menyeragamkan mushaf Al-Quran.",
    content: `Nasab Mulia dan Gelar Dzun Nurain

Utsman bin Affan berasal dari kabilah Bani Umayyah, seorang bangsawan kaya raya yang terkenal dengan budi pekerti yang sangat santun, tutur kata yang lembut, dan sifat pemalu yang luar biasa. Rasulullah SAW bahkan bersabda tentang beliau: "Tidakkah aku merasa malu kepada seseorang yang para malaikat pun merasa malu kepadanya?"

Beliau diberi gelar 'Dzun Nurain' (Pemilik Dua Cahaya) karena mendapat kehormatan agung menikahi dua putri Rasulullah SAW secara berurutan, yaitu Ruqayyah dan setelah Ruqayyah wafat, beliau menikahi Ummu Kultsum.

Kedermawanan Membeli Surga

Ketika kaum Muhajirin tiba di Madinah dan mengalami krisis air bersih karena satu-satunya sumber air tawar, Sumur Rumah, dikuasai oleh seorang Yahudi yang menjual airnya dengan harga mahal, Utsman maju membelinya dengan harga fantastis demi diwakafkan gratis bagi seluruh kaum muslimin.

Dalam persiapan Ekspedisi Tabuk (Jaisyul Usrah), Utsman menyumbangkan 950 ekor unta dengan muatannya, 50 ekor kuda, dan 1.000 dinar emas yang dituangkan ke pangkuan Rasulullah SAW. Nabi SAW bersabda dengan penuh haru: "Tiada apa pun yang dapat membahayakan Utsman setelah apa yang diperbuatnya pada hari ini."

Standarisasi Mushaf Utsmani yang Abadi

Karya terbesar Utsman bin Affan bagi peradaban umat manusia adalah pengumpulan dan penyatuan mushaf Al-Qur'an resmi (Mushaf Utsmani). Ketika wilayah Islam meluas hingga ke Armenia dan Azerbaijan, sahabat Hudzaifah bin Al-Yaman melaporkan adanya perselisihan dialek bacaan Al-Qur'an di kalangan prajurit yang berpotensi memecah belah umat.

Utsman segera membentuk panitia kodifikasi yang diketuai oleh Zaid bin Tsabit untuk menyalin mushaf induk dengan rasm yang seragam. Salinan mushaf ini kemudian dikirimkan ke berbagai penjuru negeri Islam (Kufah, Bashrah, Syam, Makkah, dan satu disimpan di Madinah). Warisan mushaf inilah yang menjadi pedoman seluruh umat Islam di dunia hingga akhir zaman.

Kesabaran Menghadapi Fitnah dan Syahidnya Utsman

Di akhir masa kekhalifahannya, fitnah pemberontakan merebak yang dipicu oleh hasutan kaum munafik. Para pemberontak mengepung kediaman Utsman di Madinah dan memutus pasokan air minumnya. Meskipun para sahabat seperti Ali, Hasan, dan Husain siap menghunus pedang membelanya, Utsman menolak keras pertumpahan darah sesama muslim di kota Nabi: "Aku tidak ingin menjadi orang pertama yang menumpahkan darah kaum mukminin demi mempertahankan kekuasaanku."

Pada hari Jumat, dalam keadaan berpuasa dan sedang membaca Al-Qur'an, para pemberontak menerobos masuk dan menebaskan pedang. Darah suci sang Khalifah membasahi lembaran Al-Qur'an yang sedang dibacanya, tepat pada ayat: "Maka Allah akan memelihara kamu dari mereka. Dan Dialah Yang Maha Mendengar lagi Maha Mengetahui." (QS. Al-Baqarah: 137).

Hikmah dan Pelajaran Berharga

1. Keagungan Sifat Malu: Rasa malu adalah cabang iman yang agung; malu berbuat maksiat melahirkan kemuliaan kepribadian dan dicintai malaikat.
2. Harta Sebagai Sarana Akhirat: Kekayaan sejati adalah kekayaan yang diinvestasikan untuk kesejahteraan umat dan meninggikan syiar Islam.
3. Kesabaran Melepaskan Ambisi Dunia: Utsman lebih memilih kehilangan nyawanya daripada menumpahkan setetes darah sesama saudara seiman.`,
    contentEn: `Noble Lineage and the Possessor of Two Lights

Uthman ibn Affan belonged to the wealthy Umayyad clan, celebrated for extraordinary generosity, gentle manners, and profound modesty. The Prophet said of him: "Should I not feel modest before a man before whom the angels feel modest?" He was honored with the title Dzun-Nurayn (Possessor of Two Lights) by marrying two daughters of the Prophet consecutively: Ruqayyah, and after her passing, Umm Kulthum.

Purchasing Paradise with Boundless Wealth

When Muslims faced severe water shortages in Madinah, Uthman bought the well of Rumah and dedicated it as a perpetual endowment for public use. During the hardship campaign of Tabuk, he outfitted the army with hundreds of camels, horses, and gold coins, prompting the Prophet to proclaim: "Nothing Uthman does after today can harm him."

Standardization of the Holy Qur'an

Uthman's greatest enduring contribution was the compilation of the standardized Qur'anic Mushaf. Observing emerging disputes over recitation dialects across expanding borders, he commissioned Zaid ibn Thabit to record the Qur'an in the standard dialect of Quraysh and distributed official copies to major Islamic centers.

Patience under Siege and Martyrdom

When rebel factions besieged his house in Madinah demanding his abdication, Uthman refused to spill Muslim blood for his own protection. While fasting and reciting the Qur'an, assassins broke in and martyred him, his blood spilling across the sacred pages.

Key Lessons and Takeaways

1. The Virtue of Modesty: Modesty adorns true faith and earns the veneration of both angels and humankind.
2. Wealth as a Tool for the Divine: Dedicating prosperity to charitable endowments creates timeless legacy.
3. Forbearance in Crisis: Sacrificing personal power to prevent communal bloodshed is the hallmark of noble stewardship.`,
    contentMs: `Kemuliaan Dzun Nurain

Utsman bin Affan terkenal sebagai hartawan yang berakhlak terpuji dan memiliki sifat pemalu yang tinggi sehingga para malaikat turut merasa malu kepadanya. Beliau digelar Dzun Nurain kerana mengahwini dua puteri Rasulullah SAW, Ruqayyah dan Ummu Kultsum.

Sedekah Membuka Jalan ke Syurga

Beliau mewakafkan telaga Rumah untuk kegunaan umat Islam Madinah serta membiayai sebahagian besar kelengkapan tentera Usrah dalam Perang Tabuk.

Penyelarasan Mushaf Utsmani

Utsman menyelamatkan perpaduan umat dengan menyelaraskan bacaan dan rasm Al-Quran ke dalam satu mushaf rasmi yang disalin dan diedarkan ke wilayah-wilayah Islam.

Syahid Mempertahankan Keamanan Umat

Tatkala rumahnya dikepung oleh pemberontak, Utsman menolak penggunaan senjata bagi mengelakkan pertumpahan darah sesama muslim. Beliau syahid dibunuh ketika sedang membaca Al-Quran dalam keadaan berpuasa.

Hikmah dan Iktibar Utama

1. Nilai Sifat Pemalu: Sifat malu adalah perisai daripada maksiat dan cerminan akhlak mulia.
2. Infaq demi Kemaslahatan Ummah: Harta yang dibelanjakan pada jalan Allah menjadi pahala berterusan yang tidak terputus.
3. Menjauhi Fitnah Sesama Muslim: Mengutamakan perpaduan dan mengelakkan fitnah darah sesama saudara seiman.`,
    quranReferences: [
      createRef(39, "9"),
      createRef(2, "261")
    ]
  },

  // 4. Ali bin Abi Thalib
  {
    id: "sahabat-ali-bin-abi-thalib",
    categoryId: "companions",
    title: "Ali bin Abi Thalib R.A.",
    titleEn: "Ali ibn Abi Talib (R.A.)",
    titleMs: "Ali bin Abi Thalib R.A.",
    subtitle: "Babul Ilmi, Singa Khaibar & Pahlawan Pengorbanan",
    subtitleEn: "Gate of Knowledge, Hero of Khaybar & Pillar of Valour",
    subtitleMs: "Babul Ilmi, Singa Khaibar & Pahlawan Pengorbanan",
    icon: "ic_faraidh_people",
    readTimeMinutes: 12,
    summary: "Kisah Ali bin Abi Thalib R.A., sepupu sekaligus menantu Rasulullah, pemuda pertama yang beriman, kesatria pemberani yang tidur di ranjang Nabi saat malam hijrah, penakluk benteng Khaibar, dan pintu gerbang ilmu hikmah.",
    summaryEn: "The life of Ali ibn Abi Talib (R.A.), the Prophet's cousin and son-in-law, the first youth to embrace Islam, hero of Khaybar, and celebrated as the gate to the city of prophetic wisdom.",
    summaryMs: "Kisah Ali bin Abi Thalib R.A., sepupu dan menantu Rasulullah, pemuda pertama memeluk Islam, pahlawan gagah berani di benteng Khaibar, dan pintu gerbang perbendaharaan ilmu.",
    content: `Didikan Kenabian dan Pemuda Pertama Beriman

Ali bin Abi Thalib diasuh dan dibesarkan langsung di rumah Rasulullah SAW sejak masa kanak-kanak sebagai bentuk balas budi Nabi kepada pamannya, Abu Thalib. Ketika wahyu pertama turun, Ali yang baru berusia sekitar 10 tahun menyaksikan Nabi dan Khadijah shalat. Tanpa ragu, kecerdasan fitrahnya menuntunnya menjadi anak-anak/pemuda pertama yang memeluk risalah Islam.

Hubungan Ali dengan Rasulullah sangatlah dekat, sebagaimana sabda beliau: "Kedudukanmu di sisiku laksana kedudukan Harun di sisi Musa, hanya saja tidak ada nabi setelahku." Ali kelak dianugerahi kehormatan agung menikahi putri tercinta Nabi, Sayyidah Fatimah Az-Zahra.

Pengorbanan Jiwa pada Malam Hijrah

Keberanian Ali mencapai puncaknya pada malam hijrah yang menegangkan. Tatkala para pemuda terpilih dari kabilah-kabilah musyrik Quraisy mengepung rumah Nabi dengan pedang terhunus siap membunuh, Rasulullah meminta Ali untuk berbaring di atas ranjang beliau berselimutkan kain hijau penutup beliau.

Ali menerima amanah maut tersebut dengan penuh ketenangan dan kerelaan jiwa, mengorbankan nyawanya demi mengelabui pandangan musuh dan memastikan keselamatan hijrah sang Nabi. Tindakan kepahlawanan ini dipuji Allah dalam Al-Qur'an sebagai bukti perniagaan jiwa demi meraih ridha Allah.

Singa Penakluk Benteng Khaibar dan Pintu Ilmu

Di medan pertempuran, Ali adalah kesatria tak tertandingi yang memegang panji Islam. Pada Perang Khaibar, setelah beberapa pasukan gagal menembus benteng pertahanan musuh yang kokoh, Rasulullah SAW bersabda: "Besok sungguh aku akan menyerahkan bendera ini kepada seorang lelaki yang mencintai Allah dan Rasul-Nya, serta dicintai oleh Allah dan Rasul-Nya; Allah akan membuka benteng ini melalui kedua tangannya."

Keesokan harinya, Nabi memanggil Ali yang sedang sakit mata, meludahi kedua matanya hingga sembuh seketika, dan menyerahkan panji. Ali maju mendobrak gerbang benteng Khaibar dan memimpin kemenangan gilang-gemilang. Selain perkasa di medan tempur, Ali adalah rujukan tertinggi para sahabat dalam urusan hukum dan peradilan, sebagaimana sabda Nabi: "Aku adalah kota ilmu dan Ali adalah pintu gerbangnya."

Kekhalifahan Penuh Ujian dan Akhir Syahid

Sebagai khalifah keempat, Ali memimpin di tengah badai fitnah perpecahan yang melanda umat. Beliau tetap memegang teguh prinsip kebenaran dan kezuhudan, menolak memperkaya diri dan kerabatnya. Ali wafat syahid akibat ditebas pedang beracun oleh Abdurrahman bin Muljam dari kaum Khawarij saat hendak mengimami shalat Subuh di Kufah pada bulan suci Ramadan.

Hikmah dan Pelajaran Berharga

1. Pengorbanan Jiwa Demi Risalah: Keberanian sejati adalah kesiapan mengorbankan diri demi keselamatan agama dan kebenaran ilahi.
2. Keseimbangan Antara Ilmu dan Keberanian: Kekuatan fisik yang dahsyat harus diimbangi dengan kedalaman ilmu, hikmah, dan keadilan moral.
3. Kezuhudan Pemimpin Sejati: Memegang tampuk kekuasaan tanpa terpikat oleh kemewahan duniawi adalah warisan mulia generasi salafus shalih.`,
    contentEn: `Prophetic Upbringing and the First Youth to Believe

Raised in the household of the Prophet from early childhood, Ali ibn Abi Talib was about ten years old when he witnessed the Prophet and Khadijah in prayer. With pure instinct, he became the first child and youth to accept Islam. The Prophet loved him deeply, later saying: "You are to me as Aaron was to Moses, except there is no prophet after me," and giving him his beloved daughter Fatimah in marriage.

Selfless Valor on the Night of Hijrah

Ali's courage shone brilliantly on the night of migration. As Quraysh assassins encircled the Prophet's house, the Prophet asked Ali to lie in his bed covered by his green mantle. Ali calmly accepted this near-certain death sentence to buy time for the Prophet's safe escape, a sacrifice immortalized in the Qur'an.

Hero of Khaybar and Gate of Wisdom

At the Battle of Khaybar, the Prophet declared: "Tomorrow I will give this banner to a man who loves Allah and His Messenger, and whom Allah and His Messenger love." Handed the banner, Ali breached the impenetrable fortress. In addition to peerless martial skill, Ali was celebrated for legal brilliance, described by the Prophet as the gate to the city of knowledge.

Righteous Caliphate and Martyrdom

As the fourth Caliph, Ali guided the community through turbulent internal trials with unbending adherence to justice and austerity. He was martyred by an assassin's poisoned blade while walking to lead dawn prayer in Kufah.

Key Lessons and Takeaways

1. Fearless Commitment to Truth: True bravery willingly places one's life on the line for divine protection.
2. Harmony of Mind and Might: Heroism requires deep spiritual wisdom and scholarly insight alongside physical strength.
3. Integrity in Adversity: Upholding justice remains mandatory even amid severe political turbulence.`,
    contentMs: `Didikan Rasulullah dan Pemuda Pertama Beriman

Ali bin Abi Thalib membesar di bawah asuhan Rasulullah SAW dan menjadi kanak-kanak pertama memeluk Islam pada usia sekitar 10 tahun. Hubungannya yang amat rapat dengan Rasulullah membuahkan pernikahan mulia bersama Sayyidah Fatimah Az-Zahra.

Pengorbanan Nyawa pada Malam Hijrah

Ali sanggup mempertaruhkan nyawanya tidur di atas katil Rasulullah berselimutkan jubah Baginda bagi mengelirukan kepungan pembunuh musyrikin Makkah semasa malam hijrah.

Singa Penakluk Khaibar dan Gedung Ilmu

Di Khaibar, Ali memimpin kemenangan menumbangkan kubu terkuat musuh. Baginda SAW mengiktiraf Ali sebagai pintu gerbang kota ilmu pengetahuan kerana keluasan pemahaman syariatnya.

Kekhalifahan yang Tegas dan Syahid

Semasa memegang jawatan khalifah keempat, Ali tetap hidup sederhana dan adil dalam menghadapi fitnah dalaman umat. Beliau syahid ditikam semasa menuju ke masjid untuk solat Subuh di Kufah.

Hikmah dan Iktibar Utama

1. Keberanian Mengorbankan Nyawa: Rela berkorban demi mempertahankan risalah dan keselamatan pemimpin umat.
2. Gabungan Ilmu dan Ketangkasan: Kekuatan sebenar seorang mukmin adalah gabungan ketajaman akal fikiran dan keberanian amal.
3. Zuhud dalam Pemerintahan: Menjauhi kemewahan dunia ketika memegang amanah kekuasaan rakyat.`,
    quranReferences: [
      createRef(2, "207"),
      createRef(3, "61")
    ]
  },

  // 5. Khadijah binti Khuwailid
  {
    id: "sahabat-khadijah",
    categoryId: "companions",
    title: "Khadijah binti Khuwailid R.A.",
    titleEn: "Khadijah bint Khuwaylid (R.A.)",
    titleMs: "Khadijah binti Khuwailid R.A.",
    subtitle: "Ummul Mukminin, Penentram Hati Nabi & Pembela Dakwah Awal",
    subtitleEn: "Mother of the Believers, First to Believe & Queen of Devotion",
    subtitleMs: "Ummul Mukminin, Penenteram Jiwa Nabi & Penyokong Dakwah",
    icon: "ic_faraidh_people",
    readTimeMinutes: 10,
    summary: "Kisah Khadijah binti Khuwailid R.A., wanita teragung yang pertama beriman, saudagar sukses bergelar Ath-Thahirah, penentram hati Rasulullah saat wahyu pertama turun, dan penyokong seluruh hartanya bagi dakwah Islam.",
    summaryEn: "The life of Khadijah bint Khuwaylid (R.A.), the first human to believe in Islam, revered noblewoman, devoted wife who comforted the Prophet during first revelation, and supported Islam with all her wealth.",
    summaryMs: "Kisah Khadijah binti Khuwailid R.A., wanita terulung yang pertama beriman, penenang jiwa Nabi tatkala wahyu pertama turun di Gua Hira, serta mengorbankan seluruh hartanya demi Islam.",
    content: `Wanita Suci dan Pernikahan Berkah

Khadijah binti Khuwailid adalah bangsawan Quraisy terhormat dan saudagar wanita kaya raya yang berakhlak mulia hingga digelari 'Ath-Thahirah' (wanita yang suci). Tertarik oleh reputasi kejujuran dan amanah Muhammad muda, Khadijah mempercayakan kafilah dagangnya kepadanya. 

Melihat kemuliaan budi pekerti baginda melalui laporan pelayannya Maisarah, Khadijah meminang Muhammad SAW. Pernikahan mereka diliputi sakinah, mawaddah, dan rahmah, serta melahirkan keturunan mulia termasuk Sayyidah Fatimah.

Penentram Jiwa Saat Wahyu Pertama Turun

Ketika Rasulullah SAW menerima wahyu pertama di Gua Hira dan pulang dalam keadaan menggigil gemetar oleh kedahsyatan perjumpaan dengan Malaikat Jibril seraya berseru: "Selimuti aku! Selimuti aku!", Khadijah menyambutnya dengan kehangatan dan ketenangan jiwa yang luar biasa.

Dengan kata-kata yang terukir abadi dalam sejarah, Khadijah menenangkan sang Nabi: "Demi Allah, Allah tidak akan sekali-kali menghinakanmu. Sesungguhnya engkau senantiasa menyambung tali silaturahim, menolong orang yang lemah, menanggung orang yang kesusahan, memuliakan tamu, dan membela kebenaran." Khadijah kemudian membawa Nabi menemui sepupunya, Waraqah bin Naufal, yang mengonfirmasi bahwa yang datang adalah malaikat wahyu.

Pengorbanan Total dan Salam dari Allah

Khadijah menyerahkan seluruh kekayaannya yang melimpah untuk menopang dakwah Islam dan membiayai kaum muslimin, terutama saat masa pemboikotan keji selama tiga tahun di Syi'ib Abi Thalib. 

Karena kesetiaan dan pengorbanannya yang tiada tara, Malaikat Jibril pernah menyampaikan kabar gembira salam dari Allah SWT untuk Khadijah serta istana megah di surga yang terbuat dari mutiara berongga, yang di dalamnya tiada kegaduhan dan tiada kepenatan.

Tahun Kesedihan dan Kenangan Abadi

Khadijah wafat pada tahun ke-10 kenabian, disusul oleh wafatnya Abu Thalib, hingga tahun tersebut dinamai 'Amul Huzni' (Tahun Kesedihan). Rasulullah SAW senantiasa mengenang kebaikan Khadijah sepanjang hayatnya dan selalu memuliakan sahabat-sahabat almarhumah istrinya tersebut.

Hikmah dan Pelajaran Berharga

1. Peran Sentral Pendamping Hidup: Kata-kata penyejuk dan dukungan moril seorang istri adalah penopang terkuat di saat-saat paling genting dalam hidup.
2. Kedermawanan Membela Nilai Luhur: Harta kekayaan mencapai puncak nilainya tatkala dikorbankan demi meninggikan nilai-nilai kebenaran dan kemanusiaan.
3. Kemuliaan Dibalas Kemuliaan: Kesetiaan sejati Khadijah diganjar dengan salam penghormatan langsung dari Allah dan istana abadi di surga.`,
    contentEn: `The Pure Noblewoman and Blessed Marriage

Khadijah bint Khuwaylid was a prominent Quraysh businesswoman known for exceptional virtue, earning the title 'Ath-Tahirah' (The Pure). Impressed by young Muhammad's honesty (Al-Amin), she proposed marriage. Their union was filled with deep love and devotion.

The Anchor in the Storm of First Revelation

When the Prophet received the first revelation in Cave Hira and returned trembling, crying: "Cover me! Cover me!", Khadijah embraced him with soothing wisdom: "By Allah, Allah will never disgrace you. You uphold family ties, help the helpless, honor guests, and support the truth." She became the very first person to accept Islam.

Total Sacrifice and Greeting from Allah

Khadijah poured her vast fortune into supporting Muslims, particularly during the harsh three-year boycott in Makkah. Gabriel once visited the Prophet, instructing him to convey greetings from Allah to Khadijah and the glad tidings of a palace of pearls in Paradise, free of noise and toil.

The Year of Sorrow

Her passing in the tenth year of prophethood broke the Prophet's heart, marking the Year of Sorrow ('Am al-Huzn). The Prophet cherished her memory for the rest of his life.

Key Lessons and Takeaways

1. The Power of Emotional Support: Reassurance and unwavering faith from a partner can ground a soul in moments of profound vulnerability.
2. Wealth in Service of the Sacred: Material riches achieve true value when expended in the path of righteousness.
3. Eternal Honor for Faithfulness: Devotion to truth earns recognition from the Creator Himself.`,
    contentMs: `Wanita Suci dan Perkahwinan Barakah

Khadijah binti Khuwailid merupakan tokoh perniagaan wanita yang amat dihormati dan digelar Ath-Thahirah. Tertarik dengan kejujuran Nabi Muhammad SAW, beliau menyunting Baginda menjadi suaminya.

Penenang Jiwa Tatkala Turun Wahyu

Ketika Nabi SAW pulang dalam keadaan menggeletar ketakutan selepas menerima wahyu di Gua Hira, Khadijah menenangkan Baginda dengan penuh kelembutan dan keyakinan bahawa Allah tidak akan mensia-siakan orang yang suka menolong insan lain. Beliau insan pertama memeluk Islam.

Pengorbanan Harta dan Salam Ilahi

Khadijah mengorbankan seluruh hartanya bagi menyokong dakwah dan membantu umat Islam semasa pemboikotan di Syi'ib Abi Thalib. Malaikat Jibril pernah menyampaikan salam khusus daripada Allah SWT kepadanya berserta janji mahligai di syurga.

Tahun Kesedihan

Kewafatan Khadijah amat meruntun hati Rasulullah SAW sehingga tahun tersebut dinamakan Tahun Kesedihan. Baginda sentiasa mengenang jasa dan memuliakan kenangan Khadijah sepanjang hayatnya.

Hikmah dan Iktibar Utama

1. Sokongan Padu Rakan Hidup: Kata-kata semangat dan ketenangan seorang isteri adalah penguat jiwa suami dalam menghadapi ujian besar.
2. Infaq Segala Demi Kebenaran: Mengorbankan kekayaan duniawi demi kejayaan dakwah Islamiah.
3. Ganjaran Kemuliaan Syurga: Kesetiaan dan pengorbanan suci dibalas dengan penghormatan tertinggi di sisi Allah SWT.`,
    quranReferences: [
      createRef(93, "8"),
      createRef(94, "1-3")
    ]
  },

  // 6. Aisyah binti Abu Bakar
  {
    id: "sahabat-aisyah",
    categoryId: "companions",
    title: "Aisyah binti Abu Bakar R.A.",
    titleEn: "Aisha bint Abi Bakr (R.A.)",
    titleMs: "Aisyah binti Abu Bakar R.A.",
    subtitle: "Al-Humaira, Ibunda Orang Mukmin & Samudra Ilmu Hadits",
    subtitleEn: "Mother of the Believers, Master of Jurisprudence & Hadith",
    subtitleMs: "Al-Humaira, Ummul Mukminin & Tokoh Keilmuan Islam",
    icon: "ic_faraidh_people",
    readTimeMinutes: 11,
    summary: "Kisah Sayyidah Aisyah binti Abu Bakar R.A., istri tercinta Rasulullah bergelar Al-Humaira, perawi ribuan hadits, wanita paling alim dalam fikih dan ilmu faraidh, serta kesuciannya dibersihkan langsung oleh firman Allah dari langit.",
    summaryEn: "The life of Aisha bint Abi Bakr (R.A.), beloved wife of the Prophet, scholar of scholars who narrated over 2,200 hadiths, master of law and medicine, whose innocence was declared in the Holy Qur'an.",
    summaryMs: "Kisah Sayyidah Aisyah binti Abu Bakar R.A., isteri Rasulullah yang paling cerdas, perawi ribuan hadis, pakar hukum syarak dan ilmu perubatan, serta kesuciannya dibuktikan oleh wahyu Al-Quran.",
    content: `Kecerdasan Luar Biasa Sang Humaira

Aisyah binti Abu Bakar adalah putri kesayangan sahabat terdekat Nabi, Abu Bakar Ash-Shiddiq. Beliau dianugerahi daya ingat yang luar biasa tajam, kecerdasan intelektual yang tinggi, kefasihan berbahasa, dan rasa ingin tahu yang mendalam tentang hikmah di balik setiap syariat yang diturunkan Allah.

Rasulullah SAW sangat mencintainya dan memanggilnya dengan panggilan sayang 'Al-Humaira' (yang pipinya kemerah-merahan). Di rumah kecil Aisyah di samping Masjid Nabawi, wahyu ilahi kerap kali turun tatkala Nabi sedang berada di sisinya.

Fitnah Besar Haditsul Ifki dan Pembelaan dari Langit

Dalam perjalanan pulang dari Ekspedisi Bani Musthaliq, Aisyah tertinggal dari kafilah karena mencari kalungnya yang hilang, hingga kemudian diantar pulang oleh Shafwan bin Mu'athal. Kaum munafik pimpinan Abdullah bin Ubay bin Salul memanfaatkan momentum ini untuk menyebarkan fitnah keji yang mengguncang Madinah (Haditsul Ifki).

Selama sebulan penuh Aisyah jatuh sakit dan menangis dalam kepasrahan total hanya kepada Allah. Di tengah penderitaan batin tersebut, Allah SWT menurunkan pembelaan agung melalui sepuluh ayat dalam Surah An-Nur (ayat 11-20), yang menegaskan kesucian mutlak Aisyah dan mengancam para penyebar fitnah dengan azab yang pedih di dunia dan akhirat.

Gedung Ilmu Fikih dan Perawi Hadits Terbesar

Sepeninggal Rasulullah SAW yang wafat di pangkuan dan di kamarnya, Aisyah menjadi rujukan keilmuan tertinggi bagi seluruh sahabat Nabi dan generasi tabi'in. Beliau meriwayatkan lebih dari 2.210 hadits shahih yang mencakup hukum ibadah, muamalah, akhlak, dan tata cara kehidupan rumah tangga Nabi.

Para sahabat terkemuka berkata: "Tidaklah kami menghadapi suatu permasalahan rumit dalam urusan agama melainkan kami menanyakannya kepada Aisyah, dan kami selalu menemukan ilmu penyelesaian darinya." Beliau juga mahir dalam ilmu silsilah bangsa Arab, syair, dan ilmu pengobatan.

Kedermawanan dan Wafatnya Sang Pendidik Umat

Meskipun menerima tunjangan yang besar dari kas Baitul Mal di masa kekhalifahan, Aisyah tidak pernah menyimpan uang tersebut hingga malam hari. Beliau segera membagikannya kepada fakir miskin padahal pakaian beliau sendiri penuh tambalan dan beliau sedang berpuasa. Aisyah wafat pada usia 66 tahun di Madinah dan dimakamkan di Pemakaman Jannatul Baqi'.

Hikmah dan Pelajaran Berharga

1. Teladan Keilmuan Wanita: Islam memuliakan pendidikan dan mengangkat kecerdasan wanita ke puncak kepemimpinan intelektual umat.
2. Ketabahan Menghadapi Ujian Reputasi: Kepasrahan dan kesabaran orang beriman saat dizalimi akan selalu berbuah pembelaan dan pertolongan dari Allah SWT.
3. Kedermawanan Bersahaja: Keluasan ilmu hakiki selalu melahirkan kezuhudan, kerendahan hati, dan kepedulian tulus terhadap sesama.`,
    contentEn: `Exceptional Intellect and the Prophet's Beloved

Aisha bint Abi Bakr was gifted with phenomenal memory, eloquence, and sharp scholarly inquiry. The Prophet lovingly called her 'Al-Humayra'. Living right beside the Prophet's mosque, she witnessed the context of revelations and the intimate Sunnah of daily prophetic life.

The Great Slander and Vindication from Above

During return from the Banu Mustaliq expedition, Aisha was accidentally left behind while searching for a lost necklace, later escorted back safely by Safwan ibn Al-Mu'attal. Hypocrites weaponized this innocent event into malicious slander (Hadith al-Ifk). Aisha wept in patient reliance upon God until Allah exonerated her through ten verses of Surah An-Nur, permanently establishing her purity.

Master Scholar and Preserver of Sunnah

Following the Prophet's passing in her quarters, Aisha became the premier teacher of the Ummah, narrating over 2,210 hadiths that illuminate Islamic family ethics and ritual law. Senior companions constantly consulted her on complex rulings, poetry, and medicine.

Generosity and Resting Place

Despite receiving substantial state stipends, she distributed every dirham to orphans and the impoverished before dusk while fasting herself. She passed away at 66 and rests in Jannat al-Baqi'.

Key Lessons and Takeaways

1. Peak of Female Scholarship: Islam champions the intellectual and spiritual empowerment of women.
2. Patience Under False Accusation: Entrusting one's integrity to Allah brings divine vindication.
3. Generosity Rooted in Knowledge: Deep understanding of religion inspires detachment from materialism.`,
    contentMs: `Kecerdasan Luar Biasa Al-Humaira

Sayyidah Aisyah r.a. dianugerahkan daya ingatan yang luar biasa dan pemahaman agama yang mendalam. Beliau menjadi rujukan hukum syarak dan meriwayatkan lebih daripada 2,210 hadis sahih.

Fitnah Haditsul Ifki dan Pembelaan Wahyu

Ketika difitnah secara kejam oleh golongan munafik, Aisyah bersabar dan menyerahkan urusannya kepada Allah. Allah SWT menurunkan 10 ayat dalam Surah An-Nur membersihkan kesuciannya daripada sebarang tuduhan jahat.

Lautan Ilmu Pengetahuan Umat

Para sahabat besar sentiasa merujuk kepada Aisyah dalam permasalahan hukum fikih, faraidh, sastera dan perubatan. Beliau juga terkenal dengan sifat pemurah menyedekahkan segala hartanya kepada fakir miskin.

Hikmah dan Iktibar Utama

1. Martabat Ilmu Wanita dalam Islam: Wanita berpeluang mencapai kemuncak keilmuan dan menjadi guru kepada seluruh umat.
2. Kesabaran Menghadapi Ujian Maruah: Keyakinan kepada Allah membuahkan pembelaan yang nyata di dunia dan akhirat.
3. Kezuhudan Orang Berilmu: Memanfaatkan kelebihan duniawi untuk kebajikan dan akhirat semata-mata.`,
    quranReferences: [
      createRef(24, "11-12"),
      createRef(24, "26")
    ]
  },

  // 7. Bilal bin Rabah
  {
    id: "sahabat-bilal-bin-rabah",
    categoryId: "companions",
    title: "Bilal bin Rabah R.A.",
    titleEn: "Bilal ibn Rabah (R.A.)",
    titleMs: "Bilal bin Rabah R.A.",
    subtitle: "Muazin Pertama Islam & Simbol Keteguhan Tauhid 'Ahad! Ahad!'",
    subtitleEn: "First Mu'adhin of Islam & Symbol of Unshakeable Monotheism",
    subtitleMs: "Muazin Pertama Islam & Simbol Keteguhan Tauhid 'Ahad! Ahad!'",
    icon: "ic_faraidh_people",
    readTimeMinutes: 10,
    summary: "Kisah Bilal bin Rabah R.A., budak belian Habsyi yang disiksa kejam di bawah terik pasir Makkah namun teguh mengumandangkan 'Ahad! Ahad!', muazin pertama Rasulullah yang azannya di atas Ka'bah mematahkan rasisme jahiliyah.",
    summaryEn: "The life of Bilal ibn Rabah (R.A.), the Abyssinian enslaved believer who endured agonizing torture proclaiming 'Ahad! Ahad!', honored as the first caller to prayer whose call atop the Kaaba crushed tribal racism.",
    summaryMs: "Kisah Bilal bin Rabah R.A., hamba Habsyi yang tabah menghadapi seksaan padang pasir dengan seruan tauhid 'Ahad! Ahad!', serta muazin pertama yang melaungkan azan di atas Kaabah.",
    content: `Keteguhan Tauhid di Bawah Batu Terik

Bilal bin Rabah adalah seorang hamba sahaya berkulit hitam asal Habasyah (Ethiopia) milik pembesar kafir Quraisy, Umayyah bin Khalaf. Ketika cahaya kebenaran Islam menyentuh relung hatinya, Bilal memeluk tauhid secara rahasia. Tatkala keislamannya terbongkar, Umayyah murka dan menjatuhkan siksaan yang luar biasa kejam.

Di tengah teriknya pasir padang gurun Makkah di bawah sengatan matahari tengah hari yang membakar, leher Bilal diikat dengan tali dan diseret oleh anak-anak Quraisy. Tubuhnya ditelentangkan di atas batu padas yang membara, lalu dadanya ditindih dengan batu besar yang nyaris meremukkan tulang rusuknya. Di tengah derita sekarat tersebut, dari bibir Bilal yang kering hanya keluar satu kata abadi yang mengguncang kebatilan: "Ahad! Ahad! (Allah Maha Esa! Allah Maha Esa!)".

Pembebasan oleh Abu Bakar dan Fajar Kemuliaan

Melihat ketabahan iman Bilal yang menakjubkan, Abu Bakar Ash-Shiddiq tergerak menebusnya dengan harga berlipat ganda dan memerdekakannya demi mencari ridha Allah. Rasulullah SAW menyambut Bilal dengan penuh kasih sayang dan mengangkat derajatnya.

Ketika syariat azan diperintahkan di Madinah pasca mimpi shahih para sahabat, Rasulullah SAW memilih Bilal sebagai muazin pertama dalam sejarah Islam karena suaranya yang merdu, lantang, dan penuh getaran keimanan yang menembus kalbu para pendengarnya.

Suara Tauhid di Puncak Ka'bah

Puncak pembebasan dan kesetaraan dalam Islam terjadi pada peristiwa Fathu Makkah (Penaklukan Kota Makkah). Di hadapan ribuan kaum Quraisy yang dahulu menindasnya, Rasulullah SAW memerintahkan Bilal naik ke atap Ka'bah dan mengumandangkan kalimat takbir serta syahadat azan pertama di kota suci tersebut.

Momen ini menjadi pukulan telak yang menghancurkan tradisi rasisme, kasta darah, dan kebanggaan nasab jahiliyah, membuktikan bahwa kemuliaan di sisi Allah semata-mata diukur dari ketakwaan hati.

Langkah Kaki di Surga dan Air Mata Kerinduan

Rasulullah SAW pernah bersabda kepada Bilal: "Wahai Bilal, ceritakan kepadaku amalan apa yang paling engkau harapkan pahalanya dalam Islam, karena sungguh aku mendengar derap suara sandalmu mendahuluiku di surga." Bilal menjawab bahwa ia tidak pernah berwudhu melainkan selalu menunaikan shalat sunnah dua rakaat setelahnya (Shalat Syukrul Wudhu).

Sepeninggal Rasulullah SAW, Bilal merasa tidak sanggup lagi mengumandangkan azan di Madinah karena selalu tersedu-sedu menangis teringat baginda Nabi. Bilal ikut berhijrah dan berjihad ke negeri Syam hingga wafat di Damaskus dalam kerinduan berjumpa sang kekasih, Rasulullah SAW.

Hikmah dan Pelajaran Berharga

1. Keteguhan Akidah di Atas Segalanya: Iman sejati tidak dapat dibeli atau digoyahkan oleh siksaan fisik maupun tekanan duniawi terberat sekalipun.
2. Kesetaraan Mutlak Manusia dalam Islam: Warna kulit, status sosial, dan garis keturunan tiada berharga di sisi Allah; ketakwaan adalah satu-satunya timbangan kemuliaan.
3. Istiqamah Menjaga Kesucian: Menjaga wudhu dan shalat sunnah adalah amalan sederhana yang mampu menghantarkan seorang hamba melangkah di surga.`,
    contentEn: `Unyielding Faith Beneath the Searing Stone

Bilal ibn Rabah, an Abyssinian enslaved man in Makkah, was among the earliest to accept monotheism. When his owner Umayyah ibn Khalaf discovered his faith, he subjected Bilal to monstrous torment, dragging him through desert sands and placing a massive burning boulder on his chest. Through agonizing pain, Bilal uttered his immortal defiance: "Ahad! Ahad! (One! One God!)"

Emancipation and the Voice of Prayer

Abu Bakr purchased and freed Bilal, earning eternal merit. When prayer was instituted in Madinah, the Prophet appointed Bilal as the first Mu'adhin of Islam due to his resonant, deeply moving voice that stirred the hearts of believers.

Proclamation Atop the Kaaba

During the conquest of Makkah, the Prophet commanded Bilal to ascend the rooftop of the Kaaba and chant the call to prayer. This historic moment shattered jahiliyyah racism, affirming that honor belongs purely to piety, not lineage or skin color.

Footsteps in Paradise

The Prophet once asked Bilal: "What deed have you done that caused me to hear the sound of your footsteps before me in Paradise?" Bilal replied that whenever he performed wudhu, he prayed two raka'at of gratitude. After the Prophet's death, Bilal could no longer bear to call the Adhan in Madinah due to grief, migrating to Syria where he passed away.

Key Lessons and Takeaways

1. Invincible Spiritual Conviction: Sincere faith withstands any level of external persecution.
2. Radical Equality in Islam: Islam obliterated racial hierarchy, measuring human worth solely by moral piety.
3. The Beauty of Quiet Devotion: Consistent small rituals like praying after ablution yield sublime eternal rewards.`,
    contentMs: `Ketabahan di Bawah Terik Matahari

Bilal bin Rabah, hamba berbangsa Habsyi, diseksa dengan kejam diletakkan batu besar di atas dadanya di padang pasir Makkah. Beliau tetap teguh dengan ucapan tauhid 'Ahad! Ahad!'.

Kemerdekaan dan Pelantikan Muazin

Abu Bakar menebus dan memerdekakan Bilal. Beliau dipilih oleh Rasulullah SAW menjadi muazin pertama melaungkan seruan solat di Madinah.

Laungan Azan di Atas Kaabah

Semasa pembukaan Makkah, Bilal diperintahkan melaungkan azan dari atas bumbung Kaabah, menghapuskan jurang perkauman dan membuktikan kemuliaan insan berasaskan takwa.

Derapan Tapak Kaki di Syurga

Rasulullah SAW mendengar derap tapak kaki Bilal di syurga berkat amalan istiqamah solat sunat wuduk. Bilal wafat di Damsyik selepas menyertai dakwah dan jihad di Syam.

Hikmah dan Iktibar Utama

1. Keteguhan Iman: Akidah yang kukuh tidak dapat digugat oleh sebarang ancaman dunia.
2. Kesaksamaan Tanpa Diskriminasi: Islam memuliakan insan atas dasar ketakwaan dan amal soleh.
3. Keberkatan Istiqamah: Amalan kecil yang dilakukan berterusan membuka pintu kemuliaan akhirat.`,
    quranReferences: [
      createRef(16, "106"),
      createRef(49, "13")
    ]
  },

  // 8. Salman Al-Farisi
  {
    id: "sahabat-salman-al-farisi",
    categoryId: "companions",
    title: "Salman Al-Farisi R.A.",
    titleEn: "Salman Al-Farsi (R.A.)",
    titleMs: "Salman Al-Farisi R.A.",
    subtitle: "Pencari Kebenaran Abadi & Ahli Strategi Perang Khandaq",
    subtitleEn: "The Seeker of Truth & Mastermind of the Trench",
    subtitleMs: "Pencari Kebenaran Sejati & Penggagas Perang Khandaq",
    icon: "ic_faraidh_people",
    readTimeMinutes: 11,
    summary: "Kisah Salman Al-Farisi R.A., bangsawan Persia yang rela menjadi budak demi mencari nabi akhir zaman, penggagas taktik parit dalam Perang Khandaq, dan sahabat yang diakui sebagai bagian dari Ahlul Bait.",
    summaryEn: "The life of Salman Al-Farsi (R.A.), Persian noble who endured enslavement in his quest for the final Prophet, visionary strategist of the Trench, honored by the Prophet as part of his household.",
    summaryMs: "Kisah Salman Al-Farisi R.A., bangsawan Parsi yang mengembara mencari nabi akhir zaman, perancang strategi parit Khandaq, serta diakui sebagai sebahagian daripada keluarga Nabi.",
    content: `Pengembaraan Panjang Mencari Cahaya Kebenaran

Salman Al-Farisi terlahir sebagai putra seorang kepala desa terpandang penganut Majusi di Isfahan, Persia. Jiwanya haus akan kebenaran hakiki hingga ia meninggalkan kemewahan istana ayahnya untuk mempelajari ajaran Nasrani. Ia berguru dari satu pendeta zuhud ke pendeta lainnya dari Damaskus, Mosul, Nisibin, hingga Amuria.

Sebelum wafat, guru terakhirnya berpesan: "Wahai anakku, tiada lagi orang yang berpegang pada ajaran murni ini. Namun telah dekat masa diutusnya seorang Nabi di tanah Arab yang membawa ajaran Ibrahim. Tanda-tandanya: ia tidak memakan sedekah, bersedia menerima hadiah, dan di antara kedua pundaknya terdapat tanda kenabian." Salman pun membayar kafilah pedagang untuk membawanya ke Arab, namun ia dikhianati dan dijual sebagai budak hingga tiba di kota Yatsrib (Madinah).

Perjumpaan dengan Tanda-Tanda Kenabian

Ketika Rasulullah SAW berhijrah ke Madinah, Salman segera mendatangi beliau untuk membuktikan tanda-tanda kenabian. Pertama, ia membawa kurma seraya berkata: "Ini sedekah untukmu dan sahabatmu." Rasulullah membagikannya kepada sahabat namun tidak memakannya sedikit pun. 

Kedua, ia membawa kurma lagi dan berkata: "Ini hadiah untukmu." Rasulullah pun memakannya bersama para sahabat. Ketiga, Salman mengamati punggung Nabi dan baginda dengan bijak membuka sedikit selendangnya, menampakkan stempel kenabian (Khatamun Nubuwwah). Seketika itu pula Salman menangis, mencium kaki Nabi, dan bersyahadat memeluk Islam.

Strategi Jenius Parit (Khandaq) Penyelamat Madinah

Ketika 10.000 pasukan gabungan kafir Quraisy dan kabilah-kabilah Arab mengepung Madinah dalam Perang Ahzab (Khandaq), kaum muslimin berada dalam bahaya kemusnahan total. Salman maju mengajukan strategi militer yang belum pernah dikenal bangsa Arab: "Wahai Rasulullah, di negeri kami Persia, bila kami dikepung musuh dalam jumlah besar, kami menggali parit di sekeliling kami."

Rasulullah SAW menerima usulan brilian tersebut. Parit digali bersama-sama secara heroik hingga mampu menahan gempuran musuh selama berminggu-minggu sampai badai dingin memporak-porandakan kemah sekutu Quraisy. Salman begitu dicintai hingga Muhajirin dan Anshar berebut mengakuinya, namun Nabi SAW bersabda: "Salman adalah bagian dari kami, Ahlul Bait (keluarga Nabi)."

Kezuhudan Gubernur Al-Mada'in

Ketika diangkat menjadi gubernur wilayah Al-Mada'in (Persia), Salman menolak kemewahan istana. Seluruh gajinya disedekahkan kepada fakir miskin, sementara ia menyambung hidup dengan menganyam daun kurma. Beliau wafat dengan hanya meninggalkan bejana air dan tikar sederhana.

Hikmah dan Pelajaran Berharga

1. Kegigihan Mencari Kebenaran: Pencarian tulus terhadap hidayah Allah akan menembus segala rintangan jarak, waktu, dan penderitaan duniawi.
2. Keterbukaan Intelektual dalam Dakwah: Islam menyambut baik setiap ide, keahlian, dan teknologi positif dari bangsa mana pun demi kemaslahatan bersama.
3. Hakikat Persaudaraan Iman: Persaudaraan Islam menghapus batasan suku bangsa dan menjadikan hamba beriman laksana satu keluarga mulia.`,
    contentEn: `The Epic Journey of the Truth Seeker

Salman was born to a wealthy Zoroastrian family in Persia. Unsatisfied with idol fire-worship, he left his wealth, studying under Christian monks across Damascus and Syria. His dying mentor informed him of the imminent arrival of the final Prophet in Arabia, identifiable by three signs: accepting gifts, refusing charity, and bearing the seal of prophethood on his back. En route, Salman was betrayed and sold into slavery in Madinah.

Verifying the Signs of Prophethood

Upon the Prophet's arrival in Madinah, Salman tested the criteria one by one with dates as charity, then gifts, and finally witnessed the prophetic seal on Muhammad's back. Overwhelmed with joy, he wept, embraced Islam, and was subsequently bought out of slavery by the Prophet and companions.

The Trench Strategy That Saved Madinah

Facing an overwhelming coalition of 10,000 enemy troops in the Battle of the Ahzab, Salman proposed an ingenious Persian defensive tactic: digging a massive trench along Madinah's exposed borders. The trench paralyzed the confederate army until divine winds scattered them. The Prophet honored him profoundly, stating: "Salman is from us, the Ahl al-Bayt."

Ascetic Governor of Al-Mada'in

As governor of Persian provinces, Salman gave away his entire salary to charity, earning his sustenance by weaving palm fronds. He passed away possessing only an eating bowl and a prayer rug.

Key Lessons and Takeaways

1. Relentless Pursuit of Wisdom: The sincere soul spares no hardship in pursuing authentic divine truth.
2. Global Innovation in Islamic Strategy: Wisdom is the lost property of the believer; wherever found, it should be embraced.
3. Spiritual Kinship Transcends Blood: Faith creates bonds deeper than biological lineage.`,
    contentMs: `Pengembaraan Panjang Mencari Hidayah

Salman Al-Farisi meninggalkan kekayaan keluarganya di Parsi bagi mencari agama yang benar. Beliau mengembara berguru dengan paderi-paderi zuhud sehinggalah dimaklumkan tentang tanda-tanda kenabian Nabi akhir zaman di Tanah Arab.

Membuktikan Tanda Kenabian

Di Madinah, Salman menguji tanda kenabian: tidak memakan sedekah, memakan hadiah dan mempunyai cop kenabian di belakang badan. Selepas melihat kesemua tanda itu, beliau memeluk Islam dengan penuh linangan air mata.

Taktik Parit Perang Khandaq

Tatkala Madinah dikepung 10,000 tentera gabungan musuh, Salman mencadangkan penggalian parit besar yang menyelamatkan kota Madinah. Rasulullah SAW mengiktiraf Salman sebagai sebahagian daripada Ahlul Bait.

Gabenor yang Amat Zuhud

Sebagai gabenor Parsi, Salman menyedekahkan semua elaunnya dan menyara hidup dengan menganyam daun kurma. Beliau wafat dengan meninggalkan harta yang amat sedikit.

Hikmah dan Iktibar Utama

1. Ketekunan Mencari Kebenaran: Perjalanan mencari hidayah menuntut keikhlasan dan pengorbanan yang tidak mengenal erti putus asa.
2. Fleksibiliti Strategi: Memanfaatkan kemahiran dan idea bernas demi kebaikan bersama.
3. Ketinggian Taraf Persaudaraan Islam: Ikatan akidah mengatasi batas keturunan dan bangsa.`,
    quranReferences: [
      createRef(62, "3"),
      createRef(33, "9-11")
    ]
  },

  // 9. Khalid bin Al-Walid
  {
    id: "sahabat-khalid-bin-walid",
    categoryId: "companions",
    title: "Khalid bin Al-Walid R.A.",
    titleEn: "Khalid ibn Al-Walid (R.A.)",
    titleMs: "Khalid bin Al-Walid R.A.",
    subtitle: "Saifullah Al-Maslul - Pedang Allah yang Terhunus",
    subtitleEn: "The Drawn Sword of Allah & Undefeated Military Genius",
    subtitleMs: "Pedang Allah yang Terhunus & Panglima Tentera Terhebat",
    icon: "ic_faraidh_people",
    readTimeMinutes: 12,
    summary: "Kisah Khalid bin Al-Walid R.A., panglima perang terhebat yang tak pernah terkalahkan dalam ratusan pertempuran, pahlawan penyelamat dalam Perang Mu'tah, penakluk imperium Romawi dan Persia, serta teladan keikhlasan prajurit sejati.",
    summaryEn: "The life of Khalid ibn Al-Walid (R.A.), Saifullah (The Sword of Allah), undefeated tactical genius of over a hundred battles, savior at Mu'tah, conqueror of empires, and model of selfless submission to divine decree.",
    summaryMs: "Kisah Khalid bin Al-Walid R.A., Saifullah yang tidak pernah tewas dalam lebih 100 peperangan, penyelamat tentera Islam di Mu'tah, penakluk Parsi dan Rom, serta teladan keikhlasan pejuang sejati.",
    content: `Kejeniusan Militer dan Fajar Keislaman

Khalid bin Al-Walid berasal dari kabilah Bani Makhzum, pemegang komando kavaleri Quraisy yang disegani. Kecerdasan taktik militernya sempat membalikkan keadaan dalam Perang Uhud. Namun, cahaya hidayah menyinari hatinya pasca Perjanjian Hudaibiyah. Bersama Amr bin Al-Ash dan Utsman bin Thalhah, Khalid berangkat ke Madinah mengikrarkan keislamannya. 

Rasulullah SAW menyambutnya dengan senyum bahagia seraya bersabda: "Aku tahu engkau memiliki akal yang cerdas, dan aku berharap akal itu hanya akan menuntunmu kepada kebaikan."

Gelar Saifullah Al-Maslul di Perang Mu'tah

Ujian pertama kepemimpinan Khalid terjadi dalam Perang Mu'tah di perbatasan Syam. Ketika 3.000 pasukan muslimin berhadapan dengan 200.000 bala tentara gabungan Romawi Byzantium dan tiga panglima Islam (Zaid bin Haritsah, Ja'far bin Abi Thalib, dan Abdullah bin Rawahah) gugur syahid berturut-turut, pasukan menunjuk Khalid memegang panji komando.

Dengan kepiawaian manuver psikologis dan taktik brilian, Khalid merombak formasi pasukan, menukar barisan depan ke belakang dan sayap kanan ke kiri, serta menciptakan kepulan debu di kejauhan hingga musuh mengira bantuan besar telah tiba. Pasukan muslimin berhasil ditarik mundur dengan selamat tanpa kerugian berarti. Atas aksi heroik ini, Rasulullah SAW menggelarinya 'Saifullah Al-Maslul' (Pedang Allah yang Terhunus).

Penakluk Dua Imperium dan Keikhlasan Sejati

Di era Khalifah Abu Bakar dan Umar, Khalid memimpin pembebasan wilayah Irak (Persia) dan Syam (Romawi). Puncaknya pada Perang Yarmuk yang legendaris, pasukannya meluluhlantakkan kekuatan raksasa Romawi.

Di tengah kecamuk perang Yarmuk, sepucuk surat dari Khalifah Umar tiba: Khalid dicopot dari posisi panglima tertinggi dan digantikan oleh Abu Ubaidah demi menjaga akidah umat agar tidak bergantung pada figur Khalid. Tanpa rasa kecewa atau protes sedikit pun, Khalid menyerahkan tongkat komando dan berkata: "Aku berperang bukan karena Umar, tetapi aku berperang semata-mata karena Tuhannya Umar." Khalid terus bertarung di garis terdepan sebagai prajurit biasa dengan ketangguhan yang sama.

Kerinduan Syahid di Atas Ranjang

Sepanjang hidupnya memimpin lebih dari 100 pertempuran tanpa pernah kalah sekalipun, sekujur tubuh Khalid dipenuhi bekas luka tebasan pedang, tusukan tombak, dan anak panah. Namun takdir Allah menentukan sang 'Pedang Allah' tidak boleh patah di tangan orang kafir. 

Menjelang wafatnya di atas ranjang di kota Homs (Syam), Khalid menangis tersedu-sedu: "Aku telah mencari syahid di ratusan medan laga, tiada sejengkal pun di tubuhku melainkan terdapat bekas luka. Namun kini aku mati di atas ranjangku seperti matinya seekor unta. Semoga mata para pengecut tidak pernah bisa tidur pulas!"

Hikmah dan Pelajaran Berharga

1. Keikhlasan Prajurit Sejati: Nilai perjuangan seorang mukmin tidak ditentukan oleh pangkat jabatan, melainkan ketulusan niat beramal karena Allah.
2. Bakat Demi Kejayaan Islam: Mengarahkan potensi kepemimpinan dan keterampilan strategis untuk membela keadilan dan meninggikan syiar agama.
3. Kematian di Tangan Takdir Ilahi: Usia dan ajal sepenuhnya berada dalam kuasa Allah SWT; keberanian di medan juang tidak akan memajukan ajal, dan kehati-hatian tidak akan menundanya.`,
    contentEn: `Military Mastery and Entry into Faith

Belonging to the elite cavalry clan of Banu Makhzum, Khalid's strategic acumen was formidable. Following the Treaty of Hudaybiyyah, divine guidance penetrated his heart. Embracing Islam alongside Amr ibn al-Aas, the Prophet welcomed him warmly: "I knew you possessed an intellect that would inevitably lead you to good."

The Sword of Allah at Mu'tah

At the Battle of Mu'tah, outnumbered 3,000 Muslims against 200,000 Byzantine forces, three successive Muslim commanders fell martyred. Khalid took the fallen banner, brilliantly reorganizing the ranks and creating deceptive dust clouds that led Byzantines to assume reinforcements arrived, executing a flawless retreat. The Prophet crowned him 'The Drawn Sword of Allah'.

Conquest of Empires and Supreme Humility

Khalid led historic campaigns liberating Iraq and Syria, culminating in the decisive Battle of Yarmouk. Amid the campaign, Caliph Umar issued orders relieving Khalid from supreme command in favor of Abu Ubaidah to protect the Ummah's reliance solely on Allah. Without hesitation, Khalid continued fighting valiantly as a regular soldier: "I do not fight for Umar; I fight for the Lord of Umar."

Tears of a Warrior's Passing

Having fought over a hundred battles without defeat, his entire body scarred by blades and arrows, Khalid wept on his deathbed in Homs: "I sought martyrdom in every battle, yet here I die upon my bed like a camel dies. May the eyes of cowards never sleep!"

Key Lessons and Takeaways

1. Pure Sincerity in Service: True dignity lies in doing duty for God, completely detached from rank or recognition.
2. Channeling Talents for Virtue: Directing strategic brilliance toward righteousness creates world-altering impact.
3. Destiny Governs Mortality: Lifespan is fixed by divine decree; courage does not shorten life, nor does fear prolong it.`,
    contentMs: `Kebijaksanaan Taktikal Memeluk Islam

Khalid bin Al-Walid, pahlawan kavaleri Quraisy yang tiada tandingan, memeluk Islam selepas perjanjian Hudaibiyah. Rasulullah SAW menyambut keislamannya dengan penuh gembira.

Saifullah Al-Maslul di Mu'tah

Dalam Perang Mu'tah, Khalid mengambil alih kepimpinan selepas tiga panglima syahid. Taktik psikologi bijaknya menyelamatkan tentera Islam daripada kepungan musuh Romawi, melayakkannya mendapat gelaran Pedang Allah yang Terhunus.

Keikhlasan di Perang Yarmuk

Khalid menumpaskan empayar Rom di Yarmuk. Ketika diarahkan oleh Khalifah Umar melepaskan jawatan panglima kepada Abu Ubaidah, Khalid akur tanpa rungutan dan terus berjuang sebagai tentera biasa semata-mata kerana Allah.

Wafat di Atas Tilam

Khalid menangis semasa nazak kerana menginginkan syahid di medan perang, namun seluruh tubuhnya yang penuh parut luka membuktikan keteguhan jihadnya.

Hikmah dan Iktibar Utama

1. Keikhlasan Tanpa Pangkat: Ikhlas berjuang demi Allah tanpa terikat kepada gelaran atau kedudukan duniawi.
2. Mengoptimumkan Bakat untuk Agama: Menggunakan segala kemahiran memartabatkan keadilan dan kebenaran.
3. Ajal Ketetapan Allah: Berani di medan perjuangan tidak mempercepatkan kematian kerana ajal ditentukan oleh Allah SWT semata-mata.`,
    quranReferences: [
      createRef(48, "29"),
      createRef(9, "111")
    ]
  },

  // 10. Hamzah bin Abdul Muthalib
  {
    id: "sahabat-hamzah-bin-abdul-muthalib",
    categoryId: "companions",
    title: "Hamzah bin Abdul Muthalib R.A.",
    titleEn: "Hamzah ibn Abdul-Muttalib (R.A.)",
    titleMs: "Hamzah bin Abdul Muthalib R.A.",
    subtitle: "Asadullah - Singa Allah & Pemimpin Para Syuhada",
    subtitleEn: "The Lion of Allah & Master of the Martyrs",
    subtitleMs: "Singa Allah & Penghulu Segala Syuhada",
    icon: "ic_faraidh_people",
    readTimeMinutes: 10,
    summary: "Kisah Hamzah bin Abdul Muthalib R.A., paman sekaligus saudara sesusuan Rasulullah, kesatria pemburu singa yang membela dakwah di Makkah, pahlawan perang Badar, dan syahid agung bergelar Sayyidusy Syuhada di Bukit Uhud.",
    summaryEn: "The life of Hamzah ibn Abdul-Muttalib (R.A.), beloved uncle of the Prophet, fearless hunter known as the Lion of Allah and Master of Martyrs, who gave his life defending the faith at Mount Uhud.",
    summaryMs: "Kisah Hamzah bin Abdul Muthalib R.A., bapa saudara Rasulullah, pemburu singa yang berani membela dakwah Islam, pahlawan Badar dan gugur sebagai Sayyidusy Syuhada di Bukit Uhud.",
    content: `Keberanian Singa Pelindung Dakwah

Hamzah bin Abdul Muthalib adalah paman Nabi Muhammad SAW yang usianya sebaya, sekaligus saudara sesusuan baginda. Sejak muda ia dikenal sebagai pemuda Quraisy yang paling gagah, gemar memanah, dan terbiasa berburu singa di padang pasir.

Keislamannya berawal dari rasa pembelaan terhadap keponakannya. Ketika mendengar Abu Jahal mencaci dan menyakiti Rasulullah SAW di dekat Bukit Shafa, Hamzah mendatangi Abu Jahal di pelataran Ka'bah dan memukulkan busur panahnya ke kepala Abu Jahal seraya berteriak lantang: "Berani engkau mencaci Muhammad, padahal aku telah memeluk agamanya dan meyakini apa yang ia katakan?! Balas pukulanku jika engkau berani!" Keislaman Hamzah menjadi benteng pelindung kokoh bagi dakwah Islam di Makkah.

Singa Allah di Medan Badar

Dalam Perang Badar, Hamzah tampil dengan bulu burung unta di dadanya sebagai penanda kesatria perang. Beliau memenangkan duel pembuka dan menebas banyak pembesar musyrikin Quraisy, termasuk Utbah bin Rabi'ah. Rasulullah SAW memberinya gelar 'Asadullah wa Asadu Rasulihi' (Singa Allah dan Singa Rasul-Nya) atas ketangkasannya yang tak tertandingi di garda terdepan.

Gugur Syahid di Bukit Uhud

Pada Perang Uhud, kaum musyrikin Makkah menaruh dendam kesumat kepada Hamzah. Jubair bin Muth'im menjanjikan kemerdekaan bagi budaknya asal Habsyi, Wahsyi bin Harb, jika berhasil membunuh Hamzah. Di tengah gemuruh pertempuran tatkala Hamzah sedang bertarung dengan dua bilah pedang, Wahsyi yang bersembunyi di balik batu melesatkan tombak beracunnya dari kejauhan tepat mengenai bagian bawah perut Hamzah hingga menembus tubuhnya.

Setelah pertempuran usai, Hindun binti Utbah melampiaskan amarah dengan merusak jasad suci Hamzah. Rasulullah SAW menangis tersedu-sedu melihat kondisi pamannya dan bersabda: "Semoga Allah merahmatimu wahai paman, engkau adalah orang yang paling menyambung silaturahim dan paling gemar berbuat kebajikan. Engkau adalah Sayyidusy Syuhada (Pemimpin Para Syuhada) di hari kiamat."

Hikmah dan Pelajaran Berharga

1. Keberanian Membela Kebenaran: Ksatria sejati menggunakan kekuatan fisik dan wibawanya untuk melindungi kaum lemah dan membela dakwah agama.
2. Keutamaan Syahid di Jalan Allah: Gelar Sayyidusy Syuhada menegaskan bahwa pengorbanan tertinggi demi kalimat tauhid dibalas dengan kedudukan paling terhormat di sisi Allah SWT.
3. Ketulusan Kasih Sayang Keluarga: Membela kerabat di atas dasar kebenaran akidah adalah bentuk pembuktian cinta yang hakiki.`,
    contentEn: `The Fearless Protector of the Prophet

Hamzah, uncle and foster brother of the Prophet, was a celebrated warrior and lion-hunter. Returning from hunting one day and hearing that Abu Jahl had insulted the Prophet, Hamzah marched to the Kaaba and struck Abu Jahl across the face with his bow, proclaiming his Islam openly. His conversion provided vital protection to early Muslims.

Lion of Allah at Badr

At the Battle of Badr, bearing an ostrich feather on his chest, Hamzah slew prominent polytheist leaders in single combat, earning the title 'The Lion of Allah and His Messenger'.

The Tragedy of Mount Uhud

Seeking vengeance for Badr, the Quraysh hired the spearman Wahshi to target Hamzah at Mount Uhud. While fighting courageously with two swords, a spear struck him down. The Prophet wept bitter tears at his body, crowning him 'Sayyid ash-Shuhada' (Master of the Martyrs).

Key Lessons and Takeaways

1. Righteous Chivalry: Physical strength reaches nobility when dedicated to safeguarding truth and protecting the vulnerable.
2. The Pinnacle of Martyrdom: Supreme sacrifice in the defense of justice guarantees eternal preeminence in the sight of God.
3. Family Bonds Sanctified by Faith: Standing by kin upon principles of divine guidance forges immortal bonds.`,
    contentMs: `Singa Pelindung Dakwah Nabi

Hamzah bin Abdul Muthalib merupakan pahlawan pemburu singa yang amat disegani di Makkah. Beliau memeluk Islam selepas membela Rasulullah SAW daripada penghinaan Abu Jahal.

Singa Allah di Medan Badar

Di medan Badar, Hamzah bertempur dengan gagah berani di barisan hadapan menumbangkan ketua-ketua musyrikin Quraisy dan digelar Singa Allah dan Singa Rasul-Nya.

Sayyidusy Syuhada di Bukit Uhud

Dalam Perang Uhud, Hamzah gugur syahid akibat rejaman lembing Wahsyi. Rasulullah SAW amat berduka atas pemergiannya dan mengisytiharkan Hamzah sebagai Penghulu Para Syuhada di akhirat kelak.

Hikmah dan Iktibar Utama

1. Keberanian Menegakkan Keadilan: Menggunakan kekuatan jasmani membela kebenaran dan menentang kezaliman.
2. Kemuliaan Pengorbanan Syahid: Martabat tertinggi dianugerahkan kepada pejuang yang gugur demi meninggikan kalimah Allah.
3. Pengorbanan Kasih Sayang Sejati: Membela keluarga atas dasar kebenaran akidah.`,
    quranReferences: [
      createRef(6, "122"),
      createRef(33, "23")
    ]
  }
];

// Write companions to topics.json
const topicsPath = 'app/src/main/assets/encyclopedia/topics.json';
const allTopics = JSON.parse(fs.readFileSync(topicsPath, 'utf8'));

// Filter out old companions
const prophets = allTopics.filter(t => t.categoryId === 'prophets');
const fiqh = allTopics.filter(t => t.categoryId === 'fiqh');
const other = allTopics.filter(t => t.categoryId !== 'prophets' && t.categoryId !== 'companions' && t.categoryId !== 'fiqh');

const finalTopics = [...prophets, ...companions, ...fiqh, ...other];

fs.writeFileSync(topicsPath, JSON.stringify(finalTopics, null, 2), 'utf8');
console.log('Successfully written', finalTopics.length, 'topics (Prophets:', prophets.length, 'Companions:', companions.length, 'Fiqh:', fiqh.length, ')');

if (fs.existsSync(dbTempPath)) {
  fs.unlinkSync(dbTempPath);
}

const fs = require('fs');
const zlib = require('zlib');
const { execSync } = require('child_process');

const dbGzPath = 'app/src/main/assets/quran/qurannew.db.gz';
const dbTempPath = '/tmp/quran_companion_part2.db';

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

console.log('Generating companions 11-20...');

const companionsPart2 = [
  // 11. Mush'ab bin Umair
  {
    id: "sahabat-mushab-bin-umair",
    categoryId: "companions",
    title: "Mush'ab bin Umair R.A.",
    titleAr: "مُصْعَبُ بْنُ عُمَيْرٍ",
    shortDescription: "Pemuda bangsawan Quraisy yang meninggalkan kemewahan demi Islam, duta dakwah pertama ke Madinah, dan syahid agung penjaga panji di Perang Uhud.",
    shortDescriptionEn: "The noble youth of Quraysh who renounced immense wealth for Islam, first ambassador to Madinah, and gallant standard-bearer martyr at Uhud.",
    shortDescriptionMs: "Pemuda bangsawan Quraisy yang meninggalkan kemewahan demi Islam, duta dakwah pertama ke Madinah, dan syahid agung di medan Uhud.",
    content: `Bangsawan Rupawan yang Memilih Jalan Iman

Mush'ab bin Umair adalah pemuda paling tampan, elegan, dan dimanjakan dengan limpahan harta di Makkah. Pakaian sutranya diimpor khusus dari Yaman dan semerbak wewangiannya menjadi tanda kehadirannya di lorong kota. Namun ketika seruan tauhid menyentuh qalbunya di Darul Arqam, Mush'ab meninggalkan seluruh kenikmatan fana. Sang ibu mengurungnya dan memutus seluruh warisan, namun keimanannya tak pernah goyah walau pakaiannya berganti kain kasar bertambal.

Duta Dakwah Pertama ke Madinah

Melihat kebijaksanaan, kefasihan lisan, dan kelembutan akhlak Mush'ab, Rasulullah SAW memilihnya sebagai duta dakwah pertama ke Yatsrib (Madinah). Dengan kesabaran luar biasa dan pendekatan Al-Qur'an yang menyentuh, Mush'ab berhasil mengislamkan tokoh-tokoh besar suku Aus dan Khazraj, termasuk Sa'ad bin Mu'adz dan Usaid bin Hudhair. Melalui tangannya, hampir seluruh rumah di Madinah telah bersinar dengan cahaya Islam sebelum peristiwa Hijrah.

Pahlawan Panji di Bukit Uhud

Pada Pertempuran Uhud, Mush'ab dipercaya memegang panji utama kaum muslimin. Ketika barisan terdesak oleh serbuan musuh, Mush'ab berdiri tegak menarik perhatian musuh agar tidak mendekati Rasulullah SAW. Tangan kanannya ditebas musuh hingga putus, ia menyambut panji dengan tangan kirinya. Saat tangan kirinya pun ditebas, ia mendekap panji ke dadanya dengan kedua pangkal lengannya sambil terus melantunkan firman Allah bahwa Muhammad hanyalah seorang rasul. Ketika ia akhirnya gugur terkena tombak, kain kafannya bahkan tidak cukup menutupi seluruh tubuhnya; jika kepalanya ditutup kakinya tersingkap, hingga Rasulullah SAW memerintahkan agar kakinya ditutup dengan rumput idzhir seraya menitikkan air mata haru.

Hikmah dan Pelajaran Berharga

1. Pengorbanan Total Demi Akidah: Hakikat kemuliaan manusia terletak pada keteguhan iman di jalan Allah, bukan pada kilauan harta dan status sosial.
2. Keberhasilan Dakwah dengan Hikmah: Ketulusan, kelembutan tutur kata, dan adab yang luhur adalah kunci utama terbukanya pintu hati manusia menerima kebenaran.
3. Kesetiaan Tanpa Syarat: Membela risalah Islam hingga tetes darah penghabisan merupakan bukti cinta sejati kepada Allah dan Rasul-Nya.`,
    contentEn: `The Elegance of Faith over Worldly Splendor

Mush'ab bin Umair was widely known as the most handsome and affluent young noble in Makkah. Draped in luxurious garments and renowned for costly perfumes, he abandoned every worldly privilege upon embracing Islam at Dar al-Arqam. Deprived of wealth and imprisoned by his mother, his spirit remained unbreakable, embracing a life of modest austerity for the divine truth.

First Ambassador to Madinah

Recognizing his wisdom, eloquent speech, and gentle demeanor, Prophet Muhammad (pbuh) dispatched Mush'ab as the first ambassador of Islam to Yathrib (Madinah). Through heartfelt recitation of the Qur'an and gentle discourse, he won the hearts of major chieftains such as Sa'd ibn Mu'adh, leading nearly every household in Madinah to embrace Islam prior to the great Hijrah.

Martyrdom of the Valiant Standard-Bearer

At the Battle of Uhud, Mush'ab proudly carried the standard of the Muslims. When chaos erupted, he held the banner aloft to shield the Prophet from enemy strikes. Though both his hands were severed in combat, he clamped the standard to his chest with his arms while reciting verses of divine steadfastness until struck down by a javelin. When he passed away, his solitary garment was so short that covering his head exposed his feet, moving the Prophet to tears as he honored his ultimate sacrifice.

Key Lessons and Takeaways

1. True Nobility in Conviction: Authentic greatness is born from steadfast devotion to truth, outshining all worldly prestige and wealth.
2. The Art of Compassionate Da'wah: Wisdom, gracious speech, and genuine kindness are the keys that unlock hearts to receive divine guidance.
3. Unflinching Loyalty: Defending righteous values until one's final breath embodies true fidelity to the Creator.`,
    contentMs: `Bangsawan Rupawan yang Memilih Iman

Mush'ab bin Umair merupakan pemuda terkaya dan paling tampan di Makkah. Beliau sanggup melepaskan segala kemewahan dan warisan keluarga demi memeluk Islam bersama Rasulullah SAW.

Duta Dakwah Pertama ke Madinah

Rasulullah SAW mengutus Mush'ab ke Madinah untuk mengajarkan Al-Qur'an. Berkat kelembutan akhlak dan kebijaksanaannya, dakwah beliau diterima meluas oleh pemimpin Aus dan Khazraj.

Gugur Syahid Mempertahankan Panji Uhud

Dalam Perang Uhud, Mush'ab memegang panji Islam dengan penuh keberanian sehingga kedua-dua belah tangannya putus ditebas musuh. Beliau syahid mendakap panji dan kain kafannya yang terhad mengundang titisan air mata Rasulullah SAW.

Hikmah dan Iktibar Utama

1. Pengorbanan Harta Demi Akidah: Meletakkan keredaan Allah melebihi keselesaan hidup duniawi.
2. Kebijaksanaan Menyampaikan Dakwah: Akhlak mulia dan kesabaran menjadi daya penarik manusia kepada Islam.
3. Kesetiaan Sejati: Berkorban nyawa mempertahankan panji kebenaran.`,
    quranReferences: [
      createRef(33, "23"),
      createRef(3, "144")
    ]
  },

  // 12. Abdurrahman bin Auf
  {
    id: "sahabat-abdurrahman-bin-auf",
    categoryId: "companions",
    title: "Abdurrahman bin Auf R.A.",
    titleAr: "عَبْدُ الرَّحْمَنِ بْنُ عَوْفٍ",
    shortDescription: "Saudagar ulung bertangan emas yang dijamin surga, teladan kedermawanan luar biasa yang mewakafkan kekayaan melimpah di jalan Allah.",
    shortDescriptionEn: "The blessed master merchant guaranteed Paradise, an unparalleled exemplar of wealth sanctified through selfless charity in the path of God.",
    shortDescriptionMs: "Saudagar mukmin yang dijamin syurga, terkenal dengan keusahawanan berkat dan kedermawanan luar biasa menginfakkan harta di jalan Allah.",
    content: `Etika Perniagaan dan Tangan Emas dari Nol

Abdurrahman bin Auf adalah salah satu dari delapan orang pertama yang memeluk Islam (As-Sabiqunal Awwalun). Ketika berhijrah ke Madinah tanpa membawa sepeser pun harta, saudaranya dari kaum Anshar, Sa'ad bin Ar-Rabi', menawarkan separuh hartanya dan salah satu istrinya. Dengan penuh rasa hormat dan kehormatan diri yang tinggi, Abdurrahman menolak dengan halus seraya berkata: "Semoga Allah memberkahi keluarga dan hartamu. Tunjukkan saja kepadaku di mana letak pasar." Berbekal kejujuran, keuletan, dan kepiawaian berdagang keju dan minyak samin, dalam waktu singkat ia menjadi salah satu saudagar terkaya di jazirah Arab.

Kafilah Raksasa yang Diguncang Kedermawanan

Kekayaan tidak pernah mengeraskan hatinya; ia selalu menganggap harta hanyalah titipan untuk melayani umat. Suatu hari, kota Madinah bergemuruh oleh derap langkah 700 unta pembawa gandum, tepung, dan pakaian yang baru tiba dari Syam. Ketika mendengar kabar dari Sayyidah Aisyah bahwa Rasulullah SAW pernah bersabda tentang dirinya yang dikhawatirkan masuk surga dengan merangkak karena hisab hartanya, Abdurrahman seketika mewakafkan seluruh 700 unta beserta muatannya untuk fakir miskin dan janda-janda di Madinah seraya bersumpah ingin memasuki surga dengan berlari tegak.

Infaq Perang Tabuk dan Keteladanan Akhir Hayat

Pada saat persiapan Perang Tabuk (Jaisyul 'Usrah), Abdurrahman menyumbangkan ratusan uqiyah emas murni untuk membiayai pasukan kaum muslimin. Menjelang wafatnya, ia mewasiatkan 400 dinar emas kepada setiap veteran Perang Badar yang masih hidup, menyantuni para istri Nabi, dan membebaskan puluhan budak. Ketulusannya mengiringi perjalanan hidupnya sebagai salah satu dari sepuluh sahabat yang dijamin masuk surga.

Hikmah dan Pelajaran Berharga

1. Kemandirian dan Etos Kerja Islami: Kehormatan diri seorang mukmin tercermin dari tekad untuk berusaha dan berniaga secara halal tanpa berpangku tangan.
2. Harta Sebagai Sarana Akhirat: Kekayaan materi yang diraih dengan kejujuran menjadi ladang amal jariyah jika dinafkahkan secara tulus demi kemaslahatan sesama.
3. Penjagaan Hati dari Ketamakan: Sikap tawadhu' dan kewaspadaan terhadap hisab akhirat menjaga seorang hartawan dari kesombongan dan cinta dunia.`,
    contentEn: `Dignity and Entrepreneurial Prowess

Abdurrahman ibn Awf was among the earliest converts to Islam. Arriving in Madinah penniless after the Hijrah, his Ansar brother Sa'd ibn al-Rabi' offered him half of all his possessions. Gracious yet fiercely self-reliant, Abdurrahman politely declined: "May God bless your wealth and family. Simply direct me to the marketplace." Through honesty, sharp acumen, and relentless hard work trading butter and cheese, he rapidly built an immense commercial enterprise.

The Tremor of 700 Camels in Charity

Wealth never corrupted his soul; he regarded prosperity purely as a trust from the Almighty. When a massive caravan of 700 camels laden with wheat and fine goods rattled Madinah, he learned that the Prophet had once remarked upon the rigorous reckoning of wealthy souls. Without hesitation, Abdurrahman donated the entire caravan and its cargo to the poor and widows of Madinah, pledging to sprint joyfully into Paradise.

The Generosity of Tabuk and Sacred Legacy

During the arduous Expedition of Tabuk, Abdurrahman contributed immense quantities of pure gold to finance the Muslim army. Prior to his passing, his will bequeathed 400 gold dinars to every surviving veteran of Badr, supported the Mothers of the Believers, and emancipated dozens of captives, securing his rank among the ten companions guaranteed Paradise.

Key Lessons and Takeaways

1. Sanctity of Self-Reliance: True dignity lies in honest trade and self-sufficiency rather than reliance upon handouts.
2. Wealth as a Spiritual Conduit: Material abundance becomes eternal light when channeled into uplifting humanity.
3. Vigilance against Materialism: Humility and mindfulness of divine accountability preserve the heart from pride and greed.`,
    contentMs: `Kemandirian Berniaga Tanpa Bergantung Budi

Abdurrahman bin Auf berhijrah ke Madinah sehelai sepinggang. Apabila ditawarkan separuh harta oleh saudaranya kaum Ansar, beliau menolak dengan sopan dan meminta ditunjukkan jalan ke pasar. Berkat kejujuran dan kemahiran berniaga, beliau menjadi saudagar yang berjaya.

Kedermawanan Kafilah 700 Unta

Mendengar hadis mengenai hisab harta, Abdurrahman menginfakkan seluruh kafilah 700 ekor unta sarat muatan makanan kepada fakir miskin Madinah demi mengharapkan keredaan Allah.

Penderma Utama Pasukan Tabuk

Beliau mendanai ekspedisi ketenteraan kaum muslimin dan mewasiatkan bantuan emas kepada para balu Nabi dan pejuang Badar, menjadi bukti perniagaan barakah yang membawa ke syurga.

Hikmah dan Iktibar Utama

1. Integriti dan Kerja Keras: Membina kekayaan dengan cara halal dan beretika.
2. Menginfakkan Harta di Jalan Allah: Menjadikan kekayaan jambatan menuju syurga.
3. Keringanan Hati Menghadapi Akhirat: Sentiasa bersiap sedia menghadapi hisab Allah SWT.`,
    quranReferences: [
      createRef(2, "274"),
      createRef(9, "100")
    ]
  },

  // 13. Sa'ad bin Abi Waqqash
  {
    id: "sahabat-saad-bin-abi-waqqash",
    categoryId: "companions",
    title: "Sa'ad bin Abi Waqqash R.A.",
    titleAr: "سَعْدُ بْنُ أَبِي وَقَّاصٍ",
    shortDescription: "Pemanah ulung pelindung Rasulullah, paman Nabi yang doanya mustajab, dan panglima besar penakluk imperium Persia di Perang Al-Qadisiyyah.",
    shortDescriptionEn: "The formidable archer of Islam, maternal uncle of the Prophet whose supplications were always answered, and hero of al-Qadisiyyah.",
    shortDescriptionMs: "Pemanah ulung pertama dalam Islam, bapa saudara Nabi yang mustajab doanya, serta panglima agung penakluk Parsi di Al-Qadisiyyah.",
    content: `Anak Panah Pertama dan Ujian Bakti kepada Ibu

Sa'ad bin Abi Waqqash memeluk Islam di usia muda pada awal dakwah Makkah. Beliau adalah orang pertama yang melesatkan anak panah dalam membela risalah Islam. Keislamannya mendapat tantangan luar biasa dari ibunya yang sangat ia cintai; sang ibu melakukan aksi mogok makan dan minum hingga hampir mati agar Sa'ad kembali ke agama nenek moyang. Dengan keteguhan akidah yang agung namun santun, Sa'ad berkata: "Wahai Ibu, demi Allah, seandainya engkau memiliki seratus nyawa lalu keluar satu demi satu, aku tidak akan pernah meninggalkan agamaku." Maka turunlah firman Allah dalam Surah Luqman yang memerintahkan berbakti kepada orang tua namun melarang taat dalam kemusyrikan.

Pemanah Kebanggaan Rasulullah di Perang Uhud

Dalam kecamuk Perang Uhud ketika musuh mengepung rapat posisi Rasulullah SAW, Sa'ad berdiri tegak sebagai perisai hidup. Rasulullah SAW menyodorkan anak panah satu demi satu seraya bersabda dengan kalimat yang tidak pernah beliau ucapkan kepada sahabat lain: "Memanahlah wahai Sa'ad, tebusanku untukmu adalah ayah dan ibuku!" Setiap panah yang dilepas Sa'ad melesat tepat menumbangkan musuh. Rasulullah juga mendoakannya secara khusus: "Ya Allah, tepatkanlah bidikannya dan kabulkanlah doanya." Sejak hari itu, doa Sa'ad tidak pernah tertolak.

Penakluk Imperium Persia di Al-Qadisiyyah

Pada masa Khalifah Umar bin Khattab, Sa'ad ditunjuk sebagai panglima tertinggi ekspedisi pembebasan Persia. Dalam Pertempuran Al-Qadisiyyah yang bersejarah, dengan strategi cemerlang dan kesabaran menghadapi pasukan gajah kaisar Yazdegerd III, Sa'ad memimpin kemenangan gemilang yang meruntuhkan kekuasaan imperium Sassania. Ia kemudian mendirikan kota Kufah sebagai benteng peradaban dan pusat keilmuan Islam.

Hikmah dan Pelajaran Berharga

1. Ketegasan Prinsip Tanpa Menghilangkan Adab: Menjaga akidah tauhid di atas segala ikatan emosional tanpa kehilangan tutur kata yang santun kepada orang tua.
2. Kekuatan Doa dan Kemurnian Hati: Doa yang mustajab lahir dari rezeki yang halal, ketaatan yang tulus, dan pengorbanan tanpa pamrih.
3. Kepemimpinan Berbasis Strategi dan Tawakal: Kemenangan besar atas kekuatan adidaya diraih dengan keahlian manajerial perang yang dipadukan dengan tawakal mutlak kepada Allah.`,
    contentEn: `The First Arrow and the Test of Filial Devotion

Sa'd ibn Abi Waqqash embraced Islam during its earliest days in Makkah and holds the honor of shooting the first arrow in defense of the faith. His conversion faced intense emotional pressure from his mother, who staged a life-threatening hunger strike. Sa'd lovingly yet resolutely responded: "Dear mother, even if you possessed a hundred souls and each departed one by one, I would never abandon my faith." In response, divine revelation descended in Surah Luqman, mandating kindness to parents while forbidding compromise on monotheism.

The Prophet's Shield at Uhud

During the critical moments of Uhud, Sa'd stood valiantly defending the Prophet. Prophet Muhammad handed him arrows, exclaiming an honor reserved exclusively for Sa'd: "Shoot, O Sa'd! May my father and mother be ransomed for you!" The Prophet specifically prayed for him: "O Allah, direct his aim and answer his prayer." From that day forward, Sa'd's supplications were perpetually answered.

Conquest of Persia at al-Qadisiyyah

Under the caliphate of Umar ibn al-Khattab, Sa'd commanded the monumental campaign against the Sasanian Empire. At the decisive Battle of al-Qadisiyyah, his tactical genius dismantled imperial forces and war elephants, liberating the land and establishing Kufa as a flourishing center of Islamic civilization.

Key Lessons and Takeaways

1. Uncompromising Faith with Gracious Adab: Monotheism stands above all family loyalties while preserving respectful conduct toward parents.
2. The Potency of Answered Prayers: Miraculous supplication stems from pure sustenance, sincerity, and selfless sacrifice.
3. Visionary Leadership: Triumph against colossal odds requires disciplined tactical planning anchored in complete trust in God.`,
    contentMs: `Anak Panah Pertama dan Ujian Ibu Kandung

Sa'ad bin Abi Waqqash merupakan sahabat pertama melepaskan anak panah demi mempertahankan Islam. Apabila ibunya mogok lapar agar beliau murtad, Sa'ad bertegas mempertahankan akidah dengan sopan hingga turun ayat Surah Luqman.

Doa Mustajab dan Perisai Uhud

Di medan Uhud, Sa'ad memanah melindungi Rasulullah SAW sehingga Baginda mendoakan ketepatan panahannya dan kemustajaban doanya.

Panglima Penakluk Parsi di Al-Qadisiyyah

Khalifah Umar melantik Sa'ad mengetuai angkatan Islam menumbangkan tentera gajah empayar Parsi di Al-Qadisiyyah serta mengasaskan kota Kufah.

Hikmah dan Iktibar Utama

1. Keutamaan Tauhid: Akidah tidak boleh digadaikan walau menghadapi tekanan insan tersayang.
2. Keberkatan Rezeki Halal: Menjadi kunci terkabulnya doa oleh Allah SWT.
3. Keberanian dan Kepimpinan Taktikal: Menggabungkan strategi bijak dan kebergantungan kepada pertolongan Ilahi.`,
    quranReferences: [
      createRef(31, "14"),
      createRef(8, "17")
    ]
  },

  // 14. Abu Ubaidah bin Al-Jarrah
  {
    id: "sahabat-abu-ubaidah-bin-jarrah",
    categoryId: "companions",
    title: "Abu Ubaidah bin Al-Jarrah R.A.",
    titleAr: "أَبُو عُبَيْدَةَ بْنُ الْجَرَّاحِ",
    shortDescription: "Kepercayaan Umat ini (Aminul Ummah), panglima agung penakluk negeri Syam yang berhati lembut, zuhud, dan teguh dalam kejujuran.",
    shortDescriptionEn: "The Trustworthy of the Ummah (Amin al-Ummah), compassionate supreme commander of the Levant campaigns, famed for sublime asceticism.",
    shortDescriptionMs: "Orang Paling Amanah bagi Umat ini (Aminul Ummah), panglima besar pembukaan Syam yang penuh tawaduk dan zuhud.",
    content: `Aminul Ummah: Manusia Paling Amanah

Amir bin Abdullah bin Al-Jarrah, yang lebih dikenal sebagai Abu Ubaidah, adalah sosok sahabat berwibawa tinggi dengan senyum ramah dan keteguhan tak tergoyahkan. Ketika delegasi kaum Nasrani dari Najran datang meminta seorang penengah yang jujur untuk menyelesaikan perselisihan mereka, Rasulullah SAW memegang tangan Abu Ubaidah dan bersabda: "Setiap umat memiliki sosok yang paling terpercaya, dan orang paling terpercaya (Amin) di kalangan umat ini adalah Abu Ubaidah bin Al-Jarrah."

Keteguhan Akidah di Perang Badar dan Cinta di Uhud

Di medan Perang Badar, Abu Ubaidah diuji dengan ujian paling dahsyat ketika ayahnya sendiri yang kafir terus-menerus memburunya untuk dibunuh; setelah berulang kali menghindar, Abu Ubaidah akhirnya terpaksa menghadapinya demi membela keselamatan kaum muslimin, peristiwa yang diabadikan dalam Surah Al-Mujadilah. Pada Perang Uhud, ketika dua mata rantai topi besi menancap dalam di pipi mulia Rasulullah SAW, Abu Ubaidah mencabutnya dengan giginya sendiri dengan sangat hati-hati agar tidak menambah rasa sakit beliau, hingga dua gigi depannya tanggal dan menjadikannya semakin tampak bersahaja dan tampan.

Panglima Penakluk Syam yang Sangat Zuhud

Pada masa kekhalifahan Umar bin Khattab, Abu Ubaidah diangkat menjadi panglima tertinggi memimpin pembebasan wilayah Syam menggantikan Khalid bin Al-Walid. Keduanya saling menghormati tanpa sedikit pun intrik kekuasaan. Di bawah kepemimpinannya, Damaskus, Homs, dan Yerusalem dibebaskan dengan damai. Ketika Khalifah Umar mengunjunginya di markas tendanya di Syam, Umar mendapati panglima besar ini hanya tidur beralaskan pelana kuda dan mangkuk kayu tua, hingga Umar meneteskan air mata seraya berkata: "Dunia telah mengubah kita semua, kecuali engkau wahai Abu Ubaidah!"

Hikmah dan Pelajaran Berharga

1. Nilai Amanah dan Kejujuran: Integritas moral yang teruji melahirkan kepercayaan mutlak dari sesama manusia dan kehormatan di hadapan Allah.
2. Kesederhanaan di Puncak Kekuasaan: Kedudukan tinggi dan kemenangan militer tidak boleh mengubah gaya hidup zuhud dan kerendahan hati seorang pemimpin.
3. Persaudaraan di Atas Ego Jabatan: Kepemimpinan adalah amanah pelayanan; peralihan komando militer dijalani dengan saling memuliakan demi tujuan dakwah.`,
    contentEn: `The Custodian of the Ummah

Amir ibn Abdullah, known universally as Abu Ubaidah ibn al-Jarrah, embodied unwavering integrity and warmth. When the Christian delegation of Najran requested a trustworthy arbiter, Prophet Muhammad took Abu Ubaidah by the hand and declared: "Every community has a trustworthy custodian, and the custodian of this nation is Abu Ubaidah."

Steadfast Faith and Tender Devotion

During the Battle of Badr, Abu Ubaidah faced the agonizing trial of encountering his own father in enemy ranks; after repeatedly retreating, he stood firm in defense of truth, commemorated in Surah al-Mujadilah. At Uhud, when two links of armor pierced the Prophet's cheek, Abu Ubaidah gently extracted them with his incisors to minimize pain, sacrificing his front teeth in profound love for the Messenger.

The Ascetic Supreme Commander of the Levant

Caliph Umar appointed Abu Ubaidah supreme commander of the Levant campaigns. Leading forces alongside Khalid ibn al-Walid with boundless mutual reverence, Damascus, Homs, and Jerusalem opened their gates peacefully. Visiting his tent, Caliph Umar found nothing but a horse saddle and wooden bowl, weeping as he declared: "The world changed all of us, except you, O Abu Ubaidah!"

Key Lessons and Takeaways

1. The Sanctity of Trustworthiness: Unblemished integrity is the crown of spiritual character and social leadership.
2. Sublime Asceticism in Power: True leadership remains untouched by the luxuries and vanity of victory.
3. Egoless Collaboration: Serving the greater good eclipses personal ambitions of status and hierarchy.`,
    contentMs: `Gelaran Aminul Ummah

Abu Ubaidah digelar oleh Rasulullah SAW sebagai orang paling amanah dalam kalangan umat Islam kerana kejujuran dan ketulusannya.

Pengorbanan Cinta di Medan Uhud

Beliau mencabut cincin besi yang menusuk pipi Rasulullah SAW menggunakan giginya sendiri sehingga patah demi mengelakkan kesakitan Baginda.

Panglima Pembebasan Syam yang Zuhud

Mengetuai ekspedisi pembebasan Syam dan Baitulmaqdis, Abu Ubaidah hidup amat sederhana dalam khemah kecil hingga menitiskan air mata Khalifah Umar.

Hikmah dan Iktibar Utama

1. Ketinggian Sifat Amanah: Asas utama kepercayaan dalam kepimpinan.
2. Kezuhudan Pemimpin: Menolak kemewahan dunia walau memegang kuasa empayar.
3. Keikhlasan Berkhidmat: Meletakkan kepentingan ummah mendahului kepentingan peribadi.`,
    quranReferences: [
      createRef(58, "22"),
      createRef(9, "100")
    ]
  },

  // 15. Abu Hurairah
  {
    id: "sahabat-abu-hurairah",
    categoryId: "companions",
    title: "Abu Hurairah R.A.",
    titleAr: "أَبُو هُرَيْرَةَ",
    shortDescription: "Penjaga perbendaharaan Hadits Nabi, teladan pencari ilmu dari Ahli Suffah yang memiliki ingatan luar biasa berkat doa Rasulullah SAW.",
    shortDescriptionEn: "The foremost custodian of Prophetic Hadith, devoted scholar of Ahl al-Suffah endowed with photographic memory through the Prophet's prayer.",
    shortDescriptionMs: "Perawi hadis terbanyak, pembesar Ahli Suffah yang dikurniakan daya ingatan luar biasa berkat doa Rasulullah SAW.",
    content: `Kucing Kecil dan Kehidupan Zuhud Ahli Suffah

Abdurrahman bin Shakhr Ad-Dausi, yang akrab dipanggil Abu Hurairah (Bapak Kucing Kecil) karena kecintaan dan kasih sayangnya merawat anak kucing dalam lengan bajunya, memeluk Islam pada tahun ke-7 Hijriyah pada saat Perang Khaibar. Sejak tiba di Madinah, ia memilih tinggal di beranda Masjid Nabawi bersama kelompok Ahli Suffah, yaitu para sahabat miskin yang mendedikasikan seluruh waktu mereka untuk beribadah dan menuntut ilmu langsung dari lisan Rasulullah SAW. Rasa lapar yang melilit perutnya kerap ia tahan dengan mengikatkan batu, namun ia tidak pernah meninggalkan majelis Nabi walau sesaat.

Doa Berkah dan Mukjizat Ingatan Emas

Menyadari keterbatasan daya ingat manusia, Abu Hurairah pernah mengadu kepada Rasulullah SAW bahwa ia terkadang lupa hadits yang didengarnya. Rasulullah SAW bersabda: "Bentangkan selendangmu." Abu Hurairah membentangkan kainnya, lalu Rasulullah SAW membuat gerakan meraup dengan kedua tangan beliau dan menuangkannya ke atas selendang itu seraya bersabda: "Rapatkan selendangmu ke dadamu." Abu Hurairah merapatkannya, dan sejak detik itu, ia tidak pernah lupa satu huruf pun dari sabda yang didengarnya dari Rasulullah SAW.

Penjaga Warisan Sunnah Sepanjang Zaman

Ketika sahabat lain disibukkan oleh urusan perniagaan di pasar atau perkebunan kurma, Abu Hurairah senantiasa membersamai Rasulullah SAW mencatat setiap gerak-gerik, fatwa, dan ketetapan beliau. Dengan ketekunan luar biasa, ia meriwayatkan lebih dari 5.300 hadits shahih yang menjadi fondasi utama hukum Islam dan akhlak umat manusia. Di akhir hayatnya, ia membagi malamnya menjadi tiga bagian: sepertiga untuk shalat malam, sepertiga untuk tidur, dan sepertiga untuk mengulang-ulang hafalan hadits Nabi.

Hikmah dan Pelajaran Berharga

1. Dedikasi Total Menuntut Ilmu: Kemiskinan materi bukanlah penghalang untuk meraih puncak kemuliaan ilmu dan amal jariyah.
2. Kasih Sayang kepada Makhluk Lemah: Julukan Abu Hurairah membuktikan bahwa Islam menghargai kelembutan hati dan kasih sayang kepada hewan ciptaan Allah.
3. Penjagaan Warisan Syariat: Pelestarian sunnah Nabi adalah pilar kokoh yang menjaga keaslian ajaran agama dari generasi ke generasi.`,
    contentEn: `The Father of Kittens and Ahl al-Suffah

Abdurrahman ibn Sakhr al-Dawsi, affectionately nicknamed Abu Hurairah ('Father of Kittens') for his tenderness in sheltering a stray kitten, embraced Islam in 7 AH. Arriving in Madinah, he joined Ahl al-Suffah on the veranda of the Prophet's Mosque—devoting his life entirely to worship and absorbing sacred knowledge. Enduring severe hunger, he never departed from the Prophet's assembly.

The Prophet's Blessing and Phenomenal Memory

Once lamenting forgetfulness to the Prophet, the Messenger instructed him to spread out his cloak, made a gesture of pouring into it, and told him to fold it to his chest. From that blessed instant, Abu Hurairah never forgot a single word he heard from the Messenger of God.

Guardian of the Sunnah

While others tended their markets or farms, Abu Hurairah shadowed the Prophet incessantly. He transmitted over 5,300 authentic traditions, preserving the legal and ethical foundation of Islamic civilization. He divided his nights into three segments: one-third for nocturnal prayer, one-third for rest, and one-third for reviewing hadith.

Key Lessons and Takeaways

1. Unyielding Passion for Sacred Knowledge: Material hardship presents no barrier to achieving immortal intellectual and spiritual legacy.
2. Compassion for Creation: The moniker Abu Hurairah reflects Islamic reverence for gentleness toward animals.
3. Preservation of Prophetic Legacy: Diligent transmission of Prophetic teachings protects divine guidance across centuries.`,
    contentMs: `Kisah Gelaran dan Ahli Suffah

Diberi gelaran Abu Hurairah kerana belas kasihannya menjaga anak kucing. Beliau tinggal di beranda Masjid Nabawi menuntut ilmu dan beribadah tanpa menghiraukan kemiskinan.

Mukjizat Hafalan Hadis

Rasulullah SAW mendoakan selendang yang dibentangkannya, menjadikan ingatan Abu Hurairah amat tajam dan tidak pernah lupa sabda Nabi.

Khazanah Hadis Umat Islam

Meriwayatkan lebih 5,300 hadis sahih dan membahagikan waktu malamnya untuk solat, tidur, dan mengulang kaji ilmu sunnah.

Hikmah dan Iktibar Utama

1. Kesungguhan Menuntut Ilmu: Mengutamakan ilmu agama melebihi mengejar kebendaan dunia.
2. Kasih Sayang Terhadap Haiwan: Ajaran Islam menekankan rahmah kepada semua makhluk.
3. Tanggungjawab Memelihara Sunnah: Menjaga ketulenan ajaran Islam sepanjang zaman.`,
    quranReferences: [
      createRef(9, "122"),
      createRef(58, "11")
    ]
  },

  // 16. Abdullah bin Mas'ud
  {
    id: "sahabat-abdullah-bin-masud",
    categoryId: "companions",
    title: "Abdullah bin Mas'ud R.A.",
    titleAr: "عَبْدُ اللَّهِ بْنُ مَسْعُودٍ",
    shortDescription: "Shahibul Sirr (Pemegang Rahasia Nabi), pembaca Al-Qur'an pertama yang mengumandangkannya di hadapan pembesar Ka'bah, berilmu luas dan bertubuh kurus.",
    shortDescriptionEn: "The Keeper of the Prophet's Secrets, first to boldly recite the Qur'an publicly at the Ka'bah, master jurist of sublime faith.",
    shortDescriptionMs: "Pemegang Rahsia Rasulullah SAW, orang pertama melantunkan Al-Qur'an secara terbuka di Kaabah, ahli fiqh terkemuka.",
    content: `Penggembala Cilik yang Mengumandangkan Kalamullah di Ka'bah

Abdullah bin Mas'ud (Ibnu Mas'ud) awalnya adalah seorang remaja penggembala kambing milik Uqbah bin Abi Mu'aith di perbukitan Makkah. Suatu hari, Rasulullah SAW dan Abu Bakar yang kehausan meminta susunya; Nabi mengusap puting kambing betina muda yang belum kawin, lalu dengan izin Allah keluarlah susu segar yang berlimpah. Takjub dengan mukjizat itu, Ibnu Mas'ud seketika beriman dan meminta diajarkan kalimat-kalimat hikmah. Di saat kaum muslimin masih merahasiakan bacaan Al-Qur'an karena kezaliman musyrikin, Ibnu Mas'ud adalah orang pertama setelah Rasulullah yang berani berdiri di samping Maqam Ibrahim dan melantunkan Surah Ar-Rahman dengan suara lantang, hingga para pembesar kafir memukulinya hingga berdarah-darah, namun ia tetap tersenyum bangga.

Shahibul Sirr dan Khadam Mulia Rasulullah

Ibnu Mas'ud mendapat kehormatan menjadi pelayan pribadi dan pemegang rahasia Rasulullah SAW (Shahibul Sirr). Ia menyiapkan sandal, siwak, dan air wudhu bagi Nabi, berjalan di depan beliau saat bepergian, dan bebas masuk ke bilik rumah Rasulullah tanpa perlu izin khusus. Dari kedekatan intensif ini, ia menyerap akhlak, tata cara ibadah, dan pemahaman mendalam tentang setiap ayat Al-Qur'an, di mana ia diturunkan, dan apa sebab turunnya.

Tangisan Rasulullah dan Betis yang Lebih Berat dari Gunung Uhud

Rasulullah SAW sangat mencintai bacaan Al-Qur'an Ibnu Mas'ud dan bersabda: "Siapa yang ingin membaca Al-Qur'an segar seperti saat ia baru diturunkan, hendaklah membacanya menurut bacaan Ibnu Ummi Abd (Ibnu Mas'ud)." Suatu hari Nabi memintanya membacakan Surah An-Nisa; ketika sampai pada ayat ke-41 ("Maka bagaimanakah halnya orang kafir nanti, apabila Kami mendatangkan seorang saksi dari tiap-tiap umat..."), Rasulullah bersabda "Cukuplah", dan Ibnu Mas'ud melihat kedua mata mulia Nabi berlinang air mata. Pernah pula para sahabat tertawa melihat kedua betisnya yang sangat kurus tertiup angin saat memanjat pohon; Rasulullah SAW menegur mereka seraya bersumpah: "Demi Dzat yang jiwaku berada di tangan-Nya, sungguh kedua betis itu di timbangan mizan pada hari kiamat lebih berat daripada Gunung Uhud!"

Hikmah dan Pelajaran Berharga

1. Keberanian Mengumandangkan Kebenaran: Takut kepada celaan manusia tidak boleh membungkam seruan kalam Ilahi di tengah kebatilan.
2. Bobot Hakiki di Sisi Allah: Nilai seorang manusia ditentukan oleh ketakwaan hati dan timbangan amalnya, bukan oleh postur fisik atau penampilan lahiriah.
3. Menghayati Al-Qur'an dengan Jiwa: Membaca Kitabullah dengan penuh penghayatan menggetarkan sanubari dan melahirkan ketundukan hakiki kepada Sang Pencipta.`,
    contentEn: `The Shepherd Boy at the Ka'bah

Abdullah ibn Mas'ud was an impoverished young shepherd in the hills of Makkah. Witnessing a miraculous flow of milk from an unbred ewe when touched by the Prophet, he embraced Islam instantly. When Muslims concealed their worship, Ibn Mas'ud became the first believer after the Prophet to boldly stand before the Ka'bah and recite Surah ar-Rahman aloud. Despite savage beatings from Quraysh aristocrats, his resolve remained unshakeable.

The Keeper of the Prophet's Secrets

Granted intimate access to the Prophet's household, Ibn Mas'ud served as his personal attendant—bearing his sandals, tooth-stick (siwak), and water basin. Through this constant companionship, he internalized every nuance of the Prophet's character and the context behind every revealed verse.

Tears of the Prophet and the Weight of Mount Uhud

The Prophet loved listening to his recitation: "Whoever desires to recite the Qur'an fresh as it was revealed, let him recite according to the reading of Ibn Mas'ud." Once reading Surah an-Nisa until verse 41, the Prophet wept silently. Later, when companions chuckled at his slender legs swaying in the wind upon a tree, the Messenger cautioned: "By Him in whose hand is my soul, in the Divine Scale on the Day of Resurrection, they are heavier than Mount Uhud!"

Key Lessons and Takeaways

1. Courage in Proclaiming Truth: True faith banishes fear of tyrannical intimidation in conveying divine guidance.
2. The True Measure of Human Worth: Righteousness and devotion in the scales of God surpass all physical appearances.
3. Soulful Recitation of Revelation: Reciting the Qur'an with profound mindfulness moves the heart and unlocks spiritual humility.`,
    contentMs: `Penggembala Berani di Kaabah

Ibnu Mas'ud merupakan orang pertama membaca Al-Qur'an (Surah Ar-Rahman) secara lantang di hadapan pembesar musyrikin Kaabah sehingga dipukul kejam demi menegakkan kalimah Allah.

Pelayan Peribadi dan Khazanah Ilmu

Diberi izin bebas keluar masuk rumah Rasulullah SAW, memegang rahsia Baginda, dan mendalami sebab turunnya setiap ayat Al-Qur'an.

Bacaan Merdu dan Betis Seberat Gunung Uhud

Rasulullah SAW mengalirkan air mata mendengar bacaan Al-Qur'an Ibnu Mas'ud dan menegaskan bahawa kedua-dua betisnya yang kurus lebih berat daripada Gunung Uhud di timbangan mizan akhirat.

Hikmah dan Iktibar Utama

1. Keberanian Menyampaikan Kebenaran: Tidak gentar menghadapi ancaman musuh dalam dakwah.
2. Nilai Taqwa Sejati: Kemuliaan di sisi Allah diukur pada ketakwaan dan ilmu, bukan rupa fizikal.
3. Menghayati Kalamullah: Membaca Al-Qur'an dengan tadabbur menyentuh lubuk sanubari.`,
    quranReferences: [
      createRef(4, "41"),
      createRef(55, "1")
    ]
  },

  // 17. Abdullah bin Abbas
  {
    id: "sahabat-abdullah-bin-abbas",
    categoryId: "companions",
    title: "Abdullah bin Abbas R.A.",
    titleAr: "عَبْدُ اللَّهِ بْنُ عَبَّاسٍ",
    shortDescription: "Turjumanul Qur'an (Penafsir Al-Qur'an) dan Habrul Ummah (Tinta Umat), sepupu Rasulullah yang didoakan paham mendalam tentang agama dan takwil.",
    shortDescriptionEn: "The Interpreter of the Qur'an (Turjuman al-Qur'an) and Sage of the Ummah, blessed with profound comprehension of scripture and wisdom.",
    shortDescriptionMs: "Jurubahasa Al-Qur'an (Turjumanul Qur'an) dan Ulama Agung Ummah, didoakan Rasulullah SAW memiliki kefahaman mendalam dalam tafsir agama.",
    content: `Doa Rasulullah untuk Pemuda Cendekia

Abdullah bin Abbas (Ibnu Abbas) lahir tiga tahun sebelum peristiwa Hijrah. Sejak usia dini, putra paman Nabi, Al-Abbas bin Abdul Muthalib, ini menunjukkan kecerdasan dan dahaga luar biasa terhadap ilmu pengetahuan. Suatu hari, ia menyiapkan air wudhu untuk Rasulullah SAW di malam hari tanpa diminta; terpukau dengan ketulusan dan ketangkasannya, Rasulullah memeluknya erat ke dadanya seraya memanjatkan doa agung: "Ya Allah, pahamkanlah ia dalam urusan agama dan ajarkanlah kepadanya takwil (tafsir Al-Qur'an)." Doa mustajab inilah yang membuka pintu-pintu samudra pemahaman ilmu dalam dadanya.

Adab Luhur Menuntut Ilmu dari Para Sahabat Senior

Setelah wafatnya Rasulullah SAW, Ibnu Abbas yang masih remaja berkeliling mendatangi rumah para sahabat senior untuk mengumpulkan hadits dan riwayat. Seringkali ia duduk menunggu di depan pintu rumah seorang sahabat hingga debu padang pasir menyelimuti wajahnya demi tidak mengganggu waktu istirahat sang guru. Ketika sang sahabat keluar dan kaget melihat sepupu Rasulullah menunggu lama, Ibnu Abbas berkata dengan penuh takzim: "Ilmu itu harus didatangi, bukan menunggu didatangi."

Penasihat Utama Khalifah dan Lautan Ilmu Umat

Keluasan ilmu dan kedewasaan cara berpikir Ibnu Abbas membuat Khalifah Umar bin Khattab selalu menyertakannya dalam majelis syura bersama para sesepuh veteran Perang Badar. Ketika sebagian sahabat mempertanyakan kehadiran pemuda belia ini, Umar menguji mereka dengan tafsir Surah An-Nashr; saat sahabat lain menjawab secara harfiah tentang kemenangan, Ibnu Abbas dengan cerdas menangkap isyarat dekatnya ajal Rasulullah SAW, yang dibenarkan oleh Umar. Julukan 'Habrul Ummah' (Tinta Umat) dan 'Al-Bahr' (Samudra Ilmu) disematkan kepadanya karena fatwa-fatwanya yang mencerahkan ribuan penuntut ilmu dari seluruh penjuru dunia Islam.

Hikmah dan Pelajaran Berharga

1. Kerendahan Hati dalam Menuntut Ilmu: Kehormatan nasab tidak membuat seorang pencari ilmu enggan berlelah-lelah dan merendahkan diri di hadapan guru.
2. Kedalaman Pemahaman Melampaui Teks: Tafsir yang benar memadukan penguasaan bahasa, konteks wahyu, dan ketajaman bashirah ruhani.
3. Pemberian Ruang Bagi Generasi Muda Berilmu: Kecakapan intelektual dan integritas moral layak mendapat tempat terhormat dalam kepemimpinan dan permusyawaratan umat.`,
    contentEn: `The Supplication for the Young Scholar

Abdullah ibn Abbas, cousin of the Prophet, was born just three years before the Hijrah. Possessing an insatiable thirst for wisdom, he once thoughtfully prepared ablution water for the Prophet unprompted. Moved by his maturity, the Messenger embraced him and prayed: "O Allah, bestow upon him deep understanding of the religion and teach him the interpretation of the Book." This prayer unlocked vast oceans of insight within his heart.

Humility at the Thresholds of Knowledge

Following the Prophet's demise, young Ibn Abbas traversed Madinah visiting senior companions to record Prophetic traditions. He frequently rested on doorsteps until swept by desert dust, refusing to disturb resting scholars. When astonished teachers stepped out to find the Prophet's kin waiting outside, he replied with supreme grace: "Knowledge must be sought; it does not come seeking you."

Council of Elders and Sage of the Ummah

His intellectual depth led Caliph Umar to seat him alongside senior veterans of Badr. When tested on Surah an-Nasr, while others interpreted it merely as triumphant conquest, Ibn Abbas astutely deduced the impending departure of the Prophet, winning Umar's ultimate praise. Celebrated as 'Habr al-Ummah' (The Ink of the Nation) and 'The Ocean', his pedagogical legacy illuminated generations of jurists.

Key Lessons and Takeaways

1. Humility in Pursuit of Knowledge: Noble lineage never excuses a seeker from patient humility before authentic teachers.
2. Depth beyond Literalism: Correct exegesis harmonizes linguistic mastery, historical context, and spiritual discernment.
3. Empowering Capable Youth: Scholarly prowess and moral maturity merit full recognition in communal council and governance.`,
    contentMs: `Doa Rasulullah SAW Membuka Pintu Ilmu

Ibnu Abbas didoakan secara khusus oleh Rasulullah SAW agar dikurniakan kefahaman mendalam dalam agama dan ilmu tafsir Al-Qur'an.

Adab Mulia Mencari Hadis

Beliau sanggup menunggu di hadapan pintu rumah para sahabat senior di bawah terik mentari dan debu demi menyalin ilmu hadis.

Penasihat Utama Majlis Khalifah Umar

Khalifah Umar melantik Ibnu Abbas menyertai majlis syura veteran Badar kerana kebijaksanaan tafsirnya, menjadikannya rujukan utama umat Islam (Habrul Ummah).

Hikmah dan Iktibar Utama

1. Adab dan Ketekunan Menuntut Ilmu: Sedia bersusah payah demi menguasai ilmu yang bermanfaat.
2. Kedalaman Kefahaman Agama: Menggali rahsia kalamullah dengan ilmu dan hikmah.
3. Menghargai Bakat Generasi Muda: Memberi ruang kepada anak muda berwibawa memimpin masyarakat.`,
    quranReferences: [
      createRef(2, "269"),
      createRef(35, "28")
    ]
  },

  // 18. Fatimah Az-Zahra
  {
    id: "sahabat-fatimah-az-zahra",
    categoryId: "companions",
    title: "Fatimah Az-Zahra R.A.",
    titleAr: "فَاطِمَةُ الزَّهْرَاءُ",
    shortDescription: "Sayyidatu Nisa'il 'Alamin (Penghulu Wanita Semesta Alam), putri bungsu kecintaan Rasulullah SAW, ibunda para pemuda surga Hasan dan Husain.",
    shortDescriptionEn: "The Master of the Women of the Worlds (Sayyidat Nisa al-Alamin), beloved youngest daughter of the Prophet and mother of Hasan and Husain.",
    shortDescriptionMs: "Penghulu Sekalian Wanita di Syurga, puteri kesayangan Rasulullah SAW dan bonda kepada Sayyidina Hasan dan Husain.",
    content: `Ummu Abiha: Pembela Ayahanda di Masa Sukar

Fatimah Az-Zahra lahir di Makkah ketika Rasulullah SAW mulai menerima wahyu ilahi. Sejak kanak-kanak, Fatimah menunjukkan keteguhan dan cinta mendalam kepada sang ayah. Ketika pembesar Quraisy menumpahkan kotoran isi perut unta ke punggung Rasulullah SAW saat beliau sedang sujud di dekat Ka'bah, Fatimah kecil berlari menghampiri dengan berani membersihkan kotoran tersebut seraya memarahi para penindas. Kelembutan dan pemeliharaannya yang begitu tulus kepada Nabi membuatnya mendapat julukan penuh cinta dari Rasulullah: "Ummu Abiha" (Ibu bagi ayahnya sendiri).

Kehidupan Rumah Tangga yang Penuh Zuhud dan Cahaya

Fatimah menikah dengan Ali bin Abi Thalib dalam mahar yang sangat sederhana, yakni sebuah baju besi (baju perang Hathami). Kehidupan rumah tangga mereka dijalani dengan penuh kezuhudan; tangan Fatimah melepuh karena memutar gilingan gandum sendiri dan memikul tempat air. Ketika keduanya menghadap Rasulullah SAW untuk meminta seorang pembantu, Nabi tidak memberi mereka harta melainkan mengajarkan wirid agung sebelum tidur: bertasbih 33 kali, bertahmid 33 kali, dan bertakbir 34 kali, yang nilainya jauh lebih utama dari seorang pelayan dunia.

Kedermawanan Tulus dan Penghulu Wanita di Surga

Kedermawanan Fatimah dan Ali diabadikan Allah SWT dalam Surah Al-Insan; ketika mereka bernazar berpuasa dan hendak berbuka, tiga hari berturut-turut datang orang miskin, anak yatim, dan tawanan yang kelaparan, dan keluarga suci ini memberikan seluruh jatah roti mereka hingga hanya berbuka dengan air putih sembari berucap: "Sesungguhnya kami memberi makan kepadamu hanyalah karena mengharapkan keridhaan Allah, kami tidak menghendaki balasan dan terima kasih dari kamu." Menjelang wafatnya Rasulullah SAW, beliau berbisik kepada Fatimah bahwa beliau akan segera wafat yang membuatnya menangis, lalu Nabi berbisik lagi bahwa Fatimah adalah anggota keluarga pertama yang akan menyusulnya dan dialah Sayyidatu Nisa'i Ahlil Jannah (pemimpin wanita surga), yang membuatnya tersenyum bahagia.

Hikmah dan Pelajaran Berharga

1. Bakti dan Pembelaan Anak kepada Orang Tua: Kasih sayang tulus seorang anak tercermin dari keberanian mendampingi dan meringankan beban orang tua dalam perjuangan hidup.
2. Kemuliaan Rumah Tangga di Atas Kesederhanaan: Kebahagiaan sejati sebuah keluarga dibangun di atas ketakwaan, saling menguatkan dalam ibadah, dan rida atas ketentuan rezeki.
3. Kedermawanan Tanpa Pamrih: Mengorbankan apa yang paling dicintai demi menolong orang yang kesusahan adalah sifat luhur penghuni surga.`,
    contentEn: `Mother of Her Father in Times of Peril

Fatimah az-Zahra was born in Makkah during the dawn of divine revelation. Endowed with profound grace, she fiercely protected her father. When Quraysh tormentors cast camel entrails upon the prostrating Prophet at the Ka'bah, young Fatimah bravely rushed forward to cleanse him and rebuke the oppressors. Her nurturing tenderness earned her the cherished title 'Ummu Abiha' (The Mother of Her Father).

Sublime Simplicity and Heavenly Litany

Fatimah married Ali ibn Abi Talib with a humble dowry of a breastplate. Their domestic life exemplified sacred modesty; her hands calloused from grinding barley and bearing water skins. Requesting a helper from the Prophet, the Messenger instead gifted them a spiritual treasure before sleep: 33 Tasbih, 33 Tahmid, and 34 Takbir—better than all worldly servants.

The Altruism of the Queen of Paradise

Their selflessness is immortalized in Surah al-Insan: fasting three consecutive days, they gifted their entire meals to a destitute person, an orphan, and a captive, subsisting purely on water for the sake of God alone. Whispering to her before his departure, the Prophet revealed she would be the first to rejoin him and crowned her 'Sayyidat Nisa al-Jannah' (Master of the Women of Paradise).

Key Lessons and Takeaways

1. Fierce Filial Devotion: Authentic love manifests in courageously supporting parents through tribulations.
2. Spiritual Elevation in Modesty: Enduring domestic peace thrives upon piety, mutual support, and contentment.
3. Pure Altruism: Sacrificing one's essential sustenance for the vulnerable mirrors the sublime ethics of Paradise.`,
    contentMs: `Gelaran Ummu Abiha

Fatimah membersihkan kotoran yang dilemparkan musyrikin ke atas Rasulullah SAW di Kaabah dan sentiasa menjaga kebajikan ayahandanya dengan penuh kasih sayang.

Kehidupan Rumah Tangga yang Zuhud

Berkahwin dengan Ali bin Abi Thalib dalam serba sederhana, mengisar gandum sendiri dan mengamalkan tasbih Fatimah sebelum tidur sebagai ganti pembantu rumah.

Pemimpin Wanita Syurga

Menginfakkan hidangan berbuka kepada orang miskin, anak yatim, dan tawanan selama tiga hari berturut-turut, diiktiraf Rasulullah SAW sebagai penghulu wanita di syurga.

Hikmah dan Iktibar Utama

1. Bakti Anak Solehah: Mendampingi ibu bapa dalam susah dan senang.
2. Kesederhanaan Rumah Tangga: Mencari reda Allah melalui kesabaran dan ibadah.
3. Keikhlasan Menginfak: Memberi bantuan kepada yang memerlukan tanpa mengharapkan balasan manusia.`,
    quranReferences: [
      createRef(76, "8"),
      createRef(33, "33")
    ]
  },

  // 19. Mu'adz bin Jabal
  {
    id: "sahabat-muadz-bin-jabal",
    categoryId: "companions",
    title: "Mu'adz bin Jabal R.A.",
    titleAr: "مُعَاذُ بْنُ جَبَلٍ",
    shortDescription: "Sahabat muda yang paling menguasai hukum halal dan haram, diutus sebagai hakim agung ke Yaman, dan imam para ulama pada hari kiamat.",
    shortDescriptionEn: "The foremost authority on lawful and prohibited ordinances, dispatched as supreme judge to Yemen, imam of the scholars on Judgment Day.",
    shortDescriptionMs: "Sahabat paling alim dalam hukum halal dan haram, diutuskan sebagai hakim ke Yaman, pemimpin para ulama di akhirat kelak.",
    content: `Cendekiawan Muda Penguasa Halal dan Haram

Mu'adz bin Jabal adalah pemuda tampan, bermata tajam, dan berbudi luhur dari kalangan Anshar suku Khazraj. Ia memeluk Islam pada usia 18 tahun saat Bai'at Aqabah Kedua. Memiliki kecerdasan luar biasa dan ketelitian dalam mencerna syariat, Rasulullah SAW memberikan pengakuan agung kepadanya: "Orang yang paling mengerti tentang halal dan haram di antara umatku adalah Mu'adz bin Jabal." Rasulullah juga menyebutnya sebagai salah satu dari empat sahabat rujukan utama untuk belajar Al-Qur'an.

Prinsip Ijtihad dan Utusan Agung ke Yaman

Ketika dakwah Islam berkembang pesat dan wilayah Yaman tunduk di bawah naungan Islam, Rasulullah SAW memilih Mu'adz sebagai gubernur, hakim agung, dan da'i untuk membimbing masyarakat di sana. Sebelum melepas keberangkatannya, terjadi dialog bersejarah tentang metodologi hukum Islam. Rasulullah bertanya: "Bagaimana engkau akan memutuskan perkara jika diajukan kepadamu?" Mu'adz menjawab: "Aku akan memutuskan berdasarkan Kitabullah (Al-Qur'an)." Nabi bertanya lagi: "Jika engkau tidak menemukannya di Kitabullah?" Mu'adz menjawab: "Dengan Sunnah Rasulullah." Nabi bertanya lagi: "Jika engkau tidak menemukannya di Sunnah Rasulullah?" Mu'adz menjawab: "Aku akan berijtihad dengan akal pikiranku dan aku tidak akan lalai." Mendengar itu, Rasulullah menepuk dada Mu'adz dengan bangga seraya memuji Allah yang telah membimbing utusan-Nya.

Cinta Rasulullah dan Doa Setelah Shalat

Rasulullah SAW sangat mencintai Mu'adz. Suatu hari Nabi memegang tangannya seraya bersabda: "Wahai Mu'adz, demi Allah, sesungguhnya aku benar-benar mencintaimu. Janganlah sekali-kali engkau lupa membaca doa ini di akhir setiap shalat: 'Allahumma a'inni 'ala dzikrika wa syukrika wa husni 'ibadatik' (Ya Allah, tolonglah aku untuk selalu mengingat-Mu, bersyukur kepada-Mu, dan memperbaiki ibadahku kepada-Mu)." Di akhirat kelak, Rasulullah mengabarkan bahwa Mu'adz akan dibangkitkan memimpin para ulama sejauh lemparan batu di hadapan mereka.

Hikmah dan Pelajaran Berharga

1. Keutamaan Menguasai Ilmu Syariat: Memahami batas halal dan haram secara mendalam adalah lentera yang menjaga manusia dari jurang kemungkaran.
2. Fleksibilitas Ijtihad dalam Bingkai Syariat: Metodologi hukum Islam membuka ruang penalaran akal sehat (ijtihad) yang berakar kokoh pada Al-Qur'an dan As-Sunnah.
3. Keseimbangan Doa dan Amal Shalih: Rasa cinta kepada Allah diwujudkan melalui dzikir yang konsisten, syukur atas nikmat, dan penyempurnaan kualitas ibadah.`,
    contentEn: `Master of the Lawful and Unlawful

Mu'adh ibn Jabal of the Ansar embraced Islam at eighteen during the Second Pledge of Aqabah. Blessed with luminous intellect, Prophet Muhammad praised his profound jurisprudence: "The most knowledgeable of my nation concerning the lawful and unlawful is Mu'adh ibn Jabal." He was among four elite scholars endorsed by the Prophet for mastering the Qur'an.

The Bedrock of Legal Reasoning and Mission to Yemen

Dispatched as governor and chief justice to Yemen, the Prophet engaged him in the definitive dialogue on Islamic jurisprudence. Asked how he would judge cases, Mu'adh answered: "By the Book of God." If not found therein: "By the Sunnah of God's Messenger." And if not found therein: "I will exercise reasoned judgment (ijtihad) without neglecting guidance." The Prophet struck his chest with joy, thanking God for guiding His emissary.

Prophetic Love and the Supplication of Remembrance

Holding Mu'adh's hand, the Prophet declared: "O Mu'adh, by Allah, I truly love you! Never fail to recite after every prayer: 'O Allah, assist me in remembering You, showing gratitude to You, and perfecting Your worship.'" On the Day of Resurrection, the Prophet foretold that Mu'adh will lead the assembly of scholars.

Key Lessons and Takeaways

1. Supremacy of Legal Comprehension: Clear discernment of sacred boundaries protects communities from transgression.
2. Dynamic Jurisprudence: Authentic Islamic thought incorporates analytical reason rooted firmly in foundational revelation.
3. Heartfelt Devotion: True love of God blossoms through continuous remembrance, sincere gratitude, and excellence in worship.`,
    contentMs: `Keilmuan Halal dan Haram

Mu'adz bin Jabal diiktiraf oleh Rasulullah SAW sebagai sahabat paling pakar mengenai hukum halal dan haram serta menjadi rujukan utama Al-Qur'an.

Asas Ijtihad ke Yaman

Diutus sebagai hakim ke Yaman, Mu'adz menegaskan prinsip membuat keputusan berasaskan Al-Qur'an, Sunnah, dan ijtihad akal, yang dipuji tinggi oleh Rasulullah SAW.

Doa Agung Selepas Solat

Rasulullah SAW menyatakan kasih sayang kepada Mu'adz dan mengajarkan doa agar sentiasa dibantu berzikir, bersyukur, dan memperelok ibadah.

Hikmah dan Iktibar Utama

1. Kepakaran Ilmu Hukum: Memahami syariat dengan mendalam menjamin keadilan ummah.
2. Prinsip Berijtihad: Menggunakan pertimbangan akal berpandukan wahyu.
3. Istiqamah Berzikir: Memohon pertolongan Ilahi dalam menyempurnakan amal ibadah.`,
    quranReferences: [
      createRef(16, "125"),
      createRef(3, "110")
    ]
  },

  // 20. Zaid bin Tsabit
  {
    id: "sahabat-zaid-bin-tsabit",
    categoryId: "companions",
    title: "Zaid bin Tsabit R.A.",
    titleAr: "زَيْدُ بْنُ ثَابِتٍ",
    shortDescription: "Sekretaris Wahyu Rasulullah SAW, ketua komite kodifikasi Mushaf Al-Qur'an standar, dan pakar faraidh (ilmu waris) terkemuka.",
    shortDescriptionEn: "The Chief Scribe of Divine Revelation, architect of the standardized Qur'anic Mushaf, and premier master of inheritance law.",
    shortDescriptionMs: "Penulis Wahyu Rasulullah SAW, ketua penghimpunan Mushaf Al-Qur'an, dan pakar ilmu faraid terulung.",
    content: `Pemuda Cerdas Penghafal Wahyu dan Ahli Bahasa

Zaid bin Tsabit memeluk Islam pada usia belia di Madinah. Ketika berusia 13 tahun, ia ingin ikut bertempur di Perang Badar namun Rasulullah SAW menolaknya dengan lembut karena usianya yang masih sangat muda. Mengetahui kecerdasan otak Zaid yang luar biasa, Rasulullah SAW memerintahkannya untuk mempelajari bahasa Ibrani dan Suryani agar dapat berkorespondensi dengan kaum Yahudi. Dengan ketekunan menakjubkan, Zaid menguasai bahasa tersebut hanya dalam waktu belasan hari. Sejak itu, Zaid diangkat menjadi salah satu juru tulis utama yang mencatat setiap wahyu Al-Qur'an yang turun di hadapan Rasulullah SAW.

Ketua Tim Kodifikasi Al-Qur'an Era Abu Bakar

Setelah Perang Yamamah yang menewaskan puluhan sahabat penghafal Al-Qur'an (huffadz), Khalifah Abu Bakar Ash-Shiddiq atas usulan Umar bin Khattab memanggil Zaid bin Tsabit untuk mengemban amanah mahaberat: menghimpun Al-Qur'an dari pelepah kurma, lempengan batu, dan hafalan dada para sahabat ke dalam satu mushaf utuh. Zaid berkata tentang beratnya tugas itu: "Demi Allah, seandainya mereka memerintahkanku memindahkan salah satu gunung, niscaya itu tidak lebih berat bagiku daripada perintah mengumpulkan Al-Qur'an." Zaid menerapkan standar verifikasi paling ketat; tidak ada satu ayat pun yang ditulis kecuali disaksikan oleh dua orang saksi adil yang mendengarnya langsung dari Rasulullah SAW.

Penyusunan Mushaf Utsmani dan Pakar Faraidh

Pada masa Khalifah Utsman bin Affan, perselisihan bacaan Al-Qur'an di berbagai pelosok wilayah Islam memuncak. Utsman kembali menunjuk Zaid bin Tsabit memimpin tim perumus mushaf standar (Mushaf Al-Imam / Rasm Utsmani) dengan dialek Quraisy sebagai pedoman abadi kaum muslimin hingga akhir zaman. Selain jasanya dalam Al-Qur'an, Zaid adalah sahabat yang paling mahir dalam ilmu faraidh (hukum waris). Ketika Ibnu Abbas memegang tali kekang kendaraannya sebagai bentuk penghormatan kepada ulama, Zaid mencium tangan Ibnu Abbas sebagai penghormatan kepada keluarga Nabi, mencerminkan indahnya adab di antara sesama sahabat.

Hikmah dan Pelajaran Berharga

1. Pemeliharaan Mukjizat Al-Qur'an: Usaha kodifikasi yang teliti dan sistematis adalah wujud nyata janji Allah dalam menjaga kesucian Al-Qur'an sepanjang masa.
2. Mengembangkan Potensi Diri Generasi Muda: Kematangan intelektual dan penguasaan bahasa menjadi modal vital dalam memajukan peradaban umat.
3. Saling Memuliakan Antara Ilmu dan Ahli Bait: Adab saling menghormati antara pemegang ilmu dan kerabat Nabi adalah teladan luhur persaudaraan Islam.`,
    contentEn: `The Scribe of Revelation and Linguist

Zaid ibn Thabit was a brilliant youth in Madinah. Deemed too young for combat at Badr, the Prophet recognized his intellectual gifts and commissioned him to master Hebrew and Syriac for diplomatic correspondence. Astoundingly, Zaid mastered both within weeks, swiftly becoming the Prophet's foremost scribe tasked with transcribing freshly revealed verses.

Compiling the Sacred Text under Abu Bakar

Following the Battle of Yamamah where dozens of Qur'an memorizers fell, Caliph Abu Bakar and Umar tasked Zaid with compiling the fragmented records into a single unified codex. Reflecting on the colossal responsibility, Zaid remarked: "By God, had they ordered me to move a mountain, it would not have been heavier than ordering me to compile the Qur'an." He instituted rigorous verification: requiring two upright witnesses for every inscribed verse.

The Uthmanic Codex and Mastery of Inheritance Law

Decades later, Caliph Uthman appointed Zaid to chair the committee standardizing the official Mushaf in the Quraysh dialect, preserving divine revelation across the globe for eternity. Renowned equally for mastery of inheritance law (fara'id), his mutual deference with Ibn Abbas remains a timeless archetype of scholarly and spiritual courtesy.

Key Lessons and Takeaways

1. Safeguarding Divine Revelation: Rigorous methodology and unmatched scrutiny manifest the divine pledge to preserve scripture forever.
2. Nurturing Youthful Intellect: Cultivating linguistic and analytical skills empowers youth to lead monumental civilizational duties.
3. Mutual Reverence in Faith: The harmonious adab between scholars and Prophetic lineage shines as an eternal model of Muslim fraternity.`,
    contentMs: `Penulis Wahyu dan Pakar Bahasa

Zaid bin Tsabit menguasai bahasa Ibrani dan Suryani dalam masa singkat atas arahan Nabi, lantas dilantik menjadi penulis utama wahyu Al-Qur'an.

Menghimpun Lembaran Al-Qur'an

Khalifah Abu Bakar mengamanahkan Zaid mengetuai usaha menghimpun ayat Al-Qur'an dengan piawaian saksi yang amat ketat selepas gugurnya para huffaz di Yamamah.

Penyusunan Mushaf Utsmani

Mengetuai jawatankuasa menyelaraskan Mushaf Utsmani serta menjadi rujukan tertinggi umat dalam ilmu faraid (pembahagian harta pusaka).

Hikmah dan Iktibar Utama

1. Pemeliharaan Kesucian Al-Qur'an: Usaha rapi memastikan kalamullah terpelihara tanpa sebarang perubahan.
2. Penguasaan Ilmu dan Bahasa: Memanfaatkan kepintaran untuk berkhidmat kepada agama.
3. Menghormati Ulama dan Ahlul Bait: Menunjukkan kemuliaan akhlak dalam menuntut dan menyebarkan ilmu.`,
    quranReferences: [
      createRef(15, "9"),
      createRef(75, "17")
    ]
  }
];

const topicsPath = 'app/src/main/assets/encyclopedia/topics.json';
const allTopics = JSON.parse(fs.readFileSync(topicsPath, 'utf8'));

// Find where companions are and append
const prophets = allTopics.filter(t => t.categoryId === 'prophets');
const existingCompanions = allTopics.filter(t => t.categoryId === 'companions');
const fiqh = allTopics.filter(t => t.categoryId === 'fiqh');
const other = allTopics.filter(t => t.categoryId !== 'prophets' && t.categoryId !== 'companions' && t.categoryId !== 'fiqh');

// Check if any part2 already exist
const newCompanions = companionsPart2.filter(c => !existingCompanions.some(ec => ec.id === c.id));

const fullCompanions = [...existingCompanions, ...newCompanions];

const finalTopics = [...prophets, ...fullCompanions, ...fiqh, ...other];

fs.writeFileSync(topicsPath, JSON.stringify(finalTopics, null, 2), 'utf8');
console.log('Successfully written', finalTopics.length, 'topics total.');
console.log('Companions count:', fullCompanions.length);

if (fs.existsSync(dbTempPath)) {
  fs.unlinkSync(dbTempPath);
}

const fs = require('fs');
const zlib = require('zlib');
const { execSync } = require('child_process');

const dbGzPath = 'app/src/main/assets/quran/qurannew.db.gz';
const dbTempPath = '/tmp/quran_ref_fix.db';

console.log('Decompressing quran database...');
fs.writeFileSync(dbTempPath, zlib.gunzipSync(fs.readFileSync(dbGzPath)));

const surahNames = {
  2: { id: "Al-Baqarah", en: "Al-Baqarah", ms: "Al-Baqarah" },
  3: { id: "Ali 'Imran", en: "Ali 'Imran", ms: "Ali 'Imran" },
  4: { id: "An-Nisa'", en: "An-Nisa", ms: "An-Nisa'" },
  6: { id: "Al-An'am", en: "Al-An'am", ms: "Al-An'am" },
  7: { id: "Al-A'raf", en: "Al-A'raf", ms: "Al-A'raf" },
  8: { id: "Al-Anfal", en: "Al-Anfal", ms: "Al-Anfal" },
  9: { id: "At-Taubah", en: "At-Tawbah", ms: "At-Taubah" },
  10: { id: "Yunus", en: "Yunus", ms: "Yunus" },
  11: { id: "Hud", en: "Hud", ms: "Hud" },
  12: { id: "Yusuf", en: "Yusuf", ms: "Yusuf" },
  14: { id: "Ibrahim", en: "Ibrahim", ms: "Ibrahim" },
  15: { id: "Al-Hijr", en: "Al-Hijr", ms: "Al-Hijr" },
  16: { id: "An-Nahl", en: "An-Nahl", ms: "An-Nahl" },
  19: { id: "Maryam", en: "Maryam", ms: "Maryam" },
  20: { id: "Taha", en: "Taha", ms: "Taha" },
  21: { id: "Al-Anbiya'", en: "Al-Anbiya", ms: "Al-Anbiya'" },
  24: { id: "An-Nur", en: "An-Nur", ms: "An-Nur" },
  26: { id: "Asy-Syu'ara'", en: "Ash-Shu'ara", ms: "Asy-Syu'ara'" },
  27: { id: "An-Naml", en: "An-Naml", ms: "An-Naml" },
  28: { id: "Al-Qashash", en: "Al-Qasas", ms: "Al-Qashash" },
  29: { id: "Al-'Ankabut", en: "Al-'Ankabut", ms: "Al-'Ankabut" },
  31: { id: "Luqman", en: "Luqman", ms: "Luqman" },
  33: { id: "Al-Ahzab", en: "Al-Ahzab", ms: "Al-Ahzab" },
  35: { id: "Fatir", en: "Fatir", ms: "Fatir" },
  37: { id: "Ash-Shaffat", en: "As-Saffat", ms: "Ash-Shaffat" },
  38: { id: "Shad", en: "Sad", ms: "Shad" },
  39: { id: "Az-Zumar", en: "Az-Zumar", ms: "Az-Zumar" },
  48: { id: "Al-Fath", en: "Al-Fath", ms: "Al-Fath" },
  49: { id: "Al-Hujurat", en: "Al-Hujurat", ms: "Al-Hujurat" },
  51: { id: "Adz-Dzariyat", en: "Adh-Dhariyat", ms: "Adz-Dzariyat" },
  54: { id: "Al-Qamar", en: "Al-Qamar", ms: "Al-Qamar" },
  55: { id: "Ar-Rahman", en: "Ar-Rahman", ms: "Ar-Rahman" },
  58: { id: "Al-Mujadilah", en: "Al-Mujadila", ms: "Al-Mujadilah" },
  59: { id: "Al-Hasyr", en: "Al-Hashr", ms: "Al-Hasyr" },
  62: { id: "Al-Jumu'ah", en: "Al-Jumu'ah", ms: "Al-Jumu'ah" },
  71: { id: "Nuh", en: "Nuh", ms: "Nuh" },
  75: { id: "Al-Qiyamah", en: "Al-Qiyamah", ms: "Al-Qiyamah" },
  76: { id: "Al-Insan", en: "Al-Insan", ms: "Al-Insan" },
  92: { id: "Al-Lail", en: "Al-Layl", ms: "Al-Lail" },
  93: { id: "Adh-Dhuha", en: "Ad-Duha", ms: "Adh-Dhuha" },
  94: { id: "Al-Insyirah", en: "Ash-Sharh", ms: "Al-Insyirah" }
};

function getVerseData(surah, start, end) {
  const sql = `SELECT text, indonesian, translation_en, malay, transliteration_id, transliteration_en FROM ayas WHERE sura=${surah} AND aya BETWEEN ${start} AND ${end} ORDER BY aya;`;
  const raw = execSync(`sqlite3 -json "${dbTempPath}" "${sql}"`).toString().trim();
  const rows = JSON.parse(raw);

  const arList = [];
  const idList = [];
  const enList = [];
  const msList = [];
  const latinList = [];
  const latinEnList = [];

  rows.forEach(row => {
    arList.push((row.text || '').trim());
    idList.push((row.indonesian || '').trim());
    enList.push((row.translation_en || '').trim());
    msList.push((row.malay || row.indonesian || '').trim());
    latinList.push((row.transliteration_id || '').trim());
    latinEnList.push((row.transliteration_en || row.transliteration_id || '').trim());
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

const topicsPath = 'app/src/main/assets/encyclopedia/topics.json';
const topics = JSON.parse(fs.readFileSync(topicsPath, 'utf8'));

let updatedRefs = 0;
topics.forEach(topic => {
  if (topic.quranReferences && topic.quranReferences.length > 0) {
    topic.quranReferences = topic.quranReferences.map(ref => {
      const parts = String(ref.ayahRange).split(/[-:–—]/);
      const start = parseInt(parts[0].replace(/\D/g, '')) || 1;
      const end = parts.length > 1 ? (parseInt(parts[1].replace(/\D/g, '')) || start) : start;
      const sn = surahNames[ref.surahNumber] || {
        id: ref.surahName || `Surah ${ref.surahNumber}`,
        en: ref.surahNameEn || `Surah ${ref.surahNumber}`,
        ms: ref.surahNameMs || `Surah ${ref.surahNumber}`
      };

      const v = getVerseData(ref.surahNumber, start, end);
      updatedRefs++;

      return {
        surahNumber: ref.surahNumber,
        surahName: sn.id,
        surahNameEn: sn.en,
        surahNameMs: sn.ms,
        ayahRange: String(ref.ayahRange),
        verseTextAr: v.verseTextAr,
        verseTextTranslation: v.verseTextTranslation,
        verseTextTranslationEn: v.verseTextTranslationEn,
        verseTextTranslationMs: v.verseTextTranslationMs,
        verseTextLatin: v.verseTextLatin,
        verseTextLatinEn: v.verseTextLatinEn
      };
    });
  }
});

fs.writeFileSync(topicsPath, JSON.stringify(topics, null, 2), 'utf8');
console.log('Successfully updated', updatedRefs, 'Quran references across', topics.length, 'topics.');

if (fs.existsSync(dbTempPath)) {
  fs.unlinkSync(dbTempPath);
}

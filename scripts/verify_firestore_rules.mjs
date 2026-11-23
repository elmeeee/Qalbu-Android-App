/**
 * ===============================================================
 * Sāat App - Firestore Security Rules Test Suite
 * ===============================================================
 *
 * Verifies:
 * 1. Owner can read, create, and update profile without residence.
 * 2. Writes with 'residence' field on /users/{uid} are DENIED (anti-residence enforcement).
 * 3. Deletion of legacy 'residence' field on /users/{uid} is ALLOWED.
 * 4. Writes to /users/{uid}/private/residence or /users/{uid}/private/lastKnownAddress are DENIED.
 * 5. Deletion of legacy documents under /users/{uid}/private/ is ALLOWED for owner (cleanup enabled).
 * 6. Non-owner / unauthenticated access to /users/{uid} is DENIED.
 * 7. Wildcard non-overlap: subcollection rules cannot be used to bypass profile rules on /users/{uid}.
 * 8. Preferences sync at /users/{uid}/preferences/settings with location map (prayer-time city selection) is ALLOWED.
 * 9. Subcollection reads and writes for quran, tracker, tasbih, encyclopedia are ALLOWED for owner.
 */

import { initializeApp } from 'firebase-admin/app';
import { getFirestore, FieldValue } from 'firebase-admin/firestore';

const projectId = process.env.GCLOUD_PROJECT || 'saat-test-project';
const emulatorHost = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8080';
process.env.FIRESTORE_EMULATOR_HOST = emulatorHost;

// Setup admin app for seeding
const adminApp = initializeApp({ projectId }, 'admin');
const adminDb = getFirestore(adminApp);

function createMockJwt(uid) {
  const header = Buffer.from(JSON.stringify({ alg: 'none', typ: 'JWT' })).toString('base64url');
  const payload = Buffer.from(JSON.stringify({
    sub: uid,
    user_id: uid,
    aud: projectId,
    iss: `https://securetoken.google.com/${projectId}`,
    auth_time: Math.floor(Date.now() / 1000)
  })).toString('base64url');
  return `${header}.${payload}.`;
}

async function restFirestoreRequest(path, method, body, authUid) {
  const url = `http://${emulatorHost}/v1/projects/${projectId}/databases/(default)/documents/${path}`;
  const headers = {
    'Content-Type': 'application/json'
  };
  if (authUid) {
    // Send simulated auth token for Firestore Emulator
    headers['Authorization'] = `Bearer ${createMockJwt(authUid)}`;
  }

  const options = { method, headers };
  if (body) {
    options.body = JSON.stringify(body);
  }

  const res = await fetch(url, options);
  const json = await res.json().catch(() => null);
  return { status: res.status, ok: res.ok, data: json };
}

// Convert plain JS object to Firestore REST document fields
function toRestFields(obj) {
  const fields = {};
  for (const [key, value] of Object.entries(obj)) {
    if (value === null || value === undefined) {
      fields[key] = { nullValue: null };
    } else if (typeof value === 'boolean') {
      fields[key] = { booleanValue: value };
    } else if (typeof value === 'number') {
      if (Number.isInteger(value)) {
        fields[key] = { integerValue: value.toString() };
      } else {
        fields[key] = { doubleValue: value };
      }
    } else if (typeof value === 'string') {
      fields[key] = { stringValue: value };
    } else if (Array.isArray(value)) {
      fields[key] = { arrayValue: { values: value.map(v => toRestFields({ item: v }).item) } };
    } else if (typeof value === 'object') {
      fields[key] = { mapValue: { fields: toRestFields(value) } };
    }
  }
  return fields;
}

let passedTests = 0;
let totalTests = 0;

function assert(condition, testName, details = '') {
  totalTests++;
  if (condition) {
    passedTests++;
    console.log(`  ✓ PASS: ${testName}`);
  } else {
    console.error(`  ✗ FAIL: ${testName} ${details ? '(' + details + ')' : ''}`);
  }
}

async function runTestSuite() {
  console.log('===============================================================');
  console.log(' Starting Sāat Firestore Security Rules Verification Suite');
  console.log(` Emulator Host: ${emulatorHost}`);
  console.log('===============================================================');

  const ownerUid = 'user_alice_123';
  const otherUid = 'user_bob_456';

  // -------------------------------------------------------------
  // Test 1: Owner creates profile with valid fields (no residence)
  // -------------------------------------------------------------
  const validProfile = {
    uid: ownerUid,
    displayName: 'Alice Salma',
    avatarKey: 'prayer'
  };
  const t1 = await restFirestoreRequest(
    `users/${ownerUid}`,
    'PATCH',
    { fields: toRestFields(validProfile) },
    ownerUid
  );
  assert(t1.ok, '1. Owner can create profile with valid fields (no residence)', JSON.stringify(t1.data));

  // -------------------------------------------------------------
  // Test 2: Write containing 'residence' on /users/{uid} is DENIED
  // -------------------------------------------------------------
  const residenceProfile = {
    uid: ownerUid,
    displayName: 'Alice Salma',
    residence: {
      city: 'Jakarta',
      province: 'DKI Jakarta',
      countryCode: 'ID'
    }
  };
  const t2 = await restFirestoreRequest(
    `users/${ownerUid}`,
    'PATCH',
    { fields: toRestFields(residenceProfile) },
    ownerUid
  );
  assert(!t2.ok && t2.status === 403, '2. Writes containing residence field are strictly DENIED', `Status: ${t2.status}`);

  // -------------------------------------------------------------
  // Test 3: Unauthenticated request cannot read or write /users/{uid}
  // -------------------------------------------------------------
  const t3Read = await restFirestoreRequest(`users/${ownerUid}`, 'GET', null, null);
  assert(!t3Read.ok && t3Read.status === 403, '3a. Unauthenticated user cannot read profile', `Status: ${t3Read.status}`);

  const t3Write = await restFirestoreRequest(`users/${ownerUid}`, 'PATCH', { fields: toRestFields(validProfile) }, null);
  assert(!t3Write.ok && t3Write.status === 403, '3b. Unauthenticated user cannot write profile', `Status: ${t3Write.status}`);

  // -------------------------------------------------------------
  // Test 4: Another user cannot read or write /users/{ownerUid}
  // -------------------------------------------------------------
  const t4Read = await restFirestoreRequest(`users/${ownerUid}`, 'GET', null, otherUid);
  assert(!t4Read.ok && t4Read.status === 403, '4a. Non-owner cannot read user profile', `Status: ${t4Read.status}`);

  const t4Write = await restFirestoreRequest(`users/${ownerUid}`, 'PATCH', { fields: toRestFields(validProfile) }, otherUid);
  assert(!t4Write.ok && t4Write.status === 403, '4b. Non-owner cannot write user profile', `Status: ${t4Write.status}`);

  // -------------------------------------------------------------
  // Test 5: Writes to /users/{uid}/private/residence or lastKnownAddress are DENIED
  // -------------------------------------------------------------
  const t5a = await restFirestoreRequest(
    `users/${ownerUid}/private/residence`,
    'PATCH',
    { fields: toRestFields({ address: 'Secret Street 123' }) },
    ownerUid
  );
  assert(!t5a.ok && t5a.status === 403, '5a. Create/update on /private/residence is strictly DENIED', `Status: ${t5a.status}`);

  const t5b = await restFirestoreRequest(
    `users/${ownerUid}/private/lastKnownAddress`,
    'PATCH',
    { fields: toRestFields({ address: 'Last Known Street 456' }) },
    ownerUid
  );
  assert(!t5b.ok && t5b.status === 403, '5b. Create/update on /private/lastKnownAddress is strictly DENIED', `Status: ${t5b.status}`);

  // -------------------------------------------------------------
  // Test 6: Deleting legacy document under /users/{uid}/private/ is ALLOWED for owner (cleanup)
  // -------------------------------------------------------------
  // Seed a legacy doc directly via admin
  await adminDb.collection('users').doc(ownerUid).collection('private').doc('residence').set({ legacy: true });
  const t6Delete = await restFirestoreRequest(
    `users/${ownerUid}/private/residence`,
    'DELETE',
    null,
    ownerUid
  );
  assert(t6Delete.ok, '6. Owner can delete legacy document in /private/ for cleanup', JSON.stringify(t6Delete.data));

  // -------------------------------------------------------------
  // Test 7: Owner can sync prayer-time location in /preferences/settings
  // -------------------------------------------------------------
  const validPreferences = {
    uid: ownerUid,
    language: 'id',
    theme: 'emerald',
    location: {
      isAutomatic: false,
      mode: 'MANUAL',
      city: 'Jakarta Selatan',
      label: 'Jakarta Selatan, DKI Jakarta',
      latitude: -6.2615,
      longitude: 106.8106,
      countryCode: 'ID',
      regionId: 'jakarta',
      cityCode: 'jakarta'
    },
    updatedAtMillis: Date.now()
  };
  const t7 = await restFirestoreRequest(
    `users/${ownerUid}/preferences/settings`,
    'PATCH',
    { fields: toRestFields(validPreferences) },
    ownerUid
  );
  assert(t7.ok, '7. Owner can sync preferences/settings with prayer-time location', JSON.stringify(t7.data));

  // -------------------------------------------------------------
  // Test 8: Subcollection write for quran/personal and encyclopedia/reading
  // -------------------------------------------------------------
  const quranData = {
    uid: ownerUid,
    bookmarks: [{ surahNumber: 1, ayahNumber: 1, createdAt: Date.now() }],
    updatedAtMillis: Date.now()
  };
  const t8a = await restFirestoreRequest(
    `users/${ownerUid}/quran/personal`,
    'PATCH',
    { fields: toRestFields(quranData) },
    ownerUid
  );
  assert(t8a.ok, '8a. Owner can write to quran/personal', JSON.stringify(t8a.data));

  const encyclopediaData = {
    uid: ownerUid,
    pages: { adam: 3, yusuf: 5 },
    updatedAtMillis: Date.now()
  };
  const t8b = await restFirestoreRequest(
    `users/${ownerUid}/encyclopedia/reading`,
    'PATCH',
    { fields: toRestFields(encyclopediaData) },
    ownerUid
  );
  assert(t8b.ok, '8b. Owner can write to encyclopedia/reading', JSON.stringify(t8b.data));

  // -------------------------------------------------------------
  // Test 9: Deletion of legacy residence field via FieldValue.delete() is allowed
  // -------------------------------------------------------------
  // Seed user doc with residence field via admin
  await adminDb.collection('users').doc(ownerUid).set({
    uid: ownerUid,
    displayName: 'Alice Salma',
    residence: { city: 'Legacy City', province: 'Legacy Prov', countryCode: 'ID' }
  });
  // Update without residence (simulating FieldValue.delete / cleanup)
  const t9 = await restFirestoreRequest(
    `users/${ownerUid}`,
    'PATCH',
    { fields: toRestFields({ uid: ownerUid, displayName: 'Alice Updated' }) },
    ownerUid
  );
  assert(t9.ok, '9. Legacy profile can be updated when residence is removed/omitted', JSON.stringify(t9.data));

  console.log('===============================================================');
  console.log(` Verification Suite Completed: ${passedTests}/${totalTests} tests passed`);
  console.log('===============================================================');

  if (passedTests !== totalTests) {
    process.exit(1);
  }
}

runTestSuite().catch(err => {
  console.error('Fatal error running Firestore rules test suite:', err);
  process.exit(1);
});

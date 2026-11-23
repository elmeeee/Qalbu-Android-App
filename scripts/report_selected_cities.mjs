#!/usr/bin/env node

/**
 * ===============================================================
 * Sāat App - Aggregate Prayer-Time City Report
 * ===============================================================
 *
 * Responsibilities:
 * 1. Counts user accounts by active manual prayer-time city.
 * 2. Labeled strictly as: "Selected prayer-time city".
 * 3. Each account is counted exactly once.
 * 4. Grouped by stable region ID where available (e.g. "jakarta", "surabaya", "kuala_lumpur").
 * 5. Automatic mode or missing valid manual city is counted as "Unknown".
 * 6. Returns aggregate counts only (no PII, no user IDs, no exact coordinates).
 */

import { initializeApp, cert, getApps } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { readFileSync, existsSync } from 'fs';

// Initialize Firebase Admin
if (getApps().length === 0) {
  if (process.env.FIRESTORE_EMULATOR_HOST) {
    initializeApp({ projectId: process.env.GCLOUD_PROJECT || 'saat-app-prod' });
  } else if (process.env.GOOGLE_APPLICATION_CREDENTIALS && existsSync(process.env.GOOGLE_APPLICATION_CREDENTIALS)) {
    const serviceAccount = JSON.parse(readFileSync(process.env.GOOGLE_APPLICATION_CREDENTIALS, 'utf8'));
    initializeApp({ credential: cert(serviceAccount) });
  } else {
    initializeApp({ projectId: process.env.GCLOUD_PROJECT || 'saat-app-prod' });
  }
}

const db = getFirestore();

async function generateReport() {
  console.log('===============================================================');
  console.log(' Sāat App - Selected Prayer-Time City (Aggregate Report)');
  console.log('===============================================================');
  console.log(`Generated At : ${new Date().toISOString()}`);
  console.log(`Target       : ${process.env.FIRESTORE_EMULATOR_HOST ? 'Emulator: ' + process.env.FIRESTORE_EMULATOR_HOST : 'Cloud Firestore'}`);
  console.log('');

  const usersSnapshot = await db.collection('users').get();
  const totalUsers = usersSnapshot.size;

  let totalEvaluated = 0;
  let automaticModeCount = 0;
  let unknownCount = 0;
  let manualCityAccounts = 0;

  // Map of regionId / cityLabel -> count
  const cityCounts = new Map();

  for (const userDoc of usersSnapshot.docs) {
    totalEvaluated++;
    const uid = userDoc.id;

    // Check /users/{uid}/preferences/settings
    const prefDoc = await db.collection('users').doc(uid).collection('preferences').doc('settings').get();

    if (!prefDoc.exists) {
      unknownCount++;
      continue;
    }

    const prefData = prefDoc.data();
    const loc = prefData?.location;

    if (!loc) {
      unknownCount++;
      continue;
    }

    const isAutomatic = loc.isAutomatic === true || loc.mode === 'GPS';

    if (isAutomatic) {
      automaticModeCount++;
      unknownCount++; // Per requirement: "Treat automatic mode or a missing valid manual city as 'Unknown'"
      continue;
    }

    // Manual mode
    const manualCity = loc.city || loc.label;
    const regionId = loc.regionId || loc.cityCode;

    if (!manualCity || typeof manualCity !== 'string' || manualCity.trim().length === 0) {
      unknownCount++;
      continue;
    }

    // Group by stable region ID where available, otherwise clean city label
    const groupKey = regionId && typeof regionId === 'string' && regionId.trim().length > 0
      ? regionId.trim().toLowerCase()
      : manualCity.trim().toLowerCase().replace(/\s+/g, '_');

    const displayLabel = manualCity.trim();
    const currentEntry = cityCounts.get(groupKey) || { count: 0, label: displayLabel };
    currentEntry.count += 1;
    cityCounts.set(groupKey, currentEntry);
    manualCityAccounts++;
  }

  // Sort by count descending
  const sortedCities = Array.from(cityCounts.entries()).sort((a, b) => b[1].count - a[1].count);

  console.log('---------------------------------------------------------------');
  console.log(' Selected prayer-time city');
  console.log('---------------------------------------------------------------');
  console.log(
    'Region ID'.padEnd(24) +
    'City / Regency Label'.padEnd(30) +
    'Account Count'.padStart(14)
  );
  console.log('-'.repeat(68));

  for (const [regionId, { count, label }] of sortedCities) {
    console.log(
      regionId.padEnd(24) +
      label.padEnd(30) +
      count.toString().padStart(14)
    );
  }

  if (unknownCount > 0) {
    console.log(
      'Unknown'.padEnd(24) +
      'Automatic mode / Unspecified'.padEnd(30) +
      unknownCount.toString().padStart(14)
    );
  }

  console.log('-'.repeat(68));
  console.log(
    'TOTAL ACCOUNTS'.padEnd(54) +
    totalEvaluated.toString().padStart(14)
  );
  console.log('---------------------------------------------------------------');
  console.log('');
  console.log('Report Breakdown:');
  console.log(`- Accounts in Automatic Mode (GPS): ${automaticModeCount}`);
  console.log(`- Accounts with Active Manual City: ${manualCityAccounts}`);
  console.log(`- Accounts Unknown / Unset        : ${unknownCount}`);
  console.log('');
  console.log('Privacy Compliance Notice:');
  console.log('✓ Each account counted exactly once.');
  console.log('✓ Grouped by stable region ID where available.');
  console.log('✓ Aggregate statistics only; zero PII or user IDs emitted.');
  console.log('===============================================================');

  return {
    totalEvaluated,
    automaticModeCount,
    manualCityAccounts,
    unknownCount,
    cities: sortedCities.map(([regionId, { count, label }]) => ({ regionId, label, count }))
  };
}

generateReport().catch(err => {
  console.error('Fatal error during aggregate city report generation:', err);
  process.exit(1);
});

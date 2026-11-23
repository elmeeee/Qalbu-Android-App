#!/usr/bin/env node

/**
 * ===============================================================
 * Sāat App - Address Data Cleanup Script (Idempotent Backend Job)
 * ===============================================================
 *
 * Responsibilities:
 * 1. Safely retire and delete legacy address data:
 *    - /users/{uid}.residence (field deleted via FieldValue.delete())
 *    - /users/{uid}/private/residence (document deleted)
 *    - /users/{uid}/private/lastKnownAddress (document deleted)
 * 2. Strict Privacy: Never log individual addresses, coordinates, or PII.
 * 3. Dry-Run by Default: Does not delete unless explicitly run with `--execute`.
 * 4. Data Preservation: Preserves user profiles, prayer-time city selections
 *    (/users/{uid}/preferences/settings.location), and all other preferences.
 * 5. Idempotent: Can be run multiple times safely.
 */

import { initializeApp, cert, getApps } from 'firebase-admin/app';
import { getFirestore, FieldValue } from 'firebase-admin/firestore';
import { readFileSync, existsSync } from 'fs';

const args = process.argv.slice(2);
const isExecute = args.includes('--execute');
const isDryRun = !isExecute || args.includes('--dry-run');

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

async function runCleanup() {
  console.log('===============================================================');
  console.log(` Sāat App - Address Data Cleanup (${isDryRun ? 'DRY-RUN MODE' : 'EXECUTE MODE'})`);
  console.log('===============================================================');
  console.log(`Timestamp: ${new Date().toISOString()}`);
  console.log(`Target: Firestore (${process.env.FIRESTORE_EMULATOR_HOST ? 'Emulator: ' + process.env.FIRESTORE_EMULATOR_HOST : 'Cloud Firestore'})`);
  console.log('');

  let scannedAccounts = 0;
  let accountsWithResidenceField = 0;
  let privateResidenceDocs = 0;
  let privateLastKnownAddressDocs = 0;
  let errorsEncountered = 0;

  const usersSnapshot = await db.collection('users').get();
  scannedAccounts = usersSnapshot.size;

  for (const userDoc of usersSnapshot.docs) {
    const uid = userDoc.id;
    const userData = userDoc.data();

    // 1. Check legacy /users/{uid}.residence
    if (userData && 'residence' in userData) {
      accountsWithResidenceField++;
      if (!isDryRun) {
        try {
          await userDoc.ref.update({
            residence: FieldValue.delete(),
            updatedAt: FieldValue.serverTimestamp()
          });
        } catch (err) {
          errorsEncountered++;
          console.error(`[Error] Failed to delete residence field for an account (UID redacted): ${err.message}`);
        }
      }
    }

    // 2. Check legacy /users/{uid}/private/residence
    const privateResidenceRef = db.collection('users').doc(uid).collection('private').doc('residence');
    const privateResidenceDoc = await privateResidenceRef.get();
    if (privateResidenceDoc.exists) {
      privateResidenceDocs++;
      if (!isDryRun) {
        try {
          await privateResidenceRef.delete();
        } catch (err) {
          errorsEncountered++;
          console.error(`[Error] Failed to delete private/residence for an account (UID redacted): ${err.message}`);
        }
      }
    }

    // 3. Check legacy /users/{uid}/private/lastKnownAddress
    const privateLastAddressRef = db.collection('users').doc(uid).collection('private').doc('lastKnownAddress');
    const privateLastAddressDoc = await privateLastAddressRef.get();
    if (privateLastAddressDoc.exists) {
      privateLastKnownAddressDocs++;
      if (!isDryRun) {
        try {
          await privateLastAddressRef.delete();
        } catch (err) {
          errorsEncountered++;
          console.error(`[Error] Failed to delete private/lastKnownAddress for an account (UID redacted): ${err.message}`);
        }
      }
    }
  }

  console.log('---------------------------------------------------------------');
  console.log(' Cleanup Summary');
  console.log('---------------------------------------------------------------');
  console.log(`Execution Mode                        : ${isDryRun ? 'DRY-RUN (No changes applied)' : 'LIVE EXECUTION (Deletions applied)'}`);
  console.log(`Total User Accounts Scanned           : ${scannedAccounts}`);
  console.log(`Accounts with /users/{uid}.residence  : ${accountsWithResidenceField}`);
  console.log(`Docs at /users/{uid}/private/residence: ${privateResidenceDocs}`);
  console.log(`Docs at .../private/lastKnownAddress  : ${privateLastKnownAddressDocs}`);
  console.log(`Total Retired Address Items Found     : ${accountsWithResidenceField + privateResidenceDocs + privateLastKnownAddressDocs}`);
  console.log(`Errors Encountered                    : ${errorsEncountered}`);
  console.log('');
  console.log('Privacy Compliance Notice:');
  console.log('✓ Zero address strings, coordinates, or PII were displayed or logged.');
  console.log('✓ Manual prayer-time city selections (/preferences/settings.location) were strictly preserved.');
  console.log('✓ User root profile documents and other subcollections remain intact.');
  console.log('===============================================================');

  if (isDryRun && (accountsWithResidenceField + privateResidenceDocs + privateLastKnownAddressDocs > 0)) {
    console.log('To apply these deletions in Firestore, run with:');
    console.log('  node scripts/cleanup_address_data.mjs --execute');
  }
}

runCleanup().catch(err => {
  console.error('Fatal error during address cleanup script:', err);
  process.exit(1);
});

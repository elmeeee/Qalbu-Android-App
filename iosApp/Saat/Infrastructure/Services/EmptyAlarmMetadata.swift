//
//  EmptyAlarmMetadata.swift
//  Saat
//
//  Created by Elmee on 12/07/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import AlarmKit

/// Minimal metadata conformance required by AlarmKit.
/// We don't need custom metadata for prayer alarms — the alarm presentation
/// carries all the information the user sees.
struct EmptyAlarmMetadata: AlarmMetadata {}

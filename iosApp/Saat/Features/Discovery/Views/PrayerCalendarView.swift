//
//  PrayerCalendarView.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI
import CoreLocation
import Combine

struct PrayerCalendarView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var prayerController: PrayerTimesController
    
    @State private var currentYear = Calendar.current.component(.year, from: Date())
    @State private var currentMonth = Calendar.current.component(.month, from: Date())
    @State private var selectedDate: Date = Calendar.current.startOfDay(for: Date())
    
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var calendarTimings: [Int: [String: String]] = [:] // Day -> Timings
    
    private let calendar = Calendar.current
    
    private var monthName: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "LLLL"
        return formatter.monthSymbols[currentMonth - 1]
    }
    
    private var shiftedWeekdaySymbols: [String] {
        let symbols = calendar.veryShortWeekdaySymbols
        let first = calendar.firstWeekday - 1
        return Array(symbols[first...] + symbols[..<first])
    }
    
    private var daysInMonthGrid: [Date?] {
        var comps = DateComponents()
        comps.year = currentYear
        comps.month = currentMonth
        comps.day = 1
        guard let firstOfMonth = calendar.date(from: comps) else { return [] }
        
        let firstWeekday = calendar.component(.weekday, from: firstOfMonth)
        let leadingEmptyCells = (firstWeekday - calendar.firstWeekday + 7) % 7
        
        guard let range = calendar.range(of: .day, in: .month, for: firstOfMonth) else { return [] }
        let numDays = range.count
        
        var grid: [Date?] = Array(repeating: nil, count: leadingEmptyCells)
        for day in 1...numDays {
            var dayComps = DateComponents()
            dayComps.year = currentYear
            dayComps.month = currentMonth
            dayComps.day = day
            if let date = calendar.date(from: dayComps) {
                grid.append(date)
            }
        }
        return grid
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 8) {
                Button(action: { dismiss() }) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(SaatTokens.Colors.slate800)
                }
                .accessibilityLabel("Back")
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(AppLanguageManager.shared.localize("prayer_calendar_title"))
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                    
                    if let city = prayerController.cityName {
                        Text(city)
                            .font(.system(size: 13))
                            .foregroundColor(SaatTokens.Colors.slate500)
                    }
                }
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)
            .background(SaatTokens.Colors.screenBackground)
            
            ScrollView {
                VStack(spacing: 16) {
                    // Month shifting controls
                    HStack {
                        Button(action: { shiftMonth(by: -1) }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 20))
                                .foregroundColor(SaatTokens.Colors.slate800)
                                .padding(8)
                        }
                        
                        Spacer()
                        
                        Text("\(monthName) \(String(currentYear))")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(SaatTokens.Colors.slate800)
                        
                        Spacer()
                        
                        Button(action: { shiftMonth(by: 1) }) {
                            Image(systemName: "chevron.right")
                                .font(.system(size: 20))
                                .foregroundColor(SaatTokens.Colors.slate800)
                                .padding(8)
                        }
                    }
                    .padding(.horizontal, 16)
                    
                    Text(AppLanguageManager.shared.localize("prayer_calendar_hint"))
                        .font(.system(size: 13))
                        .foregroundColor(SaatTokens.Colors.slate500)
                        .padding(.horizontal, 24)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Calendar grid
                    VStack(spacing: 4) {
                        // Weekday Headers
                        HStack(spacing: 0) {
                            ForEach(0..<shiftedWeekdaySymbols.count, id: \.self) { index in
                                Text(shiftedWeekdaySymbols[index])
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(SaatTokens.Colors.slate500)
                                    .frame(maxWidth: .infinity)
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 4)
                        
                        // Days grid
                        let grid = daysInMonthGrid
                        let columns = Array(repeating: GridItem(.flexible(), spacing: 4), count: 7)
                        
                        LazyVGrid(columns: columns, spacing: 4) {
                            ForEach(0..<grid.count, id: \.self) { index in
                                if let date = grid[index] {
                                    let dayNum = calendar.component(.day, from: date)
                                    let isSelected = calendar.isDate(date, inSameDayAs: selectedDate)
                                    let isToday = calendar.isDateInToday(date)
                                    let khgtInfo = LocalKhgtCalendar.shared.infoForDate(date)
                                    let isImportant = khgtInfo?.isImportantDay == true
                                    
                                    Button(action: { selectedDate = date }) {
                                        VStack(spacing: 2) {
                                            Text("\(dayNum)")
                                                .font(.system(size: 15, weight: (isSelected || isToday) ? .bold : .regular))
                                                .foregroundColor(isSelected ? .white : (isToday ? SaatTokens.Colors.deepEmerald : SaatTokens.Colors.slate800))
                                            
                                            if isImportant {
                                                Circle()
                                                    .fill(isSelected ? Color.white : SaatTokens.Colors.goldDeep)
                                                    .frame(width: 4, height: 4)
                                            }
                                        }
                                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                                        .aspectRatio(1, contentMode: .fill)
                                        .background(isSelected ? SaatTokens.Colors.deepEmerald : (isToday ? SaatTokens.Colors.deepEmerald.opacity(0.12) : SaatTokens.Colors.pureWhite))
                                        .clipShape(Circle())
                                        .overlay(
                                            Group {
                                                if isToday && !isSelected {
                                                    Circle().stroke(SaatTokens.Colors.deepEmerald.opacity(0.35), lineWidth: 1)
                                                }
                                            }
                                        )
                                        .padding(3)
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                } else {
                                    Color.clear
                                        .aspectRatio(1, contentMode: .fill)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    
                    if isLoading {
                        ProgressView()
                            .padding(.vertical, 30)
                            .tint(SaatTokens.Colors.deepEmerald)
                    } else if let error = errorMessage {
                        VStack(spacing: 12) {
                            Text(error)
                                .font(.system(size: 13))
                                .foregroundColor(.red)
                                .multilineTextAlignment(.center)
                            
                            Button(action: { Task { await fetchMonthData() } }) {
                                Text("Retry")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(SaatTokens.Colors.deepEmerald)
                                    .cornerRadius(8)
                            }
                        }
                        .padding(.vertical, 20)
                    } else {
                        // Selected Day Info & Timings
                        VStack(alignment: .leading, spacing: 14) {
                            let khgt = LocalKhgtCalendar.shared.infoForDate(selectedDate)
                            
                            VStack(alignment: .leading, spacing: 2) {
                                Text(selectedDateLabel())
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(SaatTokens.Colors.slate900)
                                
                                if let hijri = khgt?.hijriLabel {
                                    Text(hijri)
                                        .font(.system(size: 12))
                                        .foregroundColor(SaatTokens.Colors.slate500)
                                }
                            }
                            
                            // Important Hijri Event Banner
                            if let event = khgt?.eventTitle {
                                HStack(spacing: 10) {
                                    Image(systemName: "star.fill")
                                        .foregroundColor(SaatTokens.Colors.goldDeep)
                                        .font(.system(size: 14))
                                    
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(event)
                                            .font(.system(size: 13, weight: .semibold))
                                            .foregroundColor(SaatTokens.Colors.slate800)
                                        Text(khgt?.hijriLabel ?? "")
                                            .font(.system(size: 11))
                                            .foregroundColor(SaatTokens.Colors.slate500)
                                    }
                                }
                                .padding(12)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(SaatTokens.Colors.goldDeep.opacity(0.1))
                                .cornerRadius(10)
                            }
                            
                            // Prayer Timings Grid
                            let list = prayerTimingsList()
                            let gridRows = [
                                [list[0], list[1], list[2]], // Fajr, Sunrise, Dhuhr
                                [list[3], list[4], list[5]]  // Asr, Maghrib, Isha
                            ]
                            
                            VStack(spacing: 8) {
                                ForEach(0..<gridRows.count, id: \.self) { rowIndex in
                                    HStack(spacing: 8) {
                                        ForEach(0..<gridRows[rowIndex].count, id: \.self) { colIndex in
                                            let item = gridRows[rowIndex][colIndex]
                                            VStack(alignment: .leading, spacing: 4) {
                                                Text(item.0)
                                                    .font(.system(size: 11))
                                                    .foregroundColor(SaatTokens.Colors.slate500)
                                                    .lineLimit(1)
                                                
                                                Text(item.1)
                                                    .font(.system(size: 14, weight: .semibold))
                                                    .foregroundColor(SaatTokens.Colors.slate900)
                                            }
                                            .padding(.horizontal, 10)
                                            .padding(.vertical, 10)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                            .background(SaatTokens.Colors.pureWhite)
                                            .cornerRadius(12)
                                        }
                                    }
                                }
                            }
                        }
                        .padding(16)
                        .background(SaatTokens.Colors.lightGrey)
                        .cornerRadius(20)
                        .padding(.horizontal, 16)
                    }
                }
                .padding(.bottom, 30)
            .background(SaatTokens.Colors.screenBackground)
        }
        .background(SaatTokens.Colors.screenBackground)
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .task {
            await fetchMonthData()
        }
    }
    
    private func shiftMonth(by delta: Int) {
        if let newDate = calendar.date(byAdding: .month, value: delta, to: calendar.date(from: DateComponents(year: currentYear, month: currentMonth, day: 1))!) {
            currentYear = calendar.component(.year, from: newDate)
            currentMonth = calendar.component(.month, from: newDate)
            
            // Adjust selected day
            let today = Date()
            if calendar.component(.year, from: today) == currentYear && calendar.component(.month, from: today) == currentMonth {
                selectedDate = calendar.startOfDay(for: today)
            } else {
                selectedDate = calendar.date(from: DateComponents(year: currentYear, month: currentMonth, day: 1))!
            }
            
            Task {
                await fetchMonthData()
            }
        }
    }
    
    private func selectedDateLabel() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, d MMMM yyyy"
        return formatter.string(from: selectedDate)
    }
    
    private func prayerTimingsList() -> [(String, String)] {
        let day = calendar.component(.day, from: selectedDate)
        guard let timings = calendarTimings[day] else {
            return [
                ("Fajr", "--:--"),
                ("Sunrise", "--:--"),
                ("Dhuhr", "--:--"),
                ("Asr", "--:--"),
                ("Maghrib", "--:--"),
                ("Isha", "--:--")
            ]
        }
        
        let keys = ["Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"]
        return keys.map { key in
            let rawTime = timings[key] ?? "--:--"
            let clean = rawTime.components(separatedBy: " ").first ?? rawTime
            return (key, clean)
        }
    }
    
    private func fetchMonthData() async {
        isLoading = true
        errorMessage = nil
        calendarTimings = [:]
        
        // Resolve location
        var lat = -6.2088 // Jakarta default
        var lon = 106.8456
        
        if let location = prayerController.lastKnownLocation {
            lat = location.coordinate.latitude
            lon = location.coordinate.longitude
        }
        
        let method = prayerController.calculationMethod
        
        let calendar = Calendar.current
        let comps = DateComponents(year: currentYear, month: currentMonth)
        guard let monthDate = calendar.date(from: comps),
              let range = calendar.range(of: .day, in: .month, for: monthDate) else {
            errorMessage = "Invalid month/year specifications."
            isLoading = false
            return
        }

        let tzOffset = Double(TimeZone.current.secondsFromGMT(for: Date())) / 3600.0
        let timeFormatter = DateFormatter()
        timeFormatter.dateFormat = "HH:mm"
        timeFormatter.timeZone = .current

        var mapped: [Int: [String: String]] = [:]
        for day in range {
            var dayComps = comps
            dayComps.day = day
            guard let dayDate = calendar.date(from: dayComps) else { continue }
            
            let dayTimings = LocalPrayerTimesCalculator.calculate(
                date: dayDate,
                latitude: lat,
                longitude: lon,
                timezoneOffset: tzOffset,
                method: method
            )
            
            var dayTimingStrings: [String: String] = [:]
            for (key, dateValue) in dayTimings {
                dayTimingStrings[key] = timeFormatter.string(from: dateValue)
            }
            mapped[day] = dayTimingStrings
        }

        calendarTimings = mapped
        errorMessage = nil
        isLoading = false
    }
}

// MARK: - API Structs
private struct AlAdhanCalendarResponse: Codable {
    let code: Int
    let status: String
    let data: [AlAdhanDayData]
}

private struct AlAdhanDayData: Codable {
    let timings: [String: String]
    let date: AlAdhanDateInfo
}

private struct AlAdhanDateInfo: Codable {
    let readable: String
    let timestamp: String
    let gregorian: AlAdhanGregorianDate
}

private struct AlAdhanGregorianDate: Codable {
    let day: String
    let month: AlAdhanMonthInfo
    let year: String
}

private struct AlAdhanMonthInfo: Codable {
    let number: Int
    let en: String
}

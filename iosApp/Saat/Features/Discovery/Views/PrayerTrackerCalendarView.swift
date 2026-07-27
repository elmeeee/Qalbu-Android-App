//
//  PrayerTrackerCalendarView.swift
//  Saat
//
//  Created by Elmee on 25/06/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

struct PrayerTrackerCalendarView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.appContainer) private var container
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    @State private var currentYear = Calendar.current.component(.year, from: Date())
    @State private var currentMonth = Calendar.current.component(.month, from: Date())
    
    @State private var streak = 0
    @State private var bestStreak = 0
    @State private var daysProgress: [PrayerDayProgress] = []
    
    private let calendar = Calendar.current
    
    private var monthName: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "LLLL"
        formatter.locale = Locale(identifier: languageManager.currentLanguage.rawValue)
        return formatter.monthSymbols[currentMonth - 1]
    }
    
    private var shiftedWeekdaySymbols: [String] {
        var localCalendar = Calendar.current
        localCalendar.locale = Locale(identifier: languageManager.currentLanguage.rawValue)
        let symbols = localCalendar.shortWeekdaySymbols
        let first = localCalendar.firstWeekday - 1
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
                    Text(languageManager.localize("prayer_tracker_history"))
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(SaatTokens.Colors.deepEmerald)
                    
                    Text(languageManager.localize("prayer_tracker_history_sub"))
                        .font(.system(size: 13))
                        .foregroundColor(SaatTokens.Colors.slate500)
                }
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)
            .background(SaatTokens.Colors.screenBackground)
            
            ScrollView {
                VStack(spacing: 16) {
                    // Challenge stats card
                    HStack {
                        Spacer()
                        
                        VStack(spacing: 4) {
                            HStack(spacing: 4) {
                                Image(systemName: "flame.fill")
                                    .foregroundColor(SaatTokens.Colors.goldDeep)
                                    .font(.system(size: 18))
                                
                                Text("\(streak)")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(SaatTokens.Colors.deepEmerald)
                            }
                            
                            Text(languageManager.localize("current_streak"))
                                .font(.system(size: 11))
                                .foregroundColor(SaatTokens.Colors.slate500)
                        }
                        
                        Spacer()
                        
                        VStack(spacing: 4) {
                            HStack(spacing: 4) {
                                Text("\(bestStreak)")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(SaatTokens.Colors.deepEmerald)
                            }
                            
                            Text(languageManager.localize("best_streak"))
                                .font(.system(size: 11))
                                .foregroundColor(SaatTokens.Colors.slate500)
                        }
                        
                        Spacer()
                    }
                    .padding(16)
                    .background(SaatTokens.Colors.pureWhite)
                    .cornerRadius(20)
                    .shadow(color: Color.black.opacity(0.02), radius: 6, x: 0, y: 2)
                    .padding(.horizontal, 20)
                    .padding(.top, 12)
                    
                    // Month shifting controls
                    HStack {
                        Button(action: { shiftMonth(by: -1) }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 20))
                                .foregroundColor(SaatTokens.Colors.slate800)
                        }
                        
                        Spacer()
                        
                        Text("\(monthName) \(String(currentYear))")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(SaatTokens.Colors.deepEmerald)
                        
                        Spacer()
                        
                        Button(action: { shiftMonth(by: 1) }) {
                            Image(systemName: "chevron.right")
                                .font(.system(size: 20))
                                .foregroundColor(SaatTokens.Colors.slate800)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    
                    // Grid
                    VStack(spacing: 6) {
                        // Weekday Headers
                        HStack(spacing: 0) {
                            ForEach(0..<shiftedWeekdaySymbols.count, id: \.self) { index in
                                Text(shiftedWeekdaySymbols[index])
                                    .font(.system(size: 11, weight: .medium))
                                    .foregroundColor(SaatTokens.Colors.slate500)
                                    .frame(maxWidth: .infinity)
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 8)
                        
                        // Days grid
                        let grid = daysInMonthGrid
                        let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)
                        
                        LazyVGrid(columns: columns, spacing: 6) {
                            ForEach(0..<grid.count, id: \.self) { index in
                                if let date = grid[index] {
                                    let dayNum = calendar.component(.day, from: date)
                                    let isToday = calendar.isDateInToday(date)
                                    
                                    // Fetch progress
                                    let progress = dayProgress(for: date)
                                    let complete = progress.isPerfectDay
                                    let partial = progress.completedCount > 0
                                    
                                    VStack {
                                        Text("\(dayNum)")
                                            .font(.system(size: 14, weight: isToday ? .bold : .medium))
                                            .foregroundColor(complete ? .white : (partial ? SaatTokens.Colors.deepEmerald : SaatTokens.Colors.slate500))
                                        
                                        if complete {
                                            Image(systemName: "checkmark")
                                                .font(.system(size: 9, weight: .bold))
                                                .foregroundColor(.white)
                                        } else if partial {
                                            Text("\(progress.completedCount)/\(progress.totalCount)")
                                                .font(.system(size: 9, weight: .bold))
                                                .foregroundColor(SaatTokens.Colors.deepEmerald)
                                        } else {
                                            Spacer().frame(height: 11) // space for alignment
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .aspectRatio(1.0, contentMode: .fit)
                                    .background(
                                        Group {
                                            if complete {
                                                LinearGradient(
                                                    colors: [SaatTokens.Colors.deepEmerald, SaatTokens.Colors.teal],
                                                    startPoint: .topLeading,
                                                    endPoint: .bottomTrailing
                                                )
                                            } else if partial {
                                                LinearGradient(
                                                    colors: [
                                                        SaatTokens.Colors.teal.opacity(0.25),
                                                        SaatTokens.Colors.teal.opacity(0.12)
                                                    ],
                                                    startPoint: .topLeading,
                                                    endPoint: .bottomTrailing
                                                )
                                            } else {
                                                LinearGradient(
                                                    colors: [SaatTokens.Colors.lightGrey, SaatTokens.Colors.lightGrey],
                                                    startPoint: .topLeading,
                                                    endPoint: .bottomTrailing
                                                )
                                            }
                                        }
                                    )
                                    .cornerRadius(10)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 10)
                                            .stroke(isToday ? SaatTokens.Colors.goldBright : Color.clear, lineWidth: 2)
                                    )
                                } else {
                                    Color.clear
                                        .aspectRatio(1.0, contentMode: .fit)
                                }
                            }
                        }
                        .padding(.horizontal, 20)
                    }
                }
                .padding(.bottom, 30)
            }
            .background(SaatTokens.Colors.screenBackground)
        }
        .background(SaatTokens.Colors.screenBackground)
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            reloadStoreData()
        }
    }
    
    private func reloadStoreData() {
        let store = PrayerTrackerStore(appGroupIdentifier: container?.configuration.appGroupIdentifier)
        streak = store.currentStreak()
        bestStreak = store.bestStreak()
        daysProgress = store.monthProgress(year: currentYear, month: currentMonth)
    }
    
    private func shiftMonth(by delta: Int) {
        if let newDate = calendar.date(byAdding: .month, value: delta, to: calendar.date(from: DateComponents(year: currentYear, month: currentMonth, day: 1))!) {
            currentYear = calendar.component(.year, from: newDate)
            currentMonth = calendar.component(.month, from: newDate)
            reloadStoreData()
        }
    }
    
    private func dayProgress(for date: Date) -> PrayerDayProgress {
        let store = PrayerTrackerStore(appGroupIdentifier: container?.configuration.appGroupIdentifier)
        let key = store.dayKeyFor(date: date)
        return store.dayProgress(dayKey: key)
    }
}

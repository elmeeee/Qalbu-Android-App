//
//  TodayImportantDayBanner.swift
//  Saat
//

import SwiftUI

struct TodayImportantDayBanner: View {
    let info: KhgtTodayInfo
    @State private var showDetailSheet = false

    var body: some View {
        guard let event = info.eventTitle else { return AnyView(EmptyView()) }

        return AnyView(
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(Color.Token.goldDeep.opacity(0.12))
                        .frame(width: 42, height: 42)
                    
                    Image(systemName: "calendar")
                        .font(.system(size: 20))
                        .foregroundColor(Color.Token.goldDeep)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(String(localized: "khgt_important_day", defaultValue: "IMPORTANT DAY").uppercased())
                        .font(.system(size: 11, weight: .bold))
                        .tracking(1)
                        .foregroundColor(Color.Token.goldDeep)
                    
                    Text(localizedEventName(event))
                        .font(.system(size: 16, weight: .heavy))
                        .foregroundColor(Color.Token.slate900)
                }
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                LinearGradient(
                    colors: [
                        Color.Token.prayerCreamWarm.opacity(0.45),
                        Color.Token.prayerCream.opacity(0.25)
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.Token.goldDeep.opacity(0.2), lineWidth: 1)
            )
            .contentShape(Rectangle())
            .onTapGesture {
                showDetailSheet = true
            }
            .sheet(isPresented: $showDetailSheet) {
                ImportantDayDetailSheet(event: event)
                    .presentationDetents([.medium, .large])
            }
        )
    }
    
    private func localizedEventName(_ rawEvent: String) -> String {
        // A simple stub before KMP migration
        return rawEvent
    }
}

private struct ImportantDayDetailSheet: View {
    let event: String
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("ABOUT THIS DAY")
                        .font(.system(size: 11, weight: .bold))
                        .tracking(1.2)
                        .foregroundColor(Color.Token.goldDeep)
                    
                    Text(event)
                        .font(.system(size: 24, weight: .heavy))
                        .foregroundColor(Color.Token.slate900)
                }
                .padding(.bottom, 8)
                
                // About section
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 8) {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(Color.Token.teal)
                            .font(.system(size: 20))
                        Text("Significance")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color.Token.slate800)
                    }
                    Text("This is an important day in the Islamic calendar. Detailed information will be loaded from the shared module.")
                        .font(.system(size: 14))
                        .foregroundColor(Color.Token.slate700)
                }
                .padding(14)
                .background(Color.Token.sageMist.opacity(0.5))
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.Token.softGrey.opacity(0.5), lineWidth: 1)
                )
                
                // Sunnah section
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 8) {
                        Image(systemName: "heart.fill")
                            .foregroundColor(Color.Token.goldDeep)
                            .font(.system(size: 20))
                        Text("Recommended Sunnah")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color.Token.goldDeep)
                    }
                    Text("Recommendations for this day will be loaded from the shared module.")
                        .font(.system(size: 14))
                        .foregroundColor(Color.Token.slate700)
                }
                .padding(14)
                .background(Color.Token.prayerCream.opacity(0.5))
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.Token.goldDeep.opacity(0.15), lineWidth: 1)
                )
            }
            .padding(20)
            .padding(.top, 16)
        }
        .background(Color(uiColor: .systemGroupedBackground).ignoresSafeArea())
    }
}

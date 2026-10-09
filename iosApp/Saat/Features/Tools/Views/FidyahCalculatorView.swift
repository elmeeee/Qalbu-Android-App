//
//  FidyahCalculatorView.swift
//  Saat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import SwiftUI

enum FidyahMadhab: String, CaseIterable, Identifiable {
    case shafii = "Syafi'i"
    case hanafi = "Hanafi"
    case maliki = "Maliki"
    case hanbali = "Hanbali"

    var id: String { rawValue }

    var stapleWeightKg: Double {
        switch self {
        case .shafii: return 0.675 // 1 Mud (~675 gram beras)
        case .hanafi: return 1.5   // 1/2 Sha' (~1.5 - 1.6 kg gandum/beras)
        case .maliki: return 0.675 // 1 Mud
        case .hanbali: return 0.675 // 1 Mud
        }
    }

    var explanation: String {
        switch self {
        case .shafii:
            return "Kadar 1 Mud (± 675 gram / 0,75 kg beras per hari). Jika hutang puasa melewati Ramadhan berikutnya tanpa udzur, fidyah berlipat ganda sesuai kelipatan tahun."
        case .hanafi:
            return "Kadar ½ Sha' (± 1,5 kg beras/makanan pokok per hari). Fidyah tidak berlipat ganda meski melewati bertahun-tahun."
        case .maliki:
            return "Kadar 1 Mud (± 675 gram beras per hari). Disunnahkan menambah sedikit untuk fakir miskin."
        case .hanbali:
            return "Kadar 1 Mud (± 675 gram beras per hari). Wajib bagi orang tua renta, sakit menahun, dan wanita hamil/menyusui yang khawatir pada anaknya."
        }
    }
}

struct FidyahCalculatorView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared

    @State private var selectedMadhab: FidyahMadhab = .shafii
    @State private var missedDays: String = "7"
    @State private var yearsDelayed: String = "1"
    @State private var ricePricePerKg: String = "15000" // IDR default

    private var daysCount: Int {
        max(0, Int(missedDays.filter { $0.isNumber }) ?? 0)
    }

    private var yearsCount: Int {
        max(1, Int(yearsDelayed.filter { $0.isNumber }) ?? 1)
    }

    private var ricePrice: Double {
        max(0, Double(ricePricePerKg.filter { $0.isNumber }) ?? 15000.0)
    }

    private var multiplier: Int {
        selectedMadhab == .shafii ? yearsCount : 1
    }

    private var totalWeightKg: Double {
        Double(daysCount * multiplier) * selectedMadhab.stapleWeightKg
    }

    private var totalCost: Double {
        totalWeightKg * ricePrice
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            HStack(spacing: 12) {
                Button(action: { dismiss() }) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(Color(hex: "#085E43"))
                        .frame(width: 40, height: 40)
                        .background(Color.white)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(languageManager.localize("tool_fidyah_tracker_title"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "#1E293B"))

                    Text("Kalkulator 4 Mazhab & Rekod Qadha")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(Color(hex: "#64748B"))
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color(hex: "#F9F7F2"))

            ScrollView {
                VStack(spacing: 16) {
                    // Madhhab Selector
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Pilih Mazhab Fiqih")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color(hex: "#1E293B"))

                        HStack(spacing: 8) {
                            ForEach(FidyahMadhab.allCases) { m in
                                let isSelected = selectedMadhab == m
                                Button(action: { selectedMadhab = m }) {
                                    Text(m.rawValue)
                                        .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 8)
                                        .background(isSelected ? Color(hex: "#085E43") : Color.white)
                                        .foregroundColor(isSelected ? .white : Color(hex: "#334155"))
                                        .cornerRadius(12)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(isSelected ? Color.clear : Color(hex: "#E8E2D2"), lineWidth: 1)
                                        )
                                }
                                .buttonStyle(.plain)
                            }
                        }

                        Text(selectedMadhab.explanation)
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "#64748B"))
                            .padding(.top, 4)
                            .lineSpacing(2)
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))

                    // Input Form
                    VStack(spacing: 14) {
                        inputField(
                            label: "Jumlah Hari Hutang Puasa",
                            hint: "Contoh: 7 hari",
                            text: $missedDays,
                            unit: "Hari"
                        )

                        if selectedMadhab == .shafii {
                            inputField(
                                label: "Tahun Terlewat (Melewati Ramadhan)",
                                hint: "1 jika baru tahun ini, 2 jika 2 tahun",
                                text: $yearsDelayed,
                                unit: "Tahun"
                            )
                        }

                        inputField(
                            label: "Harga Beras Pokok / Kg",
                            hint: "15000",
                            text: $ricePricePerKg,
                            unit: "Rp/Kg"
                        )
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#EAE4D6"), lineWidth: 1))

                    // Calculation Results Card
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Hasil Perhitungan Fidyah")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color(hex: "#085E43"))

                        Divider()

                        resultRow(label: "Kadar Pokok per Hari", value: String(format: "%.3f kg (1 Mud)", selectedMadhab.stapleWeightKg))
                        resultRow(label: "Total Hari Dihitung", value: "\(daysCount) hari \(multiplier > 1 ? "× \(multiplier) tahun" : "")")
                        resultRow(label: "Total Beras / Makanan Pokok", value: String(format: "%.2f kg", totalWeightKg))

                        Divider()

                        HStack {
                            Text("Total Estimasi Fidyah (Uang)")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color(hex: "#1E293B"))
                            Spacer()
                            Text(formatCurrency(Decimal(totalCost)))
                                .font(.system(size: 18, weight: .heavy))
                                .foregroundColor(Color(hex: "#085E43"))
                        }
                    }
                    .padding(18)
                    .background(Color(hex: "#FFFBEB"))
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "#FDE68A"), lineWidth: 1))
                }
                .padding(.horizontal, 16)
                .padding(.top, 14)
                .padding(.bottom, 32)
            }
            .background(Color(hex: "#F9F7F2"))
        }
        .background(Color(hex: "#F9F7F2").ignoresSafeArea())
        .navigationBarHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbar(.hidden, for: .tabBar)
    }

    private func inputField(label: String, hint: String, text: Binding<String>, unit: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Color(hex: "#334155"))

            HStack {
                TextField(hint, text: text)
                    .keyboardType(.numberPad)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(Color(hex: "#1E293B"))

                Text(unit)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(Color(hex: "#64748B"))
            }
            .padding(12)
            .background(Color(hex: "#F8FAFC"))
            .cornerRadius(10)
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "#E2E8F0"), lineWidth: 1))
        }
    }

    private func resultRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.system(size: 13.5))
                .foregroundColor(Color(hex: "#64748B"))
            Spacer()
            Text(value)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(Color(hex: "#1E293B"))
        }
    }

    private func formatCurrency(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencySymbol = "Rp "
        formatter.maximumFractionDigits = 0
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "Rp 0"
    }
}

import SwiftUI

struct ZakatCalculatorView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    @State private var selectedTab: ZakatType = .maal // 0: Maal, 1: Fitrah
    enum ZakatType: Int, CaseIterable {
        case maal = 0
        case fitrah = 1
    }
    
    // Zakat Maal Inputs
    @State private var cashSavings = ""
    @State private var goldWeightGrams = ""
    @State private var silverWeightGrams = ""
    @State private var investmentsValue = ""
    @State private var debtsValue = ""
    @State private var customGoldPrice = ""
    
    // Zakat Fitrah Inputs
    @State private var familyCount = ""
    @State private var ricePricePerKg = ""
    
    // Live Gold Quote
    @State private var goldQuote: GoldPriceQuote? = nil
    @State private var isFetchingPrice = false
    @State private var fetchError = false
    
    // Zakat Country
    @State private var selectedZakatCountry = ZakatCalculatorView.detectZakatCountry()
    
    private static func detectZakatCountry() -> ZakatCountry {
        let regionCode = Locale.current.region?.identifier ?? ""
        switch regionCode.uppercased() {
        case "MY": return .malaysia
        case "SG": return .singapore
        case "BN": return .brunei
        default: return .indonesia
        }
    }
    
    private var goldPricePerGram: Double {
        if let quote = goldQuote, quote.goldPerGramIdr > 0 {
            return quote.goldPerGramIdr
        }
        let manual = Double(customGoldPrice.filter { $0.isNumber }) ?? 0.0
        return manual > 0 ? manual : 0.0
    }
    
    private var silverPricePerGram: Double {
        return goldQuote?.silverPerGramIdr ?? ZakatCalculator.silverPriceFromGold(goldPricePerGram: goldPricePerGram)
    }
    
    private var maalResult: ZakatMaalCalculationResult? {
        if goldPricePerGram <= 0.0 { return nil }
        
        let cash = Double(cashSavings.filter { $0.isNumber }) ?? 0.0
        let gold = Double(goldWeightGrams.replacingOccurrences(of: ",", with: ".")) ?? 0.0
        let silver = Double(silverWeightGrams.replacingOccurrences(of: ",", with: ".")) ?? 0.0
        let inv = Double(investmentsValue.filter { $0.isNumber }) ?? 0.0
        let debts = Double(debtsValue.filter { $0.isNumber }) ?? 0.0
        
        return ZakatCalculator.calculate(
            cash: cash,
            goldGrams: gold,
            silverGrams: silver,
            investments: inv,
            debts: debts,
            goldPricePerGram: goldPricePerGram,
            silverPricePerGram: silverPricePerGram
        )
    }
    
    private var fitrahResult: ZakatFitrahCalculationResult? {
        guard let family = Int(familyCount), family > 0 else { return nil }
        let price = Double(ricePricePerKg.filter { $0.isNumber }) ?? 0.0
        guard price > 0 else { return nil }
        
        return ZakatCalculator.calculateFitrah(familyMembers: family, staplePricePerKg: price)
    }
    
    var body: some View {
        ZStack {
            SaatTokens.Colors.screenBackground.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                HStack(spacing: 12) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "arrow.left")
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(SaatTokens.Colors.slate900)
                            .frame(width: 44, height: 44)
                    }
                    
                    Text(languageManager.localize("zakat_title"))
                        .font(.title2.bold())
                        .foregroundColor(SaatTokens.Colors.slate900)
                    
                    Spacer()
                }
                .padding(.horizontal, SaatTokens.Spacing.screenHorizontal)
                .padding(.vertical, 8)
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text(languageManager.localize("zakat_intro"))
                            .font(.body)
                            .foregroundColor(SaatTokens.Colors.slate500)
                        
                        Picker("", selection: $selectedTab) {
                            Text(languageManager.localize("zakat_type_maal")).tag(ZakatType.maal)
                            Text(languageManager.localize("zakat_type_fitrah")).tag(ZakatType.fitrah)
                        }
                        .pickerStyle(.segmented)
                        .padding(.vertical, 8)
                        
                        if selectedTab == .maal {
                            maalSection
                        } else {
                            fitrahSection
                        }
                        
                        // Results Section
                        if selectedTab == .maal, let result = maalResult {
                            resultsMaal(result)
                        } else if selectedTab == .fitrah, let result = fitrahResult {
                            resultsFitrah(result)
                        }
                        
                        // Zakat Bodies Directory
                        Divider().padding(.vertical, 8)
                        zakatBodiesSection
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 32)
                }
            }
        }
        .onAppear {
            Task { await fetchLivePrice() }
        }
        .toolbar(.hidden, for: .navigationBar)
    }
    
    // MARK: - Maal Section
    @ViewBuilder
    private var maalSection: some View {
        HStack(alignment: .center) {
            if isFetchingPrice {
                ProgressView()
                    .tint(SaatTokens.Colors.deepEmerald)
                    .frame(height: 20)
            } else {
                if let quote = goldQuote {
                    VStack(alignment: .leading) {
                        Text(String(format: languageManager.localize("zakat_live_gold_price"), formatCurrency(Decimal(quote.goldPerGramIdr))))
                            .font(.subheadline.weight(.semibold))
                            .foregroundColor(SaatTokens.Colors.deepEmerald)
                        
                        Text(String(format: languageManager.localize("zakat_live_price_source"), quote.sourceLabel))
                            .font(.caption)
                            .foregroundColor(SaatTokens.Colors.slate500)
                    }
                } else {
                    Text(languageManager.localize("zakat_price_error"))
                        .font(.subheadline)
                        .foregroundColor(SaatTokens.Colors.gold)
                }
            }
            
            Spacer()
            
            Button(action: { Task { await fetchLivePrice() } }) {
                Text(languageManager.localize("retry"))
                    .font(.subheadline)
                    .foregroundColor(SaatTokens.Colors.deepEmerald)
            }
        }
        
        if !isFetchingPrice && goldQuote == nil {
            moneyTextField(label: languageManager.localize("zakat_manual_gold_price"), text: $customGoldPrice)
            Text(languageManager.localize("zakat_manual_price_help"))
                .font(.caption)
                .foregroundColor(SaatTokens.Colors.slate500)
        }
        
        moneyTextField(label: languageManager.localize("zakat_cash"), text: $cashSavings)
        decimalTextField(label: languageManager.localize("zakat_gold_grams"), text: $goldWeightGrams)
        decimalTextField(label: languageManager.localize("zakat_silver_grams"), text: $silverWeightGrams)
        moneyTextField(label: languageManager.localize("zakat_investments"), text: $investmentsValue)
        moneyTextField(label: languageManager.localize("zakat_debts"), text: $debtsValue)
        
        Text(languageManager.localize("zakat_haul_note"))
            .font(.caption)
            .foregroundColor(SaatTokens.Colors.slate500)
    }
    
    // MARK: - Fitrah Section
    @ViewBuilder
    private var fitrahSection: some View {
        numberTextField(label: languageManager.localize("zakat_family_members"), text: $familyCount)
        moneyTextField(label: languageManager.localize("zakat_rice_price_per_kg"), text: $ricePricePerKg)
        Text(languageManager.localize("zakat_fitrah_note"))
            .font(.caption)
            .foregroundColor(SaatTokens.Colors.slate500)
    }
    
    // MARK: - Results
    @ViewBuilder
    private func resultsMaal(_ result: ZakatMaalCalculationResult) -> some View {
        Divider().padding(.vertical, 8)
        
        VStack(spacing: 8) {
            resultRow(label: languageManager.localize("zakat_net_wealth"), value: formatCurrency(Decimal(result.zakatableWealth)))
            resultRow(label: String(format: languageManager.localize("zakat_nisab_gold"), Int(result.nisabGoldGrams)), value: formatCurrency(Decimal(result.nisabGoldValue)))
            resultRow(label: String(format: languageManager.localize("zakat_nisab_silver"), Int(result.nisabSilverGrams)), value: formatCurrency(Decimal(result.nisabSilverValue)))
            resultRow(label: languageManager.localize("zakat_due"), value: formatCurrency(Decimal(result.zakatDue)), highlight: result.meetsNisab)
            
            if !result.meetsNisab {
                Text(languageManager.localize("zakat_below_nisab"))
                    .font(.caption)
                    .foregroundColor(SaatTokens.Colors.slate500)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }
    
    @ViewBuilder
    private func resultsFitrah(_ result: ZakatFitrahCalculationResult) -> some View {
        Divider().padding(.vertical, 8)
        
        VStack(spacing: 8) {
            resultRow(label: languageManager.localize("zakat_family_members"), value: "\(result.familyMembers)")
            resultRow(label: languageManager.localize("zakat_fitrah_weight_per_person"), value: "\(result.stapleWeightPerPersonKg) kg")
            resultRow(label: languageManager.localize("zakat_fitrah_total_weight"), value: "\(result.totalStapleKilograms) kg")
            resultRow(label: languageManager.localize("zakat_rice_price_per_kg"), value: formatCurrency(Decimal(result.staplePricePerKg)))
            resultRow(label: languageManager.localize("zakat_due"), value: formatCurrency(Decimal(result.zakatDue)), highlight: true)
        }
    }
    
    private func resultRow(label: String, value: String, highlight: Bool = false) -> some View {
        HStack {
            Text(label)
                .font(.body)
                .foregroundColor(SaatTokens.Colors.slate800)
            Spacer()
            Text(value)
                .font(.body.weight(.bold))
                .foregroundColor(highlight ? SaatTokens.Colors.deepEmerald : SaatTokens.Colors.slate900)
        }
    }
    
    // MARK: - Zakat Bodies Section
    @ViewBuilder
    private var zakatBodiesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(languageManager.localize("zakat_pay_where_title"))
                    .font(.headline.weight(.bold))
                    .foregroundColor(SaatTokens.Colors.slate900)
                Text(languageManager.localize("zakat_pay_where_subtitle"))
                    .font(.caption)
                    .foregroundColor(SaatTokens.Colors.slate500)
            }
            
            if selectedZakatCountry == .malaysia {
                Text(languageManager.localize("zakat_body_malaysia_note"))
                    .font(.caption)
                    .foregroundColor(SaatTokens.Colors.slate500)
            }
            
            ForEach(ZakatBodyRepository.byCountry(selectedZakatCountry)) { body in
                Button(action: {
                    if let url = URL(string: "https://\(body.websiteUrl)") {
                        UIApplication.shared.open(url)
                    }
                }) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            if let tag = body.stateTag {
                                Text(tag)
                                    .font(.caption.weight(.semibold))
                                    .foregroundColor(SaatTokens.Colors.deepEmerald)
                            }
                            Text(body.name)
                                .font(.body.weight(.semibold))
                                .foregroundColor(SaatTokens.Colors.slate900)
                            Text(body.fullName)
                                .font(.caption)
                                .foregroundColor(SaatTokens.Colors.slate500)
                            Text(body.websiteUrl)
                                .font(.caption)
                                .foregroundColor(SaatTokens.Colors.deepEmerald)
                        }
                        Spacer()
                        Image(systemName: "arrow.up.right.square")
                            .foregroundColor(SaatTokens.Colors.slate500)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(SaatTokens.Colors.pureWhite)
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(SaatTokens.Colors.softGrey, lineWidth: 1)
                    )
                }
            }
        }
    }
    
    // MARK: - Helpers
    private func moneyTextField(label: String, text: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(SaatTokens.Colors.slate500)
            
            TextField("0", text: Binding(
                get: { text.wrappedValue },
                set: { text.wrappedValue = MoneyInputFormatter.format($0) }
            ))
            .keyboardType(.numberPad)
            .padding(12)
            .background(SaatTokens.Colors.pureWhite)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(SaatTokens.Colors.softGrey, lineWidth: 1)
            )
        }
    }
    
    private func decimalTextField(label: String, text: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(SaatTokens.Colors.slate500)
            
            TextField("0.0", text: text)
                .keyboardType(.decimalPad)
                .padding(12)
                .background(SaatTokens.Colors.pureWhite)
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(SaatTokens.Colors.softGrey, lineWidth: 1)
                )
        }
    }
    
    private func numberTextField(label: String, text: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(SaatTokens.Colors.slate500)
            
            TextField("0", text: text)
                .keyboardType(.numberPad)
                .padding(12)
                .background(SaatTokens.Colors.pureWhite)
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(SaatTokens.Colors.softGrey, lineWidth: 1)
                )
        }
    }
    
    private func fetchLivePrice() async {
        isFetchingPrice = true
        fetchError = false
        if let quote = await GoldPriceRepository.shared.fetchQuote(currency: "IDR") {
            goldQuote = quote
        } else {
            fetchError = true
        }
        isFetchingPrice = false
    }
    
    private func formatCurrency(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        formatter.decimalSeparator = ","
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 0
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "0"
    }
}

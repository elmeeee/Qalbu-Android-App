import Foundation

struct ZakatBody: Identifiable {
    let id = UUID()
    let name: String
    let fullName: String
    let websiteUrl: String
    let country: ZakatCountry
    let stateTag: String?
    
    init(name: String, fullName: String, websiteUrl: String, country: ZakatCountry, stateTag: String? = nil) {
        self.name = name
        self.fullName = fullName
        self.websiteUrl = websiteUrl
        self.country = country
        self.stateTag = stateTag
    }
}

struct ZakatBodyRepository {
    static let all: [ZakatBody] = [
        // Indonesia
        ZakatBody(name: "BAZNAS", fullName: "Badan Amil Zakat Nasional", websiteUrl: "baznas.go.id", country: .indonesia),
        ZakatBody(name: "Dompet Dhuafa", fullName: "Dompet Dhuafa Republika", websiteUrl: "www.dompetdhuafa.org", country: .indonesia),
        ZakatBody(name: "LAZISMU", fullName: "Lembaga Amil Zakat Infak Sedekah Muhammadiyah", websiteUrl: "lazismu.org", country: .indonesia),
        ZakatBody(name: "LAZISNU", fullName: "Lembaga Amil Zakat Infak Shodaqoh NU", websiteUrl: "lazisnu.or.id", country: .indonesia),
        ZakatBody(name: "Rumah Zakat", fullName: "Rumah Zakat Indonesia", websiteUrl: "www.rumahzakat.org", country: .indonesia),
        ZakatBody(name: "YDSF", fullName: "Yayasan Dana Sosial Al-Falah", websiteUrl: "ydsf.org", country: .indonesia),
        
        // Malaysia
        ZakatBody(name: "PPZ-MAIWP", fullName: "Pusat Pungutan Zakat – Majlis Agama Islam Wilayah Persekutuan", websiteUrl: "www.zakat.com.my", country: .malaysia, stateTag: "WP Kuala Lumpur / Putrajaya / Labuan"),
        ZakatBody(name: "LZS", fullName: "Lembaga Zakat Selangor", websiteUrl: "www.zakatselangor.com.my", country: .malaysia, stateTag: "Selangor"),
        ZakatBody(name: "Zakat Pulau Pinang", fullName: "Majlis Agama Islam Negeri Pulau Pinang", websiteUrl: "zakat.mainpp.gov.my", country: .malaysia, stateTag: "Pulau Pinang"),
        ZakatBody(name: "MAIPk", fullName: "Majlis Agama Islam dan Adat Melayu Perak", websiteUrl: "www.maipk.gov.my", country: .malaysia, stateTag: "Perak"),
        ZakatBody(name: "MAINS", fullName: "Majlis Agama Islam Negeri Sembilan", websiteUrl: "www.mains.gov.my", country: .malaysia, stateTag: "Negeri Sembilan"),
        ZakatBody(name: "MAIJ", fullName: "Majlis Agama Islam Negeri Johor", websiteUrl: "www.maij.gov.my", country: .malaysia, stateTag: "Johor"),
        ZakatBody(name: "MAIM", fullName: "Majlis Agama Islam Melaka", websiteUrl: "www.maim.gov.my", country: .malaysia, stateTag: "Melaka"),
        ZakatBody(name: "MUIP", fullName: "Majlis Ugama Islam dan Adat Resam Melayu Pahang", websiteUrl: "www.muip.gov.my", country: .malaysia, stateTag: "Pahang"),
        ZakatBody(name: "MAIDAM", fullName: "Majlis Agama Islam dan Adat Melayu Terengganu", websiteUrl: "www.maidam.gov.my", country: .malaysia, stateTag: "Terengganu"),
        ZakatBody(name: "MAIK", fullName: "Majlis Agama Islam dan Adat Istiadat Melayu Kelantan", websiteUrl: "www.e-maik.my", country: .malaysia, stateTag: "Kelantan"),
        ZakatBody(name: "LZNK", fullName: "Lembaga Zakat Negeri Kedah", websiteUrl: "www.zakatkedah.com.my", country: .malaysia, stateTag: "Kedah"),
        ZakatBody(name: "MAIPs", fullName: "Majlis Agama Islam dan Adat Istiadat Melayu Perlis", websiteUrl: "www.maips.gov.my", country: .malaysia, stateTag: "Perlis"),
        ZakatBody(name: "MUIS Sabah", fullName: "Majlis Ugama Islam Sabah", websiteUrl: "muis.sabah.gov.my", country: .malaysia, stateTag: "Sabah"),
        ZakatBody(name: "TBS", fullName: "Tabung Baitulmal Sarawak", websiteUrl: "www.tbs.org.my", country: .malaysia, stateTag: "Sarawak"),
        
        // Singapore
        ZakatBody(name: "MUIS", fullName: "Majlis Ugama Islam Singapura", websiteUrl: "www.muis.gov.sg", country: .singapore),
        ZakatBody(name: "Zakat.sg", fullName: "Portal Zakat Rasmi Singapura", websiteUrl: "www.zakat.sg", country: .singapore),
        
        // Brunei
        ZakatBody(name: "MUIB", fullName: "Majlis Ugama Islam Brunei", websiteUrl: "www.muib.gov.bn", country: .brunei),
        ZakatBody(name: "JUZWAB", fullName: "Jabatan Urusan Zakat, Waqaf dan Baitulmal", websiteUrl: "www.mora.gov.bn", country: .brunei)
    ]
    
    static func byCountry(_ country: ZakatCountry) -> [ZakatBody] {
        return all.filter { $0.country == country }
    }
}

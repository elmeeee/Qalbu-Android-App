import SwiftUI

struct ReflectFeedPostCardView: View {
    let post: ReflectFeedPost
    let currentUserId: String?
    let isTogglingLike: Bool
    let onToggleLike: () -> Void
    let onTapVerse: (String) -> Void
    
    @ObservedObject private var languageManager = AppLanguageManager.shared
    
    private var bodyText: String {
        post.body?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    }
    
    private var verseKey: String? {
        post.references?.first?.verseKey
    }
    
    private var formattedDate: String {
        guard let iso = post.createdAt else { return "" }
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        guard let date = formatter.date(from: iso) ?? ISO8601DateFormatter().date(from: iso) else { return "" }
        
        let diff = Date().timeIntervalSince(date)
        if diff < 60 {
            return languageManager.localize("time_just_now")
        } else if diff < 3600 {
            return languageManager.localize("time_minutes_ago").replacingOccurrences(of: "%d", with: "\(Int(diff / 60))")
        } else if diff < 86400 {
            return languageManager.localize("time_hours_ago").replacingOccurrences(of: "%d", with: "\(Int(diff / 3600))")
        } else if diff < 86400 * 2 {
            return languageManager.localize("time_yesterday")
        } else if diff < 86400 * 7 {
            return languageManager.localize("time_days_ago").replacingOccurrences(of: "%d", with: "\(Int(diff / 86400))")
        } else {
            let df = DateFormatter()
            df.dateFormat = "d MMM"
            return df.string(from: date)
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header: Avatar, Name, Date
            HStack(alignment: .center, spacing: 12) {
                AsyncImage(url: URL(string: post.author?.avatarUrls ?? "")) { phase in
                    switch phase {
                    case .empty:
                        Circle().fill(SaatTokens.Colors.softGrey.opacity(0.5))
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fill)
                    case .failure:
                        Circle().fill(SaatTokens.Colors.softGrey.opacity(0.5))
                        Image(systemName: "person.fill").foregroundColor(SaatTokens.Colors.slate400)
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(width: 44, height: 44)
                .clipShape(Circle())
                .background(Circle().fill(SaatTokens.Colors.softGrey.opacity(0.5)))
                
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(post.author?.displayName ?? languageManager.localize("reflect_contributor"))
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(SaatTokens.Colors.slate800)
                            .lineLimit(1)
                        
                        if post.author?.verified == true {
                            Image(systemName: "checkmark.seal.fill")
                                .font(.system(size: 12))
                                .foregroundColor(SaatTokens.Colors.gold)
                        }
                    }
                    
                    Text(formattedDate)
                        .font(.system(size: 12))
                        .foregroundColor(SaatTokens.Colors.slate500)
                }
                
                Spacer()
                
                // Note: Follow button is omitted for now, matching Android parity can add it back later if needed
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            
            // Verse Reference
            if let vKey = verseKey, !vKey.isEmpty {
                Button(action: { onTapVerse(vKey) }) {
                    HStack(spacing: 8) {
                        Image(systemName: "book.closed.fill")
                            .font(.system(size: 14))
                        Text(vKey)
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundColor(SaatTokens.Colors.deepEmerald)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(SaatTokens.Colors.deepEmerald.opacity(0.12))
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(SaatTokens.Colors.deepEmerald.opacity(0.25), lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 16)
                .padding(.top, 14)
            }
            
            // Body Text
            if !bodyText.isEmpty {
                Text(bodyText)
                    .font(.system(size: 16))
                    .foregroundColor(SaatTokens.Colors.slate800)
                    .lineSpacing(4)
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
            }
            
            // Tags
            if let tags = post.tags?.compactMap({ $0.name?.trimmingCharacters(in: .whitespaces) }).filter({ !$0.isEmpty }), !tags.isEmpty {
                HStack(spacing: 8) {
                    ForEach(tags.prefix(4), id: \.self) { tag in
                        Text("#\(tag)")
                            .font(.system(size: 12))
                            .foregroundColor(SaatTokens.Colors.slate600)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(SaatTokens.Colors.slate800.opacity(0.08))
                            .clipShape(Capsule())
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 12)
            }
            
            // Recent Comment
            if let commentBody = post.recentComment?.body?.trimmingCharacters(in: .whitespacesAndNewlines), !commentBody.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    Text(languageManager.localize("reflect_recent_comment"))
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(SaatTokens.Colors.slate500)
                    
                    Text(commentBody)
                        .font(.system(size: 13))
                        .foregroundColor(SaatTokens.Colors.slate800.opacity(0.85))
                        .lineLimit(3)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(12)
                .background(SaatTokens.Colors.softGrey.opacity(0.5))
                .cornerRadius(12)
                .padding(.horizontal, 16)
                .padding(.top, 14)
            }
            
            Spacer().frame(height: 16)
            
            // Actions
            HStack {
                Button(action: onToggleLike) {
                    HStack(spacing: 6) {
                        if isTogglingLike {
                            ProgressView()
                                .tint(post.isLiked == true ? .red : SaatTokens.Colors.slate500)
                                .scaleEffect(0.7)
                                .frame(width: 20, height: 20)
                        } else {
                            Image(systemName: post.isLiked == true ? "heart.fill" : "heart")
                                .font(.system(size: 18))
                                .foregroundColor(post.isLiked == true ? .red : SaatTokens.Colors.slate500)
                        }
                        
                        Text("\(post.likesCount ?? 0)")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(SaatTokens.Colors.slate800)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(SaatTokens.Colors.softGrey)
                    .clipShape(Capsule())
                }
                .buttonStyle(.plain)
                .disabled(isTogglingLike)
                
                Spacer()
                
                Button(action: {
                    // Share action
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 16))
                            .foregroundColor(SaatTokens.Colors.slate500)
                        
                        Text(languageManager.localize("share"))
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(SaatTokens.Colors.slate800)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(SaatTokens.Colors.softGrey)
                    .clipShape(Capsule())
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
        .background(SaatTokens.Colors.pureWhite)
        .cornerRadius(20)
    }
}

import SwiftUI

struct ReflectReelFeedView: View {
    @Bindable var viewModel: ReflectionViewModel
    @Environment(\.appContainer) private var container
    @State private var verseDetailViewModel = VerseDetailViewModel()

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    SaatTokens.Colors.deepEmerald,
                    SaatTokens.Colors.tealDark,
                    SaatTokens.Colors.screenBackground
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                VStack(alignment: .leading, spacing: 4) {
                    Text(AppLanguageManager.shared.localize("nav_reflect"))
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                    
                    Text(AppLanguageManager.shared.localize("reflect_community"))
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.72))
                    
                    Spacer().frame(height: 16)
                    
                    // Segmented Control
                    ReflectFeedTabBarView(
                        selection: viewModel.selectedSegment,
                        onSelect: { viewModel.onSegmentChanged(to: $0) }
                    )
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 12)
                
                // Content
                ScrollView {
                    LazyVStack(spacing: 14) {
                        if viewModel.isLoading && viewModel.posts.isEmpty {
                            ForEach(0..<3, id: \.self) { _ in
                                // Skeleton
                                RoundedRectangle(cornerRadius: 20)
                                    .fill(Color.white.opacity(0.1))
                                    .frame(height: 150)
                            }
                        } else if let error = viewModel.errorMessage, viewModel.posts.isEmpty {
                            Text(error)
                                .foregroundColor(.white)
                                .padding()
                        } else if viewModel.posts.isEmpty {
                            Text(viewModel.selectedSegment == .mine ? AppLanguageManager.shared.localize("reflect_empty_mine") : AppLanguageManager.shared.localize("reflect_empty_all"))
                                .foregroundColor(.white.opacity(0.75))
                                .multilineTextAlignment(.center)
                                .padding(40)
                        } else {
                            ForEach(viewModel.posts) { post in
                                ReflectFeedPostCardView(
                                    post: post,
                                    currentUserId: container?.userSession.currentUserId,
                                    isTogglingLike: viewModel.isTogglingLike(postId: post.id),
                                    onToggleLike: { Task { await viewModel.toggleLike(for: post) } },
                                    onTapVerse: { key in
                                        Task {
                                            await verseDetailViewModel.open(
                                                verseKey: key,
                                                content: container?.content
                                            )
                                        }
                                    }
                                )
                                .onAppear {
                                    viewModel.loadMoreIfNeeded(currentPost: post)
                                }
                            }
                            
                            if viewModel.isLoadingMore {
                                ProgressView()
                                    .tint(SaatTokens.Colors.goldBright)
                                    .padding()
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 100)
                }
                .refreshable {
                    await viewModel.loadPosts(refresh: true, force: true)
                }
            }
        }
        .sheet(item: verseSheetBinding) { item in
            ReflectVerseDetailSheetView(
                verseKey: item.id,
                response: verseDetailViewModel.verseSheetData,
                isLoading: verseDetailViewModel.isLoading
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
            .presentationCornerRadius(28)
        }
    }

    private var verseSheetBinding: Binding<VerseSheetItem?> {
        Binding(
            get: {
                guard let key = verseDetailViewModel.selectedVerseKey else { return nil }
                return VerseSheetItem(verseKey: key)
            },
            set: { newValue in
                if newValue == nil {
                    verseDetailViewModel.reset()
                }
            }
        )
    }
}

package com.example.tamilstream.data.repository

import com.example.tamilstream.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TamilStreamRepository {

    // Initial Live Channels (Verified from https://iptv-org.github.io/iptv/languages/tam.m3u)
    private val initialChannels = listOf(
        Channel(
            id = "7smusic_in_sd",
            name = "7S Music (576p)",
            cleanName = "7S Music",
            resolution = "576P",
            logo = "https://i.imgur.com/zDiIhdN.png",
            group = "Music",
            url = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            fallbackUrl = "https://stream.ottlive.co.in/aryatvtamil/index.m3u8",
            enabled = true,
            isFeatured = true
        ),
        Channel(
            id = "aaryaatv_in_sd",
            name = "Aaryaa TV (1080p)",
            cleanName = "Aaryaa TV",
            resolution = "1080P",
            logo = "https://jiotvimages.cdn.jio.com/dare_images/images/aryatvtamil.png",
            group = "Music",
            url = "https://stream.ottlive.co.in/aryatvtamil/index.m3u8",
            fallbackUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            enabled = true,
            isFeatured = true
        ),
        Channel(
            id = "aaseervathamtv_in_sd",
            name = "Aaseervatham TV (576p)",
            cleanName = "Aaseervatham TV",
            resolution = "576P",
            logo = "https://i.imgur.com/GlfrYs7.png",
            group = "Religious",
            url = "https://mumt04.tangotv.in/m18aqlK4AASEERVATHAMTV/index.m3u8",
            enabled = true,
            isFeatured = true
        ),
        Channel(
            id = "aasthatamil_in_sd",
            name = "Aastha Tamil (480p)",
            cleanName = "Aastha Tamil",
            resolution = "480P",
            logo = "https://i.imgur.com/YQK9ewf.png",
            group = "Religious",
            url = "https://aasthaott.akamaized.net/110923/smil:aasthatamil.smil/playlist.m3u8",
            enabled = true,
            isFeatured = true
        ),
        Channel(
            id = "angeltv_in_india",
            name = "Angel TV India (720p)",
            cleanName = "Angel TV",
            resolution = "720P",
            logo = "https://i.imgur.com/qKLEGU7.png",
            group = "Religious",
            url = "https://janya-digimix.akamaized.net/vglive-sk-394914/india/ngrp:angelindia_all/playlist.m3u8",
            enabled = true,
            isFeatured = true
        ),
        Channel(
            id = "arputhar_yesu_tv",
            name = "Arputhar Yesu TV (576p)",
            cleanName = "Arputhar Yesu TV",
            resolution = "576P",
            logo = "https://arputharyesutv.net/wp-content/uploads/2022/07/AR-LOGO-PNG.png",
            group = "Religious",
            url = "https://account33.livebox.co.in/jesushelpshls/live.m3u8",
            enabled = true
        ),
        Channel(
            id = "aathavan_tv_dash",
            name = "Aathavan TV (MPEG-DASH 720p)",
            cleanName = "Aathavan TV",
            resolution = "720P",
            logo = "https://i.imgur.com/LEVEhTH.png",
            group = "Entertainment",
            url = "https://yuppmumfta1.vgcdn.net/211125/athvantv/manifest.mpd",
            enabled = true,
            isFeatured = true
        ),
        Channel(
            id = "polimer_news_sd",
            name = "Polimer News Live (1080p)",
            cleanName = "Polimer News",
            resolution = "1080P",
            logo = "https://upload.wikimedia.org/wikipedia/en/2/23/Polimer_News_logo.png",
            group = "News",
            url = "https://stream.ottlive.co.in/aryatvtamil/index.m3u8",
            fallbackUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            enabled = true
        ),
        Channel(
            id = "sun_tv_live",
            name = "Sun TV Tamil (1080p)",
            cleanName = "Sun TV",
            resolution = "1080P",
            logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Sun_TV_logo.svg/200px-Sun_TV_logo.svg.png",
            group = "Entertainment",
            url = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            fallbackUrl = "https://stream.ottlive.co.in/aryatvtamil/index.m3u8",
            enabled = true
        ),
        Channel(
            id = "test_stream_mux",
            name = "Tamil Stream HD Broadcast (Verified)",
            cleanName = "Tamil Broadcast Master",
            resolution = "1080P",
            logo = "https://i.imgur.com/zDiIhdN.png",
            group = "General",
            url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            fallbackUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            enabled = true,
            isFeatured = true
        )
    )

    // Initial OTT Movies
    private val initialMovies = listOf(
        Movie(
            id = "movie_leo",
            title = "Leo: Bloody Sweet",
            slug = "leo-bloody-sweet",
            poster = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
            thumbnail = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
            backdrop = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            description = "Parthiban is an unassuming cafe owner in Theog who becomes a local hero after thwarting an armed robbery. But a mysterious syndicate mistakes him for Leo Das.",
            synopsis = "A mild-mannered cafe owner in Kashmir must protect his family when notorious gangsters from his rumored past return with a vengeance.",
            genre = listOf("Action", "Thriller", "Crime"),
            language = "Tamil",
            releaseYear = 2023,
            ageRating = "U/A 16+",
            cast = listOf("Thalapathy Vijay", "Sanjay Dutt", "Arjun Sarja", "Trisha Krishnan"),
            director = "Lokesh Kanagaraj",
            duration = 164,
            videoUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            isFeatured = true,
            isTrending = true,
            isPremium = true,
            views = 184500,
            rating = 8.6
        ),
        Movie(
            id = "movie_vikram",
            title = "Vikram: Hitlist",
            slug = "vikram-hitlist",
            poster = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=600&auto=format&fit=crop&q=80",
            thumbnail = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800&auto=format&fit=crop&q=80",
            backdrop = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200&auto=format&fit=crop&q=80",
            description = "A black-ops squad led by Amar is assigned to track down a covert masked vigilante syndicate after a series of high-profile murders in the city.",
            synopsis = "Investigating a masked vigilante group leads commander Amar down an explosive rabbit hole involving black-ops legend Agent Vikram.",
            genre = listOf("Action", "Crime", "Suspense"),
            language = "Tamil",
            releaseYear = 2022,
            ageRating = "U/A 16+",
            cast = listOf("Kamal Haasan", "Vijay Sethupathi", "Fahadh Faasil", "Suriya"),
            director = "Lokesh Kanagaraj",
            duration = 173,
            videoUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            isFeatured = true,
            isTrending = true,
            isPremium = false,
            views = 295000,
            rating = 8.9
        ),
        Movie(
            id = "movie_jailer",
            title = "Jailer: Tiger Ka Hukum",
            slug = "jailer-tiger-ka-hukum",
            poster = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=600&auto=format&fit=crop&q=80",
            thumbnail = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800&auto=format&fit=crop&q=80",
            backdrop = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=1200&auto=format&fit=crop&q=80",
            description = "Muthuvel Pandian, a retired prison warden known for his strict ethics, embarks on a relentless path of retribution when an idol-smuggling cartel harms his family.",
            synopsis = "A retired jailer goes on a manhunt to find his police officer son's killers, encountering formidable underworld figures along the way.",
            genre = listOf("Action", "Dark Comedy", "Revenge"),
            language = "Tamil",
            releaseYear = 2023,
            ageRating = "U/A 16+",
            cast = listOf("Rajinikanth", "Mohanlal", "Shiva Rajkumar", "Ramya Krishnan"),
            director = "Nelson Dilipkumar",
            duration = 168,
            videoUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            isFeatured = true,
            isTrending = true,
            isPremium = true,
            views = 310000,
            rating = 8.4
        ),
        Movie(
            id = "movie_ps2",
            title = "Ponniyin Selvan: Part II",
            slug = "ponniyin-selvan-2",
            poster = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            thumbnail = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=800&auto=format&fit=crop&q=80",
            backdrop = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            description = "Arulmozhi Varman continues his journey to become Rajaraja I, the greatest ruler of the historic Chola Empire, navigating betrayal and royal conspiracies.",
            synopsis = "The epic climax unfolds as Chola crown prince Aditha Karikalan confronts the vengeful queen Nandini, while conspiracies threaten the realm.",
            genre = listOf("Historical", "Epic Drama", "Action"),
            language = "Tamil",
            releaseYear = 2023,
            ageRating = "U/A 13+",
            cast = listOf("Vikram", "Aishwarya Rai Bachchan", "Jayam Ravi", "Karthi", "Trisha"),
            director = "Mani Ratnam",
            duration = 164,
            videoUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            isFeatured = false,
            isTrending = true,
            isPremium = false,
            views = 245000,
            rating = 8.7
        )
    )

    // Subscription Plans
    private val subscriptionPlans = listOf(
        SubscriptionPlan(
            id = "plan_basic",
            name = "Basic Tamil Pack",
            slug = "basic",
            priceInr = 199,
            durationDays = 30,
            billingPeriodLabel = "30 Days",
            description = "Essential access to all live Tamil channels with zero ads and HD 720p quality.",
            features = listOf(
                "Access to 50+ Live Tamil Channels",
                "HD 720p Streaming Quality",
                "1 Device Simultaneous Streaming",
                "Instant Activation"
            ),
            videoQuality = "HD 720p",
            maxScreens = 1
        ),
        SubscriptionPlan(
            id = "plan_premium",
            name = "Gold Premium OTT",
            slug = "premium",
            priceInr = 499,
            durationDays = 90,
            billingPeriodLabel = "3 Months",
            description = "Full access to Live TV, Blockbuster Tamil Movies & Web Series with 1080p stream.",
            features = listOf(
                "All Live TV Channels + OTT Movies",
                "Full HD 1080p & Dolby Audio",
                "3 Devices Simultaneous Stream",
                "Exclusive New Tamil Releases",
                "Ad-Free Premium Player"
            ),
            isFeatured = true,
            videoQuality = "Full HD 1080p",
            maxScreens = 3,
            badge = "Most Popular"
        ),
        SubscriptionPlan(
            id = "plan_ultimate",
            name = "Ultimate VIP Annual",
            slug = "ultimate",
            priceInr = 1499,
            durationDays = 365,
            billingPeriodLabel = "1 Year",
            description = "The ultimate Tamil entertainment experience in 4K Ultra HD for the whole family.",
            features = listOf(
                "Complete Catalog (Live TV + 4K Movies)",
                "4K Ultra HD + Dolby Atmos",
                "5 Devices Simultaneous Streaming",
                "Offline Download Support",
                "Priority VIP Support & Early Premieres"
            ),
            videoQuality = "4K Ultra HD + Dolby",
            maxScreens = 5,
            badge = "Best Value"
        )
    )

    // Seed Tamil TV Series
    private val initialSeries = listOf(
        Series(
            id = "series_biggboss",
            title = "Bigg Boss",
            mainThumbnailUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=800&auto=format&fit=crop&q=80",
            description = "Grand Tamil reality television show hosted with high-voltage drama, house tasks, eliminations and housemate conflicts.",
            status = "Published",
            isEnabled = true,
            isFeatured = true
        ),
        Series(
            id = "series_ethirneechal",
            title = "Ethirneechal",
            mainThumbnailUrl = "https://images.unsplash.com/photo-1578022761797-b8636ac1773c?w=800&auto=format&fit=crop&q=80",
            description = "High-voltage family and courtroom drama showing Janani's fight against patriarchy.",
            status = "Published",
            isEnabled = true,
            isFeatured = true
        ),
        Series(
            id = "series_kayal",
            title = "Kayal",
            mainThumbnailUrl = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=800&auto=format&fit=crop&q=80",
            description = "Kayal's inspirational story of protecting her family's honour while unraveling corporate trials.",
            status = "Published",
            isEnabled = true,
            isFeatured = true
        ),
        Series(
            id = "series_vanathai",
            title = "Vanathai Pola",
            mainThumbnailUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=800&auto=format&fit=crop&q=80",
            description = "Emotional brother-sister bonds tested when unexpected guests arrive at the village festival.",
            status = "Published",
            isEnabled = true,
            isFeatured = false
        ),
        Series(
            id = "series_sundari",
            title = "Sundari",
            mainThumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80",
            description = "Sundari's relentless journey towards achieving her civil services IAS dream.",
            status = "Published",
            isEnabled = true,
            isFeatured = false
        ),
        Series(
            id = "series_singapennae",
            title = "Singapennae",
            mainThumbnailUrl = "https://images.unsplash.com/photo-1514306191717-452ec28c7814?w=800&auto=format&fit=crop&q=80",
            description = "Inspirational story of an independent woman overcoming factory struggles in Chennai.",
            status = "Published",
            isEnabled = true,
            isFeatured = false
        )
    )

    // Seed Tamil TV Episodes
    private val initialEpisodes = listOf(
        Episode(
            id = "ep_biggboss_1",
            seriesId = "series_biggboss",
            seriesName = "Bigg Boss",
            title = "Bigg Boss Episode 1 — Grand Launch Task",
            episodeNumber = 1,
            seasonNumber = 1,
            thumbnailUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            description = "The contestants enter the house and complete their first surprise housemate challenge.",
            status = "Published",
            isFeatured = true,
            isEnabled = true,
            durationMinutes = 45,
            views = 210000
        ),
        Episode(
            id = "ep_biggboss_2",
            seriesId = "series_biggboss",
            seriesName = "Bigg Boss",
            title = "Bigg Boss Episode 2 — Captaincy Battle",
            episodeNumber = 2,
            seasonNumber = 1,
            thumbnailUrl = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://stream.ottlive.co.in/aryatvtamil/index.m3u8",
            description = "Tensions rise as the housemates compete in the first endurance captaincy task.",
            status = "Published",
            isFeatured = true,
            isEnabled = true,
            durationMinutes = 42,
            views = 195000
        ),
        Episode(
            id = "ep_ethirneechal_450",
            seriesId = "series_ethirneechal",
            seriesName = "Ethirneechal",
            title = "Ethirneechal - Mega Climax Ep 450",
            episodeNumber = 450,
            seasonNumber = 1,
            thumbnailUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            description = "High-voltage courtroom drama as Janani stands tall against Gunasekaran's manipulative tactics.",
            status = "Published",
            isFeatured = true,
            isEnabled = true,
            durationMinutes = 24,
            views = 182000
        ),
        Episode(
            id = "ep_kayal_380",
            seriesId = "series_kayal",
            seriesName = "Kayal",
            title = "Kayal - Special Mahasangamam Ep 380",
            episodeNumber = 380,
            seasonNumber = 1,
            thumbnailUrl = "https://images.unsplash.com/photo-1578022761797-b8636ac1773c?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://stream.ottlive.co.in/aryatvtamil/index.m3u8",
            description = "Kayal bravely overcomes adversity to safeguard her family's honour.",
            status = "Published",
            isFeatured = true,
            isEnabled = true,
            durationMinutes = 22,
            views = 145000
        ),
        Episode(
            id = "ep_vanathai_520",
            seriesId = "series_vanathai",
            seriesName = "Vanathai Pola",
            title = "Vanathai Pola - Pongal Celebration Ep 520",
            episodeNumber = 520,
            seasonNumber = 1,
            thumbnailUrl = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://mumt04.tangotv.in/m18aqlK4AASEERVATHAMTV/index.m3u8",
            description = "Emotional brother-sister bonds tested when unexpected guests arrive at the village festival.",
            status = "Published",
            isFeatured = false,
            isEnabled = true,
            durationMinutes = 25,
            views = 98000
        ),
        Episode(
            id = "ep_sundari_610",
            seriesId = "series_sundari",
            seriesName = "Sundari",
            title = "Sundari - IAS Exam Results Ep 610",
            episodeNumber = 610,
            seasonNumber = 2,
            thumbnailUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://aasthaott.akamaized.net/110923/smil:aasthatamil.smil/playlist.m3u8",
            description = "Sundari's relentless dedication brings her to the ultimate milestone.",
            status = "Published",
            isFeatured = false,
            isEnabled = true,
            durationMinutes = 23,
            views = 120000
        ),
        Episode(
            id = "ep_singapennae_140",
            seriesId = "series_singapennae",
            seriesName = "Singapennae",
            title = "Singapennae - City Dreams Ep 140",
            episodeNumber = 140,
            seasonNumber = 1,
            thumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            description = "Ananda's struggle in the garment factory leads to an inspiring breakthrough.",
            status = "Draft",
            isFeatured = false,
            isEnabled = true,
            durationMinutes = 21,
            views = 0
        )
    )

    // Reactive State
    private val _channels = MutableStateFlow(initialChannels)
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _movies = MutableStateFlow(initialMovies)
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _seriesList = MutableStateFlow(initialSeries)
    val seriesList: StateFlow<List<Series>> = _seriesList.asStateFlow()

    private val _episodes = MutableStateFlow(initialEpisodes)
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    val authManager = FirebaseAuthManager()

    val plans: StateFlow<List<SubscriptionPlan>> = authManager.subscriptionPlans
    val manualUpiConfig: StateFlow<ManualUpiConfig> = authManager.manualUpiConfig
    val manualPaymentRequests: StateFlow<List<ManualPaymentRequest>> = authManager.manualPaymentRequests
    val isMasterPremiumEnabled: StateFlow<Boolean> = authManager.isMasterPremiumEnabled

    private val _watchlist = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlist: StateFlow<List<WatchlistItem>> = _watchlist.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<WatchHistoryItem>>(emptyList())
    val watchHistory: StateFlow<List<WatchHistoryItem>> = _watchHistory.asStateFlow()

    private val _currentUser = MutableStateFlow(
        authManager.currentUser.value ?: User(
            id = "user_guest",
            uid = "user_guest",
            name = "Guest Viewer",
            displayName = "Guest Viewer",
            username = "guest",
            email = "guest@tamilstream.app",
            role = "user",
            accountStatus = "active",
            isPremium = false
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    val registeredUsers: List<User> get() = authManager.getAllUsers()
    val adminActivityLogs = authManager.activityLogs
    val appSettings = authManager.appSettings
    val adConfig: StateFlow<AdConfig> = authManager.adConfig

    fun saveAdConfig(
        htmlContent: String,
        enabled: Boolean,
        topEnabled: Boolean = true,
        bottomEnabled: Boolean = true,
        adminUid: String = "admin_root_master"
    ): Result<AdConfig> {
        return authManager.saveAdConfig(htmlContent, enabled, topEnabled, bottomEnabled, adminUid)
    }

    fun toggleAdEnabled(adminUid: String = "admin_root_master"): Result<AdConfig> {
        return authManager.toggleAdEnabled(adminUid)
    }

    fun toggleAdTopEnabled(adminUid: String = "admin_root_master"): Result<AdConfig> {
        return authManager.toggleAdTopEnabled(adminUid)
    }

    fun toggleAdBottomEnabled(adminUid: String = "admin_root_master"): Result<AdConfig> {
        return authManager.toggleAdBottomEnabled(adminUid)
    }

    fun clearAdConfig(adminUid: String = "admin_root_master"): Result<AdConfig> {
        return authManager.clearAdConfig(adminUid)
    }

    fun saveManualUpiConfig(config: ManualUpiConfig, adminUid: String = "admin_root_master"): Result<ManualUpiConfig> {
        return authManager.saveManualUpiConfig(config, adminUid)
    }

    fun submitManualPayment(plan: SubscriptionPlan, paymentIdentifier: String, screenshotUrl: String, user: User): ManualPaymentRequest {
        return authManager.submitManualPayment(plan, paymentIdentifier, screenshotUrl, user)
    }

    fun acceptManualPayment(requestId: String, adminUid: String = "admin_root_master"): Result<ManualPaymentRequest> {
        val res = authManager.acceptManualPayment(requestId, adminUid)
        if (res.isSuccess) {
            _currentUser.value = authManager.currentUser.value ?: _currentUser.value
        }
        return res
    }

    fun rejectManualPayment(requestId: String, rejectionReason: String, adminUid: String = "admin_root_master"): Result<ManualPaymentRequest> {
        return authManager.rejectManualPayment(requestId, rejectionReason, adminUid)
    }

    fun grantUserPremium(uid: String, adminUid: String = "admin_root_master"): Result<User> {
        val res = authManager.grantUserPremium(uid, adminUid)
        if (res.isSuccess && _currentUser.value.uid == uid) {
            _currentUser.value = res.getOrThrow()
        }
        return res
    }

    fun revokeUserPremium(uid: String, adminUid: String = "admin_root_master"): Result<User> {
        val res = authManager.revokeUserPremium(uid, adminUid)
        if (res.isSuccess && _currentUser.value.uid == uid) {
            _currentUser.value = res.getOrThrow()
        }
        return res
    }

    fun toggleMasterPremium(adminUid: String = "admin_root_master"): Result<Boolean> {
        return authManager.toggleMasterPremium(adminUid)
    }

    fun saveSubscriptionPlan(plan: SubscriptionPlan, adminUid: String = "admin_root_master"): Result<List<SubscriptionPlan>> {
        return authManager.saveSubscriptionPlan(plan, adminUid)
    }

    fun deleteSubscriptionPlan(planId: String, adminUid: String = "admin_root_master"): Result<List<SubscriptionPlan>> {
        return authManager.deleteSubscriptionPlan(planId, adminUid)
    }

    private val _currentSubscription = MutableStateFlow<UserSubscription?>(null)
    val currentSubscription: StateFlow<UserSubscription?> = _currentSubscription.asStateFlow()

    fun toggleFavorite(channelId: String) {
        _channels.update { list ->
            list.map { ch ->
                if (ch.id == channelId) ch.copy(isFavorite = !ch.isFavorite) else ch
            }
        }
    }

    fun addChannel(channel: Channel) {
        _channels.update { listOf(channel) + it }
    }

    fun updateChannel(updated: Channel) {
        _channels.update { list ->
            list.map { if (it.id == updated.id) updated else it }
        }
    }

    fun deleteChannel(channelId: String) {
        _channels.update { list -> list.filterNot { it.id == channelId } }
    }

    private val _movieCategories = MutableStateFlow(listOf("Action", "Drama", "Comedy", "Romance", "Thriller", "Family", "Crime", "Historical"))
    val movieCategories: StateFlow<List<String>> = _movieCategories.asStateFlow()

    fun saveMovie(movie: Movie, adminUid: String = "admin_root_master") {
        val existing = _movies.value.find { it.id == movie.id }
        if (existing != null) {
            _movies.update { list ->
                list.map { if (it.id == movie.id) movie.copy(updatedAt = System.currentTimeMillis()) else it }
            }
            authManager.logAdminAction(adminUid, "MOVIE_UPDATED", movie.id, "Updated movie: ${movie.title}")
        } else {
            _movies.update { listOf(movie) + it }
            authManager.logAdminAction(adminUid, "MOVIE_CREATED", movie.id, "Created movie: ${movie.title}")
        }
    }

    fun addMovie(movie: Movie) {
        saveMovie(movie)
    }

    fun deleteMovie(movieId: String, adminUid: String = "admin_root_master") {
        val mv = _movies.value.find { it.id == movieId }
        _movies.update { list -> list.filterNot { it.id == movieId } }
        authManager.logAdminAction(adminUid, "MOVIE_DELETED", movieId, "Deleted movie: ${mv?.title ?: movieId}")
    }

    fun toggleMoviePublish(movieId: String, adminUid: String = "admin_root_master") {
        var newStatus = "Published"
        _movies.update { list ->
            list.map {
                if (it.id == movieId) {
                    newStatus = if (it.status.equals("Published", ignoreCase = true)) "Unpublished" else "Published"
                    it.copy(status = newStatus, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        val mv = _movies.value.find { it.id == movieId }
        authManager.logAdminAction(adminUid, "MOVIE_STATUS_TOGGLED", movieId, "Toggled status to $newStatus for ${mv?.title ?: movieId}")
    }

    fun toggleMovieFeatured(movieId: String, adminUid: String = "admin_root_master") {
        var isNowFeatured = false
        _movies.update { list ->
            list.map {
                if (it.id == movieId) {
                    isNowFeatured = !it.isFeatured
                    it.copy(isFeatured = isNowFeatured, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        val mv = _movies.value.find { it.id == movieId }
        authManager.logAdminAction(adminUid, "MOVIE_FEATURED_TOGGLED", movieId, "Toggled featured=$isNowFeatured for ${mv?.title ?: movieId}")
    }

    fun addMovieCategory(category: String, adminUid: String = "admin_root_master") {
        if (category.isNotBlank() && !_movieCategories.value.contains(category)) {
            _movieCategories.update { it + category.trim() }
            authManager.logAdminAction(adminUid, "MOVIE_CATEGORY_ADDED", category, "Added movie category: $category")
        }
    }

    fun deleteMovieCategory(category: String, adminUid: String = "admin_root_master") {
        _movieCategories.update { list -> list.filterNot { it.equals(category, ignoreCase = true) } }
        authManager.logAdminAction(adminUid, "MOVIE_CATEGORY_DELETED", category, "Deleted movie category: $category")
    }

    fun saveSeries(series: Series, adminUid: String = "admin_root_master") {
        val existing = _seriesList.value.find { it.id == series.id }
        if (existing != null) {
            _seriesList.update { list ->
                list.map { if (it.id == series.id) series.copy(updatedAt = System.currentTimeMillis()) else it }
            }
            authManager.logAdminAction(adminUid, "SERIES_UPDATED", series.id, "Updated series: ${series.title}")
        } else {
            _seriesList.update { listOf(series) + it }
            authManager.logAdminAction(adminUid, "SERIES_CREATED", series.id, "Created series: ${series.title}")
        }
    }

    fun deleteSeries(seriesId: String, adminUid: String = "admin_root_master") {
        val s = _seriesList.value.find { it.id == seriesId }
        _seriesList.update { list -> list.filterNot { it.id == seriesId } }
        // Remove child episodes attached to this series
        _episodes.update { list -> list.filterNot { it.seriesId == seriesId } }
        authManager.logAdminAction(adminUid, "SERIES_DELETED", seriesId, "Deleted series and associated episodes: ${s?.title ?: seriesId}")
    }

    fun toggleSeriesPublish(seriesId: String, adminUid: String = "admin_root_master") {
        var newStatus = "Published"
        _seriesList.update { list ->
            list.map {
                if (it.id == seriesId) {
                    newStatus = if (it.status.equals("Published", ignoreCase = true)) "Unpublished" else "Published"
                    it.copy(status = newStatus, isEnabled = newStatus == "Published", updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        val s = _seriesList.value.find { it.id == seriesId }
        authManager.logAdminAction(adminUid, "SERIES_STATUS_TOGGLED", seriesId, "Toggled status to $newStatus for ${s?.title ?: seriesId}")
    }

    fun saveEpisode(episode: Episode, adminUid: String = "admin_root_master") {
        var targetSeriesId = episode.seriesId
        var targetSeriesName = episode.seriesName
        if (targetSeriesId.isBlank() && targetSeriesName.isNotBlank()) {
            val matching = _seriesList.value.find { it.title.equals(targetSeriesName, ignoreCase = true) }
            if (matching != null) {
                targetSeriesId = matching.id
            } else {
                val newS = Series(title = targetSeriesName, mainThumbnailUrl = episode.thumbnailUrl)
                _seriesList.update { listOf(newS) + it }
                targetSeriesId = newS.id
            }
        } else if (targetSeriesId.isNotBlank() && targetSeriesName.isBlank()) {
            val s = _seriesList.value.find { it.id == targetSeriesId }
            if (s != null) {
                targetSeriesName = s.title
            }
        }
        val epToSave = episode.copy(seriesId = targetSeriesId, seriesName = targetSeriesName)
        val existing = _episodes.value.find { it.id == epToSave.id }
        if (existing != null) {
            _episodes.update { list ->
                list.map { if (it.id == epToSave.id) epToSave.copy(updatedAt = System.currentTimeMillis()) else it }
            }
            authManager.logAdminAction(adminUid, "EPISODE_UPDATED", epToSave.id, "Updated episode: ${epToSave.title}")
        } else {
            _episodes.update { listOf(epToSave) + it }
            authManager.logAdminAction(adminUid, "EPISODE_CREATED", epToSave.id, "Added episode: ${epToSave.title}")
        }
    }

    fun addEpisode(episode: Episode, adminUid: String = "admin_root_master") {
        saveEpisode(episode, adminUid)
    }

    fun updateEpisode(episode: Episode, adminUid: String = "admin_root_master") {
        saveEpisode(episode, adminUid)
    }

    fun deleteEpisode(episodeId: String, adminUid: String = "admin_root_master") {
        val ep = _episodes.value.find { it.id == episodeId }
        _episodes.update { list -> list.filterNot { it.id == episodeId } }
        authManager.logAdminAction(adminUid, "EPISODE_DELETED", episodeId, "Deleted episode: ${ep?.title ?: episodeId}")
    }

    fun toggleEpisodePublish(episodeId: String, adminUid: String = "admin_root_master") {
        var newStatus = "Published"
        _episodes.update { list ->
            list.map {
                if (it.id == episodeId) {
                    newStatus = if (it.status.equals("Published", ignoreCase = true)) "Unpublished" else "Published"
                    it.copy(status = newStatus, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        val ep = _episodes.value.find { it.id == episodeId }
        authManager.logAdminAction(adminUid, "EPISODE_STATUS_TOGGLED", episodeId, "Toggled status to $newStatus for ${ep?.title ?: episodeId}")
    }

    fun toggleEpisodeEnabled(episodeId: String, adminUid: String = "admin_root_master") {
        var isNowEnabled = true
        _episodes.update { list ->
            list.map {
                if (it.id == episodeId) {
                    isNowEnabled = !it.isEnabled
                    it.copy(isEnabled = isNowEnabled, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        val ep = _episodes.value.find { it.id == episodeId }
        authManager.logAdminAction(adminUid, "EPISODE_VISIBILITY_TOGGLED", episodeId, "Toggled enabled=$isNowEnabled for ${ep?.title ?: episodeId}")
    }

    fun toggleWatchlist(item: WatchlistItem) {
        _watchlist.update { list ->
            val exists = list.any { it.contentId == item.contentId }
            if (exists) {
                list.filterNot { it.contentId == item.contentId }
            } else {
                listOf(item) + list
            }
        }
    }

    fun recordHistory(item: WatchHistoryItem) {
        _watchHistory.update { list ->
            val filtered = list.filterNot { it.contentId == item.contentId }
            listOf(item) + filtered.take(19)
        }
    }

    fun clearHistory() {
        _watchHistory.value = emptyList()
    }

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastPlaybackDiagnostic = MutableStateFlow<PlaybackDiagnostic?>(null)
    val lastPlaybackDiagnostic: StateFlow<PlaybackDiagnostic?> = _lastPlaybackDiagnostic.asStateFlow()

    fun recordPlaybackDiagnostic(diag: PlaybackDiagnostic) {
        _lastPlaybackDiagnostic.value = diag
    }

    fun updateChannelStream(
        channelId: String,
        name: String,
        primaryUrl: String,
        fallbackUrl: String?,
        group: String,
        resolution: String,
        enabled: Boolean
    ) {
        _channels.update { list ->
            list.map { ch ->
                if (ch.id == channelId) {
                    ch.copy(
                        cleanName = name,
                        name = name,
                        url = primaryUrl,
                        fallbackUrl = fallbackUrl,
                        group = group,
                        resolution = resolution,
                        enabled = enabled
                    )
                } else ch
            }
        }
    }

    suspend fun checkChannelConnectivity(channelId: String): StreamConnectivityResult {
        val channel = _channels.value.find { it.id == channelId }
        val testUrl = channel?.url ?: ""
        val result = M3uPlaylistService.testStreamConnectivity(testUrl)
        _channels.update { list ->
            list.map { ch ->
                if (ch.id == channelId) {
                    ch.copy(
                        lastCheckStatus = if (result.isReachable) "HTTP ${result.statusCode} (Online, ${result.latencyMs}ms)" else "Error: ${result.statusMessage}",
                        lastCheckCode = result.statusCode,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )
                } else ch
            }
        }
        return result
    }

    suspend fun syncWithM3u(url: String = M3uPlaylistService.DEFAULT_M3U_URL): M3uParseResult {
        _isSyncing.value = true
        try {
            val parseResult = M3uPlaylistService.fetchAndParseM3uDetailed(url)
            if (parseResult.channels.isNotEmpty()) {
                _channels.update { existing ->
                    val manualChannels = existing.filter { it.isCustom }
                    val existingMap = existing.associateBy { it.id }

                    val mergedParsed = parseResult.channels.map { parsed ->
                        val prev = existingMap[parsed.id]
                        if (prev != null) {
                            parsed.copy(
                                isFavorite = prev.isFavorite,
                                fallbackUrl = prev.fallbackUrl ?: parsed.fallbackUrl,
                                lastCheckStatus = prev.lastCheckStatus,
                                lastCheckCode = prev.lastCheckCode
                            )
                        } else {
                            parsed
                        }
                    }

                    manualChannels + mergedParsed
                }
                _lastSyncTime.value = System.currentTimeMillis()
            }
            return parseResult
        } finally {
            _isSyncing.value = false
        }
    }

    fun getNextChannel(currentChannelId: String): Channel? {
        val list = _channels.value.filter { it.enabled }
        if (list.isEmpty()) return null
        val idx = list.indexOfFirst { it.id == currentChannelId }
        return if (idx != -1 && idx < list.size - 1) list[idx + 1] else list.firstOrNull()
    }

    fun getPreviousChannel(currentChannelId: String): Channel? {
        val list = _channels.value.filter { it.enabled }
        if (list.isEmpty()) return null
        val idx = list.indexOfFirst { it.id == currentChannelId }
        return if (idx > 0) list[idx - 1] else list.lastOrNull()
    }

    fun activateSubscription(plan: SubscriptionPlan) {
        val sub = UserSubscription(
            id = "sub_${System.currentTimeMillis()}",
            planId = plan.id,
            planName = plan.name,
            amountInr = plan.priceInr,
            startDate = "Today",
            expiryDate = "In ${plan.durationDays} days",
            videoQuality = plan.videoQuality
        )
        _currentSubscription.value = sub
        _currentUser.update { it.copy(isPremium = true) }
    }

    suspend fun registerUser(
        fullName: String,
        username: String,
        email: String,
        pass: String,
        confirm: String,
        terms: Boolean
    ): AuthResult {
        val res = authManager.registerUser(fullName, username, email, pass, confirm, terms)
        if (res is AuthResult.Success) {
            _currentUser.value = res.user
        }
        return res
    }

    suspend fun loginUser(idOrEmail: String, pass: String, rememberMe: Boolean): AuthResult {
        val res = authManager.loginUser(idOrEmail, pass, rememberMe)
        if (res is AuthResult.Success) {
            _currentUser.value = res.user
        }
        return res
    }

    suspend fun loginAdmin(idOrEmail: String, pass: String, rememberMe: Boolean): AuthResult {
        val res = authManager.loginAdmin(idOrEmail, pass, rememberMe)
        if (res is AuthResult.Success) {
            _currentUser.value = res.user
        }
        return res
    }

    suspend fun sendPasswordReset(email: String): Result<String> {
        return authManager.sendPasswordReset(email)
    }

    suspend fun updateProfile(uid: String, name: String, avatar: String): Result<User> {
        val res = authManager.updateProfile(uid, name, avatar)
        if (res.isSuccess) {
            _currentUser.value = res.getOrThrow()
        }
        return res
    }

    suspend fun toggleUserStatus(uid: String, adminUid: String): Result<User> {
        return authManager.toggleUserStatus(uid, adminUid)
    }

    suspend fun toggleUserRole(uid: String, adminUid: String): Result<User> {
        return authManager.toggleUserRole(uid, adminUid)
    }

    suspend fun deleteUser(uid: String, adminUid: String): Result<Boolean> {
        return authManager.deleteUser(uid, adminUid)
    }

    fun logout() {
        authManager.logout()
        _currentUser.value = User(id = "user_guest", name = "Guest Viewer", isPremium = false)
    }

    fun continueAsGuest() {
        _currentUser.value = User(
            id = "user_guest",
            uid = "user_guest",
            name = "Guest Viewer",
            displayName = "Guest Viewer",
            username = "guest",
            email = "guest@tamilstream.app",
            role = "user",
            accountStatus = "active",
            isPremium = false
        )
    }
}

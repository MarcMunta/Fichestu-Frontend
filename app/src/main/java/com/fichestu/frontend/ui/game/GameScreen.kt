package com.fichestu.frontend.ui.game

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fichestu.frontend.data.i18n.AppI18n
import com.fichestu.frontend.game.model.AppLanguage
import com.fichestu.frontend.game.GameRules
import com.fichestu.frontend.game.engine.GameEngine
import com.fichestu.frontend.game.model.BallOption
import com.fichestu.frontend.game.model.BallRoomPhase
import com.fichestu.frontend.game.model.BallRoomUiState
import com.fichestu.frontend.game.model.BattleCardType
import com.fichestu.frontend.game.model.BattlePhase
import com.fichestu.frontend.game.model.BattlePlayer
import com.fichestu.frontend.game.model.BattleUiState
import com.fichestu.frontend.game.model.GameUiState
import com.fichestu.frontend.game.model.MainTab
import com.fichestu.frontend.game.model.MarketToken
import com.fichestu.frontend.game.model.MarketUiState
import com.fichestu.frontend.game.model.NotificationUi
import com.fichestu.frontend.game.model.ProfileStats
import com.fichestu.frontend.game.model.ProfileUiState
import com.fichestu.frontend.game.model.TokenId
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import com.fichestu.frontend.ui.theme.TextSecondary
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun FichestuGameScreen(
    playerName: String,
    onLogout: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var notificationTrayOpen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current as? Activity

    LaunchedEffect(playerName) {
        viewModel.initializePlayer(playerName)
    }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onLogout()
        }
    }

    LaunchedEffect(uiState.transientMessage) {
        val message = uiState.transientMessage?.trim()
        if (!message.isNullOrBlank()) {
            delay(3000)
            viewModel.consumeTransientMessage()
        }
    }

    DisposableEffect(lifecycleOwner, activity, uiState.currentMatchId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && activity?.isChangingConfigurations != true) {
                viewModel.abandonActiveMatchForExit(onLogout)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val visibleTab = if (uiState.activeTab == MainTab.BATTLE) MainTab.BALL_ROOM else uiState.activeTab

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(NightBlue, Color(0xFF0E1A2D), Color(0xFF092B2B))
                )
            )
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        GameBackgroundPattern()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameTopBar(
                playerName = uiState.profile.playerName,
                profilePicUrl = uiState.profile.profilePicUrl,
                market = uiState.market,
                notificationCount = uiState.unreadNotificationCount,
                notificationsOpen = notificationTrayOpen,
                language = uiState.appLanguage,
                onToggleNotifications = {
                    val willOpen = !notificationTrayOpen
                    notificationTrayOpen = willOpen
                    if (willOpen) {
                        viewModel.markNotificationsRead()
                    }
                },
                onLogout = { viewModel.abandonActiveMatchForExit(onLogout) }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = visibleTab,
                    transitionSpec = {
                        val forward = targetState.ordinal >= initialState.ordinal
                        (slideInHorizontally(tween(260)) { if (forward) it else -it } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(tween(260)) { if (forward) -it else it } + fadeOut(tween(160)))
                    },
                    label = "main_tab_animation"
                ) { tab ->
                    when (tab) {
                        MainTab.DASHBOARD -> DashboardTab(
                            market = uiState.market,
                            rewardedAvailable = uiState.rewardedAvailable,
                            rewardedCooldownSec = uiState.rewardedCooldownSec,
                            language = uiState.appLanguage,
                            onSelectToken = viewModel::selectToken,
                            onBuy = viewModel::buySelectedToken,
                            onSell = viewModel::sellSelectedToken,
                            onOpenBallRoom = viewModel::openBallRoomTab,
                            onClaimRewarded = viewModel::claimRewardedAd
                        )

                        MainTab.BALL_ROOM, MainTab.BATTLE -> {
                            val showBallRoom = uiState.battle.phase == BattlePhase.LOCKED ||
                                uiState.activeTab != MainTab.BATTLE
                            if (showBallRoom) {
                                BallRoomFlow(
                                    ballRoom = uiState.ballRoom,
                                    cashBalance = uiState.market.totalBalance,
                                    language = uiState.appLanguage,
                                    isInRoom = uiState.currentMatchId != null ||
                                        uiState.ballRoom.players.any { it.isUser },
                                    onEnterRoom = viewModel::enterBallRoom,
                                    onCancelMatchmaking = viewModel::cancelMatchmaking,
                                    onPickBall = viewModel::pickBall,
                                    onFinishSelection = viewModel::finishBallSelection,
                                    onSelectionTimeout = viewModel::autoFinishBallSelectionOnTimeout,
                                    onMatchmakingFinished = viewModel::refreshActiveMatch,
                                    onOpenBattle = { viewModel.selectTab(MainTab.BATTLE) }
                                )
                            } else {
                                BattleFlow(
                                    battle = uiState.battle,
                                    market = uiState.market,
                                    language = uiState.appLanguage,
                                    onSelectAction = viewModel::chooseBattleAction,
                                    onSelectCard = viewModel::chooseBattleCard,
                                    onSelectTarget = viewModel::chooseBattleTarget,
                                    onPlayRound = viewModel::playBattleRound,
                                    onSelectToken = viewModel::selectToken,
                                    onResetCycle = viewModel::applyWinnerImpactAndReset
                                )
                            }
                        }

                        MainTab.PROFILE -> ProfileTab(
                            profile = uiState.profile,
                            onUsernameChange = viewModel::updateProfileUsername,
                            onEmailChange = viewModel::updateProfileEmail,
                            onSaveProfile = viewModel::saveProfile,
                            onCurrentPasswordChange = viewModel::updateCurrentPassword,
                            onNewPasswordChange = viewModel::updateNewPassword,
                            onConfirmPasswordChange = viewModel::updateConfirmPassword,
                            onChangePassword = viewModel::changePassword,
                            onUploadAvatar = viewModel::uploadProfileAvatar,
                            onSelectPresetAvatar = viewModel::selectPresetAvatar,
                            appLanguage = uiState.appLanguage,
                            onLanguageChange = viewModel::changeLanguage,
                            onLogout = { viewModel.abandonActiveMatchForExit(onLogout) }
                        )
                    }
                }
            }

            BottomGameNav(
                activeTab = visibleTab,
                language = uiState.appLanguage,
                onSelect = viewModel::selectTab
            )
        }

        AnimatedVisibility(
            visible = !uiState.transientMessage.isNullOrBlank(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 86.dp),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            NotificationToast(
                text = AppI18n.message(uiState.transientMessage, uiState.appLanguage)
                    ?: uiState.transientMessage.orEmpty(),
                modifier = Modifier
                    .heightIn(min = 42.dp)
            )
        }

        AnimatedVisibility(
            visible = notificationTrayOpen,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 86.dp, end = 14.dp),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(160))
        ) {
            NotificationTray(
                notifications = uiState.notifications,
                language = uiState.appLanguage,
                onClear = {
                    viewModel.clearNotifications()
                    notificationTrayOpen = false
                }
            )
        }
    }
}

@Composable
private fun GameTopBar(
    playerName: String,
    profilePicUrl: String?,
    market: MarketUiState,
    notificationCount: Int,
    notificationsOpen: Boolean,
    language: AppLanguage,
    onToggleNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val presetIndex = profilePicUrl.toPresetAvatarIndex()
    val changePositive = market.totalHoldingChange >= 0.0
    val chartValues = remember(market.tokens, market.totalBalance) { market.portfolioHistory() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(0.dp),
        color = Color(0xFF071326).copy(alpha = 0.96f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0xFF1A4965).copy(alpha = 0.72f), RoundedCornerShape(0.dp))
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Gold)
                    .border(2.dp, GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (presetIndex in PresetAvatars.indices) {
                    CompactPresetAvatar(preset = PresetAvatars[presetIndex], size = 42.dp)
                } else if (!profilePicUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePicUrl,
                        contentDescription = AppI18n.text("change_photo", language),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = playerName.take(1).uppercase(Locale.US),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NightBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Column(modifier = Modifier.width(172.dp)) {
                Text(
                    text = playerName,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = "Nivel 12  -  Coleccionista",
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            TopBarDivider()

            CompactTopMetric(
                label = "Valor total FTC",
                value = formatCurrency(market.totalBalance),
                detail = "≈ ${String.format(Locale.US, "%.2f €", market.totalBalance)}",
                valueColor = PureWhite,
                modifier = Modifier.width(170.dp)
            )

            TopBarDivider()

            CompactTopMetric(
                label = "Cambio 24h",
                value = formatCurrency(market.totalHoldingChange),
                detail = formatPercent(portfolioChangePercent(market)),
                valueColor = if (changePositive) Color(0xFF33E27B) else ChipRed,
                detailColor = if (changePositive) Color(0xFF33E27B) else ChipRed,
                modifier = Modifier.width(150.dp)
            )

            TokenSparkChart(
                values = chartValues,
                lineColor = if (changePositive) Color(0xFF33E27B) else ChipRed,
                glowColor = (if (changePositive) Color(0xFF33E27B) else ChipRed).copy(alpha = 0.16f),
                showLabels = false,
                modifier = Modifier
                    .width(118.dp)
                    .height(36.dp)
            )

            Spacer(Modifier.weight(1f))

            CompactNotificationButton(
                count = notificationCount,
                isOpen = notificationsOpen,
                language = language,
                onClick = onToggleNotifications
            )

            Surface(
                modifier = Modifier
                    .width(108.dp)
                    .height(42.dp)
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(8.dp),
                color = PanelBlue.copy(alpha = 0.88f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, PureWhite.copy(alpha = 0.13f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = AppI18n.text("exit", language),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactPresetAvatar(preset: PresetAvatar, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(preset.gradient))
            .padding(size * 0.16f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = preset.emoji,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = (size.value * 0.46f).sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NotificationBell(
    count: Int,
    isOpen: Boolean,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            color = if (isOpen) Gold.copy(alpha = 0.24f) else PanelBlue.copy(alpha = 0.92f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        if (isOpen) Gold else Gold.copy(alpha = 0.45f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = AppI18n.text("notifications", language),
                    tint = if (isOpen) Gold else PureWhite,
                    modifier = Modifier.size(23.dp)
                )
            }
        }

        if (count > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp),
                shape = CircleShape,
                color = ChipRed
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (count > 9) "9+" else count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationToast(
    text: String,
    modifier: Modifier = Modifier
) {
    val isCompact = text.length <= 34

    Surface(
        modifier = modifier
            .widthIn(
                min = if (isCompact) 0.dp else 300.dp,
                max = if (isCompact) 360.dp else 560.dp
            )
            .border(1.dp, Gold.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = PanelBlue.copy(alpha = 0.96f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Gold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = NightBlue,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = text,
                modifier = Modifier.widthIn(max = if (isCompact) 270.dp else 470.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                ),
                maxLines = if (isCompact) 1 else 4
            )
        }
    }
}

@Composable
private fun NotificationTray(
    notifications: List<NotificationUi>,
    language: AppLanguage,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(340.dp)
            .heightIn(max = 380.dp)
            .border(1.dp, Gold.copy(alpha = 0.55f), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = PanelBlue.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = AppI18n.text("notifications", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                if (notifications.isNotEmpty()) {
                    Text(
                        text = AppI18n.text("clear", language),
                        modifier = Modifier.clickable(onClick = onClear),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            if (notifications.isEmpty()) {
                Text(
                    text = AppI18n.text("no_notifications", language),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        val title = AppI18n.message(notification.title, language) ?: notification.title
                        val message = AppI18n.message(notification.message, language) ?: notification.message
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = DeepBlue.copy(alpha = 0.62f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        PureWhite.copy(alpha = 0.08f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = PureWhite,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = formatNotificationTime(notification.createdAt),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        letterSpacing = 0.4.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTab(
    market: MarketUiState,
    rewardedAvailable: Boolean,
    rewardedCooldownSec: Int,
    language: AppLanguage,
    onSelectToken: (TokenId) -> Unit,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onOpenBallRoom: () -> Unit,
    onClaimRewarded: () -> Unit
) {
    val selected = market.selectedMarketToken
    if (selected == null) {
        MockupMarketLoading(modifier = Modifier.fillMaxSize())
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth >= 920.dp) {
            MockupMarketDashboard(
                market = market,
                selected = selected,
                rewardedAvailable = rewardedAvailable,
                rewardedCooldownSec = rewardedCooldownSec,
                language = language,
                onSelectToken = onSelectToken,
                onBuy = onBuy,
                onSell = onSell,
                onOpenBallRoom = onOpenBallRoom,
                onClaimRewarded = onClaimRewarded,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                item {
                    MockupMarketDashboard(
                        market = market,
                        selected = selected,
                        rewardedAvailable = rewardedAvailable,
                        rewardedCooldownSec = rewardedCooldownSec,
                        language = language,
                        onSelectToken = onSelectToken,
                        onBuy = onBuy,
                        onSell = onSell,
                        onOpenBallRoom = onOpenBallRoom,
                        onClaimRewarded = onClaimRewarded,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MockupMarketLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 320.dp, max = 520.dp)
                .height(140.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0C1D33).copy(alpha = 0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Gold.copy(alpha = 0.36f), RoundedCornerShape(8.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cargando mercado",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sincronizando fichas, cartera y precios.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun MockupMarketDashboard(
    market: MarketUiState,
    selected: MarketToken,
    rewardedAvailable: Boolean,
    rewardedCooldownSec: Int,
    language: AppLanguage,
    onSelectToken: (TokenId) -> Unit,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onOpenBallRoom: () -> Unit,
    onClaimRewarded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chartValues = remember(market.tokens, market.totalBalance) { market.portfolioHistory() }

    BoxWithConstraints(modifier = modifier) {
        val wide = maxWidth >= 920.dp
        if (wide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.88f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MockupPortfolioHeader()
                    Row(
                        modifier = Modifier.weight(1.16f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MockupPortfolioChartCard(
                            market = market,
                            chartValues = chartValues,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        MockupSelectedTokenChartCard(
                            token = selected,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    Row(
                        modifier = Modifier.weight(0.90f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MockupDistributionCard(
                            market = market,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        MockupPerformanceCard(
                            market = market,
                            chartValues = chartValues,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    MockupActionRow(
                        market = market,
                        rewardedAvailable = rewardedAvailable,
                        rewardedCooldownSec = rewardedCooldownSec,
                        language = language,
                        onBuy = onBuy,
                        onSell = onSell,
                        onSwap = { onSelectToken(nextTokenId(market, selected.id)) },
                        onClaimRewarded = onClaimRewarded,
                        onOpenBallRoom = onOpenBallRoom
                    )
                }

                MockupTokenRail(
                    tokens = market.tokens,
                    selectedId = market.selectedToken,
                    onSelectToken = onSelectToken,
                    fillAvailableHeight = true,
                    modifier = Modifier
                        .weight(0.92f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MockupPortfolioHeader()
                MockupPortfolioChartCard(
                    market = market,
                    chartValues = chartValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                )
                MockupSelectedTokenChartCard(
                    token = selected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                )
                MockupDistributionCard(
                    market = market,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(158.dp)
                )
                MockupPerformanceCard(
                    market = market,
                    chartValues = chartValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(158.dp)
                )
                MockupTokenRail(
                    tokens = market.tokens,
                    selectedId = market.selectedToken,
                    onSelectToken = onSelectToken,
                    fillAvailableHeight = false,
                    modifier = Modifier.fillMaxWidth()
                )
                MockupActionRow(
                    market = market,
                    rewardedAvailable = rewardedAvailable,
                    rewardedCooldownSec = rewardedCooldownSec,
                    language = language,
                    onBuy = onBuy,
                    onSell = onSell,
                    onSwap = { onSelectToken(nextTokenId(market, selected.id)) },
                    onClaimRewarded = onClaimRewarded,
                    onOpenBallRoom = onOpenBallRoom
                )
            }
        }
    }
}

@Composable
private fun MockupPortfolioHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Cartera",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "El valor de tu cartera depende de tus fichas y sus precios en tiempo real.",
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
            )
        }
        MockupPeriodPills()
    }
}

@Composable
private fun MockupPeriodPills() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("24H", "7D", "30D", "90D").forEachIndexed { index, label ->
            val selected = index == 0
            Surface(
                modifier = Modifier
                    .width(46.dp)
                    .height(26.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (selected) Gold else Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            1.dp,
                            if (selected) Gold else PureWhite.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) NightBlue else TextSecondary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MockupDashboardCard(
    modifier: Modifier = Modifier,
    borderColor: Color = PureWhite.copy(alpha = 0.10f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0C1D33).copy(alpha = 0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(12.dp),
            content = content
        )
    }
}

@Composable
private fun MockupPortfolioChartCard(
    market: MarketUiState,
    chartValues: List<Double>,
    modifier: Modifier = Modifier
) {
    MockupDashboardCard(modifier = modifier) {
        Text(
            text = "Valor total de la cartera (FTC)",
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = formatCurrency(market.totalBalance).removeSuffix(" FTC"),
                maxLines = 1,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "FTC",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(Modifier.height(4.dp))
        TokenSparkChart(
            values = chartValues,
            lineColor = Gold,
            glowColor = Gold.copy(alpha = 0.24f),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun MockupSelectedTokenChartCard(
    token: MarketToken,
    modifier: Modifier = Modifier
) {
    val positive = token.changePercent >= 0.0
    val accent = tokenAccent(token)
    MockupDashboardCard(modifier = modifier, borderColor = accent.copy(alpha = 0.22f)) {
        Text(
            text = "${token.displayName} (${token.ticker})",
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = formatCurrency(token.currentPrice),
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = formatPercent(token.changePercent),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (positive) Color(0xFF33E27B) else ChipRed,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
        Spacer(Modifier.height(4.dp))
        TokenSparkChart(
            values = token.history,
            lineColor = accent,
            glowColor = accent.copy(alpha = 0.24f),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

private data class MockupDistributionSegment(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
private fun MockupDistributionCard(
    market: MarketUiState,
    modifier: Modifier = Modifier
) {
    val tokenSegments = market.tokens
        .filter { it.portfolioValue > 0.0 }
        .map { MockupDistributionSegment(it.displayName, it.portfolioValue, tokenAccent(it)) }
    val segments = tokenSegments.ifEmpty {
        listOf(MockupDistributionSegment("FTC en fichas", market.totalBalance.takeIf { it > 0.0 } ?: 1.0, TextSecondary))
    }
    val total = segments.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0

    MockupDashboardCard(modifier = modifier) {
        Text(
            text = "Distribucion por ficha",
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 18.dp.toPx()
                    val diameter = minOf(size.width, size.height) - stroke
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    var start = -90f
                    segments.forEach { segment ->
                        val sweep = ((segment.value / total) * 360.0).toFloat().coerceAtLeast(2f)
                        drawArc(
                            color = segment.color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatCurrency(market.totalBalance).removeSuffix(" FTC"),
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "FTC",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                segments.take(5).forEach { segment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(segment.color)
                        )
                        Text(
                            text = segment.label,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f%%", (segment.value / total) * 100.0),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MockupPerformanceCard(
    market: MarketUiState,
    chartValues: List<Double>,
    modifier: Modifier = Modifier
) {
    val change24 = market.totalHoldingChange
    val delta7 = historyDelta(chartValues, 7)
    val delta30 = historyDelta(chartValues, 30)
    val maxValue = chartValues.maxOrNull() ?: market.totalBalance
    val minValue = chartValues.minOrNull() ?: market.totalBalance

    MockupDashboardCard(modifier = modifier) {
        Text(
            text = "Rendimiento de la cartera",
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(8.dp))
        MockupPerformanceLine("Cambio 24h", change24, portfolioChangePercent(market))
        MockupPerformanceLine("Cambio 7d", delta7, percentFromDelta(market.totalBalance, delta7))
        MockupPerformanceLine("Cambio 30d", delta30, percentFromDelta(market.totalBalance, delta30))
        MockupStaticLine("Maximo historico", formatCurrency(maxValue))
        MockupStaticLine("Minimo historico", formatCurrency(minValue))
    }
}

@Composable
private fun MockupPerformanceLine(label: String, value: Double, percent: Double) {
    val positive = value >= 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
        )
        Text(
            text = formatCurrency(value),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (positive) Color(0xFF33E27B) else ChipRed,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = formatPercent(percent),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (positive) Color(0xFF33E27B) else ChipRed,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun MockupStaticLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
        )
        Text(
            text = value,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun MockupTokenRail(
    tokens: List<MarketToken>,
    selectedId: TokenId,
    onSelectToken: (TokenId) -> Unit,
    fillAvailableHeight: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.height(44.dp)) {
            Text(
                text = "Mercado de Fichas",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "Tus fichas y precios en tiempo real",
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
            )
        }
        tokens.forEach { token ->
            val cardModifier = if (fillAvailableHeight) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(106.dp)
            }
            MockupTokenRailCard(
                token = token,
                selected = token.id == selectedId,
                onClick = { onSelectToken(token.id) },
                modifier = cardModifier
            )
        }
    }
}

@Composable
private fun MockupTokenRailCard(
    token: MarketToken,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val positive = token.changePercent >= 0.0
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0C1D33).copy(alpha = if (selected) 0.98f else 0.88f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    2.dp,
                    if (selected) Gold else PureWhite.copy(alpha = 0.08f),
                    RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TokenMedallion(token = token, diameter = 42.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = token.displayName,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = token.ticker,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(token.currentPrice),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = formatPercent(token.changePercent),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (positive) Color(0xFF33E27B) else ChipRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MockupTokenMiniStat("Tienes", formatQuantity(token.holdings))
                MockupTokenMiniStat("Valor", formatCurrency(token.portfolioValue))
                MockupTokenMiniStat("% Cartera", String.format(Locale.US, "%.2f%%", token.portfolioWeightPercent))
            }
        }
    }
}

@Composable
private fun MockupTokenMiniStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun MockupActionRow(
    market: MarketUiState,
    rewardedAvailable: Boolean,
    rewardedCooldownSec: Int,
    language: AppLanguage,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onSwap: () -> Unit,
    onClaimRewarded: () -> Unit,
    onOpenBallRoom: () -> Unit
) {
    MockupDashboardCard(modifier = Modifier.fillMaxWidth().height(78.dp)) {
        Text(
            text = AppI18n.text("quick_actions", language),
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MockupActionButton(
                label = "Comprar fichas",
                icon = Icons.Default.ShoppingCart,
                primary = true,
                onClick = onBuy,
                modifier = Modifier.weight(1f)
            )
            MockupActionButton(
                label = "Vender fichas",
                icon = Icons.Default.LocalOffer,
                onClick = onSell,
                modifier = Modifier.weight(1f)
            )
            MockupActionButton(
                label = "Intercambiar",
                icon = Icons.Default.SwapHoriz,
                onClick = onSwap,
                modifier = Modifier.weight(1f)
            )
            MockupActionButton(
                label = if (rewardedAvailable) "+25 FTC" else "${rewardedCooldownSec}s",
                icon = Icons.Default.CardGiftcard,
                enabled = rewardedAvailable,
                onClick = onClaimRewarded,
                modifier = Modifier.weight(1f)
            )
            MockupActionButton(
                label = "Jugar (10 FTC)",
                icon = Icons.Default.SportsEsports,
                enabled = market.totalBalance >= GameRules.BALL_ENTRY_COST,
                onClick = onOpenBallRoom,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MockupActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(6.dp)
    val bg = when {
        !enabled -> DeepBlue.copy(alpha = 0.42f)
        primary -> Gold
        else -> PanelBlue.copy(alpha = 0.78f)
    }
    val fg = when {
        !enabled -> TextSecondary
        primary -> NightBlue
        else -> PureWhite
    }
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = bg
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, if (primary) Gold else PureWhite.copy(alpha = 0.10f), shape)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = fg,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

private fun historyDelta(values: List<Double>, window: Int): Double {
    if (values.size < 2) return 0.0
    val startIndex = (values.size - 1 - window).coerceAtLeast(0)
    return values.last() - values[startIndex]
}

private fun percentFromDelta(current: Double, delta: Double): Double {
    val previous = current - delta
    if (previous == 0.0) return 0.0
    return (delta / previous) * 100.0
}

private fun portfolioChangePercent(market: MarketUiState): Double {
    return percentFromDelta(market.holdingsValue, market.totalHoldingChange)
}

private fun nextTokenId(market: MarketUiState, current: TokenId): TokenId {
    val ids = market.tokens.map { it.id }.ifEmpty { TokenId.entries }
    val index = ids.indexOf(current).takeIf { it >= 0 } ?: 0
    return ids[(index + 1) % ids.size]
}

@Composable
private fun MarketCommandCenter(
    market: MarketUiState,
    selected: MarketToken,
    rewardedAvailable: Boolean,
    rewardedCooldownSec: Int,
    language: AppLanguage,
    onSelectToken: (TokenId) -> Unit,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onOpenBallRoom: () -> Unit,
    onClaimRewarded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedAccent = tokenAccent(selected)
    val holdingChangePositive = market.totalHoldingChange >= 0.0
    val chartValues = remember(market.tokens, market.totalBalance) { market.portfolioHistory() }
    val shape = RoundedCornerShape(8.dp)

    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Gold.copy(alpha = 0.86f),
                        selectedAccent.copy(alpha = 0.74f),
                        Color(0xFF00D7FF).copy(alpha = 0.50f)
                    )
                ),
                shape = shape
            ),
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF101725), Color(0xFF0A1424), Color(0xFF092D2D))
                    )
                )
                .padding(14.dp)
        ) {
            MarketCommandPattern(selectedAccent)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppI18n.text("market_title", language),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = AppI18n.text("portfolio_subtitle", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    LivePortfolioBadge(
                        total = market.totalBalance,
                        change = market.totalHoldingChange,
                        positive = holdingChangePositive
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWide = maxWidth >= 840.dp
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PortfolioStage(
                                market = market,
                                selected = selected,
                                chartValues = chartValues,
                                language = language,
                                modifier = Modifier.weight(1.42f)
                            )
                            TokenMarketRail(
                                tokens = market.tokens,
                                selectedId = market.selectedToken,
                                language = language,
                                onSelectToken = onSelectToken,
                                modifier = Modifier.weight(0.88f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PortfolioStage(
                                market = market,
                                selected = selected,
                                chartValues = chartValues,
                                language = language,
                                modifier = Modifier.fillMaxWidth()
                            )
                            TokenMarketRail(
                                tokens = market.tokens,
                                selectedId = market.selectedToken,
                                language = language,
                                onSelectToken = onSelectToken,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                QuickActionDeck(
                    selected = selected,
                    rewardedAvailable = rewardedAvailable,
                    rewardedCooldownSec = rewardedCooldownSec,
                    language = language,
                    onBuy = onBuy,
                    onSell = onSell,
                    onClaimRewarded = onClaimRewarded,
                    onOpenBallRoom = onOpenBallRoom
                )
            }
        }
    }
}

@Composable
private fun TopBarDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(PureWhite.copy(alpha = 0.10f))
    )
}

@Composable
private fun CompactTopMetric(
    label: String,
    value: String,
    detail: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    detailColor: Color = TextSecondary
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Text(
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium.copy(
                color = valueColor,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = detail,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = detailColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun CompactNotificationButton(
    count: Int,
    isOpen: Boolean,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            color = if (isOpen) Gold.copy(alpha = 0.20f) else Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = AppI18n.text("notifications", language),
                    tint = if (isOpen) Gold else PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (count > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp),
                shape = CircleShape,
                color = Gold
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (count > 9) "9+" else count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NightBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 8.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketCommandPattern(accent: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val grid = 80.dp.toPx()
        var y = grid
        while (y < size.height) {
            drawLine(
                color = PureWhite.copy(alpha = 0.035f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += grid
        }
        var x = grid
        while (x < size.width) {
            drawLine(
                color = Gold.copy(alpha = 0.030f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            x += grid
        }
        val chipRadius = 34.dp.toPx()
        val centers = listOf(
            Offset(size.width * 0.78f, size.height * 0.10f),
            Offset(size.width * 0.90f, size.height * 0.34f),
            Offset(size.width * 0.70f, size.height * 0.55f)
        )
        centers.forEachIndexed { index, center ->
            val tint = if (index == 0) Gold else accent
            drawCircle(
                color = tint.copy(alpha = 0.06f),
                radius = chipRadius + index * 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = tint.copy(alpha = 0.18f),
                radius = chipRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun LivePortfolioBadge(
    total: Double,
    change: Double,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (positive) Gold else ChipRed
    Surface(
        modifier = modifier.widthIn(min = 220.dp),
        shape = RoundedCornerShape(8.dp),
        color = NightBlue.copy(alpha = 0.70f)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, tint.copy(alpha = 0.46f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "FTC LIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = formatCurrency(total),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = formatCurrency(change),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = tint,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun PortfolioStage(
    market: MarketUiState,
    selected: MarketToken,
    chartValues: List<Double>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val accent = tokenAccent(selected)
    val positive = selected.changePercent >= 0.0
    val holdingPositive = selected.holdingChangeValue >= 0.0

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = NightBlue.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, PureWhite.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.widthIn(min = 250.dp, max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppI18n.text("portfolio_title", language),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp
                        )
                    )
                    Text(
                        text = formatCurrency(market.totalBalance),
                        maxLines = 1,
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = Gold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallValuePill(
                            label = AppI18n.text("token_value", language),
                            value = formatCurrency(market.holdingsValue),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(208.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF070D19).copy(alpha = 0.86f)
                ) {
                    TokenSparkChart(
                        values = chartValues,
                        lineColor = if (market.totalHoldingChange >= 0.0) Gold else ChipRed,
                        glowColor = (if (market.totalHoldingChange >= 0.0) Gold else ChipRed).copy(alpha = 0.20f),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DeepBlue.copy(alpha = 0.42f)
            ) {
                Row(
                    modifier = Modifier
                        .border(1.dp, accent.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TokenMedallion(token = selected, diameter = 66.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selected.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "${selected.ticker} · ${formatPercent(selected.changePercent)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (positive) Gold else ChipRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    TokenNumberBlock(AppI18n.text("price", language), formatCurrency(selected.currentPrice), PureWhite)
                    TokenNumberBlock(AppI18n.text("units", language), formatQuantity(selected.holdings), TextSecondary)
                    TokenNumberBlock(
                        AppI18n.text("contribution", language),
                        formatCurrency(selected.portfolioValue),
                        if (holdingPositive) Gold else ChipRed
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallValuePill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(8.dp),
        color = DeepBlue.copy(alpha = 0.40f)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = value,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun TokenMedallion(token: MarketToken, diameter: Dp) {
    val accent = tokenAccent(token)
    Box(
        modifier = Modifier
            .size(diameter)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.95f), NightBlue))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Gold.copy(alpha = 0.42f), style = Stroke(width = 3.dp.toPx()))
            drawCircle(
                color = PureWhite.copy(alpha = 0.15f),
                radius = diameter.toPx() * 0.26f,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        Text(
            text = tokenInitial(token.id),
            style = MaterialTheme.typography.titleLarge.copy(
                color = NightBlue,
                fontWeight = FontWeight.Black
            )
        )
    }
}

@Composable
private fun TokenNumberBlock(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            maxLines = 1,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun TokenMarketRail(
    tokens: List<MarketToken>,
    selectedId: TokenId,
    language: AppLanguage,
    onSelectToken: (TokenId) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = NightBlue.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, Color(0xFF00D7FF).copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppI18n.text("side_panel", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = AppI18n.text("value", language),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            tokens.forEach { token ->
                TokenRailCard(
                    token = token,
                    selected = token.id == selectedId,
                    language = language,
                    onClick = { onSelectToken(token.id) }
                )
            }
        }
    }
}

@Composable
private fun TokenRailCard(
    token: MarketToken,
    selected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val accent = tokenAccent(token)
    val positive = token.changePercent >= 0.0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accent.copy(alpha = 0.18f) else DeepBlue.copy(alpha = 0.42f)
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, if (selected) accent else PureWhite.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TokenMedallion(token = token, diameter = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = token.displayName,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = "${token.ticker} · ${formatQuantity(token.holdings)} ${AppI18n.text("units", language).lowercase()}",
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Text(
                    text = formatPercent(token.changePercent),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (positive) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            TokenSparkChart(
                values = token.history,
                lineColor = if (positive) Gold else ChipRed,
                glowColor = (if (positive) Gold else ChipRed).copy(alpha = 0.16f),
                showLabels = false,
                modifier = Modifier
                    .width(112.dp)
                    .height(42.dp)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(token.portfolioValue),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = formatCurrency(token.currentPrice),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun QuickActionDeck(
    selected: MarketToken,
    rewardedAvailable: Boolean,
    rewardedCooldownSec: Int,
    language: AppLanguage,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onClaimRewarded: () -> Unit,
    onOpenBallRoom: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = NightBlue.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, PureWhite.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppI18n.text("quick_actions", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = selected.ticker,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = tokenAccent(selected),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val wide = maxWidth >= 760.dp
                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ArcadePrimaryButton(AppI18n.text("buy_1", language), Modifier.weight(1f), onClick = onBuy)
                        ArcadeSecondaryButton(AppI18n.text("sell_1", language), Modifier.weight(1f), onClick = onSell)
                        ArcadePrimaryButton(
                            text = if (rewardedAvailable) AppI18n.text("rewarded_claim", language) else "${AppI18n.text("rewarded_cooldown", language)} ${rewardedCooldownSec}s",
                            modifier = Modifier.weight(1f),
                            enabled = rewardedAvailable,
                            onClick = onClaimRewarded
                        )
                        ArcadeSecondaryButton(AppI18n.text("play_paying_10", language), Modifier.weight(1f), onClick = onOpenBallRoom)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ArcadePrimaryButton(AppI18n.text("buy_1", language), Modifier.weight(1f), onClick = onBuy)
                            ArcadeSecondaryButton(AppI18n.text("sell_1", language), Modifier.weight(1f), onClick = onSell)
                        }
                        ArcadePrimaryButton(
                            text = if (rewardedAvailable) AppI18n.text("rewarded_claim", language) else "${AppI18n.text("rewarded_cooldown", language)} ${rewardedCooldownSec}s",
                            enabled = rewardedAvailable,
                            onClick = onClaimRewarded
                        )
                        ArcadeSecondaryButton(AppI18n.text("play_paying_10", language), onClick = onOpenBallRoom)
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioHero(
    market: MarketUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val holdingChangePositive = market.totalHoldingChange >= 0.0
    val chartValues = remember(market.tokens, market.totalBalance) { market.portfolioHistory() }

    ArcadePanel(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppI18n.text("market_title", language),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = AppI18n.text("portfolio_subtitle", language),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
            StatusTicker(
                text = AppI18n.text("portfolio_title", language),
                modifier = Modifier.width(132.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 720.dp
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PortfolioMetricRow(market, language, holdingChangePositive)
                    PortfolioChartSurface(chartValues, holdingChangePositive)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    PortfolioMetricRow(
                        market = market,
                        language = language,
                        positive = holdingChangePositive,
                        modifier = Modifier.weight(0.95f)
                    )
                    PortfolioChartSurface(
                        values = chartValues,
                        positive = holdingChangePositive,
                        modifier = Modifier.weight(1.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioMetricRow(
    market: MarketUiState,
    language: AppLanguage,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PortfolioMetric(
                label = AppI18n.text("balance_total", language),
                value = formatCurrency(market.totalBalance),
                accent = Gold,
                modifier = Modifier.weight(1f)
            )
            PortfolioMetric(
                label = AppI18n.text("token_value", language),
                value = formatCurrency(market.holdingsValue),
                accent = Gold,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PortfolioMetric(
                label = AppI18n.text("portfolio_change", language),
                value = formatCurrency(market.totalHoldingChange),
                accent = if (positive) Gold else ChipRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PortfolioMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(8.dp),
        color = NightBlue.copy(alpha = 0.50f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = value,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun PortfolioChartSurface(
    values: List<Double>,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(8.dp),
        color = NightBlue.copy(alpha = 0.58f)
    ) {
        TokenSparkChart(
            values = values,
            lineColor = if (positive) Gold else ChipRed,
            glowColor = (if (positive) Gold else ChipRed).copy(alpha = 0.18f),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Composable
private fun SelectedTokenPanel(
    token: MarketToken,
    market: MarketUiState,
    resetCountdown: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val positive = token.changePercent >= 0
    val holdingPositive = token.holdingChangeValue >= 0.0
    val accent = tokenAccent(token)

    ArcadePanel(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = AppI18n.text("selected_token", language),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = token.displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = token.ticker,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = accent,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                )
            }
            StatusTicker(
                text = "${AppI18n.text("reset", language)}: $resetCountdown",
                modifier = Modifier.width(148.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TokenMetric(
                label = AppI18n.text("price", language),
                value = formatCurrency(token.currentPrice),
                detail = formatPercent(token.changePercent),
                detailColor = if (positive) Gold else ChipRed,
                modifier = Modifier.weight(1f)
            )
            TokenMetric(
                label = AppI18n.text("units", language),
                value = formatQuantity(token.holdings),
                detail = "${AppI18n.text("weight", language)} ${String.format(Locale.US, "%.1f%%", token.portfolioWeightPercent)}",
                detailColor = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            TokenMetric(
                label = AppI18n.text("contribution", language),
                value = formatCurrency(token.portfolioValue),
                detail = formatCurrency(token.holdingChangeValue),
                detailColor = if (holdingPositive) Gold else ChipRed,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(8.dp),
            color = NightBlue.copy(alpha = 0.58f)
        ) {
            TokenSparkChart(
                values = token.history,
                lineColor = accent,
                glowColor = accent.copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${AppI18n.text("token_value", language)}: ${formatCurrency(market.holdingsValue)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "${AppI18n.text("balance_total", language)}: ${formatCurrency(market.totalBalance)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun TokenMetric(
    label: String,
    value: String,
    detail: String,
    detailColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(8.dp),
        color = DeepBlue.copy(alpha = 0.48f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = value,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = detail,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = detailColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun TokenSidePanel(
    tokens: List<MarketToken>,
    selectedId: TokenId,
    language: AppLanguage,
    onSelectToken: (TokenId) -> Unit,
    modifier: Modifier = Modifier
) {
    ArcadePanel(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = AppI18n.text("side_panel", language),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = AppI18n.text("token_value", language),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tokens.forEach { token ->
                TokenListItem(
                    token = token,
                    selected = token.id == selectedId,
                    language = language,
                    onClick = { onSelectToken(token.id) }
                )
            }
        }
    }
}

@Composable
private fun TokenListItem(
    token: MarketToken,
    selected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val changePositive = token.changePercent >= 0
    val contributionPositive = token.holdingChangeValue >= 0
    val accent = tokenAccent(token)
    val containerColor = if (selected) Gold.copy(alpha = 0.16f) else DeepBlue.copy(alpha = 0.42f)
    val borderColor = if (selected) accent else PureWhite.copy(alpha = 0.12f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = token.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${token.ticker} · ${formatQuantity(token.holdings)} ${AppI18n.text("units", language).lowercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(token.portfolioValue),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = formatCurrency(token.holdingChangeValue),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (contributionPositive) Gold else ChipRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AppI18n.text("price", language)} ${formatCurrency(token.currentPrice)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Text(
                    text = formatPercent(token.changePercent),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (changePositive) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun BallRoomTab(
    ballRoom: BallRoomUiState,
    cashBalance: Double,
    onEnterRoom: () -> Unit,
    onPickBall: (Int) -> Unit,
    onRevealMultipliers: () -> Unit,
    onOpenBattle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = AppI18n.text("ball_draw"),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            ArcadePanel {
                Text(
                    text = "${AppI18n.text("entry")}: ${GameRules.BALL_ENTRY_COST.toInt()} FTC | ${AppI18n.text("room")}: ${GameRules.ROOM_SIZE} ${AppI18n.text("players").lowercase()} | ${AppI18n.text("balls")}: ${GameRules.BALL_COUNT}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${AppI18n.text("hidden_multipliers")} ${AppI18n.text("between")} x${GameRules.MULTIPLIER_MIN} y x${GameRules.MULTIPLIER_MAX.toInt()}.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(6.dp))
                StatusTicker(
                    text = AppI18n.message(ballRoom.statusMessage) ?: ballRoom.statusMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 42.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${AppI18n.text("current_balance")}: ${formatCurrency(cashBalance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (ballRoom.phase == BallRoomPhase.WAITING_ENTRY) {
            item {
                ArcadePrimaryButton(
                    text = AppI18n.text("pay_open_room"),
                    onClick = onEnterRoom
                )
            }
        } else {
            item {
                ArcadePanel {
                    Text(
                        text = AppI18n.text("players"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ballRoom.players.forEach { player ->
                            PlayerBallStatusItem(
                                nickname = player.nickname,
                                isUser = player.isUser,
                                ballId = player.selectedBallId,
                                multiplier = player.multiplier
                            )
                        }
                    }
                }
            }

            item {
                ArcadePanel {
                    Text(
                        text = AppI18n.text("choose_ball"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    BallGrid(
                        balls = ballRoom.balls,
                        phase = ballRoom.phase,
                        onPickBall = onPickBall
                    )
                }
            }

            if (ballRoom.phase == BallRoomPhase.PICKING && ballRoom.canRevealBattle) {
                item {
                    ArcadePrimaryButton(
                        text = AppI18n.text("reveal_multipliers"),
                        onClick = onRevealMultipliers
                    )
                }
            }

            if (ballRoom.phase == BallRoomPhase.REVEALED) {
                item {
                    ArcadePrimaryButton(
                        text = AppI18n.text("go_battle"),
                        onClick = onOpenBattle
                    )
                }
            }
        }
    }
}

@Composable
private fun BallGrid(
    balls: List<BallOption>,
    phase: BallRoomPhase,
    onPickBall: (Int) -> Unit
) {
    val engine = GameEngine()

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = true
    ) {
        items(balls.size) { index ->
            val ball = balls[index]
            val isUserPick = ball.pickedBy == GameRules.USER_PLAYER_ID
            val enabled = !ball.isPicked && phase == BallRoomPhase.PICKING

            Surface(
                modifier = Modifier
                    .size(58.dp)
                    .clickable(enabled = enabled) { onPickBall(ball.id) },
                shape = CircleShape,
                color = when {
                    isUserPick -> Gold
                    ball.isPicked -> ChipRed.copy(alpha = 0.75f)
                    else -> DeepBlue
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Gold.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val label = when {
                        phase == BallRoomPhase.REVEALED && ball.isPicked -> "x${engine.formatMultiplier(ball.multiplier)}"
                        else -> ball.id.toString()
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isUserPick) NightBlue else PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBallStatusItem(
    nickname: String,
    isUser: Boolean,
    ballId: Int?,
    multiplier: Double?
) {
    val border = if (isUser) Gold else PureWhite.copy(alpha = 0.18f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DeepBlue.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isUser) "$nickname (${AppI18n.text("you_suffix")})" else nickname,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
            )

            val status = when {
                multiplier != null -> "x${"%.2f".format(Locale.US, multiplier)}"
                ballId != null -> "${AppI18n.text("ball")} $ballId"
                else -> AppI18n.text("no_ball")
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (multiplier != null) Gold else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun BattleTab(
    battle: BattleUiState,
    onSelectAction: (BattleCardType) -> Unit,
    onPlayRound: () -> Unit,
    onResetCycle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = AppI18n.text("battle_title"),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            ArcadePanel {
                val alive = battle.players.count { it.isAlive }
                Text(
                    text = "${AppI18n.text("round")} ${battle.round} | ${AppI18n.text("alive")}: $alive",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (battle.phase == BattlePhase.IN_PROGRESS) {
                    Spacer(Modifier.height(6.dp))
                    BattleRoundCountdown(battle = battle)
                }
                Spacer(Modifier.height(8.dp))
                if (battle.phase == BattlePhase.LOCKED) {
                    Text(
                        text = AppI18n.text("battle_locked_hint"),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                } else {
                    Text(
                        text = "${AppI18n.text("initial_hp_rule")}: ${GameRules.BATTLE_INITIAL_HP}. ${AppI18n.text("attacks_between")} ${GameRules.BATTLE_ATTACK_MIN} y ${GameRules.BATTLE_ATTACK_MAX}.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        if (battle.phase != BattlePhase.LOCKED) {
            item {
                ArcadePanel {
                    Text(
                        text = AppI18n.text("choose_card"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionCardButton(
                            label = AppI18n.text("attack"),
                            selected = battle.selectedAction == BattleCardType.ATTACK,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectAction(BattleCardType.ATTACK) }
                        )
                        ActionCardButton(
                            label = AppI18n.text("defense"),
                            selected = battle.selectedAction == BattleCardType.SHIELD,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectAction(BattleCardType.SHIELD) }
                        )
                        ActionCardButton(
                            label = AppI18n.text("rebound"),
                            selected = battle.selectedAction == BattleCardType.REBOUND,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectAction(BattleCardType.REBOUND) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    ArcadePrimaryButton(
                        text = if (battle.phase == BattlePhase.FINISHED) AppI18n.text("battle_finished") else AppI18n.text("play_round"),
                        enabled = battle.phase != BattlePhase.FINISHED,
                        onClick = onPlayRound
                    )
                }
            }

            if (battle.interstitialAvailable && battle.phase != BattlePhase.FINISHED) {
                item {
                    ArcadePanel {
                        Text(
                            text = AppI18n.text("interstitial_ready"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gold,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            item {
                ArcadePanel {
                    Text(
                        text = AppI18n.text("player_status"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        battle.players.forEach { player ->
                            BattlePlayerItem(player = player)
                        }
                    }
                }
            }

            item {
                ArcadePanel {
                    Text(
                        text = AppI18n.text("combat_log"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    val lines = battle.log.takeLast(10).reversed()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        lines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }
        }

        if (battle.phase == BattlePhase.FINISHED) {
            item {
                ArcadePanel {
                    BubbleText(
                        text = battle.winnerName?.let { "${AppI18n.text("winner")}: $it" } ?: AppI18n.text("no_winner"),
                        style = MaterialTheme.typography.headlineMedium,
                        fillColor = Gold,
                        outlineColor = DeepBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (battle.winningMultiplier != null) {
                            "${AppI18n.text("market_impact_applied")} con x${"%.2f".format(Locale.US, battle.winningMultiplier)}."
                        } else {
                            AppI18n.text("no_market_impact")
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite)
                    )
                    Spacer(Modifier.height(10.dp))
                    ArcadeSecondaryButton(
                        text = AppI18n.text("new_cycle"),
                        onClick = onResetCycle
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleRoundCountdown(battle: BattleUiState) {
    var remaining by remember(battle.roundDeadlineEpochMs) {
        mutableStateOf(
            battle.roundDeadlineEpochMs
                ?.let { ((it - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0) }
                ?: 0
        )
    }

    LaunchedEffect(battle.roundDeadlineEpochMs) {
        while (battle.roundDeadlineEpochMs != null) {
            remaining = ((battle.roundDeadlineEpochMs - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
            delay(1000)
        }
    }

    val submitted = battle.submittedActions ?: 0
    val total = battle.aliveHumans ?: battle.players.count { it.isUser && it.isAlive }
    Text(
        text = "Tiempo ronda: ${remaining}s | Acciones: $submitted/$total",
        style = MaterialTheme.typography.bodySmall.copy(
            color = if (remaining <= 5) ChipRed else Gold,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun ActionCardButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        ArcadePrimaryButton(
            text = label,
            modifier = modifier,
            onClick = onClick
        )
    } else {
        ArcadeSecondaryButton(
            text = label,
            modifier = modifier,
            onClick = onClick
        )
    }
}

@Composable
private fun BattlePlayerItem(player: BattlePlayer) {
    val hpFraction = (player.hp.toFloat() / GameRules.BATTLE_INITIAL_HP.toFloat()).coerceIn(0f, 1f)
    val hpColor = when {
        player.hp <= 0 -> ChipRed.copy(alpha = 0.8f)
        player.hp <= 15 -> ChipRed
        player.hp <= 30 -> Gold
        else -> Gold
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DeepBlue.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (player.isUser) Gold else PureWhite.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (player.isUser) "${player.nickname} (${AppI18n.text("you_suffix")})" else player.nickname,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "${player.hp}/${GameRules.BATTLE_INITIAL_HP} HP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (player.hp > 0) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NightBlue)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(hpFraction)
                        .clip(RoundedCornerShape(50))
                        .background(hpColor)
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "${AppI18n.text("multiplier")}: x${"%.2f".format(Locale.US, player.multiplier)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun LegacyProfileTab(
    profile: ProfileUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = AppI18n.text("nav_profile"),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            ArcadePanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!profile.profilePicUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.profilePicUrl,
                            contentDescription = AppI18n.text("profile_pic"),
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(3.dp, GoldDark, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Gold)
                                .border(3.dp, GoldDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.username.take(1).uppercase(Locale.US),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = NightBlue,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BubbleText(
                            text = profile.username.ifBlank { profile.playerName },
                            style = MaterialTheme.typography.headlineMedium,
                            fillColor = PureWhite,
                            outlineColor = DeepBlue
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "${AppI18n.text("role")}: ${profile.role}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = AppI18n.text("edit_profile"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.editUsername,
                    label = AppI18n.text("username"),
                    leading = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    onValueChange = onUsernameChange
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.editEmail,
                    label = AppI18n.text("email"),
                    leading = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    onValueChange = onEmailChange
                )
                Spacer(Modifier.height(10.dp))
                ArcadePrimaryButton(
                    text = if (profile.isSavingProfile) AppI18n.text("saving") else AppI18n.text("save_changes"),
                    enabled = !profile.isSavingProfile,
                    onClick = onSaveProfile
                )
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = AppI18n.text("change_password"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.currentPassword,
                    label = AppI18n.text("current_password"),
                    leading = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    onValueChange = onCurrentPasswordChange
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.newPassword,
                    label = AppI18n.text("new_password"),
                    leading = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    onValueChange = onNewPasswordChange
                )
                Spacer(Modifier.height(8.dp))
                ArcadeTextField(
                    value = profile.confirmPassword,
                    label = AppI18n.text("repeat_password"),
                    leading = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    onValueChange = onConfirmPasswordChange
                )
                Spacer(Modifier.height(10.dp))
                ArcadePrimaryButton(
                    text = if (profile.isSavingPassword) AppI18n.text("updating") else AppI18n.text("update_password"),
                    enabled = !profile.isSavingPassword,
                    onClick = onChangePassword
                )
            }
        }

        item {
            ArcadeSecondaryButton(
                text = AppI18n.text("logout"),
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun ArcadeTextField(
    value: String,
    label: String,
    leading: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leading, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite,
            focusedBorderColor = Gold,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextSecondary,
            focusedLeadingIconColor = Gold,
            unfocusedLeadingIconColor = TextSecondary
        )
    )
}

@Composable
private fun RuleLine(text: String) {
    Text(
        text = "- $text",
        style = MaterialTheme.typography.bodySmall.copy(
            color = TextSecondary,
            lineHeight = 18.sp
        )
    )
}

@Composable
private fun BottomGameNav(
    activeTab: MainTab,
    language: AppLanguage,
    onSelect: (MainTab) -> Unit
) {
    val items = listOf(
        BottomItem(MainTab.DASHBOARD, AppI18n.text("nav_market", language), Icons.Default.Home),
        BottomItem(MainTab.BALL_ROOM, AppI18n.text("nav_balls", language), Icons.Default.Casino),
        BottomItem(MainTab.PROFILE, AppI18n.text("nav_profile", language), Icons.Default.Person)
    )

    ArcadePanel(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val selected = item.tab == activeTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable { onSelect(item.tab) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) Gold.copy(alpha = 0.28f) else PanelBlue.copy(alpha = 0.70f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                1.dp,
                                if (selected) Gold else PureWhite.copy(alpha = 0.12f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) Gold else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selected) PureWhite else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

private data class BottomItem(
    val tab: MainTab,
    val label: String,
    val icon: ImageVector
)

private fun tokenAccent(token: MarketToken): Color {
    return runCatching { Color(android.graphics.Color.parseColor(token.colorCode)) }
        .getOrElse {
            when (token.id) {
                TokenId.ROJA -> ChipRed
                TokenId.AZUL -> Color(0xFF4DA3FF)
                TokenId.VERDE -> Color(0xFF2AD97E)
                TokenId.DORADA -> Gold
            }
        }
}

private fun tokenInitial(tokenId: TokenId): String {
    return when (tokenId) {
        TokenId.ROJA -> "R"
        TokenId.AZUL -> "A"
        TokenId.VERDE -> "V"
        TokenId.DORADA -> "D"
    }
}

private fun MarketUiState.portfolioHistory(): List<Double> {
    val maxPoints = tokens.maxOfOrNull { it.history.size } ?: 0
    if (maxPoints == 0) {
        return listOf(totalBalance)
    }

    val values = (0 until maxPoints).map { index ->
        val tokenValue = tokens.sumOf { token ->
            val offset = maxPoints - token.history.size
            val historyIndex = (index - offset).coerceAtLeast(0)
            val price = token.history.getOrNull(historyIndex) ?: token.currentPrice
            price * token.holdings
        }
        tokenValue
    }
    return if (values.all { it <= 0.0 } && totalBalance > 0.0) {
        List(maxPoints) { totalBalance }
    } else {
        values
    }
}

private fun formatNotificationTime(raw: String): String {
    return raw
        .replace("T", " ")
        .replace("Z", "")
        .take(16)
        .ifBlank { "Ahora" }
}

@Composable
private fun GameBackgroundPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val horizontalSpacing = 56.dp.toPx()
        var y = horizontalSpacing
        while (y < size.height) {
            drawLine(
                color = PureWhite.copy(alpha = 0.035f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            y += horizontalSpacing
        }

        val verticalSpacing = 120.dp.toPx()
        var x = verticalSpacing
        while (x < size.width) {
            drawLine(
                color = Gold.copy(alpha = 0.025f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            x += verticalSpacing
        }
    }
}

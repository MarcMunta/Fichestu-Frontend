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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
                    listOf(NightBlue, DeepBlue, Color(0xFF2A145B))
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
                totalBalance = uiState.market.totalBalance,
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
                                    cashBalance = uiState.market.cashBalance,
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
    totalBalance: Double,
    notificationCount: Int,
    notificationsOpen: Boolean,
    language: AppLanguage,
    onToggleNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    ArcadePanel(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Gold)
                    .border(2.dp, GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!profilePicUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePicUrl,
                        contentDescription = AppI18n.text("change_photo", language),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = playerName.take(1).uppercase(Locale.US),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NightBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                BubbleText(
                    text = playerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fillColor = PureWhite,
                    outlineColor = DeepBlue
                )
                Text(
                    text = "${AppI18n.text("balance_total", language)}: ${formatCurrency(totalBalance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            NotificationBell(
                count = notificationCount,
                isOpen = notificationsOpen,
                onClick = onToggleNotifications
            )

            Surface(
                modifier = Modifier
                    .width(106.dp)
                    .height(52.dp)
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(8.dp),
                color = PanelBlue.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Gold.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
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
private fun NotificationBell(
    count: Int,
    isOpen: Boolean,
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
                    contentDescription = "Notificaciones",
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
                                    text = notification.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = PureWhite,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = notification.message,
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
    val selected = market.selectedMarketToken ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        item {
            BubbleText(
                text = AppI18n.text("market_title", language),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 30.sp),
                fillColor = Gold,
                outlineColor = DeepBlue
            )
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 760.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SelectedTokenPanel(
                            token = selected,
                            resetCountdown = market.resetCountdownLabel,
                            language = language,
                            modifier = Modifier.weight(1.5f)
                        )
                        TokenSidePanel(
                            tokens = market.tokens,
                            selectedId = market.selectedToken,
                            language = language,
                            onSelectToken = onSelectToken,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SelectedTokenPanel(
                            token = selected,
                            resetCountdown = market.resetCountdownLabel,
                            language = language,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TokenSidePanel(
                            tokens = market.tokens,
                            selectedId = market.selectedToken,
                            language = language,
                            onSelectToken = onSelectToken,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = AppI18n.text("quick_actions", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ArcadePrimaryButton(
                        text = AppI18n.text("buy_1", language),
                        modifier = Modifier.weight(1f),
                        onClick = onBuy
                    )
                    ArcadeSecondaryButton(
                        text = AppI18n.text("sell_1", language),
                        modifier = Modifier.weight(1f),
                        onClick = onSell
                    )
                }
                Spacer(Modifier.height(10.dp))
                ArcadePrimaryButton(
                    text = if (rewardedAvailable) {
                        "REWARDED +25 FTC"
                    } else {
                        "REWARDED ${rewardedCooldownSec}s"
                    },
                    enabled = rewardedAvailable,
                    onClick = onClaimRewarded
                )
                Spacer(Modifier.height(10.dp))
                ArcadeSecondaryButton(
                    text = AppI18n.text("play_paying_10", language),
                    onClick = onOpenBallRoom
                )
            }
        }

        item {
            ArcadePanel {
                Text(
                    text = AppI18n.text("market_rules", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                RuleLine(AppI18n.text("market_rule_1", language))
                RuleLine(AppI18n.text("market_rule_2", language))
                RuleLine(AppI18n.text("market_rule_3", language))
                RuleLine(AppI18n.text("market_rule_4", language))
            }
        }
    }
}

@Composable
private fun SelectedTokenPanel(
    token: MarketToken,
    resetCountdown: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val positive = token.changePercent >= 0

    ArcadePanel(modifier = modifier) {
        BubbleText(
            text = token.displayName,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
            fillColor = Gold,
            outlineColor = DeepBlue
        )
        Text(
            text = token.ticker,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatCurrency(token.currentPrice),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = formatPercent(token.changePercent),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (positive) Gold else ChipRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            StatusTicker(
                text = "${AppI18n.text("reset", language)}: $resetCountdown",
                modifier = Modifier.width(148.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(18.dp),
            color = NightBlue.copy(alpha = 0.58f)
        ) {
            TokenSparkChart(
                values = token.history,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${AppI18n.text("holdings", language)}: ${token.holdings}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "${AppI18n.text("value", language)}: ${formatCurrency(token.portfolioValue)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gold,
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
    ArcadePanel(modifier = modifier) {
        Text(
            text = AppI18n.text("side_panel", language),
            style = MaterialTheme.typography.titleMedium.copy(
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tokens.forEach { token ->
                TokenListItem(
                    token = token,
                    selected = token.id == selectedId,
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
    onClick: () -> Unit
) {
    val changePositive = token.changePercent >= 0
    val containerColor = if (selected) Gold.copy(alpha = 0.16f) else DeepBlue.copy(alpha = 0.42f)
    val borderColor = if (selected) Gold else PureWhite.copy(alpha = 0.12f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    text = token.ticker,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(token.currentPrice),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
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
                    text = ballRoom.statusMessage,
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
    selectedTokenId: TokenId,
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
                    text = "${AppI18n.text("round")} ${battle.round} | ${AppI18n.text("alive")}: $alive | ${AppI18n.text("target_token")}: ${selectedTokenId.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
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
        val spacing = 84.dp.toPx()
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = ChipRed.copy(alpha = 0.10f),
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            x += spacing
        }

        val spacing2 = 110.dp.toPx()
        var x2 = 0f
        while (x2 < size.width + size.height) {
            drawLine(
                color = Gold.copy(alpha = 0.05f),
                start = Offset(x2, 0f),
                end = Offset(x2 - size.height, size.height),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            x2 += spacing2
        }
    }
}

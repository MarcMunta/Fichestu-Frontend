package com.fichestu.frontend.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.fichestu.frontend.game.model.BadgeUi
import com.fichestu.frontend.game.model.ProfileStats
import com.fichestu.frontend.game.model.ProfileUiState
import com.fichestu.frontend.ui.theme.AliveGreen
import com.fichestu.frontend.ui.theme.CardBorder
import com.fichestu.frontend.ui.theme.ChipRed
import com.fichestu.frontend.ui.theme.ChipRedDark
import com.fichestu.frontend.ui.theme.DeepBlue
import com.fichestu.frontend.ui.theme.Gold
import com.fichestu.frontend.ui.theme.GoldBorder
import com.fichestu.frontend.ui.theme.GoldDark
import com.fichestu.frontend.ui.theme.GoldLight
import com.fichestu.frontend.ui.theme.InputBg
import com.fichestu.frontend.ui.theme.InputBorder
import com.fichestu.frontend.ui.theme.NightBlue
import com.fichestu.frontend.ui.theme.PanelBlue
import com.fichestu.frontend.ui.theme.PureWhite
import com.fichestu.frontend.ui.theme.ReflectPurple
import com.fichestu.frontend.ui.theme.ShieldBlue
import com.fichestu.frontend.ui.theme.TextSecondary
import java.util.Locale

/**
 * ProfileTab — versión rediseñada con estética casino premium adaptada a TABLET.
 *
 * Layout en 2 columnas para tablet:
 *  - Columna izquierda: Hero Card + Logout
 *  - Columna derecha: Stats + Insignias (botón) + Editar perfil + Cambiar contraseña
 *
 * Botón INSIGNIAS abre un Dialog con TODAS las insignias del catálogo,
 * mostrando bloqueadas y desbloqueadas con estilos distintos por rareza.
 */
@Composable
fun ProfileTab(
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
    var editProfileExpanded by remember { mutableStateOf(false) }
    var changePasswordExpanded by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedAvatarIndex by remember { mutableStateOf(-1) }
    var selectedFrameIndex by remember { mutableStateOf(0) }
    var selectedScreenBgIndex by remember { mutableStateOf(0) }
    var screenBgPhotoUri by remember { mutableStateOf<String?>(null) }

    val screenBg = ScreenBackgrounds.getOrElse(selectedScreenBgIndex) { ScreenBackgrounds.first() }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── FONDO DE PANTALLA PERSONALIZABLE ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(screenBg.colors))
        )
        if (!screenBgPhotoUri.isNullOrBlank()) {
            AsyncImage(
                model = screenBgPhotoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay oscuro para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            // ── LAYOUT TABLET: 2 columnas ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Columna izquierda — Hero + Logout
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .widthIn(min = 320.dp, max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileHeroCard(
                        profile = profile,
                        large = true,
                        selectedPresetIndex = selectedAvatarIndex,
                        backgroundIndex = selectedFrameIndex,
                        onChangePicture = { showAvatarDialog = true }
                    )
                    BadgesButton(
                        unlockedCount = profile.badges.count { it.unlocked },
                        totalCount = AllBadgesCatalog.size,
                        onClick = { showBadgesDialog = true }
                    )
                    LogoutButton(onClick = onLogout)
                }

                // Columna derecha — resto de paneles
                Column(
                    modifier = Modifier.weight(0.58f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatsGrid(stats = profile.stats)

                    ExpandableSection(
                        title = "Editar perfil",
                        icon = Icons.Default.Edit,
                        expanded = editProfileExpanded,
                        onToggle = { editProfileExpanded = !editProfileExpanded }
                    ) { ProfileEditForm(profile, onUsernameChange, onEmailChange, onSaveProfile) }

                    ExpandableSection(
                        title = "Cambiar contraseña",
                        icon = Icons.Default.Lock,
                        expanded = changePasswordExpanded,
                        onToggle = { changePasswordExpanded = !changePasswordExpanded }
                    ) { PasswordForm(profile, onCurrentPasswordChange, onNewPasswordChange, onConfirmPasswordChange, onChangePassword) }

                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            // ── LAYOUT COMPACTO: 1 columna (fallback móvil) ──────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                item {
                    ProfileHeroCard(
                        profile = profile,
                        large = false,
                        selectedPresetIndex = selectedAvatarIndex,
                        backgroundIndex = selectedFrameIndex,
                        onChangePicture = { showAvatarDialog = true }
                    )
                }
                item {
                    BadgesButton(
                        unlockedCount = profile.badges.count { it.unlocked },
                        totalCount = AllBadgesCatalog.size,
                        onClick = { showBadgesDialog = true }
                    )
                }
                item { StatsGrid(stats = profile.stats) }
                item {
                    ExpandableSection(
                        title = "Editar perfil",
                        icon = Icons.Default.Edit,
                        expanded = editProfileExpanded,
                        onToggle = { editProfileExpanded = !editProfileExpanded }
                    ) { ProfileEditForm(profile, onUsernameChange, onEmailChange, onSaveProfile) }
                }
                item {
                    ExpandableSection(
                        title = "Cambiar contraseña",
                        icon = Icons.Default.Lock,
                        expanded = changePasswordExpanded,
                        onToggle = { changePasswordExpanded = !changePasswordExpanded }
                    ) { PasswordForm(profile, onCurrentPasswordChange, onNewPasswordChange, onConfirmPasswordChange, onChangePassword) }
                }
                item { LogoutButton(onClick = onLogout) }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    // ── DIALOG DE INSIGNIAS ──────────────────────────────────────────────
    if (showBadgesDialog) {
        BadgesDialog(
            unlockedTitles = profile.badges.filter { it.unlocked }.map { it.title }.toSet(),
            onDismiss = { showBadgesDialog = false }
        )
    }

    // ── DIALOG DE AVATAR ─────────────────────────────────────────────────
    if (showAvatarDialog) {
        AvatarPickerDialog(
            currentSelection = selectedAvatarIndex,
            currentFrame = selectedFrameIndex,
            currentScreenBg = selectedScreenBgIndex,
            hasPhotoBg = !screenBgPhotoUri.isNullOrBlank(),
            currentInitial = profile.username.ifBlank { profile.playerName }.take(1).uppercase(Locale.US),
            onPick = { idx ->
                selectedAvatarIndex = idx
            },
            onPickFrame = { idx ->
                selectedFrameIndex = idx
            },
            onPickScreenBg = { idx ->
                selectedScreenBgIndex = idx
                screenBgPhotoUri = null
            },
            onUploadAvatarFromGallery = {
                showAvatarDialog = false
            },
            onUploadScreenBgFromGallery = {
                // Hook visual — cuando enganches el ActivityResult, asigna la URI aquí.
                // Por ahora, simulamos el estado:
                screenBgPhotoUri = "" // backend rellenará con URI real
            },
            onDismiss = { showAvatarDialog = false }
        )
    }
    } // cierre Box exterior fondo
}

// ══════════════════════════════════════════════════════════════════════════
// FORM EXTRACTS
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun ProfileEditForm(
    profile: ProfileUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSaveProfile: () -> Unit
) {
    ProfileTextField(
        value = profile.editUsername,
        label = "Username",
        leading = Icons.Default.Person,
        keyboardType = KeyboardType.Text,
        onValueChange = onUsernameChange
    )
    Spacer(Modifier.height(10.dp))
    ProfileTextField(
        value = profile.editEmail,
        label = "Email",
        leading = Icons.Default.Email,
        keyboardType = KeyboardType.Email,
        onValueChange = onEmailChange
    )
    Spacer(Modifier.height(14.dp))
    ArcadePrimaryButton(
        text = if (profile.isSavingProfile) "GUARDANDO..." else "GUARDAR CAMBIOS",
        enabled = !profile.isSavingProfile,
        onClick = onSaveProfile,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordForm(
    profile: ProfileUiState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit
) {
    ProfileTextField(
        value = profile.currentPassword,
        label = "Contraseña actual (si tienes una)",
        leading = Icons.Default.Lock,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        onValueChange = onCurrentPasswordChange
    )
    Spacer(Modifier.height(10.dp))
    ProfileTextField(
        value = profile.newPassword,
        label = "Nueva contraseña",
        leading = Icons.Default.Lock,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        onValueChange = onNewPasswordChange
    )
    Spacer(Modifier.height(10.dp))
    ProfileTextField(
        value = profile.confirmPassword,
        label = "Repite contraseña",
        leading = Icons.Default.Lock,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        onValueChange = onConfirmPasswordChange
    )
    Spacer(Modifier.height(14.dp))
    ArcadePrimaryButton(
        text = if (profile.isSavingPassword) "ACTUALIZANDO..." else "ACTUALIZAR CONTRASEÑA",
        enabled = !profile.isSavingPassword,
        onClick = onChangePassword,
        modifier = Modifier.fillMaxWidth()
    )
}

// ══════════════════════════════════════════════════════════════════════════
// HERO CARD
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun ProfileHeroCard(
    profile: ProfileUiState,
    large: Boolean,
    selectedPresetIndex: Int = -1,
    backgroundIndex: Int = 0,
    onChangePicture: () -> Unit = {}
) {
    val shape = RoundedCornerShape(16.dp)
    val avatarSize = if (large) 140.dp else 110.dp
    val nameFont = if (large) 30.sp else 24.sp
    val bg = HeroBackgrounds.getOrElse(backgroundIndex) { HeroBackgrounds.first() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Gold.copy(alpha = 0.30f),
                spotColor = Gold.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(colors = bg.colors)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(bg.borderColor, CardBorder, bg.borderColor)
                ),
                shape = shape
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().clip(shape)) {
            val spacing = 32.dp.toPx()
            val slope = 0.7f
            var startX = -size.height * slope
            while (startX < size.width + spacing) {
                drawLine(
                    color = bg.borderColor.copy(alpha = 0.06f),
                    start = Offset(startX, 0f),
                    end = Offset(startX + size.height * slope, size.height),
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                startX += spacing
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (large) 24.dp else 18.dp, vertical = if (large) 28.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AvatarWithHalo(
                    profilePicUrl = profile.profilePicUrl,
                    username = profile.username.ifBlank { profile.playerName },
                    size = avatarSize,
                    presetIndex = selectedPresetIndex
                )
                // Botón flotante cámara
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp, bottom = 6.dp)
                        .size(if (large) 44.dp else 38.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Gold)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
                        )
                        .border(2.dp, NightBlue, CircleShape)
                        .clickable { onChangePicture() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = NightBlue,
                        modifier = Modifier.size(if (large) 22.dp else 18.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            BubbleText(
                text = profile.username.ifBlank { profile.playerName }.ifBlank { "Jugador" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = nameFont,
                    fontWeight = FontWeight.ExtraBold
                ),
                fillColor = Gold,
                outlineColor = DeepBlue,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            if (profile.email.isNotBlank()) {
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        letterSpacing = 0.4.sp
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleChip(role = profile.role)
                VerifiedChip()
            }
        }
    }
}

@Composable
private fun AvatarWithHalo(
    profilePicUrl: String?,
    username: String,
    size: Dp,
    presetIndex: Int = -1
) {
    val infinite = rememberInfiniteTransition(label = "halo")
    val rotation by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Restart),
        label = "halo_rotation"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2_400, easing = LinearEasing), RepeatMode.Reverse),
        label = "halo_pulse"
    )

    Box(modifier = Modifier.size(size + 28.dp), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.size(size + 22.dp).scale(pulse).rotate(rotation)
        ) {
            val ringRadius = this.size.minDimension / 2f - 4.dp.toPx()
            val dotCount = 36
            repeat(dotCount) { i ->
                val angle = (i.toFloat() / dotCount) * (2.0 * Math.PI).toFloat()
                val x = this.size.width / 2f + kotlin.math.cos(angle) * ringRadius
                val y = this.size.height / 2f + kotlin.math.sin(angle) * ringRadius
                val isAccent = i % 6 == 0
                drawCircle(
                    color = if (isAccent) Gold else GoldDark.copy(alpha = 0.55f),
                    radius = if (isAccent) 2.6.dp.toPx() else 1.4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(size + 8.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.28f), Gold.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 10.dp, shape = CircleShape, spotColor = Gold)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(GoldLight, Gold, GoldDark)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (presetIndex in PresetAvatars.indices) {
                PresetAvatarVisual(
                    preset = PresetAvatars[presetIndex],
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else if (!profilePicUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profilePicUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(Locale.US).ifBlank { "?" },
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = NightBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (size.value * 0.48f).sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    val display = role.ifBlank { "USER" }.uppercase(Locale.US)
    val isAdmin = display.contains("ADMIN")
    val accent = if (isAdmin) ChipRed else Gold
    val bg = if (isAdmin) ChipRedDark.copy(alpha = 0.35f) else GoldDark.copy(alpha = 0.25f)

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isAdmin) Icons.Default.Bolt else Icons.Default.Star,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = display,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isAdmin) PureWhite else Gold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            )
        }
    }
}

@Composable
private fun VerifiedChip() {
    Surface(
        shape = RoundedCornerShape(50),
        color = ShieldBlue.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, ShieldBlue.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Verified, null, tint = ShieldBlue, modifier = Modifier.size(14.dp))
            Text(
                text = "VERIFICADO",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ShieldBlue, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp
                )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// STATS GRID
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun StatsGrid(stats: ProfileStats) {
    val winRate = if (stats.battlesPlayed == 0) 0
    else ((stats.battlesWon.toFloat() / stats.battlesPlayed) * 100).toInt()

    ArcadePanel(contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.TrendingUp, null, tint = Gold, modifier = Modifier.size(18.dp))
            Text(
                text = "ESTADÍSTICAS",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                )
            )
        }
        Spacer(Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatChip(Modifier.weight(1f), "PARTIDAS", stats.battlesPlayed.toString(), Icons.Default.Casino, Gold)
            StatChip(Modifier.weight(1f), "VICTORIAS", stats.battlesWon.toString(), Icons.Default.EmojiEvents, AliveGreen)
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatChip(Modifier.weight(1f), "MEJOR x", "x${"%.2f".format(Locale.US, stats.bestMultiplier)}", Icons.Default.Bolt, ChipRed)
            StatChip(Modifier.weight(1f), "WIN RATE", "$winRate%", Icons.Default.Shield, ShieldBlue)
        }

        if (stats.ballRoomsPlayed > 0) {
            Spacer(Modifier.height(14.dp))
            ProgressRow(
                label = "Salas de bolas",
                value = stats.ballRoomsPlayed,
                max = (stats.ballRoomsPlayed.coerceAtLeast(10) / 10 + 1) * 10,
                color = ReflectPurple
            )
        }
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .heightIn(min = 88.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(InputBg, NightBlue.copy(alpha = 0.92f))))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.15f))
                ),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f))
                        .border(1.dp, accent.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp, fontSize = 10.sp
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp
                )
            )
        }
    }
}

@Composable
private fun ProgressRow(label: String, value: Int, max: Int, color: Color) {
    val pct = (value.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondary, fontWeight = FontWeight.SemiBold
            ))
            Text("$value / $max", style = MaterialTheme.typography.labelMedium.copy(
                color = color, fontWeight = FontWeight.ExtraBold
            ))
        }
        Spacer(Modifier.height(6.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(InputBg)
                .border(1.dp, InputBorder.copy(alpha = 0.6f), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(fraction = pct)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color)))
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// BADGES BUTTON + DIALOG
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun BadgesButton(unlockedCount: Int, totalCount: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GoldDark.copy(alpha = 0.30f),
                        ReflectPurple.copy(alpha = 0.22f),
                        GoldDark.copy(alpha = 0.30f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(Gold, ReflectPurple, Gold)),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono trofeo con halo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GoldLight, Gold, GoldDark)))
                    .border(2.dp, GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MilitaryTech, null, tint = NightBlue, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "INSIGNIAS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                    )
                )
                Text(
                    text = "$unlockedCount / $totalCount desbloqueadas · Toca para ver todas",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary, fontWeight = FontWeight.Medium
                    )
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NightBlue.copy(alpha = 0.6f))
                    .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "VER →",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                    )
                )
            }
        }
    }
}

// ── Catálogo de TODAS las insignias (visual, sin backend) ─────────────────
private enum class BadgeRarity { COMMON, RARE, EPIC, LEGENDARY }

private data class CatalogBadge(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val rarity: BadgeRarity
)

private val AllBadgesCatalog: List<CatalogBadge> = listOf(
    CatalogBadge("Primera partida", "Juega tu primera batalla", Icons.Default.Casino, BadgeRarity.COMMON),
    CatalogBadge("Primera victoria", "Gana tu primera batalla", Icons.Default.EmojiEvents, BadgeRarity.COMMON),
    CatalogBadge("Bienvenido", "Crea tu cuenta en Fichestu", Icons.Default.Person, BadgeRarity.COMMON),
    CatalogBadge("Multiplicador x3", "Consigue x3 en una bola", Icons.Default.Bolt, BadgeRarity.RARE),
    CatalogBadge("Veterano", "Juega 100 partidas", Icons.Default.MilitaryTech, BadgeRarity.RARE),
    CatalogBadge("Defensor", "Bloquea 25 ataques con escudo", Icons.Default.Shield, BadgeRarity.RARE),
    CatalogBadge("Racha de fuego", "Gana 5 batallas seguidas", Icons.Default.LocalFireDepartment, BadgeRarity.EPIC),
    CatalogBadge("Gran apostador", "Acumula 10000 fichas", Icons.Default.WorkspacePremium, BadgeRarity.EPIC),
    CatalogBadge("Multiplicador x5", "Consigue x5 en una bola", Icons.Default.Bolt, BadgeRarity.EPIC),
    CatalogBadge("Superviviente", "Termina batalla con 1 HP", Icons.Default.Favorite, BadgeRarity.EPIC),
    CatalogBadge("Leyenda viva", "Gana 50 batallas", Icons.Default.Star, BadgeRarity.LEGENDARY),
    CatalogBadge("Rey del Casino", "Top 1 del ranking semanal", Icons.Default.MilitaryTech, BadgeRarity.LEGENDARY)
)

private fun rarityAccent(r: BadgeRarity): Color = when (r) {
    BadgeRarity.COMMON -> ShieldBlue
    BadgeRarity.RARE -> AliveGreen
    BadgeRarity.EPIC -> ReflectPurple
    BadgeRarity.LEGENDARY -> Gold
}

private fun rarityLabel(r: BadgeRarity): String = when (r) {
    BadgeRarity.COMMON -> "COMÚN"
    BadgeRarity.RARE -> "RARA"
    BadgeRarity.EPIC -> "ÉPICA"
    BadgeRarity.LEGENDARY -> "LEGENDARIA"
}

@Composable
private fun BadgesDialog(unlockedTitles: Set<String>, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val isWide = maxWidth >= 720.dp
            val dialogWidth = if (isWide) 760.dp else maxWidth - 24.dp
            val dialogMaxHeight = maxHeight - 32.dp
            val shape = RoundedCornerShape(20.dp)

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(220)) + scaleIn(tween(260), initialScale = 0.92f),
                exit = fadeOut(tween(180)) + scaleOut(tween(180))
            ) {
                Box(
                    modifier = Modifier
                        .width(dialogWidth)
                        .heightIn(max = dialogMaxHeight)
                        .shadow(elevation = 30.dp, shape = shape, spotColor = Gold)
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    PanelBlue,
                                    DeepBlue,
                                    PanelBlue
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(GoldBorder, CardBorder, GoldBorder)),
                            shape = shape
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { /* consumir clic */ }
                ) {
                    // Patrón decorativo
                    Canvas(modifier = Modifier.fillMaxSize().clip(shape)) {
                        val spacing = 40.dp.toPx()
                        val slope = 0.7f
                        var startX = -size.height * slope
                        while (startX < size.width + spacing) {
                            drawLine(
                                color = Gold.copy(alpha = 0.04f),
                                start = Offset(startX, 0f),
                                end = Offset(startX + size.height * slope, size.height),
                                strokeWidth = 1.4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            startX += spacing
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GoldLight, Gold, GoldDark)))
                                    .border(2.dp, GoldDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MilitaryTech, null, tint = NightBlue, modifier = Modifier.size(26.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                BubbleText(
                                    text = "Insignias",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold, fontSize = 26.sp
                                    ),
                                    fillColor = Gold,
                                    outlineColor = DeepBlue
                                )
                                Text(
                                    text = "${unlockedTitles.size} de ${AllBadgesCatalog.size} desbloqueadas",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary, letterSpacing = 0.4.sp
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NightBlue.copy(alpha = 0.7f))
                                    .border(1.dp, Gold.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, "Cerrar", tint = Gold, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Gold.copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                        )
                        Spacer(Modifier.height(14.dp))

                        // Grid de badges
                        val columns = if (isWide) 3 else 2
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = dialogMaxHeight - 140.dp)
                        ) {
                            items(AllBadgesCatalog) { badge ->
                                CatalogBadgeCard(
                                    badge = badge,
                                    unlocked = unlockedTitles.contains(badge.title)
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
private fun CatalogBadgeCard(badge: CatalogBadge, unlocked: Boolean) {
    val accent = rarityAccent(badge.rarity)
    val shape = RoundedCornerShape(14.dp)

    val cardBg = if (unlocked) {
        Brush.verticalGradient(
            listOf(
                accent.copy(alpha = 0.18f),
                PanelBlue.copy(alpha = 0.85f),
                accent.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(InputBg, NightBlue.copy(alpha = 0.92f))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 170.dp)
            .clip(shape)
            .background(cardBg)
            .border(
                width = 1.5.dp,
                brush = if (unlocked) {
                    Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.4f), accent))
                } else {
                    Brush.linearGradient(listOf(InputBorder, InputBorder.copy(alpha = 0.4f)))
                },
                shape = shape
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono con anillo de rareza
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    // Halo difuso
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(accent.copy(alpha = 0.45f), Color.Transparent)
                                )
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (unlocked) {
                                Brush.linearGradient(
                                    listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.5f))
                                )
                            } else {
                                Brush.linearGradient(listOf(InputBg, PanelBlue))
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (unlocked) accent else InputBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (unlocked) badge.icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (unlocked) NightBlue else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = if (unlocked) PureWhite else TextSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Chip de rareza
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (unlocked) accent.copy(alpha = 0.20f)
                        else InputBg
                    )
                    .border(
                        width = 1.dp,
                        color = if (unlocked) accent.copy(alpha = 0.7f) else InputBorder,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = rarityLabel(badge.rarity),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (unlocked) accent else TextSecondary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// EXPANDABLE SECTION
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevron_rotation"
    )

    ArcadePanel(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldDark.copy(alpha = 0.22f))
                    .border(1.dp, Gold.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Gold, modifier = Modifier.size(18.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = PureWhite, fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Cerrar" else "Abrir",
                tint = Gold,
                modifier = Modifier.size(26.dp)
            )
            @Suppress("UNUSED_EXPRESSION") rotation
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(220)) + expandVertically(tween(260)),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(220))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Gold.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
                Spacer(Modifier.height(14.dp))
                content()
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PROFILE TEXT FIELD
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun ProfileTextField(
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
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(leading, null, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite,
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg.copy(alpha = 0.85f),
            focusedBorderColor = Gold,
            unfocusedBorderColor = InputBorder,
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextSecondary,
            focusedLeadingIconColor = Gold,
            unfocusedLeadingIconColor = TextSecondary,
            cursorColor = Gold
        )
    )
}

// ══════════════════════════════════════════════════════════════════════════
// LOGOUT BUTTON
// ══════════════════════════════════════════════════════════════════════════
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)

    Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .clip(shape)
                .background(ChipRedDark.copy(alpha = 0.85f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape)
                .background(
                    Brush.horizontalGradient(listOf(ChipRedDark, ChipRed, ChipRedDark))
                )
                .border(1.5.dp, ChipRed.copy(alpha = 0.65f), shape)
                .clickable { onClick() }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Logout, null, tint = PureWhite, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "CERRAR SESIÓN",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = PureWhite, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// AVATAR PRESETS + PICKER DIALOG
// ══════════════════════════════════════════════════════════════════════════

// ══════════════════════════════════════════════════════════════════════════
// HERO BACKGROUNDS
// ══════════════════════════════════════════════════════════════════════════
private data class HeroBackground(
    val name: String,
    val colors: List<Color>,
    val borderColor: Color
)

// ══════════════════════════════════════════════════════════════════════════
// SCREEN BACKGROUNDS (toda la pantalla del perfil)
// ══════════════════════════════════════════════════════════════════════════
private data class ScreenBackground(
    val name: String,
    val colors: List<Color>,
    val accent: Color
)

private val ScreenBackgrounds: List<ScreenBackground> = listOf(
    ScreenBackground("Default",       listOf(NightBlue, DeepBlue, Color(0xFF2A145B)), Gold),
    ScreenBackground("Velvet",        listOf(Color(0xFF1A0420), Color(0xFF4A0E5C), Color(0xFF1A0420)), ReflectPurple),
    ScreenBackground("Forest",        listOf(Color(0xFF021A0F), Color(0xFF0A4A2A), Color(0xFF021A0F)), AliveGreen),
    ScreenBackground("Crimson Felt",  listOf(Color(0xFF1A0210), Color(0xFF4A0A24), Color(0xFF1A0210)), ChipRed),
    ScreenBackground("Ocean",         listOf(Color(0xFF021430), Color(0xFF0A4A8A), Color(0xFF021430)), ShieldBlue),
    ScreenBackground("Bourbon",       listOf(Color(0xFF2A1505), Color(0xFF6E3A0A), Color(0xFF2A1505)), GoldDark),
    ScreenBackground("Slate",         listOf(Color(0xFF0A0F1A), Color(0xFF1A2238), Color(0xFF0A0F1A)), Color(0xFF8AA0C4)),
    ScreenBackground("Sunrise",       listOf(Color(0xFF2A0A1A), Color(0xFF7A2E1A), Color(0xFFB85A0E)), Color(0xFFFFB347)),
    ScreenBackground("Pure Black",    listOf(Color.Black, Color(0xFF0A0A0A), Color.Black), Gold),
    ScreenBackground("Royal Purple",  listOf(Color(0xFF1B0A2F), Color(0xFF3A1A6E), Color(0xFF1B0A2F)), GoldLight),
    ScreenBackground("Carbon",        listOf(Color(0xFF0F0F12), Color(0xFF1F1F24), Color(0xFF0F0F12)), Color(0xFFB0B8C8)),
    ScreenBackground("Cherry",        listOf(Color(0xFF2A0414), ChipRedDark, Color(0xFF2A0414)), Gold)
)

private val HeroBackgrounds: List<HeroBackground> = listOf(
    HeroBackground(
        "Casino",
        listOf(PanelBlue.copy(alpha = 0.96f), DeepBlue.copy(alpha = 0.96f), PanelBlue.copy(alpha = 0.96f)),
        Gold
    ),
    HeroBackground(
        "Royal Gold",
        listOf(GoldDark.copy(alpha = 0.55f), Color(0xFF3A2A05), GoldDark.copy(alpha = 0.55f)),
        Gold
    ),
    HeroBackground(
        "Crimson",
        listOf(Color(0xFF3A0A1E), ChipRedDark, Color(0xFF3A0A1E)),
        ChipRed
    ),
    HeroBackground(
        "Emerald",
        listOf(Color(0xFF062B1A), Color(0xFF0E5A35), Color(0xFF062B1A)),
        AliveGreen
    ),
    HeroBackground(
        "Cyber Blue",
        listOf(Color(0xFF071E36), Color(0xFF0A3A66), Color(0xFF071E36)),
        ShieldBlue
    ),
    HeroBackground(
        "Mystic Purple",
        listOf(Color(0xFF180A2F), Color(0xFF3A1A6E), Color(0xFF180A2F)),
        ReflectPurple
    ),
    HeroBackground(
        "Sunset",
        listOf(Color(0xFF3A1410), Color(0xFF7A2A1A), Color(0xFF3A1410)),
        Color(0xFFFF8A4D)
    ),
    HeroBackground(
        "Midnight",
        listOf(NightBlue, Color(0xFF030610), NightBlue),
        Color(0xFF6BA0FF)
    )
)

@Composable
private fun BackgroundSwatchTile(
    bg: HeroBackground,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(shape)
                .background(Brush.linearGradient(bg.colors))
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    brush = if (selected) {
                        Brush.linearGradient(listOf(bg.borderColor, bg.borderColor.copy(alpha = 0.4f), bg.borderColor))
                    } else {
                        Brush.linearGradient(listOf(InputBorder, InputBorder.copy(alpha = 0.5f)))
                    },
                    shape = shape
                )
        ) {
            // Líneas decorativas idénticas a las del Hero
            Canvas(modifier = Modifier.fillMaxSize().clip(shape)) {
                val spacing = 16.dp.toPx()
                val slope = 0.7f
                var startX = -size.height * slope
                while (startX < size.width + spacing) {
                    drawLine(
                        color = bg.borderColor.copy(alpha = 0.18f),
                        start = Offset(startX, 0f),
                        end = Offset(startX + size.height * slope, size.height),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    startX += spacing
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(AliveGreen)
                        .border(2.dp, NightBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = NightBlue, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = bg.name,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (selected) bg.borderColor else PureWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontSize = 11.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

private data class PresetAvatar(
    val name: String,
    val emoji: String,
    val gradient: List<Color>,
    val ringColor: Color
)

private val PresetAvatars: List<PresetAvatar> = listOf(
    PresetAvatar("Gold King",   "👑", listOf(GoldLight, Gold, GoldDark),                      Gold),
    PresetAvatar("Pyro",        "🔥", listOf(Color(0xFFFFB347), ChipRed, ChipRedDark),        ChipRed),
    PresetAvatar("Ice Shark",   "🦈", listOf(Color(0xFF7FE0FF), ShieldBlue, Color(0xFF1B4F7A)), ShieldBlue),
    PresetAvatar("Mage",        "🧙", listOf(Color(0xFFC4A6FF), ReflectPurple, Color(0xFF4C2A8A)), ReflectPurple),
    PresetAvatar("Lucky",       "🍀", listOf(Color(0xFF8BFFAE), AliveGreen, Color(0xFF146B3A)), AliveGreen),
    PresetAvatar("Joker",       "🃏", listOf(Color(0xFFFF8FB1), ChipRed, ReflectPurple),      ChipRed),
    PresetAvatar("Diamond",     "💎", listOf(Color(0xFFCFEFFF), ShieldBlue, ReflectPurple),   ShieldBlue),
    PresetAvatar("Cyborg",      "🤖", listOf(Color(0xFFB0B8C8), Color(0xFF6F7B91), DeepBlue), Color(0xFFB0B8C8)),
    PresetAvatar("Phoenix",     "🦅", listOf(Color(0xFFFFD56B), ChipRed, GoldDark),           GoldDark),
    PresetAvatar("Bandit",      "🦝", listOf(Color(0xFFC9C9C9), Color(0xFF555E73), NightBlue), Color(0xFF8A93AA)),
    PresetAvatar("Dragon",      "🐲", listOf(Color(0xFF8FFFB6), AliveGreen, Color(0xFF1F5F37)), AliveGreen),
    PresetAvatar("Vampire",     "🧛", listOf(Color(0xFFFF7A9E), ChipRedDark, NightBlue),      ChipRedDark)
)

@Composable
private fun PresetAvatarVisual(preset: PresetAvatar, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Brush.linearGradient(preset.gradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = preset.emoji,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun AvatarPickerDialog(
    currentSelection: Int,
    currentFrame: Int,
    currentScreenBg: Int,
    hasPhotoBg: Boolean,
    currentInitial: String,
    onPick: (Int) -> Unit,
    onPickFrame: (Int) -> Unit,
    onPickScreenBg: (Int) -> Unit,
    onUploadAvatarFromGallery: () -> Unit,
    onUploadScreenBgFromGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val isWide = maxWidth >= 720.dp
            val dialogWidth = if (isWide) 720.dp else maxWidth - 24.dp
            val dialogMaxHeight = maxHeight - 32.dp
            val shape = RoundedCornerShape(20.dp)

            Box(
                modifier = Modifier
                    .width(dialogWidth)
                    .heightIn(max = dialogMaxHeight)
                    .shadow(elevation = 30.dp, shape = shape, spotColor = Gold)
                    .clip(shape)
                    .background(
                        Brush.linearGradient(listOf(PanelBlue, DeepBlue, PanelBlue))
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(GoldBorder, CardBorder, GoldBorder)),
                        shape = shape
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { /* consume */ }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().clip(shape)) {
                    val spacing = 40.dp.toPx()
                    val slope = 0.7f
                    var startX = -size.height * slope
                    while (startX < size.width + spacing) {
                        drawLine(
                            color = Gold.copy(alpha = 0.04f),
                            start = Offset(startX, 0f),
                            end = Offset(startX + size.height * slope, size.height),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        startX += spacing
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GoldLight, Gold, GoldDark)))
                                .border(2.dp, GoldDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = NightBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BubbleText(
                                text = "Cambiar foto",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold, fontSize = 26.sp
                                ),
                                fillColor = Gold,
                                outlineColor = DeepBlue
                            )
                            Text(
                                text = "Elige un avatar o sube uno desde tu galería",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary, letterSpacing = 0.4.sp
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NightBlue.copy(alpha = 0.7f))
                                .border(1.dp, Gold.copy(alpha = 0.5f), CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Gold, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Gold.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                    Spacer(Modifier.height(14.dp))

                    // Botón subir foto de avatar
                    UploadFromGalleryRow(
                        title = "Subir foto de avatar",
                        subtitle = "Elige una foto de tu galería",
                        onClick = onUploadAvatarFromGallery
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "AVATARES PRESET",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    val columns = if (isWide) 4 else 3
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)
                    ) {
                        items(PresetAvatars.size) { idx ->
                            PresetAvatarTile(
                                preset = PresetAvatars[idx],
                                selected = idx == currentSelection,
                                onClick = { onPick(idx) }
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "MARCO DEL AVATAR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    val frameCols = if (isWide) 4 else 3
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(frameCols),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                    ) {
                        items(HeroBackgrounds.size) { idx ->
                            BackgroundSwatchTile(
                                bg = HeroBackgrounds[idx],
                                selected = idx == currentFrame,
                                onClick = { onPickFrame(idx) }
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "FONDO DE PANTALLA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    UploadFromGalleryRow(
                        title = "Usar foto como fondo",
                        subtitle = "Sube una imagen desde tu galería",
                        onClick = onUploadScreenBgFromGallery
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (isWide) 4 else 3),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)
                    ) {
                        items(ScreenBackgrounds.size) { idx ->
                            BackgroundSwatchTile(
                                bg = HeroBackground(
                                    name = ScreenBackgrounds[idx].name,
                                    colors = ScreenBackgrounds[idx].colors,
                                    borderColor = ScreenBackgrounds[idx].accent
                                ),
                                selected = idx == currentScreenBg && !hasPhotoBg,
                                onClick = { onPickScreenBg(idx) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadFromGalleryRow(
    title: String = "Subir desde galería",
    subtitle: String = "Elige una foto de tu dispositivo",
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        ShieldBlue.copy(alpha = 0.20f),
                        PanelBlue.copy(alpha = 0.85f),
                        ShieldBlue.copy(alpha = 0.20f)
                    )
                )
            )
            .border(1.5.dp, ShieldBlue.copy(alpha = 0.7f), shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ShieldBlue, ShieldBlue.copy(alpha = 0.6f))
                        )
                    )
                    .border(2.dp, ShieldBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhotoLibrary, null, tint = NightBlue, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = PureWhite, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NightBlue.copy(alpha = 0.6f))
                    .border(1.dp, ShieldBlue.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ABRIR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ShieldBlue, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun PresetAvatarTile(
    preset: PresetAvatar,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(
                        listOf(
                            preset.ringColor.copy(alpha = 0.25f),
                            PanelBlue.copy(alpha = 0.9f),
                            preset.ringColor.copy(alpha = 0.18f)
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(InputBg, NightBlue.copy(alpha = 0.92f)))
                }
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                brush = if (selected) {
                    Brush.linearGradient(listOf(preset.ringColor, preset.ringColor.copy(alpha = 0.4f), preset.ringColor))
                } else {
                    Brush.linearGradient(listOf(InputBorder, InputBorder.copy(alpha = 0.5f)))
                },
                shape = shape
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(preset.ringColor.copy(alpha = 0.45f), Color.Transparent)
                                )
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(preset.gradient))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(preset.ringColor, preset.ringColor.copy(alpha = 0.5f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.emoji,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(AliveGreen)
                            .border(2.dp, NightBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = NightBlue, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (selected) preset.ringColor else PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

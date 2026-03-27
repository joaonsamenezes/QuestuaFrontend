package com.questua.app.presentation.exploration.city

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.City
import com.questua.app.domain.model.QuestPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestPoint: (String) -> Unit,
    onNavigateToUnlock: (String, String) -> Unit,
    viewModel: CityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPositionState = rememberCameraPositionState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshData()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val mapProperties = remember(state.city) {
        var bounds: LatLngBounds? = null

        state.city?.boundingPolygon?.coordinates?.let { coords ->
            if (coords.isNotEmpty()) {
                val builder = LatLngBounds.builder()
                coords.forEach { point ->
                    if (point.size >= 2) builder.include(LatLng(point[0], point[1]))
                }
                bounds = builder.build()
            }
        }

        MapProperties(
            mapStyleOptions = try { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style) } catch (e: Exception) { null },
            latLngBoundsForCameraTarget = bounds,
            minZoomPreference = 12f,
            maxZoomPreference = 20f,
            isMyLocationEnabled = false
        )
    }

    LaunchedEffect(state.city) {
        state.city?.let { city ->
            cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lon), 14f))
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 120.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        sheetContent = {
            state.city?.let { city ->
                CityBottomSheetContent(city = city, achievements = state.cityAchievements)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading && state.city == null) {
                com.questua.app.core.ui.components.LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false,
                        rotationGesturesEnabled = false
                    )
                ) {
                    state.city?.boundingPolygon?.coordinates?.let { rawCoords ->
                        val polygonPoints = rawCoords.mapNotNull {
                            if (it.size >= 2) LatLng(it[0], it[1]) else null
                        }

                        if (polygonPoints.isNotEmpty()) {
                            Polygon(
                                points = polygonPoints,
                                strokeColor = primaryColor,
                                strokeWidth = 5f,
                                fillColor = primaryColor.copy(alpha = 0.15f)
                            )
                        }
                    }

                    state.questPoints.forEach { point ->
                        if (!point.isLocked) {
                            var imageLoaded by remember { mutableStateOf(false) }

                            MarkerComposable(
                                keys = arrayOf(point.id, point.iconUrl ?: "no_icon", imageLoaded),
                                state = MarkerState(position = LatLng(point.lat, point.lon)),
                                onClick = { return@MarkerComposable false }
                            ) {
                                QuestuaMapMarker(
                                    iconUrl = point.iconUrl,
                                    isLocked = false,
                                    onImageLoaded = { imageLoaded = true }
                                )
                            }

                            MarkerInfoWindow(
                                state = MarkerState(position = LatLng(point.lat, point.lon)),
                                anchor = Offset(0.5f, 0.0f),
                                onInfoWindowClick = { onNavigateToQuestPoint(point.id) }
                            ) {
                                SimpleInfoWindowContent(point = point)
                            }
                        }
                    }
                }

                SmallFloatingActionButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 24.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun QuestuaMapMarker(iconUrl: String?, isLocked: Boolean, onImageLoaded: () -> Unit) {
    val bgColor = if (isLocked) Color.DarkGray else MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(bgColor, shape = CircleShape)
                .padding(3.dp)
                .background(Color.White, shape = CircleShape)
                .padding(2.dp)
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!iconUrl.isNullOrBlank() && !isLocked) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(iconUrl.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    onSuccess = { onImageLoaded() }
                )
            } else {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = bgColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(y = (-6).dp)
                .size(width = 14.dp, height = 16.dp)
                .background(bgColor, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        )
    }
}

@Composable
fun SimpleInfoWindowContent(point: QuestPoint) {
    ElevatedCard(
        modifier = Modifier.width(220.dp).wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = point.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "EXPLORAR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CityBottomSheetContent(city: City, achievements: List<Achievement>) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = city.countryCode,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (!city.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(city.imageUrl.toFullImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = city.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            if (achievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Conquistas Locais",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(achievements) { achievement ->
            PendingAchievementCard(achievement)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PendingAchievementCard(achievement: Achievement) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.iconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(achievement.iconUrl.toFullImageUrl())
                            .crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
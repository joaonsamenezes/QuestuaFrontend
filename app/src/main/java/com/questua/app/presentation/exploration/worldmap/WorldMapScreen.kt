// app/src/main/java/com/questua/app/presentation/exploration/worldmap/WorldMapScreen.kt
package com.questua.app.presentation.exploration.worldmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.questua.app.R
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.domain.model.AdventurerTier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToCity: (String) -> Unit,
    onNavigateToUnlock: (String, String) -> Unit,
    onNavigateToTier: () -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedCity by remember { mutableStateOf<CityUiModel?>(null) }
    val listState = rememberLazyListState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
                selectedCity = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = try { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style) } catch (e: Exception) { null },
            isMyLocationEnabled = false,
            minZoomPreference = 2f,
            maxZoomPreference = 12f
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false, rotationGesturesEnabled = false)
    }

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
            WorldHubContent(
                cities = state.cities,
                selectedCityId = selectedCity?.city?.id,
                languageName = state.activeLanguage?.name ?: "Idioma",
                languageIconUrl = state.activeLanguage?.iconUrl,
                cefrLevel = state.activeCefrLevel,
                questsCompleted = state.completedQuestsCount,
                listState = listState,
                onCityClick = { cityUi, index ->
                    scope.launch {
                        selectedCity = cityUi
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(cityUi.city.lat, cityUi.city.lon), 8f), 600)
                    }
                },
                onAccess = { cityUi ->
                    if (!cityUi.city.isLocked && cityUi.isUnlocked) {
                        onNavigateToCity(cityUi.city.id)
                    } else if (!cityUi.isUnlocked && !cityUi.city.isLocked) {
                        onNavigateToUnlock(cityUi.city.id, "CITY")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                com.questua.app.core.ui.components.LoadingSpinner(modifier = Modifier.align(Alignment.Center))
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    onMapClick = {
                        selectedCity = null
                        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    }
                ) {
                    state.cities.forEachIndexed { index, cityUi ->
                        val city = cityUi.city
                        val isSelected = selectedCity?.city?.id == city.id

                        MarkerComposable(
                            keys = arrayOf(city.id, isSelected.toString(), cityUi.city.isLocked.toString()),
                            state = MarkerState(position = LatLng(city.lat, city.lon)),
                            onClick = {
                                scope.launch {
                                    selectedCity = cityUi
                                    scaffoldState.bottomSheetState.expand()
                                    listState.animateScrollToItem(index)
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(city.lat, city.lon), 8f), 600)
                                }
                                true
                            }
                        ) {
                            QuestuaPinMarker(
                                iconUrl = city.iconUrl,
                                cityName = city.name,
                                isSelected = isSelected,
                                isLocked = cityUi.city.isLocked
                            )
                        }
                    }
                }

                state.adventurerTier?.let { tier ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    ) {
                        AdventurerRankCard(tier = tier, onClick = onNavigateToTier)
                    }
                }

                if (onNavigateBack != null) {
                    SmallFloatingActionButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.align(Alignment.TopStart).padding(top = 48.dp, start = 16.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun AdventurerRankCard(tier: AdventurerTier, onClick: () -> Unit) {
    val tierColor = try { Color(android.graphics.Color.parseColor(tier.colorHex ?: "#6200EE")) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = tierColor,
            contentColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (tier.iconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(tier.iconUrl.toFullImageUrl())
                            .crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Rank de Aventureiro", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(text = tier.nameDisplay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

@Composable
fun LanguageSummaryCard(languageName: String, languageIconUrl: String?, cefrLevel: String, questsCompleted: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (languageIconUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(languageIconUrl.toFullImageUrl())
                                .crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = languageName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "$questsCompleted Missões Concluídas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = cefrLevel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun QuestuaPinMarker(iconUrl: String?, cityName: String, isSelected: Boolean = false, isLocked: Boolean = false) {
    val scale = if (isSelected) 1.25f else 1.0f
    val bgColor = if (isLocked) Color.DarkGray else MaterialTheme.colorScheme.primary
    val ringColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp * scale)
                .background(bgColor, shape = CircleShape)
                .padding(3.dp * scale)
                .background(ringColor, shape = CircleShape)
                .padding(2.dp * scale)
                .shadow(if (isSelected) 8.dp else 4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (iconUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(iconUrl.toFullImageUrl())
                        .crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    alpha = if (isLocked) 0.5f else 1.0f
                )
            } else {
                Icon(Icons.Default.LocationOn, null, tint = bgColor.copy(alpha = 0.5f))
            }

            if (isLocked) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp * scale))
                }
            }
        }

        Box(
            modifier = Modifier
                .offset(y = (-6).dp * scale)
                .size(width = 12.dp * scale, height = 14.dp * scale)
                .background(bgColor, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        )

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 6.dp,
            modifier = Modifier.offset(y = (-4).dp)
        ) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun WorldHubContent(
    cities: List<CityUiModel>,
    selectedCityId: String?,
    languageName: String,
    languageIconUrl: String?,
    cefrLevel: String,
    questsCompleted: Int,
    listState: LazyListState,
    onCityClick: (CityUiModel, Int) -> Unit,
    onAccess: (CityUiModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {

        LanguageSummaryCard(languageName = languageName, languageIconUrl = languageIconUrl, cefrLevel = cefrLevel, questsCompleted = questsCompleted)

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text("Destinos do Mundo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("${cities.count { it.isUnlocked }} de ${cities.size} cidades desbloqueadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(cities, key = { _, it -> it.city.id }) { index, cityUi ->
                CityHubCard(
                    cityUi = cityUi,
                    isSelected = cityUi.city.id == selectedCityId,
                    onClick = { onCityClick(cityUi, index) },
                    onActionClick = { onAccess(cityUi) }
                )
            }
        }
    }
}

@Composable
fun CityHubCard(
    cityUi: CityUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val city = cityUi.city
    val isCefrLocked = city.isLocked

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 10.dp else 3.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.fillMaxSize().background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                if (!city.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(city.imageUrl.toFullImageUrl()).crossfade(true).build(),
                        contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = if (isCefrLocked || !cityUi.isUnlocked) 0.5f else 1f
                    )
                }

                if (isCefrLocked || !cityUi.isUnlocked) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(city.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(city.countryCode, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onActionClick,
                    enabled = !isCefrLocked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (cityUi.isUnlocked) "ENTRAR" else "ABRIR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
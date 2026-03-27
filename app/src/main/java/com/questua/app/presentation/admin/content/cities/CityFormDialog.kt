package com.questua.app.presentation.admin.content.cities

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.*
import com.questua.app.domain.model.*
import com.questua.app.presentation.admin.content.ai.SelectorDialog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Composable
fun CityFormDialog(
    city: City? = null,
    languages: List<Language>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, desc: String, langId: String, poly: BoundingPolygon?, lat: Double, lon: Double, img: File?, icon: File?, prem: Boolean, unlock: UnlockRequirement?, ai: Boolean, pub: Boolean) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(city?.name ?: "") }
    var code by remember { mutableStateOf(city?.countryCode ?: "") }
    var desc by remember { mutableStateOf(city?.description ?: "") }
    var langId by remember { mutableStateOf(city?.languageId ?: "") }
    var lat by remember { mutableStateOf(city?.lat?.toString() ?: "") }
    var lon by remember { mutableStateOf(city?.lon?.toString() ?: "") }
    var isPremium by remember { mutableStateOf(city?.isPremium ?: false) }
    var isPublished by remember { mutableStateOf(city?.isPublished ?: true) }

    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var selectedIconFile by remember { mutableStateOf<File?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(null) }

    var polyText by remember { mutableStateOf(city?.boundingPolygon?.let { Json.encodeToString(it.coordinates) } ?: "[]") }
    var reqPremium by remember { mutableStateOf(city?.unlockRequirement?.premiumAccess ?: false) }
    var reqLevel by remember { mutableStateOf(city?.unlockRequirement?.requiredGamificationLevel?.toString() ?: "") }
    var reqCefr by remember { mutableStateOf(city?.unlockRequirement?.requiredCefrLevel ?: "") }

    var showLangPicker by remember { mutableStateOf(false) }

    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var latError by remember { mutableStateOf<String?>(null) }
    var lonError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            selectedImageFile = URIPathHelper.getFileFromUri(context, it)
        }
    }
    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedIconUri = it
            selectedIconFile = URIPathHelper.getFileFromUri(context, it)
        }
    }

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) { nameError = "Obrigatório"; isValid = false } else nameError = null
        if (code.length != 2) { codeError = "Código ISO deve ter 2 letras (ex: BR)"; isValid = false } else codeError = null

        if (lat.isNotBlank() && lat.toDoubleOrNull() == null) {
            latError = "Número inválido"
            isValid = false
        } else latError = null

        if (lon.isNotBlank() && lon.toDoubleOrNull() == null) {
            lonError = "Número inválido"
            isValid = false
        } else lonError = null

        return isValid
    }

    QuestuaBaseFormDialog(
        title = if (city == null) "Nova Cidade" else "Editar Cidade",
        onDismiss = onDismiss,
        onConfirm = {
            if (validate()) {
                val poly = try { BoundingPolygon(Json.decodeFromString(polyText)) } catch (e: Exception) { null }
                val unlock = UnlockRequirement(
                    premiumAccess = reqPremium,
                    requiredGamificationLevel = reqLevel.toIntOrNull(),
                    requiredCefrLevel = reqCefr.ifEmpty { null }
                )
                onConfirm(name, code, desc, langId, poly, lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0, selectedImageFile, selectedIconFile, isPremium, unlock, false, isPublished)
            }
        },
        confirmEnabled = name.isNotBlank() && langId.isNotBlank() && code.isNotBlank()
    ) {
        FormSectionTitle("Identidade Visual")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ImageSelectorBox(
                modifier = Modifier.weight(1f),
                label = "Capa",
                uri = selectedImageUri,
                fallbackUrl = city?.imageUrl,
                icon = Icons.Default.AddPhotoAlternate,
                onClick = { imagePicker.launch("image/*") }
            )
            ImageSelectorBox(
                modifier = Modifier.weight(1f),
                label = "Ícone",
                uri = selectedIconUri,
                fallbackUrl = city?.iconUrl,
                icon = Icons.Default.Image,
                contentScale = ContentScale.Fit,
                onClick = { iconPicker.launch("image/*") }
            )
        }

        FormSectionTitle("Informações Gerais")
        QuestuaTextField(value = name, onValueChange = { name = it; nameError = null }, label = "Nome da Cidade", errorMessage = nameError)

        QuestuaSelectorField(
            label = "Idioma da Cidade",
            value = languages.find { it.id == langId }?.name ?: "Selecionar Idioma",
            icon = Icons.Default.Language,
            onClick = { showLangPicker = true },
            isPlaceholder = langId.isEmpty()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { QuestuaTextField(value = lat, onValueChange = { lat = it; latError = null }, label = "Latitude", errorMessage = latError) }
            Box(Modifier.weight(1f)) { QuestuaTextField(value = lon, onValueChange = { lon = it; lonError = null }, label = "Longitude", errorMessage = lonError) }
        }

        QuestuaTextField(value = code, onValueChange = { code = it.uppercase().take(2); codeError = null }, label = "Código País (ISO)", errorMessage = codeError)
        QuestuaTextField(value = desc, onValueChange = { desc = it }, label = "Descrição")
        QuestuaTextField(value = polyText, onValueChange = { polyText = it }, label = "Polígono Geográfico (JSON)")

        FormSectionTitle("Regras de Desbloqueio")
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reqPremium, onCheckedChange = { reqPremium = it })
                    Text("Exigir Assinatura Premium", style = MaterialTheme.typography.bodyMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { QuestuaTextField(value = reqLevel, onValueChange = { reqLevel = it.filter { char -> char.isDigit() } }, label = "Nível Mín.") }
                    Box(Modifier.weight(1f)) { QuestuaTextField(value = reqCefr, onValueChange = { reqCefr = it.uppercase().take(2) }, label = "CEFR (ex: B1)") }
                }
            }
        }

        FormSectionTitle("Status")
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it })
                    Text("Publicado", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPremium, onCheckedChange = { isPremium = it })
                    Text("Premium", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showLangPicker) {
        SelectorDialog(
            title = "Selecione o Idioma",
            items = languages,
            itemContent = { Text(it.name, fontWeight = FontWeight.Bold) },
            onSelect = { langId = it.id; showLangPicker = false },
            onDismiss = { showLangPicker = false }
        )
    }
}

@Composable
private fun ImageSelectorBox(
    modifier: Modifier,
    label: String,
    uri: Uri?,
    fallbackUrl: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val model = uri ?: fallbackUrl?.toFullImageUrl()
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
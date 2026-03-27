package com.questua.app.presentation.admin.content.adventurertiers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.QuestuaBaseFormDialog
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.AdventurerTier

@Composable
fun AdventurerTierFormDialog(
    tier: AdventurerTier? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?, String?, Int, Int) -> Unit
) {
    var keyName by remember { mutableStateOf(tier?.keyName ?: "") }
    var nameDisplay by remember { mutableStateOf(tier?.nameDisplay ?: "") }
    var colorHex by remember { mutableStateOf(tier?.colorHex ?: "") }
    var orderIndex by remember { mutableStateOf(tier?.orderIndex?.toString() ?: "") }
    var levelRequired by remember { mutableStateOf(tier?.levelRequired?.toString() ?: "") }

    // Validation
    var keyNameError by remember { mutableStateOf<String?>(null) }
    var nameDisplayError by remember { mutableStateOf<String?>(null) }
    var orderIndexError by remember { mutableStateOf<String?>(null) }
    var levelRequiredError by remember { mutableStateOf<String?>(null) }

    // Picker de Galeria
    var iconUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            iconUri = uri
        }
    }

    fun validate(): Boolean {
        var isValid = true
        if (keyName.isBlank()) { keyNameError = "Obrigatório"; isValid = false } else keyNameError = null
        if (nameDisplay.isBlank()) { nameDisplayError = "Obrigatório"; isValid = false } else nameDisplayError = null

        if (orderIndex.isNotBlank() && orderIndex.toIntOrNull() == null) {
            orderIndexError = "Valor inválido"; isValid = false
        } else orderIndexError = null

        if (levelRequired.isNotBlank() && levelRequired.toIntOrNull() == null) {
            levelRequiredError = "Valor inválido"; isValid = false
        } else levelRequiredError = null

        return isValid
    }

    QuestuaBaseFormDialog(
        title = if (tier == null) "Novo Rank" else "Editar Rank",
        onDismiss = onDismiss,
        onConfirm = {
            if (validate()) {
                onSave(
                    keyName,
                    nameDisplay,
                    iconUri,
                    colorHex.takeIf { it.isNotBlank() },
                    orderIndex.toIntOrNull() ?: 0,
                    levelRequired.toIntOrNull() ?: 1
                )
            }
        },
        confirmEnabled = keyName.isNotBlank() && nameDisplay.isNotBlank()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Container da imagem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (iconUri != null) {
                    AsyncImage(
                        model = iconUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!tier?.iconUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(tier!!.iconUrl!!.toFullImageUrl()).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Adicionar Ícone",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            QuestuaTextField(value = keyName, onValueChange = { keyName = it; keyNameError = null }, label = "Key Name (ex: BRONZE)", errorMessage = keyNameError)
            QuestuaTextField(value = nameDisplay, onValueChange = { nameDisplay = it; nameDisplayError = null }, label = "Nome de Exibição", errorMessage = nameDisplayError)
            QuestuaTextField(value = colorHex, onValueChange = { colorHex = it }, label = "Cor Hexadecimal (ex: #FFD700)")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestuaTextField(value = orderIndex, onValueChange = { orderIndex = it.filter { char -> char.isDigit() }; orderIndexError = null }, label = "Ordem", modifier = Modifier.weight(1f), errorMessage = orderIndexError)
                QuestuaTextField(value = levelRequired, onValueChange = { levelRequired = it.filter { char -> char.isDigit() }; levelRequiredError = null }, label = "Lvl Requerido", modifier = Modifier.weight(1f), errorMessage = levelRequiredError)
            }
        }
    }
}
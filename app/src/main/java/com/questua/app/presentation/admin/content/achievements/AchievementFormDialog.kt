package com.questua.app.presentation.admin.content.achievements

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.ui.components.FormSectionTitle
import com.questua.app.core.ui.components.QuestuaBaseFormDialog
import com.questua.app.core.ui.components.QuestuaEnumDropdown
import com.questua.app.core.ui.components.QuestuaSelectorField
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.AchievementConditionType
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.City
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.Quest
import com.questua.app.domain.model.QuestPoint
import com.questua.app.core.ui.components.*

@Composable
fun AchievementFormDialog(
    achievement: Achievement? = null,
    cities: List<City> = emptyList(),
    quests: List<Quest> = emptyList(),
    questPoints: List<QuestPoint> = emptyList(),
    products: List<Product> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        key: String, name: String, desc: String,
        icon: Any?,
        rarity: RarityType, xp: Int, isHidden: Boolean, isGlobal: Boolean,
        category: String, conditionType: AchievementConditionType, targetId: String,
        requiredAmount: Int
    ) -> Unit
) {
    val context = LocalContext.current

    var keyName by remember { mutableStateOf(achievement?.keyName ?: "") }
    var name by remember { mutableStateOf(achievement?.name ?: "") }
    var description by remember { mutableStateOf(achievement?.description ?: "") }
    var xpReward by remember { mutableStateOf(achievement?.xpReward?.toString() ?: "50") }
    var rarity by remember { mutableStateOf(achievement?.rarity ?: RarityType.COMMON) }
    var isHidden by remember { mutableStateOf(achievement?.isHidden ?: false) }
    var isGlobal by remember { mutableStateOf(achievement?.isGlobal ?: true) }
    var category by remember { mutableStateOf(achievement?.category ?: "") }
    var conditionType by remember { mutableStateOf(achievement?.conditionType ?: AchievementConditionType.COMPLETE_SPECIFIC_QUEST) }
    var targetId by remember { mutableStateOf(achievement?.targetId ?: "") }
    var requiredAmount by remember { mutableStateOf(achievement?.requiredAmount?.toString() ?: "1") }
    var selectedIcon by remember { mutableStateOf<Any?>(achievement?.iconUrl) }

    var expandedTarget by remember { mutableStateOf(false) }

    // Validation
    var nameError by remember { mutableStateOf<String?>(null) }
    var keyNameError by remember { mutableStateOf<String?>(null) }
    var xpRewardError by remember { mutableStateOf<String?>(null) }
    var requiredAmountError by remember { mutableStateOf<String?>(null) }

    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedIcon = URIPathHelper.getFileFromUri(context, it) }
    }

    val targetOptions = remember(conditionType, cities, quests, questPoints, products) {
        when {
            conditionType == AchievementConditionType.UNLOCK_PREMIUM_CONTENT -> products.map { it.id to it.title }
            conditionType.name.contains("CITY") -> cities.map { it.id to it.name }
            conditionType.name.contains("QUEST_POINT") -> questPoints.map { it.id to it.title }
            conditionType.name.contains("QUEST") -> quests.map { it.id to it.title }
            else -> emptyList()
        }
    }

    val selectedTargetName = targetOptions.find { it.first == targetId }?.second ?: targetId

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) { nameError = "Obrigatório"; isValid = false } else nameError = null
        if (keyName.isBlank()) { keyNameError = "Obrigatório"; isValid = false } else keyNameError = null

        if (xpReward.isNotBlank() && xpReward.toIntOrNull() == null) {
            xpRewardError = "Valor inválido"; isValid = false
        } else xpRewardError = null

        if (requiredAmount.isNotBlank() && requiredAmount.toIntOrNull() == null) {
            requiredAmountError = "Valor inválido"; isValid = false
        } else requiredAmountError = null

        return isValid
    }

    QuestuaBaseFormDialog(
        title = if (achievement == null) "Nova Conquista" else "Editar Conquista",
        onDismiss = onDismiss,
        onConfirm = {
            if (validate()) {
                onConfirm(
                    keyName, name, description, selectedIcon,
                    rarity, xpReward.toIntOrNull() ?: 0, isHidden, isGlobal,
                    category, conditionType, targetId, requiredAmount.toIntOrNull() ?: 1
                )
            }
        },
        confirmEnabled = name.isNotBlank() && keyName.isNotBlank()
    ) {
        FormSectionTitle("Informações Básicas")
        QuestuaTextField(value = name, onValueChange = { name = it; nameError = null }, label = "Nome da Conquista", errorMessage = nameError)
        QuestuaTextField(value = keyName, onValueChange = { keyName = it; keyNameError = null }, label = "Chave Única (ID interno)", errorMessage = keyNameError)
        QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                QuestuaTextField(value = xpReward, onValueChange = { xpReward = it.filter { char -> char.isDigit() }; xpRewardError = null }, label = "Recompensa (XP)", errorMessage = xpRewardError)
            }
            Box(modifier = Modifier.weight(1f)) {
                QuestuaEnumDropdown(
                    label = "Raridade",
                    options = RarityType.entries.toTypedArray(),
                    selectedOption = rarity,
                    onOptionSelected = { rarity = it }
                )
            }
        }

        FormSectionTitle("Regras e Condições")
        QuestuaEnumDropdown(
            label = "Condição de Desbloqueio",
            options = AchievementConditionType.entries.toTypedArray(),
            selectedOption = conditionType,
            onOptionSelected = {
                conditionType = it
                targetId = ""
            }
        )

        if (targetOptions.isNotEmpty()) {
            Box {
                QuestuaSelectorField(
                    label = "Alvo (Target)",
                    value = selectedTargetName.ifEmpty { "Selecionar..." },
                    onClick = { expandedTarget = true },
                    isPlaceholder = targetId.isEmpty()
                )
                DropdownMenu(
                    expanded = expandedTarget,
                    onDismissRequest = { expandedTarget = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    targetOptions.forEach { (id, title) ->
                        DropdownMenuItem(
                            text = { Text(title) },
                            onClick = {
                                targetId = id
                                expandedTarget = false
                            }
                        )
                    }
                }
            }
        } else {
            QuestuaTextField(value = targetId, onValueChange = { targetId = it }, label = "Target ID (Manual/Opcional)")
        }

        QuestuaTextField(value = requiredAmount, onValueChange = { requiredAmount = it.filter { char -> char.isDigit() }; requiredAmountError = null }, label = "Quantidade Necessária", errorMessage = requiredAmountError)
        QuestuaTextField(value = category, onValueChange = { category = it }, label = "Categoria")

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHidden, onCheckedChange = { isHidden = it })
                    Text("Oculto", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isGlobal, onCheckedChange = { isGlobal = it })
                    Text("Global", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        FormSectionTitle("Visual")
        MediaPickerField(
            label = "Ícone",
            value = selectedIcon,
            icon = Icons.Default.Image,
            onPick = { iconPicker.launch("image/*") },
            onClear = { selectedIcon = null }
        )
    }
}
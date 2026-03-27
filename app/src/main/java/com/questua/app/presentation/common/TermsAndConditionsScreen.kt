package com.questua.app.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Termos e Condições",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Última atualização: 16 de março de 2026",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TermSection(
                "1. Aceitação do Contrato",
                "Ao utilizar o Questua, você celebra um contrato vinculativo com a plataforma. O uso dos serviços implica na aceitação plena e sem reservas de todos os itens deste documento e da nossa Política de Privacidade."
            )

            TermSection(
                "2. Proteção de Dados (LGPD)",
                "Em conformidade com a Lei nº 13.709/2018, informamos que seus dados pessoais (nome, e-mail e foto) são utilizados exclusivamente para autenticação e personalização da experiência gamificada. Seus dados de geolocalização são processados apenas para validar a presença em pontos de missão e não são armazenados em nossos servidores de forma identificável após o encerramento da sessão de jogo."
            )

            TermSection(
                "3. Propriedade Intelectual",
                "Todos os direitos relativos ao Questua, incluindo software, design, textos educativos, diálogos e personagens, são de propriedade exclusiva da Questua. É vedada qualquer tentativa de engenharia reversa, cópia de conteúdo ou uso comercial não autorizado."
            )

            TermSection(
                "4. Regras de Conduta e Segurança",
                "O usuário compromete-se a utilizar o aplicativo de forma ética. Dado que o app incentiva a visita a locais físicos, o usuário declara estar ciente de que é o único responsável por sua segurança pessoal e pela observância das leis de trânsito e normas de segurança locais durante o uso da plataforma."
            )

            TermSection(
                "5. Foro",
                "Para dirimir quaisquer controvérsias oriundas deste termo, as partes elegem o foro da comarca do usuário, conforme previsto no Código de Defesa do Consumidor brasileiro."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TermSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
        Text(
            text = title,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
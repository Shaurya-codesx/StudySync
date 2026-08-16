package com.example.studysyncandroid.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studysyncandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDeveloperScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val name = "Shaurya Sharma"
    val title = "Kotlin Full-Stack Developer (Android + Ktor)"
    val tagline = "Making complex things simple. Designed and developed to make studying feel a little less stressful."
    
    val githubProfileUrl = "https://github.com/Shaurya-codesx"
    val githubRepoUrl = "https://github.com/Shaurya-codesx/StudySync"
    val emailAddress = "shaurya.shrma7@gmail.com"

    val openUrl = { url: String ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    val sendFeedback = {
        val subject = "StudySync App Feedback"
        val body = "\n\n\n--\nDevice Info:\nManufacturer: ${Build.MANUFACTURER}\nModel: ${Build.MODEL}\nAndroid Version: ${Build.VERSION.RELEASE}"
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Feedback via..."))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.deck_list_text_primary)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.deck_list_text_primary)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.deck_list_bg)
                )
            )
        },
        containerColor = colorResource(id = R.color.deck_list_bg)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Developer Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.deck_list_card_bg)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Developer Icon",
                    tint = colorResource(id = R.color.deck_list_accent),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.deck_list_text_primary)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.deck_list_accent),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.deck_list_card_bg)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "\"$tagline\"",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = colorResource(id = R.color.deck_list_text_secondary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            DeveloperActionButton(
                text = "GitHub Profile",
                icon = Icons.Default.Person,
                onClick = { openUrl(githubProfileUrl) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeveloperActionButton(
                text = "GitHub Repository",
                icon = Icons.Default.Code,
                onClick = { openUrl(githubRepoUrl) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeveloperActionButton(
                text = "Report a Bug / Feedback",
                icon = Icons.Default.BugReport,
                onClick = sendFeedback,
                isPrimary = true
            )
        }
    }
}

@Composable
fun DeveloperActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) colorResource(id = R.color.deck_list_accent) else colorResource(id = R.color.deck_list_card_bg),
            contentColor = if (isPrimary) colorResource(id = R.color.deck_list_bg) else colorResource(id = R.color.deck_list_text_primary)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isPrimary) 4.dp else 2.dp)
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = if (isPrimary) colorResource(id = R.color.deck_list_bg) else colorResource(id = R.color.deck_list_accent)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            fontSize = 16.sp, 
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

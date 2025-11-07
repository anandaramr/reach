package com.project.reach.ui.screens.discover


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.reach.data.respository.MessageRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.ui.components.AvatarIcon
import com.project.reach.ui.components.AvatarIconSize
import java.util.UUID
import kotlin.math.log

@Composable
fun OnlineUser(
    peer: Peer,
    navigateToChat : (String) -> Unit,
    saveContact : (Peer) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(10.dp)
            .clickable(
                onClick = {
                    saveContact(peer)
                    navigateToChat(peer.uuid)
                }
            )
    ) {
        AvatarIcon(peer.username[0], AvatarIconSize.SMALL)
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
        ) {
            Text(
                peer.username,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(23.dp))
        }
    }
}
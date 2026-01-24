/**
 * Copyright (c) 2025 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.screen.loggedIn.video.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.ui.actions.UrlUserTagTransformation
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.quartz.peertube.PeerTubeChannelConfig
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class PeerTubeChannelConfigViewModel(
    private val account: Account,
) {
    val peerTubeChannels: StateFlow<List<PeerTubeChannelConfig>> = account.settings.peerTubeChannels

    fun addChannel(
        channelName: String,
        instanceUrl: String,
    ): Boolean = account.settings.addPeerTubeChannel(channelName, instanceUrl)

    fun removeChannel(channel: PeerTubeChannelConfig) {
        account.settings.removePeerTubeChannel(channel)
    }
}

@Composable
fun PeerTubeChannelInputModal(
    accountViewModel: AccountViewModel,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val account = accountViewModel.account

    val channelName = remember { mutableStateOf("") }
    val instanceUrl = remember { mutableStateOf("") }

    val viewModel = remember { PeerTubeChannelConfigViewModel(account) }

    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = true),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.add_peertube_channel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                OutlinedTextField(
                    label = { Text(stringResource(R.string.channel_name)) },
                    value = channelName.value,
                    onValueChange = { channelName.value = it },
                    placeholder = { Text(stringResource(R.string.channel_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = UrlUserTagTransformation(MaterialTheme.colorScheme.primary),
                )

                OutlinedTextField(
                    label = { Text(stringResource(R.string.instance_url)) },
                    value = instanceUrl.value,
                    onValueChange = { instanceUrl.value = it },
                    placeholder = { Text(stringResource(R.string.instance_url_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = UrlUserTagTransformation(MaterialTheme.colorScheme.primary),
                )

                val normalizedInput =
                    remember(channelName.value) {
                        normalizeChannelInput(channelName.value)
                    }
                val derivedInstance = normalizedInput.derivedInstanceUrl
                val manualInstance = instanceUrl.value.takeIf { it.isNotBlank() }
                val instanceCandidate = manualInstance ?: derivedInstance
                val canAddChannel = normalizedInput.channelName.isNotBlank() && isValidInstanceUrl(instanceCandidate)

                LaunchedEffect(derivedInstance) {
                    if (manualInstance.isNullOrBlank() && !derivedInstance.isNullOrBlank()) {
                        instanceUrl.value = derivedInstance
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            val (normalizedChannel, newDerivedInstance) = normalizeChannelInput(channelName.value)
                            val targetInstance = instanceUrl.value.takeIf { it.isNotBlank() } ?: newDerivedInstance
                            if (normalizedChannel.isNotBlank() && targetInstance != null && isValidInstanceUrl(targetInstance)) {
                                if (viewModel.addChannel(normalizedChannel, targetInstance)) {
                                    channelName.value = ""
                                    instanceUrl.value = ""
                                }
                            }
                        },
                        enabled = canAddChannel,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_channel))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_channel))
                    }
                    Button(
                        onClick = onClose,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    ) {
                        Icon(Icons.Outlined.Cancel, contentDescription = stringResource(R.string.cancel))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.cancel))
                    }
                }

                HorizontalDivider()

                // List of current channels
                val currentChannels by viewModel.peerTubeChannels.collectAsStateWithLifecycle()
                Column(modifier = Modifier.fillMaxWidth()) {
                    currentChannels.forEach { channel ->
                        PeerTubeChannelRow(
                            channel = channel,
                            onRemove = { viewModel.removeChannel(channel) },
                            accountViewModel = accountViewModel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeerTubeChannelRow(
    channel: PeerTubeChannelConfig,
    onRemove: () -> Unit,
    accountViewModel: AccountViewModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.channelName,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = channel.instanceUrl,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Icon(
            Icons.Outlined.Cancel,
            contentDescription = stringResource(R.string.remove_channel),
            modifier = Modifier.clickable(onClick = onRemove).padding(start = 8.dp).size(20.dp),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
fun AddPeerTubeChannelButton(
    accountViewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    onAddChannel: () -> Unit,
) {
    Button(
        onClick = { onAddChannel() },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_channel))
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.add_channel))
    }
}

private data class NormalizedChannelInput(
    val channelName: String,
    val derivedInstanceUrl: String?,
)

private fun normalizeChannelInput(rawInput: String): NormalizedChannelInput {
    val trimmedInput = rawInput.trim()
    if (trimmedInput.isEmpty()) {
        return NormalizedChannelInput("", null)
    }

    val maybeUri = runCatching { Uri.parse(trimmedInput) }.getOrNull()
    if (maybeUri != null && !maybeUri.scheme.isNullOrBlank() && !maybeUri.host.isNullOrBlank()) {
        val slug = extractChannelSlug(maybeUri.pathSegments)
        val baseInstance = buildBaseInstanceUrl(maybeUri)
        if (slug.isNullOrBlank()) {
            return NormalizedChannelInput("", baseInstance)
        }
        return NormalizedChannelInput(slug, baseInstance)
    }

    val fallbackSlug =
        extractChannelSlug(trimmedInput.split("/"))
            ?: trimmedInput.removePrefix("@").trim()

    return NormalizedChannelInput(
        channelName = fallbackSlug,
        derivedInstanceUrl = null,
    )
}

private fun extractChannelSlug(segments: Collection<String>): String? {
    val cleanedSegments = segments.map { it.trim() }.filter { it.isNotBlank() }
    if (cleanedSegments.isEmpty()) return null

    val firstSegment = cleanedSegments.first().lowercase(Locale.ROOT)
    val slug =
        when {
            cleanedSegments.size >= 2 &&
                (firstSegment == "c" || firstSegment == "channel" || firstSegment == "accounts") ->
                cleanedSegments.getOrNull(1)
            else -> cleanedSegments.lastOrNull()
        }

    return slug?.removePrefix("@")?.trim()
}

private fun buildBaseInstanceUrl(uri: Uri): String? {
    val host = uri.host ?: return null
    val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: "https"
    val portSegment = if (uri.port >= 0) ":${uri.port}" else ""
    return "$scheme://$host$portSegment"
}

private fun isValidInstanceUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val parsed = runCatching { Uri.parse(url.trim()) }.getOrNull() ?: return false
    val scheme = parsed.scheme?.lowercase(Locale.ROOT)
    val host = parsed.host
    return (scheme == "http" || scheme == "https") && !host.isNullOrBlank()
}

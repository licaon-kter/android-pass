package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.AndroidUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.pass.domain.ItemType

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    itemType: ItemType.Login,
    size: Int = 40
) {
    val packageName = itemType.packageInfoSet.firstOrNull()?.packageName?.value
    val website = itemType.websites.firstOrNull()
    LoginIcon(
        modifier = modifier,
        text = text,
        website = website,
        packageName = packageName,
        size = size,
    )
}

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    website: String?,
    packageName: String?,
    size: Int = 40,
) {
    if (website == null) {
        TwoLetterLoginIcon(
            modifier = modifier,
            text = text,
            size = size,
        )
    } else {
        SubcomposeAsyncImage(
            modifier = modifier
                .clip(CircleShape)
                .size(size.dp)
                .background(ProtonTheme.colors.backgroundNorm),
            model = website,
            contentDescription = null,

        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    Box(
                        modifier = Modifier
                            .placeholder()
                            .fillMaxSize()
                    )
                }
                is AsyncImagePainter.State.Success -> {
                    SubcomposeAsyncImageContent(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    FallbackLoginIcon(
                        text = text,
                        packageName = packageName,
                        size = size
                    )
                }
            }
        }
    }
}

@Composable
private fun FallbackLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    packageName: String?,
    size: Int = 40
) {
    if (packageName == null) {
        TwoLetterLoginIcon(
            modifier = modifier,
            text = text,
            size = size,
        )
    } else {
        val context = LocalContext.current
        val iconDrawable = remember(packageName) {
            AndroidUtils.getApplicationIcon(context, packageName)
        }
        when (iconDrawable) {
            None -> {
                TwoLetterLoginIcon(
                    modifier = modifier,
                    text = text,
                    size = size,
                )
            }
            is Some -> {
                Image(
                    modifier = Modifier.width(size.dp),
                    painter = rememberDrawablePainter(drawable = iconDrawable.value),
                    contentDescription = stringResource(R.string.linked_app_icon_content_description)
                )
            }
        }
    }
}

@Composable
private fun TwoLetterLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    size: Int = 40
) {
    CircleTextIcon(
        modifier = modifier,
        text = text,
        color = PassTheme.colors.accentPurpleOpaque,
        size = size
    )
}

@Preview
@Composable
fun LoginIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginIcon(text = "login text", website = null, packageName = null)
        }
    }
}

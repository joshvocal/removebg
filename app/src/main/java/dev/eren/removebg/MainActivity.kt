package dev.eren.removebg

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.eren.removebg.components.CustomButton
import dev.eren.removebg.components.CustomTopAppBar
import dev.eren.removebg.components.HorizontalColorCarousel
import dev.eren.removebg.ui.theme.RemovebgTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainActivityViewModel by viewModels()
        val state = viewModel.state

        setContent {
            RemovebgTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RemoveBackground()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveBackground(viewModel: MainActivityViewModel = viewModel()) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    val remover = remember {
        RemoveBg(context = context)
    }

    val pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    val input = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

                    viewModel.updateState {
                        this.copy(
                            inputImage = input
                        )
                    }

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            })

    LaunchedEffect(viewModel.action) {
        viewModel.action.collect { action ->
            when (action) {
                MainActivityAction.PickMedia -> pickMedia(
                    viewModel = viewModel,
                    pickMedia = pickMedia
                )

                MainActivityAction.ChangeBackground -> changeBackground(
                    viewModel = viewModel,
                    remover = remover,
                    color = Color.Black,
                )

                MainActivityAction.SegmentImage -> {
                    val inputImage = state.inputImage

                    Toast.makeText(context, "Analyzing image", Toast.LENGTH_SHORT).show()

                    if (inputImage != null) {
                        segmentImage(
                            viewModel = viewModel,
                            remover = remover,
                            inputImage = inputImage
                        ).collect { segmentedImage ->
                            viewModel.updateState {
                                this.copy(
                                    outputImage = segmentedImage,
                                    maskedImage = segmentedImage,
                                    isImageSegmentationSuccess = true,
                                    isCustomBackground = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.inputImage != null) {

                        val modifierBuilder = Modifier
                            .fillMaxWidth()

                        val modifier: Modifier = if (state.outputImage != null) {
                            if (!state.isImageSegmentationSuccess || state.isCustomBackground) {
                                modifierBuilder
                                    .clickable {
                                        viewModel.updateState {
                                            this.copy(
                                                isImageSegmentationSuccess = !this.isImageSegmentationSuccess,
                                            )
                                        }
                                    }
                            } else {
                                modifierBuilder
                                    .clickable {
                                        viewModel.updateState {
                                            this.copy(
                                                isImageSegmentationSuccess = !this.isImageSegmentationSuccess,
                                            )
                                        }
                                    }
                                    .drawBehind {

                                        drawRect(
                                            SolidColor(Color.Green),
                                            size = Size(
                                                width = size.width,
                                                height = size.height
                                            )
                                        )

                                        // Draws checkerboard in case the image contains transparent parts
                                        val tileSize = 40f
                                        val tileWidth = (size.width / tileSize).toInt()
                                        val tileHeight = (size.height / tileSize).toInt()

                                        val darkColor = Color.hsl(0f, 0f, 0.9f)
                                        val lightColor = Color.hsl(1f, 1f, 1f)

                                        for (i in 0..tileWidth) {
                                            for (j in 0..tileHeight) {
                                                drawRect(
                                                    topLeft = Offset(i * tileSize, j * tileSize),
                                                    color = if ((i + j) % 2 == 0) darkColor else lightColor,
                                                    size = Size(tileSize, tileSize)
                                                )
                                            }
                                        }
                                    }
                            }
                        } else {
                            modifierBuilder
                                .clickable {
                                    viewModel.handleViewAction(MainActivityViewAction.SegmentImage)
                                }
                        }

                        val inputImage = state.inputImage
                        val outputImage = state.outputImage
                        val isImageSegmentationSuccess = state.isImageSegmentationSuccess

                        val bitmap =
                            if (isImageSegmentationSuccess && outputImage != null) {
                                outputImage.asImageBitmap()
                            } else {
                                inputImage!!.asImageBitmap()
                            }

                        Image(
                            modifier = modifier,
                            bitmap = bitmap,
                            contentDescription = "",
                        )

                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    CustomButton(
                        onClick = {
                            pickMedia(viewModel = viewModel, pickMedia = pickMedia)
                        },
                        text = "Pick media",
                    )

                    val inputImage = state.inputImage

                    if (inputImage != null) {
                        HorizontalColorCarousel(
                            colors = colorList
                        ) { color ->

                            changeBackground(
                                viewModel = viewModel,
                                remover = remover,
                                color = color
                            )
                        }

                        CustomButton(
                            onClick = {
                                val outputImage = state.outputImage

                                if (outputImage != null) {
                                    viewModel.handleViewAction(
                                        MainActivityViewAction.SavePicture(
                                            bitmap = outputImage
                                        )
                                    )
                                }
                            },
                            text = "Save",
                        )
                    }
                }
            }
        }
    }
}


// These are required for events such as navigation since the ViewModel does not have access to context
// so they must be sent to the Fragment to be executed.
fun handleAction(action: MainActivityAction) {
    when (action) {
        MainActivityAction.ChangeBackground -> {}
        MainActivityAction.SegmentImage -> {}
        MainActivityAction.PickMedia -> {}
    }
}

private fun segmentImage(
    viewModel: MainActivityViewModel,
    remover: RemoveBg,
    inputImage: Bitmap
): Flow<Bitmap?> {
    return inputImage.let { image ->
        remover
            .clearBackground(image)
            .onStart {
                viewModel.updateState {
                    this.copy(
                        isLoading = true
                    )
                }
            }
            .onCompletion {
                viewModel.updateState {
                    this.copy(
                        isLoading = false
                    )
                }
            }
    }
}

private val colorList = listOf(
    Color.Cyan,
    Color.Red,
    Color.Green,
    Color.DarkGray,
    Color.Magenta,
    Color.Blue,
)

private fun pickMedia(

    viewModel: MainActivityViewModel,
    pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
) {
    viewModel.updateState {
        this.copy(
            outputImage = null,
            maskedImage = null,
            isImageSegmentationSuccess = false,
        )
    }

    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}

private fun changeBackground(viewModel: MainActivityViewModel, remover: RemoveBg, color: Color) {
    val maskedImage = viewModel.currentState.maskedImage

    if (maskedImage != null) {
//        val gradients = listOf(
//            Pair(android.graphics.Color.BLUE, android.graphics.Color.CYAN),
//            Pair(android.graphics.Color.BLUE, android.graphics.Color.MAGENTA),
//            Pair(android.graphics.Color.YELLOW, android.graphics.Color.RED),
//            Pair(android.graphics.Color.BLACK, android.graphics.Color.DKGRAY),
//        )
//
//        val (start, end) = gradients.random()

        val newOutputImage = remover.addBackgroundColorToBitmap(
            bitmap = maskedImage,
            backgroundColor = color.toArgb(),
        )

        viewModel.updateState {
            this.copy(
                outputImage = newOutputImage,
                isCustomBackground = true,
            )
        }
    }
}


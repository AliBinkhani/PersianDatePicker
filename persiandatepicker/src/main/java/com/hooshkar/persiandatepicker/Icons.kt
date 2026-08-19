/*
 * Vendored and trimmed from androidx.compose.material3:material3-android:1.4.0
 * (internal/Icons.kt), because the originals are `internal` to the material3 module. Only the
 * icons actually used by the date picker are kept.
 *
 * Copyright 2024 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.hooshkar.persiandatepicker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object Icons {

    internal object AutoMirrored {
        internal object Filled {
            internal val KeyboardArrowLeft: ImageVector
                get() {
                    if (_keyboardArrowLeft != null) {
                        return _keyboardArrowLeft!!
                    }
                    _keyboardArrowLeft =
                        materialIcon(
                            name = "AutoMirrored.Filled.KeyboardArrowLeft",
                            autoMirror = true,
                        ) {
                            materialPath {
                                moveTo(15.41f, 16.59f)
                                lineTo(10.83f, 12.0f)
                                lineToRelative(4.58f, -4.59f)
                                lineTo(14.0f, 6.0f)
                                lineToRelative(-6.0f, 6.0f)
                                lineToRelative(6.0f, 6.0f)
                                lineToRelative(1.41f, -1.41f)
                                close()
                            }
                        }
                    return _keyboardArrowLeft!!
                }

            private var _keyboardArrowLeft: ImageVector? = null

            internal val KeyboardArrowRight: ImageVector
                get() {
                    if (_keyboardArrowRight != null) {
                        return _keyboardArrowRight!!
                    }
                    _keyboardArrowRight =
                        materialIcon(
                            name = "AutoMirrored.Filled.KeyboardArrowRight",
                            autoMirror = true,
                        ) {
                            materialPath {
                                moveTo(8.59f, 16.59f)
                                lineTo(13.17f, 12.0f)
                                lineTo(8.59f, 7.41f)
                                lineTo(10.0f, 6.0f)
                                lineToRelative(6.0f, 6.0f)
                                lineToRelative(-6.0f, 6.0f)
                                lineToRelative(-1.41f, -1.41f)
                                close()
                            }
                        }
                    return _keyboardArrowRight!!
                }

            private var _keyboardArrowRight: ImageVector? = null
        }
    }

    internal object Filled {
        internal val Check: ImageVector
            get() {
                if (_check != null) {
                    return _check!!
                }
                _check =
                    materialIcon(name = "Filled.Check") {
                        materialPath {
                            moveTo(9.0f, 16.17f)
                            lineTo(4.83f, 12.0f)
                            lineToRelative(-1.42f, 1.41f)
                            lineTo(9.0f, 19.0f)
                            lineTo(21.0f, 7.0f)
                            lineToRelative(-1.41f, -1.41f)
                            close()
                        }
                    }
                return _check!!
            }

        private var _check: ImageVector? = null

        internal val ArrowDropDown: ImageVector
            get() {
                if (_arrowDropDown != null) {
                    return _arrowDropDown!!
                }
                _arrowDropDown =
                    materialIcon(name = "Filled.ArrowDropDown") {
                        materialPath {
                            moveTo(7.0f, 10.0f)
                            lineToRelative(5.0f, 5.0f)
                            lineToRelative(5.0f, -5.0f)
                            close()
                        }
                    }
                return _arrowDropDown!!
            }

        private var _arrowDropDown: ImageVector? = null
    }
}

private inline fun materialIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector =
    ImageVector.Builder(
            name = name,
            defaultWidth = MaterialIconDimension.dp,
            defaultHeight = MaterialIconDimension.dp,
            viewportWidth = MaterialIconDimension,
            viewportHeight = MaterialIconDimension,
            autoMirror = autoMirror,
        )
        .block()
        .build()

private inline fun ImageVector.Builder.materialPath(
    fillAlpha: Float = 1f,
    strokeAlpha: Float = 1f,
    pathFillType: PathFillType = DefaultFillType,
    pathBuilder: PathBuilder.() -> Unit,
) =
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = fillAlpha,
        stroke = null,
        strokeAlpha = strokeAlpha,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = pathFillType,
        pathBuilder = pathBuilder,
    )

// All Material icons (currently) are 24dp by 24dp, with a viewport size of 24 by 24.
private const val MaterialIconDimension = 24f

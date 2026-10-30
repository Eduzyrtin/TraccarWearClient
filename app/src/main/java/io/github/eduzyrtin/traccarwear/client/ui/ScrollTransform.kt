package io.github.eduzyrtin.traccarwear.client.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

// Los textos sueltos no se encogen ni se desvanecen en el borde redondo y la pantalla los cortaba;
// con esto siguen el mismo efecto que los botones
fun Modifier.scrollTransformed(scope: TransformingLazyColumnItemScope, spec: TransformationSpec): Modifier =
    transformedHeight(scope, spec).graphicsLayer {
        with(scope) { with(spec) { applyContainerTransformation(scrollProgress) } }
    }

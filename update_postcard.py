import sys
import re

file_path = "app/src/main/java/com/example/ui/SocialHubApp.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """    var showTipModal by remember { mutableStateOf(false) }"""
replacement = """    var showTipModal by remember { mutableStateOf(false) }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by androidx.compose.foundation.interaction.collectIsHoveredAsState(interactionSource)
    val hoverScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "hoverScale"
    )"""
content = content.replace(target, replacement)

target2 = """    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),"""
replacement2 = """    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .scale(hoverScale)
            .hoverable(interactionSource),"""
content = content.replace(target2, replacement2)

with open(file_path, "w") as f:
    f.write(content)
print("Updated!")

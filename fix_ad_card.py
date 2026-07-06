import sys

file_path = "app/src/main/java/com/example/ui/SocialHubApp.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """    val scale = 1f + (progress * 0.05f) // Subtle Ken Burns zoom effect

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .scale(hoverScale)
            .hoverable(interactionSource),"""

replacement = """    val scale = 1f + (progress * 0.05f) // Subtle Ken Burns zoom effect

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by androidx.compose.foundation.interaction.collectIsHoveredAsState(interactionSource)
    val hoverScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "hoverScaleAd"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .scale(hoverScale)
            .androidx.compose.foundation.hoverable(interactionSource),"""

content = content.replace(target, replacement)

with open(file_path, "w") as f:
    f.write(content)
print("Fixed ad card!")

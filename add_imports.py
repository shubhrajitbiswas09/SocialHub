import sys

file_path = "app/src/main/java/com/example/ui/SocialHubApp.kt"
with open(file_path, "r") as f:
    content = f.read()

imports_to_add = [
    "import androidx.compose.foundation.interaction.collectIsHoveredAsState",
    "import androidx.compose.ui.draw.scale",
    "import androidx.compose.foundation.hoverable"
]

for imp in imports_to_add:
    if imp not in content:
        content = content.replace("import androidx.compose.runtime.*", f"import androidx.compose.runtime.*\n{imp}")

# I also replaced .androidx.compose.foundation.hoverable(interactionSource) back to .hoverable(interactionSource)
content = content.replace(".androidx.compose.foundation.hoverable(interactionSource)", ".hoverable(interactionSource)")

with open(file_path, "w") as f:
    f.write(content)
print("Imports added.")

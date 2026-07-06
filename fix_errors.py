import sys

file_path = "app/src/main/java/com/example/ui/SocialHubApp.kt"
with open(file_path, "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "androidx.compose.foundation.Canvas(" in line:
        lines.insert(i, "                    val canvasInnerColor = ObsidianDark\n")
        break

for i in range(len(lines)):
    if "color = ObsidianDark," in lines[i]:
        lines[i] = lines[i].replace("ObsidianDark", "canvasInnerColor")

with open(file_path, "w") as f:
    f.writelines(lines)
print("Fixed!")

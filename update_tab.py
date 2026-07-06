import sys

file_path = "app/src/main/java/com/example/ui/SocialHubApp.kt"
with open(file_path, "r") as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if "} else {" in line and "Trust & Reputation Panel" in lines[i+1]:
        start_idx = i + 1
    if "if (showShareToast) {" in line and start_idx != -1 and end_idx == -1:
        end_idx = i
        break

if start_idx != -1 and end_idx != -1:
    new_content = """            // Profile Analytics Dashboard
            Text(
                text = "Profile Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            Text(
                text = "View engagement and follower growth over the last 30 days.",
                fontSize = 11.sp,
                color = GrayText,
                modifier = Modifier.padding(horizontal = 16.dp, bottom = 12.dp)
            )

            // High-Level KPIs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsKpiCard(title = "Total Views", value = "142K", change = "+12%", isPositive = true, modifier = Modifier.weight(1f))
                AnalyticsKpiCard(title = "Engagements", value = "18.5K", change = "+5.4%", isPositive = true, modifier = Modifier.weight(1f))
                AnalyticsKpiCard(title = "Profile Visits", value = "9,240", change = "-2.1%", isPositive = false, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Engagement Chart (Simulated Canvas)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Follower Growth", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .background(RazorTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Last 30 Days", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw Line Chart
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val points = listOf(0.1f, 0.3f, 0.2f, 0.5f, 0.4f, 0.7f, 0.6f, 0.8f, 1.0f)
                        val maxPoint = points.maxOrNull() ?: 1f
                        val widthPerPoint = size.width / (points.size - 1)
                        val height = size.height

                        val path = androidx.compose.ui.graphics.Path()
                        points.forEachIndexed { index, point ->
                            val x = index * widthPerPoint
                            val y = height - (point / maxPoint * height)
                            if (index == 0) path.moveTo(x, y)
                            else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = RazorTeal,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )

                        // Draw data points
                        points.forEachIndexed { index, point ->
                            val x = index * widthPerPoint
                            val y = height - (point / maxPoint * height)
                            drawCircle(
                                color = RazorTeal,
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            drawCircle(
                                color = ObsidianDark,
                                radius = 2.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Day 1", color = GrayText, fontSize = 9.sp)
                        Text("Day 15", color = GrayText, fontSize = 9.sp)
                        Text("Day 30", color = GrayText, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Audience Demographics (Simple Progress Bars)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Top Locations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    AnalyticsDemographicRow("United States", 45f, RazorBlue)
                    AnalyticsDemographicRow("United Kingdom", 25f, RazorTeal)
                    AnalyticsDemographicRow("Canada", 15f, Color(0xFFB06CFF))
                    AnalyticsDemographicRow("Other", 15f, GrayText)
                }
            }
        }
    }

"""
    lines = lines[:start_idx] + [new_content] + lines[end_idx:]
    with open(file_path, "w") as f:
        f.writelines(lines)
    print("Replaced!")
else:
    print(f"Indices not found! {start_idx}, {end_idx}")


package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1500dp-h500dp-xhdpi", sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { 
      SocialHubTheme { 
        SocialHubShotPreview()
      } 
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

@Composable
fun SocialHubShotPreview() {
  Box(
    modifier = Modifier
      .size(1500.dp, 500.dp)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            ObsidianDark,
            Color(0xFF0F0B23),
            ObsidianDark
          )
        )
      )
      .padding(24.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Status & Brand Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(RazorBlue, InstaPink))),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "S",
              color = Color.Black,
              fontWeight = FontWeight.Black,
              fontSize = 24.sp
            )
          }
          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "SocialHub Premium",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = LightText,
                letterSpacing = 1.sp
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Brush.linearGradient(listOf(SafeGold, RazorBlue)))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(text = "PRO", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
              }
            }
            Text(
              text = "The Next-Gen Social Monetization & Encrypted Interaction Platform",
              style = MaterialTheme.typography.bodySmall,
              color = GrayText
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Status pill
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x224ADE80),
            border = BorderStroke(1.dp, RazorTeal)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(RazorTeal)
              )
              Text(
                text = "SECURE PROTOCOL ACTIVE",
                color = RazorTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          // Ratio display badge
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x22FF4CA0),
            border = BorderStroke(1.dp, InstaPink)
          ) {
            Text(
              text = "EXCLUSIVE HIGH-FIDELITY PREVIEW",
              color = InstaPink,
              fontWeight = FontWeight.Black,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Three Main Panels
      Row(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Panel 1: Profile & Razorpay Wallet Ledger (With Ideas 2 & 10)
        Card(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CardBackground),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.AccountBox, contentDescription = "Wallet", tint = RazorBlue)
              Text(
                text = "SECURE WALLET & ESCROW",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = RazorBlue,
                letterSpacing = 1.sp
              )
            }

            // Wallet Balance Box
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, RazorBlue.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "AVAILABLE BALANCE", color = GrayText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "$500.00",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Black,
                  color = RazorBlue,
                  fontFamily = FontFamily.Monospace
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x1A4ADE80))
                  .border(BorderStroke(1.dp, RazorTeal), RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(text = "SECURE", color = RazorTeal, fontSize = 9.sp, fontWeight = FontWeight.Black)
              }
            }

            // Idea 2: Coming Soon Razorpay Secure UPI Checkout Simulator Card
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, RazorBlue.copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = "Razorpay", tint = RazorBlue, modifier = Modifier.size(14.dp))
                  Text(text = "Razorpay Quick UPI", color = LightText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFB300))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(text = "COMING SOON", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
              }
              Text(
                text = "Supports direct UPI apps (GPay, PhonePe, Paytm) & card payments inside escrow safely.",
                color = GrayText,
                fontSize = 9.sp,
                lineHeight = 12.sp
              )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Idea 10: Support Goal Tracker / Milestone Progress Bar
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                .padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "Goal: Holographic Live Rig Upgrade", color = LightText, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(text = "$420.00 / $500.00 (84%)", color = SafeGold, fontWeight = FontWeight.Black, fontSize = 10.sp)
              }
              LinearProgressIndicator(
                progress = { 0.84f },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = SafeGold,
                trackColor = Color.White.copy(alpha = 0.1f)
              )
            }
          }
        }

        // Panel 2: Live Stream, Subscription Tiers & Store (With Ideas 4 & 7)
        Card(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CardBackground),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
         ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Live", tint = InstaPink)
                Text(
                  text = "LIVESTREAMS & STORE",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Black,
                  color = InstaPink,
                  letterSpacing = 1.sp
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFFFF3B30))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(text = "LIVE NOW", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
              }
            }

            // Streaming Event info
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(InstaPink),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Live Stream", tint = Color.White, modifier = Modifier.size(18.dp))
              }
              Column {
                Text(text = "Cyberpunk Holographic 3D Art", color = LightText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "142 Active Ticket Holders", color = GrayText, fontSize = 9.sp)
              }
            }

            // Idea 7: Creator Merchandise Store Catalog Card Showcase
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, InstaPink.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                .padding(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "EXCLUSIVE NFT COLLECTIBLE", color = InstaPink, fontWeight = FontWeight.Black, fontSize = 8.sp)
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF331B29))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                  Text(text = "ONLY 9 LEFT", color = Color(0xFFFF5252), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = "Hologram Card #104", color = LightText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                  Text(text = "Price: 0.05 ETH (~$150)", color = GrayText, fontSize = 9.sp)
                }
                Button(
                  onClick = {},
                  colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  modifier = Modifier.height(24.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text(text = "BUY NOW", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
              }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Idea 4: Subscription Tier VIP Access Badges Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(text = "MONETIZATION SUBSCRIPTION TIERS", color = GrayText, fontSize = 9.sp, fontWeight = FontWeight.Black)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Tier 1: Silver
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(BorderStroke(1.dp, Color(0xFFC0C0C0).copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SILVER", color = Color(0xFFE0E0E0), fontWeight = FontWeight.Black, fontSize = 8.sp)
                    Text(text = "$5/mo", color = LightText, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                  }
                }
                // Tier 2: Gold VIP
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FFD700))
                    .border(BorderStroke(1.dp, SafeGold), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "GOLD VIP", color = SafeGold, fontWeight = FontWeight.Black, fontSize = 8.sp)
                    Text(text = "$15/mo", color = LightText, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                  }
                }
                // Tier 3: Platinum
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x224ADE80))
                    .border(BorderStroke(1.dp, RazorTeal), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PLATINUM", color = RazorTeal, fontWeight = FontWeight.Black, fontSize = 8.sp)
                    Text(text = "$49/mo", color = LightText, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                  }
                }
              }
            }
          }
        }

        // Panel 3: Decrypted Secure Chat & Gemini Insights (With Ideas 3 & 6)
        Card(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CardBackground),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure Chat", tint = RazorTeal)
                Text(
                  text = "ENCRYPTED CREATOR CHAT",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Black,
                  color = RazorTeal,
                  letterSpacing = 1.sp
                )
              }
            }

            // Idea 3: Cryptographic Shield Validation status bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x114ADE80))
                .border(BorderStroke(1.dp, RazorTeal.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp, horizontal = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Secure", tint = RazorTeal, modifier = Modifier.size(10.dp))
                Text(text = "AES-256 HANDSHAKE VALID", color = RazorTeal, fontSize = 8.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
              }
              Text(text = "Rotated: 0x9D7A", color = GrayText, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }

            // Decrypted Message from Creator
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0x154ADE80))
                  .border(BorderStroke(1.dp, RazorTeal.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                  .padding(8.dp)
                  .widthIn(max = 240.dp)
              ) {
                Column {
                  Text(text = "🔑 SECURE TRANSMISSION", color = RazorTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = "Our active session keys were successfully rotated. Chat fully E2EE verified!", color = LightText, fontSize = 10.sp, lineHeight = 13.sp)
                }
              }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Idea 6: Enhanced Gemini AI Co-Pilot Recommendation Insight Box
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(10.dp))
                .padding(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(imageVector = Icons.Default.Star, contentDescription = "Gemini", tint = SafeGold, modifier = Modifier.size(12.dp))
                  Text(text = "GEMINI CO-PILOT INSIGHT", color = SafeGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Text(text = "CONFIDENCE: 99.4% ✨", color = RazorTeal, fontSize = 8.sp, fontWeight = FontWeight.Black)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "🎯 #PromptEng: High-velocity prompt calibration is surging by +450% globally. Push customized holographic guides to drive engagement.",
                color = LightText,
                fontSize = 9.sp,
                maxLines = 2,
                lineHeight = 12.sp
              )
            }
          }
        }
      }

      // Elegant Bottom Glow Navigation Bar matching actual App layout
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x3BFFFFFF),
                Color(0x1BFFD5EC),
                Color(0x0AFFFFFF)
              )
            )
          )
          .border(
            width = 1.dp,
            brush = Brush.linearGradient(
              colors = listOf(
                RazorBlue.copy(alpha = 0.4f),
                InstaPink.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.15f)
              )
            ),
            shape = RoundedCornerShape(28.dp)
          )
      ) {
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          NavigationBarItemMock(icon = Icons.Default.Home, label = "Feed", active = true, activeColor = RazorBlue)
          NavigationBarItemMock(icon = Icons.Default.ShoppingCart, label = "Market", active = false, activeColor = RazorBlue)
          NavigationBarItemMock(icon = Icons.Default.Mail, label = "Secure Chats", active = false, activeColor = RazorBlue)
          NavigationBarItemMock(icon = Icons.Default.Person, label = "Profile", active = false, activeColor = RazorBlue)
        }
      }
    }
  }
}

@Composable
fun NavigationBarItemMock(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  active: Boolean,
  activeColor: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (active) activeColor.copy(alpha = 0.15f) else Color.Transparent)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (active) activeColor else GrayText,
      modifier = Modifier.size(20.dp)
    )
    if (active) {
      Text(
        text = label,
        color = activeColor,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp
      )
    }
  }
}

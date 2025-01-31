package edu.skku.cs.beatsaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class result : AppCompatActivity() {
    private lateinit var probabilityText: TextView
    private lateinit var resultMessage: TextView
    private lateinit var resultDescription: TextView
    private lateinit var componentScoresText: TextView
    private lateinit var heartRateText: TextView
    private lateinit var hrvProbabilityText: TextView
    private lateinit var hrvRmssdText: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        // Initialize views
        probabilityText = findViewById(R.id.probabilityText)
        resultMessage = findViewById(R.id.resultMessage)
        resultDescription = findViewById(R.id.resultDescription)
        componentScoresText = findViewById(R.id.componentScoresText)
        heartRateText = findViewById(R.id.heartRateValue)
//        hrvProbabilityText = findViewById(R.id.hrvProbabilityValue)
        hrvRmssdText = findViewById(R.id.hrvRmssdValue)
        println("This is the hrvRmssdText: ")
        println(hrvRmssdText)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.resultButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        fetchProbability()
    }

    private fun getRiskLevelInfo(probability: Double): Triple<String, String, Int> {
        return when {
            probability >= 0.80 -> Triple(
                "매우 높음 (Very High)",
                "공황장애 가능성이 매우 높습니다. 심각한 증상과 기능 저하가 있을 수 있으며, 적극적인 치료가 필요합니다.",
                R.color.risk_very_high
            )
            probability >= 0.60 -> Triple(
                "높음 (High)",
                "공황장애 가능성이 높습니다. 명백한 증상 및 회피 행동이 있으며, 치료가 필요할 수 있습니다.",
                R.color.risk_high
            )
            probability >= 0.40 -> Triple(
                "중간 (Moderate)",
                "공황장애 가능성이 중간 수준입니다. 일부 증상이 나타나며, 스트레스 상황에서 회피 행동 가능성이 있습니다.",
                R.color.risk_moderate
            )
            probability >= 0.20 -> Triple(
                "낮음 (Low)",
                "공황장애 가능성이 낮습니다. 약간의 증상이 있으나, 일상생활에 크게 영향이 없습니다.",
                R.color.risk_low
            )
            else -> Triple(
                "매우 낮음 (Very Low)",
                "공황장애 증상 가능성이 거의 없습니다. 설문 결과와 HRV 모두 양호한 상태입니다.",
                R.color.risk_very_low
            )
        }
    }

    private fun fetchProbability() {
        val request = Request.Builder()
            .url("http://192.168.0.3:5000/get_final_probability")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    probabilityText.text = "데이터를 불러오는데 실패했습니다"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            probabilityText.text = "서버 오류: ${response.code}"
                        }
                        return
                    }

                    try {
                        val jsonString = response.body?.string()
                        val jsonObject = JSONObject(jsonString)
                        val probability = jsonObject.getDouble("final_probability")
                        val componentScores = jsonObject.getJSONObject("component_scores")
                        val heartRate = jsonObject.optDouble("heart_rate", 0.0)
                        val ecgRmssd = jsonObject.optDouble("rmssd", 0.0)

                        runOnUiThread {
                            // Update probability and risk level
                            val probabilityPercentage = (probability * 100).toInt()
                            val (riskLevel, description, colorResId) = getRiskLevelInfo(probability)

                            probabilityText.text = "위험도: ${probabilityPercentage}%"
                            probabilityText.setTextColor(getColor(colorResId))

                            resultMessage.text = "위험 단계: $riskLevel"
                            resultDescription.text = description

                            // Update component scores
                            val dsm = componentScores.optDouble("dsm_score", 0.0) * 100
                            val appq = componentScores.optDouble("appq_score", 0.0) * 100
                            val hrv = componentScores.optDouble("ecg_probability", 0.0) * 100

                            componentScoresText.text = """
                    세부 평가 결과:
                    
                    PDSS 점수: ${String.format("%.1f", dsm)}% (가중치: 30%)
                    APPQ 점수: ${String.format("%.1f", appq)}% (가중치: 30%)
                    HRV 지표: ${String.format("%.1f", hrv)}% (가중치: 40%)
                """.trimIndent()

                            // Update Heart Rate display
                            heartRateText.text = String.format("%.1f", heartRate)

                            // Update HRV Probability display
//                            hrvProbabilityText.text = String.format("%.1f", hrv)

                            // Update RMSSD Value display (in milliseconds)
                            hrvRmssdText.text = String.format("%.2f", ecgRmssd) // Convert to ms
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            probabilityText.text = "데이터 처리 오류가 발생했습니다"
                        }
                    }
                }
            }
        })
    }
}
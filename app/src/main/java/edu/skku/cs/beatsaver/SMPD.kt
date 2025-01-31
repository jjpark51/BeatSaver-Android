package edu.skku.cs.beatsaver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class SMPDQuestion(val questionNumber: Int, val text: String)

val allQuestions = listOf(
    SMPDQuestion(1, "사람들과 대화하기"),
    SMPDQuestion(2, "차를 타고 자동 세차기 안으로 들어가는 것"),
    SMPDQuestion(3, "몹시 더운 날 격렬한 운동을 하기"),
    SMPDQuestion(4, "공기 침대에 입으로 공기를 빨리 불어넣기"),
    SMPDQuestion(5, "다른 사람들 앞에서 식사 하기"),
    SMPDQuestion(6, "몹시 더운 날 등산 하기"),
    SMPDQuestion(7, "치과에서 마취 받기"),
    SMPDQuestion(8, "사람들과 회의 중 중간에 끼어 들기"),
    SMPDQuestion(9, "사람들 앞에서 이야기나 연설을 하기"),
    SMPDQuestion(10, "혼자서 격렬하게 운동을 하기"),
    SMPDQuestion(11, "집에서 멀리 떨어진 곳에 혼자 가기"),
    SMPDQuestion(12, "사람들이 많은 모임에서 자신을 소개하기"),
    SMPDQuestion(13, "아무도 없는 고립된 곳에서 혼자 걷기"),
    SMPDQuestion(14, "고속도로에서 운전하기"),
    SMPDQuestion(15, "남의 눈에 튀는 옷을 입는 것"),
    SMPDQuestion(16, "길을 잃어버릴지도 모르는 상태"),
    SMPDQuestion(17, "아주 진한 커피를 마시는 것"),
    SMPDQuestion(18, "극장의 한복판 좌석에 앉아서 영화 감상하기"),
    SMPDQuestion(19, "계단을 뛰어 올라가기"),
    SMPDQuestion(20, "지하철을 타기"),
    SMPDQuestion(21, "전화로 이야기 하기"),
    SMPDQuestion(22, "모르는 사람들과 만나기"),
    SMPDQuestion(23, "다른 사람들 앞에서 글을 쓰기"),
    SMPDQuestion(24, "사람들이 꽉 차 있는 방에 들어가기"),
    SMPDQuestion(25, "집에서 멀리 떨어진 곳에 가서 하룻밤 묵기"),
    SMPDQuestion(26, "술기운이 올라오는 느낌"),
    SMPDQuestion(27, "낮게 가설된 긴 다리를 건너기")
)

class SMPD : AppCompatActivity() {
    private val answers = IntArray(27) { 0 }
    private val client = OkHttpClient()
    private var currentPage = 0
    private val questionsPerPage = 4
    private val totalPages = (27 + questionsPerPage - 1) / questionsPerPage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_smpd)

        showCurrentPage()

        findViewById<Button>(R.id.smpdButton).setOnClickListener {
            if (currentPage == totalPages - 1) {
                sendAnswers()
            } else {
                currentPage++
                showCurrentPage()
            }
        }
    }

    private fun showCurrentPage() {
        val container = findViewById<LinearLayout>(R.id.questionsContainer)
        container.removeAllViews()

        val startIndex = currentPage * questionsPerPage
        val endIndex = minOf(startIndex + questionsPerPage, allQuestions.size)

        for (i in startIndex until endIndex) {
            val question = allQuestions[i]
            val questionView = layoutInflater.inflate(R.layout.question_item, container, false)

            questionView.findViewById<TextView>(R.id.questionText).text =
                "${question.questionNumber}. ${question.text}"

            val seekBar = questionView.findViewById<SeekBar>(R.id.seekBar)
            val valueText = questionView.findViewById<TextView>(R.id.valueText)

            seekBar.progress = answers[i]
            valueText.text = answers[i].toString()

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    valueText.text = progress.toString()
                    answers[i] = progress
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            container.addView(questionView)
        }

        updateProgress()
    }

    private fun updateProgress() {
        findViewById<ProgressBar>(R.id.progressBar).apply {
            progress = ((currentPage + 1) * 100) / totalPages
        }
        findViewById<TextView>(R.id.pageIndicator).text =
            "${currentPage + 1}/$totalPages"

        findViewById<Button>(R.id.smpdButton).text =
            if (currentPage == totalPages - 1) "제출" else "다음"
    }

    private fun sendAnswers() {
        val json = JSONObject().apply {
            put("answers", JSONArray(answers.toList()))
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("http://192.168.0.3:5000/appq")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SMPD, "전송 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SMPD, "답변이 성공적으로 전송되었습니다", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SMPD, result::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SMPD, "전송 실패: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
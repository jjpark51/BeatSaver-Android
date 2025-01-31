package edu.skku.cs.beatsaver

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.google.android.material.button.MaterialButton

// Create a new file called Question.kt
data class Question(
    val questionText: String,
    val options: List<String>
)

// List of questions
val questions = listOf(
    Question(
        "1. 지난 주 동안 공황발작 또는 제한된 증상 발작을 얼마나 자주 경험하셨나요?",
        listOf(
            "1) 공황이나 제한된 증상도 없음",
            "2) 경도: 완전한 공황발작은 없고, 제한된 증상발작은 하루에 1회를 넘지 않음",
            "3) 중증도: 주 1-2회의 완전한 공황발작, 하루에 제한된 증상 발작을 여러 번 경험",
            "4) 심함: 주 3회 이상의 완전한 공황발작, 그러나 평균 하루에 1회 이상은 아님",
            "5) 극심함: 하루에도 여러번 완전한 공황 발작이 일어남.\n발작이 없는 날보다 있는 날이 더 많음"
        )
    ),
    Question(
        "2. 만약 지난주에 공황발작이 있었다면, 공황발작이 일어날 때 얼마나 불쾌했습니까? 두려웠습니까?",
        listOf(
            "1) 지난 한 주간 공황이나 제한된 증상발작이 없거나, 이로 인한 불쾌감이 없었음",
            "2) 경도의 불쾌감 (그다지 강하지 않았음)",
            "3) 중증도의 불쾌감 (강하긴 했지만 견딜만 했음)",
            "4) 심한 불쾌감 (매우 강했음)",
            "5) 극심하고 심각한 고통감 (모든 발작 동안 극심하게 고통스러웠음)"
        )
    ),
    Question(
        "3. 지난주 동안, 다음 공황발작이 일어날까봐, 또는 발작과 관련된 공포에 대해서 얼마나 불안하고 걱정하셨습니까?",
        listOf(
            "1) 전혀 염려하지 않았음",
            "2) 간혹 걱정하거나 약간만 염려했음",
            "3) 자주 걱정하거나 중간 정도 염려했음",
            "4) 매우 자주 걱정하거나 매우 방해가 될 정도 였음",
            "5) 거의 지속적으로 걱정했으며, 아무 것도  할 수 없을"
        )
    ),
    Question(
        "4. 지난주 동안, 공황발작이 올 것 같은 공포 때문에 어떤 장소나 상황을(예. 대중교통, 극장, 군중들 속, 터널, 다리, 쇼핑몰, 혼자 있을때)회피했거나, 두려움을 느낀 적이 있습니까? 아니면 같은 이유로 그런 상황에 처한다면 피해야만 하거나 두려움을 느낄 다른 상황이 있었습니까? 만약 어느 한 경우라도 그렇다면, 지난 주 동안의 공포와 회피의 정도를 평가해 주십시오.",
        listOf(
            "1) 전혀 아니다. 두려움이나 회피가 전혀 없음",
            "2) 경도: 종종 두려움이나 회피가 있기는 하지만, 평소대로 상황에 직면하거나 참을 수 있음. 일상생활을 조정할 필요는 거의 또는 전혀 없음",
            "3) 중증도: 현저한 불안이나 회피가 있으나 견딜만 함. 두려운 상활을 피하기는 하지만 동료와 함께 있으면 직면할 수 있음. 일상생활에 약간의 조정이 필요하나 전반적인 기능 손상은 없음",
            "4) 심함: 극심한 회피, 회피 때문에 일상 활동 하는 것이 힘들었고, 중요한 과제를 수행할 수 없음",
            "5) 극심함: 광범위한 공포나 회피, 일상생활의 수정이 강력히 필요하며, 중요한 과제를 수행할 수 없음."
        )
    ),
    Question(
        "5. 지난주 동안, 공황 발작 시 경험하는 것과 유사한 신체 감각을 일으키거나, 혹은 공황발작을 야기할까봐 두려워서 어떤 활동(예. 신체적 운동, 성관계, 뜨거운 물로 샤워나 목욕하기, 커피마시기, 흥분되거나 무서운 영화보기)을 회피했거나, 두려움을 느낀 적이 있습니까? 아니면 같은 이유로, 지난주 동안 피해야만 했거나 두려움을 느꼈던 다른 활동이 있었습니까? 만약 어느 한 경우라도 그렇다면, 지난주 동안 활동에 대한 공포와 회피를 평가해 주십시오.",
        listOf(
            "1) 신체적 감각의 불편함 때문에, 특정 상황이나 활동을 피하거나 두려워한 적이 없음",
            "2) 경도: 종종 두려움이나 회피가 있기는 하지만, 평소대로 거의 불편함 없이 신체적 감각을 야기하는 활동과 상황을 직면하거나 참아 낼 수 있음, 이로 인한 일상생활의 수정이 거의 필요하지 않음",
            "3) 중증도: 현저한 회피가 있기는 하나 견딜만함, 완전히는 아니지만 제한적으로 일상생활의 수정이 필요함",
            "4) 심함: 강한 회피, 생활의 근본적인 수정을 해야 하거나, 기능상 실질적인 방해를 받음",
            "5) 극심함: 광범위하고 치명적인 회피, 일상생활의 광범위한 수정이 필요하며, 중요한 과제나 활동은 수행하지 못함"
        )
    ),
    Question(
        "6. 지난주 동안 상기 증상들 모두가 집안일을 하거나 직장에서 업무를 수행하는 데 얼마나 방해를 했습니까?",
        listOf(
            "1) 집이나 직장에서 공황장애 증상으로 방해받은 적이 전혀 없음",
            "2) 집안일이나 직장에서의 업무 수행에 약간 방해를 받음. 그러나 문제가 없을 경우와 마찬가지로 거의 모든 일을 처리할 수 있음",
            "3) 집안일이나 직장에서의 업무 수행에 현저하게 방해를 받음. 그러나 여전히 해야 할 일을 처리할 수 있음.",
            "4) 집안일이나 직장에서의 업무 수행에 근본적인 손상이 있음. 이런 문제들 때문에 할 수 없는 중요한 일들이 많음. ",
            "5) 극심함: 치명적 인 손상, 증상 때문에 집안일이나 직장에서의 업무 수행을 전혀 할 수가 없음. "
        )
    ),
    Question(
        "7. 지난주 동안 공황발작, 제한된 증상발작, 발작과 관련된 걱정, 발작으로 인한 상황 및 활동에 대한 두려움이 사회활동을 얼마나 방해했습니까?",
        listOf(
            "1) 전혀 방해 받지 않음",
            "2) 사회활동에 약간 방해를 받음. 그러나 문제가 없을 경우와 마찬가지로 거의 모든 일을 처리할 수 있음.",
            "3) 사회 활동에 현저하게 방해를 받음. 그러나 노력한다면 거의 모든 일을 처리할 수  있음",
            "4) 사회적 수행에 근본적인 손상이 있음. 이러한 문제 때문에 할 수 없는 사회적 일들이 많음",
            "5) 극심함. 치명적인 손상, 증상 때문에 어떠한 사회활동도 하기가 힘듬"
        )
    ),
    // Add more questions here
)
class DSM : AppCompatActivity() {
    private var selectedAnswer: Int = -1
    private val client = OkHttpClient()
    private val answers = mutableListOf<Int>()
    private var currentQuestion = 0
    private val TOTAL_QUESTIONS = questions.size
    private lateinit var buttonContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_dsm)

        buttonContainer = findViewById(R.id.buttonContainer)

        // Show first question
        showCurrentQuestion()

        findViewById<Button>(R.id.dsmButton).setOnClickListener {
            if (selectedAnswer == -1) {
                Toast.makeText(this, "답변을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            answers.add(selectedAnswer)

            if (currentQuestion == TOTAL_QUESTIONS - 1) {
                sendAnswers()
            } else {
                currentQuestion++
                showCurrentQuestion()
                selectedAnswer = -1
            }
        }
    }

    private fun showCurrentQuestion() {
        // Update question text
        findViewById<TextView>(R.id.currentQuestionText).text = questions[currentQuestion].questionText

        // Clear previous buttons (removing all views except the question text)
        for (i in buttonContainer.childCount - 1 downTo 1) {
            buttonContainer.removeViewAt(i)
        }

        // Add new option buttons
        questions[currentQuestion].options.forEachIndexed { index, optionText ->
            val button = MaterialButton(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Button_OutlinedButton).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = optionText
                setTextColor(Color.BLACK)
                stateListAnimator = null

                // Style the button like original
                setBackgroundColor(Color.WHITE)
                textAlignment = View.TEXT_ALIGNMENT_TEXT_START

                // Set outline stroke
                strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width)
                setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CCCCCC")))

                setOnClickListener {
                    // Deselect all buttons
                    for (i in 1 until buttonContainer.childCount) {
                        (buttonContainer.getChildAt(i) as MaterialButton).setBackgroundColor(Color.WHITE)
                    }
                    // Select this button
                    setBackgroundColor(Color.LTGRAY)
                    selectedAnswer = index + 1
                }
            }
            buttonContainer.addView(button)
        }

        updateProgress()
    }

    private fun updateProgress() {
        findViewById<ProgressBar>(R.id.progressBar).progress = ((currentQuestion + 1) * 100) / TOTAL_QUESTIONS
        findViewById<TextView>(R.id.progressText).text = "${currentQuestion + 1}/$TOTAL_QUESTIONS"
    }

    private fun sendAnswers() {
        val json = JSONObject().apply {
            put("answers", JSONArray(answers))
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("http://192.168.0.3:5000/dsm")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DSM, "전송 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DSM, "답변이 성공적으로 전송되었습니다", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@DSM, SMPD::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@DSM, "전송 실패: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
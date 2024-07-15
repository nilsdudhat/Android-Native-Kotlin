package com.udemy.quiz.app.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("question")
    var question: String? = null,

    @SerializedName("option1")
    var option1: String? = null,

    @SerializedName("option2")
    var option2: String? = null,

    @SerializedName("option3")
    var option3: String? = null,

    @SerializedName("option4")
    var option4: String? = null,

    @SerializedName("correct_option")
    var correctOption: String? = null,
)
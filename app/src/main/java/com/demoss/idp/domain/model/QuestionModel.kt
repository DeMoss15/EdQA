package com.demoss.idp.domain.model

class QuestionModel(
    var text: String,
    val answers: List<AnswerModel>
)
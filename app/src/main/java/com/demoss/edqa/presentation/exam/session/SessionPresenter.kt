package com.demoss.edqa.presentation.exam.session

import com.demoss.edqa.base.mvp.BasePresenterImpl
import com.demoss.edqa.domain.model.AnswerModel
import com.demoss.edqa.domain.usecase.TestSessionUseCase

class SessionPresenter(private val testSessionUseCase: TestSessionUseCase) : BasePresenterImpl<SessionContract.View>(),
    SessionContract.Presenter {

    override fun startSession() {
        testSessionUseCase.runSession(
            {
                view?.showQuestion(it)
                showCounter()
            }, // on next question
            { view?.showTimer(it) }, // on tick
            { view?.navigateToResults() } // on time out or stop timer
        )
    }

    override fun setAnswer(answer: AnswerModel) = testSessionUseCase.setAnswer(answer)

    override fun stopSession() = testSessionUseCase.stopSession()

    private fun showCounter() {
        view?.showCounter("${testSessionUseCase.getNumberOfAnswers() + 1}/${testSessionUseCase.getTest().questions.size}")
    }
}
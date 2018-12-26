package com.demoss.idp.presentation.main.edit.question

import com.demoss.idp.base.mvp.BasePresenterImpl
import com.demoss.idp.domain.model.QuestionModel
import com.demoss.idp.domain.usecase.EditTestUseCase
import com.demoss.idp.util.Constants.NEW_ENTITY_ID
import io.reactivex.observers.DisposableSingleObserver

class EditQuestionPresenter(private val editTestUseCase: EditTestUseCase)
    : BasePresenterImpl<EditQuestionContract.View>(), EditQuestionContract.Presenter {

    override var questionId: Int = NEW_ENTITY_ID

    override fun onViewShown() {
        super.onViewShown()
        editTestUseCase.getQuestion(getSingleObserver(), questionId)
    }

    override fun saveQuestion(question: String) = editTestUseCase.saveQuestion(question)

    override fun deleteQuestion() = editTestUseCase.deleteQuestion()

    private fun getSingleObserver(): DisposableSingleObserver<QuestionModel> = object : DisposableSingleObserver<QuestionModel>() {
        override fun onSuccess(t: QuestionModel) {
            view?.showQuestion(t)
            questionId = t.id
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            view?.showToast(e.localizedMessage)
        }
    }
}
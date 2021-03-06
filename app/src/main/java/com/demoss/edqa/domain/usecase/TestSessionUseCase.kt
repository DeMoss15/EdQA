package com.demoss.edqa.domain.usecase

import com.demoss.edqa.domain.model.AnswerModel
import com.demoss.edqa.domain.model.QuestionModel
import com.demoss.edqa.domain.model.TestModel
import com.demoss.edqa.domain.usecase.model.GetTestUseCase
import com.demoss.edqa.domain.usecase.model.UpdateTestUseCase
import com.demoss.edqa.util.Constants.SECONDS_IN_MINUTE
import com.demoss.edqa.util.Constants.TIME_SEPARATOR
import com.demoss.edqa.util.setDefaultSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class TestSessionUseCase(
    private val getTestUseCase: GetTestUseCase,
    private val updateTestUseCase: UpdateTestUseCase
) {

    private lateinit var test: TestModel
    private var timer: Long = 0L
    private val userAnswers = mutableListOf<AnswerModel>()
    private var isRunning = true

    fun setTestId(testId: Int): Completable {
        resetSession()
        return getTestUseCase.buildUseCaseObservable(GetTestUseCase.Params(testId))
            .map {
                test = it
                it.questions = it.questions
                    .filter { question -> question.answers.size > 0 }
                    .toMutableList()
            }
            .ignoreElement()
    }

    private fun resetSession() {
        timer = 0L
        userAnswers.clear()
        isRunning = true
    }

    // Setup =======================================================================================
    fun getTest(): TestModel = test

    fun setupSession(isShuffled: Boolean, timeInMinutes: Long, numberOfQuestions: Int) {
        if (isShuffled) {
            test.questions.shuffle()
            test.questions.map { it.answers.shuffle() }
        }
        this.timer = timeInMinutes * 60
        test.questions = test.questions.take(numberOfQuestions).toMutableList()
    }

    // Session =====================================================================================
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var questionsObservable: BehaviorSubject<QuestionModel>

    fun setAnswer(answer: AnswerModel) {
        userAnswers.add(answer)
        test.sessionResults.rightAnswersAmount = userAnswers.filter { it.isRightAnswer }.size
        test.sessionResults.shownQuestionsAmount = userAnswers.size
        if (test.questions.size <= userAnswers.size) {
            stopSession()
            return
        }
        questionsObservable.onNext(test.questions[userAnswers.size])
    }

    fun runSession(onNextQuestion: (QuestionModel) -> Unit, onTick: (String) -> Unit, onSessionEnd: () -> Unit) {
        test.sessionResults = TempEntitiesFabric.createEmptySession()
        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            setUpTimer(onTick).mergeWith(setUpQuestions(onNextQuestion).map { it.text })
                .doOnComplete { updateTest(onSessionEnd) }
                .doOnError { it.printStackTrace() }
                .subscribe()
        )
    }

    private fun updateTest(onSessionEnd: () -> Unit) {
        compositeDisposable.add(
            updateTestUseCase.buildUseCaseObservable(UpdateTestUseCase.Params(test))
                .setDefaultSchedulers()
                .subscribe(
                    {
                        onSessionEnd()
                        compositeDisposable.clear()
                    }, { it.printStackTrace() }
                )
        )
    }

    private fun setUpTimer(onTick: (String) -> Unit): Observable<String> = createTimer().doOnNext(onTick)
        .doOnNext { test.sessionResults.spentTime = it }
        .doOnComplete { questionsObservable.onComplete() }

    private fun setUpQuestions(onNextQuestion: (QuestionModel) -> Unit): Observable<QuestionModel> {
        questionsObservable = BehaviorSubject.create<QuestionModel>()
        questionsObservable.onNext(test.questions[0])
        return questionsObservable.doOnNext(onNextQuestion)
    }

    fun stopSession() {
        isRunning = false
    }

    // Results =====================================================================================
    fun getNumberOfAnswers(): Int = test.sessionResults.shownQuestionsAmount

    fun getNumberOfRightAnswers(): Int = test.sessionResults.rightAnswersAmount

    fun getTimeSpent(): String = test.sessionResults.spentTime.also {
        if (::compositeDisposable.isInitialized) compositeDisposable.dispose()
    }

    // Util ========================================================================================
    private fun createTimer(): Observable<String> = PublishSubject
        .interval(1, TimeUnit.SECONDS)
        .map { it + 1 }
        .startWith(0)
        .takeWhile { !isTimeOut(it) && isRunning }
        .map { formatTime(it) }

    private fun formatTime(timeInSeconds: Long): String = formatNN(timeInSeconds / SECONDS_IN_MINUTE) +
            TIME_SEPARATOR + formatNN(timeInSeconds % SECONDS_IN_MINUTE)

    private fun formatNN(time: Long) = if (time <= 9) "0$time" else time.toString()

    private fun isTimeOut(currentTime: Long): Boolean =
        if (timer == 0L) false
        else currentTime > timer
}
package com.demoss.idp.domain.usecase

import com.demoss.idp.domain.model.AnswerModel
import com.demoss.idp.domain.model.QuestionModel
import com.demoss.idp.domain.model.TestModel
import com.demoss.idp.domain.usecase.model.GetTestUseCase
import com.demoss.idp.util.setDefaultSchedulers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class TestSessionUseCase(private val getTestUseCase: GetTestUseCase) {

    private lateinit var test: TestModel
    private var timer: Long = 0L
    private val userAnswers = mutableListOf<AnswerModel>()
    private var timeSpent = ""
    private var isRunning = true

    fun setTestId(testId: Int): Completable {
        resetSession()
        return getTestUseCase.buildUseCaseObservable(GetTestUseCase.Params(testId))
            .map { test = it }
            .ignoreElement()
    }

    private fun resetSession() {
        timer = 0L
        timeSpent = ""
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
    private val compositeDisposable = CompositeDisposable()
    private lateinit var timeObservable: Observable<String>
    private lateinit var questionsObservable: BehaviorSubject<QuestionModel>

    fun setAnswer(answer: AnswerModel) {
        userAnswers.add(answer)
        if (test.questions.size <= userAnswers.size) {
            questionsObservable.onComplete()
            return
        }
        questionsObservable.onNext(test.questions[userAnswers.size])
    }

    fun runSession(onNextQuestion: (QuestionModel) -> Unit, onTick: (String) -> Unit, onSessionEnd: () -> Unit) {
        // run timer
        runTimer(onTick)
        // run questions
        subscribeToQuestions(onNextQuestion)
        // combine
        compositeDisposable.add(questionsObservable.doOnComplete { isRunning = !isRunning }.subscribe()) // complete timer
        compositeDisposable.add(timeObservable.doOnComplete { questionsObservable.onComplete() }.subscribe()) // complete questions
        compositeDisposable.add(
            timeObservable.mergeWith(questionsObservable.map { it.text })
                .doOnComplete(onSessionEnd)
                .doOnComplete { compositeDisposable.clear() }
                .subscribe()
        )
    }

    private fun subscribeToQuestions(onNextQuestion: (QuestionModel) -> Unit) {
        questionsObservable = BehaviorSubject.create<QuestionModel>()
        compositeDisposable.add(questionsObservable.subscribe(onNextQuestion))
        questionsObservable.onNext(test.questions[0])
    }

    private fun runTimer(onTick: (String) -> Unit) {
        timeObservable = createTimer()
        compositeDisposable.add(timeObservable.subscribe(onTick))
        compositeDisposable.add(timeObservable.takeLast(1).subscribe { timeSpent = it })
    }

    // Results =====================================================================================
    fun getNumberOfAnswers(): Int = userAnswers.size

    fun getNumberOfRightAnswers(): Int = userAnswers.filter { it.isRightAnswer }.size

    fun getTimeSpent(): String = timeSpent

    // Private =====================================================================================
    private fun createTimer(): Observable<String> = PublishSubject
        .interval(1, TimeUnit.SECONDS)
        .startWith(0)
        .takeWhile { isRunning && (timer == 0L || (timer != 0L && it != timer)) }
        .map { time -> "${time / 60}:${time % 60}" }
        .setDefaultSchedulers()
}
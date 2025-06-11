package dev.coffee.examapp

import dev.coffee.examapp.viewmodel.*

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExamListViewModelTest::class,
    ExamViewModelTest::class,
    PracticeListViewModelTest::class,
    PracticeViewModelTest::class,
    SimilarQuestionsViewModelTest::class,
    WrongQuestionViewModelTest::class,
)
class TotalUnitViewModelTest
package dev.coffee.examapp

import PracticeListScreenTest
import androidx.compose.material3.ExperimentalMaterial3Api
import dev.coffee.examapp.exam.ExamListScreenTest
import dev.coffee.examapp.exam.ExamResultScreenTest
import dev.coffee.examapp.exam.ExamScreenTest
import dev.coffee.examapp.practice.PracticeScreenTest
import dev.coffee.examapp.statistic.StatisticScreenTest
import dev.coffee.examapp.wrongquestion.SimilarQuestionsScreenTest
import dev.coffee.examapp.wrongquestion.WrongQuestionScreenTest
import dev.coffee.examapp.ServerConfigScreenTest

import org.junit.runner.RunWith
import org.junit.runners.Suite

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExamListScreenTest::class,
    ExamResultScreenTest::class,
    ExamScreenTest::class,
    PracticeListScreenTest::class,
    PracticeScreenTest::class,
    StatisticScreenTest::class,
    SimilarQuestionsScreenTest::class,
    WrongQuestionScreenTest::class,
    ServerConfigScreenTest::class
)
class TotalUnitScreenTest
package dev.coffee.examapp.viewmodel

import androidx.lifecycle.ViewModel
import dev.coffee.examapp.network.ApiService
import dev.coffee.examapp.network.RetrofitClient

class PracticeViewModel : ViewModel() {
    private val apiService : ApiService = RetrofitClient.instance

    // TODO
}
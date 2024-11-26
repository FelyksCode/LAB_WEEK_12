package com.example.lab_week_12

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab_week_12.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

class MovieViewModel(private val movieRepository: MovieRepository) : ViewModel() {
    init {
        fetchPopularMovies()
    }

    // define the LiveData
    private val _popularMovies = MutableStateFlow(
        emptyList<Movie>()
    )
    val popularMovies: StateFlow<List<Movie>> = _popularMovies
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    private fun fetchPopularMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.fetchMovies().catch {
                // catch is a terminal operator that catches exceptions from the
                _error.value = "An exception occurred: ${it.message}"
            }.collect { movies ->
                val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                val filteredMovies = movies.filter { it.releaseDate.startsWith(currentYear) }
                    .sortedByDescending { it.popularity }
                _popularMovies.value = filteredMovies
            }
        }
    }
}
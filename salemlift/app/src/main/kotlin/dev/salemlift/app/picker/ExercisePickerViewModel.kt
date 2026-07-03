package dev.salemlift.app.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.salemlift.data.db.ExerciseDao
import dev.salemlift.data.db.ExerciseEntity
import dev.salemlift.data.db.searchByName
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * Thin read-only ViewModel for the exercise-picker dialog — the one place the
 * UI layer is allowed to query a DAO directly (ARCHITECTURE §4). Defaults to
 * the muscle's primary-mover list; typing switches to a literal name search.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExercisePickerViewModel(
    private val exerciseDao: ExerciseDao,
) : ViewModel() {
    private val muscle = MutableStateFlow<Muscle?>(null)
    private val query = MutableStateFlow("")

    val searchQuery: StateFlow<String> = query

    val exercises: StateFlow<List<ExerciseEntity>> =
        combine(muscle, query) { m, q -> m to q }
            .flatMapLatest { (m, q) -> exercisesFor(m, q) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    /** Called when the picker opens for a muscle block; resets the search. */
    fun open(target: Muscle) {
        muscle.value = target
        query.value = ""
    }

    fun setQuery(text: String) {
        query.value = text
    }

    private fun exercisesFor(
        m: Muscle?,
        q: String,
    ): Flow<List<ExerciseEntity>> =
        when {
            q.isNotBlank() -> exerciseDao.searchByName(q.trim())
            m != null -> exerciseDao.filterByPrimaryMuscle(m)
            else -> flowOf(emptyList())
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

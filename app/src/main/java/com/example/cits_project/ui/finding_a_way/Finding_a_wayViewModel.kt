import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class SearchHistoryItem(val startLocation: String, val endLocation: String)

class Finding_a_wayViewModel : ViewModel() {

    private val _searchHistory = MutableLiveData<List<SearchHistoryItem>>()
    val searchHistory: LiveData<List<SearchHistoryItem>> get() = _searchHistory

    init {
        // 초기에는 빈 리스트로 초기화합니다.
        _searchHistory.value = emptyList()
    }

    fun addSearchHistoryItem(item: SearchHistoryItem) {
        val currentList = _searchHistory.value.orEmpty().toMutableList()
        currentList.add(0, item) // 새 항목을 리스트의 맨 앞에 추가
        _searchHistory.value = currentList
    }
}

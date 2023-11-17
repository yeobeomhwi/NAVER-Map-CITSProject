import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cits_project.R

class SearchHistoryAdapter(private val searchHistory: List<SearchHistoryItem>) :
    RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startLocationTextView: TextView = itemView.findViewById(R.id.startLocationTextView)
        val endLocationTextView: TextView = itemView.findViewById(R.id.endLocationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.search_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = searchHistory[position]

        // 데이터를 ViewHolder의 뷰에 바인딩
        holder.startLocationTextView.text = item.startLocation
        holder.endLocationTextView.text = item.endLocation
    }

    override fun getItemCount(): Int {
        return searchHistory.size
    }
}

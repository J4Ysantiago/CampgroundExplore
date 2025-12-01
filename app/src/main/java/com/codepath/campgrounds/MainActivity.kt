package com.codepath.campgrounds

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.campgrounds.databinding.ActivityMainBinding
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestHeaders
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.serialization.json.Json
import okhttp3.Headers
import org.json.JSONException

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

private const val TAG = "CampgroundsMain/"
private val PARKS_API_KEY = BuildConfig.API_KEY
private val CAMPGROUNDS_URL =
    "https://developer.nps.gov/api/v1/campgrounds?api_key=${PARKS_API_KEY}"

class MainActivity : AppCompatActivity() {

    private val campgrounds = mutableListOf<Campground>()
    private lateinit var campgroundsRecyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView ONCE
        campgroundsRecyclerView = binding.campgrounds
        val campgroundAdapter = CampgroundAdapter(this, campgrounds)
        campgroundsRecyclerView.adapter = campgroundAdapter
        campgroundsRecyclerView.layoutManager = LinearLayoutManager(this)

        campgroundsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                (campgroundsRecyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )

        // ============= API REQUEST =============

        val client = AsyncHttpClient()

        // ⭐ Correct header object for THIS library version
        val headers = RequestHeaders()
        headers["User-Agent"] = "CampgroundExplorerApp"

        client.get(
            CAMPGROUNDS_URL,
            headers,
            null,
            object : JsonHttpResponseHandler() {

                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String?,
                    throwable: Throwable?
                ) {
                    Log.e(TAG, "Failed to fetch campgrounds: $statusCode")
                }

                override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                    Log.i(TAG, "Successfully fetched campgrounds")

                    try {
                        val parsedJson = createJson().decodeFromString(
                            CampgroundResponse.serializer(),
                            json.jsonObject.toString()
                        )

                        parsedJson.data?.let { list ->
                            campgrounds.clear()
                            campgrounds.addAll(list)
                            campgroundAdapter.notifyDataSetChanged()
                            Log.i("DEBUG", "Loaded ${campgrounds.size} campgrounds")

                        }

                    } catch (e: JSONException) {
                        Log.e(TAG, "JSON Exception: $e")
                    } catch (e: Exception) {
                        Log.e(TAG, "Parsing error: $e")
                    }
                }
            }
        )
    }
}

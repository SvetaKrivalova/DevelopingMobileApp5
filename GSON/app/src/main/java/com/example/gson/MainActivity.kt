package com.example.gson

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Config.DEBUG
import android.util.Log.DEBUG
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.InputStream
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        getRequest(this)
        Timber.v("Test")
    }
}

class BuildConfig {

}

fun getRequest(mainActivity: MainActivity) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                val jsonText = response?.body()?.string()
                val gsonBuilder = GsonBuilder()
                val gs = gsonBuilder.create()
                val wrapper: Wrapper = gs.fromJson(jsonText, Wrapper::class.java)
                val sizeJson: Int? = wrapper.photos?.photo?.size
                printFiveElements(wrapper, sizeJson, gs)

                val listPNG = arrayListOf<Drawable>()
                val listLinks = arrayListOf<String>()
                fillLists(listPNG, listLinks, wrapper)

                Timber.d("Size" + listPNG.size,toString())

                val rView = mainActivity.findViewById<RecyclerView>(R.id.rView)
                val layoutManager = GridLayoutManager(mainActivity, 2) //?
                val MyAdapter = MyAdapter(listPNG, listLinks)
                rView.setHasFixedSize(true)

                mainActivity.runOnUiThread(java.lang.Runnable {
                    rView.layoutManager = layoutManager
                    rView.adapter = MyAdapter
                })
            }
        }
    })

}

fun printFiveElements(wrapper: Wrapper, sizeJson: Int?, gs: Gson) {
    for (index in 0 until sizeJson!!) {
        if (index % 4 ==0) Timber.d(gs.toJson(wrapper.photos?.photo?.get(index)))
    }
}

fun fillLists(listPNG: ArrayList<Drawable>, listLinks: ArrayList<String>, wrapper: Wrapper) {
    var lim: Int = 5
    for (index in 0 .. lim) {
        try {
            var urlString = "https://farm${wrapper.photos!!.photo[index].farm}.staticflickr.com/" +
                    "${wrapper.photos!!.photo[index].server}" +
                    "${wrapper.photos!!.photo[index].id}" +
                    "${wrapper.photos!!.photo[index].secret}_z.jpg"
            var url = URL(urlString).content as InputStream
            var draw = Drawable.createFromStream(url, "scr name")

            listPNG.add(draw)
            listLinks.add(urlString)
        } catch (ex: java.lang.Exception) {
            Timber.d("Error: " + "https://farm${wrapper.photos!!.photo[index].farm}.staticflickr.com/" +
                    "${wrapper.photos!!.photo[index].server}" +
                    "${wrapper.photos!!.photo[index].id}" +
                    "${wrapper.photos!!.photo[index].secret}_z.jpg")
            lim++
            continue
        }
    }
}
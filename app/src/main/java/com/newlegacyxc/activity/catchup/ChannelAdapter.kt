package com.newlegacyxc.activity.catchup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.newlegacyxc.R
import com.newlegacyxc.models.EPGChannel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.channel_item.view.*
import kotlinx.android.synthetic.main.home_category_list_item.view.image
import kotlinx.android.synthetic.main.home_category_list_item.view.name

@Suppress("DEPRECATION")
class ChannelAdapter(private val con: Context, private var list: MutableList<out EPGChannel>) : BaseAdapter() {
    
    val inflater : LayoutInflater = con.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rootview = inflater.inflate(R.layout.channel_item,parent,false)
        val epgChannel : EPGChannel = getItem(position) as EPGChannel
        if (epgChannel.stream_icon!=null && epgChannel.stream_icon != "") {
            Picasso.with(rootview.context)
                    .load(epgChannel.stream_icon)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.icon)
                    .into(rootview.image)
            rootview.title.visibility = View.GONE
            rootview.name.visibility = View.VISIBLE
            rootview.name.text = epgChannel.name
        }else{
            rootview.image.setImageResource(R.drawable.pkg_dlg_title_bg)
            rootview.title.visibility = View.VISIBLE
            rootview.title.text = epgChannel.name
            rootview.name.visibility = View.INVISIBLE
        }
//        rootview.setOnFocusChangeListener {_ , hasFocus ->
//            if(hasFocus){
//                rootview.card.cardElevation = 10f
//                rootview.card.setCardBackgroundColor(Color.parseColor("#FFD600"))
//                rootview.scaleX = 1f
//                rootview.scaleY = 1f
//                rootview.name.setTextColor(Color.parseColor("#212121"))
//                rootview.title.setTextColor(Color.parseColor("#eeeeee"))
//            }else{
//                rootview.card.cardElevation = 1f
//                rootview.card.setCardBackgroundColor(Color.parseColor("#25ffffff"))
//                rootview.scaleX = 0.85f
//                rootview.scaleY = 0.85f
//                rootview.name.setTextColor(Color.parseColor("#eeeeee"))
//                rootview.title.setTextColor(Color.parseColor("#eeeeee"))
//            }
//        }
        return rootview
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = list.size

    fun setData(data: MutableList<out EPGChannel>){
        list = data
        notifyDataSetChanged()
    }
}
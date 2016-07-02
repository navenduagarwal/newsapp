package com.example.navendu.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by navendu on 7/2/2016.
 */
public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(Context context, ArrayList<News> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        News news = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        TextView titleText = (TextView) convertView.findViewById(R.id.list_title_textview);
        titleText.setText(news.getTitle());

        if (news.getAuthor() != null) {
            TextView authorText = (TextView) convertView.findViewById(R.id.list_author_textview);
            authorText.setText(news.getAuthor());
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_image);
        if (news.getThumbnail() != null) {
            new ImageDownloaderTask(imageView).execute(news.getThumbnail());
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
        return convertView;
    }
}

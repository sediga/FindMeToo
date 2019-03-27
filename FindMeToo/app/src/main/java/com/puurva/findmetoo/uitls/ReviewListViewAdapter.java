package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.model.ProfileReviewModel;

public class ReviewListViewAdapter  extends ArrayAdapter<ProfileReviewModel> {
    private final Context context;
    private final ProfileReviewModel[] values;

    public ReviewListViewAdapter(Context context, ProfileReviewModel[] values) {
        super(context, R.layout.activity_review_list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_review_list, parent, false);
        TextView textUserName = (TextView) rowView.findViewById(R.id.textUserName);
        TextView textReview = (TextView) rowView.findViewById(R.id.textReview);
        RatingBar ratingBar = rowView.findViewById(R.id.edit_profile_rating);
        textUserName.setText(values[position].getUserName());
        textReview.setText(values[position].getReview());
        ratingBar.setRating(values[position].getRating());
        // Change the icon for Windows and iPhone
//        String s = values[position].getReview();

        return rowView;
    }
}

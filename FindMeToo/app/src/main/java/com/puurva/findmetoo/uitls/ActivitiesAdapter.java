package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.CurrentActivity;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileReviewModel;

public class ActivitiesAdapter extends ArrayAdapter<CurrentActivity> {
    private final Context context;
    private final CurrentActivity[] values;

    public ActivitiesAdapter(Context context, CurrentActivity[] values) {
        super(context, R.layout.activity_review_list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activities_list, parent, false);
        TextView textTitle = (TextView) rowView.findViewById(R.id.text_activity_title);
        TextView textDescription = (TextView) rowView.findViewById(R.id.text_activity_description);
        textTitle.setText(values[position].Activity);
        textDescription.setText(values[position].description);
        // Change the icon for Windows and iPhone
//        String s = values[position].getReview();

        return rowView;
    }
}

package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.CurrentActivity;

public class ActivitiesAdapter extends ArrayAdapter<ActivityModel> {
    private final Context context;
    private final ActivityModel[] values;

    public ActivitiesAdapter(Context context, ActivityModel[] values) {
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
        ImageButton activityImage = (ImageButton) rowView.findViewById(R.id.activity_image);
        textTitle.setText(values[position].What);
        textDescription.setText(values[position].description);
        if (values[position].ImagePath != "" && !ImageUtility.SetImage(CommonUtility.GetFilePath() + values[position].ImagePath.toString().split("\\\\")[1] + ".png",
                activityImage, 150, 150)) {
            ImageUtility.GetActivityImage(values[position].ImagePath, activityImage, Global.TOKEN, 150, 150);
        }
        return rowView;
    }
}

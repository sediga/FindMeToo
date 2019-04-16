package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.CurrentActivity;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;

public class ProfileListAdapter extends ArrayAdapter<ProfileModel> {
    private final Context context;
    private final ProfileModel[] values;

    public ProfileListAdapter(Context context, ProfileModel[] values) {
        super(context, R.layout.users_list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.users_list, parent, false);
        CheckBox selectBox = rowView.findViewById(R.id.checkbox_select);
        ImageButton profileImage = rowView.findViewById(R.id.profile_image);
        TextView profileUser = rowView.findViewById(R.id.text_profile_user);
        profileUser.setText(values[position].getUserName());
        profileImage = rowView.findViewById(R.id.profile_image);
        ImageUtility.GetProfileImage(profileImage, Global.TOKEN, values[position].getDeviceId(), 90, 90);
        return rowView;
    }
}

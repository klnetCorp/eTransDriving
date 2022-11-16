package kr.co.klnet.aos.etransdriving.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import kr.co.klnet.aos.etransdriving.CameraFrameActivity;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.model.CloudTextListItem;

import java.util.ArrayList;

public class CloudListItemAdaptor  extends BaseAdapter {

    LayoutInflater mLayoutInflater = null;
    Context mContext = null;
    ArrayList<CloudTextListItem> mData;

    public CloudListItemAdaptor(Context context, ArrayList<CloudTextListItem> data) {
        mContext = context;
        mData = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public CloudTextListItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        View view = super.getView(position, convertView, parent);
        View view = mLayoutInflater.inflate(R.layout.cloud_text_list_item, null);

        ImageButton btnSelect = (ImageButton)view.findViewById(R.id.btnSelect);
        EditText cloudText = (EditText)view.findViewById(R.id.detectedText);

        btnSelect.setImageResource(android.R.drawable.ic_input_add);
        btnSelect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO : click event
                String text = cloudText.getText().toString();

                CameraFrameActivity activity = (CameraFrameActivity)mContext;
                activity.finishTakePhoto(text);

            }
        });
        cloudText.setText(mData.get(position).getDescription());

        return view;
    }
}

package atguigu.com.sjyytest001.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import atguigu.com.sjyytest001.R;
import atguigu.com.sjyytest001.domain.LocalVideoInfo;
import atguigu.com.sjyytest001.util.Utils;

/**
 * Created by ASUS on 2017/5/19.
 */

public class LocalVideoAdapter extends BaseAdapter {

    private ArrayList<LocalVideoInfo> videoInfos;
    private Context context;
    private Utils utils;
    public LocalVideoAdapter( Context context,ArrayList<LocalVideoInfo> videoInfos) {
        this.videoInfos = videoInfos;
        this.context = context;
        utils = new Utils();
    }

    @Override
    public int getCount() {
        return videoInfos == null? 0: videoInfos.size();
    }

    @Override
    public LocalVideoInfo getItem(int position) {
        return videoInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_local_video, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //根据位置得到对应的数据
        LocalVideoInfo mediaItem = videoInfos.get(position);
        viewHolder.tv_name.setText(mediaItem.getName());
        viewHolder.tv_size.setText(Formatter.formatFileSize(context, mediaItem.getSize()));
        viewHolder.tv_duration.setText(utils.stringForTime((int) mediaItem.getDuration()));

        return convertView;
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }
}

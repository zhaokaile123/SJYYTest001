package atguigu.com.sjyytest001.fragmentPager;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import atguigu.com.sjyytest001.R;
import atguigu.com.sjyytest001.activity.PlayerActivity;
import atguigu.com.sjyytest001.adapter.LocalVideoAdapter;
import atguigu.com.sjyytest001.basefragment.BaseFragment;
import atguigu.com.sjyytest001.domain.LocalVideoInfo;

/**
 * Created by ASUS on 2017/5/19.
 */

public class LocalVideoFragment extends BaseFragment {

    private TextView tv_nodata;
    private ListView lv;
    private ArrayList<LocalVideoInfo> videoInfos;
    private LocalVideoAdapter adapter;


    @Override
    public View initView() {

        View view = View.inflate(context, R.layout.fragment_localvideo,null);
        lv = (ListView) view.findViewById(R.id.lv);
        tv_nodata = (TextView) view.findViewById(R.id.tv_nodata);

        //点击事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //LocalVideoInfo item = adapter.getItem(position);

               /* //传递视频地址uri   吊起视频播放器
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.setDataAndType(Uri.parse(item.getData()),"video*//*");

                startActivity(intent);*/



                /*
                * 传递视频列表  要在视频信息domain 中实现序列化
                * */   //传递意图到播放器类
                Intent intent = new Intent(context, PlayerActivity.class);

                Bundle bunlder = new Bundle();
                bunlder.putSerializable("videolist",videoInfos);  //把这个点击的对象放进去
                intent.putExtra("position",position);  //后面要用到当前点击的position 所以传过去
                //放入Bundler
                intent.putExtras(bunlder);
                startActivity(intent);


            }
        });
        return view;
    }
    public void initData(){
        getData();
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(videoInfos != null && videoInfos.size()>0) {
                tv_nodata.setVisibility(View.GONE);
                adapter = new LocalVideoAdapter(context,videoInfos);
                lv.setAdapter(adapter);
            }else {
                tv_nodata.setVisibility(View.VISIBLE);
            }
        }
    };

    private void getData() {  //得到信息  开启线程  得到本地视频的信息  添加到信息结合中
        videoInfos = new ArrayList<>();
        new Thread(new Runnable() {   //开子线程  遍历列表信息
            @Override
            public void run() {

                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA
                };
                Cursor cursor = resolver.query(uri,objs,null,null,null);

                if(cursor != null) {
                    while (cursor.moveToNext()){
                        String name = cursor.getString(cursor.getColumnIndex( MediaStore.Video.Media.DISPLAY_NAME));
                        long duration = cursor.getLong(cursor.getColumnIndex( MediaStore.Video.Media.DURATION));
                        long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

                        videoInfos.add(new LocalVideoInfo(name,duration,size,url));

                    }
                    cursor.close();
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

}

package atguigu.com.sjyytest001.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import atguigu.com.sjyytest001.R;

public class PlayerActivity extends AppCompatActivity {

    private VideoView vv;
    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        vv = (VideoView)findViewById(R.id.vv);
        uri = getIntent().getData();

        //播放 准备工作  完毕 时 调用
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                vv.start();
            }
        });

        //播放完了调用
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(PlayerActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                return false;
            }
        });

        vv.setVideoURI(uri);
        vv.setMediaController(new MediaController(this));
    }


}

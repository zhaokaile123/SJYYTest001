package atguigu.com.sjyytest001.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import atguigu.com.sjyytest001.R;

/**
 * Created by ASUS on 2017/5/19.
 */
//标题栏 的存放 实现类
public class titleView extends LinearLayout implements View.OnClickListener {

    public Context context;
    private TextView tv_sousuokuang;
    private RelativeLayout rl_game;
    private ImageView jilu;

    public titleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override//布局加载完成的时候回调
    protected void onFinishInflate() {
        super.onFinishInflate();

        tv_sousuokuang = (TextView) getChildAt(1);
        rl_game = (RelativeLayout) getChildAt(2);
        jilu = (ImageView) getChildAt(3);

        tv_sousuokuang.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        jilu.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_sousuokuang:
                Toast.makeText(context, "搜索", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_game:
                Toast.makeText(context, "游戏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.jilu:
                Toast.makeText(context, "播放记录", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

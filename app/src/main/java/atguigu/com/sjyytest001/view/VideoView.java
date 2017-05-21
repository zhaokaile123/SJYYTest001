package atguigu.com.sjyytest001.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by ASUS on 2017/5/21.
 */

/*   将此类的全类名  给 引用 的 videoview
* 重写VideoView   是为了 重新测量视频的长宽  自己定义填充*/
public class VideoView extends android.widget.VideoView{
    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    //重写测量方法
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //保存测量结果
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    public void setVideoSize(int width,int hight){   //设置视频 的宽和高
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = hight;
        setLayoutParams(params);

    }
}

package atguigu.com.sjyytest001;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import java.util.ArrayList;

import atguigu.com.sjyytest001.basefragment.BaseFragment;
import atguigu.com.sjyytest001.fragmentPager.LocalMusicFragment;
import atguigu.com.sjyytest001.fragmentPager.LocalVideoFragment;
import atguigu.com.sjyytest001.fragmentPager.NetMusicFragment;
import atguigu.com.sjyytest001.fragmentPager.NetVideoFragment;

public class MainActivity extends AppCompatActivity {

    private int position;
    private Fragment tempFragment;
    private ArrayList<BaseFragment> fragments;
    private RadioGroup rg_main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_main = (RadioGroup) findViewById(R.id.rg_main);

        //把各个Fragment 添加到 结合中
        initFragment();

        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.local_video:
                        position = 0;
                        break;
                    case R.id.local_music:
                        position = 1;
                        break;
                    case R.id.net_music:
                        position = 2;
                        break;
                    case R.id.net_video:
                        position = 3;
                        break;
                }

                BaseFragment currentFrament = fragments.get(position);
                addFragment(currentFrament);

            }

        });
        rg_main.check(R.id.local_video);
    }

    private void initFragment() {

        fragments = new ArrayList<>();

        fragments.add(new LocalVideoFragment());
        fragments.add(new LocalMusicFragment());
        fragments.add(new NetMusicFragment());
        fragments.add(new NetVideoFragment());
    }

    //添加fragment
    private void addFragment(BaseFragment currentFrament) {

        if( tempFragment != currentFrament) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if(!currentFrament.isAdded()) {
                if(tempFragment != null) {
                    ft.hide(tempFragment);
                }
                ft.add(R.id.fl_content,currentFrament).commit();
            }else{
                if(tempFragment != null) {
                    ft.hide(tempFragment);
                }
                ft.show(currentFrament).commit();
            }
            tempFragment = currentFrament;
        }

    }


}

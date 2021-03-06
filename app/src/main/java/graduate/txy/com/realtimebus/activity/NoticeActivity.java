package graduate.txy.com.realtimebus.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import graduate.txy.com.realtimebus.R;

/**
 * Created by lenovo on 2016/3/20.
 */
public class NoticeActivity extends Activity {
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无title
        setContentView(R.layout.activity_notice);

        tv = (TextView)findViewById(R.id.tv_map_title);
        tv.setText(getIntent().getStringExtra("name"));
    }


    /**
     * 摁键返回
     *
     * @param view
     */
    public void returnActivity(View view) {
        backActivity();
    }

    //返回finish和设置跳转动画
    private void backActivity() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {// 按返回键时退出Activity的Activity特效动画
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            backActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

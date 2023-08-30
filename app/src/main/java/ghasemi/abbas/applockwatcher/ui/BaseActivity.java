package ghasemi.abbas.applockwatcher.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ghasemi.abbas.applockwatcher.builder.FileLog;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.components.TextView;

import java.util.Date;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class BaseActivity extends AppCompatActivity {

    private boolean disableLockActivity;
    private LinearLayout actionBar, root;
    private ImageView back, more;
    private TextView title;
    private int StackFragment = 0;
    private long x;

    private PermissionsResult permissionsResult;

    private final ActivityResultLauncher<String[]> permissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (permissionsResult != null) {
                    permissionsResult.onResult(result);
                }
            }
    );

    public void requestPermissions(PermissionsResult permissionsResult, String... permissions) {
        this.permissionsResult = permissionsResult;
        this.permissions.launch(permissions);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);

        root = findViewById(R.id.root);
        actionBar = findViewById(R.id.actionBar);
        title = findViewById(R.id.title);
        back = findViewById(R.id.back);
        more = findViewById(R.id.more);
        onCreate();
        disableLockActivity();
    }

    public void disableLockActivity() {
        disableLockActivity = true;
    }

    public void enableLockActivity() {
        disableLockActivity = true;
    }

    public void pushFragment(Fragment fragment) {
        StackFragment++;
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.root_frag, fragment)
                .commit();
    }

    public boolean popFragment() {
        if (StackFragment > 0) {
            StackFragment--;
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .remove(manager.findFragmentById(R.id.root_frag))
                    .commit();
            return true;
        }
        return false;
    }


    @Override
    public void startActivity(Intent intent) {
        disableLockActivity();
        super.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (disableLockActivity) {
            disableLockActivity = false;
            return;
        }
        Intent intent = new Intent(this, Launcher.class);
        intent.putExtra("FLAG_ACTIVITY_LOADER", false);
        startActivity(intent);
    }

    protected void onCreate() {

    }

    public void hideActionBar() {
        root.removeView(actionBar);
        try {
            ViewGroup viewGroup = (ViewGroup) actionBar.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(actionBar);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        actionBar = null;
    }

    public void hideBackBtn() {
        if (actionBar == null) {
            return;
        }
        actionBar.removeView(back);
        back = null;
    }

    public void setLayout(int layout) {
        root.addView(LayoutInflater.from(this).inflate(layout, null));
    }

    public void setView(View view) {
        root.addView(view);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }


    public String getActivityTitle() {
        return this.title.getText().toString();
    }


    public void back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (back != null) {
            finish();
        } else {
            long currentTime = new Date().getTime();
            if ((currentTime - x) > 2000) {
                x = currentTime;
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void createItemActionBar(int idDrawable, View.OnClickListener listener) {
        more.setImageResource(idDrawable);
        more.setOnClickListener(listener);
        more.setVisibility(View.VISIBLE);
    }

    public interface PermissionsResult {
        void onResult(Map<String, Boolean> result);
    }
}

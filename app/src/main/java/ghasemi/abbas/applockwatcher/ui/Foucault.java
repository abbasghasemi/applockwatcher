package ghasemi.abbas.applockwatcher.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.farasource.component.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.Permission;
import ghasemi.abbas.applockwatcher.components.Switch;
import ghasemi.abbas.applockwatcher.components.TextView;
import io.reactivex.disposables.Disposable;

public class Foucault extends BaseActivity {

    private ArrayList<HashMap<String, Object>> hashMaps;
    private RecyclerView recyclerView;
    Switch history;
    private Disposable disposable;

    @Override
    protected void onCreate() {
        setLayout(R.layout.foucoult);
        setTitle(BuildApp.getString(R.string.recorded_images));
        recyclerView = findViewById(R.id.recyclerView);
        createItemActionBar(R.drawable.ic_delete, view -> {
            final BottomSheetDialog dialog = new BottomSheetDialog(Foucault.this, R.style.BottomSheetDialogTheme);
            dialog.setContentView(R.layout.dialog_delete);
            dialog.show();
            MaterialButton ok = dialog.findViewById(R.id.ok);
            ok.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
            ok.setOnClickListener(v -> {
                dialog.dismiss();
                AppStatus.open().deleteImgSave(-1, null);
                hashMaps.clear();
                findViewById(R.id.not_found).setVisibility(View.VISIBLE);
                if (recyclerView.getAdapter() != null) {
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            });
            MaterialButton close = dialog.findViewById(R.id.close);
            close.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
            close.setOnClickListener(v -> dialog.dismiss());
        });

        history = findViewById(R.id.history);
        history.setChecked(TinyData.getInstance().getBool("recordedImages"));
        history.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && ContextCompat.checkSelfPermission(Foucault.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                new Permission(Foucault.this, v -> {
                    disableLockActivity();
                    requestPermissions(result -> history.setChecked(ContextCompat.checkSelfPermission(Foucault.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED), Manifest.permission.CAMERA);
                }, "مجوز camera", "برای ظبط تصویر هنگام اشتباه وارد شدن گذرواژه، نیاز است دسترسی به دوربین به برنامه اهدا گردد.", R.drawable.ic_round_camera_24);
                history.setChecked(false);
            } else {
                TinyData.getInstance().putBool("recordedImages", b);
            }
        });
        findViewById(R.id.History).setOnClickListener(view -> history.setChecked(!history.isChecked()));

        disposable = AppStatus.open().getImgSave().subscribe(data -> {
            hashMaps = data;
            if (hashMaps.isEmpty()) {
                findViewById(R.id.not_found).setVisibility(View.VISIBLE);
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(Foucault.this, 2));
                recyclerView.setAdapter(new Adapter());
            }
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) disposable.dispose();
        setResult(0, null);
        super.onDestroy();
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {


        @NonNull
        @Override
        public Adapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View linearLayout = LayoutInflater.from(Foucault.this).inflate(R.layout.row_img, null);
//            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new Adapter.Holder(linearLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.Holder holder, int position) {
            HashMap<String, Object> hashMap = hashMaps.get(position);
            holder.imageView.setImageURI(Uri.fromFile(new File((String) hashMap.get("img"))));
            holder.icon.setImageDrawable((Drawable) hashMap.get("icon"));
            holder.date.setText((String) hashMap.get("date"));
        }

        @Override
        public int getItemCount() {
            return hashMaps.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            TextView date;
            AppCompatImageView imageView;
            AppCompatImageView icon;

            Holder(final View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.img);
                date = itemView.findViewById(R.id.date);
                icon = itemView.findViewById(R.id.icon);
                itemView.setOnClickListener(view -> {
                    final PopupWindow popupWindow = new PopupWindow();
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setFocusable(true);
                    View view1 = LayoutInflater.from(Foucault.this).inflate(R.layout.list_img, null);
                    AppCompatImageView icon = view1.findViewById(R.id.icon);
                    TextView name = view1.findViewById(R.id.name);
                    icon.setImageDrawable((Drawable) hashMaps.get(getLayoutPosition()).get("icon"));
                    name.setText((String) hashMaps.get(getLayoutPosition()).get("name"));
                    view1.findViewById(R.id.text1).setOnClickListener(view22 -> {
                        popupWindow.dismiss();
                        Intent intent = new Intent(Foucault.this, ImageView.class);
                        intent.putExtra("imagePath", (String) hashMaps.get(getLayoutPosition()).get("img"));
                        startActivity(intent);
                    });
                    view1.findViewById(R.id.text2).setOnClickListener(view2 -> {
                        popupWindow.dismiss();
                        AppStatus.open().deleteImgSave((Integer) hashMaps.get(getLayoutPosition()).get("id"), (String) hashMaps.get(getLayoutPosition()).get("img"));
                        hashMaps.remove(getLayoutPosition());
                        notifyItemRemoved(getLayoutPosition());
                        new Handler().postDelayed(() -> {
                            recyclerView.getAdapter().notifyDataSetChanged();
                            if (hashMaps.isEmpty()) {
                                findViewById(R.id.not_found).setVisibility(View.VISIBLE);
                            }
                        }, 500);
                    });
                    popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow.setContentView(view1);
                    popupWindow.showAsDropDown(imageView);
                });
            }
        }
    }
}

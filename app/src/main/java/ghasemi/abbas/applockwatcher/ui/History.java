package ghasemi.abbas.applockwatcher.ui;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.farasource.component.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.EdgeDecorator;
import ghasemi.abbas.applockwatcher.components.Switch;
import ghasemi.abbas.applockwatcher.components.TextView;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;
import java.util.HashMap;

public class History extends BaseActivity {

    ArrayList<HashMap<String, Object>> hashMaps;
    private RecyclerView recyclerView;
    private Disposable disposable;

    @Override
    protected void onCreate() {
        setLayout(R.layout.history);
        setTitle(BuildApp.getString(R.string.history_traffic));
        recyclerView = findViewById(R.id.recyclerView);
        createItemActionBar(R.drawable.ic_delete, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog dialog = new BottomSheetDialog(History.this, R.style.BottomSheetDialogTheme);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.show();
                MaterialButton ok = dialog.findViewById(R.id.ok);
                ok.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        AppStatus.open().deleteHistory();
                        hashMaps.clear();
                        findViewById(R.id.not_found).setVisibility(View.VISIBLE);
                        if( recyclerView.getAdapter() != null) {
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    }
                });
                MaterialButton close = dialog.findViewById(R.id.close);
                close.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        final Switch history = findViewById(R.id.history);
        history.setChecked(TinyData.getInstance().getBool("storedLogins"));
        history.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                TinyData.getInstance().putBool("storedLogins", b);
            }
        });
        findViewById(R.id.History).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.setChecked(!history.isChecked());
            }
        });

        disposable = AppStatus.open().getHistory().subscribe(data -> {
            hashMaps = data;
            if (hashMaps.isEmpty()) {
                findViewById(R.id.not_found).setVisibility(View.VISIBLE);
            } else {
                EdgeDecorator edgeDecorator = new EdgeDecorator();
                edgeDecorator.setLeft(60);
                edgeDecorator.setRight(10);
                recyclerView.addItemDecoration(edgeDecorator);
                recyclerView.setLayoutManager(new LinearLayoutManager(History.this, LinearLayoutManager.VERTICAL, false));
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
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(History.this).inflate(R.layout.row_history, null);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new Adapter.Holder(linearLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.Holder holder, int position) {
            HashMap<String, Object> hashMap = hashMaps.get(position);
            holder.name.setText((String) hashMap.get("name"));
            holder.imageView.setImageDrawable((Drawable) hashMap.get("icon"));
            holder.date.setText((String) hashMap.get("date"));
            switch (hashMap.get("type").toString()) {
                case "0":
                    holder.context.setText("باز شدن صفحه ورود");
                    holder.context.setTextColor(0xff2196F3);
                    break;
                case "2":
                    holder.context.setTextColor(0xffE91E63);
                    holder.context.setText("تلاش ناموفق برای ورود");
                    break;
                case "4":
                    holder.context.setTextColor(0xff4CAF50);
                    holder.context.setText("تماس با برنامه");
                    break;
                default:
                    holder.context.setTextColor(0xff8BC34A);
                    holder.context.setText("ورود موفق به برنامه");
            }

        }

        @Override
        public int getItemCount() {
            return hashMaps.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            TextView context, name, date;
            ImageView imageView;

            Holder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.icon);
                context = itemView.findViewById(R.id.context);
                name = itemView.findViewById(R.id.name);
                date = itemView.findViewById(R.id.date);
            }
        }
    }
}


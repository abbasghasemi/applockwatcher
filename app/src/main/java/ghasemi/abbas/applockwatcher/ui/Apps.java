package ghasemi.abbas.applockwatcher.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.FileLog;
import ghasemi.abbas.applockwatcher.components.EdgeDecorator;
import ghasemi.abbas.applockwatcher.components.Lock;
import ghasemi.abbas.applockwatcher.components.TextView;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class Apps extends BaseActivity {
    private Adapter adapter;
    public final ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> usage = new ArrayList<>();
    private View progressBar;
    private ImageView close;
    private View notFound;
    private Disposable disposable;

    @Override
    protected void onCreate() {
        setLayout(R.layout.apps);
        setTitle(BuildApp.getString(R.string.my_apps));
        final EditText search = findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notFound.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                if (charSequence.toString().trim().isEmpty()) {
                    close.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.GONE);
                    adapter.animateTo(hashMaps);
                } else {
                    close.setVisibility(View.VISIBLE);
                    ArrayList<HashMap<String, Object>> hashMapsSearch = new ArrayList<>();
                    for (HashMap<String, Object> hashMap : hashMaps) {
                        if (hashMap.get("name").toString().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                            hashMapsSearch.add(hashMap);
                        }
                    }
                    adapter.animateTo(hashMapsSearch);
                    if (usage.isEmpty()) {
                        notFound.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        RecyclerView recyclerView1 = findViewById(R.id.recyclerView);
        adapter = new Adapter();
        recyclerView1.setAdapter(adapter);
        EdgeDecorator edgeEffect = new EdgeDecorator();
        edgeEffect.setLeft(65);
        edgeEffect.setRight(10);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView1.addItemDecoration(edgeEffect);
        progressBar = findViewById(R.id.progressBar);
        close = findViewById(R.id.cloze);
        close.setOnClickListener(view -> {
            close.setVisibility(View.INVISIBLE);
            search.setText("");
        });
        notFound = findViewById(R.id.not_found);
        usage.clear();
        if (hashMaps.isEmpty()) {
            close.performClick();
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            disposable = getApps().subscribe(() -> {
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });
        } else {
            usage.addAll(hashMaps);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) disposable.dispose();
        super.onDestroy();
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.view> {

        @NonNull
        @Override
        public Adapter.view onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout linearLayout = (FrameLayout) LayoutInflater.from(Apps.this).inflate(R.layout.row_app, null);
            linearLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new Adapter.view(linearLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.view holder, int position) {
            holder.set(position);
        }

        @Override
        public int getItemCount() {
            return usage.size();
        }

        class view extends RecyclerView.ViewHolder {

            ImageView imageView;
            TextView textView;
            Lock aSwitch;

            public view(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.name);
                imageView = itemView.findViewById(R.id.icon);
                aSwitch = itemView.findViewById(R.id.lock);
            }

            void set(int p) {
                final HashMap<String, Object> hashMap = usage.get(p);
                imageView.setImageDrawable((Drawable) hashMap.get("icon"));
                textView.setText((String) hashMap.get("name"));
                aSwitch.setLock((Boolean) hashMap.get("isLock"));
                imageView.setOnClickListener(v -> {
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(hashMap.get("pkg").toString());
                        Apps.this.startActivity(intent);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                });
                itemView.setOnClickListener(view -> {
                    aSwitch.setLock(!aSwitch.isLock(), true);
                    hashMap.put("isLock", aSwitch.isLock());
                    StringBuilder s = new StringBuilder();
                    if (aSwitch.isLock()) {
                        AppStatus.open().add((String) hashMap.get("pkg"));
                    } else {
                        AppStatus.open().remove((String) hashMap.get("pkg"));
                    }
                    if (aSwitch.isLock()) {
                        itemView.setBackgroundColor(0xffF1F8E9);
                    } else {
                        itemView.setBackgroundColor(Color.TRANSPARENT);
                    }
                });
                if (aSwitch.isLock()) {
                    itemView.setBackgroundColor(0x1028B66D);
                } else {
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }

        private void animateTo(ArrayList<HashMap<String, Object>> models) {
            try {
                applyAndAnimateRemovals(models);
                applyAndAnimateAdditions(models);
                applyAndAnimateMovedItems(models);
            } catch (Exception e) {
                //
            }
        }

        private void applyAndAnimateRemovals(ArrayList<HashMap<String, Object>> newModels) {
            for (int i = usage.size() - 1; i >= 0; i--) {
                final HashMap<String, Object> model = usage.get(i);
                if (!newModels.contains(model)) {
                    removeItem(i);
                }
            }
        }

        private void applyAndAnimateAdditions(ArrayList<HashMap<String, Object>> newModels) {

            for (int i = 0, count = newModels.size(); i < count; i++) {
                final HashMap<String, Object> model = newModels.get(i);
                if (!usage.contains(model)) {
                    addItem(i, model);
                }
            }
        }

        private void applyAndAnimateMovedItems(ArrayList<HashMap<String, Object>> newModels) {
            for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
                final HashMap<String, Object> model = newModels.get(toPosition);
                final int fromPosition = usage.indexOf(model);
                if (fromPosition >= 0 && fromPosition != toPosition) {
                    moveItem(fromPosition, toPosition);
                }
            }
        }

        public void removeItem(int position) {
            usage.remove(position);
            notifyItemRemoved(position);
        }

        public void addItem(int position, HashMap<String, Object> model) {
            usage.add(position, model);
            notifyItemInserted(position);
        }

        public void moveItem(int fromPosition, int toPosition) {
            final HashMap<String, Object> model = usage.remove(fromPosition);
            usage.add(toPosition, model);
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    private Completable getApps()  {
        return Completable.create(emitter -> {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);
            PackageManager packageManager = getPackageManager();
            int i = 0;
            final ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<>();
            for (ResolveInfo resolveInfo : resolveInfoList) {
                try {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    String pkg = resolveInfo.activityInfo.packageName;
                    if (pkg.equals(Apps.this.getPackageName())) {
                        continue;
                    }
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkg, 0);
                    hashMap.put("pkg", pkg);
                    hashMap.put("icon", packageManager.getApplicationIcon(applicationInfo));
                    hashMap.put("name", packageManager.getApplicationLabel(applicationInfo));
                    boolean isLock = AppStatus.open().has(pkg);
                    hashMap.put("isLock", isLock);
                    hashMap.put("id", i++);
                    hashMaps.add(hashMap);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            Collections.sort(hashMaps, (o1, o2) -> {
                boolean b1 = (boolean) o1.get("isLock");
                boolean b2 = (boolean) o2.get("isLock");
                if (b1 == b2) return 0;
                return b1 ? -1 : 1;
            });
            Apps.this.hashMaps.addAll(hashMaps);
            Apps.this.usage.addAll(hashMaps);
            if (!emitter.isDisposed()) emitter.onComplete();
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }
}
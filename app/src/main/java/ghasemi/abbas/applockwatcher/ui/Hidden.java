package ghasemi.abbas.applockwatcher.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.FileParser;
import ghasemi.abbas.applockwatcher.builder.FilesCenter;
import ghasemi.abbas.applockwatcher.builder.OnBackPressedFragment;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.DocumentDrawable;
import ghasemi.abbas.applockwatcher.components.Lock;
import ghasemi.abbas.applockwatcher.components.TextView;
import ghasemi.abbas.applockwatcher.media.ImageViewHelper;
import ghasemi.abbas.applockwatcher.media.VideoPlayer;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Hidden extends BaseActivity implements OnBackPressedFragment {
    private final List<FileParser> files = new ArrayList<>();
    private View notFound;
    private RecyclerView recyclerView;
    private final FileAdapter fileAdapter = new FileAdapter();
    private int type;
    private String action;
    private Disposable disposable;
    private boolean isProgress = true;
    private boolean isChecked;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate() {
        super.onCreate();
        setLayout(R.layout.file);
        setTitle(getIntent().getStringExtra("title"));
        notFound = findViewById(R.id.not_found);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        action = getIntent().getStringExtra("action");
        type = getIntent().getIntExtra("type", 0);
        if (getIntent().getBooleanExtra("add", false)) {
            createItemActionBar(R.drawable.ic_add, v -> {
                Intent intent = new Intent(Hidden.this, Hidden.class);
                intent.putExtra("action", action);
                intent.putExtra("title", getIntent().getStringExtra("title2"));
                intent.putExtra("type", 1);
                startActivity(intent);
//                    finish();
            });
        }
        recyclerView.setAdapter(fileAdapter);
        refreshLayout = findViewById(R.id.swipe);
        refreshLayout.setEnabled(false);
        refreshLayout.setOnRefreshListener(() -> {
            if (!files.isEmpty()) {
                int size = files.size();
                files.clear();
                fileAdapter.notifyItemRangeRemoved(0, size);
                isProgress = true;
                fileAdapter.notifyItemInserted(0);
            }
            init();
        });
        init();
    }

    private void init() {
        notFound.setVisibility(View.INVISIBLE);
        if (type == 0) {
            disposable = FilesCenter.allFiles(action).subscribe(fileParsers -> {
                isProgress = false;
                fileAdapter.notifyItemRemoved(files.size());
                files.addAll(fileParsers);
                fileAdapter.notifyItemRangeInserted(0, files.size());
                if (files.isEmpty()) {
                    notFound.setVisibility(View.VISIBLE);
                } else {
                    startCheck();
                }
                refreshLayout.setEnabled(true);
                refreshLayout.setRefreshing(false);
            });
        } else if (type == 1) {
            FilesCenter.collectionsFiles(FilesCenter.root, action, false).subscribe(new Observer<List<FileParser>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disposable = d;
                }

                @Override
                public void onNext(List<FileParser> fileParsers) {
                    files.addAll(fileParsers);
                    fileAdapter.notifyItemRangeInserted(files.size() - fileParsers.size(), fileParsers.size());
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    if (files.isEmpty()) {
                        notFound.setVisibility(View.VISIBLE);
                    }
                    isProgress = false;
                    fileAdapter.notifyItemRemoved(files.size());
                    if (action.equals("file")) {
                        recyclerView.scrollToPosition(0);
                    }
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                }
            });
        } else if (type == 2) {
            disposable = FilesCenter.collectionsFiles((File) getIntent().getSerializableExtra("file"), action, true).singleElement().subscribe(fileParsers -> {
                if (fileParsers.isEmpty()) {
                    notFound.setVisibility(View.VISIBLE);
                }
                isProgress = false;
                fileAdapter.notifyItemRemoved(files.size());
                files.addAll(fileParsers);
                fileAdapter.notifyItemRangeInserted(0, files.size());
                refreshLayout.setEnabled(true);
                refreshLayout.setRefreshing(false);
            });
        }
    }

    private void startCheck() {
        if (isChecked) {
            return;
        }
        isChecked = true;
        boolean b = new Random().nextInt(11) > 9;
        if (b) {
            new Handler().postDelayed(() -> {
                if (!isFinishing()) BuildApp.rateApp(Hidden.this);
            }, 500);
        }
    }

    @Override
    public void onBackPressed() {
        if (!popFragment()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressedFragment(int pos) {
        popFragment();
        if (pos != -1) {
            if (files.get(pos).selected) {
                FilesCenter.showFile(files.get(pos).mFile);
            }
            files.remove(pos);
            fileAdapter.notifyItemRemoved(pos);
            if (files.isEmpty()) {
                notFound.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) disposable.dispose();
        super.onDestroy();
    }

    public class FileAdapter extends RecyclerView.Adapter<FileAdapter.Holder> {

        @NonNull
        @Override
        public FileAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(getLayoutInflater().inflate(i == 0 ? R.layout.row_file : R.layout.row_progress, null));
        }

        @Override
        public int getItemViewType(int position) {
            return isProgress && position == files.size() ? 1 : 0;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull final FileAdapter.Holder holder, final int i) {
            if (holder.getLayoutPosition() == files.size()) {
                holder.bindProgress();
            } else {
                holder.bindFile(files.get(holder.getLayoutPosition()));
            }
        }

        @Override
        public int getItemCount() {
            if (isProgress) return files.size() + 1;
            return files.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView type, name, duration;
            Lock mSwitch;

            Holder(@NonNull View itemView) {
                super(itemView);
                mSwitch = itemView.findViewById(R.id.lock);
                image = itemView.findViewById(R.id.image);
                type = itemView.findViewById(R.id.type);
                name = itemView.findViewById(R.id.name);
                if (name != null) {
                    name.setSelected(true);
                }
                duration = itemView.findViewById(R.id.duration);

            }

            public void bindProgress() {

            }

            public void bindFile(FileParser fileParser) {
                duration.setVisibility(View.GONE);
                image.setOnClickListener(null);
                image.setClickable(false);
                image.setFocusable(false);
                name.setText(fileParser.mFile.getName());
                if (fileParser.mFile.isDirectory()) {
                    type.setText(String.format("%s, %s %s", BuildApp.getString(R.string.folder), FilesCenter.countItemWithFilter(fileParser.mFile, action), BuildApp.getString(R.string.items)));
                    image.setImageResource(R.drawable.folder_file);
                    mSwitch.setVisibility(View.GONE);
                    itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(Hidden.this, Hidden.class);
                        intent.putExtra("action", action);
                        intent.putExtra("title", fileParser.mFile.getName());
                        intent.putExtra("type", 2);
                        intent.putExtra("file", fileParser.mFile);
                        startActivity(intent);
                    });
                } else {
                    type.setText(parseSize(fileParser));
                    mSwitch.setVisibility(View.VISIBLE);
                    mSwitch.setLock(fileParser.selected);
                    itemView.setOnClickListener(v -> {
                        mSwitch.setLock(!mSwitch.isLock(), true);
                        fileParser.selected = mSwitch.isLock();
                        if (mSwitch.isLock()) {
                            String[] n = fileParser.mFile.getName().split("\\.");
                            String type = FilesCenter.getTypeFile(n[n.length - 1]);
                            FilesCenter.hiddenFile(fileParser.mFile, type);
                            if (TinyData.getInstance().getString("backgroundImagePath").equals(fileParser.mFile.getPath())) {
                                TinyData.getInstance().putString("backgroundImagePath", "");
                                TinyData.getInstance().putLong("position_image_uri", 0);
                            }
                        } else {
                            FilesCenter.showFile(fileParser.mFile);
                        }
                    });

                    String[] n = fileParser.mFile.getName().split("\\.");
                    Drawable thumbDrawable = Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), FilesCenter.getThumbForNameOrMime(n[n.length - 1], true), null)).mutate();
                    if (n.length > 1) {
                        image.setImageDrawable(new DocumentDrawable(thumbDrawable, n[n.length - 1]));
                    } else {
                        image.setImageDrawable(new DocumentDrawable(thumbDrawable, "?"));
                    }

                    String originPath;
                    if (fileParser.selected) {
                        originPath = FilesCenter.createHiddenFile(fileParser.mFile).getPath();
                    } else {
                        originPath = fileParser.mFile.getPath();
                    }
                    switch (n[n.length - 1]) {
                        case "apk": {
                            PackageInfo pk = getPackageManager().getPackageArchiveInfo(originPath, PackageManager.GET_ACTIVITIES);
                            if (pk == null) {
                                image.setImageDrawable(new DocumentDrawable(thumbDrawable, "apk"));
                            } else {
                                ApplicationInfo info = pk.applicationInfo;
                                info.sourceDir = originPath;
                                info.publicSourceDir = originPath;
                                image.setImageDrawable(pk.applicationInfo.loadIcon(getPackageManager()));
                            }
                            image.setOnClickListener(view -> {
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setDataAndType(FileProvider.getUriForFile(Hidden.this,getPackageName() + ".fileProvider", new File(originPath)), "application/vnd.android.package-archive");
//                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
//                                intent.putExtra(Intent.EXTRA_RETURN_RESULT, false);
//                                intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationInfo().packageName);
//                                try {
//                                    startActivity(intent);
//                                } catch (Exception e) {
//                                    //
//                                }
                            });
                        }
                        break;
                        case "jpeg":
                        case "jpg":
                        case "png":
                        case "gif": {
                            Glide.with(Hidden.this)
                                    .load(originPath)
                                    .placeholder(new DocumentDrawable(thumbDrawable, n[n.length - 1]))
                                    .transition(new DrawableTransitionOptions().crossFade())
//                                    .transform(new RoundedCorners(BuildApp.dp(7)))
                                    .into(image);
                            image.setOnClickListener(v -> pushFragment(new ImageViewHelper(originPath, fileParser.mFile.getName(), getLayoutPosition())));
                        }
                        break;
                        case "mp4":
                        case "wmv":
                        case "mkv":
                        case "avi":
                        case "3gp": {
                            Glide.with(Hidden.this)
                                    .load(originPath)
                                    .placeholder(new DocumentDrawable(thumbDrawable, n[n.length - 1]))
                                    .transition(new DrawableTransitionOptions().crossFade())
//                                    .transform(new RoundedCorners(BuildApp.dp(7)))
                                    .into(image);
                            duration.setVisibility(View.VISIBLE);
                            duration.setText(parsTime(originPath));
                            image.setOnClickListener(v -> pushFragment(new VideoPlayer(originPath)));
                        }
                        break;
                        default:
                            image.setOnClickListener(v -> FilesCenter.openFile(originPath));
                    }
                }
            }
        }
    }

    private String parseSize(FileParser fileParser) {
        long bytes;
        if (fileParser.selected) {
            bytes = FilesCenter.createHiddenFile(fileParser.mFile).length();
        } else {
            bytes = fileParser.mFile.length();
        }
        if (bytes >= 1024 * 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f %s", (float) bytes / (1024 * 1024 * 1024), "GB");
        } else if (bytes >= 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f %s", (float) bytes / (1024 * 1024), "MB");
        } else if (bytes >= 1024) {
            return String.format(Locale.getDefault(), "%.1f %s", (float) bytes / 1024, "KB");
        }
        return String.format(Locale.getDefault(), "%.1f %s", (float) bytes, "B");
    }


    private String parsTime(String url) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, Uri.fromFile(new File(url)));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillie = Long.parseLong(time);
            retriever.release();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.close();
            }
            return convertMillieToHMmSs(timeInMillie);
        } catch (Exception e) {
            return "E:P:T";
        }
    }


    public static String convertMillieToHMmSs(long millie) {
        long seconds = (millie / 1000);
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;
        if (hour > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
        }
    }
}

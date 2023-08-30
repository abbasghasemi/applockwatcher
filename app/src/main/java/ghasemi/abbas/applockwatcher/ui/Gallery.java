package ghasemi.abbas.applockwatcher.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.TinyData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Gallery extends BaseActivity {

    private ImageView imageView;
    private Adapter adapter;
    private ArrayList<Bundle> list = new ArrayList<>();
    private int onClick = (int) TinyData.getInstance().getLong("position_image_uri");
    private View progressBar;

    @Override
    protected void onCreate() {

        setLayout(R.layout.gallery);
        setTitle(BuildApp.getString(R.string.wallpaper));

        imageView = findViewById(R.id.image);
        progressBar = findViewById(R.id.progressBar);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter = new Adapter());
        String url = TinyData.getInstance().getString("backgroundImagePath");
        if (!url.isEmpty()) {
            File imgFile = new File(url);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            } else {
                TinyData.getInstance().putString("backgroundImagePath", "");
            }
        }
        createItemActionBar(R.drawable.ic_check, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressBar.getVisibility() == View.GONE && imageView.getDrawable() != null && onClick != 0) {
                    Bitmap anImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/AppLock/";

                    try {
                        File file = new File(extStorageDirectory, list.get(onClick).getString("id") + ".jpg");
                        FileOutputStream outStream = new FileOutputStream(file);
                        anImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        TinyData.getInstance().putString("backgroundImagePath", extStorageDirectory + list.get(onClick).getString("id") + ".jpg");
                        TinyData.getInstance().putLong("position_image_uri", onClick);
                        BuildApp.toast("پس زمینه تغییر یافت.");
                    } catch (Exception e) {
                        BuildApp.toast("خطا در ذخیره تصویر.");
                    }
                    finish();
                } else {
                    BuildApp.toast("هنوز تصویر بارگزاری نشده است!");
                }
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        String json = get_wallpaper();
        init(json);
        progressBar.setVisibility(View.GONE);
    }

    private void init(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            list.add(null);
            for (int i = 0; i < jsonArray.length(); i++) {
                Bundle bundle = new Bundle();
                bundle.putString("small_img", jsonArray.getJSONObject(i).getString("small_img"));
                bundle.putString("large_img", jsonArray.getJSONObject(i).getString("large_img"));
                bundle.putString("id", jsonArray.getJSONObject(i).getString("id"));
                list.add(bundle);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String get_wallpaper() {
       return "{ \"items\": [ { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/5dztcwqmauxsqyo/1.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/5dztcwqmauxsqyo/1.jpg\", \"id\": 12 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/ps9y0o3uq6dzn3y/2.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/ps9y0o3uq6dzn3y/2.jpg\", \"id\": 13 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/83o7m9o1zy7ksje/3.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/83o7m9o1zy7ksje/3.jpg\", \"id\": 14 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/rq55zfwhhtjdjqm/4.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/rq55zfwhhtjdjqm/4.jpg\", \"id\": 15 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/zxuw6f1wf78nqml/5.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/zxuw6f1wf78nqml/5.jpg\", \"id\": 16 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/rwqmdzl14mitu62/6.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/rwqmdzl14mitu62/6.jpg\", \"id\": 17 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/36ve4cfp754or8l/7.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/36ve4cfp754or8l/7.jpg\", \"id\": 18 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/iag6jf60zg6t2fz/8.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/iag6jf60zg6t2fz/8.jpg\", \"id\": 19 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/nazqyifk4dp8czx/9.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/nazqyifk4dp8czx/9.jpg\", \"id\": 20 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/rk356vbyk927vhm/10.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/rk356vbyk927vhm/10.jpg\", \"id\": 21 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/m371mqdw5gy7pki/11.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/m371mqdw5gy7pki/11.jpg\", \"id\": 22 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/2smu69my6b4nc8q/12.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/2smu69my6b4nc8q/12.jpg\", \"id\": 23 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/degf12933r5ycwq/13.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/degf12933r5ycwq/13.jpg\", \"id\": 24 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/ie7lw0ugszx1w2j/14.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/ie7lw0ugszx1w2j/14.jpg\", \"id\": 25 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/htyv1j6qk5jomxj/15.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/htyv1j6qk5jomxj/15.jpg\", \"id\": 26 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/5x849jpq86756r7/16.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/5x849jpq86756r7/16.jpg\", \"id\": 27 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/m58k60ohttg6cv4/1.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/m58k60ohttg6cv4/1.jpg\", \"id\": 1 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/s33fj3lj4wftqok/2.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/s33fj3lj4wftqok/2.jpg\", \"id\": 2 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/gmosr8g5gx6qqb1/3.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/gmosr8g5gx6qqb1/3.jpg\", \"id\": 3 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/c5rp3b4z7t93yd4/4.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/c5rp3b4z7t93yd4/4.jpg\", \"id\": 4 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/5270d5i3pp5d02i/5.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/5270d5i3pp5d02i/5.jpg\", \"id\": 5 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/rdx36qfjhgv9w9t/6.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/rdx36qfjhgv9w9t/6.jpg\", \"id\": 6 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/9bfuz8vqed7dgop/7.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/9bfuz8vqed7dgop/7.jpg\", \"id\": 7 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/pvyyhnyv4j60ntw/8.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/pvyyhnyv4j60ntw/8.jpg\", \"id\": 8 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/l5f7iyq8lyvnk2i/9.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/l5f7iyq8lyvnk2i/9.jpg\", \"id\": 9 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/cnm1qwixz0jks7m/10.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/cnm1qwixz0jks7m/10.jpg\", \"id\": 10 }, { \"large_img\": \"https://www.dl.dropboxusercontent.com/s/38pr6ni5bmgpjkk/11.jpg\", \"small_img\": \"https://www.dl.dropboxusercontent.com/s/38pr6ni5bmgpjkk/11.jpg\", \"id\": 11 } ] }";
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        @NonNull
        @Override
        public Adapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new Holder(LayoutInflater.from(Gallery.this).inflate(R.layout.row_img_back, null));
        }

        @Override
        public void onBindViewHolder(@NonNull final Adapter.Holder holder, final int position) {
            if (position > 0) {
                Glide.with(Gallery.this).load(list.get(position).getString("small_img")).into(holder.imageView);
                holder.itemView.setBackgroundColor(0x26000000);
                holder.back_image.setImageResource(R.drawable.ic_photo_size);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int p = onClick;
                        onClick = position;
                        adapter.notifyItemChanged(p);
                        holder.click.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        Glide.with(Gallery.this).load(list.get(position).getString("large_img")).into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                imageView.setImageDrawable(resource);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    }
                });
            } else {
                holder.itemView.setBackgroundColor(0);
                holder.back_image.setImageResource(R.drawable.fingerprint_dialog_error);
                holder.imageView.setOnClickListener(null);
                holder.imageView.setFocusable(false);
                holder.imageView.setClickable(false);
                holder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.fingerprint_dialog_error));
            }

            if (onClick != 0 && position == onClick) {
                holder.click.setVisibility(View.VISIBLE);
            } else {
                holder.click.setVisibility(View.INVISIBLE);
            }

            if (position == 0 || position == list.size() - 1) {
                holder.line.setVisibility(View.GONE);
            } else {
                holder.line.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView imageView, back_image;
            View click, line;

            Holder(View itemView) {
                super(itemView);
                click = itemView.findViewById(R.id.click);
                line = itemView.findViewById(R.id.line);
                back_image = itemView.findViewById(R.id.back_image);
                imageView = itemView.findViewById(R.id.image);
            }
        }

    }

}

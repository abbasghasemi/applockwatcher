package ghasemi.abbas.applockwatcher.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.RecommendedApps;

public final class RecommendedAppsView extends LinearLayout {
    private final RecyclerView list;
    private final ImageView toggle;
    private final AppsAdapter adapter = new AppsAdapter();
    private final int rowHeight;
    private boolean expanded = true;
    private ValueAnimator expansionAnimator;

    public RecommendedAppsView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.colorBackground));
        setPadding(dp(10), dp(2), dp(10), dp(2));
        setVisibility(GONE);
        rowHeight = dp(56);

        FrameLayout header = new FrameLayout(context);
        View rule = new View(context);
        rule.setBackgroundColor(0xffd9e5dd);
        FrameLayout.LayoutParams ruleParams = new FrameLayout.LayoutParams(-1, dp(1), Gravity.CENTER_VERTICAL);
        header.addView(rule, ruleParams);
        toggle = new ImageView(context);
        toggle.setImageResource(R.drawable.ic_recommended_toggle);
        toggle.setScaleType(ImageView.ScaleType.CENTER);
        toggle.setRotation(180f);
        toggle.setContentDescription(getResources().getString(R.string.hide_recommended_apps));
        toggle.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000),
                rounded(getResources().getColor(R.color.colorBackground), 0xffd9e5dd, dp(16)),
                rounded(Color.WHITE, 0, dp(16))));
        toggle.setOnClickListener(v -> toggleList());
        header.addView(toggle, new FrameLayout.LayoutParams(dp(34), dp(30), Gravity.CENTER));
        addView(header, new LinearLayout.LayoutParams(-1, dp(32)));

        list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        list.setAdapter(adapter);
        list.setOverScrollMode(OVER_SCROLL_NEVER);
        list.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left > 0) adapter.updateViewport(right - left);
        });
        addView(list, new LinearLayout.LayoutParams(-1, rowHeight));

        RecommendedApps.load(context, apps -> {
            adapter.submit(apps);
            setVisibility(apps.isEmpty() ? GONE : VISIBLE);
        });
    }

    private void toggleList() {
        if (expansionAnimator != null) expansionAnimator.cancel();
        expanded = !expanded;
        list.setVisibility(VISIBLE);
        int from = list.getLayoutParams().height;
        ValueAnimator animator = ValueAnimator.ofInt(from, expanded ? rowHeight : 0);
        animator.setDuration(220);
        animator.addUpdateListener(value -> {
            ViewGroup.LayoutParams params = list.getLayoutParams();
            params.height = (Integer) value.getAnimatedValue();
            list.setLayoutParams(params);
            list.setAlpha(params.height / (float) rowHeight);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (expansionAnimator == animator) {
                    if (!expanded) list.setVisibility(GONE);
                    expansionAnimator = null;
                }
            }
        });
        expansionAnimator = animator;
        animator.start();
        toggle.animate().rotation(expanded ? 180f : 0f).setDuration(220).start();
        toggle.setContentDescription(getResources().getString(expanded
                ? R.string.hide_recommended_apps : R.string.show_recommended_apps));
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(radius);
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private final class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.Holder> {
        private final List<RecommendedApps.App> apps = new ArrayList<>();
        private int viewport;
        private final int gap = dp(6);

        void submit(List<RecommendedApps.App> items) {
            apps.clear();
            apps.addAll(items);
            notifyDataSetChanged();
        }

        void updateViewport(int width) {
            if (viewport != width) {
                viewport = width;
                notifyDataSetChanged();
            }
        }

        @Override public int getItemCount() { return apps.size(); }

        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            Context context = parent.getContext();
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(7), dp(3), dp(7), dp(3));
            card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000),
                    rounded(Color.TRANSPARENT, 0xffd9e5dd, dp(9)),
                    rounded(Color.WHITE, 0, dp(9))));
            card.setClickable(true);
            card.setLayoutParams(new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, -1));

            ImageView icon = new ImageView(context);
            icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(34)));
            LinearLayout details = new LinearLayout(context);
            details.setOrientation(VERTICAL);
            details.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(0, -1, 1f);
            detailParams.setMarginStart(dp(8));
            card.addView(details, detailParams);

            TextView title = new TextView(context);
            title.setTextSize(13);
            title.setTextColor(0xff212121);
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            title.setMarqueeRepeatLimit(-1);
            title.setSelected(true);
            details.addView(title, new LinearLayout.LayoutParams(-1, -2));
            TextView badge = new TextView(context);
            badge.setTextSize(10);
            badge.setMaxLines(1);
            badge.setEllipsize(TextUtils.TruncateAt.END);
            badge.setPadding(dp(6), 0, dp(6), 0);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
            badgeParams.topMargin = dp(1);
            details.addView(badge, badgeParams);
            return new Holder(card, icon, title, badge);
        }

        @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            RecommendedApps.App app = apps.get(position);
            int width = apps.size() == 1 ? viewport : apps.size() == 2
                    ? (viewport - gap) / 2 : (int) ((viewport - 2 * gap) / 2.15f);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.width = Math.max(dp(100), width);
            params.setMarginEnd(position == apps.size() - 1 ? 0 : gap);
            holder.itemView.setLayoutParams(params);
            holder.title.setText(app.title);
            holder.badge.setVisibility(app.badge == null ? GONE : VISIBLE);
            if (app.badge != null) {
                holder.badge.setText(app.badge);
                int badgeColor = 0xff0cdc73;
                if (app.badgeColor != null && app.badgeColor.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
                    try { badgeColor = Color.parseColor(app.badgeColor); } catch (IllegalArgumentException ignored) { }
                }
                holder.badge.setBackground(rounded(badgeColor, 0, dp(5)));
                int visible = ColorUtils.compositeColors(badgeColor, Color.WHITE);
                holder.badge.setTextColor(ColorUtils.calculateContrast(Color.BLACK, visible)
                        >= ColorUtils.calculateContrast(Color.WHITE, visible) ? Color.BLACK : Color.WHITE);
            }
            if (app.icon.startsWith("https://")) Glide.with(holder.icon).load(app.icon)
                    .placeholder(android.R.drawable.sym_def_app_icon).into(holder.icon);
            else holder.icon.setImageResource(android.R.drawable.sym_def_app_icon);
            holder.itemView.setOnClickListener(v -> {
                try { getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.url))); }
                catch (Exception ignored) { }
            });
        }

        final class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title, badge;
            Holder(View view, ImageView icon, TextView title, TextView badge) {
                super(view);
                this.icon = icon;
                this.title = title;
                this.badge = badge;
            }
        }
    }
}

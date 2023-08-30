package ghasemi.abbas.applockwatcher.components;


import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.builder.BuildApp;


public class EdgeDecorator extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int left = -1;
    private int right = -1;


    public EdgeDecorator() {
        mDivider = new ColorDrawable(ApplicationLoader.context.getResources().getColor(R.color.colorDivider));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null) {
            return;
        }
        if (parent.getChildAdapterPosition(view) < 1) {
            return;
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider.getIntrinsicHeight();
        } else {
            outRect.left = mDivider.getIntrinsicWidth();
        }
    }

    public void setLeft(int left) {
        this.left = BuildApp.dp(left);
    }

    public void setRight(int right) {
        this.right = BuildApp.dp(right);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null) {
            super.onDrawOver(c, parent, state);
            return;
        }

        // Initialization needed to avoid compiler warning
        int left = 0, right = 0, top = 0, bottom = 0;
        int orientation = getOrientation(parent);
        int childCount = parent.getChildCount();

        if (orientation == LinearLayoutManager.VERTICAL) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
        } else { //horizontal
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
        }

        for (int i = 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getTop() - params.topMargin;
                bottom = top + BuildApp.dp(.5f);
            } else { //horizontal
                left = child.getLeft() - params.leftMargin;
                right = left + BuildApp.dp(.5f);
            }
            mDivider.setBounds(this.left == -1 ? left : this.left, top, this.right == -1 ? right : (right - this.right), bottom);
            mDivider.draw(c);
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        } else {
            throw new IllegalStateException(
                    "DividerItemDecoration can only be used with a LinearLayoutManager.");
        }
    }
}
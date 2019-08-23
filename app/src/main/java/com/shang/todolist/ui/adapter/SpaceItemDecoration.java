package com.shang.todolist.ui.adapter;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * RecycleView的分隔间距
 * Created by shang on 2019/6/19.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    int mLeftSpace;
    int mTopSpace;
    int mRightSpace;
    int mBottomSpace;
    int extraSpace;//上下增加的额外的高度

    /**
     * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
     * the number of pixels that the item view should be inset by, similar to padding or margin.
     * The default implementation sets the bounds of outRect to 0 and returns.
     * <p>
     * <p>
     * If this ItemDecoration does not affect the positioning of item views, it should set
     * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
     * before returning.
     * <p>
     * <p>
     * If you need to access Adapter for additional data, you can call
     * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
     * View.
     *
     * @param outRect Rect to receive the output.
     * @param view    The child view to decorate
     * @param parent  RecyclerView this ItemDecoration is decorating
     * @param state   The current state of RecyclerView.
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = mLeftSpace;
        outRect.right = mRightSpace;
        outRect.bottom = mBottomSpace;
        outRect.top = mTopSpace;
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        //竖直方向的
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            if (parent.getChildAdapterPosition(view) == 0)
                outRect.top += extraSpace;
            if (parent.getChildAdapterPosition(view) == layoutManager.getItemCount() - 1)
                outRect.bottom += extraSpace;
        }
    }


    public SpaceItemDecoration(int leftSpace, int topSpace, int rightSpace, int bottomSpace, int extraSpace) {
        this.mLeftSpace = leftSpace;
        this.mTopSpace = topSpace;
        this.mRightSpace = rightSpace;
        this.mBottomSpace = bottomSpace;
        this.extraSpace = extraSpace;
    }
}
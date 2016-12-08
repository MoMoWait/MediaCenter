package com.rockchips.mediacenter.viewutils.pathheaderlayout;

import java.util.Stack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rockchips.mediacenter.viewutils.R;

/**
 * 页面顶部组件
 * @author t00181037 z160481
 * 
 */
public class PathHeaderLayout extends RelativeLayout
{

    private static final String TAG = "PathHeaderLayout";

    /**
     * 控制路径超过几级就省略表示的常量 默认为5级
     */
    private static final int PATH_STEP_SIZE = 5;

    /**
     * 路径省略符号
     */
    private static final String PATH_STEP_SUSPENSION_POINTS_SIGN = "...";

    /**
     * 显示ICON ImageView
     */
    private ImageView mImageView;

    /**
     * 当前浏览方式
     */
    private String mCurrentB = "";

    /**
     * 右边提示区域
     */
    private LinearLayout mRightMentTip;

    /**
     * 存放路径的栈
     */
    private Stack<PathItem> mPathStack;

    /**
     * 装载路径容器
     **/
    private LinearLayout mPathContainer;

    /**
     * 解析器
     */
    private LayoutInflater mInflater;

    /**
     * 路径默认宽度
     */
    private static final int PATH_LIMIT_WIDTH_DEFAULT = 150;

    /**
     * 路径显示长度限制 默认为150dip
     * */
    private int mPathLimitWidth = PATH_LIMIT_WIDTH_DEFAULT;
    /**
     * 默认字符串长度
     */
    private static final int STRING_LENGTH_DEFAULT = 8;

    /**
     * 默认截取字符串长度
     * */
    private int mStringLength = STRING_LENGTH_DEFAULT;

    private ImageView mIvRightMenuIcon;

    private ImageView mIvRightMenuIcon2;

    private TextView mTvRightMenuTip;

    private TextView mTvRightMenuTip2;

    public int getStringLength()
    {
        return mStringLength;
    }

    public void setStringLength(int length)
    {
        this.mStringLength = length;
    }

    public PathHeaderLayout(Context context)
    {
        super(context);
        init(context);
    }

    public PathHeaderLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public PathHeaderLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化布局文件
     */
    private void init(Context context)
    {
        Log.d(TAG, "init--start");

        this.mInflater = LayoutInflater.from(context);
        PathHeaderLayout.inflate(context, R.layout.path_header_layout, this);
        mImageView = (ImageView) this.findViewById(R.id.header_icon);
        mPathContainer = (LinearLayout) this.findViewById(R.id.pathContainer);
        mRightMentTip = (LinearLayout) this.findViewById(R.id.menu_tip);
        if (mPathStack == null)
        {
            mPathStack = new Stack<PathItem>();
        }

        mIvRightMenuIcon = (ImageView) this.findViewById(R.id.right_menu_icon);
        mIvRightMenuIcon2 = (ImageView) this.findViewById(R.id.right_menu_icon2);
        mTvRightMenuTip = (TextView) this.findViewById(R.id.right_menu_tip);
        mTvRightMenuTip2 = (TextView) this.findViewById(R.id.right_menu_tip2);
    }

    /**
     * 增加进入项目； 从F盘进入到根目录，再从根目录A进入到子目录B addPath(F) addPath(A) addPath(B)
     * 
     * 显示为F>A>B
     * 
     * 每次push都更新显示路径
     * 
     * @param path
     */
    public void pushPathStack(String path)
    {
        if (!"".equals(getCurrentB()))
        {
            PathItem item = mPathStack.peek();
            item.setDisplay(false);
            setRightMenuTipDisplay(false);
        }
        this.pushPathStack(path, R.drawable.next_path_icon);
    }

    /**
     * <一句话功能简述> 添加路径 <功能详细描述>
     * @param path 路径名
     * @param resId 间隔图片
     * @see [类、类#方法、类#成员]
     */
    public void pushPathStack(String path, int resId)
    {
        if (path != null && !"".equals(path.trim()) && mPathStack != null)
        {
            mPathStack.push(getCutStr(path, resId));
        }
        addPathView(mPathStack, resId);
    }

    /**
     * 点击“返回键”时，调用将stack最顶层的弹出stack，每次pop都更新下路径
     * */
    public String popPathStack()
    {
        String path = "";
        if (mPathStack != null && !mPathStack.isEmpty())
        {
            mPathStack.pop();
            if (!mPathStack.isEmpty())
            {
                // 判断stack顶部的路径是否是过滤路径，如果是则将路径显示标志位置为true
                PathItem pathItem = mPathStack.peek();
                if (getCurrentB().equals(pathItem.getmPathName()))// 英文没有考虑
                {
                    pathItem.setDisplay(true);
                    setRightMenuTipDisplay(true);
                }
                addPathView(mPathStack);
            }
            else
            {
                mPathContainer.removeAllViews();
            }
        }
        return path;
    }

    /**
     * 设置右边提示信息是否显示
     */
    public void setRightMenuTipDisplay(boolean display)
    {
        if (mRightMentTip != null)
        {
            if (display)
            {
                mRightMentTip.setVisibility(View.VISIBLE);
            }
            else
            {
                mRightMentTip.setVisibility(View.GONE);
            }
        }
    }
    
    public int getStackSize()
    {
        return mPathStack.size();
    }

    /**
     * wanghuanlai add for righteMenu set <一句话功能简述>设置头部信息的右侧提示
     * <功能详细描述>当参数为-1时表示隐藏,参数为-2时使用默认值
     * @param icon1
     * @param text1
     * @param icon2
     * @param text2
     * @see [类、类#方法、类#成员]
     */
    public void setRightMenuTips(int icon1, int text1, int icon2, int text2)
    {
        if (icon1 == 0)
        {
            mIvRightMenuIcon.setVisibility(View.GONE);
        }
        else
        {
            mIvRightMenuIcon.setImageResource(icon1);
        }

        if (text1 == 0)
        {
            mTvRightMenuTip.setVisibility(View.GONE);
        }
        else
        {
            mTvRightMenuTip.setText(text1);
        }

        if (icon2 == 0)
        {
            mIvRightMenuIcon2.setVisibility(View.GONE);
        }
        else
        {
            mIvRightMenuIcon2.setImageResource(icon2);
        }

        if (text2 == 0)
        {
            mTvRightMenuTip2.setVisibility(View.GONE);
        }
        else
        {
            mTvRightMenuTip2.setText(text2);
        }
    }

    /**
     * 返回路径名经过剪切后的pathitem
     **/
    private PathItem getCutStr(String str, int resId)
    {
        PathItem pathItem = new PathItem();
        if (str != null && str.length() > mStringLength)
        {
            pathItem.setmPahtName(str);
            pathItem.setmResid(resId);
            pathItem.setDisplay(true);
        }
        else
        {
            pathItem.setmPahtName(str);
            pathItem.setmResid(resId);
            pathItem.setDisplay(true);
        }
        return pathItem;
    }

    /**
     * 动态生成路径
     * @param stack 路径栈
     * @param resId 图片资源
     */
    private void addPathView(Stack<PathItem> stack)
    {
        this.addPathView(stack, R.drawable.next_path_icon);
    }

    /**
     * 动态生成路径
     * @param stack 路径栈
     * @param resId 图片资源
     */
    private void addPathView(Stack<PathItem> stack, int resId)
    {
        if (mPathContainer != null && stack != null && !stack.isEmpty())
        {
            mPathContainer.removeAllViews();
            int size = stack.size();
            String pathName = "";
            TextView pathTextViw = null;
            ImageView nextPathImageView = null;
            int imageWith = (computeWH(getResources(), R.drawable.next_path_icon))[0]; // 图片宽度
            int pathTotalWith = 0; // 路径总宽度
            if (size < PATH_STEP_SIZE)
            {
                PathItem item = null;

                for (int i = 0; i < size; i++)
                {
                    item = stack.get(i);
                    pathName = item.getmPathName();
                    pathTextViw = (TextView) mInflater.inflate(R.layout.path_item_textview, null);
                    nextPathImageView = (ImageView) mInflater.inflate(R.layout.path_item_imageview, null);
                    pathTextViw.setMaxWidth(mPathLimitWidth);
                    // 如果不显示pathitem
                    // 则将mPathContainer的前个imageview设为gone，且将不add TextViw
                    if (!item.isDisplay())
                    {
                        int childCount = mPathContainer.getChildCount();
                        View view = null;
                        if (childCount - 1 > 0)
                        {
                            view = mPathContainer.getChildAt(childCount - 1);
                        }

                        if (view != null)
                        {
                            view.setVisibility(View.GONE);
                        }
                    }
                    else
                    {
                        pathTextViw.setText(pathName);
                        mPathContainer.addView(pathTextViw);
                    }
                    nextPathImageView.setImageResource(item.getmResid());
                    if (i < size - 1)
                    {
                        mPathContainer.addView(nextPathImageView);
                    }
                    pathTotalWith = (int) (pathTotalWith + imageWith + getTextViewWith(pathTextViw));
                }
            }
            else
            {
                removeViewByLimit(pathTextViw, nextPathImageView, stack, size);
            }
        }

    }

    /**
     * 省略号代替路径
     */
    private void removeViewByLimit(TextView pathTextViw, ImageView nextPathImageView, Stack<PathItem> stack, int size)
    {
        pathTextViw = (TextView) mInflater.inflate(R.layout.path_item_textview, null);
        nextPathImageView = (ImageView) mInflater.inflate(R.layout.path_item_imageview, null);
        pathTextViw.setText(stack.get(0).getmPathName());
        mPathContainer.addView(pathTextViw);
        mPathContainer.addView(nextPathImageView);

        pathTextViw = (TextView) mInflater.inflate(R.layout.path_item_textview, null);
        nextPathImageView = (ImageView) mInflater.inflate(R.layout.path_item_imageview, null);
        pathTextViw.setText(stack.get(1).getmPathName());
        mPathContainer.addView(pathTextViw);
        mPathContainer.addView(nextPathImageView);

        pathTextViw = (TextView) mInflater.inflate(R.layout.path_item_textview, null);
        nextPathImageView = (ImageView) mInflater.inflate(R.layout.path_item_imageview, null);
        pathTextViw.setText(PATH_STEP_SUSPENSION_POINTS_SIGN);
        mPathContainer.addView(pathTextViw);
        mPathContainer.addView(nextPathImageView);

        pathTextViw = (TextView) mInflater.inflate(R.layout.path_item_textview, null);
        nextPathImageView = (ImageView) mInflater.inflate(R.layout.path_item_imageview, null);
        pathTextViw.setText(stack.get(size - 2).getmPathName());
        mPathContainer.addView(pathTextViw);
        mPathContainer.addView(nextPathImageView);

        pathTextViw = (TextView) mInflater.inflate(R.layout.path_item_textview, null);
        pathTextViw.setText(stack.get(size - 1).getmPathName());
        mPathContainer.addView(pathTextViw);
    }

    /**
     * 获取 TextView的with
     * */
    private float getTextViewWith(TextView textView)
    {
        float with = 0;
        if (textView != null)
        {
            Paint paint = new Paint();
            paint.setTextSize(textView.getTextSize());
            with = paint.measureText(textView.getText().toString());
        }
        return with;
    }

    /**
     * 计算ImageView的大小（decodeResource）
     * 
     * @param resources
     * @param resourceId
     * @return
     */
    private int[] computeWH(Resources resources, int resourceId)
    {
        int[] wh =
        { 0, 0 };
        if (resources == null)
        {
            return wh;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resourceId, options);
        if (options.mCancel || options.outWidth == -1 || options.outHeight == -1)
        {
            return wh;
        }

        wh[0] = options.outWidth;
        wh[1] = options.outHeight;

        return wh;
    }

    /**
     * 设置icon的资源ID，
     * 
     * @param iconId
     */
    public void setIcon(int iconId)
    {
        if (mImageView != null)
        {
            mImageView.setImageResource(iconId);
        }
    }

    public void setIcon(Bitmap icon)
    {
        if (mImageView != null)
        {
            mImageView.setImageBitmap(icon);
        }

    }

    /**
     ** 增加进入项目； 从F盘进入到根目录，再从根目录A进入到子目录B addPath(F) addPath(A) addPath(B)
     * 
     * 显示为F>A>B
     */
    public Stack<PathItem> getPathStack()
    {
        return mPathStack;
    }

    public int getPathLimitWith()
    {
        return mPathLimitWidth;
    }

    public void setPathLimitWith(int pathLimitWith)
    {
        this.mPathLimitWidth = pathLimitWith;
    }

    public String getCurrentB()
    {
        return mCurrentB;
    }

    public void setCurrentB(String mCurrentB)
    {
        this.mCurrentB = mCurrentB;
    }
}
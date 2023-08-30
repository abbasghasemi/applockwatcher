package ghasemi.abbas.applockwatcher.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.farasource.component.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.OnBackPressedFragment;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.TextView;
import ghasemi.abbas.applockwatcher.components.TouchImageView;

public class ImageViewHelper extends Fragment {

    private OnBackPressedFragment onBackPressedFragment;
    private String path, name;
    private LinearLayout action_bar;
    private TextView title;
    private int pos;
    private TouchImageView imageView;

    public ImageViewHelper(String url, String name, int pos) {
        path = url;
        this.name = name;
        this.pos = pos;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, null);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        action_bar = view.findViewById(R.id.action_bar);
        title = view.findViewById(R.id.title);
        title.setText(name);
        view.findViewById(R.id.back).setOnClickListener(v -> {
            if (onBackPressedFragment != null) {
                onBackPressedFragment.onBackPressedFragment(-1);
            }
            onBackPressedFragment = null;

        });
        imageView = view.findViewById(R.id.image_v);
        imageView.setImageURI(Uri.fromFile(new File(path)));

        imageView.setOnClickListener(v -> {
            if (action_bar.getVisibility() == View.INVISIBLE) {
                action_bar.setVisibility(View.VISIBLE);
            } else {
                action_bar.setVisibility(View.INVISIBLE);
            }
        });

        view.findViewById(R.id.more).setOnClickListener(v -> {
            View view1 = LayoutInflater.from(v.getContext()).inflate(R.layout.more_option, null);
            PopupWindow popupWindow = new PopupWindow();
            popupWindow.setFocusable(true);
            popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.setContentView(view1);
            popupWindow.showAsDropDown(v);
            view1.findViewById(R.id.edit).setOnClickListener(v15 -> {
                if (popupWindow.isShowing()) popupWindow.dismiss();
                Intent intent = new Intent(getActivity(), ghasemi.abbas.applockwatcher.ui.ImageView.class);
                intent.putExtra("imagePath", path);
                startActivity(intent);
            });
            view1.findViewById(R.id.delete).setOnClickListener(v14 -> {
                if (popupWindow.isShowing()) popupWindow.dismiss();
                final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.show();
                TextView title_dialog = dialog.findViewById(R.id.title_dialog);
                title_dialog.setText("آیا می خواهید تصویر حذف گردد؟");
                MaterialButton ok = dialog.findViewById(R.id.ok);
                ok.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Main-Bold.ttf"));
                ok.setOnClickListener(v13 -> {
                    dialog.dismiss();
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    if (onBackPressedFragment != null) {
                        onBackPressedFragment.onBackPressedFragment(pos);
                    }
                    onBackPressedFragment = null;
                });
                MaterialButton close = dialog.findViewById(R.id.close);
                close.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Main-Bold.ttf"));
                close.setOnClickListener(v12 -> dialog.dismiss());
            });
            view1.findViewById(R.id.wallpaper).setOnClickListener(v1 -> {
                if (popupWindow.isShowing()) popupWindow.dismiss();
                TinyData.getInstance().putString("backgroundImagePath", path);
                TinyData.getInstance().putLong("position_image_uri", 0);
                BuildApp.toast("پس زمینه تغییر یافت.");
            });
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onBackPressedFragment = (OnBackPressedFragment) getActivity();
        requireActivity().getWindow().setStatusBarColor(0xff2C2C2C);
        requireActivity().getWindow().setNavigationBarColor(0xff2C2C2C);
        BuildApp.windowInsetsControllerCompat(requireActivity().getWindow()).setAppearanceLightNavigationBars(false);
    }

    @Override
    public void onDetach() {
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        requireActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        BuildApp.windowInsetsControllerCompat(requireActivity().getWindow()).setAppearanceLightNavigationBars(true);
        super.onDetach();
    }
}

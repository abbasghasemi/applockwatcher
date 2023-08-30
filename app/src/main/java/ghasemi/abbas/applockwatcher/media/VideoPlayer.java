package ghasemi.abbas.applockwatcher.media;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.FilesCenter;

public class VideoPlayer extends Fragment {

    private String path;

    public VideoPlayer(String url) {
        path = url;
    }

    private VideoView videoPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_player, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoPlayer = view.findViewById(R.id.video_view);
        videoPlayer.setVideoPath(path);
        final MediaController mediaController = new MediaController(videoPlayer.getContext());
        mediaController.setMediaPlayer(videoPlayer);
        videoPlayer.setMediaController(mediaController);
        videoPlayer.setOnPreparedListener(mediaPlayer -> {
            View layout = mediaController.getChildAt(0);
            layout.setBackgroundColor(Color.TRANSPARENT);
            if (layout.getLayoutParams() instanceof FrameLayout.LayoutParams){
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
                params.leftMargin = BuildApp.dp(10);
                params.rightMargin = BuildApp.dp(10);
            }
        });
        videoPlayer.start();
        View openInNew = view.findViewById(R.id.open_in_new);
        openInNew.setOnClickListener(view1 -> {
            FilesCenter.openFile(path);
            videoPlayer.stopPlayback();
        });
        videoPlayer.setOnClickListener(v -> {
            if (openInNew.getVisibility() == View.VISIBLE) {
                openInNew.setVisibility(View.INVISIBLE);
            } else {
                openInNew.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getWindow().setStatusBarColor(0xff2C2C2C);
        requireActivity().getWindow().setNavigationBarColor(0xff2C2C2C);
        BuildApp.windowInsetsControllerCompat(requireActivity().getWindow()).setAppearanceLightNavigationBars(false);
    }

    @Override
    public void onDetach() {
        videoPlayer.stopPlayback();
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        requireActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        BuildApp.windowInsetsControllerCompat(requireActivity().getWindow()).setAppearanceLightNavigationBars(true);
        super.onDetach();
    }
}
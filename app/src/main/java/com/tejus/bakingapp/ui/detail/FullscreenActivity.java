package com.tejus.bakingapp.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.tejus.bakingapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullscreenActivity extends AppCompatActivity {

    private static final String LOG_TAG = FullscreenActivity.class.getSimpleName();

    public static final String EXTRA_VIDEO_URL_KEY = "video_url";
    public static final String EXTRA_CURRENT_POSITION_KEY = "current_position";
    public static final String EXTRA_CURRENT_WINDOW_INDEX_KEY = "current_window_index";
    public static final String EXTRA_PLAY_WHEN_READY_KEY = "play_when_ready";

    @BindView(R.id.player_view_fullscreen)
    PlayerView mPlayerView;

    private String mVideoUrl;
    private SimpleExoPlayer mPlayer;
    private boolean mPlayWhenReady;
    private long mCurrentPosition;
    private int mCurrentWindowIndex;
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ButterKnife.bind(this);

        //Load the video to be played
        getIntentData();

        //Enable "Lean back" mode
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        initialiseMediaSession();
        initialisePlayer();
    }

    /**
     * Load and verify the video and playback state from the launch intent
     */
    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null &&
                bundle.containsKey(EXTRA_VIDEO_URL_KEY) &&
                bundle.containsKey(EXTRA_CURRENT_POSITION_KEY) &&
                bundle.containsKey(EXTRA_CURRENT_WINDOW_INDEX_KEY) &&
                bundle.containsKey(EXTRA_PLAY_WHEN_READY_KEY)) {
            mVideoUrl = bundle.getString(EXTRA_VIDEO_URL_KEY);
            mCurrentPosition = bundle.getLong(EXTRA_CURRENT_POSITION_KEY);
            mCurrentWindowIndex = bundle.getInt(EXTRA_CURRENT_WINDOW_INDEX_KEY);
            mPlayWhenReady = bundle.getBoolean(EXTRA_PLAY_WHEN_READY_KEY);
        } else {
            invalidVideo();
        }
    }

    /**
     * Close activity and display a toast in case the video is invalid
     */
    private void invalidVideo() {
        Toast.makeText(this, "Invalid video!", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Helper method to start the MediaSession
     */
    private void initialiseMediaSession() {
        mMediaSession = new MediaSessionCompat(this, LOG_TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setMediaButtonReceiver(null);
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_STOP);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MediaSessionCallback());
        mMediaSession.setActive(true);
    }

    /**
     * Helper method to start the ExoPlayer instance
     */
    private void initialisePlayer() {
        String userAgent = Util.getUserAgent(this, "BakingApp");

        mPlayer = ExoPlayerFactory.newSimpleInstance(this);
        mPlayerView.setPlayer(mPlayer);
        mPlayer.addListener(new ExoEventCallback());

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_SPEECH)
                .build();
        mPlayer.setAudioAttributes(audioAttributes, true);

        DefaultHttpDataSourceFactory httpSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        ExtractorMediaSource extractorMediaSource = new ExtractorMediaSource.Factory(httpSourceFactory)
                .createMediaSource(Uri.parse(mVideoUrl));

        if (mCurrentPosition != C.TIME_UNSET) {
            mPlayer.seekTo(mCurrentWindowIndex, mCurrentPosition);
            mPlayer.prepare(extractorMediaSource, false, false);
        } else {
            mPlayer.prepare(extractorMediaSource);
        }

        mPlayer.setPlayWhenReady(mPlayWhenReady);

        ImageView fullscreenIcon = mPlayerView.findViewById(R.id.exo_fullscreen);
        fullscreenIcon.setImageDrawable(getDrawable(R.drawable.baseline_fullscreen_exit_white_36));
        fullscreenIcon.setOnClickListener((v) -> exitFullscreen());
    }

    /**
     * Helper method to release the ExoPlayer instance
     */
    private void releasePlayer() {
        if (mPlayer != null) {
            mCurrentPosition = mPlayer.getCurrentPosition();
            mCurrentWindowIndex = mPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mPlayWhenReady = true;
            mPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mPlayWhenReady = false;
            mPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onStop() {
            mPlayWhenReady = false;
            mPlayer.setPlayWhenReady(false);
            mPlayer.seekTo(0);
        }
    }

    private class ExoEventCallback implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            //Check Player for null, as it seems to cause a crash when calling getCurrentPosition()
            //in certain situations when the EventListener is called after a device rotation.
            if (mPlayer == null) {
                return;
            }
            if (playbackState == Player.STATE_READY && playWhenReady) {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        mPlayer.getCurrentPosition(), 1f);
            } else if (playbackState == Player.STATE_READY) {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        mPlayer.getCurrentPosition(), 1f);
            } else if (playbackState == Player.STATE_ENDED) {
                mPlayWhenReady = false;
                mPlayer.setPlayWhenReady(false);
                mPlayer.seekTo(0);
                mStateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                        0, 1f);
                exitFullscreen();
            }
            mMediaSession.setPlaybackState(mStateBuilder.build());
        }
    }

    /**
     * Helper method to end the activity and set the result with
     * the current playback state
     */
    private void exitFullscreen() {
        Intent intent = new Intent();
        mCurrentPosition = mPlayer.getCurrentPosition();
        mCurrentWindowIndex = mPlayer.getCurrentWindowIndex();
        mPlayWhenReady = mPlayer.getPlayWhenReady();
        intent.putExtra(EXTRA_CURRENT_POSITION_KEY, mCurrentPosition);
        intent.putExtra(EXTRA_CURRENT_WINDOW_INDEX_KEY, mCurrentWindowIndex);
        intent.putExtra(EXTRA_PLAY_WHEN_READY_KEY, mPlayWhenReady);
        setResult(RESULT_OK, intent);
        mPlayer.setPlayWhenReady(false);
        finish();
    }

    @Override
    public void onBackPressed() {
        exitFullscreen();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaSession.setActive(false);
    }
}

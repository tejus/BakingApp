package com.tejus.bakingapp.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
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

        initialiseMediaSession();
        initialisePlayer();
    }

    private void invalidVideo() {
        Toast.makeText(this, "Invalid video!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initialiseMediaSession() {
        mMediaSession = new MediaSessionCompat(this, LOG_TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setMediaButtonReceiver(null);
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MediaSessionCallback());
        mMediaSession.setActive(true);
    }

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
    }

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
        public void onSkipToPrevious() {
            mPlayWhenReady = false;
            mPlayer.setPlayWhenReady(false);
            mPlayer.seekTo(0);
        }
    }

    private class ExoEventCallback implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_READY && playWhenReady) {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        mPlayer.getCurrentPosition(), 1f);
            } else if (playbackState == Player.STATE_READY) {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        mPlayer.getCurrentPosition(), 1f);
            } else if (playbackState == Player.STATE_ENDED) {
                exitFullscreen();
            }
            mMediaSession.setPlaybackState(mStateBuilder.build());
        }
    }

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

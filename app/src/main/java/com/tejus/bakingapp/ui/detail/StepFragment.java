package com.tejus.bakingapp.ui.detail;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Step;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StepFragment extends Fragment {

    private static final String LOG_TAG = StepFragment.class.getSimpleName();

    private static final String CURRENT_POSITION_KEY = "current_position";
    private static final String CURRENT_WINDOW_KEY = "current_window";
    private static final String PLAY_WHEN_READY_KEY = "play_when_ready";
    private static final String EXTRA_STEP_KEY = "step";
    private static final String EXTRA_STEP_COUNT_KEY = "step_count";

    @BindView(R.id.tv_step_heading)
    TextView mTvStepHeading;
    @BindView(R.id.frame_preview_video)
    FrameLayout mPreviewFrame;
    @BindView(R.id.iv_video_overlay)
    ImageView mIvVideoOverlay;
    @BindView(R.id.iv_preview)
    ImageView mIvPreview;
    @BindView(R.id.player_view)
    PlayerView mPlayerView;
    @BindView(R.id.tv_step_desc)
    TextView mTvStepDesc;
    private Unbinder mUnbinder;

    private Context mContext;
    private Step mStep;
    private boolean mHasVideo;
    private boolean mIsPlayerInitialised;
    private SimpleExoPlayer mPlayer;
    private long mCurrentPosition;
    private int mCurrentWindowIndex;
    private boolean mPlayWhenReady;
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    public StepFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public static Fragment newInstance(Step step, int stepCount) {
        Fragment fragment = new StepFragment();
        if (step != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_STEP_KEY, step);
            bundle.putInt(EXTRA_STEP_COUNT_KEY, stepCount);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_step, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        int stepCount;
        if (bundle != null && bundle.containsKey(EXTRA_STEP_KEY)) {
            mStep = bundle.getParcelable(EXTRA_STEP_KEY);
            stepCount = bundle.getInt(EXTRA_STEP_COUNT_KEY);
        } else {
            throw new ClassCastException(rootView.getContext().toString()
                    + " must pass a Step object to fragment!");
        }

        if (mStep != null) {
            mTvStepHeading.setText(mStep.getShortDescription());
            if (stepCount == 0) {
                mTvStepDesc.setVisibility(View.GONE);
            } else {
                mTvStepDesc.setText(mStep.getDescription());
            }
            if (!TextUtils.isEmpty(mStep.getThumbnailURL())) {
                loadThumbnail();
            }
            if (!TextUtils.isEmpty(mStep.getVideoURL())) {
                mHasVideo = true;
                initialiseMediaSession();
            } else {
                mHasVideo = false;
            }
        }

        if (savedInstanceState != null && mHasVideo) {
            mCurrentPosition = savedInstanceState.getLong(CURRENT_POSITION_KEY);
            mCurrentWindowIndex = savedInstanceState.getInt(CURRENT_WINDOW_KEY);
            mPlayWhenReady = savedInstanceState.getBoolean(PLAY_WHEN_READY_KEY);
            mIsPlayerInitialised = true;
        } else {
            mCurrentPosition = C.TIME_UNSET;
            mCurrentWindowIndex = C.INDEX_UNSET;
            mPlayWhenReady = false;
            mIsPlayerInitialised = false;
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24)
            if (mIsPlayerInitialised) {
                initialisePlayer();
            } else if (mHasVideo) {
                initialiseVideoOverlay();
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24)
            if (mIsPlayerInitialised) {
                initialisePlayer();
            } else if (mHasVideo) {
                initialiseVideoOverlay();
            }
    }

    private void loadThumbnail() {
        mPreviewFrame.setVisibility(View.VISIBLE);
        mIvPreview.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(Uri.parse(mStep.getThumbnailURL()))
                .placeholder(R.color.colorPrimaryDark)
                .error(R.color.colorGrey)
                .into(mIvPreview);
    }

    private void initialiseMediaSession() {
        mMediaSession = new MediaSessionCompat(mContext, LOG_TAG);
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

    private void initialiseVideoOverlay() {
        mPreviewFrame.setVisibility(View.VISIBLE);
        mIvVideoOverlay.setVisibility(View.VISIBLE);
        mIvVideoOverlay.setOnClickListener((v) -> {
            mPlayWhenReady = true;
            initialisePlayer();
        });
    }

    private void initialisePlayer() {
        mPreviewFrame.setVisibility(View.VISIBLE);
        mPlayerView.setVisibility(View.VISIBLE);
        mIvPreview.setVisibility(View.GONE);
        mIvVideoOverlay.setVisibility(View.GONE);
        String userAgent = Util.getUserAgent(mContext, "BakingApp");

        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext);
        mPlayerView.setPlayer(mPlayer);
        mPlayer.addListener(new ExoEventCallback());

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_SPEECH)
                .build();
        mPlayer.setAudioAttributes(audioAttributes, true);

        DefaultHttpDataSourceFactory httpSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        ExtractorMediaSource extractorMediaSource = new ExtractorMediaSource.Factory(httpSourceFactory)
                .createMediaSource(Uri.parse(mStep.getVideoURL()));

        if (mCurrentPosition != C.TIME_UNSET) {
            mPlayer.seekTo(mCurrentWindowIndex, mCurrentPosition);
            mPlayer.prepare(extractorMediaSource, false, false);
        } else {
            mPlayer.prepare(extractorMediaSource);
        }

        mIsPlayerInitialised = true;
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
            }
            mMediaSession.setPlaybackState(mStateBuilder.build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24 && mIsPlayerInitialised) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24 && mIsPlayerInitialised) {
            releasePlayer();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CURRENT_POSITION_KEY, mCurrentPosition);
        outState.putInt(CURRENT_WINDOW_KEY, mCurrentWindowIndex);
        outState.putBoolean(PLAY_WHEN_READY_KEY, mPlayWhenReady);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHasVideo) {
            mMediaSession.setActive(false);
        }
        mUnbinder.unbind();
    }
}

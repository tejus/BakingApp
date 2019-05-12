package com.tejus.bakingapp.ui.detail;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private static final String PLAYER_INITIALISED_KEY = "player_initialised";
    private static final String EXTRA_STEP_KEY = "step";
    private static final String EXTRA_STEP_COUNT_KEY = "step_count";
    private static final int FULLSCREEN_REQUEST_CODE = 6281;

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
    private int mStepCount;
    private boolean mHasVideo;
    //Keep track of whether player was initialised in order to restore state
    private boolean mWasPlayerInitialised;
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

    /**
     * Helper static method to obtain an instance of StepFragment
     */
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
        if (bundle != null && bundle.containsKey(EXTRA_STEP_KEY)) {
            mStep = bundle.getParcelable(EXTRA_STEP_KEY);
            mStepCount = bundle.getInt(EXTRA_STEP_COUNT_KEY);
        } else {
            throw new ClassCastException(rootView.getContext().toString()
                    + " must pass a Step object to fragment!");
        }

        validateStep();

        //Restore video playback state from previous instance, if both exist
        restoreState(savedInstanceState);

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Run through the data in mStep, initialise views as required
     */
    private void validateStep() {
        if (mStep != null) {
            if (!TextUtils.isEmpty(mStep.getShortDescription())) {
                mTvStepHeading.setText(mStep.getShortDescription());
            }
            //Do not show the step description for the Introduction step
            if (mStepCount == 0) {
                mTvStepDesc.setVisibility(View.GONE);
            } else if (!TextUtils.isEmpty(mStep.getDescription())) {
                mTvStepDesc.setText(mStep.getDescription());
            }

            //Setup video if there is a videoURL
            if (!TextUtils.isEmpty(mStep.getVideoURL())) {
                setupVideo();
            } else {
                mHasVideo = false;
            }

            //Setup thumbnail if there is a thumbnailURL
            if (!TextUtils.isEmpty(mStep.getThumbnailURL())) {
                setupThumbnail();
            }
        }
    }

    /**
     * Helper method to show the PlayerView and start the MediaSession
     */

    private void setupVideo() {
        mPreviewFrame.setVisibility(View.VISIBLE);
        mHasVideo = true;
        initialiseMediaSession();
    }

    /**
     * Helper method to show the thumbnail view and validate the URL
     */
    private void setupThumbnail() {
        //In case the video has been set to thumbnailURL instead of videoURL, catch it here
        if (!mHasVideo) {
            String thumbnailUrl = mStep.getThumbnailURL();
            if (TextUtils.equals(thumbnailUrl.substring(thumbnailUrl.length() - 3), "mp4")) {
                mStep.setVideoURL(thumbnailUrl);
                setupVideo();
                return;
            }
        }
        mPreviewFrame.setVisibility(View.VISIBLE);
        loadThumbnail();
    }

    /**
     * Helper method to load the thumbnail image
     */
    private void loadThumbnail() {
        showThumbnail();
        Picasso.get()
                .load(Uri.parse(mStep.getThumbnailURL()))
                .placeholder(R.color.colorPrimaryDark)
                .error(R.color.colorGrey)
                .into(mIvPreview);
    }

    /**
     * Restore previous state in case of configuration change
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mHasVideo) {
            mCurrentPosition = savedInstanceState.getLong(CURRENT_POSITION_KEY);
            mCurrentWindowIndex = savedInstanceState.getInt(CURRENT_WINDOW_KEY);
            mPlayWhenReady = savedInstanceState.getBoolean(PLAY_WHEN_READY_KEY);
            mWasPlayerInitialised = savedInstanceState.getBoolean(PLAYER_INITIALISED_KEY);
        } else {
            mCurrentPosition = C.TIME_UNSET;
            mCurrentWindowIndex = C.INDEX_UNSET;
            mPlayWhenReady = false;
            mWasPlayerInitialised = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24)
            if (mWasPlayerInitialised) {
                initialisePlayer();
            } else if (mHasVideo) {
                showVideoOverlay();
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24)
            if (mWasPlayerInitialised) {
                initialisePlayer();
            } else if (mHasVideo) {
                showVideoOverlay();
            }
    }

    /**
     * Helper method to start the MediaSession
     */
    private void initialiseMediaSession() {
        mMediaSession = new MediaSessionCompat(mContext, LOG_TAG);
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
     * Helper method to show a clickable overlay on the PlayerView
     */
    private void showVideoOverlay() {
        mIvVideoOverlay.setVisibility(View.VISIBLE);
        mIvVideoOverlay.setOnClickListener((v) -> {
            mPlayWhenReady = true;
            initialisePlayer();
        });
    }

    /**
     * Helper method to show the thumbnail and hide the PlayerView
     */
    private void showThumbnail() {
        mPlayerView.setVisibility(View.GONE);
        mIvPreview.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to show the PlayerView and hide the overlay and thumbnail
     */
    private void showVideo() {
        mPlayerView.setVisibility(View.VISIBLE);
        mIvPreview.setVisibility(View.GONE);
        mIvVideoOverlay.setVisibility(View.GONE);
    }

    /**
     * Helper method to start the ExoPlayer instance
     */
    private void initialisePlayer() {
        showVideo();
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

        mWasPlayerInitialised = true;
        mPlayer.setPlayWhenReady(mPlayWhenReady);

        ImageView fullscreenToggle = mPlayerView.findViewById(R.id.exo_fullscreen);
        fullscreenToggle.setImageDrawable(mContext.getDrawable(R.drawable.baseline_fullscreen_white_36));
        fullscreenToggle.setOnClickListener((v) -> enterFullscreen());
    }

    /**
     * Helper method to launch FullscreenActivity with the playback state and data
     */
    private void enterFullscreen() {
        Intent intent = new Intent(mContext, FullscreenActivity.class);
        Bundle bundle = new Bundle();
        mCurrentPosition = mPlayer.getCurrentPosition();
        mCurrentWindowIndex = mPlayer.getCurrentWindowIndex();
        mPlayWhenReady = mPlayer.getPlayWhenReady();
        bundle.putString(FullscreenActivity.EXTRA_VIDEO_URL_KEY, mStep.getVideoURL());
        bundle.putLong(FullscreenActivity.EXTRA_CURRENT_POSITION_KEY, mCurrentPosition);
        bundle.putInt(FullscreenActivity.EXTRA_CURRENT_WINDOW_INDEX_KEY, mCurrentWindowIndex);
        bundle.putBoolean(FullscreenActivity.EXTRA_PLAY_WHEN_READY_KEY, mPlayWhenReady);
        intent.putExtras(bundle);
        mPlayWhenReady = false;
        mPlayer.setPlayWhenReady(false);
        startActivityForResult(intent, FULLSCREEN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FULLSCREEN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mCurrentPosition = data.getLongExtra(FullscreenActivity.EXTRA_CURRENT_POSITION_KEY, C.TIME_UNSET);
            mCurrentWindowIndex = data.getIntExtra(FullscreenActivity.EXTRA_CURRENT_WINDOW_INDEX_KEY, C.INDEX_UNSET);
            mPlayWhenReady = data.getBooleanExtra(FullscreenActivity.EXTRA_PLAY_WHEN_READY_KEY, false);
            if (mCurrentPosition != C.TIME_UNSET) {
                initialisePlayer();
            }
        }
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
            showThumbnail();
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
            }
            mMediaSession.setPlaybackState(mStateBuilder.build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24 && mWasPlayerInitialised) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24 && mWasPlayerInitialised) {
            releasePlayer();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CURRENT_POSITION_KEY, mCurrentPosition);
        outState.putInt(CURRENT_WINDOW_KEY, mCurrentWindowIndex);
        outState.putBoolean(PLAY_WHEN_READY_KEY, mPlayWhenReady);
        outState.putBoolean(PLAYER_INITIALISED_KEY, mWasPlayerInitialised);
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

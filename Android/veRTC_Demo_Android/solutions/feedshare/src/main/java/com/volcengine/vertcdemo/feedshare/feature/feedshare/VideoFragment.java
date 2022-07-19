package com.volcengine.vertcdemo.feedshare.feature.feedshare;

import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRELOAD;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.Source;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.utils.WeakHandler;
import com.volcengine.vertcdemo.feedshare.core.FeedShareDataManger;
import com.volcengine.vertcdemo.feedshare.core.FeedShareRTMClient;
import com.volcengine.vertcdemo.feedshare.core.FeedShareRtcManager;
import com.volcengine.vertcdemo.feedshare.view.pager.CustomRecyclerView;
import com.volcengine.vertcdemo.feedshare.view.pager.PagerLayoutManager;
import com.volcengine.vertcdemo.feedshare.view.pager.RecyclerViewPagerListener;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;
import com.volcengine.vertcdemo.feedshare.bean.VideoResponse;
import com.volcengine.vertcdemo.feedshare.event.ContentUpdateInform;
import com.volcengine.vertcdemo.feedshare.bean.MessageContent;
import com.volcengine.vertcdemo.feedshare.bean.VideoStatusInfo;
import com.volcengine.vertcdemo.feedshare.view.DisplayMode;
import com.volcengine.vertcdemo.feedshare.view.VOLCVideoController;
import com.volcengine.vertcdemo.feedshare.view.VOLCVideoView;
import com.volcengine.vertcdemo.feedshare.view.VideoAudioProcessor;
import com.volcengine.vertcdemo.feedshare.view.VideoPlayListener;
import com.volcengine.vertcdemo.feedshare.view.VideoPlayListenerAdapter;
import com.volcengine.vertcdemo.feedshare.view.layers.CoverLayer;
import com.volcengine.vertcdemo.feedshare.view.layers.LoadFailLayer;
import com.volcengine.vertcdemo.feedshare.view.layers.LoadingLayer;
import com.volcengine.vertcdemo.feedshare.view.layers.SmallToolbarLayer;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment implements RecyclerViewPagerListener, WeakHandler.IHandler {
    private static final String TAG = "VideoFragment";

    private List<VideoItem> mInitVideos;
    private BaseAdapter<VideoItem> mAdapter;
    private int mLastPosition = -1;
    private VideoItem mCurVideoItem = null;
    private VOLCVideoView mCurrentVideoView;

    private boolean mSelectFirst;
    private CustomRecyclerView mRecyclerView;
    private PagerLayoutManager mLayoutManager;
    private long lastProgressSyncTs = -1;

    private VideoPlayListener mPlayListenerForSync = new VideoPlayListenerAdapter() {
        @Override
        public void onCallPlay() {
            boolean isHost = FeedShareDataManger.getInstance().isHost();
            Log.e(TAG, "mPlayListenerForSync onCallPlay isHost:" + isHost
                    + ",mCurrentVideoView:" + mCurrentVideoView
                    + ",mCurVideoItem:" + mCurVideoItem.videoUrl);
            sendVideoStatus(false);
        }

        @Override
        public void onVideoPlay() {
            Log.e(TAG, "mPlayListenerForSync onVideoPlay"
                    + ",mCurrentVideoView:" + mCurrentVideoView
                    + ",mCurVideoItem:" + mCurVideoItem.videoUrl);
            sendVideoStatus(false);
        }

        @Override
        public void onVideoPause() {
            Log.e(TAG, "mPlayListenerForSync onVideoPause"
                    + ",mCurrentVideoView:" + mCurrentVideoView
                    + ",mCurVideoItem:" + mCurVideoItem);
            sendVideoStatus(false);
        }


        @Override
        public void onProgressUpdate(int progress, int duration) {
            boolean isHost = FeedShareDataManger.getInstance().isHost();
            Log.e(TAG, "mPlayListenerForSync onProgressUpdate isHost:" + isHost
                    + ",playStatus:" + mCurrentVideoView.getPlayStatus());
            sendVideoStatus(true);
        }
    };

    /**
     * 向房间内嘉宾同步播放状态和进度信息
     *
     * @param syncProgress 是否为了同步进度，如果不是需要消息立即发
     */
    private void sendVideoStatus(boolean syncProgress) {
        boolean isHost = FeedShareDataManger.getInstance().isHost();
        if (!isHost || mCurrentVideoView == null || mCurVideoItem == null) {
            return;
        }
        long curTs = SystemClock.elapsedRealtime();
        if (!syncProgress || (curTs - lastProgressSyncTs > 1000 && mCurVideoItem != null)) {
            sendVideoStatus(mCurVideoItem.videoId,
                    mCurrentVideoView.getProgress(),
                    mCurrentVideoView.getPlayStatus());
            lastProgressSyncTs = SystemClock.elapsedRealtime();
        }
    }

    private void sendVideoStatus(String videoId, int progress, int status) {
        VideoStatusInfo info = new VideoStatusInfo();
        info.videoId = videoId;
        info.progress = progress;
        info.status = status;
        String msg = SyncMessageUtil.createVideoStatusMessage(info);
        FeedShareRtcManager.getInstance().sendRoomMessage(msg);
    }

    private final WeakHandler mSyncWeakHandler = new WeakHandler(Looper.getMainLooper(), this);
    private static final int MSG_VIDEO_STATUS = 1;
    private ISyncHandler mVideoSyncHandler = new SyncHandlerAdapter() {

        @Override
        public void handleVideoStatus(String peerUid, MessageContent content) {
            //仅嘉宾需要处理视频当前播放状态的消息
            if (FeedShareDataManger.getInstance().isHost()) {
                return;
            }
            Message message = mSyncWeakHandler.obtainMessage();
            message.what = MSG_VIDEO_STATUS;
            message.obj = content;
            mSyncWeakHandler.sendMessage(message);
        }

    };

    public void setInitVideos(List<VideoItem> initVideos) {
        this.mInitVideos = initVideos;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FeedShareRtcManager.getInstance().addSyncHandler(mVideoSyncHandler);
        mAdapter = new BaseAdapter<VideoItem>(new ArrayList<>()) {
            @Override
            public int getLayoutId(final int viewType) {
                return R.layout.item_feed_share_video;
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final VideoItem data, final int position) {
                Log.i(TAG, "onCreate videoItem:" + data.videoUrl);
                VOLCVideoView videoView = holder.getView(R.id.video_view);
                VOLCVideoController controller = new VOLCVideoController(videoView.getContext(), data, videoView);
                controller.addPlayListener(mPlayListenerForSync);
                VideoAudioProcessor processor = new VideoAudioProcessor(FeedShareRtcManager.getInstance().getEngine());
                controller.setAudioProcessor(processor);
                videoView.setVideoController(controller);
                videoView.setDisplayMode(DisplayMode.DISPLAY_MODE_ASPECT_FILL);
                videoView.addLayer(new CoverLayer());
                SmallToolbarLayer toolbarLayer = new SmallToolbarLayer();
                toolbarLayer.setCanPlay(FeedShareDataManger.getInstance().isHost());
                videoView.addLayer(toolbarLayer);
                videoView.addLayer(new LoadFailLayer());
                videoView.addLayer(new LoadingLayer());
                videoView.refreshLayers();

                if (!mSelectFirst) {
                    mSelectFirst = true;
                    onPageSelected(position, holder.itemView);
                }
            }

            @Override
            public int getPosition(String videoId) {
                int position = -1;
                if (videoId == null) {
                    return position;
                }
                List<VideoItem> data = getAll();
                int size = data == null ? 0 : data.size();
                if (size == 0) {
                    return position;
                }
                for (int i = 0; i < size; i++) {
                    if (TextUtils.equals(videoId, data.get(i).videoId)) {
                        return i;
                    }
                }
                return -1;
            }

            @Override
            public void addAll(List<VideoItem> datas) {
                super.addAll(datas);
                //更新视频预加载列表
                addPreloadVideoSource(datas);
            }
        };
        SolutionDemoEventManager.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_share_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLayoutManager = new PagerLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setOnViewPagerListener(this);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        boolean isHost = FeedShareDataManger.getInstance().isHost();
        List<VideoItem> videos = mInitVideos;
        int size = videos == null ? 0 : videos.size();
        Log.e(TAG, "onViewCreated isHost:" + isHost + ",videos:" + size);
        if (size > 0) {
            mAdapter.addAll(videos);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentVideoView != null) {
            mCurrentVideoView.onResume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCurrentVideoView != null) {
            mCurrentVideoView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
        mPlayListenerForSync = null;
        FeedShareRtcManager.getInstance().removeSyncHandler(mVideoSyncHandler);
        mVideoSyncHandler = null;
        SolutionDemoEventManager.unregister(this);
    }

    @Override
    public void onInitComplete(int position, View view) {
        TTVideoEngineLog.d(TAG, "onInitComplete position " + position + ", view " + view);
        if (view == null) {
            return;
        }
        VOLCVideoView videoView = view.findViewById(R.id.video_view);
        if (videoView != null && videoView.getPlayStatus() != VideoStatusInfo.STATUS_PLAYING) {
            Log.e(TAG, "onInitComplete invoke play!");
            videoView.play();
        }
    }

    @Override
    public void onPageRelease(final int position, final View view) {
        TTVideoEngineLog.d(TAG, "onPageRelease position " + position + ", view " + view);
        if (view == null) {
            return;
        }
        VOLCVideoView videoView = view.findViewById(R.id.video_view);
        videoView.release();
    }

    @Override
    public void onPageSelected(final int position, final View view) {
        Log.d(TAG, "onPageSelected position " + position + ",mLastPosition:" + mLastPosition);
        if (position == mLastPosition) {
            TTVideoEngineLog.d(TAG, "onPageSelected position is last position");
            return;
        }
        mLastPosition = position;
        syncCurrentPlayVideo(position);
        View ItemView = view;
        final View tempView = mLayoutManager.findViewByPosition(position);
        if (tempView != null) {
            ItemView = tempView;
        }

        if (ItemView == null) {
            TTVideoEngineLog.d(TAG, "onPageSelected view is null");
            return;
        }

        VOLCVideoView videoView = ItemView.findViewById(R.id.video_view);
        if (mCurrentVideoView != null) {
            mCurrentVideoView.mute();
            mCurrentVideoView.release();
        }
        mCurrentVideoView = videoView;
        if (mCurrentVideoView != null && videoView.getPlayStatus() != VideoStatusInfo.STATUS_PLAYING) {
            Log.e(TAG, "onPageSelected invoke play!");
            videoView.play();
        }
        boolean isHost = FeedShareDataManger.getInstance().isHost();
        mRecyclerView.setCanScrollVertically(isHost);
        if (isHost) {
            preLoadVideos(position);
        }
    }

    private void preLoadVideos(int position) {
        boolean isHost = FeedShareDataManger.getInstance().isHost();
        //距离底部5个内开始预加载数据
        if (isHost && mAdapter != null && (mAdapter.getItemCount() - position) < 5) {
            requestVideoList();
        }
    }

    private void requestVideoList() {
        FeedShareRTMClient rtmClient = FeedShareRtcManager.getInstance().getRTMClient();
        if (rtmClient == null){
            return;
        }
        rtmClient.getContentList(new IRequestCallback<VideoResponse>() {

            @Override
            public void onSuccess(VideoResponse data) {
                if (data == null || data.videoItemList == null || !isVisible()) return;
                int size = data.videoItemList.size();
                if (size > 0) {
                    mAdapter.addAll(data.videoItemList);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                Log.i(TAG, "requestVideos onError errorCode:" + errorCode + ",message:" + message);
            }
        });
    }

    private void syncCurrentPlayVideo(int position) {
        List<VideoItem> videos = mAdapter.getAll();
        if (videos != null && videos.size() > 0) {
            mCurVideoItem = videos.get(position);
            if (FeedShareDataManger.getInstance().isHost()) {
                Log.i(TAG, "syncCurrentPlayVideo");
                sendVideoStatus(mCurVideoItem.videoId, 1, VideoStatusInfo.STATUS_PLAYING);
            }
        }
    }

    private void cleanUp() {
        if (mCurrentVideoView == null) {
            return;
        }
        mCurrentVideoView.release();
        mCurrentVideoView = null;
        TTVideoEngine.clearAllStrategy();
    }

    @Override
    public void handleMsg(Message msg) {
        if (mAdapter == null || !isVisible()) {
            return;
        }
        if (msg == null || !(msg.obj instanceof MessageContent)) {
            return;
        }
        MessageContent content = (MessageContent) msg.obj;
        if (msg.what == MSG_VIDEO_STATUS) {
            mSyncWeakHandler.removeMessages(MSG_VIDEO_STATUS);
            if (content.videoStatus == null) return;
            Log.e(TAG, "handleVideoStatus videoStatus:" + VideoStatusInfo.toJson(content.videoStatus));
            scrollToRightPosition(content);
            if (mCurrentVideoView == null) return;
            if (content.videoStatus.status == VideoStatusInfo.STATUS_PLAYING
                    && mCurrentVideoView.getPlayStatus() != VideoStatusInfo.STATUS_PLAYING) {
                mCurrentVideoView.play();
            } else if (content.videoStatus.status == VideoStatusInfo.STATUS_PAUSED
                    && mCurrentVideoView.getPlayStatus() != VideoStatusInfo.STATUS_PAUSED) {
                mCurrentVideoView.pause();
            }
            int curProgress = mCurrentVideoView.getProgress(); // s
            // 比主播快, 考虑到主播重播时嘉宾还在结尾
            boolean isSeek = (content.videoStatus.progress - curProgress > 2)
                    || (content.videoStatus.progress > 4 && curProgress - content.videoStatus.progress > 2);
            if (isSeek) {
                mCurrentVideoView.seek(content.videoStatus.progress * 1000);
            }
        }
    }

    private void scrollToRightPosition(MessageContent content) {
        if (mCurVideoItem == null || TextUtils.equals(mCurVideoItem.videoId, content.videoStatus.videoId)) {
            Log.e(TAG, "scrollToRightPosition no need!");
            return;
        }
        int targetPosition = mAdapter.getPosition(content.videoStatus.videoId);
        int curPosition = mCurVideoItem == null ? -1 : mAdapter.getPosition(mCurVideoItem.videoId);
        if (mRecyclerView != null && targetPosition >= 0 && targetPosition != curPosition) {
            mRecyclerView.scrollToPosition(targetPosition);
            int position = mLayoutManager.findFirstCompletelyVisibleItemPosition();
            View itemView = mLayoutManager.findViewByPosition(position);
            Log.e(TAG, "scrollToRightPosition targetPosition:" + targetPosition + ",curPosition:" + curPosition + ",scrolledPosition:" + position);
            onPageSelected(position, itemView);
        }
    }

    /**
     * 增加视频数据，仅嘉宾需要处理
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateVideos(ContentUpdateInform contentUpdateEvent) {
        if (!isVisible() || contentUpdateEvent == null || FeedShareDataManger.getInstance().isHost()) {
            return;
        }
        Log.e(TAG, "VideoFragment onUpdateVideos contentUpdateEvent:" + contentUpdateEvent.contentList.size());
        mAdapter.addAll(contentUpdateEvent.contentList);
    }

    /**
     * 向播放器添加预加载视频数据任务
     *
     * @param contentList
     * @return
     */
    private void addPreloadVideoSource(List<VideoItem> contentList) {
        if (contentList == null || contentList.size() == 0) return;
        List<StrategySource> sources = null;
        for (VideoItem item : contentList) {
            if (sources == null) {
                sources = new ArrayList<>(contentList.size());
            }
            if (TextUtils.isEmpty(item.videoUrl)) continue;
            StrategySource source = new DirectUrlSource.Builder()
                    .setVid(item.videoId)
                    .addItem(new DirectUrlSource.UrlItem.Builder()
                            .setUrl(item.videoUrl)
                            .setEncodeType(Source.EncodeType.H264)
                            .setCacheKey(TTVideoEngine.computeMD5(item.videoUrl))
                            .build())
                    .build();
            item.playStrategySource = source;
            sources.add(source);
        }
        TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRELOAD, STRATEGY_SCENE_SMALL_VIDEO);
        TTVideoEngine.addStrategySources(sources);
    }
}

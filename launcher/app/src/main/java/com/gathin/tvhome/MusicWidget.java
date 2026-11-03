package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/** Optional, event-driven controls for apps that expose Android media sessions.
 * No player, network lookups, notification reads, idle policy or background polling.
 */
final class MusicWidget extends LinearLayout {
    private final MediaSessionManager manager;
    private final ComponentName access;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Consumer<Intent> launch;
    private final TextView title, subtitle, toggle, next;
    private final List<MediaController> sessions = new ArrayList<>();
    private MediaController selected;
    private boolean active, listening, authorized;
    private final Runnable accessChanged = this::restart;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionListener = this::sessionsChanged;
    private final MediaController.Callback callback = new MediaController.Callback() {
        @Override public void onMetadataChanged(MediaMetadata metadata) { render(); }
        @Override public void onPlaybackStateChanged(PlaybackState state) { render(); }
        @Override public void onSessionDestroyed() { reload(); }
    };

    MusicWidget(Context context, Consumer<Intent> launch) {
        super(context);
        this.launch = launch;
        manager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        access = new ComponentName(context, MusicAccessService.class);
        setOrientation(HORIZONTAL); setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(16), dp(10), dp(12), dp(10));
        GradientDrawable background = shape(0xba111d2c); setBackground(background);
        LinearLayout words = new LinearLayout(context); words.setOrientation(VERTICAL);
        title = text("Music", 14); title.setTypeface(null, Typeface.BOLD);
        subtitle = text("Start playback in a supported app", 11); subtitle.setTextColor(0xffbdc9d7);
        words.addView(title); words.addView(subtitle);
        addView(words, new LayoutParams(0, -2, 1));
        toggle = control("Set up"); next = control("Next");
        addView(toggle, new LayoutParams(dp(86), dp(38)));
        LayoutParams np = new LayoutParams(dp(72), dp(38)); np.leftMargin = dp(8); addView(next, np);
        toggle.setOnClickListener(v -> {
            if (!authorized) { requestAccess(); return; }
            if (selected == null) { toast("Start music in a supported app first"); return; }
            try {
                PlaybackState state = selected.getPlaybackState();
                if (state == null) return;
                long action = playing(state) ? PlaybackState.ACTION_PAUSE : PlaybackState.ACTION_PLAY;
                if ((state.getActions() & (action | PlaybackState.ACTION_PLAY_PAUSE)) == 0) {
                    toast("This app does not expose playback controls"); return;
                }
                if (playing(state)) selected.getTransportControls().pause();
                else selected.getTransportControls().play();
            } catch (RuntimeException e) { reload(); }
        });
        next.setOnClickListener(v -> {
            try { if (selected != null) selected.getTransportControls().skipToNext(); }
            catch (RuntimeException e) { reload(); }
        });
        render();
    }

    /** The two stable buttons may be included in the Home D-pad navigation row. */
    List<View> controls() { return Arrays.asList(toggle, next); }
    void active(boolean value) {
        if (active == value) return;
        active = value;
        if (value && isAttachedToWindow()) start(); else stop();
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); if (active) start(); }
    @Override protected void onDetachedFromWindow() { stop(); super.onDetachedFromWindow(); }
    private void restart() { stop(); if (active && isAttachedToWindow()) start(); }
    private void start() {
        if (listening) return;
        MusicAccessService.observers.add(accessChanged);
        try {
            if (manager == null) throw new IllegalStateException("Media sessions unavailable");
            manager.addOnActiveSessionsChangedListener(sessionListener, access, main);
            listening = true; authorized = true; reload();
        } catch (RuntimeException e) { authorized = false; render(); }
    }
    private void stop() {
        MusicAccessService.observers.remove(accessChanged);
        if (listening && manager != null) {
            try { manager.removeOnActiveSessionsChangedListener(sessionListener); } catch (RuntimeException ignored) {}
        }
        listening = false;
        clearSessions(); selected = null;
    }
    private void clearSessions() {
        for (MediaController controller : sessions) {
            try { controller.unregisterCallback(callback); } catch (RuntimeException ignored) {}
        }
        sessions.clear();
    }
    private void reload() {
        if (!active || !isAttachedToWindow() || manager == null) return;
        try { sessionsChanged(manager.getActiveSessions(access)); authorized = true; render(); }
        catch (RuntimeException e) { authorized = false; clearSessions(); selected = null; render(); }
    }
    private void sessionsChanged(List<MediaController> incoming) {
        if (!active || !isAttachedToWindow()) return;
        clearSessions();
        if (incoming != null) for (MediaController controller : incoming) {
            // A pathological app cannot make the Home retain an unbounded session list.
            if (sessions.size() == 12) break;
            try { controller.registerCallback(callback, main); sessions.add(controller); }
            catch (RuntimeException ignored) {}
        }
        render();
    }
    private static boolean playing(PlaybackState state) {
        return state != null && (state.getState() == PlaybackState.STATE_PLAYING
                || state.getState() == PlaybackState.STATE_BUFFERING
                || state.getState() == PlaybackState.STATE_CONNECTING);
    }
    private void render() {
        try { renderState(); }
        catch (RuntimeException e) {
            selected = null;
            title.setText("Music"); subtitle.setText("Playback information is unavailable");
            toggle.setText(authorized ? "Play" : "Set up"); next.setEnabled(false); next.setAlpha(.4f);
        }
    }
    private void renderState() {
        selected = null;
        for (MediaController controller : sessions) {
            if (selected == null) selected = controller;
            if (playing(controller.getPlaybackState())) { selected = controller; break; }
        }
        MediaMetadata metadata = selected == null ? null : selected.getMetadata();
        PlaybackState state = selected == null ? null : selected.getPlaybackState();
        CharSequence song = metadata == null ? null : metadata.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        if (TextUtils.isEmpty(song) && metadata != null) song = metadata.getText(MediaMetadata.METADATA_KEY_TITLE);
        CharSequence artist = metadata == null ? null : metadata.getText(MediaMetadata.METADATA_KEY_ARTIST);
        title.setText(TextUtils.isEmpty(song) ? "Music" : song);
        subtitle.setText(!authorized ? "Enable notification access to show playback"
                : selected == null ? "Start playback in a supported app"
                : TextUtils.isEmpty(artist) ? "Now playing from your app" : artist);
        toggle.setText(!authorized ? "Set up" : playing(state) ? "Pause" : "Play");
        next.setEnabled(authorized && state != null && (state.getActions() & PlaybackState.ACTION_SKIP_TO_NEXT) != 0);
        next.setAlpha(next.isEnabled() ? 1f : .4f);
    }
    private void requestAccess() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        if (intent.resolveActivity(getContext().getPackageManager()) == null) {
            toast("Notification access settings are unavailable on this Fire OS. You can turn the widget off in Studio.");
        } else launch.accept(intent);
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void toast(String value) { Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show(); }
    private TextView text(String value, int size) {
        TextView view = new TextView(getContext()); view.setText(value); view.setTextSize(size);
        view.setTextColor(0xfff4f6fb); view.setSingleLine(); view.setEllipsize(TextUtils.TruncateAt.END);
        view.setIncludeFontPadding(false); return view;
    }
    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(10)); return d;
    }
    private TextView control(String label) {
        TextView button = text(label, 12); button.setGravity(Gravity.CENTER);
        button.setId(View.generateViewId()); button.setFocusable(true); button.setClickable(true);
        StateListDrawable states = new StateListDrawable(); GradientDrawable focus = shape(0xff28435b);
        focus.setStroke(dp(2), 0xffa9ead8); states.addState(new int[]{android.R.attr.state_focused}, focus);
        states.addState(new int[]{}, shape(0x552e4158)); button.setBackground(states); return button;
    }
}

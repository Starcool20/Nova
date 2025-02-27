package com.hackathon.nova.overlay;

import android.animation.*;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.graphics.Insets;
import android.util.DisplayMetrics;
import android.view.animation.LinearInterpolator;
import java.util.List;
import java.util.Arrays;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hackathon.nova.Processes;
import com.hackathon.nova.R;
import com.hackathon.nova.speech.VoiceRecognizer;
import android.widget.ImageButton;
import android.os.Build;

public class OverlayWindow {

  private static WindowManager windowManager;
  private static WindowManager.LayoutParams layoutParams;
  private static View OverlayView;
  private static boolean startSpeechRecognizer = false;
  public static boolean isWindowShowing = false;
  private static ObjectAnimator scaleAnimator;
  private static ObjectAnimator animator;
  private static TextView a;
  private static TextView b;
  private static TextView c;
  private static TextView d;
  private static TextView e;
  private static TextView f;
  private static TextView g;
  private static TextView h;
  private static TextView i;
  private static TextView j;
  private static LinearLayout layout;
  private static boolean stopAnimation = false;
  private static Handler handler;
  private static Runnable animatorRunnable;

  public OverlayWindow(Context context) {
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    int screenWidth;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      // Use WindowMetrics for API 30+
      WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
      Rect bounds = windowMetrics.getBounds();
      screenWidth = bounds.width(); // Get screen width
    } else {
      // Use DisplayMetrics for older devices
      DisplayMetrics displayMetrics = new DisplayMetrics();
      windowManager.getDefaultDisplay().getMetrics(displayMetrics);
      screenWidth = displayMetrics.widthPixels; // Get screen width
    }

    layoutParams =
        new WindowManager.LayoutParams(
            screenWidth - 50,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
    layoutParams.gravity = Gravity.BOTTOM;
    layoutParams.y = 80; // Moves the overlay 50 pixels up from the bottom.

    OverlayView = LayoutInflater.from(context).inflate(R.layout.overlay, null);
    layout = OverlayView.findViewById(R.id.layout);
    a = OverlayView.findViewById(R.id.a);
    b = OverlayView.findViewById(R.id.b);
    c = OverlayView.findViewById(R.id.c);
    d = OverlayView.findViewById(R.id.d);
    e = OverlayView.findViewById(R.id.e);
    f = OverlayView.findViewById(R.id.f);
    g = OverlayView.findViewById(R.id.g);
    h = OverlayView.findViewById(R.id.h);
    i = OverlayView.findViewById(R.id.i);
    j = OverlayView.findViewById(R.id.j);
  }

  private static void registerListeners() {
    scaleAnimator.addListener(
        new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator _param1) {}

          @Override
          public void onAnimationEnd(Animator _param1) {
            if (stopAnimation) {
              stopAnimation = false;
              stopAnim();
              if (windowManager != null) {
                startSpeechRecognizer = false;
                isWindowShowing = false;
                windowManager.removeView(OverlayView);
              }
              a.setText("L");
              b.setText("i");
              c.setText("s");
              d.setText("t");
              e.setText("e");
              f.setText("n");
              g.setText("i");
              h.setText("n");
              i.setText("g");
              j.setText("");
              VoiceRecognizer.startListening();
            }
          }

          @Override
          public void onAnimationCancel(Animator _param1) {}

          @Override
          public void onAnimationRepeat(Animator _param1) {}
        });
  }

  public static void showOverlay() {
    if (isWindowShowing) {
      destroy();
    } else {
      try {
        windowManager.addView(OverlayView, layoutParams);
        isWindowShowing = true;
        VoiceRecognizer.playFeedbackSound();

        showViewWithAnim(0f, 1f);
        startTextViewAnim();
      } catch (Exception e) {
        destroy();
      }
    }
  }

  public static void destroy() {
    stopAnimation = true;
    showViewWithAnim(1f, 0f);
  }

  private static void showViewWithAnim(float start, float end) {
    // Use PropertyValuesHolder to animate both scaleX and scaleY from -50 to 1
    PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", start, end);
    PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", start, end);

    // Create ObjectAnimator with both properties
    scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(layout, scaleX, scaleY);
    scaleAnimator.setDuration(300); // 1 second duration
    scaleAnimator.setInterpolator(new LinearInterpolator());
    scaleAnimator.start();

    registerListeners();
  }

  public static void processingAudio() {
    a.setText("P");
    b.setText("r");
    c.setText("o");
    d.setText("c");
    e.setText("e");
    f.setText("s");
    g.setText("s");
    h.setText("i");
    i.setText("n");
    j.setText("g");
  }

  public static void response() {
    a.setText("R");
    b.setText("e");
    c.setText("s");
    d.setText("p");
    e.setText("o");
    f.setText("n");
    g.setText("d");
    h.setText("i");
    i.setText("n");
    j.setText("g");
  }

  public static void showError() {
    a.setText("E");
    b.setText("r");
    c.setText("r");
    d.setText("o");
    e.setText("r");
    f.setText("");
    g.setText("");
    h.setText("");
    i.setText("");
    j.setText("");

    new Handler()
        .postDelayed(
            new Runnable() {

              @Override
              public void run() {
                destroy();
              }
            },
            3000);
  }

  private static void startTextViewAnim() {
    List<View> views = Arrays.asList(a, b, c, d, e, f, g, h, i, j);
    int delay = 150; // Delay in milliseconds between animations
    int duration = 200; // Animation duration for each view

    handler = new Handler();
    animatorRunnable =
        new Runnable() {
          int index = 0;

          @Override
          public void run() {
            if (index < views.size()) {
              View currentView = views.get(index);
              animator = ObjectAnimator.ofFloat(currentView, "translationY", -50f, 0f);
              animator.setDuration(duration);
              animator.setInterpolator(new LinearInterpolator());
              animator.start();

              index++;
              handler.postDelayed(this, delay); // Delay before next view animates
            } else {
              index = 0; // Reset index for looping
              handler.postDelayed(this, delay); // Restart the sequence
            }
          }
        };

    // Start the animation sequence
    handler.post(animatorRunnable);
  }

  private static void stopAnim() {
    if (scaleAnimator != null) {
      scaleAnimator.cancel();
      scaleAnimator.end();
      scaleAnimator.addListener(null);
      scaleAnimator = null;
    }
    if (animator != null) {
      animator.cancel();
      animator.end();
      handler.removeCallbacks(animatorRunnable);
      animator = null;
      handler = null;
      animatorRunnable = null;
    }
  }
}

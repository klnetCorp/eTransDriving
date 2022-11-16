// Generated code from Butter Knife. Do not modify!
package kr.co.klnet.aos.etransdriving;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PhotoFragment_ViewBinding implements Unbinder {
  private PhotoFragment target;

  private View view7f0900aa;

  @UiThread
  public PhotoFragment_ViewBinding(final PhotoFragment target, View source) {
    this.target = target;

    View view;
    target.previewLayout = Utils.findRequiredViewAsType(source, R.id.preview_layout, "field 'previewLayout'", LinearLayout.class);
    target.borderCamera = Utils.findRequiredView(source, R.id.border_camera, "field 'borderCamera'");
    target.resBorderSizeTV = Utils.findRequiredViewAsType(source, R.id.res_border_size, "field 'resBorderSizeTV'", TextView.class);
    target.seekBarHorizontal = Utils.findRequiredViewAsType(source, R.id.seekBarHorizontal, "field 'seekBarHorizontal'", SeekBar.class);
    target.seekBarVertical = Utils.findRequiredViewAsType(source, R.id.seekBarVertical, "field 'seekBarVertical'", VerticalSeekBar.class);
    view = Utils.findRequiredView(source, R.id.make_photo_button, "method 'makePhoto'");
    view7f0900aa = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.makePhoto();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    PhotoFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.previewLayout = null;
    target.borderCamera = null;
    target.resBorderSizeTV = null;
    target.seekBarHorizontal = null;
    target.seekBarVertical = null;

    view7f0900aa.setOnClickListener(null);
    view7f0900aa = null;
  }
}

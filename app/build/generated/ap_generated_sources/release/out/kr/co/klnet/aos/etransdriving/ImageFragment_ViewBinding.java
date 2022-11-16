// Generated code from Butter Knife. Do not modify!
package kr.co.klnet.aos.etransdriving;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ImageFragment_ViewBinding implements Unbinder {
  private ImageFragment target;

  @UiThread
  public ImageFragment_ViewBinding(ImageFragment target, View source) {
    this.target = target;

    target.layout_root = Utils.findRequiredViewAsType(source, R.id.layout_root, "field 'layout_root'", FrameLayout.class);
    target.resPhoto = Utils.findRequiredViewAsType(source, R.id.res_photo, "field 'resPhoto'", ImageView.class);
    target.resPhotoSize = Utils.findRequiredViewAsType(source, R.id.res_photo_size, "field 'resPhotoSize'", TextView.class);
    target.progressBar = Utils.findRequiredViewAsType(source, R.id.progressBar, "field 'progressBar'", ProgressBar.class);
    target.cloud_result_list = Utils.findRequiredViewAsType(source, R.id.cloud_result_list, "field 'cloud_result_list'", ListView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ImageFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.layout_root = null;
    target.resPhoto = null;
    target.resPhotoSize = null;
    target.progressBar = null;
    target.cloud_result_list = null;
  }
}

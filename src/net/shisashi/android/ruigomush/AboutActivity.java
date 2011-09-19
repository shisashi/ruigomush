/**
 * about画面
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        MovementMethod movementMethod = LinkMovementMethod.getInstance();

        {
            TextView aboutMicahHainlineLink = (TextView) findViewById(R.id.about_micah_hainline_link);
            aboutMicahHainlineLink.setMovementMethod(movementMethod);

            String url = getString(R.string.micah_hainline_url);
            String html = "licenced under CC-by-SA <a href=\"" + url + "\">Micah Hainline</a>";

            aboutMicahHainlineLink.setText(Html.fromHtml(html));
        }

        {
            TextView aboutStackoverflowLink = (TextView) findViewById(R.id.about_stackoverflow_link);
            aboutStackoverflowLink.setMovementMethod(movementMethod);

            String url = getString(R.string.rowlayout_url);
            String html = "<a href=\"" + url + "\">java - Line-breaking widget layout <br> for Android - Stack Overflow</a>";

            aboutStackoverflowLink.setText(Html.fromHtml(html));
        }
    }
}

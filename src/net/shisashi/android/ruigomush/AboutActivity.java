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
            String url = getString(R.string.wordnet_ja_url);
            String copyright1 = getString(R.string.wordnet_ja_copyright1);
            String copyright2 = getString(R.string.wordnet_ja_copyright2);
            String html = String.format("<a href=\"%s\">%s<br>%s</a>", url, copyright1, copyright2);

            TextView wordnetJaLink = (TextView) findViewById(R.id.wordnet_ja_link);
            wordnetJaLink.setMovementMethod(movementMethod);
            wordnetJaLink.setText(Html.fromHtml(html));
        }

        {
            String url = getString(R.string.micah_hainline_url);
            String html = "licenced under CC-by-SA <a href=\"" + url + "\">Micah Hainline</a>";

            TextView aboutMicahHainlineLink = (TextView) findViewById(R.id.about_micah_hainline_link);
            aboutMicahHainlineLink.setMovementMethod(movementMethod);
            aboutMicahHainlineLink.setText(Html.fromHtml(html));
        }

        {
            String url = getString(R.string.rowlayout_url);
            String html = "<a href=\"" + url + "\">java - Line-breaking widget layout <br> for Android - Stack Overflow</a>";

            TextView aboutStackoverflowLink = (TextView) findViewById(R.id.about_stackoverflow_link);
            aboutStackoverflowLink.setMovementMethod(movementMethod);
            aboutStackoverflowLink.setText(Html.fromHtml(html));
        }
    }
}

/**
 * ヘルプ画面
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;

public class HelpActivity extends Activity {
    private static final int MENU_ID_SEARCH = Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.help);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        ((WebView) findViewById(R.id.helpWebView)).loadUrl("file:///android_asset/readme.html");

        {
            OnClickListener onClick = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSearchRequested();
                }
            };
            findViewById(R.id.searchButton).setOnClickListener(onClick);
            findViewById(R.id.helpSearchButton).setOnClickListener(onClick);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_SEARCH, Menu.NONE, R.string.search).setIcon(android.R.drawable.ic_menu_search);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_SEARCH:
            onSearchRequested();
            return true;
        }
        return false;
    }

}

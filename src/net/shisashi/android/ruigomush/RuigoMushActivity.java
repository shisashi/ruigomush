/**
 * メイン画面
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asolutions.widget.RowLayout;

public class RuigoMushActivity extends Activity {
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    private static final String REPLACE_KEY = "replace_key";
    private static final int MENU_ID_DOWNLOAD_DIC = Menu.FIRST;
    private static final int MENU_ID_LONGCLICK_HINT = MENU_ID_DOWNLOAD_DIC + 1;
    private static final int MENU_ID_ABOUT = MENU_ID_DOWNLOAD_DIC + 2;
    private static final int MENU_ID_SEARCH = MENU_ID_DOWNLOAD_DIC + 3;
    private boolean isMushroom;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        setContentView(R.layout.main);
        dbHelper = new DBHelper();

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        if (!DBHelper.DB_FILE.exists()) {
            // 辞書がないのでDLを促す
            suggestDownloadDictionary();
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        Log.i("RUIGO", "onCreate:" + intent);

        // 検索ボタンクリック
        findViewById(R.id.searchButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });

        if (ACTION_INTERCEPT.equals(action)) {
            // マッシュルームアプリとして起動したので、その単語を検索する
            isMushroom = true;
            String replaceString = intent.getStringExtra(REPLACE_KEY);
            doSearchWithQuery(replaceString);
        }
        else if (Intent.ACTION_SEARCH.equals(action)) {
            // 検索から起動したので、その単語を検索する
            isMushroom = false;
            doSearchWithIntent(intent);
        }
        else {
            // 通常起動したときは検索を促す
            isMushroom = false;
            onSearchRequested();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("RUIGO", "onNewIntent: " + intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            doSearchWithIntent(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.closeDatabase();
    }

    @Override
    protected void onDestroy() {
        Log.i("RUIGO", "onDestroy");
        super.onDestroy();
    }

    private void doSearchWithIntent(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        Log.i("RUIGO", "doSearchWithIntent: " + intent);
        doSearchWithQuery(query);
    }

    /**
     * UIから値を取得し、検索処理を実行し、描画処理を呼び出す
     */
    private void doSearchWithQuery(String query) {
        if (query.length() == 0)
            return;

        List<Synset> searchResult = dbHelper.search(query);
        if (searchResult == null) {
            // 辞書がないのでDLを促す
            suggestDownloadDictionary();
        }
        else if (searchResult.size() == 0) {
            // 類語なし
            Toast.makeText(this, R.string.synset_not_found, Toast.LENGTH_SHORT).show();
        }
        else {
            // 検索結果あり
            // タイトルを変更するのは類語があったときのみ
            String appName = getString(R.string.app_name);
            setTitle(appName + " : " + query);
            showSearchResult(searchResult);
        }
    }

    /**
     * 検索結果を表示する
     * 
     * @param searchResult
     *            検索結果のリスト
     */
    private void showSearchResult(final List<Synset> searchResult) {
        ListView resultListView = (ListView) findViewById(R.id.resultListView);

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return searchResult.size();
            }

            @Override
            public Object getItem(int position) {
                return searchResult.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Synset synset = (Synset) getItem(position);

                LinearLayout layout = new LinearLayout(RuigoMushActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                TextView definitionView = new TextView(RuigoMushActivity.this);
                definitionView.setText(synset.definition);
                layout.addView(definitionView);
                RowLayout wordsLayout = new RowLayout(RuigoMushActivity.this, null);
                for (final String word : synset.words) {
                    wordsLayout.addView(createButton(word));
                }
                layout.addView(wordsLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return layout;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        resultListView.setAdapter(adapter);
    }

    /**
     * メニュー作成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_DOWNLOAD_DIC, Menu.NONE, R.string.menu_download_dic);
        menu.add(Menu.NONE, MENU_ID_LONGCLICK_HINT, Menu.NONE, R.string.longclick_hint);
        menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE, R.string.about).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(Menu.NONE, MENU_ID_SEARCH, Menu.NONE, R.string.search).setIcon(android.R.drawable.ic_menu_search);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * メニュー選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_DOWNLOAD_DIC:
            // 確認ダイアログを出して辞書更新
            new AlertDialog.Builder(this).setMessage(R.string.dic_download_confirm)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            downloadDictionary();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
            return true;
        case MENU_ID_ABOUT:
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        case MENU_ID_SEARCH:
            onSearchRequested();
            return true;
        case MENU_ID_LONGCLICK_HINT:
            // 何もしない
            return true;
        }
        return false;
    }

    /**
     * 類語が選択されたときの処理
     * 
     * @param text
     *            選択された類語
     */
    private void ruigoSelected(final String text) {
        Log.i("RUIGO", "ruigoSelected: " + text);
        if (isMushroom) {
            // マッシュルームに結果を返す
            Intent intent = new Intent();
            intent.putExtra(REPLACE_KEY, text);
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            // マッシュルームからでない
            // クリップボードにコピーして良いか確認メッセージを出し、コピーする
            new AlertDialog.Builder(this).setMessage(R.string.copy_ruigo_to_clipboard_title)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            clipboardManager.setText(text);
                            Toast.makeText(RuigoMushActivity.this, R.string.copied_ruigo_to_clipboard_message, Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
        }
    }

    /**
     * 辞書がないときにDLを促すメッセージを表示する
     */
    private void suggestDownloadDictionary() {
        new AlertDialog.Builder(this).setTitle(R.string.dic_alert_title).setMessage(R.string.dic_alert_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        downloadDictionary();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

    /**
     * 辞書をダウンロードする
     */
    private void downloadDictionary() {
        DownloadAsyncTask task = new DownloadAsyncTask(this);
        task.execute();
    }

    /**
     * 検索結果の単語を表示するボタンを作成する
     * 
     * @param word
     *            ボタンに表示する単語
     * @return 作成されたボタン
     */
    private Button createButton(final String word) {
        final Button b = new Button(RuigoMushActivity.this);
        b.setText(word);
        b.setOnLongClickListener(buttonOnLongClick);
        b.setOnClickListener(buttonOnClick);
        return b;
    }

    /**
     * 検索結果ボタン押下時の処理
     */
    private OnClickListener buttonOnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // 類語のボタン押下
            ruigoSelected(((Button) v).getText().toString());
        }
    };

    /**
     * 検索結果ボタン長押し時の処理
     */
    private OnLongClickListener buttonOnLongClick = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // 類語のボタン長押し。バイブして再検索
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(25);
            String word = ((Button) v).getText().toString();
            doSearchWithQuery(word);
            return true;
        }
    };
}

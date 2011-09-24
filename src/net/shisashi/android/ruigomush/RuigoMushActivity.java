/**
 * メイン画面
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.asolutions.widget.RowLayout;

public class RuigoMushActivity extends Activity {
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    private static final String REPLACE_KEY = "replace_key";
    private static final int MENU_ID_DOWNLOAD_DIC = Menu.FIRST;
    private static final int MENU_ID_LONGCLICK_HINT = MENU_ID_DOWNLOAD_DIC + 1;
    private static final int MENU_ID_ABOUT = MENU_ID_LONGCLICK_HINT + 2;
    private EditText edit;
    private boolean isMushroom;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        edit = (EditText) findViewById(R.id.queryEditText);
        dbHelper = new DBHelper();

        // edit で確定させたら自動で検索する
        edit.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                invokeSearch();
                return true;
            }
        });

        // 検索ボタンクリック
        findViewById(R.id.searchButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeSearch();
            }
        });

        if (!DBHelper.DB_FILE.exists()) {
            // 辞書がないのでDLを促す
            suggestDownloadDictionary();
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && ACTION_INTERCEPT.equals(action)) {
            // マッシュルームアプリとして起動したので、その単語を検索する
            isMushroom = true;
            String replaceString = intent.getStringExtra(REPLACE_KEY);
            edit.setText(replaceString);
            edit.setSelection(replaceString.length());
            invokeSearch();
        }
        else {
            // 通常起動した
            isMushroom = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.closeDatabase();
    }

    /**
     * UIから値を取得し、検索処理を実行し、描画処理を呼び出す
     */
    private void invokeSearch() {
        String query = edit.getText().toString();
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
            showSearchResult(searchResult);
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edit.getWindowToken(), 0);
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
        menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE, R.string.about);
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
            edit.setText(word);
            edit.setSelection(word.length());
            invokeSearch();
            return true;
        }
    };
}

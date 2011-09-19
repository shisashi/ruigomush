/**
 * 辞書のダウンロード処理
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import net.shisashi.android.ruigomush.R;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DownloadAsyncTask extends AsyncTask<Void, Integer, Void> {
    private ProgressDialog progressDialog;
    private Activity activity;

    public DownloadAsyncTask(Activity activity) {
        this.activity = activity;
        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        String message = activity.getString(R.string.progress_downloading);
        progressDialog.setTitle(message);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        // TODO ダウンロードして直接辞書のdbを上書きじゃなくて、一時ファイルにDLし終えたら上書きの方がいいんじゃないだろうか
        String urlString = "http://ruigomush.appspot.com/dictionary.db.gz";
        DBHelper.DB_DIRECTORY.mkdirs();

        try {
            HttpURLConnection http = (HttpURLConnection) new URL(urlString).openConnection();
            http.setRequestMethod("GET");
            http.connect();

            GZIPInputStream is = new GZIPInputStream(http.getInputStream());
            FileUtils.copyInputStreamToFile(is, DBHelper.DB_FILE);
            is.close();
        }
        catch (IOException e) {
            Log.e("RUIGO", "e", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
            Toast.makeText(this.activity, R.string.download_complete_message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCancelled() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onCancelled();
    }
}

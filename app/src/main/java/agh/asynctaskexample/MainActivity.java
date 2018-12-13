package agh.asynctaskexample;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity
{
	private ProgressDialog progressDialog;
	private EditText urlEdit;
	private EditText localFilenameEdit;
	public static final int progress_bar_type = 0;

	static class DownloadQueryData{
		public String url;
		public String localFilename;


		public DownloadQueryData(String url, String localFilename) {
			this.url = url;
			this.localFilename = localFilename;
		}
	}


	private void initProgressDialog(){
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Downloading file...");
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(100);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
	}


	private void initUrlEditListener(){
		urlEdit.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				int idx = charSequence.length() -1;
				while (charSequence.charAt(idx) != '/' && idx >= 0)
					--idx;
				if (idx >= 0)
					localFilenameEdit.setText(charSequence.toString().substring(idx + 1));
			}

			@Override
			public void afterTextChanged(Editable editable) { }
		});
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		urlEdit = findViewById(R.id.urlEdit);
		localFilenameEdit = findViewById(R.id.localFilenameEdit);
		initProgressDialog();
		initUrlEditListener();
	}


	public void onButtonDownloadClicked(View view) {
		new DownloadFileFromURL().execute(
				new DownloadQueryData(
						urlEdit.getText().toString(),
						localFilenameEdit.getText().toString()));
	}


	class DownloadFileFromURL extends AsyncTask<DownloadQueryData, Integer, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}


		@Override
		protected String doInBackground(DownloadQueryData... urls) {
			int count;
			try {
				URL url = new URL(urls[0].url);
				HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
				InputStream input = new BufferedInputStream(connection.getInputStream(), 8192);

				int lenghtOfFile = Integer.parseInt(connection.getHeaderField("content-length"));
				OutputStream output = new FileOutputStream(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
								+ "/" + urls[0].localFilename);

				byte data[] = new byte[4096];
				long total = 0;
				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress((int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();

			}
			catch (Exception e) {
				return e.getMessage();
			}
			return "File downloaded successfully";
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress(progress[0]);
		}


		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
		}

	}
}

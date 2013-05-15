package org.aribeiro.spike.tagwriter;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private NfcAdapter mAdapter;
	private Intent serviceIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.bt_stop).setOnClickListener(this);
		findViewById(R.id.bt_write).setOnClickListener(this);
		findViewById(R.id.bt_scan).setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		stopWriting();
		if (serviceIntent != null) {			
			stopService(serviceIntent);
		}
		serviceIntent = null;
		super.onPause();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bt_write:
			startWriting();
			break;

		case R.id.bt_stop:
			stopWriting();
			break;

		case R.id.bt_scan:
			scan();
			break;
		}

	}

	private void scan() {

		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");

				Map<String, String> parseDescriptor = parseDescriptor(contents);

				getTextView(R.id.txt_id).setText("");
				getTextView(R.id.txt_name).setText("");
				getTextView(R.id.txt_description).setText("");
				getTextView(R.id.txt_url).setText("");
				
				for (String key : parseDescriptor.keySet()) {

					if ("id".equalsIgnoreCase(key)) {
						getTextView(R.id.txt_id).setText(parseDescriptor.get("id"));

					} else if ("name".equalsIgnoreCase(key)) {
						getTextView(R.id.txt_name).setText(parseDescriptor.get("name"));

					} else if ("description".equalsIgnoreCase(key)) {
						getTextView(R.id.txt_description).setText(parseDescriptor.get("description"));

					} else if ("url".equalsIgnoreCase(key)) {
						getTextView(R.id.txt_url).setText(parseDescriptor.get("url"));

					}
				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	private Map<String, String> parseDescriptor(String descriptor) {
		if ((descriptor == null) || (descriptor.length() == 0)) {
			return null;
		}
		HashMap<String, String> result = new HashMap<String, String>();
		String[] tokens = descriptor.split("(?<!&#\\d{2,3});");

		for (String token : tokens) {
			String[] keyValue = token.split("=", 2);
			if (keyValue.length == 2) {
				result.put(keyValue[0], keyValue[1].replace("&#59;", ";"));
			}
		}
		return result;
	}

	private void startWriting() {

		changeStatus(false);

		findViewById(R.id.bt_write).setVisibility(View.GONE);
		findViewById(R.id.bt_stop).setVisibility(View.VISIBLE);

		PendingIntent pendingIntent = PendingIntent.getService(this, 0, getServiceIntent(), 0);

		IntentFilter sNdef = new IntentFilter();
		sNdef.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
		
		IntentFilter sNdef2 = new IntentFilter();
		sNdef2.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
		
		IntentFilter sNdef3 = new IntentFilter();
		sNdef3.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		
		IntentFilter[] intentFiltersArray = new IntentFilter[] { sNdef , sNdef2, sNdef3 };

		String[][] techListsArray = new String[][] { new String[] { NfcF.class.getName(), 
				MifareClassic.class.getName(),
				MifareUltralight.class.getName(),
				NfcA.class.getName(),
				NfcB.class.getName(),
				NfcF.class.getName(),
				NfcV.class.getName(),
				IsoDep.class.getName(),
				NdefFormatable.class.getName(),
				Ndef.class.getName()
				} };

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
		
		//Toast.makeText(this, "Started for " + ServiceWriter.content, Toast.LENGTH_SHORT).show();

	}

	private void stopWriting() {
		findViewById(R.id.bt_write).setVisibility(View.VISIBLE);
		findViewById(R.id.bt_stop).setVisibility(View.GONE);
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
			mAdapter = null;
		}
		if (serviceIntent != null)  {
			stopService(serviceIntent);
			serviceIntent = null;
		}
		changeStatus(true);
	}

	private void changeStatus(boolean isEnabled) {
		findViewById(R.id.txt_id).setEnabled(isEnabled);
		findViewById(R.id.txt_description).setEnabled(isEnabled);
		findViewById(R.id.txt_name).setEnabled(isEnabled);
		findViewById(R.id.txt_url).setEnabled(isEnabled);
		findViewById(R.id.bt_scan).setEnabled(isEnabled);
	}

	private String generateContent() {
		String id = getTextView(R.id.txt_id).getText().toString();
		String name = getTextView(R.id.txt_name).getText().toString();
		String desc = getTextView(R.id.txt_description).getText().toString();
		String url = getTextView(R.id.txt_url).getText().toString();
		String hash = getTextView(R.id.txt_hash).getText().toString();

		HashMap<String, String> map = new HashMap<String, String>();

		if (!TextUtils.isEmpty(id)) {
			map.put("id", id);

			if (!TextUtils.isEmpty(hash)) {
			    map.put("hash", hash);			    
			}
			
			if (!TextUtils.isEmpty(url)) {
				map.put("url", url);
			}
		}

		if (!TextUtils.isEmpty(name)) {
			map.put("name", name);
		}

		if (!TextUtils.isEmpty(desc)) {
			map.put("description", desc);
		}

		StringBuilder sb = new StringBuilder();
		int count = 0;
		int size = map.size();
		for (String key : map.keySet()) {
			sb.append(key).append("=").append(map.get(key));

			if (count < (size - 1)) {
				sb.append(";");
			}

			count++;
		}

		return sb.toString();

	}

	private TextView getTextView(int id) {
		return (TextView) findViewById(id);
	}

	private Intent getServiceIntent() {
		serviceIntent = new Intent(this, ServiceWriter.class);
		String generateContent = generateContent();
		ServiceWriter.content = generateContent;

		return serviceIntent;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("ST", "Down " + keyCode);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Log.d("ST", "Long " + keyCode);
		return super.onKeyLongPress(keyCode, event);
	}
	
	

}
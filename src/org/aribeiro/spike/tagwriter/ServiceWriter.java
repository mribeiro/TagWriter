package org.aribeiro.spike.tagwriter;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Service;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.IBinder;
import android.widget.Toast;

public class ServiceWriter extends Service {

	public static String content;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	String TAG = "SW";

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			Toast.makeText(this, "No intent", Toast.LENGTH_SHORT).show();
			return super.onStartCommand(intent, flags, startId);
		}

		Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		if (tag == null) {
			Toast.makeText(this, "No tag", Toast.LENGTH_SHORT).show();
			return super.onStartCommand(intent, flags, startId);
		}

		Ndef ndef = null;
		try {
			String payload = content;

			NdefRecord mimeRecord = new NdefRecord(
					NdefRecord.TNF_MIME_MEDIA,
					"android/appdescriptor".getBytes(Charset.forName("UTF-8")),
					new byte[0], payload.getBytes(Charset.forName("UTF-8")));

			NdefRecord[] records = { mimeRecord };
			NdefMessage message = new NdefMessage(records);

			// Get an instance of Ndef for the tag.
			ndef = Ndef.get(tag);

			if (hasSpace(message, ndef)) {
				// Enable I/O
				ndef.connect();
				
				// Write the message
				ndef.writeNdefMessage(message);
				
				Toast.makeText(this, "TAG written", Toast.LENGTH_SHORT).show();				
			}
			

		} catch (Exception ex) {
			Toast.makeText(this, "Could not write TAG", Toast.LENGTH_SHORT).show();

		} finally {

			if (ndef != null) {
				try {
					ndef.close();
				} catch (IOException e) {
				}
			}

		}
		return super.onStartCommand(intent, flags, startId);

	}

	private boolean hasSpace(NdefMessage msg, Ndef tag) {
		int maxSize = tag.getMaxSize();
		int size = msg.toByteArray().length;

		if (size > maxSize) {
			Toast.makeText(this, "The content is too big (" + size + "/" + maxSize + ")", Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}

	}

}

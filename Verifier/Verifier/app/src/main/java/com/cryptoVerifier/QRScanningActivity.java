package com.cryptoVerifier;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Naor on 03/01/2016.
 */
public abstract class QRScanningActivity extends AppCompatActivity {

    protected void requestQRString() {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            intent.putExtra("CHARACTER_SET", "ISO-8859-1");
            startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {

                String contents = data.getStringExtra("SCAN_RESULT");
                onQrCodeReceived(contents);
            }
            if (resultCode == RESULT_CANCELED) {
                onQrCodeReceived(null);
            }
        }
    }


    protected abstract void onQrCodeReceived(String qrString);
}

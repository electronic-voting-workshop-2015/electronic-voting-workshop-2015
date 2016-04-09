package ngapps.qrtostring;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MainActivity extends QRScanningActivity {

    public static final String NUM = "num";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = (Button) findViewById(R.id.button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  requestQRString();
               // onQrCodeReceived("test text");
            }
        });
    }

    @Override
    protected void onQrCodeReceived(String qrString) {
        String filename = "scan" + getNextScanNum() + ".txt";
        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Scans");


       // FileOutputStream outputStream;

        try {
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
//            fileDir.createNewFile();
//            //fileDir.createNewFile();
//
//            outputStream = new FileOutputStream(fileDir);
//            outputStream.write(qrString.getBytes());
//            outputStream.close();

            //FileWriter writer = new FileWriter(new File(fileDir, filename));
            Writer writer  = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(fileDir, filename)), "ISO-8859-1"));
            writer.append(qrString);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "string was stored at:" + fileDir.getAbsolutePath(), Toast.LENGTH_LONG).show();

    }




    public String getNextScanNum() {
        SharedPreferences settings = getSharedPreferences("Preferences", MODE_PRIVATE);
        //SharedPreferences.Editor prefEditor = settings.edit();
        int num = settings.getInt(NUM, -1);
        if (num == -1) {
            settings.edit().putInt(NUM, 1).commit();
        }

        int curNum = settings.getInt(NUM, 0);
        settings.edit().putInt(NUM, curNum + 1).commit();
        return String.valueOf(curNum);


    }
}

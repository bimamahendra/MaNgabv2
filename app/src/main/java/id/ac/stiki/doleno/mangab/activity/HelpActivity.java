package id.ac.stiki.doleno.mangab.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.model.User;
import id.ac.stiki.doleno.mangab.preference.AppPreference;

public class HelpActivity extends AppCompatActivity {
    private User user;

    private TextView tvOpening, tvMenuOne, tvMainOne, tvMainTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        user = AppPreference.getUser(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tbHelp);
        tvOpening = findViewById(R.id.tvOpening);
        tvMainOne = findViewById(R.id.tvMainOne);
        tvMainTwo = findViewById(R.id.tvMainTwo);
        tvMenuOne = findViewById(R.id.tvMenuOne);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (user.type.equalsIgnoreCase("mahasiswa")) {
            tvOpening.setText("In the main page, there are 2 menus that can be used by students:");
            tvMenuOne.setText("Scan QR Code");
            tvMainOne.setText("1.\t Used to scan the QR code given by the lecturer for the class.\n\n" +
                    "2.\tYou will be recorded into class attendance if the notification shows “Attended”.\n\n" +
                    "3.\tIf the lecturer wants to use a unique code to verify attendance, you can enter the unique code given in this menu.");
            tvMainTwo.setText("1.\tUsed to see a list of classes that have been encountered.");

        } else {
            tvOpening.setText("In the main page, there are 2 menus that can be used by lecturers:");
            tvMenuOne.setText("QR Code Generate");
            tvMainOne.setText("1.\t Used to create a QR code in every online or offline class.\n\n" +
                    "2.\tAfter filling in class data and class topics, a QR code will be raised which will be scanned by students or use a unique code that will be inputted by students.\n\n" +
                    "3.\tAfter the class ends then next to the attendance recapitulation page where you can change the status of students who are absent and also add notes for the class.\n\n" +
                    "4.\tAfter recapitulation, the data will be sent to the administration.");
            tvMainTwo.setText("1.\t Used to see a list of classes that have been encountered.\n\n" +
                    "2.\tIf the class has finished, and the attendance recapitulation has not been done. Then you can access the QR code again on the history page.");
        }

    }
}
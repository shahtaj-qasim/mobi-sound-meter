package com.mobi.soundmeter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Results extends AppCompatActivity {
    FirebaseFirestore fbStore;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
    Date date, currentDate;
    String formatted, currentFormatted;
    String zipCode, dbDate;
    String city;
    Map<String, Object> msg;
    String heading;
    double maximum, average, minimum, sum;
    DecimalFormat df = new DecimalFormat("#.##");

    TextView textHead1, textHead2, textSub1, textSub2, textHead3, textHead4, textHead5, textHead6,textHead7,
    textSub3, textSub4, textSub5, textSub6, textSub7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        textHead1=(TextView)findViewById(R.id.textView1);
        textHead2=(TextView)findViewById(R.id.textView2);
        textSub1=(TextView)findViewById(R.id.textViewSub1);
        textSub2=(TextView)findViewById(R.id.textViewSub2);
        textHead3=(TextView)findViewById(R.id.textView3);
        textSub3=(TextView)findViewById(R.id.textViewSub3);
        textHead4=(TextView)findViewById(R.id.textView4);
        textSub4=(TextView)findViewById(R.id.textViewSub4);
        textHead5=(TextView)findViewById(R.id.textView5);
        textSub5=(TextView)findViewById(R.id.textViewSub5);
        textHead6=(TextView)findViewById(R.id.textView6);
        textSub6=(TextView)findViewById(R.id.textViewSub6);
        textHead7=(TextView)findViewById(R.id.textView7);
        textSub7=(TextView)findViewById(R.id.textViewSub7);

        TextView textHeads[] = {textHead1, textHead2, textHead3, textHead4, textHead5, textHead6, textHead7};
        TextView textSubs[] = {textSub1, textSub2, textSub3, textSub4, textSub5, textSub6, textSub7};

                currentDate = new Date(System.currentTimeMillis());
                fbStore = FirebaseFirestore.getInstance();
                CollectionReference collectionRef = fbStore.collection("noiseCollection");
        for(int i=0; i<7; i++) {
            date = DateUtils.addDays(new Date(), -i);
            formatted = formatter.format(date);
            textHeads[i].setText(formatted + " -  No data provided");
            textSubs[i].setText("No data provided");


            int finalI = i;
            collectionRef.whereEqualTo("date", formatted).orderBy("average", Query.Direction.DESCENDING).limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    sum = (double) document.get("sum");
                                    average = (double) document.get("average");
                                    maximum = (double) document.get("maximum");
                                    minimum = (double) document.get("minimum");
                                    Log.d(TAG, "I am maximum average" + document.getId() + " => " + document.getData());
                                    String heading = document.get("date") + " - " + document.getString("postalCode") + ", " + document.getString("city");
                                    textHeads[finalI].setText(heading);
                                    textSubs[finalI].setText("Total: " + Double.valueOf(df.format(sum)) + " dB    "
                                            + "Maximum: " + Double.valueOf(df.format(maximum)) + " dB    "
                                            + "Average: " + Double.valueOf(df.format(average)) + " dB    "
                                            + "Minimum: " + Double.valueOf(df.format(minimum)) + " dB");
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

    }
}

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Results extends AppCompatActivity {
    FirebaseFirestore fbStore;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
    Date date;
    String formatted;
    String zipCode;
    String city;
    Map<String, Object> msg;
    String heading;

    TextView textHead1, textHead2, textSub1, textSub2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        textHead1=(TextView)findViewById(R.id.textView1);
        textHead2=(TextView)findViewById(R.id.textView2);
        textSub1=(TextView)findViewById(R.id.textViewSub1);
        textSub2=(TextView)findViewById(R.id.textViewSub2);

                date = new Date(System.currentTimeMillis());
                fbStore = FirebaseFirestore.getInstance();
                CollectionReference collectionRef = fbStore.collection("noiseCollection");
                formatted = formatter.format(date);
                textHead1.setText(formatted+ " -  No data provided");
                collectionRef.whereEqualTo("date",formatted).orderBy("average", Query.Direction.DESCENDING).limit(1)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        zipCode = document.getString("postalCode");
                                        city = document.getString("city");
                                        Log.d(TAG, "I am maximum average"+ document.getId() + " => " + document.getData()+ " "+zipCode);
                                        //msg  = document.getData();
                                         String heading1 =formatted+ " - " +zipCode+", "+city;
                                        textHead1.setText(heading1);
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                date = DateUtils.addDays(new Date(), -1);
                formatted = formatter.format(date);
                textHead2.setText(formatted+ " -  No data provided");
                collectionRef.whereEqualTo("date",formatted).orderBy("average", Query.Direction.DESCENDING).limit(1)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                            zipCode = document.getString("postalCode");
                                            city = document.getString("city");
                                            Log.d(TAG, "I am maximum average" + document.getId() + " => " + document.getData() + " " + zipCode);
                                            msg = document.getData();
                                            String heading2 = formatted + " - " + zipCode + ", " + city;
                                            textHead2.setText(heading2);
                                    }

                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });



     //   ListView simpleList;
//        String Results[] = {results[0], results[1], "Result 3", "Result 4", "Result 5", "Result 6"};
//        simpleList = (ListView)findViewById(R.id.simpleListView);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_list_view, R.id.textView, Results);
//        simpleList.setAdapter(arrayAdapter);
    }


}

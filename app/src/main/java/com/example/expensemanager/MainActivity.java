package com.example.expensemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.expensemanager.databinding.ActivityMainBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemsClickListener{
    ActivityMainBinding binding;
    private  ExpensesAdapter expensesAdapter;
    Intent intent;
    private long income=0,expense=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expensesAdapter=new ExpensesAdapter(this,this);
        binding.recycler.setAdapter(expensesAdapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        intent=new Intent(MainActivity.this,AddExpenseActivity.class);

        binding.addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("type","income");
                startActivity(intent);
            }
        });
        binding.addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("type","Expense");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please");
        progressDialog.setMessage("Wait");
        progressDialog.setCancelable(false);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            progressDialog.show();
            FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.cancel();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.cancel();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        income=0;expense=0;
        getData();
    }

    private void getData() {
        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .whereEqualTo("uid",FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        expensesAdapter.clear();
                        List<DocumentSnapshot> dsList=queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds:dsList){
                            ExpenseModel expenseModel=ds.toObject(ExpenseModel.class);
                            if (expenseModel.getType().equals("Income")){
                                long l = income + expenseModel.getAmount();
                            }else {
                                expense+=expenseModel.getAmount();
                            }
                            expensesAdapter.add(expenseModel);
                        }
                        setUpGraph();

                    }
                });
    }

    private void setUpGraph() {
        List<PieEntry> pieEntryList=new ArrayList<>();
        List<Integer> colorsList=new ArrayList<>();
        if (income!=0){
            pieEntryList.add(new PieEntry(income,"income"));
            colorsList.add(getResources().getColor(R.color.greenone));
        }
        if (expense!=0){
            pieEntryList.add(new PieEntry(expense,"expense"));
            colorsList.add(getResources().getColor(R.color.black));
        }
        PieDataSet pieDataSet=new PieDataSet(pieEntryList,String.valueOf(income-expense));
        pieDataSet.setColor(colorsList);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.white));
        pieData pieDat=new PieData(pieDataSet);

        binding.pieChart.setData(pieDat);
        binding.pieChart.invalidate();


    }

    @Override
    public void onClick(ExpenseModel expenseModel) {
        intent.putExtra("model",expenseModel);
        startActivity(intent);
    }
}


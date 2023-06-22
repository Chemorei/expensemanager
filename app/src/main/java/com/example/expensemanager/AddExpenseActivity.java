package com.example.expensemanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.expensemanager.databinding.ActivityAddExpenseBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity {
    ActivityAddExpenseBinding binding;
    private String type;
    private ExpenseModel expenseModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        type=getIntent().getStringExtra("type");
        expenseModel=(ExpenseModel)getIntent().getSerializableExtra("model");
        if (expenseModel!=null){
            type=expenseModel.getType();
            binding.amount.setText(String.valueOf(expenseModel.getAmount()));
            binding.category.setText(expenseModel.getCategory());
            binding.note.setText(expenseModel.getNote());
        }


        if (type.equals("income")){
            binding.incomeRadio.setChecked(true);
        }else{
            binding.expenseRadio.setChecked(true);
        }

        binding.incomeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="income";
            }
        });
        binding.expenseRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Expense";
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        if (expenseModel== null) {
            menuInflater.inflate(R.menu.add_menu,menu);
        }else {
            menuInflater.inflate(R.menu.update_menu,menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.saveExpense){
            if (type!=null){
                createExpense();
            }else {
                updateExpense();
            }
            return true;
        }
        if (id==R.id.deleteExpense){
            deleteExpense();
        }
        return false;
    }

    private void deleteExpense() {
        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseModel.getExpenseId())
                .delete();
        finish();
    }

    private void createExpense() {
        String expenseId= UUID.randomUUID().toString();
        String amount=binding.amount.getText().toString();
        String note=binding.note.getText().toString();
        String category=binding.category.getText().toString();

        boolean incomeChecked=binding.incomeRadio.isChecked();
        if (incomeChecked){
            type="Income";
        }else{
            type="Expense";
        }

        if(amount.trim().length()==0){
            binding.amount.setError("Empty");
            return;
        }
        ExpenseModel expenseModel=new ExpenseModel(expenseId,note,category,type,Long.parseLong(amount), Calendar.getInstance().getTimeInMillis(),FirebaseAuth.getInstance().getUid());

        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseId)
                .set(expenseModel);
        finish();

    }
    private void updateExpense() {

        String expenseId= expenseModel.getExpenseId();
        String amount=binding.amount.getText().toString();
        String note=binding.note.getText().toString();
        String category=binding.category.getText().toString();

        boolean incomeChecked=binding.incomeRadio.isChecked();
        if (incomeChecked){
            type="Income";
        }else{
            type="Expense";
        }

        if(amount.trim().length()==0){
            binding.amount.setError("Empty");
            return;
        }
        ExpenseModel model=new ExpenseModel(expenseId,note,category,type,Long.parseLong(amount), expenseModel.getTime(),
                FirebaseAuth.getInstance().getUid());

        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseId)
                .set(model);
        finish();

    }
}
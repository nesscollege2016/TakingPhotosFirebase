package tomerbu.edu.takingphotosfirebase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dev on 8/29/2016.
 */
public class FireImageAdapter extends
        RecyclerView.Adapter<FireImageAdapter.ImageViewHolder>{

    ArrayList<DataSnapshot> dataSnapshotArrayList = new ArrayList<>();
    ArrayList<DataSnapshot> dataSnapshotChildList = new ArrayList<>();
    Context context;
    LayoutInflater inflater;

    public FireImageAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().
                getReference().child("Recipes").
                child(uid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshotArrayList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    dataSnapshotArrayList.add(child);
                }
                //Done fetching the data
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    
    
    int indexForKey(String key){
        for (int i = 0; i < dataSnapshotChildList.size(); i++) {
            DataSnapshot child = dataSnapshotChildList.get(i);
            if (child.getKey().equals(key))
                return i;
        }
        throw new IllegalArgumentException();
    }

    private void initData(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().
                getReference().child("Recipes").
                child(uid);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                dataSnapshotChildList.add(dataSnapshot);
                notifyItemInserted(dataSnapshotChildList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int idx = indexForKey(dataSnapshot.getKey());
                dataSnapshotChildList.set(idx, dataSnapshot);
                notifyItemChanged(idx);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int idx = indexForKey(dataSnapshot.getKey());
                dataSnapshotChildList.remove(idx);
                notifyItemRemoved(idx);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        DataSnapshot snapshot = dataSnapshotArrayList.get(position);
        String uri = snapshot.getValue(String.class);
        Picasso.with(context).load(uri).into(holder.ivImageItem);
        holder.data = snapshot;
    }

    @Override
    public int getItemCount() {
        return dataSnapshotArrayList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView ivImageItem;
        CheckBox cbImageItem;
        DataSnapshot data;

        public ImageViewHolder(View itemView) {
            super(itemView);
            ivImageItem = (ImageView) itemView.findViewById(R.id.ivImageItem);
            cbImageItem = (CheckBox) itemView.findViewById(R.id.cbImageItem);
        }
    }
}

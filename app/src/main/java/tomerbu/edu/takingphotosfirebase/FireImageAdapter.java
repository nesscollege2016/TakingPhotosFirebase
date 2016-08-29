package tomerbu.edu.takingphotosfirebase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
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

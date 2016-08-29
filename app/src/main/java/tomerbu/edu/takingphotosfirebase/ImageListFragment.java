package tomerbu.edu.takingphotosfirebase;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImageListFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_image_list, container, false);
        //findViewById for the recycler
        RecyclerView rvImageList = (RecyclerView) v.findViewById(R.id.rvImageList);

        //Set the layout manager
        rvImageList.setLayoutManager(new GridLayoutManager(getContext(), 3));

        //set the adapter
        rvImageList.setAdapter(new FireImageAdapter(getContext()));
        return v;
    }

}

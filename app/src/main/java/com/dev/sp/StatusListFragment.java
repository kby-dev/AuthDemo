package com.dev.sp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.model.RequestModel;
import com.dev.authdemo.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.maps.model.LatLng;
import com.dev.sp.RequestViewHolder;


/**
 * Shows a Firestore‑backed list of requests filtered by status,
 * and (for “Pending”) further filtered to within 10 miles of the SP.
 */
public class StatusListFragment extends Fragment {
    private static final String ARG_STATUS = "arg_status";
    private String status;
    private FirestoreRecyclerAdapter<RequestModel, RequestViewHolder> adapter;
    private FirebaseFirestore db;

    public static StatusListFragment newInstance(String status) {
        StatusListFragment f = new StatusListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_STATUS, status);
        f.setArguments(b);
        return f;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        status = getArguments().getString(ARG_STATUS, "Pending");
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) view;
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ServiceProviderActivity act = (ServiceProviderActivity) getActivity();

        Query q = db.collection("Requests").whereEqualTo("status", status);
        if ("Pending".equalsIgnoreCase(status) && act != null && act.isOnline()) {
            LatLng loc = act.getProviderLocation();
            if (loc != null) {
                double miles = 10.0, latDiff = miles/69.0;
                double lonDiff = miles/(69.0*Math.cos(Math.toRadians(loc.latitude)));
                q = q.whereGreaterThanOrEqualTo("latitude", loc.latitude - latDiff)
                        .whereLessThanOrEqualTo("latitude",  loc.latitude + latDiff)
                        .whereGreaterThanOrEqualTo("longitude", loc.longitude - lonDiff)
                        .whereLessThanOrEqualTo("longitude",  loc.longitude + lonDiff);
            }
        }

        FirestoreRecyclerOptions<RequestModel> opts =
                new FirestoreRecyclerOptions.Builder<RequestModel>()
                        .setQuery(q, RequestModel.class)
                        .build();

        adapter = new FirestoreRecyclerAdapter<RequestModel, RequestViewHolder>(opts) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder,
                                            int pos, @NonNull RequestModel model) {
                holder.bind(model, db, pos, this, requireContext());
                // Optional: item click to open detail map
            }
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(
                    @NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_request_serviceprovider, parent, false);
                return new RequestViewHolder(v);
            }
        };

        rv.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}

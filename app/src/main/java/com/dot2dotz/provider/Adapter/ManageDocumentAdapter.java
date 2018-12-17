package com.dot2dotz.provider.Adapter;


import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dot2dotz.provider.Bean.Document;
import com.dot2dotz.provider.Helper.URLHelper;
import com.dot2dotz.provider.Model.DocumentResponse;
import com.dot2dotz.provider.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ManageDocumentAdapter extends RecyclerView.Adapter<ManageDocumentAdapter.MyViewHolder> {

    private Context context;
    private List<DocumentResponse> documentResponseList;
    private DocumentClickListener mDocumentClickListener;

    public ManageDocumentAdapter(Context mContext,
                                 List<DocumentResponse> mDocList) {
        this.context = mContext;
        this.documentResponseList = mDocList;
    }

    public void setList(List<DocumentResponse> list) {
        this.documentResponseList = list;
    }

    public void setManagedDocumentClickListener(DocumentClickListener
                                                        managedDocumentClickListener) {
        this.mDocumentClickListener = managedDocumentClickListener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.layout_doc_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final DocumentResponse documentResponse = documentResponseList.get(position);

        if (documentResponse != null && documentResponseList != null &&
                !documentResponseList.isEmpty()) {
            if (documentResponse.getUrl() != null &&
                    !documentResponse.getUrl().isEmpty()) {
                if(!documentResponse.getUrl().contains("file:///")) {
                    String url = URLHelper.base + "storage" + "/" + documentResponse.getUrl();
                    Picasso.with(context)
                            .load(url)
                            .placeholder(R.drawable.doc_placeholder)
                            .error(R.drawable.doc_placeholder)
                            .memoryPolicy(MemoryPolicy.NO_STORE)
                            .into(holder.mDocImage);
                } else {
                    holder.mDocImage.setImageURI(Uri.parse(documentResponse.getUrl()));
                }
            } else {
                holder.mDocImage.setImageResource(R.drawable.doc_placeholder);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDocumentClickListener.onManagedDocumentClick(documentResponse,position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return documentResponseList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mDocName;
        private ImageView mDocImage;

        public MyViewHolder(View itemView) {
            super(itemView);

            mDocImage = itemView.findViewById(R.id.manageDocumentImage);
            mDocName = itemView.findViewById(R.id.manageDocumentName);
        }
    }

    public interface DocumentClickListener {
        void onManagedDocumentClick(DocumentResponse document, int pos);
    }
}

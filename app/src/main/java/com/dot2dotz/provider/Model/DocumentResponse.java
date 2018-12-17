package com.dot2dotz.provider.Model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DocumentResponse implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("provider_id")
    @Expose
    private Integer providerId;
    @SerializedName("document_id")
    @Expose
    private String documentId;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("unique_id")
    @Expose
    private Integer uniqueId;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("expires_at")
    @Expose
    private String expiresAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    protected DocumentResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            providerId = null;
        } else {
            providerId = in.readInt();
        }
        documentId = in.readString();
        url = in.readString();
        if (in.readByte() == 0) {
            uniqueId = null;
        } else {
            uniqueId = in.readInt();
        }
        status = in.readString();
        expiresAt = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<DocumentResponse> CREATOR = new Creator<DocumentResponse>() {
        @Override
        public DocumentResponse createFromParcel(Parcel in) {
            return new DocumentResponse(in);
        }

        @Override
        public DocumentResponse[] newArray(int size) {
            return new DocumentResponse[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Integer uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
        if (providerId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(providerId);
        }
        parcel.writeString(documentId);
        parcel.writeString(url);
        if (uniqueId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(uniqueId);
        }
        parcel.writeString(status);
        parcel.writeString(expiresAt);
        parcel.writeString(createdAt);
        parcel.writeString(updatedAt);
    }
}

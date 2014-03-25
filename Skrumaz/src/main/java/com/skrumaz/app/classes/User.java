package com.skrumaz.app.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Paito Anderson on 2014-03-22.
 */
public class User {
    private long Oid;
    private String Name;
    private String Email;
    private byte[] Photo;

    @Override
    public String toString() {
        return String.valueOf(this.Name);
    }

    public User(String name) {
        this.Name = name;
    }

    public long getOid() {
        return Oid;
    }

    public void setOid(long oid) {
        Oid = oid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public byte[] getPhoto() {
        return Photo;
    }

    public Bitmap getPhotoBitmap() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Photo);
        return BitmapFactory.decodeStream(byteArrayInputStream);
    }

    public void setPhoto(byte[] photo) {
        Photo = photo;
    }

    public void setPhotoBitmap(Bitmap photo) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        Photo = byteArrayOutputStream.toByteArray();
    }
}

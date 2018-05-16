package com.apps.sayantan.chatapp;


class ItemDataModel {

    private String message, name, photoUrl;

    ItemDataModel(){}

    ItemDataModel(String message, String name, String photoUrl) {
        setMessage(message);
        setName(name);
        setPhotoUrl(photoUrl);
    }

    String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    String getPhotoUrl() {
        return photoUrl;
    }

    private void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

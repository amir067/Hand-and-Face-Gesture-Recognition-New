package com.mcs.FaceGestureApp.Api;

import com.google.firebase.database.core.view.Event;

import java.util.List;

public interface MyCallback {
    void onCallback(List<Event> eventList);
}

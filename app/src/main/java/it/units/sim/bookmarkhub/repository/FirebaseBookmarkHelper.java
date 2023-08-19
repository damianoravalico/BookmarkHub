package it.units.sim.bookmarkhub.repository;


import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.units.sim.bookmarkhub.R;
import it.units.sim.bookmarkhub.model.Bookmark;

public class FirebaseBookmarkHelper {
    public static final String BOOKMARKS_COLLECTION_NAME = "bookmarks";

    public static void addNewBookmark(String name, String url, String data, String category, BookmarkCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(BOOKMARKS_COLLECTION_NAME)
                .add(new Bookmark(FirebaseAuth.getInstance().getUid(), name, url, data, category))
                .addOnSuccessListener(r -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(R.string.add_bookmark_failure));
    }

    public static void fetchBookmarks(String categoryName, MutableLiveData<List<Bookmark>> bookmarksLiveData) {
        FirebaseFirestore.getInstance()
                .collection(BOOKMARKS_COLLECTION_NAME)
                .whereEqualTo("user_id",
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .whereEqualTo("category", categoryName)
                .orderBy("bookmark_name", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.d(FirebaseCategoryHelper.class.getName(), "Error while retrieve category entries");
                    }
                    if (value != null) {
                        bookmarksLiveData.postValue(value.getDocuments()
                                .stream()
                                .map(s1 -> s1.toObject(Bookmark.class))
                                .collect(Collectors.toList()));
                    }
                });
    }

    public static void deleteBookmark(Bookmark bookmark, BookmarkCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(BOOKMARKS_COLLECTION_NAME)
                .document(bookmark.id)
                .delete()
                .addOnSuccessListener(r -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(R.string.delete_bookmark_failure));
    }

    public static void modifyBookmark(Bookmark bookmark, BookmarkCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(BOOKMARKS_COLLECTION_NAME)
                .document(bookmark.id)
                .set(bookmark)
                .addOnSuccessListener(r -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(R.string.modify_bookmark_failure));
    }

    public interface BookmarkCallback {
        void onSuccess();

        void onError(int errorStringId);
    }

}

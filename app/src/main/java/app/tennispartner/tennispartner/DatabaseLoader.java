package app.tennispartner.tenispartner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import app.tennispartner.tenispartner.models.User;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.stream.IntStream;

public class DatabaseLoader {

    // Users

    private static String[] imena = {
            "John",
            "Mary",
            "Robert",
            "Nick",
            "Wayne",
            "Gregory",
            "Paul",
            "Bob",
            "Hanna",
            "Stephen"
    };

    private static String[] prezimena = {
            "Doe",
            "Poppins",
            "Henry",
            "Sander",
            "Gates",
            "Baily",
            "Tucker",
            "March",
            "Perch",
            "Apple"
    };
    private static String[] fbId = {
            "100034663716283",
            "100034553803561",
            "100034523895609",
            "100034400243694",
            "100034701723740",
            "100034626728826",
            "100034593280904",
            "100034548853919"
    };

    private static double[][] points = new double[][]{
            {44.868028, 13.866623},
            {44.908545, 13.992182},
            {44.940875, 14.022014},
            {44.852744, 13.893926},
            {44.895373, 13.817234},
            {44.959458, 13.854418},
            {45.145041, 13.900540},
            {45.233450, 13.946616},
            {45.335464, 14.451502},
            {45.821111, 16.103683},
            {45.806114, 15.910029}
    };

    private static String[] birthday = {
            "09/18/1990",
            "08/11/1958",
            "09/06/1977",
            "09/05/1998",
            "09/12/1968",
            "01/01/2000"
    };

    private static Long[] lastLogin = {
            System.currentTimeMillis(),
            1520357089000L,
            1551893089000L,
            1488821089000L
    };
    /*
    private static String[] provider = {
            "facebook.com",
            null,
            "google.oom"
    };*/

    // Games

    private static Long[] time = {
            1539972660L,
            1546687940L,
            1539985768L,
            1539957648L
    };
    private static Long[] duration = {
            323L,
            436L,
            1534L,
            3245L
    };


    public static void loadDatabase() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        CollectionReference geoFirestoreRef = firestore.collection("users");
        GeoFirestore geoFirestore = new GeoFirestore(geoFirestoreRef);

        IntStream.range(1, 10).forEach(i -> {

            String im = imena[i % imena.length];
            String prez = prezimena[i % prezimena.length];
            String fb = fbId[i % fbId.length];
            String b = birthday[i % birthday.length];
            String avatarUrl = String.format(User.FB_AVATAR, fb);
            double[] l = points[i % points.length];
            long last = lastLogin[i % lastLogin.length];

            User u = new User("+3858603928", im, prez, "male", b, "facebook.com", fb, avatarUrl, last);
            /*IntStream.range(1, 10).forEach(j -> {
                Long tm = time[j % time.length];
                Long dr = duration[j % duration.length];
                Game game = new Game("Verudela", tm, dr, "6:7 5:6 4:6");
                u.getGames().add(game);
                game.getUsers().add(new User((long) 564, fb, "+3853243256465", im, prez, i, "male", lat, lon));
            });*/
            firestore.collection("users").add(u).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    String docRef = documentReference.getId();
                    geoFirestore.setLocation(docRef, new GeoPoint(l[0], l[1]));


                    //documentReference.collection("games")
                }
            });
        });
    }

}

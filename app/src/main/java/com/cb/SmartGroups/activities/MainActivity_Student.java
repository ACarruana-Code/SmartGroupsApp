package com.cb.SmartGroups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.fragments.student.GroupalChat;
import com.cb.SmartGroups.fragments.student.Interactivity;
import com.cb.SmartGroups.fragments.student.Files;
import com.cb.SmartGroups.fragments.student.Home;
import com.cb.SmartGroups.fragments.student.Map;
import com.cb.SmartGroups.utils.dialogs.commondialogs.SelectDisplayedCourse;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * The main activity for the student.
 *
 * @author Martín Mateos Sánchez and Adrián Carruana Martín
 */
public class MainActivity_Student extends AppCompatActivity implements SelectDisplayedCourse.onSelectedCourse {

    // Firebase
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    // Selected course and subject
    private String selectedCourse;
    private String selectedSubject;

    private MenuItem menuItem;

    // Views
    TextView noCourseSelected;
    FrameLayout fragmentContainer;
    BottomNavigationView bottomNavigationView;

    // Toolbar
    Toolbar toolbar;
    String name; // TODO: Just for test

    // Map
    private ProximityObserver proximityObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_student);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Toolbar
        toolbar = findViewById(R.id.topAppBar);

        // Views
        noCourseSelected = findViewById(R.id.noCourseSelected);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (selectedCourse == null || selectedSubject == null){
            noCourseSelected.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
            bottomNavigationView.setVisibility(View.GONE);
        }

        // ****** Map *******
        /*bottomNavigationView.getMenu().findItem(R.id.nav_student_Map).setVisible(false);

        EstimoteCloudCredentials cloudCredentials =
                new EstimoteCloudCredentials("attendance-2gf", "16fc1b780a50d5f664ed3509948ac053");

        this.proximityObserver =
                new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
                        .onError(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "proximity observer error: " + throwable);
                                return null;
                            }
                        })
                        .withBalancedPowerMode()
                        .build();

        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("classroom")
                .inNearRange()
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext context) {
                        String classroomOwner = context.getAttachments().get("classroom");
                        Log.d("app", "Welcome to " + classroomOwner + "'s classroom");
                        bottomNavigationView.getMenu().findItem(R.id.nav_student_Map).setVisible(true);
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext context) {
                        Log.d("app", "Bye bye, come again!");
                        bottomNavigationView.getMenu().findItem(R.id.nav_student_Map).setVisible(false);
                        return null;
                    }
                })
                .build();
*/
        // ****** END - Map *******

        toolbar.setTitle("Smart Groups");
        setSupportActionBar(toolbar);

        // Bottom navigation management
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(R.id.nav_student_home);
    }

    // Static interface to create the fragment associated with the pressed item on the BottomNavigationView
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        this.menuItem = item;
        Fragment selectedFragment;

        int itemId = item.getItemId();

        if (itemId == R.id.nav_student_interactivity) {
            selectedFragment = new Interactivity(selectedCourse, selectedSubject);
        } else if (itemId == R.id.nav_student_groups) {
            selectedFragment = new GroupalChat(selectedCourse, selectedSubject);
        } else if (itemId == R.id.nav_student_home) {
            selectedFragment = new Home(selectedCourse, selectedSubject);
        } else if (itemId == R.id.nav_student_files) {
            selectedFragment = new Files(selectedCourse, selectedSubject);
        } else if (itemId == R.id.nav_student_Map) {
            selectedFragment = new Map();
        } else {
            selectedFragment = new Home(selectedCourse, selectedSubject);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();

        return true;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.topappbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.select_course_subject){
            HashMap<String, ArrayList<String>> detail = new HashMap<String, ArrayList<String>>();
            SelectDisplayedCourse dialog = new SelectDisplayedCourse(detail);
            populateCoursesListAndShow(detail, dialog);
        } else if (item.getItemId() == R.id.menu_logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectedCourseChange(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;

        noCourseSelected.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.VISIBLE);

        navListener.onNavigationItemSelected(menuItem); // Update fragments with the new info
    }

    public void populateCoursesListAndShow(HashMap<String, ArrayList<String>> detail, SelectDisplayedCourse dialog) {
        fStore
                .collection("CoursesOrganization")
                .whereArrayContains("allParticipantsIDs", fAuth.getUid())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ArrayList<String> subjectNames = new ArrayList<String>();
                detail.put(document.getId(), subjectNames);

                fStore.collection("CoursesOrganization").document(document.getId())
                        .collection("Subjects").whereArrayContains("studentIDs", fAuth.getUid())
                        .get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (QueryDocumentSnapshot document1 : queryDocumentSnapshots1) {
                        subjectNames.add(document1.getId());
                    }
                });

            }
            dialog.show(getSupportFragmentManager(), "dialog");
        });
    }

}
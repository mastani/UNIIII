package mobina.com.uniiii.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mobina.com.uniiii.R;
import mobina.com.uniiii.Utility.ApplicationController;
import mobina.com.uniiii.Utility.Utilies;
import mobina.com.uniiii.abstracts.User;
import mobina.com.uniiii.adapter.RequestsAdapter;

public class RequestsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listV;
    RequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        listV = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        Utilies.requestsUsers.clear();
        adapter = new RequestsAdapter(Utilies.requestsUsers);
        listV.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        fetchRequests();
                                    }
                                }
        );

    }

    @Override
    public void onRefresh() {
        fetchRequests();
    }

    private void fetchRequests() {
        swipeRefreshLayout.setRefreshing(true);

        String URL = Utilies.URL + "loadRequests.php";
        StringRequest req = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject mainObject = new JSONObject(response);
                            if (mainObject.has("success") && mainObject.getBoolean("success")) {
                                JSONArray usersArray = mainObject.getJSONArray("users");
                                Utilies.requestsUsers.clear();

                                for (int i = 0; i < usersArray.length(); i++) {
                                    JSONObject user = usersArray.getJSONObject(i);
                                    int id = user.getInt("id");
                                    String name = user.getString("name");
                                    String email = user.getString("email");
                                    String mobile = user.getString("mobile");
                                    String latitude = user.getString("latitude");
                                    String longitude = user.getString("longitude");
                                    String update_time = user.getString("update_time");

                                    Utilies.requestsUsers.add(new User(id, name, email, mobile, latitude, longitude, update_time));
                                }
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {

                        }

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", Utilies.me.getEmail());
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        ApplicationController.getInstance().addToRequestQueue(req);
    }
}

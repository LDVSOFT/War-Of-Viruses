package net.ldvsoft.warofviruses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Activity which displays all the played finished games that stored locally.
 * User can select some game to replay it
 */
public class GameHistoryActivity extends AppCompatActivity {
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView recycler = (RecyclerView) findViewById(R.id.games);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this);
        recycler.setAdapter(adapter);
    }

    private class GameHistoryLoader extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> gameHistory = null;
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (gameHistory == null) {
                return;
            }
            LinearLayout layout = (LinearLayout) findViewById(R.id.history_layout);
            layout.removeAllViewsInLayout();
            for (String game : gameHistory) {
                String[] data = game.split(";");
                Button button = new Button(GameHistoryActivity.this);
                button.setText(data[1]);
                final long id = Long.parseLong(data[0]);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GameHistoryActivity.this, GameActivityReplay.class);
                        intent.putExtra(WoVPreferences.REPLAY_GAME_ID, id);
                        startActivity(intent);
                    }
                });
                layout.addView(button);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            gameHistory = DBOpenHelper.getInstance(GameHistoryActivity.this).getGameHistory();

            return null;
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        //new GameHistoryLoader().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private Activity context;

    public MyAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_game_relpay, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.figure.setImageDrawable(BoardCellButton.cellEmpty);
        holder.opponent.setText("KAPPA");
        holder.result.setText("DRAW");
        Date date = new GregorianCalendar(2016, 9, 1, 15, 35).getTime();
        holder.date.setText(DateFormat.getDateFormat(context).format(date));
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private BoardCellButton figure;
        private TextView opponent;
        private TextView result;
        private TextView date;

        private ViewHolder(View view) {
            super(view);
            figure = (BoardCellButton) view.findViewById(R.id.figure);
            opponent = (TextView) view.findViewById(R.id.opponent);
            result = (TextView) view.findViewById(R.id.result);
            date = (TextView) view.findViewById(R.id.date);
        }
    }
}


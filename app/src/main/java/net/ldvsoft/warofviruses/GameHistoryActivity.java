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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ldvsoft.warofviruses.GameLogic.GameState;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import static net.ldvsoft.warofviruses.GameLogic.GameState.*;

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

        adapter.notifyDataSetChanged();
    }

    private class GameHistoryLoader extends AsyncTask<Void, Void, Void> {
        private ArrayList<Game> gameHistory = null;

        @Override
        protected void onPostExecute(Void aVoid) {
            if (gameHistory == null) {
                return;
            }
            adapter.fetchData(/*FIXME*/0, gameHistory);
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
        new GameHistoryLoader().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new GameHistoryLoader().execute();
                return true;
            default:
                return false;
        }
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private long userId;
    private ArrayList<Game> data;
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
        Game game = data.get(position);
        switch (game.getMineFigure(userId)) {
            case NONE:
                holder.opponent.setText(context.getString(R.string.GAME_LOCAL));
                holder.figure.setImageDrawable(BoardCellButton.cellEmpty);
                switch (game.getGameState()) {
                    case CROSS_WON:
                        holder.result.setText(context.getString(R.string.GAME_CROSS_WON));
                        break;
                    case ZERO_WON:
                        holder.result.setText(context.getString(R.string.GAME_ZERO_WON));
                        break;
                    case DRAW:
                        holder.result.setText(context.getString(R.string.GAME_DRAW));
                        break;
                }
                break;
            case CROSS:
                holder.opponent.setText(game.getZeroPlayer().getName());
                switch (game.getGameState()) {
                    case CROSS_WON:
                        holder.figure.setImageDrawable(BoardCellButton.cellCross);
                        holder.result.setText(context.getString(R.string.GAME_WON));
                        break;
                    case ZERO_WON:
                        holder.figure.setImageDrawable(BoardCellButton.cellCrossDead);
                        holder.result.setText(context.getString(R.string.GAME_LOST));
                        break;
                    case DRAW:
                        holder.figure.setImageDrawable(BoardCellButton.cellEmpty);
                        holder.result.setText(context.getString(R.string.GAME_DRAW));
                }
                break;
            case ZERO:
                holder.opponent.setText(game.getCrossPlayer().getName());
                switch (game.getGameState()) {
                    case CROSS_WON:
                        holder.figure.setImageDrawable(BoardCellButton.cellZeroDead);
                        holder.result.setText(context.getString(R.string.GAME_LOST));
                        break;
                    case ZERO_WON:
                        holder.figure.setImageDrawable(BoardCellButton.cellZero);
                        holder.result.setText(context.getString(R.string.GAME_WON));
                        break;
                    case DRAW:
                        holder.figure.setImageDrawable(BoardCellButton.cellEmpty);
                        holder.result.setText(context.getString(R.string.GAME_DRAW));
                }
                break;
        }
        Date date = new GregorianCalendar(2016, 9, 1, 15, 35).getTime();
        holder.date.setText(DateFormat.getDateFormat(context).format(date));
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getGameId();
    }

    public void fetchData(long userId, ArrayList<Game> data) {
        this.userId = userId;
        this.data = data;
        notifyDataSetChanged();
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


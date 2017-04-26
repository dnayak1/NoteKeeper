package dhiraj.com.realm;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.RealmResults;

public class GridRecyclerAdapter extends RecyclerView.Adapter<GridRecyclerAdapter.GridRecyclerViewHolder> {
//    ArrayList<Note> arrayListGrid=new ArrayList<>();
    OrderedRealmCollection<Note> arrayListGrid;
    Context mContext;
    private IGridListener gridListener;
    SimpleDateFormat simpleDateFormat,simpleDateFormatNewFormat;
    String dateString,outputDateString,stringPrettyTime;
    Date inputDate,outputDate;
    PrettyTime prettyTime;


    public GridRecyclerAdapter(Context mContext, OrderedRealmCollection<Note> arrayListGrid, IGridListener gridListener) {
        this.arrayListGrid = arrayListGrid;
        this.mContext = mContext;
        this.gridListener = gridListener;

    }

    @Override
    public GridRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(mContext);
        View view= layoutInflater.inflate(R.layout.grid_item_layout,parent,false);
        GridRecyclerAdapter.GridRecyclerViewHolder gridRecyclerViewHolder=new GridRecyclerAdapter.GridRecyclerViewHolder(view);
        return gridRecyclerViewHolder;
    }

    @Override
    public void onBindViewHolder(GridRecyclerViewHolder holder, int position) {

        final Note note=arrayListGrid.get(position);
        holder.textViewShowNote.setText(note.getNoteName());
        if(note.getNotePriority()==2){
            holder.textViewShowPriority.setText("Low");
        }
        else if(note.getNotePriority()==1){
            holder.textViewShowPriority.setText("Medium");
        }
        else if(note.getNotePriority()==0){
            holder.textViewShowPriority.setText("High");
        }
        simpleDateFormat=new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
        simpleDateFormatNewFormat=new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        dateString=note.getNoteUpdateDate().toString();
        try {
            inputDate=simpleDateFormat.parse(dateString);
            outputDateString=simpleDateFormatNewFormat.format(inputDate);
            outputDate=simpleDateFormatNewFormat.parse(outputDateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        prettyTime=new PrettyTime();
        stringPrettyTime=prettyTime.format(outputDate);
        holder.textViewShowTime.setText(stringPrettyTime);
        if(note.getNoteStatus()==0){
            holder.checkBox.setChecked(false);
        }
        else if(note.getNoteStatus()==1){
            holder.checkBox.setChecked(true);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridListener.changeStatus(note);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                gridListener.deleteNote(note);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayListGrid.size();
    }

    public static class GridRecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView textViewShowNote;
        TextView textViewShowPriority;
        TextView textViewShowTime;
        CheckBox checkBox;
        public GridRecyclerViewHolder(View itemView) {
            super(itemView);
            textViewShowNote= (TextView) itemView.findViewById(R.id.textViewShowNote);
            textViewShowPriority= (TextView) itemView.findViewById(R.id.textViewPriority);
            textViewShowTime= (TextView) itemView.findViewById(R.id.textViewTime);
            checkBox= (CheckBox) itemView.findViewById(R.id.checkBox);
        }
    }

    interface IGridListener
    {
        void deleteNote(Note note);
        void changeStatus(Note note);
    }
}

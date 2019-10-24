/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.custom.swiperefreshlist;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;

import de.domjos.unitrackerlibrary.model.ListObject;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;

public class RecyclerAdapter extends Adapter<RecyclerAdapter.RecycleViewHolder> {
    private ArrayList<ListObject> data;
    private View.OnClickListener mClickListener;
    private RecyclerView recyclerView;
    private int menuId = -1, noEntryItem = -1;
    private Activity activity;
    private String currentTitle;
    private boolean showCheckBoxes;
    private SwipeRefreshDeleteList.ReloadListener reloadListener;
    private LinearLayout controls;

    class RecycleViewHolder extends ViewHolder implements View.OnCreateContextMenuListener {
        private TextView mTitle, mSubTitle;
        private CheckBox chkSelector;
        private ImageView ivIcon;

        RecycleViewHolder(View itemView) {
            super(itemView);

            boolean scroll = MainActivity.GLOBALS.getSettings(itemView.getContext()).isScrollList();

            mTitle = itemView.findViewById(R.id.lblTitle);
            mTitle.setSelected(scroll);
            mSubTitle = itemView.findViewById(R.id.lblSubTitle);
            mSubTitle.setSelected(scroll);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            chkSelector = itemView.findViewById(R.id.chkSelector);
            chkSelector.setChecked(false);

            itemView.setOnCreateContextMenuListener(this);

            if(menuId==-1) {
                itemView.setOnLongClickListener(view -> {
                    showCheckBoxes = !showCheckBoxes;
                    controls.setVisibility(showCheckBoxes ? View.VISIBLE : View.GONE);
                    if(reloadListener!=null) {
                        reloadListener.onReload();
                        for(int i = 0; i<=data.size()-1; i++) {
                            data.get(i).setSelected(false);
                            chkSelector.setChecked(false);
                        }
                    }
                    return true;
                });
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (menuId != -1) {
                currentTitle = mTitle.getText().toString();
                menu.add(R.string.sys_multiple).setOnMenuItemClickListener(menuItem -> {
                    showCheckBoxes = !showCheckBoxes;
                    controls.setVisibility(showCheckBoxes ? View.VISIBLE : View.GONE);
                    if(reloadListener!=null) {
                        reloadListener.onReload();
                        for(int i = 0; i<=data.size()-1; i++) {
                            data.get(i).setSelected(false);
                            chkSelector.setChecked(false);
                        }
                    }
                    return true;
                });
                MenuInflater inflater = activity.getMenuInflater();
                inflater.inflate(menuId, menu);
            }
        }
    }

    RecyclerAdapter(RecyclerView recyclerView, Activity activity, LinearLayout controls) {
        this.data = new ArrayList<>();
        this.recyclerView = recyclerView;
        this.activity = activity;
        this.controls = controls;
    }

    void reload(SwipeRefreshDeleteList.ReloadListener reloadListener) {
        this.reloadListener = reloadListener;
    }

    public ListObject getObject() {
        if(this.currentTitle!=null) {
            if (!this.currentTitle.isEmpty()) {
                for (ListObject listObject : data) {
                    if (listObject.getDescriptionObject().getTitle().equals(this.currentTitle)) {
                        return listObject;
                    }
                }
            }
        }
        return null;
    }

    @Override
    @NonNull
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new RecycleViewHolder(itemView);
    }

    void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

    void onSwipeListener(SwipeToDeleteCallback callback) {
        new ItemTouchHelper(callback).attachToRecyclerView(this.recyclerView);
    }

    void setContextMenu(int menuId) {
        this.menuId = menuId;
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        if(data!=null) {
            if(data.get(position)!=null) {
                if(data.get(position).getDescriptionObject()!=null) {
                    holder.mTitle.setText(data.get(position).getDescriptionObject().getTitle());
                    holder.mSubTitle.setText(data.get(position).getDescriptionObject().getDescription());
                    holder.ivIcon.setImageDrawable(data.get(position).getIcon());
                    holder.itemView.setOnClickListener(view -> {
                        if (mClickListener != null) {
                            mClickListener.onClick(view);
                        }
                    });
                    holder.chkSelector.setChecked(false);
                    holder.chkSelector.setVisibility(this.showCheckBoxes ? View.VISIBLE : View.GONE);
                    holder.chkSelector.setOnCheckedChangeListener((compoundButton, b) -> data.get(position).setSelected(b));
                }
            }
        }
    }

    public ListObject getItem(int position) {
        return data.get(position);
    }

    public int getItemPosition(ListObject listObject) {
        try {
            int position  = 0;
            for(ListObject obj : data) {
                if(obj.getDescriptionObject().getTitle().equals(listObject.getDescriptionObject().getTitle())) {
                    return position;
                }
                position++;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void deleteItem(int position) {
        this.data.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        int size = data.size();
        if (size > 0) {
            data.subList(0, size).clear();
            notifyItemRangeRemoved(0, size);
        }

        DescriptionObject descriptionObject = new DescriptionObject();
        descriptionObject.setTitle(this.activity.getString(R.string.messages_no_item));
        ListObject obj = new ListObject(this.activity, null, descriptionObject);
        this.data.add(obj);
        this.noEntryItem = this.data.indexOf(obj);
    }

    public void add(ListObject object) {
        if (this.noEntryItem != -1) {
            this.data.remove(this.noEntryItem);
            this.noEntryItem = -1;
        }

        data.add(object);

        synchronized (this) {
            notifyDataSetChanged();
        }
    }
}